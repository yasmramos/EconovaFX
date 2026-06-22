package com.econovafx.model;

import io.ebean.annotation.TenantId;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Base entity class with common fields including native Ebean multi-tenant support.
 * Uses @TenantId for native multi-tenancy support in Ebean 17+.
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
    @Column(name = "version")
    protected Long version;

    @WhenCreated
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @WhenModified
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
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
}
