package com.econovafx.modules.payables.model;

import com.econovafx.modules.core.model.BaseEntity;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.core.model.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;

/**
 * SupplierPayment - Represents a payment made to a supplier.
 * 
 * Implements Resolution 340/2004 requirements for the Payables Module:
 * - Payment registration with supplier identification
 * - Document reference
 * - Date tracking
 * - Total amount and partial payment balance
 * - Accounting classification
 * - Invoice allocation details
 * 
 * @author EconovaFX Development Team
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "supplier_payments")
public class SupplierPayment extends BaseEntity {

    /**
     * Payment document number
     */
    @Column(name = "payment_number", unique = true, nullable = false)
    private String paymentNumber;

    /**
     * Supplier who received the payment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private ThirdParty supplier;

    /**
     * Payment date
     */
    @Column(name = "payment_date", nullable = false, columnDefinition = "DATE")
    private LocalDate paymentDate;

    /**
     * Payment method
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    /**
     * Reference document number (check, transfer, etc.)
     */
    @Column(name = "reference_document", length = 100)
    private String referenceDocument;

    /**
     * Total payment amount
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    /**
     * Amount allocated to invoices
     */
    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal allocatedAmount;

    /**
     * Unallocated amount (advance payment)
     */
    @Column(name = "unallocated_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal unallocatedAmount;

    /**
     * Related accounting entry
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_entry_id")
    private Transaction accountingTransaction;

    /**
     * Payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    /**
     * Notes or observations
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Indicates if this is an advance payment
     */
    @Column(name = "is_advance_payment", nullable = false)
    private boolean advancePayment;

    /**
     * Currency of the payment (for foreign currency transactions)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    // Constructors
    public SupplierPayment() {
        this.allocatedAmount = BigDecimal.ZERO;
        this.unallocatedAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.status = PaymentStatus.PENDING;
        this.advancePayment = false;
    }

    // Getters and Setters
    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public ThirdParty getSupplier() {
        return supplier;
    }

    public void setSupplier(ThirdParty supplier) {
        this.supplier = supplier;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceDocument() {
        return referenceDocument;
    }

    public void setReferenceDocument(String referenceDocument) {
        this.referenceDocument = referenceDocument;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        updateUnallocatedAmount();
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
        updateUnallocatedAmount();
    }

    public BigDecimal getUnallocatedAmount() {
        return unallocatedAmount;
    }

    private void updateUnallocatedAmount() {
        if (this.totalAmount != null && this.allocatedAmount != null) {
            this.unallocatedAmount = this.totalAmount.subtract(this.allocatedAmount);
            this.advancePayment = this.unallocatedAmount.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    public Transaction getAccountingTransaction() {
        return accountingTransaction;
    }

    public void setAccountingTransaction(Transaction accountingTransaction) {
        this.accountingTransaction = accountingTransaction;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isAdvancePayment() {
        return advancePayment;
    }

    public void setAdvancePayment(boolean advancePayment) {
        this.advancePayment = advancePayment;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /**
     * Allocate payment to an invoice
     * @param invoice the invoice to allocate to
     * @param amount amount to allocate
     */
    public void allocateToInvoice(SupplierInvoice invoice, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Allocation amount must be greater than zero");
        }
        if (amount.compareTo(this.unallocatedAmount) > 0) {
            throw new IllegalArgumentException("Allocation amount exceeds unallocated balance");
        }
        
        invoice.applyPayment(amount);
        this.allocatedAmount = this.allocatedAmount.add(amount);
        updateUnallocatedAmount();
        
        if (this.unallocatedAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = PaymentStatus.FULLY_ALLOCATED;
        }
    }

    /**
     * Cancel/unallocate payment from an invoice
     * @param invoice the invoice to unallocate from
     * @param amount amount to unallocate
     */
    public void unallocateFromInvoice(SupplierInvoice invoice, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unallocation amount must be greater than zero");
        }
        if (amount.compareTo(this.allocatedAmount) > 0) {
            throw new IllegalArgumentException("Unallocation amount exceeds allocated balance");
        }
        
        // Reverse payment application (simplified - in real scenario would need more complex logic)
        this.allocatedAmount = this.allocatedAmount.subtract(amount);
        updateUnallocatedAmount();
        this.status = PaymentStatus.PENDING;
    }

    // Enums
    public enum PaymentMethod {
        CASH,
        BANK_TRANSFER,
        CHECK,
        CREDIT_CARD,
        DEBIT_CARD,
        PROMISSORY_NOTE,
        OTHER
    }

    public enum PaymentStatus {
        PENDING,
        PARTIALLY_ALLOCATED,
        FULLY_ALLOCATED,
        CANCELLED
    }
}
