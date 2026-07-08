package com.econovafx.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Currency in the system according to Resolution 340/2004.
 * Supports multi-currency operations (CUP, USD, EUR, etc.)
 */
@Entity
@Table(name = "currencies")
public class Currency extends BaseEntity {

    @jakarta.persistence.Column(nullable = false, unique = true, length = 3)
    private String code; // ISO Code: CUP, USD, EUR, etc.

    @jakarta.persistence.Column(nullable = false, length = 50)
    private String name; // Currency name

    @jakarta.persistence.Column(length = 10)
    private String symbol = "$"; // Currency symbol

    /**
     * Exchange rate relative to the company's base currency.
     * Example: if base is CUP and this is USD, rate could be 24.0
     */
    @jakarta.persistence.Column(precision = 18, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    /**
     * Date when the exchange rate is valid
     */
    @jakarta.persistence.Column(name = "rate_date")
    private LocalDateTime rateDate;

    /**
     * Indicates if this is the base currency of the system
     */
    @jakarta.persistence.Column(name = "is_base")
    private Boolean isBase = false;

    /**
     * Status: ACTIVE, INACTIVE
     */
    @jakarta.persistence.Column(length = 20)
    private String status = "ACTIVE";

    /**
     * Number of decimal places for this currency
     */
    @jakarta.persistence.Column(name = "decimal_places")
    private Integer decimalPlaces = 2;

    // Constructors
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

    // Getters and Setters
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
