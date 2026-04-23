package com.econovafx.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Transaction entity
 */
class TransactionTest {

    @Test
    void testCreateTransaction() {
        Transaction transaction = new Transaction();
        transaction.setNumber("TXN-2026-000001");
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");
        transaction.setDescription("Test transaction");

        assertEquals("TXN-2026-000001", transaction.getNumber());
        assertEquals(LocalDate.now(), transaction.getDate());
        assertEquals("JOURNAL", transaction.getType());
        assertEquals("Test transaction", transaction.getDescription());
    }

    @Test
    void testDefaultIsPosted() {
        Transaction transaction = new Transaction();
        assertFalse(transaction.getIsPosted());
    }

    @Test
    void testSetAndGetIsPosted() {
        Transaction transaction = new Transaction();
        transaction.setIsPosted(true);
        assertTrue(transaction.getIsPosted());
    }

    @Test
    void testDefaultTotals() {
        Transaction transaction = new Transaction();
        assertEquals(BigDecimal.ZERO, transaction.getTotalDebit());
        assertEquals(BigDecimal.ZERO, transaction.getTotalCredit());
    }

    @Test
    void testSetAndGetTotals() {
        Transaction transaction = new Transaction();
        BigDecimal debit = new BigDecimal("1000.00");
        BigDecimal credit = new BigDecimal("1000.00");
        transaction.setTotalDebit(debit);
        transaction.setTotalCredit(credit);

        assertEquals(debit, transaction.getTotalDebit());
        assertEquals(credit, transaction.getTotalCredit());
    }

    @Test
    void testSetAndGetReference() {
        Transaction transaction = new Transaction();
        transaction.setReference("REF-001");
        assertEquals("REF-001", transaction.getReference());
    }

    @Test
    void testSetAndGetCreatedBy() {
        Transaction transaction = new Transaction();
        User user = new User();
        user.setUsername("testuser");
        transaction.setCreatedBy(user);

        assertEquals(user, transaction.getCreatedBy());
    }

    @Test
    void testIsBalancedWithEqualTotals() {
        Transaction transaction = new Transaction();
        transaction.setTotalDebit(new BigDecimal("1000.00"));
        transaction.setTotalCredit(new BigDecimal("1000.00"));

        assertTrue(transaction.isBalanced());
    }

    @Test
    void testIsNotBalancedWithUnequalTotals() {
        Transaction transaction = new Transaction();
        transaction.setTotalDebit(new BigDecimal("1000.00"));
        transaction.setTotalCredit(new BigDecimal("800.00"));

        assertFalse(transaction.isBalanced());
    }

    @Test
    void testIsBalancedWithZeroTotals() {
        Transaction transaction = new Transaction();
        transaction.setTotalDebit(BigDecimal.ZERO);
        transaction.setTotalCredit(BigDecimal.ZERO);

        assertTrue(transaction.isBalanced());
    }

    @Test
    void testAddEntryUpdatesTotals() {
        Transaction transaction = new Transaction();
        Account debitAccount = new Account("1001", "Cash", AccountType.ASSET);
        Account creditAccount = new Account("4001", "Sales", AccountType.REVENUE);

        TransactionEntry debitEntry = new TransactionEntry(transaction, debitAccount, 
                                                           new BigDecimal("500.00"), BigDecimal.ZERO);
        transaction.addEntry(debitEntry);

        assertEquals(new BigDecimal("500.00"), transaction.getTotalDebit());
        assertEquals(BigDecimal.ZERO, transaction.getTotalCredit());
    }

    @Test
    void testAddMultipleEntriesUpdatesTotals() {
        Transaction transaction = new Transaction();
        Account assetAccount = new Account("1001", "Cash", AccountType.ASSET);
        Account revenueAccount = new Account("4001", "Sales", AccountType.REVENUE);

        TransactionEntry debitEntry = new TransactionEntry(transaction, assetAccount,
                                                           new BigDecimal("1000.00"), BigDecimal.ZERO);
        TransactionEntry creditEntry = new TransactionEntry(transaction, revenueAccount,
                                                            BigDecimal.ZERO, new BigDecimal("1000.00"));
        
        transaction.addEntry(debitEntry);
        transaction.addEntry(creditEntry);

        assertEquals(new BigDecimal("1000.00"), transaction.getTotalDebit());
        assertEquals(new BigDecimal("1000.00"), transaction.getTotalCredit());
        assertTrue(transaction.isBalanced());
    }

    @Test
    void testAddEntrySetsBackReference() {
        Transaction transaction = new Transaction();
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        TransactionEntry entry = new TransactionEntry();
        entry.setAccount(account);
        entry.setDebitAmount(new BigDecimal("500.00"));
        entry.setCreditAmount(BigDecimal.ZERO);

        transaction.addEntry(entry);

        assertEquals(transaction, entry.getTransaction());
        assertTrue(transaction.getEntries().contains(entry));
    }

    @Test
    void testAddEntryWithZeroDebitDoesNotUpdateTotalDebit() {
        Transaction transaction = new Transaction();
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        TransactionEntry entry = new TransactionEntry(transaction, account,
                                                      BigDecimal.ZERO, new BigDecimal("500.00"));
        
        transaction.addEntry(entry);

        assertEquals(BigDecimal.ZERO, transaction.getTotalDebit());
        assertEquals(new BigDecimal("500.00"), transaction.getTotalCredit());
    }

    @Test
    void testAddEntryWithZeroCreditDoesNotUpdateTotalCredit() {
        Transaction transaction = new Transaction();
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        TransactionEntry entry = new TransactionEntry(transaction, account,
                                                      new BigDecimal("500.00"), BigDecimal.ZERO);
        
        transaction.addEntry(entry);

        assertEquals(new BigDecimal("500.00"), transaction.getTotalDebit());
        assertEquals(BigDecimal.ZERO, transaction.getTotalCredit());
    }

    @Test
    void testGetEntriesReturnsMutableList() {
        Transaction transaction = new Transaction();
        assertNotNull(transaction.getEntries());
        assertTrue(transaction.getEntries().isEmpty());

        Account account = new Account("1001", "Cash", AccountType.ASSET);
        TransactionEntry entry = new TransactionEntry(transaction, account,
                                                      new BigDecimal("100.00"), BigDecimal.ZERO);
        transaction.addEntry(entry);

        assertEquals(1, transaction.getEntries().size());
    }

    @Test
    void testComplexTransactionWithMultipleEntries() {
        Transaction transaction = new Transaction();
        transaction.setNumber("TXN-2026-000001");
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");

        Account cashAccount = new Account("1001", "Cash", AccountType.ASSET);
        Account salesAccount = new Account("4001", "Sales", AccountType.REVENUE);
        Account taxAccount = new Account("2001", "Tax Payable", AccountType.LIABILITY);

        // Debit cash 1100, Credit sales 1000, Credit tax 100
        TransactionEntry debitEntry = new TransactionEntry(transaction, cashAccount,
                                                           new BigDecimal("1100.00"), BigDecimal.ZERO);
        TransactionEntry creditEntry1 = new TransactionEntry(transaction, salesAccount,
                                                             BigDecimal.ZERO, new BigDecimal("1000.00"));
        TransactionEntry creditEntry2 = new TransactionEntry(transaction, taxAccount,
                                                             BigDecimal.ZERO, new BigDecimal("100.00"));

        transaction.addEntry(debitEntry);
        transaction.addEntry(creditEntry1);
        transaction.addEntry(creditEntry2);

        assertEquals(3, transaction.getEntries().size());
        assertEquals(new BigDecimal("1100.00"), transaction.getTotalDebit());
        assertEquals(new BigDecimal("1100.00"), transaction.getTotalCredit());
        assertTrue(transaction.isBalanced());
    }
}
