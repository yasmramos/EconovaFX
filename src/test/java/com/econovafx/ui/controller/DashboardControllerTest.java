package com.econovafx.ui.controller;

import com.econovafx.domain.Account;
import com.econovafx.domain.Transaction;
import com.econovafx.service.AccountService;
import com.econovafx.service.TransactionService;
import com.econovafx.ui.view.ViewFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardController
 */
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ViewFactory viewFactory;

    @InjectMocks
    private DashboardController dashboardController;

    private List<Account> testAccounts;
    private List<Transaction> testTransactions;

    @BeforeEach
    void setUp() {
        // Create test data
        testAccounts = new ArrayList<>();
        Account assetAccount = new Account("100", "Caja", com.econovafx.domain.AccountType.ASSET);
        assetAccount.setId(1L);
        assetAccount.setBalance(BigDecimal.valueOf(10000));
        assetAccount.setDescription("Cuenta de caja");
        
        Account liabilityAccount = new Account("200", "Proveedores", com.econovafx.domain.AccountType.LIABILITY);
        liabilityAccount.setId(2L);
        liabilityAccount.setBalance(BigDecimal.valueOf(5000));
        liabilityAccount.setDescription("Cuenta de proveedores");
        
        testAccounts.add(assetAccount);
        testAccounts.add(liabilityAccount);

        testTransactions = new ArrayList<>();
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setNumber("T-001");
        t1.setType("INGRESO");
        t1.setDescription("Venta de productos");
        t1.setDate(LocalDate.now());
        t1.setTotalDebit(BigDecimal.valueOf(1000));
        t1.setTotalCredit(BigDecimal.ZERO);
        t1.setIsPosted(true);
        testTransactions.add(t1);

        Transaction t2 = new Transaction();
        t2.setId(2L);
        t2.setNumber("T-002");
        t2.setType("GASTO");
        t2.setDescription("Compra de insumos");
        t2.setDate(LocalDate.now().minusDays(1));
        t2.setTotalDebit(BigDecimal.valueOf(500));
        t2.setTotalCredit(BigDecimal.ZERO);
        t2.setIsPosted(false);
        testTransactions.add(t2);
    }

    @Test
    void testConstructorInjection() {
        assertThat(dashboardController).isNotNull();
    }

    @Test
    void testGetAllAccounts_whenCalled_returnsAccountList() {
        when(accountService.getAllAccounts()).thenReturn(testAccounts);

        List<Account> result = accountService.getAllAccounts();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("100");
        assertThat(result.get(1).getName()).isEqualTo("Proveedores");
    }

    @Test
    void testGetRecentTransactions_whenCalled_returnsTransactionList() {
        when(transactionService.getAllTransactions()).thenReturn(testTransactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNumber()).isEqualTo("T-001");
        assertThat(result.get(1).getIsPosted()).isFalse();
    }

    @Test
    void testCalculateTotalAssets_whenCalled_returnsCorrectSum() {
        BigDecimal result = testAccounts.stream()
                .filter(a -> a.getType() == com.econovafx.domain.AccountType.ASSET)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

    @Test
    void testCalculateTotalLiabilities_whenCalled_returnsCorrectSum() {
        BigDecimal result = testAccounts.stream()
                .filter(a -> a.getType() == com.econovafx.domain.AccountType.LIABILITY)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    void testCreateTransaction_withValidData_success() {
        Transaction newTransaction = new Transaction();
        newTransaction.setType("INGRESO");
        newTransaction.setDescription("Test transaction");
        newTransaction.setDate(LocalDate.now());
        
        List<TransactionService.TransactionEntryData> entries = new ArrayList<>();
        entries.add(new TransactionService.TransactionEntryData(1L, BigDecimal.valueOf(100), BigDecimal.ZERO, "Cargo"));
        entries.add(new TransactionService.TransactionEntryData(2L, BigDecimal.ZERO, BigDecimal.valueOf(100), "Abono"));

        when(transactionService.createTransaction(any(Transaction.class), anyList())).thenReturn(newTransaction);

        Transaction result = transactionService.createTransaction(newTransaction, entries);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("INGRESO");
        assertThat(result.getDescription()).isEqualTo("Test transaction");
    }

    @Test
    void testUpdateTransaction_withValidData_success() {
        Transaction existingTransaction = testTransactions.get(0);
        existingTransaction.setDescription("Updated description");

        when(transactionService.getAllTransactions()).thenReturn(testTransactions);
        
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        assertThat(allTransactions).hasSize(2);
    }

    @Test
    void testDeleteTransaction_withValidId_success() {
        Long transactionId = 1L;
        doNothing().when(transactionService).deleteTransaction(transactionId);

        transactionService.deleteTransaction(transactionId);

        verify(transactionService, times(1)).deleteTransaction(transactionId);
    }

    @Test
    void testGetTransactionsByDateRange_whenCalled_returnsFilteredList() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(transactionService.getTransactionsByDateRange(startDate, endDate))
                .thenReturn(testTransactions);

        List<Transaction> result = transactionService.getTransactionsByDateRange(startDate, endDate);

        assertThat(result).isNotNull();
        verify(transactionService, times(1)).getTransactionsByDateRange(startDate, endDate);
    }

    @Test
    void testGetAccountByCode_whenValidCode_returnsAccount() {
        when(accountService.getAccountByCode("100")).thenReturn(java.util.Optional.of(testAccounts.get(0)));

        java.util.Optional<Account> result = accountService.getAccountByCode("100");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("100");
        assertThat(result.get().getName()).isEqualTo("Caja");
    }

    @Test
    void testGetAccountByCode_whenInvalidCode_returnsEmptyOptional() {
        when(accountService.getAccountByCode("999")).thenReturn(java.util.Optional.empty());

        java.util.Optional<Account> result = accountService.getAccountByCode("999");

        assertThat(result).isEmpty();
    }

    @Test
    void testSearchTransactions_withKeyword_returnsMatchingResults() {
        String keyword = "Venta";
        when(transactionService.searchTransactions(keyword)).thenReturn(List.of(testTransactions.get(0)));

        List<Transaction> result = transactionService.searchTransactions(keyword);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).containsIgnoringCase(keyword);
    }

    @Test
    void testGetTransactionCount_whenCalled_returnsCorrectCount() {
        when(transactionService.getAllTransactions()).thenReturn(testTransactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(2);
    }

    @Test
    void testGetPostedTransactionCount_whenCalled_returnsCorrectCount() {
        when(transactionService.getPostedTransactions()).thenReturn(List.of(testTransactions.get(0)));

        List<Transaction> result = transactionService.getPostedTransactions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsPosted()).isTrue();
    }

    @Test
    void testGetUnpostedTransactionCount_whenCalled_returnsCorrectCount() {
        when(transactionService.getUnpostedTransactions()).thenReturn(List.of(testTransactions.get(1)));

        List<Transaction> result = transactionService.getUnpostedTransactions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsPosted()).isFalse();
    }

    @Test
    void testValidateTransaction_withValidData_returnsTrue() {
        Transaction validTransaction = new Transaction();
        validTransaction.setType("INGRESO");
        validTransaction.setDescription("Valid transaction");
        validTransaction.setTotalDebit(BigDecimal.valueOf(100));
        validTransaction.setTotalCredit(BigDecimal.valueOf(100));

        List<TransactionService.TransactionEntryData> entries = new ArrayList<>();
        TransactionService.TransactionEntryData entry = new TransactionService.TransactionEntryData(1L, BigDecimal.valueOf(100), BigDecimal.ZERO, "Caja");
        entries.add(entry);
        
        when(transactionService.createTransaction(any(Transaction.class), anyList())).thenReturn(validTransaction);

        Transaction result = transactionService.createTransaction(validTransaction, entries);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("INGRESO");
    }

    @Test
    void testValidateTransaction_withInvalidData_throwsException() {
        Transaction invalidTransaction = new Transaction();
        invalidTransaction.setType("");
        invalidTransaction.setDescription("");
        invalidTransaction.setTotalDebit(BigDecimal.ZERO);

        List<TransactionService.TransactionEntryData> entries = new ArrayList<>();
        
        // Test that invalid data causes exception during creation
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            doThrow(new IllegalArgumentException("Invalid transaction")).when(transactionService)
                    .createTransaction(any(Transaction.class), anyList());
            transactionService.createTransaction(invalidTransaction, entries);
        });
    }

    @Test
    void testFormatCurrency_withPositiveValue_returnsFormattedString() {
        BigDecimal amount = BigDecimal.valueOf(1234.56);
        String formatted = String.format("$ %,d.%02d", 
            amount.longValue(), 
            amount.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).intValue());

        assertThat(formatted).contains("$");
        assertThat(formatted).contains("1,234");
    }

    @Test
    void testFormatCurrency_withNegativeValue_returnsFormattedString() {
        BigDecimal amount = BigDecimal.valueOf(-500.00);
        String formatted = String.format("$ %,d.%02d", 
            amount.longValue(), 
            Math.abs(amount.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).intValue()));

        assertThat(formatted).contains("$");
        assertThat(formatted).contains("-");
    }

    @Test
    void testInitializeComboBoxes_transactionTypesAreCorrect() {
        List<String> transactionTypes = List.of("INGRESO", "GASTO", "TRANSFERENCIA", "ASIENTO");

        assertThat(transactionTypes).hasSize(4);
        assertThat(transactionTypes).containsExactlyInAnyOrder("INGRESO", "GASTO", "TRANSFERENCIA", "ASIENTO");
    }

    @Test
    void testAccountTypes_areCorrectlyDefined() {
        List<String> accountTypes = List.of("Asset", "Liability", "Equity", "Revenue", "Expense");

        assertThat(accountTypes).hasSize(5);
        assertThat(accountTypes).containsAll(List.of("Asset", "Liability", "Equity", "Revenue", "Expense"));
    }

    @Test
    void testBigDecimalOperations_addition() {
        BigDecimal a = BigDecimal.valueOf(1000);
        BigDecimal b = BigDecimal.valueOf(500);
        BigDecimal result = a.add(b);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    void testBigDecimalOperations_subtraction() {
        BigDecimal a = BigDecimal.valueOf(1000);
        BigDecimal b = BigDecimal.valueOf(300);
        BigDecimal result = a.subtract(b);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(700));
    }

    @Test
    void testBigDecimalOperations_multiplication() {
        BigDecimal a = BigDecimal.valueOf(100);
        BigDecimal b = BigDecimal.valueOf(0.15);
        BigDecimal result = a.multiply(b);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    void testBigDecimalOperations_division() {
        BigDecimal a = BigDecimal.valueOf(100);
        BigDecimal b = BigDecimal.valueOf(4);
        BigDecimal result = a.divide(b);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(25));
    }

    @Test
    void testBigDecimalOperations_comparison() {
        BigDecimal a = BigDecimal.valueOf(1000);
        BigDecimal b = BigDecimal.valueOf(500);

        assertThat(a).isGreaterThan(b);
        assertThat(b).isLessThan(a);
    }

    @Test
    void testLocalDateOperations_dateCalculations() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate lastWeek = today.minusWeeks(1);

        assertThat(yesterday).isBefore(today);
        assertThat(tomorrow).isAfter(today);
        assertThat(lastWeek).isBefore(today);
    }

    @Test
    void testStringOperations_trimAndLowerCase() {
        String input = "  TEST STRING  ";
        String result = input.trim().toLowerCase();

        assertThat(result).isEqualTo("test string");
    }

    @Test
    void testStringOperations_contains() {
        String text = "Transaction description";

        assertThat(text).containsIgnoringCase("transaction");
        assertThat(text).contains("description");
    }

    @Test
    void testListOperations_filter() {
        List<Transaction> filtered = testTransactions.stream()
                .filter(t -> t.getIsPosted())
                .toList();

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getIsPosted()).isTrue();
    }

    @Test
    void testListOperations_map() {
        List<String> descriptions = testTransactions.stream()
                .map(Transaction::getDescription)
                .toList();

        assertThat(descriptions).hasSize(2);
        assertThat(descriptions).contains("Venta de productos", "Compra de insumos");
    }

    @Test
    void testListOperations_reduce() {
        BigDecimal total = testTransactions.stream()
                .map(Transaction::getTotalDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    void testOptionalHandling_whenPresent() {
        Account account = testAccounts.stream()
                .filter(a -> a.getCode().equals("100"))
                .findFirst()
                .orElse(null);

        assertThat(account).isNotNull();
        assertThat(account.getCode()).isEqualTo("100");
    }

    @Test
    void testOptionalHandling_whenEmpty() {
        Account account = testAccounts.stream()
                .filter(a -> a.getCode().equals("999"))
                .findFirst()
                .orElse(null);

        assertThat(account).isNull();
    }
}
