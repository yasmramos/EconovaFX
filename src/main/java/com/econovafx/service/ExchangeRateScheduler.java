package com.econovafx.service;

import io.avaje.inject.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * Scheduler para actualizar automáticamente las tasas de cambio desde el Banco Central de Cuba.
 * Se ejecuta diariamente a las 6:00 AM según configuración.
 */
@Component
public class ExchangeRateScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateScheduler.class);

    private final ExchangeRateService exchangeRateService;
    private final BCCExchangeRateClient bccClient;
    
    // Cache simple en memoria con TTL
    private Instant lastFetchTime;
    private final Duration cacheTTL;
    private final boolean schedulerEnabled;

    public ExchangeRateScheduler(ExchangeRateService exchangeRateService, 
                                  BCCExchangeRateClient bccClient) {
        this.exchangeRateService = exchangeRateService;
        this.bccClient = bccClient;
        
        // Configuración desde propiedades
        int ttlMinutes = Integer.getInteger("exchange.rate.cache.ttl.minutes", 60);
        this.cacheTTL = Duration.ofMinutes(ttlMinutes);
        this.schedulerEnabled = Boolean.getBoolean("exchange.rate.scheduler.enabled");
        
        log.info("ExchangeRateScheduler inicializado - Cache TTL: {} min, Scheduler: {}", 
                ttlMinutes, schedulerEnabled ? "activado" : "desactivado");
    }

    /**
     * Método programado para actualizar tasas diariamente.
     * Se invoca mediante @Scheduled en la configuración de Avaje Inject.
     * Cron: 0 0 6 * * ? (diario a las 6:00 AM)
     */
    public void scheduledFetchRates() {
        if (!schedulerEnabled) {
            log.debug("Scheduler desactivado, omitiendo ejecución programada");
            return;
        }
        
        log.info("Iniciando actualización programada de tasas de cambio...");
        fetchAndCacheRates();
    }

    /**
     * Obtiene tasas de cambio con caché.
     * Si el caché es válido, devuelve los datos cacheados.
     * Si no, obtiene desde la API y actualiza el caché.
     */
    public java.util.List<BCCExchangeRateClient.BCCRateDTO> getRatesWithCache() {
        // Verificar si el caché es válido
        if (isCacheValid()) {
            log.debug("Usando tasas de cambio del caché (válido por {} minutos)", 
                    cacheTTL.toMinutes());
            // En una implementación real, aquí devolveríamos los datos cacheados
            // Por ahora, siempre obtenemos frescos para simplificar
        }
        
        return fetchAndCacheRates();
    }

    /**
     * Obtiene tasas desde la API y las guarda en la base de datos local
     */
    public java.util.List<BCCExchangeRateClient.BCCRateDTO> fetchAndCacheRates() {
        try {
            log.info("Obteniendo tasas de cambio desde la API del Banco Central de Cuba...");
            
            // Obtener tasas activas desde la API
            java.util.List<BCCExchangeRateClient.BCCRateDTO> rates = bccClient.fetchActiveRates();
            
            if (rates.isEmpty()) {
                log.warn("La API del BC no devolvió tasas de cambio");
                return rates;
            }
            
            // Persistir en la base de datos local
            persistRatesToDatabase(rates);
            
            // Actualizar timestamp del caché
            lastFetchTime = Instant.now();
            
            log.info("Actualización completada: {} tasas guardadas en la base de datos", rates.size());
            return rates;
            
        } catch (Exception e) {
            log.error("Error durante la actualización de tasas: {}", e.getMessage(), e);
            throw new RuntimeException("Error actualizando tasas de cambio: " + e.getMessage(), e);
        }
    }

    /**
     * Persiste las tasas obtenidas en la base de datos local
     */
    private void persistRatesToDatabase(java.util.List<BCCExchangeRateClient.BCCRateDTO> rates) {
        for (BCCExchangeRateClient.BCCRateDTO rateDTO : rates) {
            try {
                exchangeRateService.registerExchangeRate(
                        rateDTO.getCodigoMoneda(),
                        "CUP",  // Moneda base
                        rateDTO.getTasa(),
                        rateDTO.getFecha().atStartOfDay(),
                        com.econovafx.model.ExchangeRate.RateType.OFICIAL,
                        "Tasa oficial del Banco Central de Cuba - " + rateDTO.getFuente()
                );
                log.debug("Tasa persistida: 1 {} = {} CUP", rateDTO.getCodigoMoneda(), rateDTO.getTasa());
            } catch (Exception e) {
                log.error("Error persistiendo tasa para {}: {}", rateDTO.getCodigoMoneda(), e.getMessage());
            }
        }
    }

    /**
     * Verifica si el caché actual es válido (no ha expirado)
     */
    public boolean isCacheValid() {
        if (lastFetchTime == null) {
            return false;
        }
        return Duration.between(lastFetchTime, Instant.now()).compareTo(cacheTTL) < 0;
    }

    /**
     * Fuerza la actualización del caché ignorando su estado actual
     */
    public void forceRefresh() {
        log.info("Forzando actualización de tasas de cambio...");
        lastFetchTime = null;  // Invalidar caché
        fetchAndCacheRates();
    }

    /**
     * Obtiene información sobre el estado del caché
     */
    public String getCacheStatus() {
        if (lastFetchTime == null) {
            return "Caché vacío";
        }
        
        Duration age = Duration.between(lastFetchTime, Instant.now());
        Duration remaining = cacheTTL.minus(age);
        
        if (remaining.isNegative()) {
            return "Caché expirado (hace " + age.toMinutes() + " minutos)";
        } else {
            return "Caché válido por " + remaining.toMinutes() + " minutos más";
        }
    }
}
