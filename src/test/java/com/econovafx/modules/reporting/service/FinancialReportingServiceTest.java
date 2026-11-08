package com.econovafx.modules.reporting.service;

import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.reporting.model.FinancialReport;
import com.econovafx.modules.reporting.model.ReportLine;
import com.econovafx.modules.reporting.repository.FinancialReportRepository;
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
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private FinancialReportRepository reportRepository;

    private FinancialReportingService service;

    private static final LocalDate EPOCH = LocalDate.of(1900, 1, 1);

    @BeforeEach
    void setUp() {
        service = new FinancialReportingService(reportRepository, accountRepository, transactionRepository);
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
        
        when(transactionRepository.findPostedByDateRange(EPOCH, endDate))
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
        
        when(transactionRepository.findPostedByDateRange(EPOCH, endDate))
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
        
        when(transactionRepository.findPostedByDateRange(EPOCH, endDate))
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
        when(transactionRepository.findPostedByDateRange(EPOCH, endDate))
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
        
        when(transactionRepository.findPostedByDateRange(EPOCH, endDate))
                .thenReturn(Arrays.asList());

        // Act
        BigDecimal balance = invokeCalculateAccountBalance(cashAccount, endDate);

        // Assert: Zero balance when no transactions
        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void testGenerateChangesInEquityStatement() {
        Account equity = new Account("3000", "Capital", AccountType.EQUITY);
        equity.setId(10L);
        Account rev = new Account("4000", "Ingresos", AccountType.REVENUE);
        rev.setId(11L);
        Account exp = new Account("5000", "Gastos", AccountType.EXPENSE);
        exp.setId(12L);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        when(accountRepository.findByType(AccountType.EQUITY)).thenReturn(List.of(equity));
        when(accountRepository.findByType(AccountType.REVENUE)).thenReturn(List.of(rev));
        when(accountRepository.findByType(AccountType.EXPENSE)).thenReturn(List.of(exp));

        Transaction opening = createPostedTransaction(LocalDate.of(2025, 12, 15), equity,
                BigDecimal.ZERO, BigDecimal.valueOf(5000));
        when(transactionRepository.findPostedByDateRange(EPOCH, start.minusDays(1)))
                .thenReturn(List.of(opening));

        Transaction revenueTx = createPostedTransaction(LocalDate.of(2026, 6, 1), rev,
                BigDecimal.ZERO, BigDecimal.valueOf(2000));
        Transaction expenseTx = createPostedTransaction(LocalDate.of(2026, 7, 1), exp,
                BigDecimal.valueOf(500), BigDecimal.ZERO);
        when(transactionRepository.findPostedByDateRange(start, end))
                .thenReturn(List.of(revenueTx, expenseTx));

        when(reportRepository.save(any(FinancialReport.class))).thenAnswer(inv -> inv.getArgument(0));

        FinancialReport report = service.generateChangesInEquityStatement(start, end, 2026);

        assertEquals(FinancialReport.ReportType.CHANGES_IN_EQUITY, report.getReportType());
        List<ReportLine> lines = report.getLines();
        assertEquals(3, lines.size());
        assertEquals(0, lines.get(0).getCreditAmount().compareTo(BigDecimal.valueOf(5000)));
        assertEquals(0, lines.get(1).getCreditAmount().compareTo(BigDecimal.valueOf(1500)));
        assertEquals(0, lines.get(2).getCreditAmount().compareTo(BigDecimal.valueOf(6500)));
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
