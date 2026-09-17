package com.econovafx.modules.inventory.model;

import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.core.model.BaseEntity;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un producto o artículo del inventario.
 * Cumple con los requisitos de control de inventarios según Resolución 340/2004.
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItem extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private InventoryCategory category;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal minimumStock = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal maximumStock = BigDecimal.ZERO;

    @Column(nullable = false)
    private String unitOfMeasure = "UNIDAD";

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 50)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private ThirdParty supplier;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    // ==================== CAMPOS PARA CUMPLIMIENTO RESOLUCIÓN 340/2004 CUBA ====================

    /**
     * Indica si el producto está en proceso de carga inicial de inventarios.
     * Durante la carga inicial, no se permiten movimientos regulares hasta que se complete el cuadre.
     */
    @Column(nullable = false)
    private boolean initialLoadInProgress = false;

    /**
     * Cantidad reportada durante el conteo físico inicial.
     * Se usa para comparar con el stock del sistema y calcular diferencias.
     */
    @Column(precision = 19, scale = 4)
    private BigDecimal initialPhysicalCount = BigDecimal.ZERO;

    /**
     * Cuenta contable contrapartida para movimientos de inventario.
     * Requerido por la Resolución 340/2004 para automatización de asientos.
     */
    @Column(length = 50)
    private String contraAccountCode;

    /**
     * Centro de costos asociado al producto para reportes analíticos.
     */
    @Column(length = 50)
    private String costCenterCode;

    // Getters y Setters adicionales para campos de cumplimiento
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

    public InventoryCategory getCategory() {
        return category;
    }

    public void setCategory(InventoryCategory category) {
        this.category = category;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(BigDecimal minimumStock) {
        this.minimumStock = minimumStock;
    }

    public BigDecimal getMaximumStock() {
        return maximumStock;
    }

    public void setMaximumStock(BigDecimal maximumStock) {
        this.maximumStock = maximumStock;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public ThirdParty getSupplier() {
        return supplier;
    }

    public void setSupplier(ThirdParty supplier) {
        this.supplier = supplier;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    /**
     * Verifica si el producto está en proceso de carga inicial.
     */
    public boolean isInitialLoadInProgress() {
        return initialLoadInProgress;
    }

    /**
     * Establece el estado de carga inicial del producto.
     */
    public void setInitialLoadInProgress(boolean initialLoadInProgress) {
        this.initialLoadInProgress = initialLoadInProgress;
    }

    /**
     * Obtiene la cantidad reportada en el conteo físico inicial.
     */
    public BigDecimal getInitialPhysicalCount() {
        return initialPhysicalCount;
    }

    /**
     * Establece la cantidad reportada en el conteo físico inicial.
     */
    public void setInitialPhysicalCount(BigDecimal initialPhysicalCount) {
        this.initialPhysicalCount = initialPhysicalCount;
    }

    /**
     * Obtiene el código de la cuenta contable contrapartida.
     */
    public String getContraAccountCode() {
        return contraAccountCode;
    }

    /**
     * Establece el código de la cuenta contable contrapartida.
     */
    public void setContraAccountCode(String contraAccountCode) {
        this.contraAccountCode = contraAccountCode;
    }

    /**
     * Obtiene el código del centro de costos asociado.
     */
    public String getCostCenterCode() {
        return costCenterCode;
    }

    /**
     * Establece el código del centro de costos asociado.
     */
    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    /**
     * Calcula la diferencia entre el stock del sistema y el conteo físico inicial.
     * @return Diferencia positiva si hay exceso, negativa si hay faltante
     */
    public BigDecimal calculateInitialDifference() {
        if (initialPhysicalCount == null) {
            return BigDecimal.ZERO;
        }
        return initialPhysicalCount.subtract(currentStock);
    }

    /**
     * Verifica si el stock actual está por encima del máximo.
     */
    public boolean isAboveMaximumStock() {
        return currentStock.compareTo(maximumStock) > 0;
    }

    /**
     * Calcula el valor total del inventario (costo * stock).
     */
    public BigDecimal getTotalValue() {
        return unitCost.multiply(currentStock);
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", currentStock=" + currentStock +
                ", unitCost=" + unitCost +
                '}';
    }
}
