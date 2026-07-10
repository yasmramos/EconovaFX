package com.econovafx.modules.cash.repository;

import com.econovafx.modules.cash.model.CashBox;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository for Cash Box data access.
 */
public class CashBoxRepository {
    
    private final Map<Long, CashBox> database = new ConcurrentHashMap<>();
    private Long currentId = 1L;

    public synchronized CashBox save(CashBox cashBox) {
        if (cashBox.getId() == null) {
            cashBox.setId(currentId++);
        }
        cashBox.setUpdatedAt(java.time.Instant.now());
        database.put(cashBox.getId(), cashBox);
        return cashBox;
    }

    public Optional<CashBox> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    public List<CashBox> findAll() {
        return new ArrayList<>(database.values());
    }

    public List<CashBox> findOpenBoxes() {
        return database.values().stream()
                .filter(CashBox::getOpen)
                .collect(Collectors.toList());
    }

    public boolean deleteById(Long id) {
        return database.remove(id) != null;
    }

    public void updateBalance(Long id, BigDecimal newBalance) {
        findById(id).ifPresent(box -> {
            box.setBalance(newBalance);
            box.setUpdatedAt(java.time.Instant.now());
            save(box);
        });
    }
}
