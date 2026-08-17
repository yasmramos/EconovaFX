package com.econovafx.modules.payroll.repository;

import com.econovafx.modules.payroll.model.PayrollConcept;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for PayrollConcept entity.
 * Provides data access methods for payroll concept management.
 */
@Component
public class PayrollConceptRepository {

    private static final Logger logger = LoggerFactory.getLogger(PayrollConceptRepository.class);
    
    private final Database database;
    
    @Inject
    public PayrollConceptRepository(Database database) {
        this.database = database;
    }

    /**
     * Find all concepts.
     */
    public List<PayrollConcept> findAll() {
        return database.find(PayrollConcept.class).orderBy().asc("conceptCode").findList();
    }

    /**
     * Find concept by ID.
     */
    public Optional<PayrollConcept> findById(Long id) {
        return Optional.ofNullable(database.find(PayrollConcept.class, id));
    }

    /**
     * Save a concept.
     */
    public PayrollConcept save(PayrollConcept concept) {
        database.save(concept);
        logger.debug("PayrollConcept saved: {}", concept.getConceptCode());
        return concept;
    }

    /**
     * Delete a concept.
     */
    public void delete(PayrollConcept concept) {
        database.delete(concept);
        logger.debug("PayrollConcept deleted: {}", concept.getConceptCode());
    }

    /**
     * Find concepts by type.
     */
    public List<PayrollConcept> findByType(PayrollConcept.ConceptType type) {
        return database.find(PayrollConcept.class)
                .where().eq("conceptType", type)
                .orderBy().asc("conceptCode").findList();
    }

    /**
     * Find concepts by type and active status.
     */
    public List<PayrollConcept> findByTypeAndActive(PayrollConcept.ConceptType type, boolean active) {
        return database.find(PayrollConcept.class)
                .where().eq("conceptType", type).eq("active", active)
                .orderBy().asc("conceptCode").findList();
    }

    /**
     * Find active concepts.
     */
    public List<PayrollConcept> findByActiveTrue() {
        return database.find(PayrollConcept.class)
                .where().eq("active", true)
                .orderBy().asc("conceptCode").findList();
    }

    /**
     * Check if concept code exists.
     */
    public boolean existsByCode(String code) {
        return database.find(PayrollConcept.class)
                .where().eq("conceptCode", code).exists();
    }
}
