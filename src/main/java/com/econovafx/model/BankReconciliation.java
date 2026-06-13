package com.econovafx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Bank Reconciliation according to Resolution 340/2004.
 */
public class BankReconciliation {
    
    public enum Status {
        IN_PROGRESS, COMPLETED, CANCELLED
    }

    private Long id;
    private Long bankAccountId;
    private String reconciliationNumber;
    private LocalDate statementDate;
    private BigDecimal bankBalance;
    private BigDecimal systemBalance;
    private BigDecimal reconciledBalance;
    private Status status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime completedAt;
    private String completedBy;
    
    private List<ReconciliationItem> bankItems = new ArrayList<>();
    private List<ReconciliationItem> systemItems = new ArrayList<>();

    public BankReconciliation() {
        this.status = Status.IN_PROGRESS;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getCompletedBy() { return completedBy; }
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }
    public List<ReconciliationItem> getBankItems() { return bankItems; }
    public void setBankItems(List<ReconciliationItem> bankItems) { this.bankItems = bankItems; }
    public List<ReconciliationItem> getSystemItems() { return systemItems; }
    public void setSystemItems(List<ReconciliationItem> systemItems) { this.systemItems = systemItems; }

    public void addBankItem(ReconciliationItem item) {
        this.bankItems.add(item);
    }

    public void addSystemItem(ReconciliationItem item) {
        this.systemItems.add(item);
    }
}
