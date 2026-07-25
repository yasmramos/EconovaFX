package com.econovafx.modules.core.exception;

/**
 * Exception thrown when validation fails for business rules.
 * Provides consistent validation error handling across the application.
 */
public class ValidationException extends BusinessException {
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", String.format("Field '%s': %s", field, message));
    }

    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
    }
}
