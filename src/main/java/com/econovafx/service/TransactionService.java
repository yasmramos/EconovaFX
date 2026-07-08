package com.econovafx.service;

import com.econovafx.model.*;
import com.econovafx.repository.AccountRepository;
import com.econovafx.repository.TransactionRepository;
import com.econovafx.security.RequiresTenant;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing transactions with audit logging
 */
@Component
@RequiresTenant
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;
    
    @Inject
    public TransactionService(TransactionRepository transactionRepository,
                             AccountRepository accountRepository,
                             AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }
    
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public Optional<Transaction> getTransactionByNumber(String number) {
        return transactionRepository.findByNumber(number);
    }
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByDateRange(startDate, endDate);
    }
    
    public List<Transaction> getPostedTransactions() {
        return transactionRepository.findPostedTransactions();
    }
    
    public List<Transaction> getUnpostedTransactions() {
        return transactionRepository.findUnpostedTransactions();
    }
    
    public List<Transaction> searchTransactions(String searchTerm) {
        return transactionRepository.searchByDescription(searchTerm);
    }
    
    /**
     * Create a new transaction with double-entry bookkeeping
     */
    public Transaction createTransaction(Transaction transaction, List<TransactionEntryData> entries, String username) {
        validateTransaction(transaction, entries);
        
        if (transaction.getNumber() == null || transaction.getNumber().trim().isEmpty()) {
            String nextNumber = generateTransactionNumber();
            transaction.setNumber(nextNumber);
        }
        
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (TransactionEntryData entryData : entries) {
            Account account = accountRepository.findById(entryData.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Account not found: " + entryData.getAccountId()));
            
            TransactionEntry entry = new TransactionEntry();
            entry.setAccount(account);
            entry.setDebitAmount(entryData.getDebitAmount());
            entry.setCreditAmount(entryData.getCreditAmount());
            entry.setDescription(entryData.getDescription());
            
            transaction.addEntry(entry);
            
            totalDebit = totalDebit.add(entryData.getDebitAmount());
            totalCredit = totalCredit.add(entryData.getCreditAmount());
        }
        
        if (totalDebit.compareTo(totalCredit) != 0) {
            String errorMsg = "Transaction is not balanced. Debit: " + totalDebit + ", Credit: " + totalCredit;
            auditService.logFailure(username, AuditLog.OperationType.CREATE, "Transaction", null, 
                                   "Create transaction - validation failed", errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        transaction.setTotalDebit(totalDebit);
        transaction.setTotalCredit(totalCredit);
        transaction.setIsPosted(false);
        
        Transaction saved = transactionRepository.save(transaction);
        logger.info("Transaction created: {}", saved.getNumber());
        
        // Audit log
        auditService.logWithValues(username, AuditLog.OperationType.CREATE, "Transaction", 
                                  saved.getId(), "Created transaction: " + saved.getNumber(),
                                  null, buildTransactionJson(saved));
        
        return saved;
    }
    
    /**
     * Create transaction without audit (for backward compatibility)
     */
    public Transaction createTransaction(Transaction transaction, List<TransactionEntryData> entries) {
        return createTransaction(transaction, entries, "system");
    }
    
    /**
     * Post a transaction (apply to account balances)
     */
    public Transaction postTransaction(Long transactionId, String username) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + transactionId));
        
        if (transaction.getIsPosted()) {
            auditService.logFailure(username, AuditLog.OperationType.PUBLISH_TRANSACTION, "Transaction", 
                                   transactionId, "Post transaction - already posted", "Transaction already posted");
            throw new IllegalStateException("Transaction already posted");
        }
        
        if (!transaction.isBalanced()) {
            auditService.logFailure(username, AuditLog.OperationType.PUBLISH_TRANSACTION, "Transaction", 
                                   transactionId, "Post transaction - not balanced", "Transaction is not balanced");
            throw new IllegalStateException("Transaction is not balanced");
        }
        
        for (TransactionEntry entry : transaction.getEntries()) {
            Account account = entry.getAccount();
            
            BigDecimal balanceChange = BigDecimal.ZERO;
            AccountType type = account.getType();
            
            if (entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (type == AccountType.ASSET || type == AccountType.EXPENSE) {
                    balanceChange = entry.getDebitAmount();
                } else {
                    balanceChange = entry.getDebitAmount().negate();
                }
            }
            
            if (entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (type == AccountType.LIABILITY || type == AccountType.EQUITY || 
                    type == AccountType.REVENUE) {
                    balanceChange = entry.getCreditAmount();
                } else {
                    balanceChange = entry.getCreditAmount().negate();
                }
            }
            
            account.setBalance(account.getBalance().add(balanceChange));
            accountRepository.update(account);
        }
        
        transaction.setIsPosted(true);
        transactionRepository.update(transaction);
        
        logger.info("Transaction posted: {}", transaction.getNumber());
        
        // Audit log
        auditService.logSuccess(username, AuditLog.OperationType.PUBLISH_TRANSACTION, "Transaction",
                               transactionId, "Posted transaction: " + transaction.getNumber());
        
        return transaction;
    }
    
    /**
     * Post transaction without audit (for backward compatibility)
     */
    public Transaction postTransaction(Long transactionId) {
        return postTransaction(transactionId, "system");
    }
    
    /**
     * Reverse/Void a posted transaction
     */
    public Transaction reverseTransaction(Long transactionId, String reason, String username) {
        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + transactionId));
        
        if (!original.getIsPosted()) {
            auditService.logFailure(username, AuditLog.OperationType.REJECT, "Transaction",
                                   transactionId, "Reverse transaction - not posted", "Cannot reverse unposted transaction");
            throw new IllegalStateException("Cannot reverse unposted transaction");
        }
        
        Transaction reversal = new Transaction();
        reversal.setDate(LocalDate.now());
        reversal.setType("REVERSAL");
        reversal.setDescription("Reversal: " + reason + " (Original: " + original.getNumber() + ")");
        reversal.setReference(original.getNumber());
        
        List<TransactionEntryData> reverseEntries = original.getEntries().stream()
                .map(entry -> new TransactionEntryData(
                        entry.getAccount().getId(),
                        entry.getCreditAmount(),
                        entry.getDebitAmount(),
                        "Reversal of " + entry.getDescription()))
                .toList();
        
        Transaction savedReversal = createTransaction(reversal, reverseEntries, username);
        
        // Audit log for reversal
        auditService.logWithValues(username, AuditLog.OperationType.REJECT, "Transaction",
                                  transactionId, "Reversed transaction: " + original.getNumber() + " - Reason: " + reason,
                                  buildTransactionJson(original), buildTransactionJson(savedReversal));
        
        return savedReversal;
    }
    
    /**
     * Reverse transaction without audit (for backward compatibility)
     */
    public Transaction reverseTransaction(Long transactionId, String reason) {
        return reverseTransaction(transactionId, reason, "system");
    }
    
    public void deleteTransaction(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + id));
        
        if (transaction.getIsPosted()) {
            auditService.logFailure(username, AuditLog.OperationType.DELETE, "Transaction",
                                   id, "Delete transaction - is posted", "Cannot delete posted transaction. Please reverse it.");
            throw new IllegalStateException("Cannot delete posted transaction. Please reverse it.");
        }
        
        Long entityId = transaction.getId();
        transactionRepository.delete(transaction);
        logger.info("Transaction deleted: {}", transaction.getNumber());
        
        // Audit log
        auditService.logSuccess(username, AuditLog.OperationType.DELETE, "Transaction",
                               entityId, "Deleted transaction: " + transaction.getNumber());
    }
    
    /**
     * Delete transaction without audit (for backward compatibility)
     */
    public void deleteTransaction(Long id) {
        deleteTransaction(id, "system");
    }
    
    private void validateTransaction(Transaction transaction, List<TransactionEntryData> entries) {
        if (transaction.getDate() == null) {
            throw new IllegalArgumentException("Transaction date is required");
        }
        if (transaction.getType() == null || transaction.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type is required");
        }
        if (entries == null || entries.size() < 2) {
            throw new IllegalArgumentException("Transaction must have at least 2 entries");
        }
    }
    
    private String generateTransactionNumber() {
        long count = transactionRepository.count() + 1;
        return "TXN-" + LocalDate.now().getYear() + "-" + 
               String.format("%06d", count);
    }
    
    /**
     * Build JSON representation of transaction for audit logging
     */
    private String buildTransactionJson(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(transaction.getId()).append(",");
        json.append("\"number\":\"").append(transaction.getNumber()).append("\",");
        json.append("\"date\":\"").append(transaction.getDate()).append("\",");
        json.append("\"type\":\"").append(transaction.getType()).append("\",");
        json.append("\"description\":\"").append(transaction.getDescription()).append("\",");
        json.append("\"totalDebit\":").append(transaction.getTotalDebit()).append(",");
        json.append("\"totalCredit\":").append(transaction.getTotalCredit()).append(",");
        json.append("\"isPosted\":").append(transaction.getIsPosted());
        json.append("}");
        return json.toString();
    }
    
    public long getTransactionsCount() {
        return transactionRepository.count();
    }

    public List<Transaction> getTransactionsByThirdPartyId(Long thirdPartyId) {
        return transactionRepository.findByThirdPartyId(thirdPartyId);
    }
    
    public static class TransactionEntryData {
        private final Long accountId;
        private final BigDecimal debitAmount;
        private final BigDecimal creditAmount;
        private final String description;
        
        public TransactionEntryData(Long accountId, BigDecimal debitAmount, 
                                   BigDecimal creditAmount, String description) {
            this.accountId = accountId;
            this.debitAmount = debitAmount;
            this.creditAmount = creditAmount;
            this.description = description;
        }
        
        public Long getAccountId() { return accountId; }
        public BigDecimal getDebitAmount() { return debitAmount; }
        public BigDecimal getCreditAmount() { return creditAmount; }
        public String getDescription() { return description; }
    }
}
