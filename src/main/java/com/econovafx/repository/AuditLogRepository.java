package com.econovafx.repository;

import com.econovafx.model.AuditLog;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AuditLog entities
 */
@Component
public class AuditLogRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogRepository.class);
    
    private final Database database;
    
    @Inject
    public AuditLogRepository(Database database) {
        this.database = database;
    }
    
    public Optional<AuditLog> findById(Long id) {
        return Optional.ofNullable(database.find(AuditLog.class, id));
    }
    
    public List<AuditLog> findAll() {
        return database.find(AuditLog.class).orderBy().desc("createdAt").findList();
    }
    
    public List<AuditLog> findByUsername(String username) {
        return database.find(AuditLog.class)
                .where().eq("username", username)
                .orderBy().desc("createdAt")
                .findList();
    }
    
    public List<AuditLog> findByOperationType(AuditLog.OperationType operationType) {
        return database.find(AuditLog.class)
                .where().eq("operationType", operationType)
                .orderBy().desc("createdAt")
                .findList();
    }
    
    public List<AuditLog> findByEntityType(String entityType) {
        return database.find(AuditLog.class)
                .where().eq("entityType", entityType)
                .orderBy().desc("createdAt")
                .findList();
    }
    
    public List<AuditLog> findByEntityId(Long entityId) {
        return database.find(AuditLog.class)
                .where().eq("entityId", entityId)
                .orderBy().desc("createdAt")
                .findList();
    }
    
    public List<AuditLog> findByDateRange(Instant startDate, Instant endDate) {
        return database.find(AuditLog.class)
                .where()
                .ge("createdAt", startDate)
                .le("createdAt", endDate)
                .orderBy().desc("createdAt")
                .findList();
    }
    
    public List<AuditLog> findFailedOperations() {
        return database.find(AuditLog.class)
                .where().eq("success", false)
                .orderBy().desc("createdAt")
                .findList();
    }
    
    public AuditLog save(AuditLog auditLog) {
        database.save(auditLog);
        logger.debug("Audit log saved: {}", auditLog);
        return auditLog;
    }
    
    public long count() {
        return database.find(AuditLog.class).findCount();
    }
    
    public long countByUser(String username) {
        return database.find(AuditLog.class)
                .where().eq("username", username)
                .findCount();
    }
}
