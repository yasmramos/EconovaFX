package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository;
import com.econovafx.modules.core.repository.ExchangeRateRepository;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.Currency;
import com.econovafx.modules.core.model.ExchangeRate;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.core.security.RequiresTenant;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para cálculo y gestión de diferencias cambiarias.
 * 
 * Calcula automáticamente la diferencia entre la tasa de cambio al momento de la factura
 * vs la tasa de cambio al momento del pago, generando el asiento contable correspondiente.
 */
@Component
@RequiresTenant
public class ExchangeDifferenceService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeDifferenceService.class);

    private final ExchangeDifferenceRepository exchangeDifferenceRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    @Inject
    public ExchangeDifferenceService(
            ExchangeDifferenceRepository exchangeDifferenceRepository,
            ExchangeRateRepository exchangeRateRepository,
            TransactionService transactionService,
            AccountRepository accountRepository,
            AuditService auditService) {
        this.exchangeDifferenceRepository = exchangeDifferenceRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    /**
     * Calcula y registra la diferencia cambiaria al pagar una factura en moneda extranjera.
     * 
     * @param documentType Tipo de documento (SALES_INVOICE, PURCHASE_INVOICE)
     * @param documentId ID del documento (factura)
     * @param documentNumber Número del documento
     * @param thirdParty Cliente o proveedor
     * @param currency Moneda extranjera
     * @param foreignAmount Monto en moneda extranjera que se está pagando
     * @param invoiceDate Fecha de la factura original
     * @param paymentDate Fecha del pago
     * @param username Usuario que realiza el pago
     * @return ExchangeDifference con el cálculo de la diferencia
     */
    public ExchangeDifference calculateAndRecordDifference(
            String documentType,
            Long documentId,
            String documentNumber,
            ThirdParty thirdParty,
            Currency currency,
            BigDecimal foreignAmount,
            LocalDate invoiceDate,
            LocalDate paymentDate,
            String username) {

        logger.info("Calculando diferencia cambiaria para documento: {} - {}", documentType, documentNumber);

        // 1. Obtener tasa de cambio histórica de la fecha de la factura
        BigDecimal originalExchangeRate = getHistoricalExchangeRate(currency, invoiceDate);
        
        // 2. Obtener tasa de cambio de la fecha del pago
        BigDecimal paymentExchangeRate = getHistoricalExchangeRate(currency, paymentDate);

        logger.debug("Tasa original ({}): {}, Tasa pago ({}): {}", 
            invoiceDate, originalExchangeRate, paymentDate, paymentExchangeRate);

        // 3. Calcular montos en moneda local
        BigDecimal localCurrencyAmountAtInvoice = foreignAmount.multiply(originalExchangeRate);
        BigDecimal localCurrencyAmountAtPayment = foreignAmount.multiply(paymentExchangeRate);

        // 4. Calcular diferencia
        BigDecimal differenceAmount = localCurrencyAmountAtPayment.subtract(localCurrencyAmountAtInvoice);
        
        // 5. Determinar tipo de diferencia
        ExchangeDifference.DifferenceType differenceType;
        if (differenceAmount.compareTo(BigDecimal.ZERO) > 0) {
            differenceType = ExchangeDifference.DifferenceType.GAIN; // Ganancia (pagamos menos en moneda local)
        } else if (differenceAmount.compareTo(BigDecimal.ZERO) < 0) {
            differenceType = ExchangeDifference.DifferenceType.LOSS; // Pérdida (pagamos más en moneda local)
        } else {
            differenceType = ExchangeDifference.DifferenceType.NONE;
        }

        // 6. Crear registro de diferencia cambiaria
        ExchangeDifference exchangeDifference = new ExchangeDifference();
        exchangeDifference.setDocumentType(documentType);
        exchangeDifference.setDocumentId(documentId);
        exchangeDifference.setDocumentNumber(documentNumber);
        exchangeDifference.setThirdParty(thirdParty);
        exchangeDifference.setCurrency(currency);
        exchangeDifference.setOriginalExchangeRate(originalExchangeRate);
        exchangeDifference.setPaymentExchangeRate(paymentExchangeRate);
        exchangeDifference.setOriginalAmount(foreignAmount);
        exchangeDifference.setLocalCurrencyAmountAtInvoice(localCurrencyAmountAtInvoice);
        exchangeDifference.setLocalCurrencyAmountAtPayment(localCurrencyAmountAtPayment);
        exchangeDifference.setDifferenceAmount(differenceAmount.abs()); // Guardar valor absoluto
        exchangeDifference.setDifferenceType(differenceType);
        exchangeDifference.setInvoiceDate(invoiceDate);
        exchangeDifference.setPaymentDate(paymentDate);
        exchangeDifference.setNotes(String.format(
            "Diferencia calculada: Tasa original=%s, Tasa pago=%s, Monto extranjero=%s",
            originalExchangeRate, paymentExchangeRate, foreignAmount));

        // 7. Guardar registro
        ExchangeDifference savedDifference = exchangeDifferenceRepository.save(exchangeDifference);
        logger.info("Diferencia cambiaria registrada: Tipo={}, Monto={}", differenceType, differenceAmount.abs());

        // 8. Generar asiento contable si hay diferencia significativa
        if (differenceType != ExchangeDifference.DifferenceType.NONE && 
            differenceAmount.abs().compareTo(BigDecimal.valueOf(0.01)) >= 0) {
            
            try {
                Long journalEntryId = createAccountingEntryForDifference(savedDifference, username).getId();
                savedDifference.setJournalEntryId(journalEntryId);
                exchangeDifferenceRepository.update(savedDifference);
                logger.info("Asiento contable generado para diferencia cambiaria: {}", journalEntryId);

                // Auditoría
                auditService.logWithValues(
                    username,
                    AuditLog.OperationType.CREATE,
                    "ExchangeDifference",
                    savedDifference.getId(),
                    "Diferencia cambiaria registrada: " + documentNumber,
                    null,
                    buildDifferenceJson(savedDifference)
                );

            } catch (Exception e) {
                logger.error("Error generando asiento contable para diferencia cambiaria: {}", e.getMessage());
                savedDifference.setNotes(savedDifference.getNotes() + " | Error en asiento: " + e.getMessage());
                exchangeDifferenceRepository.update(savedDifference);
                throw new RuntimeException("Error generando asiento contable: " + e.getMessage(), e);
            }
        }

        return savedDifference;
    }

    /**
     * Obtiene la tasa de cambio histórica para una moneda y fecha específicas.
     * Si no existe tasa para esa fecha exacta, busca la tasa más reciente anterior.
     */
    private BigDecimal getHistoricalExchangeRate(Currency currency, LocalDate date) {
        // Intentar obtener tasa exacta para la fecha
        return exchangeRateRepository.findByCurrencyAndDate(currency, date)
            .map(ExchangeRate::getRate)
            .orElseGet(() -> {
                // Si no existe, buscar la tasa más reciente anterior
                List<ExchangeRate> rates = exchangeRateRepository.findByCurrencyBeforeDate(currency, date);
                if (!rates.isEmpty()) {
                    return rates.get(0).getRate(); // Retorna la más reciente (ordenadas por fecha descendente)
                }
                // Si no hay tasas históricas, usar tasa actual
                return exchangeRateRepository.findCurrentByCurrency(currency)
                    .map(ExchangeRate::getRate)
                    .orElse(BigDecimal.ONE); // Default a 1 si no hay nada
            });
    }

    /**
     * Genera el asiento contable para la diferencia cambiaria.
     * 
     * Para GANANCIA cambiaria:
     *   DEBE: Cuenta de Diferencia Cambiaria (Ingreso)
     *   HABER: Cuentas por Cobrar/Pagar (ajuste)
     * 
     * Para PÉRDIDA cambiaria:
     *   DEBE: Cuentas por Cobrar/Pagar (ajuste)
     *   HABER: Cuenta de Diferencia Cambiaria (Gasto)
     */
    private Transaction createAccountingEntryForDifference(ExchangeDifference difference, String username) {
        BigDecimal differenceAmount = difference.getDifferenceAmount();
        ExchangeDifference.DifferenceType type = difference.getDifferenceType();

        // Obtener cuentas configurables
        String gainAccountCode = "499-001"; // Ingresos por diferencia cambiaria (configurable)
        String lossAccountCode = "599-001"; // Gastos por diferencia cambiaria (configurable)
        
        Account differenceAccount;
        if (type == ExchangeDifference.DifferenceType.GAIN) {
            differenceAccount = accountRepository.findByCode(gainAccountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Cuenta de ingresos por diferencia cambiaria no configurada: " + gainAccountCode));
        } else {
            differenceAccount = accountRepository.findByCode(lossAccountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Cuenta de gastos por diferencia cambiaria no configurada: " + lossAccountCode));
        }

        // Crear transacción
        Transaction transaction = new Transaction();
        transaction.setDate(difference.getPaymentDate());
        transaction.setType("EXCHANGE_DIFFERENCE");
        transaction.setDescription(String.format(
            "%s cambiaria por %s - Factura %s",
            type == ExchangeDifference.DifferenceType.GAIN ? "Ganancia" : "Pérdida",
            difference.getCurrency().getCode(),
            difference.getDocumentNumber()));
        transaction.setReference("DIFF-" + difference.getDocumentNumber());

        TransactionService.TransactionEntryData entry1, entry2;
        
        if (type == ExchangeDifference.DifferenceType.GAIN) {
            // Ganancia: DEBE diferencia cambiaria, HABER ajuste de cuenta
            entry1 = new TransactionService.TransactionEntryData(
                differenceAccount.getId(),
                differenceAmount,
                BigDecimal.ZERO,
                "Ganancia cambiaria " + difference.getDocumentNumber());
            
            entry2 = new TransactionService.TransactionEntryData(
                getCashOrPayableAccount(difference).getId(),
                BigDecimal.ZERO,
                differenceAmount,
                "Ajuste por ganancia cambiaria");
        } else {
            // Pérdida: DEBE ajuste de cuenta, HABER diferencia cambiaria
            entry1 = new TransactionService.TransactionEntryData(
                getCashOrPayableAccount(difference).getId(),
                differenceAmount,
                BigDecimal.ZERO,
                "Ajuste por pérdida cambiaria");
            
            entry2 = new TransactionService.TransactionEntryData(
                differenceAccount.getId(),
                BigDecimal.ZERO,
                differenceAmount,
                "Pérdida cambiaria " + difference.getDocumentNumber());
        }

        return transactionService.createTransaction(transaction, List.of(entry1, entry2), username);
    }

    /**
     * Obtiene la cuenta de efectivo o por pagar según el tipo de documento.
     */
    private Account getCashOrPayableAccount(ExchangeDifference difference) {
        // Esta lógica debe adaptarse según si es factura de venta o compra
        // Por ahora retorna una cuenta genérica configurable
        String accountCode = difference.getDocumentType().equals("SALES_INVOICE") 
            ? "106-001" // Cuentas por cobrar clientes
            : "201-001"; // Cuentas por pagar proveedores
        
        return accountRepository.findByCode(accountCode)
            .orElseThrow(() -> new IllegalArgumentException(
                "Cuenta configurada no encontrada: " + accountCode));
    }

    /**
     * Obtiene todas las diferencias cambiarias registradas.
     */
    public List<ExchangeDifference> getAllDifferences() {
        return exchangeDifferenceRepository.findAll();
    }

    /**
     * Obtiene diferencias cambiarias por documento.
     */
    public List<ExchangeDifference> getDifferencesByDocument(String documentType, Long documentId) {
        return exchangeDifferenceRepository.findByDocument(documentType, documentId);
    }

    /**
     * Obtiene el total de ganancias cambiarias en un período.
     */
    public BigDecimal getTotalGainsInPeriod(LocalDate startDate, LocalDate endDate) {
        return exchangeDifferenceRepository.getTotalGains(startDate, endDate);
    }

    /**
     * Obtiene el total de pérdidas cambiarias en un período.
     */
    public BigDecimal getTotalLossesInPeriod(LocalDate startDate, LocalDate endDate) {
        return exchangeDifferenceRepository.getTotalLosses(startDate, endDate);
    }

    /**
     * Construye JSON de la diferencia para auditoría.
     */
    private String buildDifferenceJson(ExchangeDifference diff) {
        if (diff == null) {
            return null;
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(diff.getId()).append(",");
        json.append("\"documentType\":\"").append(diff.getDocumentType()).append("\",");
        json.append("\"documentNumber\":\"").append(diff.getDocumentNumber()).append("\",");
        json.append("\"differenceType\":\"").append(diff.getDifferenceType()).append("\",");
        json.append("\"differenceAmount\":").append(diff.getDifferenceAmount());
        json.append("}");
        return json.toString();
    }
}
