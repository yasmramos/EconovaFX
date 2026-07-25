package com.econovafx.modules.core.exception;

/**
 * Global exception handler for UI layer
 * Centralizes error handling and display logic
 */
public class GlobalExceptionHandler {
    
    private static final String DEFAULT_ERROR_TITLE = "System Error";
    private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred. Please try again.";
    
    /**
     * Handle exception and return user-friendly message
     * @param exception The exception to handle
     * @return User-friendly error message
     */
    public static String handleException(Exception exception) {
        if (exception instanceof BusinessException businessException) {
            return formatBusinessException(businessException);
        } else if (exception instanceof ValidationException validationException) {
            return formatValidationException(validationException);
        } else if (exception instanceof EntityNotFoundException notFoundException) {
            return formatNotFoundException(notFoundException);
        } else {
            // Log the full stack trace for debugging
            System.err.println("Unexpected error: " + exception.getMessage());
            exception.printStackTrace();
            return DEFAULT_ERROR_MESSAGE;
        }
    }
    
    /**
     * Get error title based on exception type
     * @param exception The exception
     * @return Appropriate error title
     */
    public static String getErrorTitle(Exception exception) {
        if (exception instanceof BusinessException || exception instanceof ValidationException) {
            return "Business Rule Error";
        } else if (exception instanceof EntityNotFoundException) {
            return "Not Found";
        } else {
            return DEFAULT_ERROR_TITLE;
        }
    }
    
    private static String formatBusinessException(BusinessException ex) {
        StringBuilder message = new StringBuilder(ex.getMessage());
        if (ex.getErrorCode() != null && !ex.getErrorCode().isEmpty()) {
            message.append(" [Code: ").append(ex.getErrorCode()).append("]");
        }
        return message.toString();
    }
    
    private static String formatValidationException(ValidationException ex) {
        StringBuilder message = new StringBuilder("Validation failed: ");
        message.append(ex.getMessage());
        
        if (ex.getFieldName() != null && !ex.getFieldName().isEmpty()) {
            message.append(" (Field: ").append(ex.getFieldName()).append(")");
        }
        
        return message.toString();
    }
    
    private static String formatNotFoundException(EntityNotFoundException ex) {
        StringBuilder message = new StringBuilder("The requested ");
        message.append(ex.getEntityType() != null ? ex.getEntityType() : "resource");
        message.append(" was not found.");
        
        if (ex.getEntityId() != null) {
            message.append(" (ID: ").append(ex.getEntityId()).append(")");
        }
        
        return message.toString();
    }
}
