package com.econovafx.config;

import com.econovafx.security.*;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;
import io.avaje.inject.aop.MethodInterceptor;

/**
 * Factory class para registrar los interceptores de seguridad en Avaje Inject.
 * 
 * Registra:
 * - TenantValidationInterceptor: Valida tenant activo en métodos @RequiresTenant
 * - AuthenticationInterceptor: Valida autenticación en métodos @RequiresAuthentication
 * - RoleInterceptor: Valida roles en métodos @RequiresRole
 * - PermissionInterceptor: Valida permisos en métodos @RequiresPermission
 */
@Factory
public class SecurityModule {

    /**
     * Registra el interceptor de validación de tenant.
     * Se ejecuta antes de cualquier método anotado con @RequiresTenant.
     * 
     * @return El interceptor de validación de tenant
     */
    @Bean
    @Named("tenantValidation")
    public MethodInterceptor tenantValidationInterceptor() {
        return new TenantValidationInterceptor();
    }

    /**
     * Registra el interceptor de autenticación.
     * Se ejecuta antes de cualquier método anotado con @RequiresAuthentication.
     * 
     * @return El interceptor de autenticación
     */
    @Bean
    @Named("authentication")
    public MethodInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor();
    }

    /**
     * Registra el interceptor de roles.
     * Se ejecuta antes de cualquier método anotado con @RequiresRole.
     * 
     * @return El interceptor de roles
     */
    @Bean
    @Named("role")
    public MethodInterceptor roleInterceptor() {
        return new RoleInterceptor();
    }

    /**
     * Registra el interceptor de permisos.
     * Se ejecuta antes de cualquier método anotado con @RequiresPermission.
     * 
     * @return El interceptor de permisos
     */
    @Bean
    @Named("permission")
    public MethodInterceptor permissionInterceptor() {
        return new PermissionInterceptor();
    }
}
