package com.econovafx.modules.core.security;

import io.avaje.inject.aop.Invocation;
import io.avaje.inject.aop.MethodInterceptor;

/**
 * Interceptor for @RequiresAuthentication annotation.
 * Blocks method execution if the user is not authenticated.
 */
public class AuthenticationInterceptor implements MethodInterceptor {

    @Override
    public void invoke(Invocation invocation) throws Throwable {
        // Check if user is authenticated
        if (!SecurityUtil.isAuthenticated()) {
            throw new AuthorizationException("Access denied: Authentication required");
        }
        
        // Proceed with method execution
        invocation.invoke();
    }
}
