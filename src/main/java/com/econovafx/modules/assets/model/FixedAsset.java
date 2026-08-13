package com.econovafx.modules.assets.model;

import com.econovafx.modules.core.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fixed_assets")
public class FixedAsset extends BaseEntity {

    public enum DepreciationMethod {
        STRAIGHT_LINE, DECLINING_BALANCE, UNITS_OF_PRODUCTION
    }

    @Column(nullable = false)
    private String assetCode;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DepreciationMethod depreciationMethod;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal acquisitionCost;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate acquisitionDate;

    @Column(nullable = false)
    private Integer usefulLifeMonths;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal accumulatedDepreciation;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal netBookValue;

    @Column(nullable = false)
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private com.econovafx.modules.accounting.model.Account assetAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depreciation_account_id")
    private com.econovafx.modules.accounting.model.Account depreciationAccount;

    // Getters y Setters
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public DepreciationMethod getDepreciationMethod() { return depreciationMethod; }
    public void setDepreciationMethod(DepreciationMethod depreciationMethod) { this.depreciationMethod = depreciationMethod; }
    
    public BigDecimal getAcquisitionCost() { return acquisitionCost; }
    public void setAcquisitionCost(BigDecimal acquisitionCost) { this.acquisitionCost = acquisitionCost; }
    
    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate = acquisitionDate; }
    
    public Integer getUsefulLifeMonths() { return usefulLifeMonths; }
    public void setUsefulLifeMonths(Integer usefulLifeMonths) { this.usefulLifeMonths = usefulLifeMonths; }
    
    public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }
    
    public BigDecimal getNetBookValue() { return netBookValue; }
    public void setNetBookValue(BigDecimal netBookValue) { this.netBookValue = netBookValue; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public com.econovafx.modules.accounting.model.Account getAssetAccount() { return assetAccount; }
    public void setAssetAccount(com.econovafx.modules.accounting.model.Account assetAccount) { this.assetAccount = assetAccount; }
    
    public com.econovafx.modules.accounting.model.Account getDepreciationAccount() { return depreciationAccount; }
    public void setDepreciationAccount(com.econovafx.modules.accounting.model.Account depreciationAccount) { this.depreciationAccount = depreciationAccount; }
}
