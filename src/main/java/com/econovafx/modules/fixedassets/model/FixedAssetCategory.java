package com.econovafx.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Categoría de Activos Fijos.
 * Requerido para el control y depreciación según Resolución 340/2004.
 */
@Entity
@Table(name = "fixed_asset_categories")
public class FixedAssetCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String description;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal depreciationRate; // Porcentaje anual de depreciación

    @Column(nullable = false)
    private Integer usefulLifeYears; // Vida útil en años

    @Column(name = "accumulated_depreciation_account", length = 20)
    private String accumulatedDepreciationAccount; // Cuenta contable de depreciación acumulada

    @Column(name = "depreciation_expense_account", length = 20)
    private String depreciationExpenseAccount; // Cuenta contable de gasto por depreciación

    @Column(name = "asset_account", length = 20)
    private String assetAccount; // Cuenta contable del activo

    @Column(nullable = false)
    private boolean active = true;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDepreciationRate() { return depreciationRate; }
    public void setDepreciationRate(BigDecimal depreciationRate) { this.depreciationRate = depreciationRate; }

    public Integer getUsefulLifeYears() { return usefulLifeYears; }
    public void setUsefulLifeYears(Integer usefulLifeYears) { this.usefulLifeYears = usefulLifeYears; }

    public String getAccumulatedDepreciationAccount() { return accumulatedDepreciationAccount; }
    public void setAccumulatedDepreciationAccount(String accumulatedDepreciationAccount) { this.accumulatedDepreciationAccount = accumulatedDepreciationAccount; }

    public String getDepreciationExpenseAccount() { return depreciationExpenseAccount; }
    public void setDepreciationExpenseAccount(String depreciationExpenseAccount) { this.depreciationExpenseAccount = depreciationExpenseAccount; }

    public String getAssetAccount() { return assetAccount; }
    public void setAssetAccount(String assetAccount) { this.assetAccount = assetAccount; }

    // Helper methods for DepreciationService compatibility
    public String getDepreciationExpenseAccountCode() { return depreciationExpenseAccount; }
    public String getAccumulatedDepreciationAccountCode() { return accumulatedDepreciationAccount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
