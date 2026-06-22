package com.econovafx.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing an accounting period (fiscal year/month).
 * Used to control closing and opening of financial periods.
 */
@Entity
@Table(name = "accounting_periods")
public class AccountingPeriod extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name; // e.g., "Fiscal Year 2024", "January 2024"

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate startDate;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodStatus status = PeriodStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType type = PeriodType.MONTHLY;

    @Column(length = 255)
    private String closedBy;

    @Column(columnDefinition = "DATE")
    private LocalDate closedDate;

    @Column(length = 500)
    private String closingNotes;

    public enum PeriodStatus {
        OPEN,
        CLOSED,
        LOCKED
    }

    public enum PeriodType {
        MONTHLY,
        ANNUAL,
        CUSTOM
    }

    // Constructors
    public AccountingPeriod() {
    }

    public AccountingPeriod(String name, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public AccountingPeriod(String name, LocalDate startDate, LocalDate endDate, PeriodType type) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public PeriodStatus getStatus() {
        return status;
    }

    public void setStatus(PeriodStatus status) {
        this.status = status;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public LocalDate getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }

    public boolean isOpen() {
        return this.status == PeriodStatus.OPEN;
    }

    public boolean isClosed() {
        return this.status == PeriodStatus.CLOSED || this.status == PeriodStatus.LOCKED;
    }

    public PeriodType getType() {
        return type;
    }

    public void setType(PeriodType type) {
        this.type = type;
    }

    public String getClosingNotes() {
        return closingNotes;
    }

    public void setClosingNotes(String closingNotes) {
        this.closingNotes = closingNotes;
    }

    public boolean isMonthly() {
        return this.type == PeriodType.MONTHLY;
    }

    public boolean isAnnual() {
        return this.type == PeriodType.ANNUAL;
    }
}
