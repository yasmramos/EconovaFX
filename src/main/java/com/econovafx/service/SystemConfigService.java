package com.econovafx.service;

import com.econovafx.model.SystemConfiguration;
import com.econovafx.repository.SystemConfigRepository;
import io.avaje.inject.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Servicio para gestión de configuración del sistema.
 * Integra Avaje Config con valores persistentes en base de datos.
 */
@Component
public class SystemConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigService.class);

    private final SystemConfigRepository repository;

    public SystemConfigService(SystemConfigRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtiene la configuración actual del sistema.
     * Si no existe, inicializa con valores por defecto.
     */
    public SystemConfiguration getCurrentConfig() {
        Optional<SystemConfiguration> config = repository.getCurrent();
        if (config.isEmpty()) {
            logger.info("Inicializando configuración del sistema con valores por defecto");
            return repository.initializeDefaults();
        }
        return config.get();
    }

    /**
     * Guarda la configuración del sistema.
     * @param config La configuración a guardar
     * @return La configuración guardada
     */
    public SystemConfiguration saveConfig(SystemConfiguration config) {
        logger.info("Guardando configuración del sistema");
        validateConfig(config);
        return repository.save(config);
    }

    /**
     * Valida la configuración antes de guardarla.
     * @param config Configuración a validar
     * @throws IllegalArgumentException si la configuración es inválida
     */
    private void validateConfig(SystemConfiguration config) {
        if (config.getEntityName() == null || config.getEntityName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la entidad es obligatorio");
        }

        if (config.getBaseCurrency() == null || config.getBaseCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda base es obligatoria");
        }

        if (config.getFiscalYearStartMonth() < 1 || config.getFiscalYearStartMonth() > 12) {
            throw new IllegalArgumentException("El mes de inicio del ejercicio fiscal debe estar entre 1 y 12");
        }

        if (config.getFiscalYearEndMonth() < 1 || config.getFiscalYearEndMonth() > 12) {
            throw new IllegalArgumentException("El mes de fin del ejercicio fiscal debe estar entre 1 y 12");
        }

        if (config.getFiscalYearStartMonth() >= config.getFiscalYearEndMonth()) {
            logger.warn("El mes de inicio ({}) es mayor o igual al mes de fin ({})", 
                config.getFiscalYearStartMonth(), config.getFiscalYearEndMonth());
        }

        if (config.getPasswordMinLength() < 4) {
            throw new IllegalArgumentException("La longitud mínima de contraseña debe ser al menos 4");
        }

        if (config.getMaxLoginAttempts() < 1) {
            throw new IllegalArgumentException("Los intentos máximos de login deben ser al menos 1");
        }

        if (config.getSessionTimeoutMinutes() < 5) {
            throw new IllegalArgumentException("El timeout de sesión debe ser al menos 5 minutos");
        }

        if (config.getAuditRetentionYears() < 1) {
            throw new IllegalArgumentException("Los años de retención de auditoría deben ser al menos 1");
        }

        if (config.getDecimalPrecision() < 0 || config.getDecimalPrecision() > 10) {
            throw new IllegalArgumentException("La precisión decimal debe estar entre 0 y 10");
        }

        // Validar método de valoración de inventario
        String valuationMethod = config.getInventoryValuationMethod();
        if (!"PEPS".equals(valuationMethod) && !"PROMEDIO_PONDERADO".equals(valuationMethod)) {
            throw new IllegalArgumentException("El método de valoración debe ser PEPS o PROMEDIO_PONDERADO");
        }

        // Validar tipo de régimen
        String regimeType = config.getRegimeType();
        if (!"MONO".equals(regimeType) && !"MULTI".equals(regimeType)) {
            throw new IllegalArgumentException("El tipo de régimen debe ser MONO o MULTI");
        }
    }

    /**
     * Verifica si la configuración ha sido inicializada.
     */
    public boolean isInitialized() {
        return repository.isInitialized();
    }

    /**
     * Obtiene un valor específico de configuración.
     * @param key Clave del valor (usando reflexión para obtener el campo)
     * @return El valor configurado o null si no existe
     */
    public Object getConfigValue(String key) {
        SystemConfiguration config = getCurrentConfig();
        
        // Mapeo manual de claves comunes
        return switch (key) {
            case "entityName" -> config.getEntityName();
            case "entityNIF" -> config.getEntityNIF();
            case "baseCurrency" -> config.getBaseCurrency();
            case "accountingPlan" -> config.getAccountingPlan();
            case "fiscalYearStartMonth" -> config.getFiscalYearStartMonth();
            case "fiscalYearEndMonth" -> config.getFiscalYearEndMonth();
            case "regimeType" -> config.getRegimeType();
            case "passwordMinLength" -> config.getPasswordMinLength();
            case "auditEnabled" -> config.getAuditEnabled();
            case "inventoryValuationMethod" -> config.getInventoryValuationMethod();
            case "decimalPrecision" -> config.getDecimalPrecision();
            case "timeZone" -> config.getTimeZone();
            case "locale" -> config.getLocale();
            default -> null;
        };
    }

    /**
     * Reinicia la configuración a valores por defecto.
     * @return La configuración reinicializada
     */
    public SystemConfiguration resetToDefaults() {
        logger.warn("Reiniciando configuración del sistema a valores por defecto");
        Optional<SystemConfiguration> existing = repository.getCurrent();
        if (existing.isPresent()) {
            // Eliminar la existente (soft delete o hard delete según implementación)
            // Por ahora, simplemente creamos una nueva si no hay mecanismo de delete
            logger.info("Configuración existente será reemplazada");
        }
        return repository.initializeDefaults();
    }
}
