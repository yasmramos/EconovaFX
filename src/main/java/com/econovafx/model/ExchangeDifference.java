package com.econovafx.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro de Diferencia Cambiaria generada al pagar facturas en moneda extranjera.
 * Calcula la diferencia entre la tasa de cambio al momento de la factura vs la tasa al momento del pago.
 */
@Entity
@Table(name = "exchange_differences")
public class ExchangeDifference extends BaseEntity {

    @Column(nullable = false)
    private String documentType; // SALES_INVOICE, PURCHASE_INVOICE, etc.

    @Column(nullable = false)
    private Long documentId; // ID de la factura original

    @Column(nullable = false)
    private String documentNumber; // Número de la factura original

    @ManyToOne(optional = false)
    @JoinColumn(name = "third_party_id", nullable = false)
    private ThirdParty thirdParty; // Cliente o Proveedor

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency; // Moneda extranjera

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal originalExchangeRate; // Tasa al momento de la factura

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal paymentExchangeRate; // Tasa al momento del pago

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount; // Monto en moneda extranjera

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal localCurrencyAmountAtInvoice; // Monto en moneda local al momento de la factura

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal localCurrencyAmountAtPayment; // Monto en moneda local al momento del pago

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal differenceAmount; // Diferencia (positiva = ganancia, negativa = pérdida)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DifferenceType differenceType = DifferenceType.NONE; // GAIN, LOSS, NONE

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(name = "journal_entry_id")
    private Long journalEntryId; // Referencia al asiento contable generado por la diferencia

    @Column(length = 500)
    private String notes;

    // Getters and Setters
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public ThirdParty getThirdParty() { return thirdParty; }
    public void setThirdParty(ThirdParty thirdParty) { this.thirdParty = thirdParty; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public BigDecimal getOriginalExchangeRate() { return originalExchangeRate; }
    public void setOriginalExchangeRate(BigDecimal originalExchangeRate) { this.originalExchangeRate = originalExchangeRate; }

    public BigDecimal getPaymentExchangeRate() { return paymentExchangeRate; }
    public void setPaymentExchangeRate(BigDecimal paymentExchangeRate) { this.paymentExchangeRate = paymentExchangeRate; }

    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }

    public BigDecimal getLocalCurrencyAmountAtInvoice() { return localCurrencyAmountAtInvoice; }
    public void setLocalCurrencyAmountAtInvoice(BigDecimal localCurrencyAmountAtInvoice) { this.localCurrencyAmountAtInvoice = localCurrencyAmountAtInvoice; }

    public BigDecimal getLocalCurrencyAmountAtPayment() { return localCurrencyAmountAtPayment; }
    public void setLocalCurrencyAmountAtPayment(BigDecimal localCurrencyAmountAtPayment) { this.localCurrencyAmountAtPayment = localCurrencyAmountAtPayment; }

    public BigDecimal getDifferenceAmount() { return differenceAmount; }
    public void setDifferenceAmount(BigDecimal differenceAmount) { this.differenceAmount = differenceAmount; }

    public DifferenceType getDifferenceType() { return differenceType; }
    public void setDifferenceType(DifferenceType differenceType) { this.differenceType = differenceType; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public Long getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(Long journalEntryId) { this.journalEntryId = journalEntryId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public enum DifferenceType {
        GAIN,   // Ganancia cambiaria
        LOSS,   // Pérdida cambiaria
        NONE    // Sin diferencia
    }
}
