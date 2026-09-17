package com.econovafx.modules.reporting.service;

import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FinancialReportingService with date-filtered balance calculation.
 * Tests Resolution 340/2004 compliance for period-based financial reporting.
 */
@ExtendWith(MockitoExtension.class)
class FinancialReportingServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private FinancialReportingService service;

    @BeforeEach
    void setUp() {
        service = new FinancialReportingService(null, accountRepository, transactionRepository);
    }

    @Test
    void testCalculateAccountBalance_CumulativeFromMinDate() {
        // Arrange: Create asset account
        Account cashAccount = new Account("1000", "Cash", AccountType.ASSET);
        cashAccount.setId(1L);
        
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        // Create transactions from beginning to endDate
        Transaction tx1 = createPostedTransaction(LocalDate.of(2024, 1, 10), cashAccount, 
                                                   BigDecimal.valueOf(1000), BigDecimal.ZERO);
        Transaction tx2 = createPostedTransaction(LocalDate.of(2024, 1, 20), cashAccount, 
                                                   BigDecimal.valueOf(500), BigDecimal.ZERO);
        
        List<Transaction> transactions = Arrays.asList(tx1, tx2);
        
        when(transactionRepository.findPostedByDateRange(LocalDate.MIN, endDate))
                .thenReturn(transactions);

        // Act: Use reflection to test private method
        BigDecimal balance = invokeCalculateAccountBalance(cashAccount, endDate);

        // Assert: Cumulative balance from all posted transactions
        assertEquals(BigDecimal.valueOf(1500), balance);
    }

    @Test
    void testCalculateAccountBalancePeriod_OnlyWithinPeriod() {
        // Arrange: Create revenue account
        Account revenueAccount = new Account("4000", "Revenue", AccountType.REVENUE);
        revenueAccount.setId(2L);
        
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        // Create one transaction within period and one outside
        Transaction txWithin = createPostedTransaction(LocalDate.of(2024, 1, 15), revenueAccount, 
                                                        BigDecimal.ZERO, BigDecimal.valueOf(2000));
        Transaction txOutside = createPostedTransaction(LocalDate.of(2024, 2, 15), revenueAccount, 
                                                         BigDecimal.ZERO, BigDecimal.valueOf(9999));
        
        List<Transaction> transactions = Arrays.asList(txWithin, txOutside);
        
        when(transactionRepository.findPostedByDateRange(startDate, endDate))
                .thenReturn(Arrays.asList(txWithin)); // Only return within-period transactions

        // Act
        BigDecimal balance = invokeCalculateAccountBalancePeriod(revenueAccount, startDate, endDate);

        // Assert: Only transaction within period should be included
        assertEquals(BigDecimal.valueOf(2000), balance);
        assertNotEquals(BigDecimal.valueOf(2000 + 9999), balance);
    }

    @Test
    void testCalculateAccountBalance_AssetAccount_DebitIncreases() {
        // Arrange
        Account assetAccount = new Account("1000", "Cash", AccountType.ASSET);
        assetAccount.setId(1L);
        
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        Transaction tx = createPostedTransaction(LocalDate.of(2024, 1, 15), assetAccount, 
                                                  BigDecimal.valueOf(1000), BigDecimal.ZERO);
        
        when(transactionRepository.findPostedByDateRange(LocalDate.MIN, endDate))
                .thenReturn(Arrays.asList(tx));

        // Act
        BigDecimal balance = invokeCalculateAccountBalance(assetAccount, endDate);

        // Assert: Debit increases asset accounts
        assertEquals(BigDecimal.valueOf(1000), balance);
    }

    @Test
    void testCalculateAccountBalance_LiabilityAccount_CreditIncreases() {
        // Arrange
        Account liabilityAccount = new Account("2000", "Accounts Payable", AccountType.LIABILITY);
        liabilityAccount.setId(2L);
        
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        Transaction tx = createPostedTransaction(LocalDate.of(2024, 1, 15), liabilityAccount, 
                                                  BigDecimal.ZERO, BigDecimal.valueOf(3000));
        
        when(transactionRepository.findPostedByDateRange(LocalDate.MIN, endDate))
                .thenReturn(Arrays.asList(tx));

        // Act
        BigDecimal balance = invokeCalculateAccountBalance(liabilityAccount, endDate);

        // Assert: Credit increases liability accounts (negative balance in our convention)
        assertEquals(BigDecimal.valueOf(3000), balance);
    }

    @Test
    void testCalculateAccountBalance_ExcludesDraftTransactions() {
        // Arrange
        Account cashAccount = new Account("1000", "Cash", AccountType.ASSET);
        cashAccount.setId(1L);
        
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        Transaction postedTx = createPostedTransaction(LocalDate.of(2024, 1, 10), cashAccount, 
                                                        BigDecimal.valueOf(500), BigDecimal.ZERO);
        Transaction draftTx = createDraftTransaction(LocalDate.of(2024, 1, 20), cashAccount, 
                                                      BigDecimal.valueOf(9999), BigDecimal.ZERO);
        
        // Only posted transactions should be returned by repository method
        when(transactionRepository.findPostedByDateRange(LocalDate.MIN, endDate))
                .thenReturn(Arrays.asList(postedTx));

        // Act
        BigDecimal balance = invokeCalculateAccountBalance(cashAccount, endDate);

        // Assert: Draft transaction should not affect balance
        assertEquals(BigDecimal.valueOf(500), balance);
        assertNotEquals(BigDecimal.valueOf(500 + 9999), balance);
    }

    @Test
    void testCalculateAccountBalance_EmptyTransactionList() {
        // Arrange
        Account cashAccount = new Account("1000", "Cash", AccountType.ASSET);
        cashAccount.setId(1L);
        
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        when(transactionRepository.findPostedByDateRange(LocalDate.MIN, endDate))
                .thenReturn(Arrays.asList());

        // Act
        BigDecimal balance = invokeCalculateAccountBalance(cashAccount, endDate);

        // Assert: Zero balance when no transactions
        assertEquals(BigDecimal.ZERO, balance);
    }

    /**
     * Helper method to create a POSTED transaction
     */
    private Transaction createPostedTransaction(LocalDate date, Account account, 
                                                 BigDecimal debitAmount, BigDecimal creditAmount) {
        Transaction tx = new Transaction();
        tx.setDate(date);
        tx.setStatus(TransactionStatus.POSTED);
        
        TransactionEntry entry = new TransactionEntry();
        entry.setAccount(account);
        entry.setDebitAmount(debitAmount);
        entry.setCreditAmount(creditAmount);
        tx.setEntries(Arrays.asList(entry));
        
        return tx;
    }

    /**
     * Helper method to create a DRAFT transaction
     */
    private Transaction createDraftTransaction(LocalDate date, Account account, 
                                                BigDecimal debitAmount, BigDecimal creditAmount) {
        Transaction tx = new Transaction();
        tx.setDate(date);
        tx.setStatus(TransactionStatus.DRAFT);
        
        TransactionEntry entry = new TransactionEntry();
        entry.setAccount(account);
        entry.setDebitAmount(debitAmount);
        entry.setCreditAmount(creditAmount);
        tx.setEntries(Arrays.asList(entry));
        
        return tx;
    }

    /**
     * Helper method to invoke private calculateAccountBalance method via reflection.
     */
    private BigDecimal invokeCalculateAccountBalance(Account account, LocalDate endDate) {
        try {
            java.lang.reflect.Method method = FinancialReportingService.class
                    .getDeclaredMethod("calculateAccountBalance", Account.class, LocalDate.class);
            method.setAccessible(true);
            return (BigDecimal) method.invoke(service, account, endDate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke calculateAccountBalance", e);
        }
    }

    /**
     * Helper method to invoke private calculateAccountBalancePeriod method via reflection.
     */
    private BigDecimal invokeCalculateAccountBalancePeriod(Account account, LocalDate startDate, LocalDate endDate) {
        try {
            java.lang.reflect.Method method = FinancialReportingService.class
                    .getDeclaredMethod("calculateAccountBalancePeriod", Account.class, LocalDate.class, LocalDate.class);
            method.setAccessible(true);
            return (BigDecimal) method.invoke(service, account, startDate, endDate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke calculateAccountBalancePeriod", e);
        }
    }
}
