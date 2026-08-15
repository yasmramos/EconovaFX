package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.ClosingEntry;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.model.TransactionStatus;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.ClosingEntryRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.core.exception.ValidationException;
import com.econovafx.modules.core.security.RequiresTenant;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing opening balances in the General Ledger.
 * 
 * Resolution 340/2004 Compliance:
 * - MC.2: Opening balances must be loaded from a Trial Balance
 * - MC.3: Opening closure is conditioned on balanced debits and credits
 * - After opening closure, balance changes only through TransactionService
 */
@Component
@RequiresTenant
public class OpeningBalanceService {

    private static final Logger logger = LoggerFactory.getLogger(OpeningBalanceService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final ClosingEntryRepository closingEntryRepository;

    @Inject
    public OpeningBalanceService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            TransactionService transactionService,
            ClosingEntryRepository closingEntryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.closingEntryRepository = closingEntryRepository;
    }

    /**
     * Represents an opening balance entry for loading balances.
     */
    public static class OpeningBalanceEntry {
        private Long accountId;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private String description;

        public OpeningBalanceEntry(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount, String description) {
            this.accountId = accountId;
            this.debitAmount = debitAmount != null ? debitAmount : BigDecimal.ZERO;
            this.creditAmount = creditAmount != null ? creditAmount : BigDecimal.ZERO;
            this.description = description;
        }

        public Long getAccountId() { return accountId; }
        public BigDecimal getDebitAmount() { return debitAmount; }
        public BigDecimal getCreditAmount() { return creditAmount; }
        public String getDescription() { return description; }
    }

    /**
     * Result of validating opening balance entries.
     */
    public static class OpeningBalanceValidationResult {
        private final boolean balanced;
        private final BigDecimal totalDebits;
        private final BigDecimal totalCredits;
        private final BigDecimal difference;

        public OpeningBalanceValidationResult(BigDecimal totalDebits, BigDecimal totalCredits) {
            this.totalDebits = totalDebits;
            this.totalCredits = totalCredits;
            this.balanced = totalDebits.compareTo(totalCredits) == 0;
            this.difference = totalDebits.subtract(totalCredits);
        }

        public boolean isBalanced() { return balanced; }
        public BigDecimal getTotalDebits() { return totalDebits; }
        public BigDecimal getTotalCredits() { return totalCredits; }
        public BigDecimal getDifference() { return difference; }
    }

    /**
     * Validates opening balance entries without creating transactions.
     * Resolution 340/2004 MC.3: Opening closure requires balanced debits and credits.
     * 
     * @param entries List of opening balance entries from Trial Balance
     * @return Validation result with totals and balance status
     */
    public OpeningBalanceValidationResult validateOpeningBalances(List<OpeningBalanceEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new ValidationException("Opening balance entries cannot be empty");
        }

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (OpeningBalanceEntry entry : entries) {
            // Validate account exists
            Account account = accountRepository.findById(entry.getAccountId())
                    .orElseThrow(() -> new ValidationException(
                            "Account not found with ID: " + entry.getAccountId()));

            totalDebits = totalDebits.add(entry.getDebitAmount());
            totalCredits = totalCredits.add(entry.getCreditAmount());
        }

        return new OpeningBalanceValidationResult(totalDebits, totalCredits);
    }

    /**
     * Creates opening balance transactions and registers closing entry.
     * Resolution 340/2004 MC.2-3: Loads opening balances and closes opening process.
     * 
     * @param entries List of opening balance entries from Trial Balance
     * @param fiscalYear Fiscal year for the opening
     * @param username Username of the user performing the operation
     * @return The created opening transaction
     * @throws ValidationException if balances are not balanced
     */
    public Transaction createOpeningBalances(
            List<OpeningBalanceEntry> entries,
            Integer fiscalYear,
            String username) {
        
        // First validate that balances are balanced
        OpeningBalanceValidationResult validation = validateOpeningBalances(entries);
        
        if (!validation.isBalanced()) {
            throw new ValidationException(
                    String.format("Opening balances are not balanced. Debits: %s, Credits: %s, Difference: %s",
                            validation.getTotalDebits(), validation.getTotalCredits(), validation.getDifference()));
        }

        // Create opening transaction
        Transaction openingTransaction = new Transaction();
        openingTransaction.setDate(LocalDate.now());
        openingTransaction.setType("OPENING");
        openingTransaction.setDescription("Opening balances for fiscal year " + fiscalYear);
        openingTransaction.setReference("OPENING-" + fiscalYear);
        openingTransaction.setStatus(TransactionStatus.DRAFT);

        List<TransactionService.TransactionEntryData> entryDataList = new ArrayList<>();
        
        for (OpeningBalanceEntry entry : entries) {
            // Validate that the account exists before building the entry
            accountRepository.findById(entry.getAccountId())
                    .orElseThrow(() -> new ValidationException(
                            "Account not found with ID: " + entry.getAccountId()));

            TransactionService.TransactionEntryData entryData = 
                    new TransactionService.TransactionEntryData(
                            entry.getAccountId(),
                            entry.getDebitAmount(),
                            entry.getCreditAmount(),
                            entry.getDescription() != null ? entry.getDescription() : "Opening balance"
                    );
            entryDataList.add(entryData);

            // Balance is applied when the opening transaction is posted below;
            // do not modify the account balance directly here to avoid double-counting.
        }

        // Create the transaction
        Transaction savedTransaction = transactionService.createTransaction(
                openingTransaction, entryDataList, username);

        // Post the opening transaction
        savedTransaction = transactionService.postTransaction(savedTransaction.getId(), username);

        // Create closing entry to mark opening as closed
        ClosingEntry closingEntry = new ClosingEntry();
        closingEntry.setClosingType(ClosingEntry.ClosingType.OPENING);
        closingEntry.setClosingDate(LocalDate.now());
        closingEntry.setFiscalYear(fiscalYear);
        closingEntry.setRelatedTransaction(savedTransaction);
        closingEntry.setPosted(true);

        // Save closing entry using repository
        closingEntryRepository.save(closingEntry);
        logger.info("Opening closing entry created for fiscal year {} with transaction {}", 
                fiscalYear, savedTransaction.getNumber());

        logger.info("Opening balances created successfully for fiscal year {} by {}", 
                fiscalYear, username);

        return savedTransaction;
    }

    /**
     * Checks if opening has been closed for a fiscal year.
     * 
     * @param fiscalYear Fiscal year to check
     * @return true if opening is closed, false otherwise
     */
    public boolean isOpeningClosed(Integer fiscalYear) {
        return closingEntryRepository.isOpeningClosed(fiscalYear);
    }

    /**
     * Validates that opening can be modified (not yet closed).
     * Resolution 340/2004 MC.2: Opening balances can only be modified before closure.
     * 
     * @param fiscalYear Fiscal year to check
     * @throws IllegalStateException if opening is already closed
     */
    public void validateOpeningNotClosed(Integer fiscalYear) {
        if (isOpeningClosed(fiscalYear)) {
            throw new IllegalStateException(
                    "Opening balances for fiscal year " + fiscalYear + " are already closed. " +
                    "Changes can only be made through TransactionService.");
        }
    }
}
