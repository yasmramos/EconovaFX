package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.FinancialStatementModelRepository;
import com.econovafx.modules.accounting.repository.FinancialStatementRowRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FinancialStatementService with date-filtered balance calculation.
 * Tests Resolution 340/2004 compliance for period-based financial reporting.
 */
@ExtendWith(MockitoExtension.class)
class FinancialStatementServiceTest {

    @Mock
    private FinancialStatementModelRepository modelRepository;

    @Mock
    private FinancialStatementRowRepository rowRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private FinancialStatementService service;

    @BeforeEach
    void setUp() {
        service = new FinancialStatementService(modelRepository, rowRepository, 
                                               accountRepository, transactionRepository);
    }

    @Test
    void testCalculateAccountBalances_FiltersByDateRange() {
        // Arrange: Create accounts
        Account cashAccount = new Account("1000", "Cash", AccountType.ASSET);
        cashAccount.setBalance(BigDecimal.valueOf(10000)); // This should NOT be used
        
        Account revenueAccount = new Account("4000", "Revenue", AccountType.REVENUE);
        revenueAccount.setBalance(BigDecimal.valueOf(50000)); // This should NOT be used
        
        List<Account> accounts = Arrays.asList(cashAccount, revenueAccount);

        // Create transactions within date range
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        Transaction tx1 = new Transaction();
        tx1.setDate(LocalDate.of(2024, 1, 15));
        tx1.setStatus(TransactionStatus.POSTED);
        
        TransactionEntry entry1 = new TransactionEntry();
        entry1.setAccount(cashAccount);
        entry1.setDebitAmount(BigDecimal.valueOf(1000));
        entry1.setCreditAmount(BigDecimal.ZERO);
        
        TransactionEntry entry2 = new TransactionEntry();
        entry2.setAccount(revenueAccount);
        entry2.setDebitAmount(BigDecimal.ZERO);
        entry2.setCreditAmount(BigDecimal.valueOf(1000));
        
        tx1.setEntries(Arrays.asList(entry1, entry2));
        
        List<Transaction> transactions = Arrays.asList(tx1);
        
        when(transactionRepository.findByDateRange(startDate, endDate))
                .thenReturn(transactions);

        // Act: Use reflection to test private method
        Map<String, BigDecimal> balances = invokeCalculateAccountBalances(accounts, startDate, endDate);

        // Assert: Balances should come from transactions, not Account.getBalance()
        assertEquals(BigDecimal.valueOf(1000), balances.get("1000"));
        assertEquals(BigDecimal.valueOf(1000), balances.get("4000")); // Credit increases revenue
        assertNotEquals(BigDecimal.valueOf(10000), balances.get("1000")); // Not from Account.balance
        assertNotEquals(BigDecimal.valueOf(50000), balances.get("4000")); // Not from Account.balance
    }

    @Test
    void testCalculateAccountBalances_ExcludesNonPostedTransactions() {
        // Arrange: Create accounts
        Account cashAccount = new Account("1000", "Cash", AccountType.ASSET);
        List<Account> accounts = Arrays.asList(cashAccount);

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        // Create one POSTED and one DRAFT transaction
        Transaction postedTx = new Transaction();
        postedTx.setDate(LocalDate.of(2024, 1, 15));
        postedTx.setStatus(TransactionStatus.POSTED);
        
        TransactionEntry postedEntry = new TransactionEntry();
        postedEntry.setAccount(cashAccount);
        postedEntry.setDebitAmount(BigDecimal.valueOf(500));
        postedEntry.setCreditAmount(BigDecimal.ZERO);
        postedTx.setEntries(Arrays.asList(postedEntry));
        
        Transaction draftTx = new Transaction();
        draftTx.setDate(LocalDate.of(2024, 1, 20));
        draftTx.setStatus(TransactionStatus.DRAFT); // Should be excluded
        
        TransactionEntry draftEntry = new TransactionEntry();
        draftEntry.setAccount(cashAccount);
        draftEntry.setDebitAmount(BigDecimal.valueOf(9999)); // Should NOT affect balance
        draftEntry.setCreditAmount(BigDecimal.ZERO);
        draftTx.setEntries(Arrays.asList(draftEntry));
        
        List<Transaction> transactions = Arrays.asList(postedTx, draftTx);
        
        when(transactionRepository.findByDateRange(startDate, endDate))
                .thenReturn(transactions);

        // Act
        Map<String, BigDecimal> balances = invokeCalculateAccountBalances(accounts, startDate, endDate);

        // Assert: Only POSTED transaction should be included
        assertEquals(BigDecimal.valueOf(500), balances.get("1000"));
        assertNotEquals(BigDecimal.valueOf(500 + 9999), balances.get("1000"));
    }

    @Test
    void testCalculateAccountBalances_HandlesMultipleTransactions() {
        // Arrange
        Account cashAccount = new Account("1000", "Cash", AccountType.ASSET);
        List<Account> accounts = Arrays.asList(cashAccount);

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        // Create multiple transactions
        List<Transaction> transactions = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Transaction tx = new Transaction();
            tx.setDate(LocalDate.of(2024, 1, i));
            tx.setStatus(TransactionStatus.POSTED);
            
            TransactionEntry entry = new TransactionEntry();
            entry.setAccount(cashAccount);
            entry.setDebitAmount(BigDecimal.valueOf(100));
            entry.setCreditAmount(BigDecimal.ZERO);
            tx.setEntries(Arrays.asList(entry));
            
            transactions.add(tx);
        }
        
        when(transactionRepository.findByDateRange(startDate, endDate))
                .thenReturn(transactions);

        // Act
        Map<String, BigDecimal> balances = invokeCalculateAccountBalances(accounts, startDate, endDate);

        // Assert: Sum of all transactions (5 * 100)
        assertEquals(BigDecimal.valueOf(500), balances.get("1000"));
    }

    @Test
    void testCalculateAccountBalances_EmptyTransactionList() {
        // Arrange
        Account cashAccount = new Account("1000", "Cash", AccountType.ASSET);
        List<Account> accounts = Arrays.asList(cashAccount);

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        when(transactionRepository.findByDateRange(startDate, endDate))
                .thenReturn(new ArrayList<>());

        // Act
        Map<String, BigDecimal> balances = invokeCalculateAccountBalances(accounts, startDate, endDate);

        // Assert: Zero balance when no transactions
        assertEquals(BigDecimal.ZERO, balances.get("1000"));
    }

    /**
     * Helper method to invoke private calculateAccountBalances method via reflection.
     */
    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> invokeCalculateAccountBalances(
            List<Account> accounts, LocalDate startDate, LocalDate endDate) {
        try {
            java.lang.reflect.Method method = FinancialStatementService.class
                    .getDeclaredMethod("calculateAccountBalances", List.class, LocalDate.class, LocalDate.class);
            method.setAccessible(true);
            return (Map<String, BigDecimal>) method.invoke(service, accounts, startDate, endDate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke calculateAccountBalances", e);
        }
    }
}
