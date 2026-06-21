package com.econovafx.domain;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una Moneda en el sistema según Resolución 340/2004.
 * Soporta operaciones en múltiples monedas (CUP, USD, EUR, etc.)
 */
@Entity
@Table(name = "currencies")
public class Currency extends BaseEntity {

    @Id
    private Long id;

    @jakarta.persistence.Column(nullable = false, unique = true, length = 3)
    private String code; // Código ISO: CUP, USD, EUR, etc.

    @jakarta.persistence.Column(nullable = false, length = 50)
    private String name; // Nombre de la moneda

    @jakarta.persistence.Column(length = 10)
    private String symbol = "$"; // Símbolo de la moneda

    /**
     * Tasa de cambio respecto a la moneda base de la empresa.
     * Ejemplo: si la base es CUP y esta es USD, rate podría ser 24.0
     */
    @jakarta.persistence.Column(precision = 18, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    /**
     * Fecha de vigencia de la tasa de cambio
     */
    @jakarta.persistence.Column(name = "rate_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime rateDate;

    /**
     * Indica si es la moneda base del sistema
     */
    @jakarta.persistence.Column(name = "is_base")
    private Boolean isBase = false;

    /**
     * Estado: ACTIVE, INACTIVE
     */
    @jakarta.persistence.Column(length = 20)
    private String status = "ACTIVE";

    /**
     * Número de decimales para esta moneda
     */
    @jakarta.persistence.Column(name = "decimal_places")
    private Integer decimalPlaces = 2;

    @Version
    private Long version;

    // Constructores
    public Currency() {
    }

    public Currency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.exchangeRate = BigDecimal.ONE;
        this.isBase = false;
        this.status = "ACTIVE";
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public LocalDateTime getRateDate() {
        return rateDate;
    }

    public void setRateDate(LocalDateTime rateDate) {
        this.rateDate = rateDate;
    }

    public Boolean getIsBase() {
        return isBase;
    }

    public void setIsBase(Boolean isBase) {
        this.isBase = isBase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(Integer decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", exchangeRate=" + exchangeRate +
                ", isBase=" + isBase +
                '}';
    }
}
