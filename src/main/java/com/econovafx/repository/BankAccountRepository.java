package com.econovafx.repository;

import com.econovafx.model.BankAccount;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository for Bank Account data access.
 * In-memory implementation for desktop application.
 */
public class BankAccountRepository {
    
    private final Map<Long, BankAccount> database = new ConcurrentHashMap<>();
    private Long currentId = 1L;

    public synchronized BankAccount save(BankAccount account) {
        if (account.getId() == null) {
            account.setId(currentId++);
        }
        account.setUpdatedAt(java.time.LocalDateTime.now());
        database.put(account.getId(), account);
        return account;
    }

    public Optional<BankAccount> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    public List<BankAccount> findAll() {
        return new ArrayList<>(database.values());
    }

    public List<BankAccount> findActiveAccounts() {
        return database.values().stream()
                .filter(BankAccount::getActive)
                .collect(Collectors.toList());
    }

    public Optional<BankAccount> findByAccountNumber(String accountNumber) {
        return database.values().stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst();
    }

    public boolean deleteById(Long id) {
        return database.remove(id) != null;
    }

    public void updateBalance(Long id, BigDecimal newBalance) {
        findById(id).ifPresent(account -> {
            account.setBalance(newBalance);
            account.setUpdatedAt(java.time.LocalDateTime.now());
            save(account);
        });
    }
}
