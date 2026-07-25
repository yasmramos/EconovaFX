package com.econovafx.modules.core.exception;

/**
 * Exception thrown when a requested entity is not found.
 * Used for consistent "not found" error handling across the application.
 */
public class EntityNotFoundException extends BusinessException {
    
    public EntityNotFoundException(Class<?> entityType, Long id) {
        super(
            "ENTITY_NOT_FOUND",
            String.format("%s not found with ID: %d", entityType.getSimpleName(), id)
        );
    }

    public EntityNotFoundException(Class<?> entityType, String fieldName, Object value) {
        super(
            "ENTITY_NOT_FOUND",
            String.format("%s not found with %s: %s", entityType.getSimpleName(), fieldName, value)
        );
    }

    public EntityNotFoundException(String message) {
        super("ENTITY_NOT_FOUND", message);
    }
}
