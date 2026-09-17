package com.econovafx.modules.core.service;

import com.econovafx.modules.core.model.BusinessUnit;
import com.econovafx.modules.core.repository.BusinessUnitRepository;
import io.ebean.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Business Units within companies.
 */
@Singleton
public class BusinessUnitService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessUnitService.class);

    @Inject
    public BusinessUnitRepository businessUnitRepository;

    /**
     * Get all active business units for a company.
     */
    public List<BusinessUnit> findByCompanyId(Long companyId) {
        return businessUnitRepository.findByCompanyId(companyId);
    }

    /**
     * Get all business units for a company (active and inactive).
     */
    public List<BusinessUnit> findAllByCompanyId(Long companyId) {
        return businessUnitRepository.findAllByCompanyId(companyId);
    }

    /**
     * Find a business unit by ID.
     */
    public Optional<BusinessUnit> findById(Long id) {
        return businessUnitRepository.findById(id);
    }

    /**
     * Save a business unit.
     */
    @Transactional
    public BusinessUnit save(BusinessUnit unit) {
        BusinessUnit saved = businessUnitRepository.save(unit);
        logger.info("Business Unit saved: {} ({})", saved.getName(), saved.getCode());
        return saved;
    }

    /**
     * Delete a business unit.
     */
    @Transactional
    public void delete(Long id) {
        businessUnitRepository.delete(id);
        logger.info("Business Unit deleted: ID {}", id);
    }

    /**
     * Check if a company has any business units.
     */
    public boolean hasUnits(Long companyId) {
        return businessUnitRepository.countByCompany(companyId) > 0;
    }

    /**
     * Get the count of business units for a company.
     */
    public int countByCompany(Long companyId) {
        return businessUnitRepository.countByCompany(companyId);
    }
}
