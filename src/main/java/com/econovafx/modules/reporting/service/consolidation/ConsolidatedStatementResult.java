package com.econovafx.modules.reporting.service.consolidation;

import com.econovafx.modules.accounting.model.FinancialStatementModel;
import com.econovafx.modules.accounting.service.FinancialStatementService.StatementRowResult;
import com.econovafx.modules.core.model.Company;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Result object for consolidated financial statement generation.
 * Contains aggregated financial data across multiple companies (tenants).
 * 
 * @author Development Team
 * @since 1.0.0
 */
public class ConsolidatedStatementResult {

    /**
     * The financial statement model used for consolidation.
     */
    private FinancialStatementModel model;

    /**
     * Start date of the consolidation period.
     */
    private LocalDate startDate;

    /**
     * End date of the consolidation period.
     */
    private LocalDate endDate;

    /**
     * List of companies included in the consolidation.
     */
    private List<Company> includedCompanies;

    /**
     * Consolidated rows with aggregated values.
     */
    private List<ConsolidatedRow> consolidatedRows;

    /**
     * Breakdown of individual company values per row for traceability.
     * Key: Company ID, Value: List of StatementRowResult for that company.
     */
    private Map<Long, List<StatementRowResult>> companyBreakdown;

    /**
     * Date when the consolidated statement was generated.
     */
    private LocalDate generatedAt;

    /**
     * Whether intercompany eliminations were applied to this consolidation.
     */
    private boolean eliminationsApplied;

    /**
     * List of intercompany eliminations applied (for audit trail).
     */
    private List<com.econovafx.modules.accounting.model.IntercompanyElimination> appliedEliminations;

    /**
     * Gets the financial statement model.
     * @return the model
     */
    public FinancialStatementModel getModel() {
        return model;
    }

    /**
     * Sets the financial statement model.
     * @param model the model to set
     */
    public void setModel(FinancialStatementModel model) {
        this.model = model;
    }

    /**
     * Gets the start date of the period.
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the period.
     * @param startDate the start date to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date of the period.
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the period.
     * @param endDate the end date to set
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets the list of included companies.
     * @return the list of companies
     */
    public List<Company> getIncludedCompanies() {
        return includedCompanies;
    }

    /**
     * Sets the list of included companies.
     * @param includedCompanies the list to set
     */
    public void setIncludedCompanies(List<Company> includedCompanies) {
        this.includedCompanies = includedCompanies;
    }

    /**
     * Gets the consolidated rows with aggregated values.
     * @return the list of consolidated rows
     */
    public List<ConsolidatedRow> getConsolidatedRows() {
        return consolidatedRows;
    }

    /**
     * Sets the consolidated rows.
     * @param consolidatedRows the list to set
     */
    public void setConsolidatedRows(List<ConsolidatedRow> consolidatedRows) {
        this.consolidatedRows = consolidatedRows;
    }

    /**
     * Gets the breakdown of values per company for traceability.
     * @return map of company ID to their statement rows
     */
    public Map<Long, List<StatementRowResult>> getCompanyBreakdown() {
        return companyBreakdown;
    }

    /**
     * Sets the company breakdown map.
     * @param companyBreakdown the map to set
     */
    public void setCompanyBreakdown(Map<Long, List<StatementRowResult>> companyBreakdown) {
        this.companyBreakdown = companyBreakdown;
    }

    /**
     * Gets the generation date.
     * @return the generated at date
     */
    public LocalDate getGeneratedAt() {
        return generatedAt;
    }

    /**
     * Sets the generation date.
     * @param generatedAt the date to set
     */
    public void setGeneratedAt(LocalDate generatedAt) {
        this.generatedAt = generatedAt;
    }

    /**
     * Checks whether intercompany eliminations were applied.
     * @return true if eliminations were applied
     */
    public boolean isEliminationsApplied() {
        return eliminationsApplied;
    }

    /**
     * Sets whether intercompany eliminations were applied.
     * @param eliminationsApplied true if eliminations should be applied
     */
    public void setEliminationsApplied(boolean eliminationsApplied) {
        this.eliminationsApplied = eliminationsApplied;
    }

    /**
     * Gets the list of applied intercompany eliminations (audit trail).
     * @return list of eliminations, or null if not tracked
     */
    public List<com.econovafx.modules.accounting.model.IntercompanyElimination> getAppliedEliminations() {
        return appliedEliminations;
    }

    /**
     * Sets the list of applied intercompany eliminations.
     * @param appliedEliminations list of eliminations for audit trail
     */
    public void setAppliedEliminations(List<com.econovafx.modules.accounting.model.IntercompanyElimination> appliedEliminations) {
        this.appliedEliminations = appliedEliminations;
    }

    @Override
    public String toString() {
        return "ConsolidatedStatementResult{" +
                "model=" + (model != null ? model.getName() : "null") +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", companiesCount=" + (includedCompanies != null ? includedCompanies.size() : 0) +
                ", consolidatedRowsCount=" + (consolidatedRows != null ? consolidatedRows.size() : 0) +
                ", eliminationsApplied=" + eliminationsApplied +
                '}';
    }
}
