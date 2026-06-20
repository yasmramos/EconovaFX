package com.econovafx.repository;

import com.econovafx.domain.ExchangeRate;
import com.econovafx.domain.Currency;
import io.avaje.inject.Component;
import io.ebean.Database;
import io.ebean.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de tipos de cambio según Resolución 340/2004.
 */
@Component
public class ExchangeRateRepository {

    private final Database database;

    public ExchangeRateRepository(Database database) {
        this.database = database;
    }

    /**
     * Obtiene la tasa de cambio vigente entre dos monedas en una fecha determinada
     */
    public Optional<ExchangeRate> findCurrentRate(Currency fromCurrency, Currency toCurrency, LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now();
        }

        ExchangeRate rate = database.find(ExchangeRate.class)
                .where()
                .eq("fromCurrency", fromCurrency)
                .eq("toCurrency", toCurrency)
                .le("effectiveDate", date)
                .eq("status", "ACTIVE")
                .orderBy().desc("effectiveDate")
                .setMaxRows(1)
                .findOne();

        return Optional.ofNullable(rate);
    }

    /**
     * Obtiene todas las tasas de cambio entre dos monedas
     */
    public List<ExchangeRate> findAllRates(Currency fromCurrency, Currency toCurrency) {
        return database.find(ExchangeRate.class)
                .where()
                .eq("fromCurrency", fromCurrency)
                .eq("toCurrency", toCurrency)
                .orderBy().desc("effectiveDate")
                .findList();
    }

    /**
     * Obtiene el histórico de tasas de cambio de los últimos días
     */
    public List<ExchangeRate> findHistory(Currency fromCurrency, Currency toCurrency, int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return database.find(ExchangeRate.class)
                .where()
                .eq("fromCurrency", fromCurrency)
                .eq("toCurrency", toCurrency)
                .ge("effectiveDate", fromDate)
                .orderBy().desc("effectiveDate")
                .findList();
    }

    /**
     * Registra una nueva tasa de cambio
     */
    public ExchangeRate save(ExchangeRate exchangeRate) {
        try (Transaction transaction = database.beginTransaction()) {
            // Desactivar tasas anteriores del mismo tipo
            database.find(ExchangeRate.class)
                    .where()
                    .eq("fromCurrency", exchangeRate.getFromCurrency())
                    .eq("toCurrency", exchangeRate.getToCurrency())
                    .eq("rateType", exchangeRate.getRateType())
                    .eq("status", "ACTIVE")
                    .findList()
                    .forEach(rate -> {
                        rate.setStatus("HISTORICAL");
                        database.save(rate);
                    });

            database.save(exchangeRate);
            transaction.commit();
            return exchangeRate;
        }
    }

    /**
     * Obtiene todas las tasas activas
     */
    public List<ExchangeRate> findAllActive() {
        return database.find(ExchangeRate.class)
                .fetch("fromCurrency")
                .fetch("toCurrency")
                .where().eq("status", "ACTIVE")
                .orderBy().desc("effectiveDate")
                .findList();
    }

    /**
     * Busca una tasa por ID
     */
    public Optional<ExchangeRate> findById(Long id) {
        ExchangeRate rate = database.find(ExchangeRate.class, id);
        return Optional.ofNullable(rate);
    }

    /**
     * Elimina una tasa de cambio (marca como inactiva)
     */
    public void deactivate(Long id) {
        try (Transaction transaction = database.beginTransaction()) {
            ExchangeRate rate = database.find(ExchangeRate.class, id);
            if (rate != null) {
                rate.setStatus("INACTIVE");
                database.save(rate);
                transaction.commit();
            }
        }
    }

    /**
     * Obtiene la tasa de cambio inversa
     */
    public Optional<ExchangeRate> findInverseRate(Currency fromCurrency, Currency toCurrency, LocalDateTime date) {
        return findCurrentRate(toCurrency, fromCurrency, date);
    }

    /**
     * Calcula la tasa de cambio entre dos monedas usando la moneda base como intermediaria
     */
    public java.math.BigDecimal calculateCrossRate(Currency fromCurrency, Currency toCurrency, 
                                                   Currency baseCurrency, LocalDateTime date) {
        if (fromCurrency.equals(toCurrency)) {
            return java.math.BigDecimal.ONE;
        }

        Optional<ExchangeRate> fromToBase = findCurrentRate(fromCurrency, baseCurrency, date);
        Optional<ExchangeRate> baseToTo = findCurrentRate(baseCurrency, toCurrency, date);

        if (fromToBase.isPresent() && baseToTo.isPresent()) {
            return fromToBase.get().getRate().multiply(baseToTo.get().getRate());
        }

        return java.math.BigDecimal.ZERO;
    }

    /**
     * Convierte un monto de una moneda a otra
     */
    public java.math.BigDecimal convertAmount(java.math.BigDecimal amount, 
                                              Currency fromCurrency, 
                                              Currency toCurrency,
                                              LocalDateTime date) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        Optional<ExchangeRate> rate = findCurrentRate(fromCurrency, toCurrency, date);
        if (rate.isPresent()) {
            return amount.multiply(rate.get().getRate());
        }

        // Intentar con la tasa inversa
        Optional<ExchangeRate> inverseRate = findInverseRate(fromCurrency, toCurrency, date);
        if (inverseRate.isPresent()) {
            return amount.divide(inverseRate.get().getRate(), java.math.RoundingMode.HALF_UP);
        }

        throw new IllegalStateException("No se encontró tasa de cambio entre " + 
                                        fromCurrency.getCode() + " y " + toCurrency.getCode());
    }
}
