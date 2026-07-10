package com.econovafx.modules.billing.model;

import com.econovafx.modules.core.model.BaseEntity;
import com.econovafx.modules.core.model.Currency;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de Factura de Venta.
 * Integrada con inventario y contabilidad para cumplir Resolución 340/2004.
 */
@Entity
@Table(name = "sales_invoices")
public class SalesInvoice extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String invoiceNumber; // Formato: SERIE-NUMERO (ej: A-000123)

    @ManyToOne(optional = false)
    @JoinColumn(name = "billing_series_id", nullable = false)
    private BillingSeries billingSeries;

    @ManyToOne(optional = false)
    @JoinColumn(name = "third_party_id", nullable = false)
    private ThirdParty thirdParty; // Cliente

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @Column(precision = 18, scale = 2)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesInvoiceLine> lines = new ArrayList<>();

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    @Column(name = "journal_entry_id")
    private Long journalEntryId; // Referencia al asiento contable generado

    @Column(name = "warehouse_id")
    private Long warehouseId; // Almacén de origen

    // Getters and Setters
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public BillingSeries getBillingSeries() { return billingSeries; }
    public void setBillingSeries(BillingSeries billingSeries) { this.billingSeries = billingSeries; }

    public ThirdParty getThirdParty() { return thirdParty; }
    public void setThirdParty(ThirdParty thirdParty) { this.thirdParty = thirdParty; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public List<SalesInvoiceLine> getLines() { return lines; }
    public void setLines(List<SalesInvoiceLine> lines) { this.lines = lines; }
    public void addLine(SalesInvoiceLine line) {
        lines.add(line);
        line.setSalesInvoice(this);
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(Long journalEntryId) { this.journalEntryId = journalEntryId; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public enum InvoiceStatus {
        DRAFT,       // Borrador
        ISSUED,      // Emitida (numerada)
        SENT,        // Enviada al cliente
        PAID,        // Pagada
        CANCELLED,   // Cancelada
        VOID         // Anulada
    }
}
