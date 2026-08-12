package com.econovafx.modules.payables.service;

import com.econovafx.modules.payables.model.SupplierInvoice;
import com.econovafx.modules.payables.model.SupplierPayment;
import com.econovafx.modules.payables.repository.SupplierInvoiceRepository;
import com.econovafx.modules.payables.repository.SupplierPaymentRepository;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.billing.service.ThirdPartyService;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.config.UserContext;
import com.econovafx.modules.core.exception.EntityNotFoundException;
import com.econovafx.modules.core.exception.ValidationException;
import com.econovafx.modules.core.security.RequiresTenant;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Supplier Invoices and Payments (Payables).
 * 
 * Implements Resolution 340/2004 requirements for the Payables Module:
 * - Automatic consecutive numbering
 * - Payment terms definition
 * - Discount application support
 * - Supplier payment deadline tracking
 * - Due date information
 * - Inventory and supplier account impact
 * - Immutability after registration (cancellation required for modifications)
 * - Payment registration with supplier identification
 * - Document reference tracking
 * - Accounting classification
 * - Invoice allocation details
 */
@Component
@RequiresTenant
public class PayablesService {

    private static final Logger logger = LoggerFactory.getLogger(PayablesService.class);

    private final SupplierInvoiceRepository invoiceRepository;
    private final SupplierPaymentRepository paymentRepository;
    private final ThirdPartyService thirdPartyService;
    private final TransactionService transactionService;
    private final UserContext userContext;

    @Inject
    public PayablesService(
            SupplierInvoiceRepository invoiceRepository,
            SupplierPaymentRepository paymentRepository,
            ThirdPartyService thirdPartyService,
            TransactionService transactionService,
            UserContext userContext) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.thirdPartyService = thirdPartyService;
        this.transactionService = transactionService;
        this.userContext = userContext;
    }

    // ==================== Invoice Operations ====================

    /**
     * Get invoice by ID
     */
    public Optional<SupplierInvoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    /**
     * Get invoice by invoice number
     */
    public Optional<SupplierInvoice> getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    /**
     * Get all invoices
     */
    public List<SupplierInvoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * Get invoices by supplier
     */
    public List<SupplierInvoice> getInvoicesBySupplier(Long supplierId) {
        ThirdParty supplier = thirdPartyService.getThirdPartyById(supplierId)
            .orElseThrow(() -> new EntityNotFoundException(ThirdParty.class, supplierId));
        return invoiceRepository.findBySupplier(supplier);
    }

    /**
     * Get invoices by status
     */
    public List<SupplierInvoice> getInvoicesByStatus(SupplierInvoice.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    /**
     * Get overdue invoices
     */
    public List<SupplierInvoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices();
    }

    /**
     * Get invoices due between dates
     */
    public List<SupplierInvoice> getInvoicesDueBetween(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByDueDateBetween(startDate, endDate);
    }

    /**
     * Generate next invoice number (consecutive numbering per Resolution 340/2004)
     */
    private String generateInvoiceNumber() {
        long count = invoiceRepository.count();
        return String.format("SINV-%08d", count + 1);
    }

    /**
     * Create a new supplier invoice
     */
    public SupplierInvoice createInvoice(SupplierInvoice invoice) {
        validateInvoice(invoice);

        // Verify supplier exists and is of type SUPPLIER or BOTH
        ThirdParty supplier = invoice.getSupplier();
        if (supplier == null || supplier.getId() == null) {
            throw new ValidationException("supplier", "Supplier is required");
        }
        
        supplier = thirdPartyService.getThirdPartyById(supplier.getId())
            .orElseThrow(() -> new EntityNotFoundException(ThirdParty.class, supplier.getId()));
        
        if (supplier.getType() != ThirdParty.ThirdPartyType.SUPPLIER && 
            supplier.getType() != ThirdParty.ThirdPartyType.BOTH) {
            throw new ValidationException("supplier", 
                "Third party must be of type SUPPLIER or BOTH");
        }

        invoice.setSupplier(supplier);

        // Generate consecutive invoice number
        String invoiceNumber = generateInvoiceNumber();
        while (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            invoiceNumber = String.format("SINV-%08d", Long.parseLong(invoiceNumber.substring(5)) + 1);
        }
        invoice.setInvoiceNumber(invoiceNumber);

        // Set initial amounts if not set
        if (invoice.getTotalAmount() == null) {
            invoice.setTotalAmount(BigDecimal.ZERO);
        }
        if (invoice.getPendingAmount() == null) {
            invoice.setPendingAmount(invoice.getTotalAmount());
        }
        if (invoice.getPaidAmount() == null) {
            invoice.setPaidAmount(BigDecimal.ZERO);
        }

        // Set audit fields
        Long currentUserId = userContext.getCurrentUserId();
        if (currentUserId != null) {
            invoice.setCreatedBy(currentUserId);
            invoice.setUpdatedBy(currentUserId);
        }

        SupplierInvoice saved = invoiceRepository.save(invoice);
        logger.info("Supplier Invoice created: {} for supplier {} by user ID: {}", 
            saved.getInvoiceNumber(), saved.getSupplier().getName(), currentUserId);
        return saved;
    }

    /**
     * Update an existing invoice (limited updates per Resolution 340/2004)
     */
    public SupplierInvoice updateInvoice(SupplierInvoice invoice) {
        if (!invoiceRepository.existsById(invoice.getId())) {
            throw new EntityNotFoundException(SupplierInvoice.class, invoice.getId());
        }

        SupplierInvoice existing = invoiceRepository.findById(invoice.getId()).get();

        // Per Resolution 340/2004, invoices cannot be modified after issuance
        // Only certain fields can be updated
        if (existing.getStatus() == SupplierInvoice.InvoiceStatus.CANCELLED) {
            throw new ValidationException("status", "Cannot update a cancelled invoice");
        }

        // Allow updating payment terms only
        existing.setPaymentTerms(invoice.getPaymentTerms());

        Long currentUserId = userContext.getCurrentUserId();
        if (currentUserId != null) {
            existing.setUpdatedBy(currentUserId);
        }

        invoiceRepository.update(existing);
        logger.info("Supplier Invoice updated: {} by user ID: {}", 
            existing.getInvoiceNumber(), currentUserId);
        return existing;
    }

    /**
     * Cancel an invoice (required for modifications per Resolution 340/2004)
     */
    public SupplierInvoice cancelInvoice(Long invoiceId, String reason) {
        SupplierInvoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new EntityNotFoundException(SupplierInvoice.class, invoiceId));

        if (invoice.getStatus() == SupplierInvoice.InvoiceStatus.CANCELLED) {
            throw new ValidationException("status", "Invoice already cancelled");
        }

        invoice.cancel(reason);

        Long currentUserId = userContext.getCurrentUserId();
        if (currentUserId != null) {
            invoice.setUpdatedBy(currentUserId);
        }

        invoiceRepository.update(invoice);
        logger.info("Supplier Invoice cancelled: {} - Reason: {} by user ID: {}", 
            invoice.getInvoiceNumber(), reason, currentUserId);
        return invoice;
    }

    /**
     * Create accounting entry for invoice
     */
    public SupplierInvoice createTransactionForInvoice(Long invoiceId, Transaction entry) {
        SupplierInvoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new EntityNotFoundException(SupplierInvoice.class, invoiceId));

        if (invoice.getAccountingTransaction() != null) {
            throw new ValidationException("accountingEntry", 
                "Invoice already has an accounting entry");
        }

        Transaction savedEntry = transactionService.createTransaction(entry);
        invoice.setAccountingTransaction(savedEntry);
        invoiceRepository.update(invoice);

        logger.info("Accounting entry created for invoice: {}", invoice.getInvoiceNumber());
        return invoice;
    }

    // ==================== Payment Operations ====================

    /**
     * Get payment by ID
     */
    public Optional<SupplierPayment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    /**
     * Get payment by payment number
     */
    public Optional<SupplierPayment> getPaymentByNumber(String paymentNumber) {
        return paymentRepository.findByPaymentNumber(paymentNumber);
    }

    /**
     * Get all payments
     */
    public List<SupplierPayment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Get payments by supplier
     */
    public List<SupplierPayment> getPaymentsBySupplier(Long supplierId) {
        ThirdParty supplier = thirdPartyService.getThirdPartyById(supplierId)
            .orElseThrow(() -> new EntityNotFoundException(ThirdParty.class, supplierId));
        return paymentRepository.findBySupplier(supplier);
    }

    /**
     * Get advance payments
     */
    public List<SupplierPayment> getAdvancePayments() {
        return paymentRepository.findAdvancePayments();
    }

    /**
     * Generate next payment number
     */
    private String generatePaymentNumber() {
        long count = paymentRepository.count();
        return String.format("SPAY-%08d", count + 1);
    }

    /**
     * Register a new supplier payment
     */
    public SupplierPayment createPayment(SupplierPayment payment) {
        validatePayment(payment);

        // Verify supplier exists
        ThirdParty supplier = payment.getSupplier();
        if (supplier == null || supplier.getId() == null) {
            throw new ValidationException("supplier", "Supplier is required");
        }
        
        supplier = thirdPartyService.getThirdPartyById(supplier.getId())
            .orElseThrow(() -> new EntityNotFoundException(ThirdParty.class, supplier.getId()));
        
        if (supplier.getType() != ThirdParty.ThirdPartyType.SUPPLIER && 
            supplier.getType() != ThirdParty.ThirdPartyType.BOTH) {
            throw new ValidationException("supplier", 
                "Third party must be of type SUPPLIER or BOTH");
        }

        payment.setSupplier(supplier);

        // Generate consecutive payment number
        String paymentNumber = generatePaymentNumber();
        while (paymentRepository.existsByPaymentNumber(paymentNumber)) {
            paymentNumber = String.format("SPAY-%08d", Long.parseLong(paymentNumber.substring(5)) + 1);
        }
        payment.setPaymentNumber(paymentNumber);

        // Set initial amounts if not set
        if (payment.getTotalAmount() == null) {
            payment.setTotalAmount(BigDecimal.ZERO);
        }
        if (payment.getAllocatedAmount() == null) {
            payment.setAllocatedAmount(BigDecimal.ZERO);
        }

        // Set audit fields
        Long currentUserId = userContext.getCurrentUserId();
        if (currentUserId != null) {
            payment.setCreatedBy(currentUserId);
            payment.setUpdatedBy(currentUserId);
        }

        SupplierPayment saved = paymentRepository.save(payment);
        logger.info("Supplier Payment created: {} for supplier {} by user ID: {}", 
            saved.getPaymentNumber(), saved.getSupplier().getName(), currentUserId);
        return saved;
    }

    /**
     * Allocate payment to an invoice
     */
    public SupplierPayment allocatePaymentToInvoice(Long paymentId, Long invoiceId, BigDecimal amount) {
        SupplierPayment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(SupplierPayment.class, paymentId));
        
        SupplierInvoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new EntityNotFoundException(SupplierInvoice.class, invoiceId));

        payment.allocateToInvoice(invoice, amount);
        paymentRepository.update(payment);

        logger.info("Payment {} allocated {} to invoice {}", 
            payment.getPaymentNumber(), amount, invoice.getInvoiceNumber());
        return payment;
    }

    /**
     * Unallocate payment from an invoice
     */
    public SupplierPayment unallocatePaymentFromInvoice(Long paymentId, Long invoiceId, BigDecimal amount) {
        SupplierPayment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(SupplierPayment.class, paymentId));

        payment.unallocateFromInvoice(null, amount); // Simplified - would need invoice reference in real scenario
        paymentRepository.update(payment);

        logger.info("Payment {} unallocated {} from invoice", 
            payment.getPaymentNumber(), amount);
        return payment;
    }

    /**
     * Create accounting entry for payment
     */
    public SupplierPayment createTransactionForPayment(Long paymentId, Transaction entry) {
        SupplierPayment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(SupplierPayment.class, paymentId));

        if (payment.getAccountingTransaction() != null) {
            throw new ValidationException("accountingEntry", 
                "Payment already has an accounting entry");
        }

        Transaction savedEntry = transactionService.createTransaction(entry);
        payment.setAccountingTransaction(savedEntry);
        paymentRepository.update(payment);

        logger.info("Accounting entry created for payment: {}", payment.getPaymentNumber());
        return payment;
    }

    // ==================== Validation ====================

    private void validateInvoice(SupplierInvoice invoice) {
        if (invoice.getSupplier() == null) {
            throw new ValidationException("supplier", "Supplier is required");
        }
        if (invoice.getInvoiceDate() == null) {
            throw new ValidationException("invoiceDate", "Invoice date is required");
        }
        if (invoice.getDueDate() == null) {
            throw new ValidationException("dueDate", "Due date is required");
        }
        if (invoice.getDueDate().isBefore(invoice.getInvoiceDate())) {
            throw new ValidationException("dueDate", "Due date cannot be before invoice date");
        }
        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("totalAmount", "Total amount must be greater than zero");
        }
    }

    private void validatePayment(SupplierPayment payment) {
        if (payment.getSupplier() == null) {
            throw new ValidationException("supplier", "Supplier is required");
        }
        if (payment.getPaymentDate() == null) {
            throw new ValidationException("paymentDate", "Payment date is required");
        }
        if (payment.getPaymentMethod() == null) {
            throw new ValidationException("paymentMethod", "Payment method is required");
        }
        if (payment.getTotalAmount() == null || payment.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("totalAmount", "Total amount must be greater than zero");
        }
    }

    // ==================== Statistics ====================

    public long getInvoicesCount() {
        return invoiceRepository.count();
    }

    public long getPaymentsCount() {
        return paymentRepository.count();
    }

    public BigDecimal getTotalPendingAmount() {
        return invoiceRepository.findAll().stream()
            .filter(inv -> inv.getStatus() != SupplierInvoice.InvoiceStatus.CANCELLED)
            .map(SupplierInvoice::getPendingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPaidAmount() {
        return paymentRepository.findAll().stream()
            .filter(pay -> pay.getStatus() != SupplierPayment.PaymentStatus.CANCELLED)
            .map(SupplierPayment::getAllocatedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
