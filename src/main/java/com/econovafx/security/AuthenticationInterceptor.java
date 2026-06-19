package com.econovafx.security;

import io.avaje.inject.aop.Aspect;
import io.avaje.inject.aop.Invocation;

/**
 * Interceptor for @RequiresAuthentication annotation.
 * Blocks method execution if the user is not authenticated.
 */
@Aspect
public class AuthenticationInterceptor {

    public Object intercept(Invocation invocation) throws Throwable {
        // Check if user is authenticated
        if (!SecurityUtil.isAuthenticated()) {
            throw new AuthorizationException("Access denied: Authentication required");
        }
        
        // Proceed with method execution
        return invocation.proceed();
    }
}
