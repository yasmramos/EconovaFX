package com.econovafx.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity representing an Exchange Rate according to Resolution 340/2004.
 * Records historical exchange rates between currencies.
 */
@Entity
@Table(name = "exchange_rates")
public class ExchangeRate extends BaseEntity {

    /**
     * Source currency (the one being converted)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_currency_id", nullable = false)
    private Currency fromCurrency;

    /**
     * Target currency (the one being converted to)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_currency_id", nullable = false)
    private Currency toCurrency;

    /**
     * Exchange rate: how many units of toCurrency equal 1 unit of fromCurrency
     */
    @Column(nullable = false, precision = 18, scale = 6)
    private java.math.BigDecimal rate;

    /**
     * Date when the rate becomes effective
     */
    @Column(name = "effective_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime effectiveDate;

    /**
     * End date of validity (optional, for temporary rates)
     */
    @Column(name = "end_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime endDate;

    /**
     * Rate type: OFICIAL, MERCADO, ESPECIAL, CONTABLE
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RateType rateType = RateType.OFICIAL;

    /**
     * User who registered the rate
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user")
    private User createdByUser;

    /**
     * Observations about the rate
     */
    @Column(length = 500)
    private String observations;

    /**
     * Status: ACTIVE, INACTIVE, HISTORICAL
     */
    @Column(length = 20)
    private String status = "ACTIVE";

    public enum RateType {
        OFICIAL,      // Official rate from Central Bank
        MERCADO,      // Market rate
        ESPECIAL,     // Special rate for specific operations
        CONTABLE      // Accounting rate for closing
    }

    // Constructors
    public ExchangeRate() {
    }

    public ExchangeRate(Currency fromCurrency, Currency toCurrency, 
                       java.math.BigDecimal rate, LocalDateTime effectiveDate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.effectiveDate = effectiveDate;
        this.rateType = RateType.OFICIAL;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public Currency getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(Currency fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public Currency getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(Currency toCurrency) {
        this.toCurrency = toCurrency;
    }

    public java.math.BigDecimal getRate() {
        return rate;
    }

    public void setRate(java.math.BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public RateType getRateType() {
        return rateType;
    }

    public void setRateType(RateType rateType) {
        this.rateType = rateType;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
                "id=" + id +
                ", fromCurrency=" + (fromCurrency != null ? fromCurrency.getCode() : null) +
                ", toCurrency=" + (toCurrency != null ? toCurrency.getCode() : null) +
                ", rate=" + rate +
                ", effectiveDate=" + effectiveDate +
                ", rateType=" + rateType +
                '}';
    }
}
