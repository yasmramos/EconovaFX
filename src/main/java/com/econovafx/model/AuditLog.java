package com.econovafx.model;

import jakarta.persistence.*;

/**
 * Entity for audit logging of system operations
 * Tracks all critical operations for security and compliance
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "username"),
    @Index(name = "idx_audit_operation", columnList = "operation_type"),
    @Index(name = "idx_audit_entity", columnList = "entity_type"),
    @Index(name = "idx_audit_timestamp", columnList = "created_at")
})
public class AuditLog extends BaseEntity {

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(length = 2000)
    private String description;

    @Column(length = 500)
    private String ipAddress;

    @Column(length = 200)
    private String userAgent;

    @Column(name = "old_values", length = 2000)
    private String oldValues;

    @Column(name = "new_values", length = 2000)
    private String newValues;

    @Column(nullable = false)
    private Boolean success = true;

    @Column(length = 1000)
    private String errorMessage;

    /**
     * Types of operations that can be audited
     */
    public enum OperationType {
        CREATE,
        UPDATE,
        DELETE,
        VIEW,
        EXPORT,
        IMPORT,
        LOGIN,
        LOGOUT,
        PASSWORD_CHANGE,
        ROLE_CHANGE,
        APPROVE,
        REJECT,
        CLOSE_PERIOD,
        REOPEN_PERIOD,
        PUBLISH_TRANSACTION,
        UNPUBLISH_TRANSACTION,
        BACKUP,
        RESTORE,
        CONFIG_CHANGE,
        OTHER
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", operationType=" + operationType +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", success=" + success +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
