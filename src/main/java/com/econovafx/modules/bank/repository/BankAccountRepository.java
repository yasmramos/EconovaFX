package com.econovafx.modules.bank.repository;

import com.econovafx.modules.bank.model.BankAccount;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Bank Account data access using Ebean ORM.
 */
@Component
public class BankAccountRepository {
    
    private final Database database;

    @Inject
    public BankAccountRepository(Database database) {
        this.database = database;
    }

    public BankAccount save(BankAccount account) {
        database.save(account);
        return account;
    }

    public Optional<BankAccount> findById(Long id) {
        return Optional.ofNullable(database.find(BankAccount.class, id));
    }

    public List<BankAccount> findAll() {
        return database.find(BankAccount.class).findList();
    }

    public List<BankAccount> findActiveAccounts() {
        return database.find(BankAccount.class)
                .where().eq("active", true)
                .findList();
    }

    public Optional<BankAccount> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(database.find(BankAccount.class)
                .where().eq("accountNumber", accountNumber)
                .findOne());
    }

    public boolean deleteById(Long id) {
        int rowsDeleted = database.delete(BankAccount.class, id);
        return rowsDeleted > 0;
    }

    public void updateBalance(Long id, BigDecimal newBalance) {
        BankAccount account = database.find(BankAccount.class, id);
        if (account != null) {
            account.setBalance(newBalance);
            database.update(account);
        }
    }
}
