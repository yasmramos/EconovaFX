package com.econovafx.modules.core.repository;

import com.econovafx.modules.core.model.Company;
import io.ebean.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de empresas (tenants) en el sistema multi-empresa.
 */
@Singleton
public class CompanyRepository {

    private io.ebean.Database database;

    // Constructor para testing - no inicializa Ebean
    protected CompanyRepository(boolean initialize) {
        this.database = null;
    }

    public CompanyRepository(io.ebean.Database database) {
        this.database = database != null ? database : io.ebean.DB.getDefault();
    }

    @Inject
    public CompanyRepository() {
        // Usamos la base de datos maestra (default) para gestionar las empresas
        this.database = io.ebean.DB.getDefault();
    }

    /**
     * Obtiene todas las empresas activas.
     */
    public List<Company> findAllActive() {
        return database.find(Company.class)
                .where().eq("status", "ACTIVE")
                .orderBy().asc("name")
                .findList();
    }

    /**
     * Obtiene todas las empresas (incluyendo inactivas).
     */
    public List<Company> findAll() {
        return database.find(Company.class)
                .orderBy().asc("name")
                .findList();
    }

    /**
     * Busca una empresa por su ID.
     */
    public Optional<Company> findById(Long id) {
        return Optional.ofNullable(database.find(Company.class, id));
    }

    /**
     * Busca una empresa por su código.
     */
    public Optional<Company> findByCode(String code) {
        return database.find(Company.class)
                .where().eq("code", code)
                .findOneOrEmpty();
    }

    /**
     * Busca una empresa por su NIF.
     */
    public Optional<Company> findByNif(String nif) {
        return database.find(Company.class)
                .where().eq("nif", nif)
                .findOneOrEmpty();
    }

    /**
     * Guarda una nueva empresa o actualiza una existente.
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
     * Elimina una empresa por su ID.
     */
    @Transactional
    public void deleteById(Long id) {
        database.delete(Company.class, id);
    }

    /**
     * Cambia el estado de una empresa.
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
