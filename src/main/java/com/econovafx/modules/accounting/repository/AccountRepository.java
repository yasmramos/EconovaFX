package com.econovafx.modules.accounting.repository;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import io.avaje.inject.Component;
import io.ebean.Database;
import io.ebean.ExpressionList;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Account entities
 */
@Component
public class AccountRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountRepository.class);
    
    private final Database database;
    
    @Inject
    public AccountRepository(Database database) {
        this.database = database;
    }
    
    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(database.find(Account.class, id));
    }
    
    public Optional<Account> findByCode(String code) {
        return Optional.ofNullable(database.find(Account.class)
                .where().eq("code", code).findOne());
    }
    
    public List<Account> findAll() {
        return database.find(Account.class)
                .orderBy().asc("code").findList();
    }
    
    public List<Account> findByType(AccountType type) {
        return database.find(Account.class)
                .where().eq("type", type).orderBy().asc("code").findList();
    }
    
    public List<Account> findByParentAccount(Long parentAccountId) {
        return database.find(Account.class)
                .where().eq("parentAccount.id", parentAccountId)
                .orderBy().asc("code").findList();
    }
    
    public List<Account> findRootAccounts() {
        return database.find(Account.class)
                .where().isNull("parentAccount")
                .orderBy().asc("code").findList();
    }
    
    public List<Account> searchByName(String searchTerm) {
        return database.find(Account.class)
                .where().ilike("name", "%" + searchTerm + "%")
                .orderBy().asc("code").findList();
    }
    
    public Account save(Account account) {
        database.save(account);
        logger.debug("Account saved: {}", account.getCode());
        return account;
    }
    
    public void update(Account account) {
        database.update(account);
        logger.debug("Account updated: {}", account.getCode());
    }
    
    public void delete(Account account) {
        database.delete(account);
        logger.debug("Account deleted: {}", account.getCode());
    }
    
    public void deleteById(Long id) {
        database.delete(Account.class, id);
        logger.debug("Account deleted by ID: {}", id);
    }
    
    public boolean existsByCode(String code) {
        return database.find(Account.class)
                .where().eq("code", code).exists();
    }
    
    public boolean existsById(Long id) {
        return database.find(Account.class, id) != null;
    }
    
    public long count() {
        return database.find(Account.class).findCount();
    }
    
    public long countByType(AccountType type) {
        return database.find(Account.class)
                .where().eq("type", type).findCount();
    }
    
    public List<Account> findActiveAccounts() {
        return database.find(Account.class)
                .where().eq("isActive", true)
                .orderBy().asc("code").findList();
    }
    
    public void updateBalance(Long accountId, BigDecimal amount) {
        Account account = database.find(Account.class, accountId);
        if (account != null) {
            account.setBalance(account.getBalance().add(amount));
            database.update(account);
            logger.debug("Account balance updated: {} -> {}", 
                    account.getCode(), account.getBalance());
        }
    }
    
    public ExpressionList<Account> query() {
        return database.find(Account.class).where();
    }
}
