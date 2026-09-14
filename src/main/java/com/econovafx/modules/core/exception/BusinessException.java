package com.econovafx.modules.core.exception;

/**
 * Base exception for all domain-specific exceptions in EconovaFX.
 * Provides a consistent hierarchy for error handling across the application.
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final Object[] args;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }
}
