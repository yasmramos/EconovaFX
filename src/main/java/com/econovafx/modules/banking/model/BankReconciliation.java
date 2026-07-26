package com.econovafx.modules.banking.model;

import com.econovafx.modules.core.model.BaseEntity;
import com.econovafx.modules.accounting.model.Transaction;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bank_reconciliations")
public class BankReconciliation extends BaseEntity {

    @Column(nullable = false)
    private String bankAccountNumber;

    @Column(nullable = false)
    private LocalDate reconciliationDate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal bankStatementBalance;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal bookBalance;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal reconciledBalance;

    @Column(nullable = false)
    private boolean isCompleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private com.econovafx.modules.core.model.User completedBy;

    @Column
    private LocalDate completedDate;

    // Getters y Setters
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    
    public LocalDate getReconciliationDate() { return reconciliationDate; }
    public void setReconciliationDate(LocalDate reconciliationDate) { this.reconciliationDate = reconciliationDate; }
    
    public BigDecimal getBankStatementBalance() { return bankStatementBalance; }
    public void setBankStatementBalance(BigDecimal bankStatementBalance) { this.bankStatementBalance = bankStatementBalance; }
    
    public BigDecimal getBookBalance() { return bookBalance; }
    public void setBookBalance(BigDecimal bookBalance) { this.bookBalance = bookBalance; }
    
    public BigDecimal getReconciledBalance() { return reconciledBalance; }
    public void setReconciledBalance(BigDecimal reconciledBalance) { this.reconciledBalance = reconciledBalance; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public com.econovafx.modules.core.model.User getCompletedBy() { return completedBy; }
    public void setCompletedBy(com.econovafx.modules.core.model.User completedBy) { this.completedBy = completedBy; }
    
    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }
}
