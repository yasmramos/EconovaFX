package com.econovafx.modules.core.security;

import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.Company;
import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor que valida que siempre haya un tenant activo en el contexto
 * antes de ejecutar cualquier método anotado con @RequiresTenant.
 * 
 * Esto previene acceso accidental a datos sin contexto de tenant definido.
 */
public class TenantValidationInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TenantValidationInterceptor.class);

    @Override
    public void invoke(Invocation invocation) throws Throwable {
        // Verificar que haya un tenant activo en el contexto
        if (!TenantContext.hasTenant()) {
            String errorMessage = String.format(
                "No active tenant found in context. Cannot execute method: %s.%s",
                invocation.method().getDeclaringClass().getSimpleName(),
                invocation.method().getName()
            );
            logger.error("SECURITY VIOLATION: {}", errorMessage);
            throw new AuthorizationException(errorMessage);
        }

        // Verificar que el tenant tenga ID válido
        Company currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant.getId() == null) {
            String errorMessage = String.format(
                "Tenant has null ID. Cannot execute method: %s.%s",
                invocation.method().getDeclaringClass().getSimpleName(),
                invocation.method().getName()
            );
            logger.error("SECURITY VIOLATION: {}", errorMessage);
            throw new AuthorizationException(errorMessage);
        }

        logger.debug("Tenant validated: {} ({})", currentTenant.getCode(), currentTenant.getId());
        
        // Proceder con la ejecución del método
        invocation.invoke();
    }
}
