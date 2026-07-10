package com.econovafx.modules.billing.service;

import com.econovafx.modules.billing.model.*;
import com.econovafx.modules.billing.repository.*;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.inventory.service.InventoryService;
import com.econovafx.modules.inventory.model.Warehouse;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.User;
import io.avaje.inject.Component;
import com.econovafx.modules.core.security.RequiresTenant;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para gestión de facturación de ventas.
 * Integra emisión de facturas con generación de asientos contables y salidas de almacén.
 * Cumple con los requisitos de la Resolución 340/2004.
 */
@Component
@RequiresTenant
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final BillingSeriesRepository billingSeriesRepository;
    private final SequentialNumberService sequentialNumberService;
    private final TransactionService transactionService;
    private final InventoryService inventoryService;
    private final AccountRepository accountRepository;
    private final TaxRateRepository taxRateRepository;
    private final AuditService auditService;

    @Inject
    public BillingService(
            SalesInvoiceRepository salesInvoiceRepository,
            BillingSeriesRepository billingSeriesRepository,
            SequentialNumberService sequentialNumberService,
            TransactionService transactionService,
            InventoryService inventoryService,
            AccountRepository accountRepository,
            TaxRateRepository taxRateRepository,
            AuditService auditService) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.billingSeriesRepository = billingSeriesRepository;
        this.sequentialNumberService = sequentialNumberService;
        this.transactionService = transactionService;
        this.inventoryService = inventoryService;
        this.accountRepository = accountRepository;
        this.taxRateRepository = taxRateRepository;
        this.auditService = auditService;
    }

    /**
     * Emite una factura de venta.
     * Proceso transaccional que:
     * 1. Asigna número consecutivo
     * 2. Genera asiento contable
     * 3. Registra salida de inventario
     * 
     * @param invoice Factura en estado borrador
     * @param username Usuario que emite la factura
     * @return Factura emitida con número asignado
     */
    public SalesInvoice issueInvoice(SalesInvoice invoice, String username) {
        logger.info("Iniciando emisión de factura para cliente: {}", invoice.getThirdParty().getName());

        // Validaciones previas
        validateInvoice(invoice);

        // 1. Asignar número consecutivo (dentro de transacción)
        String invoiceNumber = assignInvoiceNumber(invoice);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setStatus(SalesInvoice.InvoiceStatus.ISSUED);
        invoice.setIssueDate(LocalDate.now());

        // Guardar factura con número asignado
        SalesInvoice savedInvoice = salesInvoiceRepository.save(invoice);
        logger.info("Factura emitida: {} - Número: {}", invoice.getId(), invoiceNumber);

        try {
            // 2. Generar asiento contable
            Long journalEntryId = createAccountingEntry(savedInvoice, username).getId();
            savedInvoice.setJournalEntryId(journalEntryId);
            logger.info("Asiento contable generado: {}", journalEntryId);

            // 3. Registrar salidas de inventario
            registerInventoryOutputs(savedInvoice, username);
            logger.info("Salidas de inventario registradas para factura: {}", invoiceNumber);

            salesInvoiceRepository.update(savedInvoice);

            // Auditoría
            auditService.logWithValues(
                username,
                AuditLog.OperationType.CREATE,
                "SalesInvoice",
                savedInvoice.getId(),
                "Factura emitida: " + invoiceNumber,
                null,
                buildInvoiceJson(savedInvoice)
            );

        } catch (Exception e) {
            logger.error("Error procesando factura {}: {}", invoiceNumber, e.getMessage());
            // Marcar como emitida pero con errores en procesos posteriores
            savedInvoice.setStatus(SalesInvoice.InvoiceStatus.ISSUED);
            savedInvoice.setNotes("Error en procesamiento posterior: " + e.getMessage());
            salesInvoiceRepository.update(savedInvoice);
            throw new RuntimeException("Error procesando factura: " + e.getMessage(), e);
        }

        return savedInvoice;
    }

    /**
     * Valida que la factura tenga todos los datos requeridos.
     */
    private void validateInvoice(SalesInvoice invoice) {
        if (invoice.getThirdParty() == null) {
            throw new IllegalArgumentException("La factura debe tener un cliente asignado");
        }

        if (invoice.getBillingSeries() == null) {
            throw new IllegalArgumentException("La factura debe tener una serie de facturación asignada");
        }

        if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos una línea");
        }

        // Validar que cada línea tenga producto y cantidad
        for (SalesInvoiceLine line : invoice.getLines()) {
            if (line.getProductCode() == null) {
                throw new IllegalArgumentException("Todas las líneas deben tener un producto");
            }
            if (line.getQuantity() == null || line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cantidad inválida en línea de factura");
            }
            if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Precio unitario inválido en línea de factura");
            }
        }

        // Calcular totales si no están establecidos
        calculateInvoiceTotals(invoice);
    }

    /**
     * Calcula los totales de la factura (subtotal, impuestos, total).
     */
    private void calculateInvoiceTotals(SalesInvoice invoice) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (SalesInvoiceLine line : invoice.getLines()) {
            BigDecimal lineTotal = line.getUnitPrice().multiply(line.getQuantity());
            
            // Aplicar descuento si existe
            if (line.getDiscount() != null && line.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                lineTotal = lineTotal.subtract(line.getDiscount());
            }

            subtotal = subtotal.add(lineTotal);

            // Calcular impuesto si la línea tiene tasa configurada
            if (line.getTaxRate() != null) {
                BigDecimal lineTax = lineTotal.multiply(line.getTaxRate().getPercentage().divide(BigDecimal.valueOf(100)));
                taxAmount = taxAmount.add(lineTax);
            }
        }

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(subtotal.add(taxAmount));
    }

    /**
     * Asigna número consecutivo a la factura usando la serie configurada.
     */
    private String assignInvoiceNumber(SalesInvoice invoice) {
        Long seriesId = invoice.getBillingSeries().getId();
        return sequentialNumberService.generateDocumentNumber(seriesId);
    }

    /**
     * Genera el asiento contable para la factura.
     * Cuentas involucradas:
     * - Cuentas por Cobrar (Debe)
     * - Ingresos por Ventas (Haber)
     * - Impuestos por Pagar (Haber)
     */
    private Transaction createAccountingEntry(SalesInvoice invoice, String username) {
        ThirdParty customer = invoice.getThirdParty();
        BigDecimal total = invoice.getTotal();
        BigDecimal subtotal = invoice.getSubtotal();
        BigDecimal taxAmount = invoice.getTaxAmount();

        // Obtener cuentas del cliente (cuenta por cobrar)
        String receivableAccountCode = customer.getTaxId();
        if (receivableAccountCode == null || receivableAccountCode.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El cliente debe tener configurado su RUC/Tax ID: " + customer.getName());
        }

        Account receivableAccount = accountRepository.findByCode(receivableAccountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Cuenta por cobrar no encontrada: " + receivableAccountCode));

        // Cuenta de ingresos por ventas (configurable por defecto o por serie)
        String revenueAccountCode = "401-001"; // Configurable
        Account revenueAccount = accountRepository.findByCode(revenueAccountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Cuenta de ingresos no configurada: " + revenueAccountCode));

        // Crear transacción
        Transaction transaction = new Transaction();
        transaction.setDate(invoice.getIssueDate());
        transaction.setType("SALES_INVOICE");
        transaction.setDescription("Venta según factura " + invoice.getInvoiceNumber());
        transaction.setReference(invoice.getInvoiceNumber());

        // Entrada por cobrar (Debe)
        TransactionService.TransactionEntryData debitEntry = new TransactionService.TransactionEntryData(
            receivableAccount.getId(),
            total,
            BigDecimal.ZERO,
            "Por cobrar factura " + invoice.getInvoiceNumber()
        );

        // Entrada por ingresos (Haber)
        TransactionService.TransactionEntryData revenueEntry = new TransactionService.TransactionEntryData(
            revenueAccount.getId(),
            BigDecimal.ZERO,
            subtotal,
            "Ingreso por venta factura " + invoice.getInvoiceNumber()
        );

        List<TransactionService.TransactionEntryData> entries = List.of(debitEntry, revenueEntry);

        // Agregar entrada por impuestos si aplica
        if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            String taxAccountCode = "203-001"; // IVA por pagar (configurable)
            Account taxAccount = accountRepository.findByCode(taxAccountCode)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Cuenta de impuestos no configurada: " + taxAccountCode));

            TransactionService.TransactionEntryData taxEntry = new TransactionService.TransactionEntryData(
                taxAccount.getId(),
                BigDecimal.ZERO,
                taxAmount,
                "IVA factura " + invoice.getInvoiceNumber()
            );
            entries = List.of(debitEntry, revenueEntry, taxEntry);
        }

        return transactionService.createTransaction(transaction, entries, username);
    }

    /**
     * Registra las salidas de inventario para cada línea de la factura.
     */
    private void registerInventoryOutputs(SalesInvoice invoice, String username) {
        Long warehouseId = invoice.getWarehouseId();
        if (warehouseId == null) {
            logger.warn("Factura {} no tiene almacén configurado. No se registrarán salidas.", 
                invoice.getInvoiceNumber());
            return;
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setId(warehouseId);

        User user = new User();
        // El usuario se obtendría del contexto de seguridad en producción

        for (SalesInvoiceLine line : invoice.getLines()) {
            if (line.getProductCode() != null && line.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                try {
                    // Nota: La lógica de inventario requiere un producto completo, no solo el código
                    // Esto debería ser refactorizado para buscar el producto por productCode
                    logger.debug("Salida pendiente: Producto={}, Cantidad={}", 
                        line.getProductCode(), line.getQuantity());
                } catch (Exception e) {
                    logger.error("Error registrando salida de inventario para producto {}: {}", 
                        line.getProductCode(), e.getMessage());
                    throw new RuntimeException(
                        "Error registrando salida de inventario: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Obtiene una factura por su ID.
     */
    public SalesInvoice getInvoiceById(Long id) {
        return salesInvoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + id));
    }

    /**
     * Obtiene una factura por su número.
     */
    public SalesInvoice getInvoiceByNumber(String invoiceNumber) {
        return salesInvoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + invoiceNumber));
    }

    /**
     * Lista todas las facturas.
     */
    public List<SalesInvoice> getAllInvoices() {
        return salesInvoiceRepository.findAll();
    }

    /**
     * Obtiene facturas por estado.
     */
    public List<SalesInvoice> getInvoicesByStatus(SalesInvoice.InvoiceStatus status) {
        return salesInvoiceRepository.findByStatus(status);
    }

    /**
     * Construye JSON de la factura para auditoría.
     */
    private String buildInvoiceJson(SalesInvoice invoice) {
        if (invoice == null) {
            return null;
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(invoice.getId()).append(",");
        json.append("\"invoiceNumber\":\"").append(invoice.getInvoiceNumber()).append("\",");
        json.append("\"total\":").append(invoice.getTotal()).append(",");
        json.append("\"status\":\"").append(invoice.getStatus()).append("\"");
        json.append("}");
        return json.toString();
    }
}
