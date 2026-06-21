package com.econovafx.domain;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un almacén o depósito de inventario.
 * Permite manejar múltiples ubicaciones físicas.
 */
@Entity
@Table(name = "warehouses")
public class Warehouse extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 200)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ValuationMethod valuationMethod = ValuationMethod.FIFO;

    @Column(nullable = false)
    private boolean active = true;

    @WhenCreated
    @Column(name = "created_at", columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @WhenModified
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime updatedAt;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public ValuationMethod getValuationMethod() {
        return valuationMethod;
    }

    public void setValuationMethod(ValuationMethod valuationMethod) {
        this.valuationMethod = valuationMethod;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    @Override
    public String toString() {
        return "Warehouse{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", valuationMethod=" + valuationMethod +
                '}';
    }
}
