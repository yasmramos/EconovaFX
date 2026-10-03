package com.econovafx.modules.core.service;

import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.repository.AuditLogRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for audit logging and security tracking
 * Provides comprehensive audit trail for compliance with Resolution 340/2004
 */
@Component
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    private final AuditLogRepository auditLogRepository;
    
    @Inject
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    /**
     * Log a successful operation
     */
    public void logSuccess(String username, AuditLog.OperationType operationType, 
                          String entityType, Long entityId, String description) {
        logOperation(username, operationType, entityType, entityId, description, true, null);
    }
    
    /**
     * Log a failed operation
     */
    public void logFailure(String username, AuditLog.OperationType operationType,
                          String entityType, Long entityId, String description, String errorMessage) {
        logOperation(username, operationType, entityType, entityId, description, false, errorMessage);
    }
    
    /**
     * Log an operation with old and new values for tracking changes
     */
    public void logWithValues(String username, AuditLog.OperationType operationType,
                             String entityType, Long entityId, String description,
                             String oldValues, String newValues) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setOperationType(operationType);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setSuccess(true);
        
        auditLogRepository.save(auditLog);
        logger.info("Audit: {} - {} on {} ({}) by {}", 
                   operationType, description, entityType, entityId, username);
    }
    
    private void logOperation(String username, AuditLog.OperationType operationType,
                             String entityType, Long entityId, String description,
                             Boolean success, String errorMessage) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setOperationType(operationType);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        auditLog.setSuccess(success);
        auditLog.setErrorMessage(errorMessage);
        
        auditLogRepository.save(auditLog);
        
        if (success) {
            logger.info("Audit: {} - {} on {} ({}) by {}", 
                       operationType, description, entityType, entityId, username);
        } else {
            logger.warn("Audit FAILED: {} - {} on {} ({}) by {} - Error: {}", 
                       operationType, description, entityType, entityId, username, errorMessage);
        }
    }
    
    /**
     * Get all audit logs
     */
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
    
    /**
     * Get audit logs by username
     */
    public List<AuditLog> getAuditLogsByUser(String username) {
        return auditLogRepository.findByUsername(username);
    }
    
    /**
     * Get audit logs by operation type
     */
    public List<AuditLog> getAuditLogsByOperationType(AuditLog.OperationType operationType) {
        return auditLogRepository.findByOperationType(operationType);
    }
    
    /**
     * Get audit logs by entity type (e.g., "Transaction", "User")
     */
    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }
    
    /**
     * Get audit logs for a specific entity
     */
    public List<AuditLog> getAuditLogsByEntityId(Long entityId) {
        return auditLogRepository.findByEntityId(entityId);
    }
    
    /**
     * Get audit logs within a date range
     */
    public List<AuditLog> getAuditLogsByDateRange(Instant startDate, Instant endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Get all failed operations
     */
    public List<AuditLog> getFailedOperations() {
        return auditLogRepository.findFailedOperations();
    }
    
    /**
     * Get audit log by ID
     */
    public Optional<AuditLog> getAuditLogById(Long id) {
        return auditLogRepository.findById(id);
    }
    
    /**
     * Get total count of audit logs
     */
    public long getAuditLogCount() {
        return auditLogRepository.count();
    }
    
    /**
     * Get count of operations by user
     */
    public long getOperationCountByUser(String username) {
        return auditLogRepository.countByUser(username);
    }
    
    /**
     * Export audit logs to CSV format for reporting
     */
    public String exportToCSV(List<AuditLog> logs) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Username,Operation Type,Entity Type,Entity ID,Description,Success,Created At\n");
        
        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",");
            csv.append(escapeCsv(log.getUsername())).append(",");
            csv.append(log.getOperationType()).append(",");
            csv.append(escapeCsv(log.getEntityType())).append(",");
            csv.append(log.getEntityId()).append(",");
            csv.append(escapeCsv(log.getDescription())).append(",");
            csv.append(log.getSuccess()).append(",");
            csv.append(log.getCreatedAt()).append("\n");
        }
        
        return csv.toString();
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma or quote
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
