package com.econovafx.domain;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una categoría de inventario.
 * Permite organizar los productos en grupos jerárquicos.
 */
@Entity
@Table(name = "inventory_categories")
public class InventoryCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private InventoryCategory parent;

    @Column(nullable = false)
    private boolean active = true;

    @WhenCreated
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @WhenModified
    @Column(nullable = false)
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

    public InventoryCategory getParent() {
        return parent;
    }

    public void setParent(InventoryCategory parent) {
        this.parent = parent;
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

    /**
     * Obtiene la ruta completa de la categoría (padres -> actual).
     */
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    @Override
    public String toString() {
        return "InventoryCategory{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
