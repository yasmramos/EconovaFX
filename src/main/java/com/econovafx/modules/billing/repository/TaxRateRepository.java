package com.econovafx.modules.billing.repository;

import com.econovafx.modules.billing.model.TaxRate;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TaxRate entities.
 * Maneja operaciones de acceso a datos para tasas impositivas.
 */
@Component
public class TaxRateRepository {

    private static final Logger logger = LoggerFactory.getLogger(TaxRateRepository.class);

    private final Database database;

    @Inject
    public TaxRateRepository(Database database) {
        this.database = database;
    }

    public Optional<TaxRate> findById(Long id) {
        return Optional.ofNullable(database.find(TaxRate.class, id));
    }

    public Optional<TaxRate> findByCode(String code) {
        return Optional.ofNullable(database.find(TaxRate.class)
                .where().eq("code", code).findOne());
    }

    public List<TaxRate> findAll() {
        return database.find(TaxRate.class)
                .orderBy().asc("code").findList();
    }

    public List<TaxRate> findActiveRates() {
        return database.find(TaxRate.class)
                .where().eq("active", true)
                .orderBy().asc("code").findList();
    }

    public TaxRate save(TaxRate taxRate) {
        database.save(taxRate);
        logger.debug("TaxRate saved: {} - {}", taxRate.getCode(), taxRate.getName());
        return taxRate;
    }

    public void update(TaxRate taxRate) {
        database.update(taxRate);
        logger.debug("TaxRate updated: {}", taxRate.getId());
    }

    public void delete(TaxRate taxRate) {
        database.delete(taxRate);
        logger.debug("TaxRate deleted: {}", taxRate.getCode());
    }

    public void deleteById(Long id) {
        database.delete(TaxRate.class, id);
        logger.debug("TaxRate deleted by ID: {}", id);
    }

    public boolean existsByCode(String code) {
        return database.find(TaxRate.class)
                .where().eq("code", code).exists();
    }

    public boolean existsByCodeExclude(String code, Long excludeId) {
        var query = database.find(TaxRate.class)
                .where().eq("code", code);
        if (excludeId != null) {
            query.ne("id", excludeId);
        }
        return query.exists();
    }

    public boolean existsById(Long id) {
        return database.find(TaxRate.class, id) != null;
    }

    public long count() {
        return database.find(TaxRate.class).findCount();
    }

    public long countActive() {
        return database.find(TaxRate.class)
                .where().eq("active", true).findCount();
    }
}
