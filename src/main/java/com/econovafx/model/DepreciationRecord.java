package com.econovafx.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro de depreciación mensual de Activos Fijos.
 * Genera el asiento contable automático según Resolución 340/2004.
 */
@Entity
@Table(name = "depreciation_records")
public class DepreciationRecord extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "fixed_asset_id", nullable = false)
    private FixedAsset fixedAsset;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month; // 1-12

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal depreciationAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal accumulatedDepreciation;

    @Column(precision = 18, scale = 2)
    private BigDecimal netBookValue;

    @Column(nullable = false)
    private LocalDate processingDate;

    @Column(name = "journal_entry_id")
    private Long journalEntryId; // Referencia al asiento contable generado

    @Column(length = 255)
    private String notes;

    @Column(nullable = false)
    private boolean posted = false; // Si ya se generó el asiento contable

    // Getters and Setters
    public FixedAsset getFixedAsset() { return fixedAsset; }
    public void setFixedAsset(FixedAsset fixedAsset) { this.fixedAsset = fixedAsset; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public BigDecimal getDepreciationAmount() { return depreciationAmount; }
    public void setDepreciationAmount(BigDecimal depreciationAmount) { this.depreciationAmount = depreciationAmount; }

    public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }

    public BigDecimal getNetBookValue() { return netBookValue; }
    public void setNetBookValue(BigDecimal netBookValue) { this.netBookValue = netBookValue; }

    public LocalDate getProcessingDate() { return processingDate; }
    public void setProcessingDate(LocalDate processingDate) { this.processingDate = processingDate; }

    public Long getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(Long journalEntryId) { this.journalEntryId = journalEntryId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isPosted() { return posted; }
    public void setPosted(boolean posted) { this.posted = posted; }
}
