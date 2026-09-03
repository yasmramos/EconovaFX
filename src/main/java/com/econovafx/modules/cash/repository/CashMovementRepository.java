package com.econovafx.modules.cash.repository;

import com.econovafx.modules.cash.model.CashMovement;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Cash Movement data access using Ebean ORM.
 */
@Component
public class CashMovementRepository {
    
    private final Database database;

    @Inject
    public CashMovementRepository(Database database) {
        this.database = database;
    }

    public CashMovement save(CashMovement movement) {
        database.save(movement);
        return movement;
    }

    public Optional<CashMovement> findById(Long id) {
        return Optional.ofNullable(database.find(CashMovement.class, id));
    }

    public List<CashMovement> findAll() {
        return database.find(CashMovement.class).findList();
    }

    public List<CashMovement> findByAccountId(Long accountId) {
        return database.find(CashMovement.class)
                .where().or().eq("sourceAccountId", accountId).eq("destinationAccountId", accountId).endOr()
                .findList();
    }

    public List<CashMovement> findByStatus(CashMovement.Status status) {
        return database.find(CashMovement.class)
                .where().eq("status", status)
                .findList();
    }

    public List<CashMovement> findPendingMovements() {
        return findByStatus(CashMovement.Status.PENDING);
    }

    public List<CashMovement> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return database.find(CashMovement.class)
                .where().ge("date", startDate).le("date", endDate)
                .findList();
    }

    public boolean deleteById(Long id) {
        int rowsDeleted = database.delete(CashMovement.class, id);
        return rowsDeleted > 0;
    }
}
