package com.econovafx.modules.accounting.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an intercompany elimination entry for consolidated financial statements.
 * Used to eliminate reciprocal transactions between companies in the same group.
 * 
 * Resolution 340/2004 compliance: Intercompany transactions must be eliminated
 * to avoid double-counting in consolidated financial statements.
 * 
 * @author Development Team
 * @since 1.0.0
 */
public class IntercompanyElimination {

    /**
     * Unique identifier for this elimination entry
     */
    private Long id;

    /**
     * Type of elimination (RECEIVABLE_PAYABLE, REVENUE_EXPENSE, UNREALIZED_PROFIT)
     */
    private EliminationType type;

    /**
     * Description of the elimination
     */
    private String description;

    /**
     * Source company ID (the company that initiated the transaction)
     */
    private Long sourceCompanyId;

    /**
     * Target company ID (the company that received the transaction)
     */
    private Long targetCompanyId;

    /**
     * Account code being eliminated (e.g., intercompany receivable)
     */
    private String accountCode;

    /**
     * Counter account code for the elimination entry
     */
    private String counterAccountCode;

    /**
     * Amount to eliminate
     */
    private BigDecimal amount;

    /**
     * Currency code for the elimination amount
     */
    private String currencyCode;

    /**
     * Date of the original transaction being eliminated
     */
    private LocalDate transactionDate;

    /**
     * Reference to the original transaction ID
     */
    private Long originalTransactionId;

    /**
     * Whether this elimination has been posted to the consolidation adjustment journal
     */
    private boolean isPosted;

    /**
     * Date when the elimination was created
     */
    private LocalDate createdAt;

    /**
     * User ID who created the elimination
     */
    private Long createdByUserId;

    /**
     * Related elimination entries (for multi-leg eliminations)
     */
    private List<IntercompanyElimination> relatedEliminations = new ArrayList<>();

    public enum EliminationType {
        /**
         * Elimination of intercompany receivables and payables
         * Example: Company A owes Company B $1000
         */
        RECEIVABLE_PAYABLE("Eliminación Cuentas por Cobrar/Pagar Intercompañía"),

        /**
         * Elimination of intercompany revenues and expenses
         * Example: Company A recorded revenue from sale to Company B
         */
        REVENUE_EXPENSE("Eliminación Ingresos/Gastos Intercompañía"),

        /**
         * Elimination of unrealized profits from intercompany inventory transfers
         * Example: Company A sold inventory to Company B with profit, but B hasn't sold it externally
         */
        UNREALIZED_PROFIT("Eliminación Utilidades No Realizadas"),

        /**
         * Elimination of intercompany loans and interest
         */
        LOAN_INTEREST("Eliminación Préstamos e Intereses Intercompañía"),

        /**
         * Other types of intercompany eliminations
         */
        OTHER("Otras Eliminaciones");

        private final String displayName;

        EliminationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public IntercompanyElimination() {
    }

    /**
     * Creates a new intercompany elimination entry
     * 
     * @param type Type of elimination
     * @param description Description of what is being eliminated
     * @param sourceCompanyId ID of the source company
     * @param targetCompanyId ID of the target company
     * @param accountCode Account code being eliminated
     * @param counterAccountCode Counter account for the elimination entry
     * @param amount Amount to eliminate
     * @param transactionDate Date of the original transaction
     */
    public IntercompanyElimination(EliminationType type,
                                   String description,
                                   Long sourceCompanyId,
                                   Long targetCompanyId,
                                   String accountCode,
                                   String counterAccountCode,
                                   BigDecimal amount,
                                   LocalDate transactionDate) {
        this.type = type;
        this.description = description;
        this.sourceCompanyId = sourceCompanyId;
        this.targetCompanyId = targetCompanyId;
        this.accountCode = accountCode;
        this.counterAccountCode = counterAccountCode;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.isPosted = false;
        this.createdAt = LocalDate.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EliminationType getType() {
        return type;
    }

    public void setType(EliminationType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(Long sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    public Long getTargetCompanyId() {
        return targetCompanyId;
    }

    public void setTargetCompanyId(Long targetCompanyId) {
        this.targetCompanyId = targetCompanyId;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getCounterAccountCode() {
        return counterAccountCode;
    }

    public void setCounterAccountCode(String counterAccountCode) {
        this.counterAccountCode = counterAccountCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(Long originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public boolean isPosted() {
        return isPosted;
    }

    public void setPosted(boolean posted) {
        isPosted = posted;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public List<IntercompanyElimination> getRelatedEliminations() {
        return relatedEliminations;
    }

    public void setRelatedEliminations(List<IntercompanyElimination> relatedEliminations) {
        this.relatedEliminations = relatedEliminations;
    }

    public void addRelatedElimination(IntercompanyElimination elimination) {
        this.relatedEliminations.add(elimination);
    }

    /**
     * Checks if this elimination involves the given company
     * 
     * @param companyId The company ID to check
     * @return true if the company is either source or target
     */
    public boolean involvesCompany(Long companyId) {
        return sourceCompanyId.equals(companyId) || targetCompanyId.equals(companyId);
    }

    /**
     * Gets the other company involved in this elimination
     * 
     * @param companyId One of the companies involved
     * @return The other company ID, or null if companyId is not involved
     */
    public Long getOtherCompany(Long companyId) {
        if (sourceCompanyId.equals(companyId)) {
            return targetCompanyId;
        } else if (targetCompanyId.equals(companyId)) {
            return sourceCompanyId;
        }
        return null;
    }

    @Override
    public String toString() {
        return "IntercompanyElimination{" +
                "id=" + id +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", sourceCompanyId=" + sourceCompanyId +
                ", targetCompanyId=" + targetCompanyId +
                ", accountCode='" + accountCode + '\'' +
                ", amount=" + amount +
                '}';
    }
}
