package com.econovafx.service;

import com.econovafx.domain.*;
import com.econovafx.repository.AccountRepository;
import com.econovafx.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionService with manual stub mocks
 */
class TransactionServiceTest {

    private StubTransactionRepository transactionRepository;
    private StubAccountRepositoryForTransaction accountRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = new StubTransactionRepository();
        accountRepository = new StubAccountRepositoryForTransaction();
        transactionService = new TransactionService(transactionRepository, accountRepository);
    }

    @Test
    void testCreateTransactionSuccess() {
        Account debitAccount = accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        Account creditAccount = accountRepository.save(new Account("4001", "Sales", AccountType.REVENUE));

        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");
        transaction.setDescription("Test transaction");

        List<TransactionService.TransactionEntryData> entries = List.of(
            new TransactionService.TransactionEntryData(debitAccount.getId(), new BigDecimal("1000.00"), BigDecimal.ZERO, "Debit cash"),
            new TransactionService.TransactionEntryData(creditAccount.getId(), BigDecimal.ZERO, new BigDecimal("1000.00"), "Credit sales")
        );

        Transaction result = transactionService.createTransaction(transaction, entries);

        assertNotNull(result);
        assertTrue(transactionRepository.saveCalled);
    }

    @Test
    void testCreateTransactionGeneratesNumber() {
        accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        accountRepository.save(new Account("4001", "Sales", AccountType.REVENUE));

        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");

        List<TransactionService.TransactionEntryData> entries = List.of(
            new TransactionService.TransactionEntryData(1L, new BigDecimal("500.00"), BigDecimal.ZERO, "Debit"),
            new TransactionService.TransactionEntryData(2L, BigDecimal.ZERO, new BigDecimal("500.00"), "Credit")
        );

        Transaction result = transactionService.createTransaction(transaction, entries);

        assertNotNull(result.getNumber());
        assertTrue(result.getNumber().startsWith("TXN-2026-"));
    }

    @Test
    void testCreateTransactionFailsWhenNotBalanced() {
        accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        accountRepository.save(new Account("4001", "Sales", AccountType.REVENUE));

        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");

        List<TransactionService.TransactionEntryData> entries = List.of(
            new TransactionService.TransactionEntryData(1L, new BigDecimal("1000.00"), BigDecimal.ZERO, "Debit"),
            new TransactionService.TransactionEntryData(2L, BigDecimal.ZERO, new BigDecimal("800.00"), "Credit")
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.createTransaction(transaction, entries)
        );

        assertTrue(exception.getMessage().contains("not balanced"));
        assertFalse(transactionRepository.saveCalled);
    }

    @Test
    void testCreateTransactionFailsWithNullDate() {
        Transaction transaction = new Transaction();
        transaction.setType("JOURNAL");

        List<TransactionService.TransactionEntryData> entries = List.of(
            new TransactionService.TransactionEntryData(1L, new BigDecimal("100.00"), BigDecimal.ZERO, "Debit"),
            new TransactionService.TransactionEntryData(2L, BigDecimal.ZERO, new BigDecimal("100.00"), "Credit")
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.createTransaction(transaction, entries)
        );

        assertEquals("Transaction date is required", exception.getMessage());
    }

    @Test
    void testCreateTransactionFailsWithNullType() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());

        List<TransactionService.TransactionEntryData> entries = List.of(
            new TransactionService.TransactionEntryData(1L, new BigDecimal("100.00"), BigDecimal.ZERO, "Debit"),
            new TransactionService.TransactionEntryData(2L, BigDecimal.ZERO, new BigDecimal("100.00"), "Credit")
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.createTransaction(transaction, entries)
        );

        assertEquals("Transaction type is required", exception.getMessage());
    }

    @Test
    void testCreateTransactionFailsWithLessThanTwoEntries() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");

        List<TransactionService.TransactionEntryData> entries = List.of(
            new TransactionService.TransactionEntryData(1L, new BigDecimal("100.00"), BigDecimal.ZERO, "Debit")
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.createTransaction(transaction, entries)
        );

        assertEquals("Transaction must have at least 2 entries", exception.getMessage());
    }

    @Test
    void testCreateTransactionFailsWhenAccountNotFound() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");

        List<TransactionService.TransactionEntryData> entries = List.of(
            new TransactionService.TransactionEntryData(999L, new BigDecimal("100.00"), BigDecimal.ZERO, "Debit"),
            new TransactionService.TransactionEntryData(2L, BigDecimal.ZERO, new BigDecimal("100.00"), "Credit")
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.createTransaction(transaction, entries)
        );

        assertEquals("Account not found: 999", exception.getMessage());
    }

    @Test
    void testPostTransactionSuccess() {
        Account debitAccount = accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        debitAccount.setBalance(new BigDecimal("5000.00"));
        
        Account creditAccount = accountRepository.save(new Account("4001", "Sales", AccountType.REVENUE));
        creditAccount.setBalance(new BigDecimal("10000.00"));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setNumber("TXN-2026-000001");
        transaction.setIsPosted(false);
        transaction.setTotalDebit(new BigDecimal("1000.00"));
        transaction.setTotalCredit(new BigDecimal("1000.00"));

        TransactionEntry debitEntry = new TransactionEntry(transaction, debitAccount,
                                                           new BigDecimal("1000.00"), BigDecimal.ZERO);
        TransactionEntry creditEntry = new TransactionEntry(transaction, creditAccount,
                                                            BigDecimal.ZERO, new BigDecimal("1000.00"));
        transaction.setEntries(List.of(debitEntry, creditEntry));
        
        transactionRepository.save(transaction);

        Transaction result = transactionService.postTransaction(1L);

        assertTrue(result.getIsPosted());
        assertEquals(2, accountRepository.updateCallCount);
        assertTrue(transactionRepository.updateCalled);
    }

    @Test
    void testPostTransactionFailsWhenAlreadyPosted() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setIsPosted(true);
        transactionRepository.save(transaction);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transactionService.postTransaction(1L)
        );

        assertEquals("Transaction already posted", exception.getMessage());
    }

    @Test
    void testPostTransactionFailsWhenNotBalanced() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setIsPosted(false);
        transaction.setTotalDebit(new BigDecimal("1000.00"));
        transaction.setTotalCredit(new BigDecimal("800.00"));
        transactionRepository.save(transaction);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transactionService.postTransaction(1L)
        );

        assertEquals("Transaction is not balanced", exception.getMessage());
    }

    @Test
    void testPostTransactionFailsWhenNotFound() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.postTransaction(999L)
        );

        assertEquals("Transaction not found: 999", exception.getMessage());
    }

    @Test
    void testPostTransactionUpdatesAssetAccountCorrectly() {
        Account assetAccount = accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        assetAccount.setBalance(new BigDecimal("5000.00"));
        
        Account revenueAccount = accountRepository.save(new Account("4001", "Sales", AccountType.REVENUE));
        revenueAccount.setBalance(new BigDecimal("10000.00"));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setIsPosted(false);
        transaction.setTotalDebit(new BigDecimal("500.00"));
        transaction.setTotalCredit(new BigDecimal("500.00"));

        TransactionEntry debitEntry = new TransactionEntry(transaction, assetAccount,
                                                           new BigDecimal("500.00"), BigDecimal.ZERO);
        TransactionEntry creditEntry = new TransactionEntry(transaction, revenueAccount,
                                                            BigDecimal.ZERO, new BigDecimal("500.00"));
        transaction.setEntries(List.of(debitEntry, creditEntry));
        
        transactionRepository.save(transaction);

        transactionService.postTransaction(1L);

        // Asset account should increase with debit
        assertEquals(new BigDecimal("5500.00"), assetAccount.getBalance());
        // Revenue account should increase with credit
        assertEquals(new BigDecimal("10500.00"), revenueAccount.getBalance());
    }

    @Test
    void testPostTransactionUpdatesExpenseAccountCorrectly() {
        Account expenseAccount = accountRepository.save(new Account("5001", "Rent Expense", AccountType.EXPENSE));
        expenseAccount.setBalance(new BigDecimal("2000.00"));
        
        Account assetAccount = accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        assetAccount.setBalance(new BigDecimal("5000.00"));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setIsPosted(false);
        transaction.setTotalDebit(new BigDecimal("300.00"));
        transaction.setTotalCredit(new BigDecimal("300.00"));

        TransactionEntry debitEntry = new TransactionEntry(transaction, expenseAccount,
                                                           new BigDecimal("300.00"), BigDecimal.ZERO);
        TransactionEntry creditEntry = new TransactionEntry(transaction, assetAccount,
                                                            BigDecimal.ZERO, new BigDecimal("300.00"));
        transaction.setEntries(List.of(debitEntry, creditEntry));
        
        transactionRepository.save(transaction);

        transactionService.postTransaction(1L);

        // Expense account should increase with debit
        assertEquals(new BigDecimal("2300.00"), expenseAccount.getBalance());
        // Asset account should decrease with credit
        assertEquals(new BigDecimal("4700.00"), assetAccount.getBalance());
    }

    @Test
    void testPostTransactionUpdatesLiabilityAccountCorrectly() {
        Account assetAccount = accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        assetAccount.setBalance(new BigDecimal("5000.00"));
        
        Account liabilityAccount = accountRepository.save(new Account("2001", "Loan Payable", AccountType.LIABILITY));
        liabilityAccount.setBalance(new BigDecimal("10000.00"));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setIsPosted(false);
        transaction.setTotalDebit(new BigDecimal("1000.00"));
        transaction.setTotalCredit(new BigDecimal("1000.00"));

        TransactionEntry debitEntry = new TransactionEntry(transaction, assetAccount,
                                                           new BigDecimal("1000.00"), BigDecimal.ZERO);
        TransactionEntry creditEntry = new TransactionEntry(transaction, liabilityAccount,
                                                            BigDecimal.ZERO, new BigDecimal("1000.00"));
        transaction.setEntries(List.of(debitEntry, creditEntry));
        
        transactionRepository.save(transaction);

        transactionService.postTransaction(1L);

        // Asset account should increase with debit
        assertEquals(new BigDecimal("6000.00"), assetAccount.getBalance());
        // Liability account should increase with credit
        assertEquals(new BigDecimal("11000.00"), liabilityAccount.getBalance());
    }

    @Test
    void testReverseTransactionSuccess() {
        Account debitAccount = accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        Account creditAccount = accountRepository.save(new Account("4001", "Sales", AccountType.REVENUE));

        Transaction original = new Transaction();
        original.setId(1L);
        original.setNumber("TXN-2026-000001");
        original.setIsPosted(true);
        original.setDate(LocalDate.now().minusDays(5));
        original.setType("JOURNAL");
        original.setDescription("Original transaction");

        TransactionEntry debitEntry = new TransactionEntry(original, debitAccount,
                                                           new BigDecimal("500.00"), BigDecimal.ZERO);
        debitEntry.setDescription("Debit entry");
        TransactionEntry creditEntry = new TransactionEntry(original, creditAccount,
                                                            BigDecimal.ZERO, new BigDecimal("500.00"));
        creditEntry.setDescription("Credit entry");
        original.setEntries(List.of(debitEntry, creditEntry));
        
        transactionRepository.save(original);

        Transaction result = transactionService.reverseTransaction(1L, "Error in original");

        assertNotNull(result);
        assertEquals(2, transactionRepository.saveCount); // Original + reversal
    }

    @Test
    void testReverseTransactionFailsWhenNotPosted() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setIsPosted(false);
        transactionRepository.save(transaction);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transactionService.reverseTransaction(1L, "Test reason")
        );

        assertEquals("Cannot reverse unposted transaction", exception.getMessage());
    }

    @Test
    void testReverseTransactionFailsWhenNotFound() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.reverseTransaction(999L, "Test reason")
        );

        assertEquals("Transaction not found: 999", exception.getMessage());
    }

    @Test
    void testDeleteTransactionSuccess() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setNumber("TXN-2026-000001");
        transaction.setIsPosted(false);
        transactionRepository.save(transaction);

        transactionService.deleteTransaction(1L);

        assertTrue(transactionRepository.deleteCalled);
        assertEquals(0, transactionRepository.count());
    }

    @Test
    void testDeleteTransactionFailsWhenPosted() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setIsPosted(true);
        transactionRepository.save(transaction);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transactionService.deleteTransaction(1L)
        );

        assertEquals("Cannot delete posted transaction. Please reverse it.", exception.getMessage());
        assertFalse(transactionRepository.deleteCalled);
    }

    @Test
    void testDeleteTransactionFailsWhenNotFound() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transactionService.deleteTransaction(999L)
        );

        assertEquals("Transaction not found: 999", exception.getMessage());
    }

    @Test
    void testGetTransactionById() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setNumber("TXN-2026-000001");
        transactionRepository.save(transaction);

        Optional<Transaction> result = transactionService.getTransactionById(1L);

        assertTrue(result.isPresent());
        assertEquals("TXN-2026-000001", result.get().getNumber());
    }

    @Test
    void testGetTransactionByNumber() {
        Transaction transaction = new Transaction();
        transaction.setNumber("TXN-2026-000001");
        transactionRepository.save(transaction);

        Optional<Transaction> result = transactionService.getTransactionByNumber("TXN-2026-000001");

        assertTrue(result.isPresent());
    }

    @Test
    void testGetAllTransactions() {
        transactionService.createTransaction(
            createTransactionData(),
            createBalancedEntries()
        );
        transactionService.createTransaction(
            createTransactionData(),
            createBalancedEntries()
        );

        List<Transaction> result = transactionService.getAllTransactions();

        assertEquals(2, result.size());
    }

    @Test
    void testGetTransactionsByDateRange() {
        transactionService.createTransaction(createTransactionData(), createBalancedEntries());

        List<Transaction> result = transactionService.getTransactionsByDateRange(
            LocalDate.now().minusDays(30), LocalDate.now().plusDays(1)
        );

        assertEquals(1, result.size());
    }

    @Test
    void testGetPostedTransactions() {
        Transaction txn = transactionService.createTransaction(createTransactionData(), createBalancedEntries());
        transactionService.postTransaction(txn.getId());

        List<Transaction> result = transactionService.getPostedTransactions();

        assertEquals(1, result.size());
    }

    @Test
    void testGetUnpostedTransactions() {
        transactionService.createTransaction(createTransactionData(), createBalancedEntries());

        List<Transaction> result = transactionService.getUnpostedTransactions();

        assertEquals(1, result.size());
    }

    @Test
    void testSearchTransactions() {
        Transaction transaction = new Transaction();
        transaction.setDescription("Payment for services");
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");
        transactionRepository.save(transaction);

        List<Transaction> result = transactionService.searchTransactions("Payment");

        assertEquals(1, result.size());
    }

    @Test
    void testGetTransactionsCount() {
        transactionService.createTransaction(createTransactionData(), createBalancedEntries());
        transactionService.createTransaction(createTransactionData(), createBalancedEntries());

        long count = transactionService.getTransactionsCount();

        assertEquals(2, count);
    }

    // Helper methods
    private Transaction createTransactionData() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");
        transaction.setDescription("Test transaction");
        return transaction;
    }

    private List<TransactionService.TransactionEntryData> createBalancedEntries() {
        Account debitAccount = accountRepository.save(new Account("1001", "Cash", AccountType.ASSET));
        Account creditAccount = accountRepository.save(new Account("4001", "Sales", AccountType.REVENUE));
        
        return List.of(
            new TransactionService.TransactionEntryData(debitAccount.getId(), new BigDecimal("100.00"), BigDecimal.ZERO, "Debit"),
            new TransactionService.TransactionEntryData(creditAccount.getId(), BigDecimal.ZERO, new BigDecimal("100.00"), "Credit")
        );
    }

    // Stub implementations
    private static class StubTransactionRepository extends TransactionRepository {
        boolean saveCalled = false;
        boolean updateCalled = false;
        boolean deleteCalled = false;
        int saveCount = 0;
        private List<Transaction> transactions = new ArrayList<>();
        private Long nextId = 1L;

        public StubTransactionRepository() {
            super(null);
        }

        @Override
        public Optional<Transaction> findById(Long id) {
            return transactions.stream()
                .filter(t -> t.getId() != null && t.getId().equals(id))
                .findFirst();
        }

        @Override
        public Optional<Transaction> findByNumber(String number) {
            return transactions.stream()
                .filter(t -> number.equals(t.getNumber()))
                .findFirst();
        }

        @Override
        public List<Transaction> findAll() {
            return new ArrayList<>(transactions);
        }

        @Override
        public List<Transaction> findByDateRange(LocalDate startDate, LocalDate endDate) {
            return transactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .toList();
        }

        @Override
        public List<Transaction> findPostedTransactions() {
            return transactions.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsPosted()))
                .toList();
        }

        @Override
        public List<Transaction> findUnpostedTransactions() {
            return transactions.stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsPosted()))
                .toList();
        }

        @Override
        public List<Transaction> searchByDescription(String searchTerm) {
            return transactions.stream()
                .filter(t -> t.getDescription() != null && 
                           t.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
        }

        @Override
        public Transaction save(Transaction transaction) {
            if (transaction.getId() == null) {
                transaction.setId(nextId++);
            }
            transactions.add(transaction);
            saveCalled = true;
            saveCount++;
            return transaction;
        }

        @Override
        public void update(Transaction transaction) {
            updateCalled = true;
        }

        @Override
        public void delete(Transaction transaction) {
            transactions.removeIf(t -> t.getId().equals(transaction.getId()));
            deleteCalled = true;
        }

        @Override
        public long count() {
            return transactions.size();
        }
    }

    private static class StubAccountRepositoryForTransaction extends AccountRepository {
        private List<Account> accounts = new ArrayList<>();
        private Long nextId = 1L;
        int updateCallCount = 0;

        public StubAccountRepositoryForTransaction() {
            super(null);
        }

        @Override
        public Optional<Account> findById(Long id) {
            return accounts.stream()
                .filter(a -> a.getId() != null && a.getId().equals(id))
                .findFirst();
        }

        @Override
        public Account save(Account account) {
            if (account.getId() == null) {
                account.setId(nextId++);
            }
            accounts.add(account);
            return account;
        }

        @Override
        public void update(Account account) {
            updateCallCount++;
        }
    }
}
