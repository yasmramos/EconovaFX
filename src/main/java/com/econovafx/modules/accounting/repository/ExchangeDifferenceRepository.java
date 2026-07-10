package com.econovafx.modules.accounting.repository;

import com.econovafx.modules.accounting.model.ExchangeDifference;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de diferencias cambiarias.
 */
@Singleton
public class ExchangeDifferenceRepository {

    private final Database database;

    @Inject
    public ExchangeDifferenceRepository(Database database) {
        this.database = database;
    }

    /**
     * Guarda una diferencia cambiaria.
     */
    public ExchangeDifference save(ExchangeDifference exchangeDifference) {
        database.save(exchangeDifference);
        return exchangeDifference;
    }

    /**
     * Actualiza una diferencia cambiaria.
     */
    public void update(ExchangeDifference exchangeDifference) {
        database.update(exchangeDifference);
    }

    /**
     * Obtiene una diferencia cambiaria por su ID.
     */
    public Optional<ExchangeDifference> findById(Long id) {
        return Optional.ofNullable(database.find(ExchangeDifference.class).setId(id).findOne());
    }

    /**
     * Obtiene todas las diferencias cambiarias.
     */
    public List<ExchangeDifference> findAll() {
        return database.find(ExchangeDifference.class).findList();
    }

    /**
     * Obtiene diferencias cambiarias por documento (factura).
     */
    public List<ExchangeDifference> findByDocument(String documentType, Long documentId) {
        return database.find(ExchangeDifference.class)
                .where()
                .eq("documentType", documentType)
                .eq("documentId", documentId)
                .findList();
    }

    /**
     * Obtiene diferencias cambiarias por tipo (GANANCIA o PERDIDA).
     */
    public List<ExchangeDifference> findByDifferenceType(ExchangeDifference.DifferenceType type) {
        return database.find(ExchangeDifference.class)
                .where()
                .eq("differenceType", type)
                .findList();
    }

    /**
     * Obtiene el total de ganancias cambiarias en un período.
     */
    public java.math.BigDecimal getTotalGains(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<java.math.BigDecimal> amounts = database.find(ExchangeDifference.class)
                .select("differenceAmount")
                .where()
                .eq("differenceType", ExchangeDifference.DifferenceType.GAIN)
                .between("paymentDate", startDate, endDate)
                .findSingleAttributeList();
        
        return amounts.stream()
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * Obtiene el total de pérdidas cambiarias en un período.
     */
    public java.math.BigDecimal getTotalLosses(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<java.math.BigDecimal> amounts = database.find(ExchangeDifference.class)
                .select("differenceAmount")
                .where()
                .eq("differenceType", ExchangeDifference.DifferenceType.LOSS)
                .between("paymentDate", startDate, endDate)
                .findSingleAttributeList();
        
        return amounts.stream()
                .filter(java.util.Objects::nonNull)
                .map(java.math.BigDecimal::abs)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
