package com.econovafx.security;

/**
 * Exception thrown when authorization fails.
 * Similar to Apache Shiro's AuthorizationException.
 */
public class AuthorizationException extends RuntimeException {
    
    public AuthorizationException(String message) {
        super(message);
    }
    
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
