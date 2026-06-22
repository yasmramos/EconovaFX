package com.econovafx.service;

import com.econovafx.model.Account;
import com.econovafx.model.AccountType;
import com.econovafx.repository.AccountRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing accounts
 */
@Component
public class AccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    
    private final AccountRepository accountRepository;
    
    @Inject
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    
    public Optional<Account> getAccountByCode(String code) {
        return accountRepository.findByCode(code);
    }
    
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    public List<Account> getAccountsByType(AccountType type) {
        return accountRepository.findByType(type);
    }
    
    public List<Account> getRootAccounts() {
        return accountRepository.findRootAccounts();
    }
    
    public List<Account> getChildAccounts(Long parentAccountId) {
        return accountRepository.findByParentAccount(parentAccountId);
    }
    
    public List<Account> searchAccounts(String searchTerm) {
        return accountRepository.searchByName(searchTerm);
    }
    
    public Account createAccount(Account account) {
        validateAccount(account);
        
        if (accountRepository.existsByCode(account.getCode())) {
            throw new IllegalArgumentException("Account code already exists: " + account.getCode());
        }
        
        Account saved = accountRepository.save(account);
        logger.info("Account created: {}", saved.getCode());
        return saved;
    }
    
    public Account updateAccount(Account account) {
        validateAccount(account);
        
        if (!accountRepository.existsById(account.getId())) {
            throw new IllegalArgumentException("Account not found with ID: " + account.getId());
        }
        
        accountRepository.update(account);
        logger.info("Account updated: {}", account.getCode());
        return account;
    }
    
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + id));
        
        if (!account.getChildAccounts().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete account with child accounts");
        }
        
        accountRepository.delete(account);
        logger.info("Account deleted: {}", account.getCode());
    }
    
    private void validateAccount(Account account) {
        if (account.getCode() == null || account.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Account code is required");
        }
        if (account.getName() == null || account.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Account name is required");
        }
        if (account.getType() == null) {
            throw new IllegalArgumentException("Account type is required");
        }
    }
    
    public long getAccountsCount() {
        return accountRepository.count();
    }
    
    public long getAccountsCountByType(AccountType type) {
        return accountRepository.countByType(type);
    }
}
