package com.econovafx.modules.billing.service;

import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.billing.repository.ThirdPartyRepository;
import com.econovafx.modules.core.config.UserContext;
import com.econovafx.modules.core.exception.EntityNotFoundException;
import com.econovafx.modules.core.exception.ValidationException;
import io.avaje.inject.Component;
import com.econovafx.modules.core.security.RequiresTenant;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Third Parties (Customers and Suppliers)
 */
@Component
@RequiresTenant
public class ThirdPartyService {
    
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyService.class);
    
    private final ThirdPartyRepository thirdPartyRepository;
    private final UserContext userContext;
    
    @Inject
    public ThirdPartyService(ThirdPartyRepository thirdPartyRepository, UserContext userContext) {
        this.thirdPartyRepository = thirdPartyRepository;
        this.userContext = userContext;
    }
    
    public Optional<ThirdParty> getThirdPartyById(Long id) {
        return thirdPartyRepository.findById(id);
    }
    
    public Optional<ThirdParty> getThirdPartyByIdentification(String identificationNumber) {
        return thirdPartyRepository.findByIdentificationNumber(identificationNumber);
    }
    
    public List<ThirdParty> getAllThirdParties() {
        return thirdPartyRepository.findAll();
    }
    
    public List<ThirdParty> getThirdPartiesByType(ThirdParty.ThirdPartyType type) {
        return thirdPartyRepository.findByType(type);
    }
    
    public List<ThirdParty> getCustomers() {
        return thirdPartyRepository.findCustomers();
    }
    
    public List<ThirdParty> getSuppliers() {
        return thirdPartyRepository.findSuppliers();
    }
    
    public List<ThirdParty> getActiveThirdParties() {
        return thirdPartyRepository.findActiveThirdParties();
    }
    
    public List<ThirdParty> searchThirdParties(String searchTerm) {
        return thirdPartyRepository.searchByName(searchTerm);
    }
    
    public List<ThirdParty> searchByName(String searchTerm) {
        return thirdPartyRepository.searchByName(searchTerm);
    }
    
    public List<ThirdParty> searchByIdentification(String searchTerm) {
        return thirdPartyRepository.searchByIdentification(searchTerm);
    }
    
    public ThirdParty createThirdParty(ThirdParty thirdParty) {
        validateThirdParty(thirdParty);
        
        if (thirdPartyRepository.existsByIdentificationNumber(thirdParty.getIdentificationNumber())) {
            throw new ValidationException(
                "identificationNumber",
                "Identification number already exists: " + thirdParty.getIdentificationNumber()
            );
        }
        
        // Set audit fields
        Long currentUserId = userContext.getCurrentUserId();
        if (currentUserId != null) {
            thirdParty.setCreatedBy(currentUserId);
            thirdParty.setUpdatedBy(currentUserId);
        }
        
        ThirdParty saved = thirdPartyRepository.save(thirdParty);
        if (saved != null) {
            logger.info("ThirdParty created: {} ({}) by user ID: {}", 
                saved.getName(), saved.getIdentificationNumber(), currentUserId);
        }
        return saved;
    }
    
    public ThirdParty updateThirdParty(ThirdParty thirdParty) {
        validateThirdParty(thirdParty);
        
        if (!thirdPartyRepository.existsById(thirdParty.getId())) {
            throw new EntityNotFoundException(ThirdParty.class, thirdParty.getId());
        }
        
        // Set audit field for update
        Long currentUserId = userContext.getCurrentUserId();
        if (currentUserId != null) {
            thirdParty.setUpdatedBy(currentUserId);
        }
        
        thirdPartyRepository.update(thirdParty);
        logger.info("ThirdParty updated: {} by user ID: {}", thirdParty.getName(), currentUserId);
        return thirdParty;
    }
    
    public void deleteThirdParty(Long id) {
        ThirdParty thirdParty = thirdPartyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ThirdParty.class, id));
        
        thirdPartyRepository.delete(thirdParty);
        logger.info("ThirdParty deleted: {}", thirdParty.getName());
    }
    
    public void deactivateThirdParty(Long id) {
        ThirdParty thirdParty = thirdPartyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ThirdParty.class, id));
        
        thirdParty.setIsActive(false);
        thirdPartyRepository.update(thirdParty);
        logger.info("ThirdParty deactivated: {}", thirdParty.getName());
    }
    
    public void activateThirdParty(Long id) {
        ThirdParty thirdParty = thirdPartyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ThirdParty.class, id));
        
        thirdParty.setIsActive(true);
        thirdPartyRepository.update(thirdParty);
        logger.info("ThirdParty activated: {}", thirdParty.getName());
    }
    
    private void validateThirdParty(ThirdParty thirdParty) {
        if (thirdParty.getName() == null || thirdParty.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Name is required");
        }
        if (thirdParty.getEmail() == null || thirdParty.getEmail().trim().isEmpty()) {
            throw new ValidationException("email", "Email is required");
        }
        if (thirdParty.getType() == null) {
            throw new ValidationException("type", "Type is required");
        }
    }
    
    public long getThirdPartiesCount() {
        return thirdPartyRepository.count();
    }
    
    public long getCustomersCount() {
        return thirdPartyRepository.countByType(ThirdParty.ThirdPartyType.CUSTOMER) + 
               thirdPartyRepository.countByType(ThirdParty.ThirdPartyType.BOTH);
    }
    
    public long getSuppliersCount() {
        return thirdPartyRepository.countByType(ThirdParty.ThirdPartyType.SUPPLIER) + 
               thirdPartyRepository.countByType(ThirdParty.ThirdPartyType.BOTH);
    }
    
    public void updateBalance(Long thirdPartyId, Double amount) {
        ThirdParty thirdParty = thirdPartyRepository.findById(thirdPartyId)
                .orElseThrow(() -> new IllegalArgumentException("ThirdParty not found with ID: " + thirdPartyId));
        
        Double newBalance = thirdParty.getCurrentBalance() + amount;
        thirdParty.setCurrentBalance(newBalance);
        thirdPartyRepository.update(thirdParty);
        logger.debug("ThirdParty balance updated: {} -> {}", thirdParty.getName(), newBalance);
    }
}
