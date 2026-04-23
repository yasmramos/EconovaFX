package com.econovafx.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Account entity
 */
class AccountTest {

    @Test
    void testCreateAccountWithConstructor() {
        Account account = new Account("1001", "Cash", AccountType.ASSET);

        assertEquals("1001", account.getCode());
        assertEquals("Cash", account.getName());
        assertEquals(AccountType.ASSET, account.getType());
    }

    @Test
    void testAccountDefaultBalance() {
        Account account = new Account();
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    @Test
    void testSetAndGetBalance() {
        Account account = new Account();
        BigDecimal balance = new BigDecimal("1000.50");
        account.setBalance(balance);

        assertEquals(balance, account.getBalance());
    }

    @Test
    void testSetAndGetCode() {
        Account account = new Account();
        account.setCode("2001");
        assertEquals("2001", account.getCode());
    }

    @Test
    void testSetAndGetName() {
        Account account = new Account();
        account.setName("Accounts Payable");
        assertEquals("Accounts Payable", account.getName());
    }

    @Test
    void testSetAndGetType() {
        Account account = new Account();
        account.setType(AccountType.LIABILITY);
        assertEquals(AccountType.LIABILITY, account.getType());
    }

    @Test
    void testSetAndGetDescription() {
        Account account = new Account();
        account.setDescription("Main cash account");
        assertEquals("Main cash account", account.getDescription());
    }

    @Test
    void testParentChildRelationship() {
        Account parent = new Account("1000", "Assets", AccountType.ASSET);
        Account child = new Account("1001", "Cash", AccountType.ASSET);

        child.setParentAccount(parent);
        assertEquals(parent, child.getParentAccount());
    }

    @Test
    void testToString() {
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        assertEquals("1001 - Cash", account.toString());
    }

    @Test
    void testToStringWithNullCode() {
        Account account = new Account();
        account.setName("Test Account");
        assertEquals("null - Test Account", account.toString());
    }

    @Test
    void testTypeAsString() {
        Account account = new Account();
        account.setType(AccountType.EXPENSE);
        assertEquals("EXPENSE", account.getTypeAsString());
    }

    @Test
    void testTypeAsStringWithNull() {
        Account account = new Account();
        assertNull(account.getTypeAsString());
    }

    @Test
    void testChildAccountsInitialization() {
        Account account = new Account();
        assertNotNull(account.getChildAccounts());
        assertTrue(account.getChildAccounts().isEmpty());
    }

    @Test
    void testTransactionEntriesInitialization() {
        Account account = new Account();
        assertNotNull(account.getTransactionEntries());
        assertTrue(account.getTransactionEntries().isEmpty());
    }
}
