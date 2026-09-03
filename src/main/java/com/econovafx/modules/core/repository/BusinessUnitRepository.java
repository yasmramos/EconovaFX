package com.econovafx.modules.core.repository;

import com.econovafx.modules.core.model.BusinessUnit;
import io.ebean.Database;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BusinessUnit entities.
 */
@Singleton
public class BusinessUnitRepository {

    private final Database database;

    @Inject
    public BusinessUnitRepository(Database database) {
        this.database = database;
    }

    /**
     * Find all active business units for a company.
     */
    public List<BusinessUnit> findByCompanyId(Long companyId) {
        return database.find(BusinessUnit.class)
                .where()
                .eq("company.id", companyId)
                .eq("status", "ACTIVE")
                .orderBy().asc("code")
                .findList();
    }

    /**
     * Find all business units for a company (active and inactive).
     */
    public List<BusinessUnit> findAllByCompanyId(Long companyId) {
        return database.find(BusinessUnit.class)
                .where()
                .eq("company.id", companyId)
                .orderBy().asc("code")
                .findList();
    }

    /**
     * Find a business unit by ID.
     */
    public Optional<BusinessUnit> findById(Long id) {
        BusinessUnit unit = database.find(BusinessUnit.class, id);
        return Optional.ofNullable(unit);
    }

    /**
     * Find a business unit by code and company.
     */
    public Optional<BusinessUnit> findByCodeAndCompany(String code, Long companyId) {
        BusinessUnit unit = database.find(BusinessUnit.class)
                .where()
                .eq("code", code)
                .eq("company.id", companyId)
                .findOne();
        return Optional.ofNullable(unit);
    }

    /**
     * Save a business unit.
     */
    public BusinessUnit save(BusinessUnit unit) {
        database.save(unit);
        return unit;
    }

    /**
     * Delete a business unit.
     */
    public void delete(Long id) {
        database.delete(BusinessUnit.class, id);
    }

    /**
     * Count business units for a company.
     */
    public int countByCompany(Long companyId) {
        return database.find(BusinessUnit.class)
                .where()
                .eq("company.id", companyId)
                .findCount();
    }
}
