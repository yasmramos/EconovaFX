package com.econovafx.repository;

import com.econovafx.domain.ReportDefinition;
import com.econovafx.domain.ReportDefinition.ReportType;
import io.ebean.DB;
import io.ebean.Database;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Report Definitions
 */
@Singleton
public class ReportDefinitionRepository {

    private final Database database;

    public ReportDefinitionRepository() {
        this.database = DB.getDefault();
    }

    public Optional<ReportDefinition> findById(Long id) {
        return database.find(ReportDefinition.class).setId(id).findOneOrEmpty();
    }

    public Optional<ReportDefinition> findByCode(String code) {
        return database.find(ReportDefinition.class).where().eq("code", code).findOneOrEmpty();
    }

    public List<ReportDefinition> findAll() {
        return database.find(ReportDefinition.class).findList();
    }

    public List<ReportDefinition> findByType(ReportType type) {
        return database.find(ReportDefinition.class)
                .where().eq("reportType", type).findList();
    }

    public List<ReportDefinition> findPublic() {
        return database.find(ReportDefinition.class)
                .where().eq("isPublic", true).findList();
    }

    public ReportDefinition save(ReportDefinition definition) {
        database.save(definition);
        return definition;
    }

    public void update(ReportDefinition definition) {
        database.update(definition);
    }

    public void delete(ReportDefinition definition) {
        database.delete(definition);
    }

    public long count() {
        return database.find(ReportDefinition.class).findCount();
    }
}
