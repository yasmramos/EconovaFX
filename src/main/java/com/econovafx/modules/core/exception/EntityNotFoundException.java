package com.econovafx.modules.core.exception;

/**
 * Exception thrown when a requested entity is not found.
 * Used for consistent "not found" error handling across the application.
 */
public class EntityNotFoundException extends BusinessException {
    
    private final Class<?> entityType;
    private final Object entityId;
    
    public EntityNotFoundException(Class<?> entityType, Long id) {
        super(
            "ENTITY_NOT_FOUND",
            String.format("%s not found with ID: %d", entityType.getSimpleName(), id)
        );
        this.entityType = entityType;
        this.entityId = id;
    }

    public EntityNotFoundException(Class<?> entityType, String fieldName, Object value) {
        super(
            "ENTITY_NOT_FOUND",
            String.format("%s not found with %s: %s", entityType.getSimpleName(), fieldName, value)
        );
        this.entityType = entityType;
        this.entityId = value;
    }

    public EntityNotFoundException(String message) {
        super("ENTITY_NOT_FOUND", message);
        this.entityType = null;
        this.entityId = null;
    }
    
    /**
     * Creates an exception for entity not found by ID
     */

    private static Class<?> getEntityClass(String entityType) {
        try {
            return Class.forName("com.econovafx.modules.accounting.model." + entityType);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName("com.econovafx.modules.core.model." + entityType);
            } catch (ClassNotFoundException ex) {
                return Object.class;
            }
        }
    }
    public static EntityNotFoundException notFound(String entityType, Long id) {
        return new EntityNotFoundException(getEntityClass(entityType), id);
    }
    
    public Class<?> getEntityType() {
        return entityType;
    }
    
    public Object getEntityId() {
        return entityId;
    }
}
