package com.econovafx.repository;

import com.econovafx.domain.FinancialStatementRow;
import io.ebean.Database;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Financial Statement Rows
 */
@Singleton
public class FinancialStatementRowRepository {

    private final Database database;

    @Inject
    public FinancialStatementRowRepository(Database database) {
        this.database = database;
    }

    public Optional<FinancialStatementRow> findById(Long id) {
        return database.find(FinancialStatementRow.class).setId(id).findOneOrEmpty();
    }

    public List<FinancialStatementRow> findByModelId(Long modelId) {
        return database.find(FinancialStatementRow.class)
                .where().eq("model.id", modelId)
                .orderBy().asc("rowNumber")
                .findList();
    }

    public List<FinancialStatementRow> findAll() {
        return database.find(FinancialStatementRow.class).findList();
    }

    public FinancialStatementRow save(FinancialStatementRow row) {
        database.save(row);
        return row;
    }

    public void update(FinancialStatementRow row) {
        database.update(row);
    }

    public void delete(FinancialStatementRow row) {
        database.delete(row);
    }

    public void deleteByModelId(Long modelId) {
        database.find(FinancialStatementRow.class)
                .where().eq("model.id", modelId)
                .delete();
    }

    public long count() {
        return database.find(FinancialStatementRow.class).findCount();
    }

    public long countByModelId(Long modelId) {
        return database.find(FinancialStatementRow.class)
                .where().eq("model.id", modelId)
                .findCount();
    }
}
