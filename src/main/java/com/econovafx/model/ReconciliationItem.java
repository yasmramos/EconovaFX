package com.econovafx.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a reconciliation item (bank or system side).
 */
public class ReconciliationItem {
    
    public enum OriginType {
        BANK, SYSTEM
    }

    private Long id;
    private Long reconciliationId;
    private OriginType originType;
    private String description;
    private LocalDate date;
    private BigDecimal amount;
    private boolean reconciled;
    private Long cashMovementId;
    private String bankReference;

    public ReconciliationItem() {
        this.reconciled = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReconciliationId() { return reconciliationId; }
    public void setReconciliationId(Long reconciliationId) { this.reconciliationId = reconciliationId; }
    public OriginType getOriginType() { return originType; }
    public void setOriginType(OriginType originType) { this.originType = originType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public boolean isReconciled() { return reconciled; }
    public void setReconciled(boolean reconciled) { this.reconciled = reconciled; }
    public Long getCashMovementId() { return cashMovementId; }
    public void setCashMovementId(Long cashMovementId) { this.cashMovementId = cashMovementId; }
    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }
}
