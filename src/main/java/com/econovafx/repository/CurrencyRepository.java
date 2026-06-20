package com.econovafx.repository;

import com.econovafx.domain.Currency;
import io.avaje.inject.Component;
import io.ebean.Database;
import io.ebean.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de monedas según Resolución 340/2004.
 */
@Component
public class CurrencyRepository {

    private final Database database;

    public CurrencyRepository(Database database) {
        this.database = database;
    }

    /**
     * Obtiene todas las monedas activas
     */
    public List<Currency> findAllActive() {
        return database.find(Currency.class)
                .where().eq("status", "ACTIVE")
                .orderBy().asc("code")
                .findList();
    }

    /**
     * Obtiene todas las monedas (activas e inactivas)
     */
    public List<Currency> findAll() {
        return database.find(Currency.class)
                .orderBy().asc("code")
                .findList();
    }

    /**
     * Busca una moneda por su código ISO
     */
    public Optional<Currency> findByCode(String code) {
        Currency currency = database.find(Currency.class)
                .where().eq("code", code.toUpperCase())
                .findOne();
        return Optional.ofNullable(currency);
    }

    /**
     * Obtiene la moneda base del sistema
     */
    public Optional<Currency> findBaseCurrency() {
        Currency currency = database.find(Currency.class)
                .where().eq("isBase", true)
                .eq("status", "ACTIVE")
                .findOne();
        return Optional.ofNullable(currency);
    }

    /**
     * Guarda o actualiza una moneda
     */
    public Currency save(Currency currency) {
        try (Transaction transaction = database.beginTransaction()) {
            if (currency.getIsBase() != null && currency.getIsBase()) {
                // Si esta es la moneda base, desmarcar las demás
                database.find(Currency.class)
                        .where().eq("isBase", true)
                        .ne("id", currency.getId())
                        .findList()
                        .forEach(c -> {
                            c.setIsBase(false);
                            database.save(c);
                        });
            }
            database.save(currency);
            transaction.commit();
            return currency;
        }
    }

    /**
     * Elimina una moneda (marca como inactiva)
     */
    public void deactivate(Long id) {
        try (Transaction transaction = database.beginTransaction()) {
            Currency currency = database.find(Currency.class, id);
            if (currency != null) {
                currency.setStatus("INACTIVE");
                database.save(currency);
                transaction.commit();
            }
        }
    }

    /**
     * Actualiza la tasa de cambio de una moneda
     */
    public void updateExchangeRate(Long id, java.math.BigDecimal newRate) {
        try (Transaction transaction = database.beginTransaction()) {
            Currency currency = database.find(Currency.class, id);
            if (currency != null) {
                currency.setExchangeRate(newRate);
                currency.setRateDate(java.time.LocalDateTime.now());
                database.save(currency);
                transaction.commit();
            }
        }
    }

    /**
     * Verifica si existe una moneda con el código dado
     */
    public boolean existsByCode(String code) {
        return database.find(Currency.class)
                .where().eq("code", code.toUpperCase())
                .exists();
    }

    /**
     * Obtiene el count de monedas activas
     */
    public long countActive() {
        return database.find(Currency.class)
                .where().eq("status", "ACTIVE")
                .findCount();
    }
}
