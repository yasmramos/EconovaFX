package com.econovafx.repository;

import com.econovafx.model.SystemConfiguration;
import io.avaje.inject.Component;
import io.ebean.Database;
import io.ebean.ExpressionList;

import java.util.Optional;

/**
 * Repositorio para gestión de configuración del sistema.
 */
@Component
public class SystemConfigRepository {

    private final Database database;

    public SystemConfigRepository(Database database) {
        this.database = database;
    }

    /**
     * Obtiene la configuración actual del sistema.
     * Solo debe existir un registro de configuración.
     */
    public Optional<SystemConfiguration> getCurrent() {
        return database.find(SystemConfiguration.class).findOneOrEmpty();
    }

    /**
     * Guarda o actualiza la configuración del sistema.
     */
    public SystemConfiguration save(SystemConfiguration config) {
        if (config.getId() == null) {
            // Verificar si ya existe una configuración
            Optional<SystemConfiguration> existing = getCurrent();
            if (existing.isPresent()) {
                // Actualizar la existente
                config.setId(existing.get().getId());
            }
        }
        database.save(config);
        return config;
    }

    /**
     * Inicializa la configuración con valores por defecto si no existe.
     */
    public SystemConfiguration initializeDefaults() {
        Optional<SystemConfiguration> existing = getCurrent();
        if (existing.isPresent()) {
            return existing.get();
        }

        SystemConfiguration defaults = new SystemConfiguration();
        defaults.setEntityName("Entidad Demo");
        defaults.setBaseCurrency("CUP");
        defaults.setAccountingPlan("Normalizado");
        defaults.setFiscalYearStartMonth(1);
        defaults.setFiscalYearEndMonth(12);
        defaults.setRegimeType("MONO");
        defaults.setPasswordMinLength(6);
        defaults.setPasswordRequireUppercase(false);
        defaults.setPasswordRequireNumbers(false);
        defaults.setPasswordExpirationDays(90);
        defaults.setMaxLoginAttempts(5);
        defaults.setSessionTimeoutMinutes(30);
        defaults.setAuditEnabled(true);
        defaults.setAuditRetentionYears(5);
        defaults.setAuditLogChanges(true);
        defaults.setIncludeEntityLogo(true);
        defaults.setAutoBackupEnabled(false);
        defaults.setBackupFrequencyDays(7);
        defaults.setAllowNegativeInventory(false);
        defaults.setInventoryValuationMethod("PROMEDIO_PONDERADO");
        defaults.setDecimalPrecision(2);
        defaults.setTimeZone("America/Havana");
        defaults.setLocale("es_CU");

        database.save(defaults);
        return defaults;
    }

    /**
     * Verifica si la configuración ha sido inicializada.
     */
    public boolean isInitialized() {
        return getCurrent().isPresent();
    }
}
