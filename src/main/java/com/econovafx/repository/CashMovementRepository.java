package com.econovafx.repository;

import com.econovafx.model.CashMovement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository for Cash Movement data access.
 */
public class CashMovementRepository {
    
    private final Map<Long, CashMovement> database = new ConcurrentHashMap<>();
    private Long currentId = 1L;

    public synchronized CashMovement save(CashMovement movement) {
        if (movement.getId() == null) {
            movement.setId(currentId++);
        }
        movement.setUpdatedAt(java.time.LocalDateTime.now());
        database.put(movement.getId(), movement);
        return movement;
    }

    public Optional<CashMovement> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    public List<CashMovement> findAll() {
        return new ArrayList<>(database.values());
    }

    public List<CashMovement> findByAccountId(Long accountId) {
        return database.values().stream()
                .filter(m -> accountId.equals(m.getSourceAccountId()) || accountId.equals(m.getDestinationAccountId()))
                .collect(Collectors.toList());
    }

    public List<CashMovement> findByStatus(CashMovement.Status status) {
        return database.values().stream()
                .filter(m -> m.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<CashMovement> findPendingMovements() {
        return findByStatus(CashMovement.Status.PENDING);
    }

    public boolean deleteById(Long id) {
        return database.remove(id) != null;
    }
}
