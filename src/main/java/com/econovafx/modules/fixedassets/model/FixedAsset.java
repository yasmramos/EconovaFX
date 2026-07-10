package com.econovafx.modules.fixedassets.model;
import com.econovafx.modules.core.model.BaseEntity;

import com.econovafx.modules.billing.model.ThirdParty;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Activo Fijo individual.
 * Control detallado de bienes de la empresa según Resolución 340/2004.
 */
@Entity
@Table(name = "fixed_assets")
public class FixedAsset extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code; // Código interno del activo

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FixedAssetCategory category;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal acquisitionCost;

    @Column(nullable = false)
    private LocalDate acquisitionDate;

    @Column(name = "purchase_invoice_number", length = 50)
    private String purchaseInvoiceNumber;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private ThirdParty supplier;

    @Column(precision = 18, scale = 2)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal netBookValue;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetStatus status = AssetStatus.ACTIVE;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(name = "location", length = 150)
    private String location; // Ubicación física

    @Column(name = "responsible_user_id")
    private Long responsibleUserId; // Usuario responsable del activo

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public FixedAssetCategory getCategory() { return category; }
    public void setCategory(FixedAssetCategory category) { this.category = category; }

    public BigDecimal getAcquisitionCost() { return acquisitionCost; }
    public void setAcquisitionCost(BigDecimal acquisitionCost) { this.acquisitionCost = acquisitionCost; }

    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate = acquisitionDate; }

    public String getPurchaseInvoiceNumber() { return purchaseInvoiceNumber; }
    public void setPurchaseInvoiceNumber(String purchaseInvoiceNumber) { this.purchaseInvoiceNumber = purchaseInvoiceNumber; }

    public ThirdParty getSupplier() { return supplier; }
    public void setSupplier(ThirdParty supplier) { this.supplier = supplier; }

    public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }

    public BigDecimal getNetBookValue() { return netBookValue; }
    public void setNetBookValue(BigDecimal netBookValue) { this.netBookValue = netBookValue; }

    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getResponsibleUserId() { return responsibleUserId; }
    public void setResponsibleUserId(Long responsibleUserId) { this.responsibleUserId = responsibleUserId; }

    public enum AssetStatus {
        ACTIVE,        // En uso
        MAINTENANCE,   // En mantenimiento
        IDLE,          // Ocioso
        DISPOSED,      // Dado de baja
        SOLD           // Vendido
    }
}
