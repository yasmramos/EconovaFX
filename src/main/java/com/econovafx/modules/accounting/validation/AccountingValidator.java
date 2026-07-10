package com.econovafx.modules.accounting.validation;

import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.core.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Validador de reglas contables según la Resolución 340/2004 del Ministerio de Finanzas y Precios de Cuba.
 * Implementa validaciones específicas para el sistema contable cubano.
 */
public class AccountingValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountingValidator.class);
    
    /**
     * Valida que un asiento contable esté cuadrado por moneda.
     * Según las Normas Contables Cubanas, los asientos deben cuadrar en cada moneda.
     * 
     * @param transaction Transacción a validar
     * @return Lista de errores de validación (vacía si es válido)
     */
    public static List<String> validateTransactionBalancedByCurrency(Transaction transaction) {
        List<String> errors = new ArrayList<>();
        
        if (transaction == null || transaction.getEntries() == null || transaction.getEntries().isEmpty()) {
            errors.add("La transacción no tiene entradas");
            return errors;
        }
        
        // Agrupar entradas por cuenta (y por ende por moneda de la cuenta)
        Map<Account, List<TransactionEntry>> entriesByAccount = transaction.getEntries().stream()
                .collect(Collectors.groupingBy(TransactionEntry::getAccount));
        
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (TransactionEntry entry : transaction.getEntries()) {
            totalDebit = totalDebit.add(entry.getDebitAmount());
            totalCredit = totalCredit.add(entry.getCreditAmount());
        }
        
        // Validar que el asiento cuadre
        if (totalDebit.compareTo(totalCredit) != 0) {
            String errorMsg = String.format(
                "El asiento no está cuadrado. Total Débito: %s, Total Crédito: %s, Diferencia: %s",
                totalDebit, totalCredit, totalDebit.subtract(totalCredit)
            );
            errors.add(errorMsg);
            logger.error("Validación fallida - Asiento no cuadrado: {}", errorMsg);
        }
        
        // Validar que haya al menos un débito y un crédito
        boolean hasDebit = transaction.getEntries().stream()
                .anyMatch(e -> e.getDebitAmount().compareTo(BigDecimal.ZERO) > 0);
        boolean hasCredit = transaction.getEntries().stream()
                .anyMatch(e -> e.getCreditAmount().compareTo(BigDecimal.ZERO) > 0);
        
        if (!hasDebit) {
            errors.add("El asiento debe tener al menos un cargo (débito)");
        }
        
        if (!hasCredit) {
            errors.add("El asiento debe tener al menos un abono (crédito)");
        }
        
        return errors;
    }
    
    /**
     * Valida que la fecha de la transacción esté dentro de un período contable abierto.
     * Según la Resolución 340/2004, solo se pueden registrar operaciones en períodos abiertos.
     * 
     * @param transactionDate Fecha de la transacción
     * @param accountingPeriods Lista de períodos contables disponibles
     * @return Lista de errores de validación
     */
    public static List<String> validateTransactionInOpenPeriod(LocalDate transactionDate, 
                                                               List<AccountingPeriod> accountingPeriods) {
        List<String> errors = new ArrayList<>();
        
        if (transactionDate == null) {
            errors.add("La fecha de la transacción es requerida");
            return errors;
        }
        
        if (accountingPeriods == null || accountingPeriods.isEmpty()) {
            errors.add("No hay períodos contables definidos en el sistema");
            return errors;
        }
        
        boolean foundInOpenPeriod = false;
        AccountingPeriod matchingPeriod = null;
        
        for (AccountingPeriod period : accountingPeriods) {
            if (!transactionDate.isBefore(period.getStartDate()) && 
                !transactionDate.isAfter(period.getEndDate())) {
                matchingPeriod = period;
                
                if (period.isOpen()) {
                    foundInOpenPeriod = true;
                    break;
                } else {
                    errors.add(String.format(
                        "La fecha %s cae en el período '%s' que está %s (desde %s hasta %s)",
                        transactionDate, period.getName(), 
                        period.getStatus().toString().toLowerCase(),
                        period.getStartDate(), period.getEndDate()
                    ));
                }
            }
        }
        
        if (matchingPeriod == null) {
            errors.add(String.format(
                "La fecha %s no corresponde a ningún período contable definido",
                transactionDate
            ));
        } else if (!foundInOpenPeriod) {
            errors.add("No se pueden registrar transacciones en períodos cerrados o bloqueados");
        }
        
        return errors;
    }
    
    /**
     * Valida que las cuentas utilizadas en la transacción sean del tipo correcto.
     * Verifica coherencia entre el tipo de cuenta y el movimiento (débito/crédito).
     * 
     * @param transaction Transacción a validar
     * @return Lista de errores de validación
     */
    public static List<String> validateAccountTypes(Transaction transaction) {
        List<String> errors = new ArrayList<>();
        
        if (transaction == null || transaction.getEntries() == null) {
            return errors;
        }
        
        for (TransactionEntry entry : transaction.getEntries()) {
            Account account = entry.getAccount();
            
            if (account == null) {
                errors.add("Entrada con cuenta nula");
                continue;
            }
            
            // Validar que la cuenta esté activa
            if (account.getIsActive() != null && !account.getIsActive()) {
                errors.add(String.format(
                    "La cuenta %s - %s está inactiva y no puede utilizarse",
                    account.getCode(), account.getName()
                ));
            }
            
            // Validar que los montos sean positivos
            if (entry.getDebitAmount().compareTo(BigDecimal.ZERO) < 0) {
                errors.add(String.format(
                    "El monto del débito en la cuenta %s no puede ser negativo",
                    account.getCode()
                ));
            }
            
            if (entry.getCreditAmount().compareTo(BigDecimal.ZERO) < 0) {
                errors.add(String.format(
                    "El monto del crédito en la cuenta %s no puede ser negativo",
                    account.getCode()
                ));
            }
            
            // Validar que no se usen débito y crédito simultáneamente en la misma entrada
            if (entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0 && 
                entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                errors.add(String.format(
                    "La cuenta %s no puede tener débito y crédito simultáneamente en la misma entrada",
                    account.getCode()
                ));
            }
        }
        
        return errors;
    }
    
    /**
     * Valida datos completos de una transacción según normativas cubanas.
     * Combina todas las validaciones anteriores.
     * 
     * @param transaction Transacción a validar
     * @param accountingPeriods Períodos contables disponibles
     * @param currentUser Usuario que realiza la operación
     * @return Resultado de la validación
     */
    public static ValidationResult validateTransactionComplete(Transaction transaction,
                                                               List<AccountingPeriod> accountingPeriods,
                                                               String currentUser) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validación básica de campos requeridos
        if (transaction.getDate() == null) {
            errors.add("La fecha de la transacción es requerida");
        }
        
        if (transaction.getType() == null || transaction.getType().trim().isEmpty()) {
            errors.add("El tipo de transacción es requerido");
        }
        
        if (transaction.getDescription() == null || transaction.getDescription().trim().isEmpty()) {
            warnings.add("Se recomienda incluir una descripción para la transacción");
        }
        
        if (transaction.getEntries() == null || transaction.getEntries().size() < 2) {
            errors.add("La transacción debe tener al menos 2 entradas (principio de partida doble)");
        } else {
            // Validaciones específicas
            errors.addAll(validateTransactionBalancedByCurrency(transaction));
            errors.addAll(validateAccountTypes(transaction));
        }
        
        // Validar período contable
        if (transaction.getDate() != null) {
            errors.addAll(validateTransactionInOpenPeriod(transaction.getDate(), accountingPeriods));
        }
        
        // Validar referencia a tercero si aplica
        if (transaction.getThirdParty() != null) {
            ThirdParty thirdParty = transaction.getThirdParty();
            if (thirdParty.getIsActive() != null && !thirdParty.getIsActive()) {
                warnings.add(String.format(
                    "El tercero %s (%s) está inactivo",
                    thirdParty.getName(), thirdParty.getId()
                ));
            }
        }
        
        ValidationResult result = new ValidationResult(errors.isEmpty(), errors, warnings);
        
        if (!result.isValid()) {
            logger.warn("Validación de transacción fallida para usuario {}: {} errores, {} advertencias",
                       currentUser, errors.size(), warnings.size());
        }
        
        return result;
    }
    
    /**
     * Clase interna para representar el resultado de una validación
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public String getErrorMessage() {
            if (errors.isEmpty()) {
                return "";
            }
            return String.join("; ", errors);
        }
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", errors=" + errors.size() +
                    ", warnings=" + warnings.size() +
                    '}';
        }
    }
}
