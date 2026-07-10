package com.econovafx.modules.bank.service;

import com.econovafx.modules.core.security.RequiresTenant;
import io.avaje.inject.Component;

import com.econovafx.modules.bank.model.BankAccount;
import com.econovafx.modules.bank.repository.BankAccountRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Bank Accounts.
 */
@Component
@RequiresTenant
public class BankAccountService {
    
    private final BankAccountRepository repository = new BankAccountRepository();

    public BankAccount createAccount(BankAccount account) {
        if (account.getCode() == null || account.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Account code is required");
        }
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required");
        }
        if (account.getAccountingAccount() == null || account.getAccountingAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("Accounting account is required per Resolution 340/2004");
        }
        return repository.save(account);
    }

    public Optional<BankAccount> getAccount(Long id) {
        return repository.findById(id);
    }

    public List<BankAccount> getAllAccounts() {
        return repository.findAll();
    }

    public List<BankAccount> getActiveAccounts() {
        return repository.findActiveAccounts();
    }

    public BankAccount updateAccount(BankAccount account) {
        if (account.getId() == null) {
            throw new IllegalArgumentException("Account ID is required for update");
        }
        return repository.save(account);
    }

    public boolean deleteAccount(Long id) {
        return repository.deleteById(id);
    }

    public void updateBalance(Long id, java.math.BigDecimal newBalance) {
        repository.updateBalance(id, newBalance);
    }
}
