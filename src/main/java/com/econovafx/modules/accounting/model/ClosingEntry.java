package com.econovafx.modules.accounting.model;

import com.econovafx.modules.core.model.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "closing_entries")
public class ClosingEntry extends BaseEntity {

    public enum ClosingType {
        INCOME, EXPENSE, RESULT, OPENING
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClosingType closingType;

    @Column(nullable = false)
    private LocalDate closingDate;

    @Column(nullable = false)
    private Integer fiscalYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction relatedTransaction;

    @Column(nullable = false)
    private boolean isPosted;

    // Getters y Setters
    public ClosingType getClosingType() { return closingType; }
    public void setClosingType(ClosingType closingType) { this.closingType = closingType; }
    
    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }
    
    public Integer getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(Integer fiscalYear) { this.fiscalYear = fiscalYear; }
    
    public Transaction getRelatedTransaction() { return relatedTransaction; }
    public void setRelatedTransaction(Transaction relatedTransaction) { this.relatedTransaction = relatedTransaction; }
    
    public boolean isPosted() { return isPosted; }
    public void setPosted(boolean posted) { isPosted = posted; }
}
