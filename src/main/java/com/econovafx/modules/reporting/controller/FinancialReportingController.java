package com.econovafx.modules.reporting.controller;

import com.econovafx.modules.reporting.model.FinancialReport;
import com.econovafx.modules.reporting.service.FinancialReportingService;
import com.econovafx.modules.reporting.service.consolidation.ConsolidatedStatementResult;
import com.econovafx.modules.reporting.service.consolidation.ConsolidationService;
import io.avaje.inject.Component;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Financial Reporting operations.
 * Provides methods to generate and manage financial reports required by Resolution 340/2004.
 */
@Component
public class FinancialReportingController {

    @Inject
    FinancialReportingService reportingService;

    @Inject
    ConsolidationService consolidationService;

    /**
     * Generate a Balance Sheet report.
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    public FinancialReport generateBalanceSheet(LocalDate startDate, LocalDate endDate, Integer fiscalYear) {
        return reportingService.generateBalanceSheet(startDate, endDate, fiscalYear);
    }

    /**
     * Generate an Income Statement report.
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    public FinancialReport generateIncomeStatement(LocalDate startDate, LocalDate endDate, Integer fiscalYear) {
        return reportingService.generateIncomeStatement(startDate, endDate, fiscalYear);
    }

    /**
     * Generate a Trial Balance report.
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    public FinancialReport generateTrialBalance(LocalDate endDate, Integer fiscalYear) {
        return reportingService.generateTrialBalance(endDate, fiscalYear);
    }

    /**
     * Generate a Cash Flow Statement report.
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    public FinancialReport generateCashFlowStatement(LocalDate startDate, LocalDate endDate, Integer fiscalYear) {
        return reportingService.generateCashFlowStatement(startDate, endDate, fiscalYear);
    }

    /**
     * Finalize a report.
     * @param id ID of the report to finalize
     * @return finalized report
     */
    public FinancialReport finalizeReport(Long id) {
        return reportingService.finalizeReport(id);
    }

    /**
     * Get all reports.
     * @return list of all reports
     */
    public List<FinancialReport> getAllReports() {
        return reportingService.getAllReports();
    }

    /**
     * Get report by ID.
     * @param id the report ID
     * @return optional containing the report
     */
    public java.util.Optional<FinancialReport> getReportById(Long id) {
        return reportingService.getReportById(id);
    }

    /**
     * Consolidate financial statements across multiple companies.
     * Implements requirement II.18 of Resolution 340/2004.
     * 
     * This method generates a consolidated financial statement by:
     * - Iterating through each specified company
     * - Switching tenant context to generate individual statements
     * - Aggregating values by row concept
     * - Restoring the original tenant context
     * 
     * @param companyIds List of company IDs to include in consolidation
     * @param modelId ID of the financial statement model to use
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return ConsolidatedStatementResult with aggregated financial data
     * @throws IllegalArgumentException if company IDs list is empty or model not found
     * @throws IllegalStateException if any company is not ACTIVE
     * @throws RuntimeException if error occurs during consolidation
     */
    public ConsolidatedStatementResult consolidate(List<Long> companyIds,
                                                    Long modelId,
                                                    LocalDate startDate,
                                                    LocalDate endDate) {
        return consolidate(companyIds, modelId, startDate, endDate, false);
    }

    /**
     * Consolidate financial statements across multiple companies with optional intercompany eliminations.
     * Implements requirement II.18 of Resolution 340/2004.
     * 
     * This method generates a consolidated financial statement by:
     * - Iterating through each specified company
     * - Switching tenant context to generate individual statements
     * - Aggregating values by row concept
     * - Applying intercompany eliminations if requested
     * - Restoring the original tenant context
     * 
     * @param companyIds List of company IDs to include in consolidation
     * @param modelId ID of the financial statement model to use
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @param applyEliminations Whether to apply intercompany elimination adjustments
     * @return ConsolidatedStatementResult with aggregated financial data
     * @throws IllegalArgumentException if company IDs list is empty or model not found
     * @throws IllegalStateException if any company is not ACTIVE
     * @throws RuntimeException if error occurs during consolidation
     */
    public ConsolidatedStatementResult consolidate(List<Long> companyIds,
                                                    Long modelId,
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    boolean applyEliminations) {
        return consolidationService.consolidateStatement(companyIds, modelId, startDate, endDate, applyEliminations);
    }
}
