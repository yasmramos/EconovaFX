package com.econovafx.model;

import com.econovafx.model.BaseEntity;
import io.ebean.annotation.DbEnumValue;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a reconciliation item (bank or system side).
 */
@Entity
@Table(name = "reconciliation_item")
public class ReconciliationItem extends BaseEntity {

    public enum OriginType {
        BANK, SYSTEM
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false)
    private BankReconciliation reconciliation;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_type", nullable = false)
    private OriginType originType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private Boolean reconciled = false;

    @Column(name = "cash_movement_id")
    private Long cashMovementId;

    @Column(name = "bank_reference")
    private String bankReference;

    public ReconciliationItem() {
        super();
        this.reconciled = false;
    }

    public BankReconciliation getReconciliation() { return reconciliation; }
    public void setReconciliation(BankReconciliation reconciliation) { this.reconciliation = reconciliation; }
    public OriginType getOriginType() { return originType; }
    public void setOriginType(OriginType originType) { this.originType = originType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Boolean getReconciled() { return reconciled; }
    public void setReconciled(Boolean reconciled) { this.reconciled = reconciled; }
    public Long getCashMovementId() { return cashMovementId; }
    public void setCashMovementId(Long cashMovementId) { this.cashMovementId = cashMovementId; }
    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }
}
