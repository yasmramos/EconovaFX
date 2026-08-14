package com.econovafx.modules.reporting.service.consolidation;

import com.econovafx.modules.accounting.model.FinancialStatementModel;
import com.econovafx.modules.accounting.model.IntercompanyElimination;
import com.econovafx.modules.accounting.service.FinancialStatementService;
import com.econovafx.modules.accounting.service.FinancialStatementService.StatementRowResult;
import com.econovafx.modules.accounting.service.IntercompanyEliminationService;
import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.service.CompanyService;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for consolidating financial statements across multiple companies (tenants).
 * Implements requirement II.18 of Resolution 340/2004.
 * 
 * This service orchestrates multi-tenant operations by:
 * 1. Saving the current tenant context
 * 2. Iterating through each company, switching tenant context
 * 3. Generating individual financial statements
 * 4. Aggregating results by row identity
 * 5. Applying intercompany eliminations (when enabled)
 * 6. Restoring the original tenant context
 * 
 * @author Development Team
 * @since 1.0.0
 */
@Component
public class ConsolidationService {

    private static final Logger logger = LoggerFactory.getLogger(ConsolidationService.class);

    private final CompanyService companyService;
    private final FinancialStatementService financialStatementService;
    private final IntercompanyEliminationService intercompanyEliminationService;

    @Inject
    public ConsolidationService(CompanyService companyService,
                                FinancialStatementService financialStatementService,
                                IntercompanyEliminationService intercompanyEliminationService) {
        this.companyService = companyService;
        this.financialStatementService = financialStatementService;
        this.intercompanyEliminationService = intercompanyEliminationService;
    }

    /**
     * Consolidates financial statements across multiple companies with optional intercompany eliminations.
     * 
     * This overloaded method provides full control over whether to apply intercompany eliminations.
     * 
     * @param companyIds List of company IDs to consolidate
     * @param modelId ID of the financial statement model to use
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @param applyEliminations Whether to apply intercompany elimination adjustments
     * @return ConsolidatedStatementResult containing aggregated financial data
     * @throws IllegalArgumentException if no company IDs provided or model not found
     * @throws IllegalStateException if any company is not ACTIVE
     * @throws RuntimeException if error occurs during consolidation for any company
     */
    public ConsolidatedStatementResult consolidateStatement(List<Long> companyIds,
                                                            Long modelId,
                                                            LocalDate startDate,
                                                            LocalDate endDate,
                                                            boolean applyEliminations) {
        if (companyIds == null || companyIds.isEmpty()) {
            throw new IllegalArgumentException("Company IDs list cannot be empty");
        }

        // Save original tenant context for restoration
        Company originalTenant = TenantContext.getCurrentTenant();
        boolean hadOriginalTenant = (originalTenant != null);

        logger.info("Starting financial statement consolidation for {} companies, model {}, eliminations={}", 
                    companyIds.size(), modelId, applyEliminations);

        try {
            // Fetch and validate all companies first
            List<Company> companies = new ArrayList<>();
            for (Long companyId : companyIds) {
                Company company = companyService.findById(companyId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Company not found with ID: " + companyId));
                
                if (!"ACTIVE".equals(company.getStatus())) {
                    throw new IllegalStateException(
                            "Company '" + company.getName() + "' (ID: " + companyId + ") is not ACTIVE. Status: " + company.getStatus());
                }
                
                companies.add(company);
            }

            // Map to store consolidated rows by row number for aggregation
            Map<Integer, ConsolidatedRowData> consolidatedRowsMap = new HashMap<>();
            
            // Map to store breakdown per company for traceability
            Map<Long, List<StatementRowResult>> companyBreakdown = new HashMap<>();

            // Process each company
            for (Company company : companies) {
                logger.debug("Processing company: {} ({})", company.getName(), company.getCode());

                try {
                    // Switch to this company's tenant context
                    companyService.selectTenant(company);

                    // Generate statement for this company
                    FinancialStatementService.FinancialStatementResult individualResult = 
                            financialStatementService.generateStatement(modelId, startDate, endDate);

                    // Store breakdown for traceability
                    companyBreakdown.put(company.getId(), individualResult.getRows());

                    // Aggregate rows by row number
                    aggregateRows(individualResult.getRows(), consolidatedRowsMap, company);

                    logger.info("Successfully processed company: {} - {} rows", 
                                company.getName(), individualResult.getRows().size());

                } catch (Exception e) {
                    logger.error("Error processing company: {} ({}) - Error: {}", 
                                 company.getName(), company.getCode(), e.getMessage());
                    // Re-throw to fail fast and let caller handle
                    throw new RuntimeException(
                            "Failed to generate statement for company: " + company.getName() + 
                            " - " + e.getMessage(), e);
                }
            }

            // Convert map to sorted list of consolidated rows
            List<ConsolidatedRow> consolidatedRows = consolidatedRowsMap.entrySet().stream()
                    .map(entry -> {
                        ConsolidatedRowData data = entry.getValue();
                        return new ConsolidatedRow(
                                data.rowNumber,
                                data.label,
                                data.rowType,
                                data.consolidatedValue,
                                data.indentLevel,
                                data.isBold,
                                data.isItalic,
                                data.companyValues // Breakdown per company
                        );
                    })
                    .sorted((r1, r2) -> r1.getRowNumber().compareTo(r2.getRowNumber()))
                    .collect(Collectors.toList());

            // Apply intercompany eliminations if requested
            if (applyEliminations) {
                logger.info("Applying intercompany eliminations...");
                
                // Identify intercompany transactions
                List<IntercompanyElimination> eliminations = 
                        intercompanyEliminationService.identifyIntercompanyTransactions(
                                companies, startDate, endDate);
                
                // Apply eliminations to consolidated rows
                consolidatedRows = intercompanyEliminationService.applyEliminations(
                        consolidatedRows, companyBreakdown, eliminations);
                
                // Validate eliminations are balanced
                if (!intercompanyEliminationService.validateEliminationsBalanced(eliminations)) {
                    logger.warn("Intercompany eliminations are not balanced! Review required.");
                }
                
                logger.info("Applied {} intercompany eliminations", eliminations.size());
            }

            // Get the model from the first company (all should have same model)
            FinancialStatementModel model = null;
            if (!companies.isEmpty()) {
                Company firstCompany = companies.get(0);
                companyService.selectTenant(firstCompany);
                try {
                    FinancialStatementService.FinancialStatementResult tempResult = 
                            financialStatementService.generateStatement(modelId, startDate, endDate);
                    model = tempResult.getModel();
                } finally {
                    // Context will be restored in outer finally
                }
            }

            ConsolidatedStatementResult result = new ConsolidatedStatementResult();
            result.setModel(model);
            result.setStartDate(startDate);
            result.setEndDate(endDate);
            result.setIncludedCompanies(companies);
            result.setConsolidatedRows(consolidatedRows);
            result.setCompanyBreakdown(companyBreakdown);
            result.setGeneratedAt(LocalDate.now());
            result.setEliminationsApplied(applyEliminations);

            logger.info("Consolidation completed successfully: {} companies, {} consolidated rows, eliminations={}", 
                        companies.size(), consolidatedRows.size(), applyEliminations);

            return result;

        } finally {
            // Always restore original tenant context
            if (hadOriginalTenant && originalTenant != null) {
                TenantContext.setCurrentTenant(originalTenant);
                logger.debug("Restored original tenant: {}", originalTenant.getCode());
            } else {
                TenantContext.clear();
                logger.debug("Cleared tenant context (no original tenant)");
            }
        }
    }

    /**
     * Aggregates statement rows into the consolidated map.
     * 
     * Rows are matched by rowNumber for aggregation. If a row number appears
     * in multiple companies, their values are summed.
     * 
     * @param rows Individual statement rows to aggregate
     * @param consolidatedRowsMap Map storing consolidated data by row number
     * @param company The company these rows belong to (for breakdown tracking)
     */
    private void aggregateRows(List<StatementRowResult> rows,
                               Map<Integer, ConsolidatedRowData> consolidatedRowsMap,
                               Company company) {
        for (StatementRowResult row : rows) {
            Integer rowNumber = calculateRowNumber(row);
            
            ConsolidatedRowData existingData = consolidatedRowsMap.get(rowNumber);
            
            if (existingData == null) {
                // First occurrence of this row number
                existingData = new ConsolidatedRowData();
                existingData.rowNumber = rowNumber;
                existingData.label = row.getLabel();
                existingData.rowType = row.getRowType();
                existingData.indentLevel = row.getIndentLevel();
                existingData.isBold = row.getIsBold();
                existingData.isItalic = row.getIsItalic();
                existingData.consolidatedValue = BigDecimal.ZERO;
                existingData.companyValues = new HashMap<>();
                
                consolidatedRowsMap.put(rowNumber, existingData);
            }
            
            // Sum the value
            BigDecimal value = row.getValue() != null ? row.getValue() : BigDecimal.ZERO;
            existingData.consolidatedValue = existingData.consolidatedValue.add(value);
            
            // Store individual company value for breakdown
            existingData.companyValues.put(company.getId(), value);
        }
    }

    /**
     * Calculates a unique row number for aggregation purposes.
     * Uses label hash if row number is not available.
     * 
     * @param row The statement row
     * @return A unique identifier for this row concept
     */
    private Integer calculateRowNumber(StatementRowResult row) {
        // In a real implementation, FinancialStatementRow would have a proper rowNumber
        // For now, we use label-based identification as fallback
        // TODO: Enhance when FinancialStatementRow includes explicit rowNumber
        if (row.getLabel() != null) {
            return row.getLabel().hashCode();
        }
        return System.identityHashCode(row);
    }

    /**
     * Applies intercompany eliminations using the IntercompanyEliminationService.
     * 
     * This method delegates to the specialized service for identifying and applying
     * eliminations of intercompany transactions to avoid double-counting.
     * 
     * @param consolidatedRows The consolidated rows before eliminations
     * @param companyBreakdown Breakdown of values per company
     * @param companies List of companies in the consolidation
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return Rows after applying intercompany eliminations
     * 
     * Resolution 340/2004 compliance:
     * - Identifies reciprocal accounts between companies (intercompany receivables/payables)
     * - Eliminates intercompany revenues and expenses
     * - Removes unrealized profits from intercompany inventory transfers
     * - Generates elimination journal entries for audit trail
     */
    protected List<ConsolidatedRow> applyIntercompanyEliminations(
            List<ConsolidatedRow> consolidatedRows,
            Map<Long, List<StatementRowResult>> companyBreakdown,
            List<Company> companies,
            LocalDate startDate,
            LocalDate endDate) {
        
        logger.info("Applying intercompany eliminations for {} companies", companies.size());
        
        // Identify intercompany transactions
        List<IntercompanyElimination> eliminations = 
                intercompanyEliminationService.identifyIntercompanyTransactions(
                        companies, startDate, endDate);
        
        if (eliminations.isEmpty()) {
            logger.debug("No intercompany transactions identified for elimination");
            return consolidatedRows;
        }
        
        // Apply eliminations to consolidated rows
        List<ConsolidatedRow> adjustedRows = intercompanyEliminationService.applyEliminations(
                consolidatedRows, companyBreakdown, eliminations);
        
        // Validate eliminations are balanced
        boolean balanced = intercompanyEliminationService.validateEliminationsBalanced(eliminations);
        if (!balanced) {
            logger.error("Intercompany eliminations are NOT balanced! Manual review required.");
        } else {
            logger.info("Successfully applied {} balanced intercompany eliminations", eliminations.size());
        }
        
        return adjustedRows;
    }

    /**
     * Hook for future intercompany eliminations (deprecated - use new method with full parameters).
     * 
     * @param consolidatedRows The consolidated rows before eliminations
     * @param companyBreakdown Breakdown of values per company
     * @return Rows after applying intercompany eliminations
     */
    @Deprecated
    protected List<ConsolidatedRow> applyIntercompanyEliminations(
            List<ConsolidatedRow> consolidatedRows,
            Map<Long, List<StatementRowResult>> companyBreakdown) {
        // Deprecated - use applyIntercompanyEliminations with companies, startDate, endDate
        logger.debug("Legacy applyIntercompanyEliminations called - returning rows unchanged");
        return consolidatedRows;
    }

    /**
     * Internal data class for accumulating consolidated row data.
     */
    private static class ConsolidatedRowData {
        Integer rowNumber;
        String label;
        com.econovafx.modules.accounting.model.FinancialStatementRow.RowType rowType;
        BigDecimal consolidatedValue;
        Integer indentLevel;
        Boolean isBold;
        Boolean isItalic;
        Map<Long, BigDecimal> companyValues;
    }
}
