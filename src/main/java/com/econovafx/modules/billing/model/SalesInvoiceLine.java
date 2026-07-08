package com.econovafx.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Línea de detalle de Factura de Venta.
 * Incluye cálculo de impuestos por línea según Resolución 340/2004.
 */
@Entity
@Table(name = "sales_invoice_lines")
public class SalesInvoiceLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;

    @Column(nullable = false)
    private Integer lineNumber;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private InventoryItem item;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 4)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "tax_rate_id")
    private TaxRate taxRate;

    @Column(precision = 18, scale = 4)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "warehouse_out_id")
    private Long warehouseOutId; // Referencia a salida de almacén generada

    // Getters and Setters
    public SalesInvoice getSalesInvoice() { return salesInvoice; }
    public void setSalesInvoice(SalesInvoice salesInvoice) { this.salesInvoice = salesInvoice; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public InventoryItem getItem() { return item; }
    public void setItem(InventoryItem item) { this.item = item; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public TaxRate getTaxRate() { return taxRate; }
    public void setTaxRate(TaxRate taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Long getWarehouseOutId() { return warehouseOutId; }
    public void setWarehouseOutId(Long warehouseOutId) { this.warehouseOutId = warehouseOutId; }
}
