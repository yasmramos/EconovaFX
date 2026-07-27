package com.econovafx.modules.inventory.model;

import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.core.model.BaseEntity;
import com.econovafx.modules.core.model.User;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un movimiento de inventario (entrada, salida, ajuste, transferencia).
 * Cumple con los requisitos de trazabilidad según Resolución 340/2004.
 */
@Entity
@Table(name = "inventory_movements")
public class InventoryMovement extends BaseEntity {

    public enum MovementType {
        ENTRY,              // Entrada por compra, producción o devolución
        OUTPUT,             // Salida por venta, consumo o merma
        ADJUSTMENT,         // Ajuste por inventario físico
        TRANSFER,           // Transferencia entre almacenes
        INITIAL_LOAD        // Carga inicial de saldos (Requerido por Resolución 340/2004)
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MovementType type;

    @Column(nullable = false)
    private String documentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "third_party_id")
    private ThirdParty thirdParty;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction relatedTransaction;

    @WhenCreated
    @Column(nullable = false, updatable = false)
    private LocalDateTime movementDate;

    // ==================== CAMPOS PARA CUMPLIMIENTO RESOLUCIÓN 340/2004 CUBA ====================

    /**
     * Código del centro de costos asociado al movimiento.
     * Requerido para reportes analíticos según Resolución 340/2004.
     */
    @Column(length = 50)
    private String costCenterCode;

    /**
     * Diferencia calculada entre el conteo físico y el stock del sistema.
     * Solo aplica para movimientos de tipo ADJUSTMENT e INITIAL_LOAD.
     */
    @Column(precision = 19, scale = 4)
    private BigDecimal differenceQuantity = BigDecimal.ZERO;

    /**
     * Indica si el movimiento ha sido transferido al sub-ledger contable.
     * Requerido para el proceso de posting de inventarios.
     */
    @Column(nullable = false)
    private boolean postedToSubledger = false;

    /**
     * Fecha del período contable al que pertenece este movimiento.
     * Usado para validación de períodos cerrados.
     */
    @Column(nullable = false)
    private LocalDate accountingPeriodDate;

    // Getters y Setters adicionales
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public InventoryItem getItem() {
        return item;
    }

    public void setItem(InventoryItem item) {
        this.item = item;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ThirdParty getThirdParty() {
        return thirdParty;
    }

    public void setThirdParty(ThirdParty thirdParty) {
        this.thirdParty = thirdParty;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Transaction getRelatedTransaction() {
        return relatedTransaction;
    }

    public void setRelatedTransaction(Transaction relatedTransaction) {
        this.relatedTransaction = relatedTransaction;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
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
     * Obtiene la cantidad de diferencia entre conteo físico y sistema.
     */
    public BigDecimal getDifferenceQuantity() {
        return differenceQuantity;
    }

    /**
     * Establece la cantidad de diferencia, calculando automáticamente si es necesario.
     */
    public void setDifferenceQuantity(BigDecimal differenceQuantity) {
        this.differenceQuantity = differenceQuantity;
    }

    /**
     * Verifica si el movimiento ha sido transferido al sub-ledger contable.
     */
    public boolean isPostedToSubledger() {
        return postedToSubledger;
    }

    /**
     * Marca el movimiento como transferido al sub-ledger contable.
     */
    public void setPostedToSubledger(boolean postedToSubledger) {
        this.postedToSubledger = postedToSubledger;
    }

    /**
     * Obtiene la fecha del período contable asociado.
     */
    public LocalDate getAccountingPeriodDate() {
        return accountingPeriodDate;
    }

    /**
     * Establece la fecha del período contable asociado.
     */
    public void setAccountingPeriodDate(LocalDate accountingPeriodDate) {
        this.accountingPeriodDate = accountingPeriodDate;
    }

    /**
     * Calcula automáticamente el monto total si no está establecido.
     */
    public void calculateTotalAmount() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.totalAmount = quantity.multiply(unitCost);
        }
    }

    @Override
    public String toString() {
        return "InventoryMovement{" +
                "id=" + id +
                ", type=" + type +
                ", documentNumber='" + documentNumber + '\'' +
                ", item=" + (item != null ? item.getName() : "null") +
                ", quantity=" + quantity +
                '}';
    }
}
