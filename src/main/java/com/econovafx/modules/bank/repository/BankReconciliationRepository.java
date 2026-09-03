package com.econovafx.modules.bank.repository;

import com.econovafx.modules.bank.model.BankReconciliation;
import com.econovafx.modules.core.config.DatabaseConfig;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Bank Reconciliation data access using Ebean ORM.
 */
@Singleton
public class BankReconciliationRepository {
    
    private final Database database;

    /**
     * Constructor with Database injection for Avaje Inject.
     */
    @Inject
    public BankReconciliationRepository(Database database) {
        this.database = database;
    }

    /**
     * Default constructor that obtains Database from DatabaseConfig.
     * Used when repository is instantiated manually without dependency injection.
     */
    public BankReconciliationRepository() {
        this(DatabaseConfig.getServer());
    }

    public BankReconciliation save(BankReconciliation reconciliation) {
        database.save(reconciliation);
        return reconciliation;
    }

    public Optional<BankReconciliation> findById(Long id) {
        return database.find(BankReconciliation.class).setId(id).findOneOrEmpty();
    }

    public List<BankReconciliation> findAll() {
        return database.find(BankReconciliation.class).findList();
    }

    public List<BankReconciliation> findByBankAccountId(Long bankAccountId) {
        return database.find(BankReconciliation.class)
                .where().eq("bankAccountId", bankAccountId)
                .orderBy().desc("statementDate")
                .findList();
    }

    public List<BankReconciliation> findByStatus(BankReconciliation.Status status) {
        return database.find(BankReconciliation.class)
                .where().eq("status", status)
                .findList();
    }

    public boolean deleteById(Long id) {
        int rowsDeleted = database.delete(BankReconciliation.class, id);
        return rowsDeleted > 0;
    }
}
