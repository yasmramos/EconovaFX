package com.econovafx.modules.accounting.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.model.AccountingPeriod;
import com.econovafx.modules.accounting.model.AccountingPeriod.PeriodStatus;

/**
 * Unit tests for AccountingValidator.
 * Validates compliance with Cuban Accounting Resolution 340/2004.
 */
class AccountingValidatorTest {

    private AccountingValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccountingValidator();
    }

    @Test
    void testValidateBalancedEntry_SingleCurrency_Success() {
        // Given: Entry with debits = credits in same currency
        Transaction transaction = createBalancedTransaction();
        
        // When: validateTransactionBalancedByCurrency is called
        List<String> errors = AccountingValidator.validateTransactionBalancedByCurrency(transaction);
        
        // Then: Should return empty list (valid)
        assertTrue(errors.isEmpty(), "Expected no errors for balanced transaction, but got: " + errors);
    }

    @Test
    void testValidateBalancedEntry_Unbalanced_ThrowsException() {
        // Given: Entry with debits != credits
        Transaction transaction = createUnbalancedTransaction();
        
        // When & Then: Should have validation errors
        List<String> errors = AccountingValidator.validateTransactionBalancedByCurrency(transaction);
        assertFalse(errors.isEmpty(), "Expected validation errors for unbalanced transaction");
        assertTrue(errors.stream().anyMatch(e -> e.contains("no está cuadrado")), 
                   "Expected error message about unbalanced entry");
    }

    @Test
    void testValidateBalancedEntry_MultiCurrency_ConvertsCorrectly() {
        // Given: Entry with multiple currencies using exchange rates
        Transaction transaction = createMultiCurrencyTransaction();
        
        // When: validateTransactionBalancedByCurrency is called
        List<String> errors = AccountingValidator.validateTransactionBalancedByCurrency(transaction);
        
        // Then: Should detect imbalance if not properly converted
        assertFalse(errors.isEmpty(), "Expected errors for multi-currency transaction without proper conversion");
    }

    @Test
    void testValidateOpenPeriod_Success() {
        // Given: Current date falls within an open accounting period
        LocalDate transactionDate = LocalDate.of(2026, 6, 15);
        List<AccountingPeriod> periods = createOpenPeriods();
        
        // When: validateTransactionInOpenPeriod is called
        List<String> errors = AccountingValidator.validateTransactionInOpenPeriod(transactionDate, periods);
        
        // Then: Should return empty list (valid)
        assertTrue(errors.isEmpty(), "Expected no errors for transaction in open period, but got: " + errors);
    }

    @Test
    void testValidateOpenPeriod_ClosedPeriod_ThrowsException() {
        // Given: Transaction date in a closed period
        LocalDate closedDate = LocalDate.of(2023, 12, 15);
        List<AccountingPeriod> periods = createClosedPeriods();
        
        // When & Then: Should have validation errors
        List<String> errors = AccountingValidator.validateTransactionInOpenPeriod(closedDate, periods);
        assertFalse(errors.isEmpty(), "Expected validation errors for transaction in closed period");
        assertTrue(errors.stream().anyMatch(e -> e.contains("cerrado") || e.contains("período")), 
                   "Expected error message about closed period");
    }

    @Test
    void testValidateDoubleEntry_MinimumTwoLines() {
        // Given: Entry with only one line
        Transaction singleLineTransaction = createSingleLineTransaction();
        
        // When & Then: Should have validation errors
        List<String> errors = AccountingValidator.validateTransactionBalancedByCurrency(singleLineTransaction);
        assertFalse(errors.isEmpty(), "Expected validation errors for single-line entry");
    }

    @Test
    void testValidateActiveAccount_Success() {
        // Given: Active account in transaction
        Transaction transaction = createTransactionWithActiveAccount();
        
        // When: validateAccountTypes is called
        List<String> errors = AccountingValidator.validateAccountTypes(transaction);
        
        // Then: Should not have errors about inactive account
        assertTrue(errors.stream().noneMatch(e -> e.contains("inactiva")), 
                   "Did not expect errors about inactive account for active account");
    }

    @Test
    void testValidateActiveAccount_Inactive_ThrowsException() {
        // Given: Inactive account in transaction
        Transaction transaction = createTransactionWithInactiveAccount();
        
        // When & Then: Should have validation errors about inactive account
        List<String> errors = AccountingValidator.validateAccountTypes(transaction);
        assertFalse(errors.isEmpty(), "Expected validation errors for inactive account");
        assertTrue(errors.stream().anyMatch(e -> e.contains("inactiva")), 
                   "Expected error message about inactive account");
    }

    @Test
    void testValidatePositiveAmount_Success() {
        // Given: Transaction with positive amounts
        Transaction transaction = createTransactionWithPositiveAmounts();
        
        // When: validateAccountTypes is called
        List<String> errors = AccountingValidator.validateAccountTypes(transaction);
        
        // Then: Should not have errors about negative amounts
        assertTrue(errors.stream().noneMatch(e -> e.contains("negativo")), 
                   "Did not expect errors about negative amounts for positive amounts");
    }

    @Test
    void testValidatePositiveAmount_Negative_ThrowsException() {
        // Given: Transaction with negative amount
        Transaction transaction = createTransactionWithNegativeAmount();
        
        // When & Then: Should have validation errors about negative amounts
        List<String> errors = AccountingValidator.validateAccountTypes(transaction);
        assertFalse(errors.isEmpty(), "Expected validation errors for negative amounts");
        assertTrue(errors.stream().anyMatch(e -> e.contains("negativo")), 
                   "Expected error message about negative amounts");
    }
    
    // Helper methods to create test data
    
    private Transaction createBalancedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("ASIENTO");
        transaction.setDescription("Test balanced transaction");
        
        Account account1 = new Account("101", "Cash", AccountType.ASSET);
        account1.setId(1L);
        account1.setIsActive(true);
        
        Account account2 = new Account("201", "Accounts Payable", AccountType.LIABILITY);
        account2.setId(2L);
        account2.setIsActive(true);
        
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry debitEntry = new TransactionEntry();
        debitEntry.setAccount(account1);
        debitEntry.setDebitAmount(new BigDecimal("1000.00"));
        debitEntry.setCreditAmount(BigDecimal.ZERO);
        entries.add(debitEntry);
        
        TransactionEntry creditEntry = new TransactionEntry();
        creditEntry.setAccount(account2);
        creditEntry.setDebitAmount(BigDecimal.ZERO);
        creditEntry.setCreditAmount(new BigDecimal("1000.00"));
        entries.add(creditEntry);
        
        transaction.setEntries(entries);
        return transaction;
    }
    
    private Transaction createUnbalancedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("ASIENTO");
        transaction.setDescription("Test unbalanced transaction");
        
        Account account1 = new Account("101", "Cash", AccountType.ASSET);
        account1.setId(1L);
        account1.setIsActive(true);
        
        Account account2 = new Account("201", "Accounts Payable", AccountType.LIABILITY);
        account2.setId(2L);
        account2.setIsActive(true);
        
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry debitEntry = new TransactionEntry();
        debitEntry.setAccount(account1);
        debitEntry.setDebitAmount(new BigDecimal("1000.00"));
        debitEntry.setCreditAmount(BigDecimal.ZERO);
        entries.add(debitEntry);
        
        TransactionEntry creditEntry = new TransactionEntry();
        creditEntry.setAccount(account2);
        creditEntry.setDebitAmount(BigDecimal.ZERO);
        creditEntry.setCreditAmount(new BigDecimal("900.00"));
        entries.add(creditEntry);
        
        transaction.setEntries(entries);
        return transaction;
    }
    
    private Transaction createMultiCurrencyTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("ASIENTO");
        transaction.setDescription("Test multi-currency transaction");
        
        Account account1 = new Account("101", "Cash USD", AccountType.ASSET);
        account1.setId(1L);
        account1.setIsActive(true);
        
        Account account2 = new Account("102", "Cash CUP", AccountType.ASSET);
        account2.setId(2L);
        account2.setIsActive(true);
        
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry usdEntry = new TransactionEntry();
        usdEntry.setAccount(account1);
        usdEntry.setDebitAmount(new BigDecimal("100.00"));
        usdEntry.setCreditAmount(BigDecimal.ZERO);
        entries.add(usdEntry);
        
        TransactionEntry cupEntry = new TransactionEntry();
        cupEntry.setAccount(account2);
        cupEntry.setDebitAmount(BigDecimal.ZERO);
        cupEntry.setCreditAmount(new BigDecimal("1000.00"));
        entries.add(cupEntry);
        
        transaction.setEntries(entries);
        return transaction;
    }
    
    private List<AccountingPeriod> createOpenPeriods() {
        List<AccountingPeriod> periods = new ArrayList<>();
        
        // Create a period that includes the current date (2026-06-20)
        AccountingPeriod currentPeriod = new AccountingPeriod();
        currentPeriod.setName("Junio 2026");
        currentPeriod.setStartDate(LocalDate.of(2026, 6, 1));
        currentPeriod.setEndDate(LocalDate.of(2026, 6, 30));
        currentPeriod.setStatus(PeriodStatus.OPEN);
        periods.add(currentPeriod);
        
        return periods;
    }
    
    private List<AccountingPeriod> createClosedPeriods() {
        List<AccountingPeriod> periods = new ArrayList<>();
        
        AccountingPeriod closedPeriod = new AccountingPeriod();
        closedPeriod.setName("Diciembre 2023");
        closedPeriod.setStartDate(LocalDate.of(2023, 12, 1));
        closedPeriod.setEndDate(LocalDate.of(2023, 12, 31));
        closedPeriod.setStatus(PeriodStatus.CLOSED);
        periods.add(closedPeriod);
        
        return periods;
    }
    
    private Transaction createSingleLineTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("ASIENTO");
        transaction.setDescription("Test single line transaction");
        
        Account account1 = new Account("101", "Cash", AccountType.ASSET);
        account1.setId(1L);
        account1.setIsActive(true);
        
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry singleEntry = new TransactionEntry();
        singleEntry.setAccount(account1);
        singleEntry.setDebitAmount(new BigDecimal("1000.00"));
        singleEntry.setCreditAmount(BigDecimal.ZERO);
        entries.add(singleEntry);
        
        transaction.setEntries(entries);
        return transaction;
    }
    
    private Transaction createTransactionWithActiveAccount() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("ASIENTO");
        
        Account activeAccount = new Account("101", "Cash", AccountType.ASSET);
        activeAccount.setId(1L);
        activeAccount.setIsActive(true);
        
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry entry = new TransactionEntry();
        entry.setAccount(activeAccount);
        entry.setDebitAmount(new BigDecimal("500.00"));
        entry.setCreditAmount(BigDecimal.ZERO);
        entries.add(entry);
        
        transaction.setEntries(entries);
        return transaction;
    }
    
    private Transaction createTransactionWithInactiveAccount() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("ASIENTO");
        
        Account inactiveAccount = new Account("102", "Old Account", AccountType.ASSET);
        inactiveAccount.setId(2L);
        inactiveAccount.setIsActive(false);
        
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry entry = new TransactionEntry();
        entry.setAccount(inactiveAccount);
        entry.setDebitAmount(new BigDecimal("500.00"));
        entry.setCreditAmount(BigDecimal.ZERO);
        entries.add(entry);
        
        transaction.setEntries(entries);
        return transaction;
    }
    
    private Transaction createTransactionWithPositiveAmounts() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("ASIENTO");
        
        Account account = new Account("101", "Cash", AccountType.ASSET);
        account.setId(1L);
        account.setIsActive(true);
        
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry entry = new TransactionEntry();
        entry.setAccount(account);
        entry.setDebitAmount(new BigDecimal("500.00"));
        entry.setCreditAmount(BigDecimal.ZERO);
        entries.add(entry);
        
        transaction.setEntries(entries);
        return transaction;
    }
    
    private Transaction createTransactionWithNegativeAmount() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("ASIENTO");
        
        Account account = new Account("101", "Cash", AccountType.ASSET);
        account.setId(1L);
        account.setIsActive(true);
        
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry entry = new TransactionEntry();
        entry.setAccount(account);
        entry.setDebitAmount(new BigDecimal("-100.00"));
        entry.setCreditAmount(BigDecimal.ZERO);
        entries.add(entry);
        
        transaction.setEntries(entries);
        return transaction;
    }
}
