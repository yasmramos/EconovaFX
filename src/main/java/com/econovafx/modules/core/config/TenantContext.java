package com.econovafx.modules.core.config;

import com.econovafx.modules.core.model.Company;

/**
 * Contexto de Tenant almacenado en ThreadLocal para gestión multi-empresa.
 * Mantiene la empresa activa actual para cada hilo de ejecución.
 */
public class TenantContext {

    private static final ThreadLocal<Company> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Establece la empresa activa para el hilo actual.
     * @param company La empresa a establecer como activa
     */
    public static void setCurrentTenant(Company company) {
        CURRENT_TENANT.set(company);
    }

    /**
     * Obtiene la empresa activa del hilo actual.
     * @return La empresa activa o null si no hay ninguna
     */
    public static Company getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Obtiene el ID de la empresa activa.
     * @return El ID de la empresa o null si no hay ninguna
     */
    public static Long getCurrentTenantId() {
        Company company = CURRENT_TENANT.get();
        return company != null ? company.getId() : null;
    }

    /**
     * Limpia el contexto del tenant para el hilo actual.
     * Debe llamarse al finalizar operaciones multi-tenant.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * Verifica si hay un tenant configurado en el contexto actual.
     * @return true si hay un tenant activo, false en caso contrario
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}
