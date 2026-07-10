package com.econovafx.testutil;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.core.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Test data factory for creating test entities
 */
public class TestDataFactory {

    public static Account createAccount() {
        return createAccount("1001", "Test Account", AccountType.ASSET);
    }

    public static Account createAccount(String code, String name, AccountType type) {
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setType(type);
        account.setBalance(BigDecimal.ZERO);
        account.setIsActive(true);
        return account;
    }

    public static Account createAccountWithBalance(String code, String name, AccountType type, BigDecimal balance) {
        Account account = createAccount(code, name, type);
        account.setBalance(balance);
        return account;
    }

    public static Transaction createTransaction() {
        Transaction transaction = new Transaction();
        transaction.setNumber("TXN-2026-000001");
        transaction.setDate(LocalDate.now());
        transaction.setType("JOURNAL");
        transaction.setDescription("Test transaction");
        transaction.setIsActive(true);
        return transaction;
    }

    public static TransactionEntry createTransactionEntry(Transaction transaction, Account account,
                                                          BigDecimal debit, BigDecimal credit) {
        TransactionEntry entry = new TransactionEntry();
        entry.setTransaction(transaction);
        entry.setAccount(account);
        entry.setDebitAmount(debit);
        entry.setCreditAmount(credit);
        entry.setDescription("Test entry");
        return entry;
    }

    public static User createUser() {
        return createUser("testuser", "test@example.com", "Test User");
    }

    public static User createUser(String username, String email, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword("hashed_password123");
        user.setRole(User.UserRole.USER);
        user.setIsActive(true);
        return user;
    }

    public static User createAdminUser() {
        User user = createUser("admin", "admin@example.com", "Admin User");
        user.setRole(User.UserRole.ADMIN);
        return user;
    }
}
