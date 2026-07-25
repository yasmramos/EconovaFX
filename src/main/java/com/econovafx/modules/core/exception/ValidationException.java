package com.econovafx.modules.core.exception;

/**
 * Exception thrown when validation fails for business rules.
 * Provides consistent validation error handling across the application.
 */
public class ValidationException extends BusinessException {
    
    private final String fieldName;
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.fieldName = null;
    }

    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", String.format("Field '%s': %s", field, message));
        this.fieldName = field;
    }

    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
        this.fieldName = null;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
