package com.econovafx.repository;

import com.econovafx.domain.FinancialStatementModel;
import com.econovafx.domain.FinancialStatementModel.ModelType;
import io.ebean.DB;
import io.ebean.Database;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Financial Statement Models
 */
@Singleton
public class FinancialStatementModelRepository {

    private final Database database;

    public FinancialStatementModelRepository() {
        this.database = DB.getDefault();
    }

    public Optional<FinancialStatementModel> findById(Long id) {
        return database.find(FinancialStatementModel.class).setId(id).findOneOrEmpty();
    }

    public Optional<FinancialStatementModel> findByCode(String code) {
        return database.find(FinancialStatementModel.class).where().eq("code", code).findOneOrEmpty();
    }

    public List<FinancialStatementModel> findAll() {
        return database.find(FinancialStatementModel.class).findList();
    }

    public List<FinancialStatementModel> findByType(ModelType type) {
        return database.find(FinancialStatementModel.class)
                .where().eq("modelType", type).findList();
    }

    public List<FinancialStatementModel> findActive() {
        return database.find(FinancialStatementModel.class)
                .where().eq("isActive", true).findList();
    }

    public FinancialStatementModel save(FinancialStatementModel model) {
        database.save(model);
        return model;
    }

    public void update(FinancialStatementModel model) {
        database.update(model);
    }

    public void delete(FinancialStatementModel model) {
        database.delete(model);
    }

    public long count() {
        return database.find(FinancialStatementModel.class).findCount();
    }
}
