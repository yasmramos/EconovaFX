package com.econovafx.security;

import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * Interceptor for @RequiresPermission annotation.
 * Blocks method execution if the current user does not have the required permission(s).
 */
public class PermissionInterceptor implements MethodInterceptor {

    @Override
    public void invoke(Invocation invocation) throws Throwable {
        Method method = invocation.method();
        
        // Check for @RequiresPermission on method
        RequiresPermission requiresPermission = method.getAnnotation(RequiresPermission.class);
        
        // If not on method, check on class level
        if (requiresPermission == null) {
            requiresPermission = method.getDeclaringClass().getAnnotation(RequiresPermission.class);
        }
        
        if (requiresPermission != null) {
            String[] permissions = requiresPermission.value();
            RequiresPermission.Logical logical = requiresPermission.logical();
            
            if (logical == RequiresPermission.Logical.AND) {
                // User must have ALL permissions
                SecurityUtil.requireAllPermissions(permissions);
            } else {
                // User must have AT LEAST ONE permission
                SecurityUtil.requireAnyPermission(permissions);
            }
        }
        
        // Proceed with method execution
        invocation.invoke();
    }
}
