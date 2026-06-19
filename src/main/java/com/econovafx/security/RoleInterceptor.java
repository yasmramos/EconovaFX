package com.econovafx.security;

import io.avaje.inject.aop.Aspect;
import io.avaje.inject.aop.Invocation;

import java.lang.reflect.Method;

/**
 * Interceptor for @RequiresRole annotation.
 * Blocks method execution if the current user does not have the required role(s).
 */
@Aspect
public class RoleInterceptor {

    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.method();
        
        // Check for @RequiresRole on method
        RequiresRole requiresRole = method.getAnnotation(RequiresRole.class);
        
        // If not on method, check on class level
        if (requiresRole == null) {
            requiresRole = invocation.target().getClass().getAnnotation(RequiresRole.class);
        }
        
        if (requiresRole != null) {
            String[] roles = requiresRole.value();
            RequiresRole.Logical logical = requiresRole.logical();
            
            if (logical == RequiresRole.Logical.AND) {
                // User must have ALL roles
                for (String role : roles) {
                    SecurityUtil.requireRole(role);
                }
            } else {
                // User must have AT LEAST ONE role
                SecurityUtil.requireAnyRole(roles);
            }
        }
        
        // Proceed with method execution
        return invocation.proceed();
    }
}
