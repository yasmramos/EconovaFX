package com.econovafx.model;

import com.econovafx.model.BaseEntity;
import io.ebean.annotation.DbEnumValue;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Bank Reconciliation according to Resolution 340/2004.
 */
@Entity
@Table(name = "bank_reconciliation")
public class BankReconciliation extends BaseEntity {

    public enum Status {
        IN_PROGRESS, COMPLETED, CANCELLED
    }

    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(name = "reconciliation_number", nullable = false, unique = true)
    private String reconciliationNumber;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "bank_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal bankBalance;

    @Column(name = "system_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal systemBalance;

    @Column(name = "reconciled_balance", precision = 19, scale = 4)
    private BigDecimal reconciledBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.IN_PROGRESS;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by")
    private String completedBy;

    @OneToMany(mappedBy = "reconciliation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReconciliationItem> bankItems = new ArrayList<>();

    @OneToMany(mappedBy = "reconciliation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReconciliationItem> systemItems = new ArrayList<>();

    public BankReconciliation() {
        super();
        this.status = Status.IN_PROGRESS;
    }

    public Long getBankAccountId() { return bankAccountId; }
    public void setBankAccountId(Long bankAccountId) { this.bankAccountId = bankAccountId; }
    public String getReconciliationNumber() { return reconciliationNumber; }
    public void setReconciliationNumber(String reconciliationNumber) { this.reconciliationNumber = reconciliationNumber; }
    public LocalDate getStatementDate() { return statementDate; }
    public void setStatementDate(LocalDate statementDate) { this.statementDate = statementDate; }
    public BigDecimal getBankBalance() { return bankBalance; }
    public void setBankBalance(BigDecimal bankBalance) { this.bankBalance = bankBalance; }
    public BigDecimal getSystemBalance() { return systemBalance; }
    public void setSystemBalance(BigDecimal systemBalance) { this.systemBalance = systemBalance; }
    public BigDecimal getReconciledBalance() { return reconciledBalance; }
    public void setReconciledBalance(BigDecimal reconciledBalance) { this.reconciledBalance = reconciledBalance; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getCompletedBy() { return completedBy; }
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }
    public List<ReconciliationItem> getBankItems() { return bankItems; }
    public void setBankItems(List<ReconciliationItem> bankItems) { this.bankItems = bankItems; }
    public List<ReconciliationItem> getSystemItems() { return systemItems; }
    public void setSystemItems(List<ReconciliationItem> systemItems) { this.systemItems = systemItems; }

    public void addBankItem(ReconciliationItem item) {
        item.setReconciliation(this);
        item.setOriginType(ReconciliationItem.OriginType.BANK);
        this.bankItems.add(item);
    }

    public void addSystemItem(ReconciliationItem item) {
        item.setReconciliation(this);
        item.setOriginType(ReconciliationItem.OriginType.SYSTEM);
        this.systemItems.add(item);
    }
}
