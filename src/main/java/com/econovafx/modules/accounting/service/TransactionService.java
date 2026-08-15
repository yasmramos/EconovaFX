package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.ClosingEntryRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.accounting.service.AccountingPeriodService;
import com.econovafx.modules.core.exception.EntityNotFoundException;
import com.econovafx.modules.core.exception.ValidationException;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.security.RequiresTenant;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final AccountingPeriodService accountingPeriodService;
    private final ClosingEntryRepository closingEntryRepository;
    
    @Inject
    public TransactionService(TransactionRepository transactionRepository,
                             AccountRepository accountRepository,
                             AuditService auditService,
                             AccountingPeriodService accountingPeriodService,
                             ClosingEntryRepository closingEntryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
        this.accountingPeriodService = accountingPeriodService;
        this.closingEntryRepository = closingEntryRepository;
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
        
        // Set initial status to DRAFT
        transaction.setStatus(TransactionStatus.DRAFT);
        
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (TransactionEntryData entryData : entries) {
            Account account = accountRepository.findById(entryData.getAccountId())
                    .orElseThrow(() -> EntityNotFoundException.notFound("Account", entryData.getAccountId()));
            
            TransactionEntry entry = new TransactionEntry();
            entry.setAccount(account);
            entry.setDebitAmount(entryData.getDebitAmount().setScale(4, java.math.RoundingMode.HALF_UP));
            entry.setCreditAmount(entryData.getCreditAmount().setScale(4, java.math.RoundingMode.HALF_UP));
            entry.setDescription(entryData.getDescription());
            
            transaction.addEntry(entry);
            
            totalDebit = totalDebit.add(entryData.getDebitAmount());
            totalCredit = totalCredit.add(entryData.getCreditAmount());
        }
        
        // Recalculate totals from entries to ensure consistency
        transaction.recalculateTotals();
        totalDebit = transaction.getTotalDebit();
        totalCredit = transaction.getTotalCredit();
        
        if (totalDebit.compareTo(totalCredit) != 0) {
            String errorMsg = "Transaction is not balanced. Debit: " + totalDebit + ", Credit: " + totalCredit;
            auditService.logFailure(username, AuditLog.OperationType.CREATE, "Transaction", null, 
                                   "Create transaction - validation failed", errorMsg);
            throw ValidationException.unbalancedTransaction(totalDebit, totalCredit);
        }
        
        transaction.setTotalDebit(totalDebit.setScale(4, java.math.RoundingMode.HALF_UP));
        transaction.setTotalCredit(totalCredit.setScale(4, java.math.RoundingMode.HALF_UP));
        
        Transaction saved = transactionRepository.save(transaction);
        logger.info("Transaction created: {} with status: {}", saved.getNumber(), saved.getStatus());
        
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
     * Resolution 340/2004: Validates period is open before posting
     */
    public Transaction postTransaction(Long transactionId, String username) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> EntityNotFoundException.notFound("Transaction", transactionId));
        
        // Resolution 340/2004: Validate period is open before posting
        accountingPeriodService.validatePeriodOpenForPosting(transaction.getDate());
        
        // Check if transaction can be posted based on status
        if (!transaction.getStatus().canPost()) {
            String errorMsg = "Transaction cannot be posted in current status: " + transaction.getStatus().getDescription();
            auditService.logFailure(username, AuditLog.OperationType.PUBLISH_TRANSACTION, "Transaction", 
                                   transactionId, "Post transaction - invalid status", errorMsg);
            throw ValidationException.transactionAlreadyPosted();
        }
        
        if (!transaction.isBalanced()) {
            auditService.logFailure(username, AuditLog.OperationType.PUBLISH_TRANSACTION, "Transaction", 
                                   transactionId, "Post transaction - not balanced", "Transaction is not balanced");
            throw ValidationException.unbalancedTransaction(transaction.getTotalDebit(), transaction.getTotalCredit());
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
        
        // Update status to POSTED
        transaction.setStatus(TransactionStatus.POSTED);
        transactionRepository.update(transaction);
        
        logger.info("Transaction posted: {} with status: {}", transaction.getNumber(), transaction.getStatus());
        
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
                .orElseThrow(() -> EntityNotFoundException.notFound("Transaction", transactionId));
        
        // Check if transaction can be reversed based on status
        if (!original.getStatus().canReverse()) {
            String errorMsg = "Transaction cannot be reversed in current status: " + original.getStatus().getDescription();
            auditService.logFailure(username, AuditLog.OperationType.REJECT, "Transaction",
                                   transactionId, "Reverse transaction - invalid status", errorMsg);
            throw ValidationException.cannotReverseUnpostedTransaction();
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
        
        // Update original transaction status to REVERSED
        original.setStatus(TransactionStatus.REVERSED);
        transactionRepository.update(original);
        
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
                .orElseThrow(() -> EntityNotFoundException.notFound("Transaction", id));
        
        // Only DRAFT transactions can be deleted
        if (!transaction.getStatus().canModify()) {
            auditService.logFailure(username, AuditLog.OperationType.DELETE, "Transaction",
                                   id, "Delete transaction - invalid status", 
                                   "Cannot delete transaction in status: " + transaction.getStatus().getDescription() + ". Please reverse it first.");
            throw ValidationException.cannotDeletePostedTransaction();
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
            throw new ValidationException("date", "Transaction date cannot be null");
        }
        if (transaction.getType() == null || transaction.getType().trim().isEmpty()) {
            throw new ValidationException("type", "Transaction type cannot be null");
        }
        if (entries == null || entries.size() < 2) {
            throw new ValidationException("entries", "Transaction must have at least 2 entries");
        }
    }
    
    private String generateTransactionNumber() {
        long count = transactionRepository.count() + 1;
        return "TXN-" + LocalDate.now().getYear() + "-" + 
               String.format("%06d", count);
    }
    
    /**
     * Generate closing entries for nominal accounts (revenue and expense).
     * Resolution 340/2004: Required before annual closure.
     * 
     * @param fiscalYear The fiscal year to close nominal accounts for
     * @param username The user executing the closure
     * @return List of closing entries created
     */
    public List<ClosingEntry> closeNominalAccounts(Integer fiscalYear, String username) {
        logger.info("Closing nominal accounts for fiscal year {}", fiscalYear);
        
        List<ClosingEntry> closingEntries = new ArrayList<>();
        LocalDate closingDate = LocalDate.of(fiscalYear, 12, 31);
        
        // Find all revenue accounts (credit balance accounts that need to be debited to zero)
        List<Account> revenueAccounts = accountRepository.findByType(AccountType.REVENUE);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        // Create closing transaction for revenue accounts
        if (!revenueAccounts.isEmpty()) {
            Transaction revenueClosure = new Transaction();
            revenueClosure.setDate(closingDate);
            revenueClosure.setType("CLOSING_ENTRY");
            revenueClosure.setDescription("Close revenue accounts for fiscal year " + fiscalYear);
            revenueClosure.setReference("CLOSE-REV-" + fiscalYear);
            
            List<TransactionEntryData> entries = new ArrayList<>();
            
            // Debit each revenue account to zero it out
            for (Account revenueAccount : revenueAccounts) {
                if (revenueAccount.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                    // Revenue accounts have credit balance, so we debit to close
                    entries.add(new TransactionEntryData(
                            revenueAccount.getId(),
                            revenueAccount.getBalance(), // Debit amount
                            BigDecimal.ZERO,
                            "Close revenue account: " + revenueAccount.getName()
                    ));
                    totalRevenue = totalRevenue.add(revenueAccount.getBalance());
                }
            }
            
            // Credit the income summary/result account with total revenue
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                Account incomeSummaryAccount = getOrCreateIncomeSummaryAccount();
                entries.add(new TransactionEntryData(
                        incomeSummaryAccount.getId(),
                        BigDecimal.ZERO,
                        totalRevenue,
                        "Transfer revenue to income summary"
                ));
                
                Transaction createdTransaction = createTransaction(revenueClosure, entries, username);
                postTransaction(createdTransaction.getId(), username);
                
                // Create closing entry record
                ClosingEntry entry = new ClosingEntry();
                entry.setClosingType(ClosingEntry.ClosingType.INCOME);
                entry.setClosingDate(closingDate);
                entry.setFiscalYear(fiscalYear);
                entry.setRelatedTransaction(createdTransaction);
                entry.setPosted(true);
                closingEntryRepository.save(entry);
                closingEntries.add(entry);
                
                logger.info("Created revenue closing entry for {} amount: {}", fiscalYear, totalRevenue);
            }
        }
        
        // Find all expense accounts (debit balance accounts that need to be credited to zero)
        List<Account> expenseAccounts = accountRepository.findByType(AccountType.EXPENSE);
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        // Create closing transaction for expense accounts
        if (!expenseAccounts.isEmpty()) {
            Transaction expenseClosure = new Transaction();
            expenseClosure.setDate(closingDate);
            expenseClosure.setType("CLOSING_ENTRY");
            expenseClosure.setDescription("Close expense accounts for fiscal year " + fiscalYear);
            expenseClosure.setReference("CLOSE-EXP-" + fiscalYear);
            
            List<TransactionEntryData> entries = new ArrayList<>();
            
            // Credit each expense account to zero it out
            for (Account expenseAccount : expenseAccounts) {
                if (expenseAccount.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                    // Expense accounts have debit balance, so we credit to close
                    entries.add(new TransactionEntryData(
                            expenseAccount.getId(),
                            BigDecimal.ZERO,
                            expenseAccount.getBalance(), // Credit amount
                            "Close expense account: " + expenseAccount.getName()
                    ));
                    totalExpenses = totalExpenses.add(expenseAccount.getBalance());
                }
            }
            
            // Debit the income summary/result account with total expenses
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                Account incomeSummaryAccount = getOrCreateIncomeSummaryAccount();
                entries.add(new TransactionEntryData(
                        incomeSummaryAccount.getId(),
                        totalExpenses,
                        BigDecimal.ZERO,
                        "Transfer expenses to income summary"
                ));
                
                Transaction createdTransaction = createTransaction(expenseClosure, entries, username);
                postTransaction(createdTransaction.getId(), username);
                
                // Create closing entry record
                ClosingEntry entry = new ClosingEntry();
                entry.setClosingType(ClosingEntry.ClosingType.EXPENSE);
                entry.setClosingDate(closingDate);
                entry.setFiscalYear(fiscalYear);
                entry.setRelatedTransaction(createdTransaction);
                entry.setPosted(true);
                closingEntryRepository.save(entry);
                closingEntries.add(entry);
                
                logger.info("Created expense closing entry for {} amount: {}", fiscalYear, totalExpenses);
            }
        }
        
        // Close income summary to retained earnings (result closure)
        Account incomeSummaryAccount = getOrCreateIncomeSummaryAccount();
        BigDecimal netIncome = totalRevenue.subtract(totalExpenses);
        
        if (netIncome.compareTo(BigDecimal.ZERO) != 0) {
            Transaction resultClosure = new Transaction();
            resultClosure.setDate(closingDate);
            resultClosure.setType("CLOSING_ENTRY");
            resultClosure.setDescription("Close income summary to retained earnings for fiscal year " + fiscalYear);
            resultClosure.setReference("CLOSE-RES-" + fiscalYear);
            
            List<TransactionEntryData> entries = new ArrayList<>();
            Account retainedEarningsAccount = getOrCreateRetainedEarningsAccount();
            
            if (netIncome.compareTo(BigDecimal.ZERO) > 0) {
                // Net income: debit income summary, credit retained earnings
                entries.add(new TransactionEntryData(
                        incomeSummaryAccount.getId(),
                        BigDecimal.ZERO,
                        netIncome.abs(),
                        "Transfer net income to retained earnings"
                ));
                entries.add(new TransactionEntryData(
                        retainedEarningsAccount.getId(),
                        netIncome.abs(),
                        BigDecimal.ZERO,
                        "Net income for fiscal year " + fiscalYear
                ));
            } else {
                // Net loss: credit income summary, debit retained earnings
                entries.add(new TransactionEntryData(
                        incomeSummaryAccount.getId(),
                        netIncome.abs(),
                        BigDecimal.ZERO,
                        "Transfer net loss to retained earnings"
                ));
                entries.add(new TransactionEntryData(
                        retainedEarningsAccount.getId(),
                        BigDecimal.ZERO,
                        netIncome.abs(),
                        "Net loss for fiscal year " + fiscalYear
                ));
            }
            
            Transaction createdTransaction = createTransaction(resultClosure, entries, username);
            postTransaction(createdTransaction.getId(), username);
            
            // Create closing entry record
            ClosingEntry entry = new ClosingEntry();
            entry.setClosingType(ClosingEntry.ClosingType.RESULT);
            entry.setClosingDate(closingDate);
            entry.setFiscalYear(fiscalYear);
            entry.setRelatedTransaction(createdTransaction);
            entry.setPosted(true);
            closingEntryRepository.save(entry);
            closingEntries.add(entry);
            
            logger.info("Created result closing entry for {} - Net Income: {}", fiscalYear, netIncome);
        }
        
        logger.info("Completed closing nominal accounts for fiscal year {} - Created {} entries", fiscalYear, closingEntries.size());
        return closingEntries;
    }
    
    /**
     * Get or create the income summary account used for closing nominal accounts.
     * 
     * @return The income summary account
     */
    private Account getOrCreateIncomeSummaryAccount() {
        // Try to find existing income summary account by code pattern
        Optional<Account> existing = accountRepository.findByCode("4.99");
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new income summary account
        Account incomeSummary = new Account();
        incomeSummary.setCode("4.99");
        incomeSummary.setName("Income Summary");
        incomeSummary.setDescription("Temporary account for closing revenues and expenses");
        incomeSummary.setType(AccountType.EQUITY);
        incomeSummary.setBalance(BigDecimal.ZERO);
        incomeSummary.setParentAccount(null);
        
        return accountRepository.save(incomeSummary);
    }
    
    /**
     * Get or create the retained earnings account.
     * 
     * @return The retained earnings account
     */
    private Account getOrCreateRetainedEarningsAccount() {
        // Try to find existing retained earnings account by code pattern
        Optional<Account> existing = accountRepository.findByCode("3.1");
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new retained earnings account
        Account retainedEarnings = new Account();
        retainedEarnings.setCode("3.1");
        retainedEarnings.setName("Retained Earnings");
        retainedEarnings.setDescription("Accumulated earnings from previous periods");
        retainedEarnings.setType(AccountType.EQUITY);
        retainedEarnings.setBalance(BigDecimal.ZERO);
        retainedEarnings.setParentAccount(null);
        
        return accountRepository.save(retainedEarnings);
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
    
    /**
     * Generate preview of transaction before posting.
     * Resolution 340/2004: Allows review before final posting.
     * 
     * @param transactionId ID of the transaction to preview
     * @return TransactionPreview object with simulated posting effects
     */
    public TransactionPreview generateTransactionPreview(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> EntityNotFoundException.notFound("Transaction", transactionId));
        
        // Only DRAFT transactions can be previewed for posting
        if (!transaction.getStatus().canPost()) {
            throw ValidationException.transactionAlreadyPosted();
        }
        
        if (!transaction.isBalanced()) {
            throw ValidationException.unbalancedTransaction(transaction.getTotalDebit(), transaction.getTotalCredit());
        }
        
        // Validate period is open (without actually posting)
        accountingPeriodService.validatePeriodOpenForPosting(transaction.getDate());
        
        // Simulate balance changes without persisting them
        List<AccountBalanceChange> simulatedChanges = new ArrayList<>();
        
        for (TransactionEntry entry : transaction.getEntries()) {
            Account account = entry.getAccount();
            BigDecimal currentBalance = account.getBalance();
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
            
            BigDecimal newBalance = currentBalance.add(balanceChange);
            simulatedChanges.add(new AccountBalanceChange(
                account.getId(),
                account.getCode(),
                account.getName(),
                currentBalance,
                balanceChange,
                newBalance
            ));
        }
        
        logger.info("Transaction preview generated: {} - Status: {}", 
                   transaction.getNumber(), transaction.getStatus());
        
        return new TransactionPreview(transaction, simulatedChanges);
    }
    
    /**
     * Inner class representing balance change simulation for preview
     */
    public static class AccountBalanceChange {
        private final Long accountId;
        private final String accountCode;
        private final String accountName;
        private final BigDecimal currentBalance;
        private final BigDecimal change;
        private final BigDecimal newBalance;
        
        public AccountBalanceChange(Long accountId, String accountCode, String accountName,
                                   BigDecimal currentBalance, BigDecimal change, BigDecimal newBalance) {
            this.accountId = accountId;
            this.accountCode = accountCode;
            this.accountName = accountName;
            this.currentBalance = currentBalance;
            this.change = change;
            this.newBalance = newBalance;
        }
        
        public Long getAccountId() { return accountId; }
        public String getAccountCode() { return accountCode; }
        public String getAccountName() { return accountName; }
        public BigDecimal getCurrentBalance() { return currentBalance; }
        public BigDecimal getChange() { return change; }
        public BigDecimal getNewBalance() { return newBalance; }
    }
    
    /**
     * Inner class representing complete transaction preview
     */
    public static class TransactionPreview {
        private final Transaction transaction;
        private final List<AccountBalanceChange> balanceChanges;
        private final boolean periodValid;
        private final boolean balanced;
        private final String validationMessage;
        
        public TransactionPreview(Transaction transaction, List<AccountBalanceChange> balanceChanges) {
            this.transaction = transaction;
            this.balanceChanges = balanceChanges;
            this.periodValid = true;
            this.balanced = transaction.isBalanced();
            this.validationMessage = balanced ? "Transaction ready for posting" : "Transaction is not balanced";
        }
        
        public Transaction getTransaction() { return transaction; }
        public List<AccountBalanceChange> getBalanceChanges() { return balanceChanges; }
        public boolean isPeriodValid() { return periodValid; }
        public boolean isBalanced() { return balanced; }
        public String getValidationMessage() { return validationMessage; }
        
        /**
         * Check if transaction is safe to post based on preview
         */
        public boolean isSafeToPost() {
            return periodValid && balanced && transaction.getStatus().canPost();
        }
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
