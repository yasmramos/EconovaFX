package com.econovafx.domain;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un Tipo de Cambio según Resolución 340/2004.
 * Registra el histórico de tasas de cambio entre monedas.
 */
@Entity
@Table(name = "exchange_rates")
public class ExchangeRate extends BaseEntity {

    @Id
    private Long id;

    /**
     * Moneda origen (la que se convierte)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_currency_id", nullable = false)
    private Currency fromCurrency;

    /**
     * Moneda destino (a la que se convierte)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_currency_id", nullable = false)
    private Currency toCurrency;

    /**
     * Tasa de cambio: cuántas unidades de toCurrency equivalen a 1 unidad de fromCurrency
     */
    @Column(nullable = false, precision = 18, scale = 6)
    private java.math.BigDecimal rate;

    /**
     * Fecha de vigencia de la tasa
     */
    @Column(name = "effective_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime effectiveDate;

    /**
     * Fecha de fin de vigencia (opcional, para tasas temporales)
     */
    @Column(name = "end_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime endDate;

    /**
     * Tipo de tasa: OFICIAL, MERCADO, ESPECIAL
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RateType rateType = RateType.OFICIAL;

    /**
     * Usuario que registró la tasa
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user")
    private User createdByUser;

    /**
     * Observaciones sobre la tasa
     */
    @Column(length = 500)
    private String observations;

    /**
     * Estado: ACTIVE, INACTIVE, HISTORICAL
     */
    @Column(length = 20)
    private String status = "ACTIVE";

    @Version
    private Long version;

    @WhenCreated
    private LocalDateTime createdAt;

    @WhenModified
    private LocalDateTime updatedAt;

    public enum RateType {
        OFICIAL,      // Tasa oficial del Banco Central
        MERCADO,      // Tasa de mercado
        ESPECIAL,     // Tasa especial para operaciones específicas
        CONTABLE      // Tasa contable para cierre
    }

    // Constructores
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

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
