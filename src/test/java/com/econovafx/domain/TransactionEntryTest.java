package com.econovafx.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionEntry entity
 */
class TransactionEntryTest {

    @Test
    void testCreateTransactionEntryWithConstructor() {
        Transaction transaction = new Transaction();
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        BigDecimal debit = new BigDecimal("500.00");
        BigDecimal credit = BigDecimal.ZERO;

        TransactionEntry entry = new TransactionEntry(transaction, account, debit, credit);

        assertEquals(transaction, entry.getTransaction());
        assertEquals(account, entry.getAccount());
        assertEquals(debit, entry.getDebitAmount());
        assertEquals(credit, entry.getCreditAmount());
    }

    @Test
    void testDefaultAmounts() {
        TransactionEntry entry = new TransactionEntry();
        assertEquals(BigDecimal.ZERO, entry.getDebitAmount());
        assertEquals(BigDecimal.ZERO, entry.getCreditAmount());
    }

    @Test
    void testSetAndGetDebitAmount() {
        TransactionEntry entry = new TransactionEntry();
        BigDecimal debit = new BigDecimal("1000.00");
        entry.setDebitAmount(debit);

        assertEquals(debit, entry.getDebitAmount());
    }

    @Test
    void testSetAndGetCreditAmount() {
        TransactionEntry entry = new TransactionEntry();
        BigDecimal credit = new BigDecimal("750.00");
        entry.setCreditAmount(credit);

        assertEquals(credit, entry.getCreditAmount());
    }

    @Test
    void testSetAndGetAccount() {
        TransactionEntry entry = new TransactionEntry();
        Account account = new Account("2001", "Accounts Payable", AccountType.LIABILITY);
        entry.setAccount(account);

        assertEquals(account, entry.getAccount());
    }

    @Test
    void testSetAndGetTransaction() {
        TransactionEntry entry = new TransactionEntry();
        Transaction transaction = new Transaction();
        entry.setTransaction(transaction);

        assertEquals(transaction, entry.getTransaction());
    }

    @Test
    void testSetAndGetDescription() {
        TransactionEntry entry = new TransactionEntry();
        entry.setDescription("Payment received");
        assertEquals("Payment received", entry.getDescription());
    }

    @Test
    void testNetAmountWithDebitOnly() {
        TransactionEntry entry = new TransactionEntry();
        entry.setDebitAmount(new BigDecimal("1000.00"));
        entry.setCreditAmount(BigDecimal.ZERO);

        assertEquals(new BigDecimal("1000.00"), entry.getNetAmount());
    }

    @Test
    void testNetAmountWithCreditOnly() {
        TransactionEntry entry = new TransactionEntry();
        entry.setDebitAmount(BigDecimal.ZERO);
        entry.setCreditAmount(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("-500.00"), entry.getNetAmount());
    }

    @Test
    void testNetAmountWithBothDebitAndCredit() {
        TransactionEntry entry = new TransactionEntry();
        entry.setDebitAmount(new BigDecimal("1000.00"));
        entry.setCreditAmount(new BigDecimal("300.00"));

        assertEquals(new BigDecimal("700.00"), entry.getNetAmount());
    }

    @Test
    void testNetAmountWithEqualDebitAndCredit() {
        TransactionEntry entry = new TransactionEntry();
        entry.setDebitAmount(new BigDecimal("500.00"));
        entry.setCreditAmount(new BigDecimal("500.00"));

        assertEquals(BigDecimal.ZERO, entry.getNetAmount().stripTrailingZeros());
    }

    @Test
    void testNetAmountWithZeroAmounts() {
        TransactionEntry entry = new TransactionEntry();
        entry.setDebitAmount(BigDecimal.ZERO);
        entry.setCreditAmount(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, entry.getNetAmount());
    }
}
