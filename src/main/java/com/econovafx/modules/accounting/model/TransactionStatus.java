package com.econovafx.modules.accounting.model;

/**
 * Transaction status enumeration for accounting workflow
 */
public enum TransactionStatus {
    DRAFT("Borrador"),
    POSTED("Publicado"),
    REVERSED("Anulado");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if transaction can be modified in this status
     */
    public boolean canModify() {
        return this == DRAFT;
    }

    /**
     * Check if transaction can be posted in this status
     */
    public boolean canPost() {
        return this == DRAFT;
    }

    /**
     * Check if transaction can be reversed in this status
     */
    public boolean canReverse() {
        return this == POSTED;
    }
}
