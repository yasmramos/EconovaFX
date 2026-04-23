package com.econovafx.domain;

/**
 * Account type enumeration for accounting classification
 */
public enum AccountType {
    ASSET("Activo"),
    LIABILITY("Pasivo"),
    EQUITY("Patrimonio"),
    REVENUE("Ingreso"),
    EXPENSE("Gasto");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
