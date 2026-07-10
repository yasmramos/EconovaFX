package com.econovafx.modules.billing.repository;

import com.econovafx.modules.billing.model.ThirdParty;
import io.avaje.inject.Component;
import io.ebean.Database;
import io.ebean.ExpressionList;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ThirdParty entities (Customers and Suppliers)
 */
@Component
public class ThirdPartyRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyRepository.class);
    
    private final Database database;
    
    @Inject
    public ThirdPartyRepository(Database database) {
        this.database = database;
    }
    
    public Optional<ThirdParty> findById(Long id) {
        return Optional.ofNullable(database.find(ThirdParty.class, id));
    }
    
    public Optional<ThirdParty> findByIdentificationNumber(String identificationNumber) {
        return Optional.ofNullable(database.find(ThirdParty.class)
                .where().eq("identificationNumber", identificationNumber).findOne());
    }
    
    public List<ThirdParty> findAll() {
        return database.find(ThirdParty.class)
                .orderBy().asc("name").findList();
    }
    
    public List<ThirdParty> findByType(ThirdParty.ThirdPartyType type) {
        return database.find(ThirdParty.class)
                .where().eq("type", type).orderBy().asc("name").findList();
    }
    
    public List<ThirdParty> findCustomers() {
        return database.find(ThirdParty.class)
                .where().or().eq("type", ThirdParty.ThirdPartyType.CUSTOMER).eq("type", ThirdParty.ThirdPartyType.BOTH).endOr()
                .orderBy().asc("name").findList();
    }
    
    public List<ThirdParty> findSuppliers() {
        return database.find(ThirdParty.class)
                .where().or().eq("type", ThirdParty.ThirdPartyType.SUPPLIER).eq("type", ThirdParty.ThirdPartyType.BOTH).endOr()
                .orderBy().asc("name").findList();
    }
    
    public List<ThirdParty> searchByName(String searchTerm) {
        return database.find(ThirdParty.class)
                .where().ilike("name", "%" + searchTerm + "%")
                .orderBy().asc("name").findList();
    }
    
    public List<ThirdParty> searchByIdentification(String searchTerm) {
        return database.find(ThirdParty.class)
                .where().ilike("identificationNumber", "%" + searchTerm + "%")
                .orderBy().asc("name").findList();
    }
    
    public ThirdParty save(ThirdParty thirdParty) {
        database.save(thirdParty);
        logger.debug("ThirdParty saved: {} ({})", thirdParty.getName(), thirdParty.getIdentificationNumber());
        return thirdParty;
    }
    
    public void update(ThirdParty thirdParty) {
        database.update(thirdParty);
        logger.debug("ThirdParty updated: {}", thirdParty.getName());
    }
    
    public void delete(ThirdParty thirdParty) {
        database.delete(thirdParty);
        logger.debug("ThirdParty deleted: {}", thirdParty.getName());
    }
    
    public void deleteById(Long id) {
        database.delete(ThirdParty.class, id);
        logger.debug("ThirdParty deleted by ID: {}", id);
    }
    
    public boolean existsByIdentificationNumber(String identificationNumber) {
        return database.find(ThirdParty.class)
                .where().eq("identificationNumber", identificationNumber).exists();
    }
    
    public boolean existsById(Long id) {
        return database.find(ThirdParty.class, id) != null;
    }
    
    public long count() {
        return database.find(ThirdParty.class).findCount();
    }
    
    public long countByType(ThirdParty.ThirdPartyType type) {
        return database.find(ThirdParty.class)
                .where().eq("type", type).findCount();
    }
    
    public List<ThirdParty> findActiveThirdParties() {
        return database.find(ThirdParty.class)
                .where().eq("isActive", true)
                .orderBy().asc("name").findList();
    }
    
    public List<ThirdParty> findByCity(String city) {
        return database.find(ThirdParty.class)
                .where().eq("city", city)
                .orderBy().asc("name").findList();
    }
    
    public ExpressionList<ThirdParty> query() {
        return database.find(ThirdParty.class).where();
    }
}
