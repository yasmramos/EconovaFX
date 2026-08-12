package com.econovafx.modules.payroll.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;

/**
 * Payroll period entity for defining payroll cycles.
 * Supports weekly, biweekly, monthly, and custom periods.
 */
@Entity
@Table(name = "payroll_periods")
public class PayrollPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_code", unique = true, nullable = false)
    private String periodCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "frequency")
    @Enumerated(EnumType.STRING)
    private FrequencyType frequency = FrequencyType.MONTHLY;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "is_closed")
    private boolean closed = false;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "week_number")
    private Integer weekNumber;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FrequencyType getFrequency() {
        return frequency;
    }

    public void setFrequency(FrequencyType frequency) {
        this.frequency = frequency;
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

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    /**
     * Calculate the number of days in the period.
     */
    public int getDaysInPeriod() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return Period.between(startDate, endDate).getDays() + 1;
    }

    /**
     * Check if a date falls within this period.
     */
    public boolean containsDate(LocalDate date) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Frequency type enumeration.
     */
    public enum FrequencyType {
        WEEKLY,           // Every week
        BIWEEKLY,         // Every two weeks
        SEMIMONTHLY,      // Twice per month
        MONTHLY,          // Once per month
        BIMONTHLY,        // Every two months
        QUARTERLY,        // Every three months
        ANNUAL            // Once per year
    }
}
