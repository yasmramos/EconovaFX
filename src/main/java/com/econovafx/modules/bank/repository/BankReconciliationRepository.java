package com.econovafx.repository;

import com.econovafx.model.BankReconciliation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository for Bank Reconciliation data access.
 */
public class BankReconciliationRepository {
    
    private final Map<Long, BankReconciliation> database = new ConcurrentHashMap<>();
    private Long currentId = 1L;

    public synchronized BankReconciliation save(BankReconciliation reconciliation) {
        if (reconciliation.getId() == null) {
            reconciliation.setId(currentId++);
        }
        database.put(reconciliation.getId(), reconciliation);
        return reconciliation;
    }

    public Optional<BankReconciliation> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    public List<BankReconciliation> findAll() {
        return new ArrayList<>(database.values());
    }

    public List<BankReconciliation> findByBankAccountId(Long bankAccountId) {
        return database.values().stream()
                .filter(r -> r.getBankAccountId().equals(bankAccountId))
                .collect(Collectors.toList());
    }

    public List<BankReconciliation> findByStatus(BankReconciliation.Status status) {
        return database.values().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }

    public boolean deleteById(Long id) {
        return database.remove(id) != null;
    }
}
