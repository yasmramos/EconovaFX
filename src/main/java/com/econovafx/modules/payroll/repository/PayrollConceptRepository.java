package com.econovafx.modules.payroll.repository;

import com.econovafx.modules.payroll.model.PayrollConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PayrollConcept entity.
 * Provides data access methods for payroll concept management.
 */
@Repository
public interface PayrollConceptRepository extends JpaRepository<PayrollConcept, Long> {

    /**
     * Find concept by concept code.
     */
    Optional<PayrollConcept> findByConceptCode(String conceptCode);

    /**
     * Find concepts by type (EARNING, DEDUCTION, etc.).
     */
    List<PayrollConcept> findByConceptType(PayrollConcept.ConceptType type);

    /**
     * Find active concepts.
     */
    List<PayrollConcept> findByActiveTrue();

    /**
     * Find taxable concepts.
     */
    @Query("SELECT pc FROM PayrollConcept pc WHERE pc.taxable = true AND pc.active = true")
    List<PayrollConcept> findTaxableConcepts();

    /**
     * Find social security concepts.
     */
    @Query("SELECT pc FROM PayrollConcept pc WHERE pc.socialSecurity = true AND pc.active = true")
    List<PayrollConcept> findSocialSecurityConcepts();

    /**
     * Check if concept code exists.
     */
    boolean existsByConceptCode(String conceptCode);

    /**
     * Find concepts ordered by priority.
     */
    @Query("SELECT pc FROM PayrollConcept pc WHERE pc.active = true ORDER BY pc.priorityOrder ASC")
    List<PayrollConcept> findAllActiveOrderedByPriority();
}
