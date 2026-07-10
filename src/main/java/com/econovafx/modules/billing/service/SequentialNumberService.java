package com.econovafx.modules.billing.service;

import com.econovafx.modules.billing.model.BillingSeries;
import com.econovafx.modules.billing.model.BillingSeries.DocumentType;
import com.econovafx.modules.billing.repository.BillingSeriesRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

/**
 * Servicio para gestión de numeración consecutiva de documentos.
 * Garantiza la integridad y secuencialidad estricta exigida por la Resolución 340/2004.
 */
@Component
public class SequentialNumberService {

    private static final Logger logger = LoggerFactory.getLogger(SequentialNumberService.class);

    private final BillingSeriesRepository billingSeriesRepository;

    @Inject
    public SequentialNumberService(BillingSeriesRepository billingSeriesRepository) {
        this.billingSeriesRepository = billingSeriesRepository;
    }

    /**
     * Obtiene el próximo número consecutivo para una serie documental.
     * Este método debe ser llamado dentro de una transacción para garantizar atomicidad.
     * 
     * @param seriesId ID de la serie de facturación
     * @return El próximo número consecutivo asignado
     * @throws IllegalStateException si la serie no existe o está inactiva
     * @throws IllegalArgumentException si se alcanza el límite de la serie
     */
    public synchronized int getNextSequentialNumber(Long seriesId) {
        BillingSeries series = billingSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new IllegalArgumentException("Serie de facturación no encontrada: " + seriesId));

        if (!series.isActive()) {
            throw new IllegalStateException("La serie de facturación está inactiva: " + series.getSeriesCode());
        }

        // Verificar si se alcanzó el límite
        if (series.getCurrentNumber() > series.getEndNumber()) {
            throw new IllegalStateException(
                String.format("Se alcanzó el límite de la serie %s (%d). Límite: %d",
                    series.getSeriesCode(), series.getCurrentNumber(), series.getEndNumber()));
        }

        int nextNumber = series.getCurrentNumber();
        series.setCurrentNumber(nextNumber + 1);
        billingSeriesRepository.update(series);

        logger.info("Número consecutivo asignado: Serie={}, Número={}", series.getSeriesCode(), nextNumber);
        
        return nextNumber;
    }

    /**
     * Genera el número de documento formateado según el patrón de la serie.
     * Formato típico: SERIE-NUMERO (ej: A-000123)
     * 
     * @param seriesId ID de la serie de facturación
     * @return Número de documento formateado
     */
    public String generateDocumentNumber(Long seriesId) {
        BillingSeries series = billingSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new IllegalArgumentException("Serie de facturación no encontrada: " + seriesId));

        int nextNumber = getNextSequentialNumber(seriesId);
        
        // Formatear el número con ceros a la izquierda (6 dígitos)
        DecimalFormat formatter = new DecimalFormat("000000");
        String formattedNumber = formatter.format(nextNumber);
        
        return series.getSeriesCode() + "-" + formattedNumber;
    }

    /**
     * Verifica si un número es válido para una serie dada.
     * 
     * @param seriesId ID de la serie
     * @param number Número a verificar
     * @return true si el número es válido para la serie
     */
    public boolean isValidNumberForSeries(Long seriesId, int number) {
        return billingSeriesRepository.findById(seriesId)
                .map(series -> number >= series.getStartNumber() && number <= series.getEndNumber())
                .orElse(false);
    }

    /**
     * Reinicia el contador de una serie (solo permitido para administradores).
     * 
     * @param seriesId ID de la serie
     * @param newStartNumber Nuevo número inicial
     */
    public void resetCounter(Long seriesId, int newStartNumber) {
        BillingSeries series = billingSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new IllegalArgumentException("Serie de facturación no encontrada: " + seriesId));

        if (newStartNumber < series.getStartNumber() || newStartNumber > series.getEndNumber()) {
            throw new IllegalArgumentException(
                String.format("El número %d está fuera del rango permitido [%d, %d]",
                    newStartNumber, series.getStartNumber(), series.getEndNumber()));
        }

        series.setCurrentNumber(newStartNumber);
        billingSeriesRepository.update(series);

        logger.warn("Contador de serie reiniciado: Serie={}, Nuevo valor={}", series.getSeriesCode(), newStartNumber);
    }

    /**
     * Obtiene información sobre el estado actual de una serie.
     * 
     * @param seriesId ID de la serie
     * @return Mensaje descriptivo del estado
     */
    public String getSeriesStatus(Long seriesId) {
        return billingSeriesRepository.findById(seriesId)
                .map(series -> String.format(
                    "Serie: %s | Tipo: %s | Actual: %d | Rango: [%d, %d] | Activa: %b",
                    series.getSeriesCode(),
                    series.getDocumentType().getDisplayName(),
                    series.getCurrentNumber(),
                    series.getStartNumber(),
                    series.getEndNumber(),
                    series.isActive()))
                .orElse("Serie no encontrada");
    }
}
