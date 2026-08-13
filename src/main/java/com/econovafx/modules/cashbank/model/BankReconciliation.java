package com.econovafx.modules.cashbank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bank Reconciliation entity for matching bank statements with accounting records.
 * Complies with Resolution 340/2004 requirements for bank reconciliation.
 */
@Entity
@Table(name = "bank_reconciliations")
public class BankReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(nullable = false)
    private LocalDate reconciliationDate;

    @Column(nullable = false)
    private BigDecimal statementBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal bookBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal depositsInTransit = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal outstandingChecks = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal bankFees = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal interestEarned = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal adjustedBalance = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, COMPLETED, CANCELLED

    @Column(length = 255)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }

    public LocalDate getReconciliationDate() { return reconciliationDate; }
    public void setReconciliationDate(LocalDate reconciliationDate) { this.reconciliationDate = reconciliationDate; }

    public BigDecimal getStatementBalance() { return statementBalance; }
    public void setStatementBalance(BigDecimal statementBalance) { this.statementBalance = statementBalance; }

    public BigDecimal getBookBalance() { return bookBalance; }
    public void setBookBalance(BigDecimal bookBalance) { this.bookBalance = bookBalance; }

    public BigDecimal getDepositsInTransit() { return depositsInTransit; }
    public void setDepositsInTransit(BigDecimal depositsInTransit) { this.depositsInTransit = depositsInTransit; }

    public BigDecimal getOutstandingChecks() { return outstandingChecks; }
    public void setOutstandingChecks(BigDecimal outstandingChecks) { this.outstandingChecks = outstandingChecks; }

    public BigDecimal getBankFees() { return bankFees; }
    public void setBankFees(BigDecimal bankFees) { this.bankFees = bankFees; }

    public BigDecimal getInterestEarned() { return interestEarned; }
    public void setInterestEarned(BigDecimal interestEarned) { this.interestEarned = interestEarned; }

    public BigDecimal getAdjustedBalance() { return adjustedBalance; }
    public void setAdjustedBalance(BigDecimal adjustedBalance) { this.adjustedBalance = adjustedBalance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
