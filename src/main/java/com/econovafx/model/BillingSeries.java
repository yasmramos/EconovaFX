package com.econovafx.model;

import jakarta.persistence.*;

/**
 * Control de series y folios de facturación.
 * Garantiza la numeración consecutiva estricta exigida por la Resolución 340/2004.
 */
@Entity
@Table(name = "billing_series")
public class BillingSeries extends BaseEntity {

    @Column(nullable = false, length = 10)
    private String seriesCode; // Ej: "A", "B", "001"

    @Column(nullable = false)
    private Integer currentNumber; // Próximo número a asignar

    @Column(nullable = false)
    private Integer startNumber = 1;

    @Column(nullable = false)
    private Integer endNumber; // Límite autorizado (si aplica)

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 100)
    private String description;

    @Column(name = "document_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    // Getters and Setters
    public String getSeriesCode() { return seriesCode; }
    public void setSeriesCode(String seriesCode) { this.seriesCode = seriesCode; }

    public Integer getCurrentNumber() { return currentNumber; }
    public void setCurrentNumber(Integer currentNumber) { this.currentNumber = currentNumber; }

    public Integer getStartNumber() { return startNumber; }
    public void setStartNumber(Integer startNumber) { this.startNumber = startNumber; }

    public Integer getEndNumber() { return endNumber; }
    public void setEndNumber(Integer endNumber) { this.endNumber = endNumber; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    /**
     * Tipos de documentos soportados.
     */
    public enum DocumentType {
        INVOICE("Factura"),
        CREDIT_NOTE("Nota de Crédito"),
        DEBIT_NOTE("Nota de Débito"),
        RECEIPT("Recibo"),
        TICKET("Boleta");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
