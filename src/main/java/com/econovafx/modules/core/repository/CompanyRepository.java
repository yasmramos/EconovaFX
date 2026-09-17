package com.econovafx.modules.core.repository;

import com.econovafx.modules.core.model.Company;
import io.avaje.inject.Component;
import io.ebean.Database;
import io.ebean.annotation.Transactional;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * Repository for company (tenant) management in the multi-company system.
 */
@Component
public class CompanyRepository {

    private final Database database;

    @Inject
    public CompanyRepository(Database database) {
        this.database = database;
    }

    /**
     * Get all active companies.
     */
    public List<Company> findAllActive() {
        return database.find(Company.class)
                .where().eq("status", "ACTIVE")
                .orderBy().asc("name")
                .findList();
    }

    /**
     * Get all companies (including inactive).
     */
    public List<Company> findAll() {
        return database.find(Company.class)
                .orderBy().asc("name")
                .findList();
    }

    /**
     * Find a company by its ID.
     */
    public Optional<Company> findById(Long id) {
        return Optional.ofNullable(database.find(Company.class, id));
    }

    /**
     * Find a company by its code.
     */
    public Optional<Company> findByCode(String code) {
        return database.find(Company.class)
                .where().eq("code", code)
                .findOneOrEmpty();
    }

    /**
     * Find a company by its NIF (tax ID).
     */
    public Optional<Company> findByNif(String nif) {
        return database.find(Company.class)
                .where().eq("nif", nif)
                .findOneOrEmpty();
    }

    /**
     * Save a new company or update an existing one.
     */
    @Transactional
    public Company save(Company company) {
        if (company.getId() == null) {
            database.save(company);
        } else {
            database.update(company);
        }
        return company;
    }

    /**
     * Delete a company by its ID.
     */
    @Transactional
    public void deleteById(Long id) {
        database.delete(Company.class, id);
    }

    /**
     * Update the status of a company.
     */
    @Transactional
    public void updateStatus(Long companyId, String status) {
        Company company = database.find(Company.class, companyId);
        if (company != null) {
            company.setStatus(status);
            database.update(company);
        }
    }
}
