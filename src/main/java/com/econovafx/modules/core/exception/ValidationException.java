package com.econovafx.modules.core.exception;

import java.math.BigDecimal;

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
    
    /**
     * Creates an exception for unbalanced transactions
     */
    public static ValidationException unbalancedTransaction(BigDecimal debit, BigDecimal credit) {
        return new ValidationException(
            String.format("Transaction is not balanced. Debit: %s, Credit: %s", debit, credit)
        );
    }
    
    /**
     * Creates an exception when trying to post an already posted transaction
     */
    public static ValidationException transactionAlreadyPosted() {
        return new ValidationException("Transaction is already posted");
    }
    
    /**
     * Creates an exception when trying to reverse an unposted transaction
     */
    public static ValidationException cannotReverseUnpostedTransaction() {
        return new ValidationException("Cannot reverse unposted transaction");
    }
    
    /**
     * Creates an exception when trying to delete a posted transaction
     */
    public static ValidationException cannotDeletePostedTransaction() {
        return new ValidationException("Cannot delete posted transaction. Please reverse it.");
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
