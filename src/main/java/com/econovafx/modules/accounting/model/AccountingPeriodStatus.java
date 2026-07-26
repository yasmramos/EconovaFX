package com.econovafx.modules.accounting.model;

/**
 * Estados de un período contable según Resolución 340/2004
 */
public enum AccountingPeriodStatus {
    OPEN,      // Período abierto para registro de operaciones
    CLOSED,    // Período cerrado, no permite nuevos asientos
    LOCKED     // Período bloqueado definitivamente (auditado)
}
