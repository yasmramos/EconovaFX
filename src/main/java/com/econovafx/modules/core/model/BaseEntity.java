package com.econovafx.modules.core.model;

import io.ebean.annotation.TenantId;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Base entity class with common fields including native Ebean multi-tenant support.
 * Uses @TenantId for native multi-tenancy support in Ebean 17+.
 * Includes audit fields for tracking user actions.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    /**
     * Tenant ID for data isolation using native Ebean support.
     * All transactional entities must have this field.
     * Ebean automatically filters by this value in all queries.
     */
    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, columnDefinition = "BIGINT")
    private Long tenantId;

    @Version
    protected Long version;

    @WhenCreated
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @WhenModified
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * User ID who created this record (for audit purposes).
     * Should be set automatically by the application before persisting.
     */
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    /**
     * User ID who last modified this record (for audit purposes).
     * Should be set automatically by the application before updating.
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}
