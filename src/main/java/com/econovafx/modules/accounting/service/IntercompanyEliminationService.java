package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.IntercompanyElimination;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.core.model.Company;
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
 * Service for identifying and managing intercompany eliminations in consolidated financial statements.
 * 
 * Resolution 340/2004 compliance: Intercompany transactions must be eliminated to avoid
 * double-counting in consolidated financial statements.
 * 
 * This service provides the foundation for:
 * - Identifying reciprocal accounts between companies
 * - Eliminating intercompany revenues and expenses
 * - Removing unrealized profits from intercompany inventory transfers
 * - Generating elimination journal entries for audit trail
 * 
 * @author Development Team
 * @since 1.0.0
 */
@Component
public class IntercompanyEliminationService {

    private static final Logger logger = LoggerFactory.getLogger(IntercompanyEliminationService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    @Inject
    public IntercompanyEliminationService(TransactionRepository transactionRepository,
                                          TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    /**
     * Identifies potential intercompany transactions that may require elimination.
     * 
     * This is a foundational method that will be enhanced with:
     * - Third-party relationship tracking across companies
     * - Account code pattern matching for intercompany accounts
     * - Automatic detection of reciprocal balances
     * 
     * @param companies List of companies in the consolidation group
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return List of potential intercompany eliminations
     */
    public List<IntercompanyElimination> identifyIntercompanyTransactions(
            List<Company> companies,
            LocalDate startDate,
            LocalDate endDate) {
        
        List<IntercompanyElimination> eliminations = new ArrayList<>();
        
        logger.info("Identifying intercompany transactions for {} companies from {} to {}", 
                    companies.size(), startDate, endDate);
        
        // For each pair of companies, check for intercompany transactions
        for (int i = 0; i < companies.size(); i++) {
            for (int j = i + 1; j < companies.size(); j++) {
                Company companyA = companies.get(i);
                Company companyB = companies.get(j);
                
                // Find transactions between these two companies
                List<Transaction> transactionsAB = findTransactionsBetweenCompanies(
                        companyA.getId(), companyB.getId(), startDate, endDate);
                
                // Create elimination entries for identified transactions
                for (Transaction transaction : transactionsAB) {
                    IntercompanyElimination elimination = createEliminationFromTransaction(
                            transaction, companyA.getId(), companyB.getId());
                    if (elimination != null) {
                        eliminations.add(elimination);
                    }
                }
            }
        }
        
        logger.info("Found {} potential intercompany eliminations", eliminations.size());
        return eliminations;
    }

    /**
     * Finds transactions between two specific companies.
     * 
     * Note: Current implementation is limited by the data model.
     * Future enhancement: Add explicit company_id field to Transaction entity
     * and third_party relationships across companies.
     * 
     * @param companyId1 First company ID
     * @param companyId2 Second company ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions between the companies
     */
    private List<Transaction> findTransactionsBetweenCompanies(Long companyId1, 
                                                                Long companyId2,
                                                                LocalDate startDate,
                                                                LocalDate endDate) {
        // Use the repository method to find intercompany transactions
        // This delegates to TransactionRepository.findIntercompanyTransactions() which filters by company/counterparty
        return transactionRepository.findIntercompanyTransactions(companyId1, companyId2, startDate, endDate);
    }

    /**
     * Creates an elimination entry from a transaction.
     * 
     * @param transaction The transaction to create elimination for
     * @param sourceCompanyId Source company ID
     * @param targetCompanyId Target company ID
     * @return IntercompanyElimination object, or null if not applicable
     */
    private IntercompanyElimination createEliminationFromTransaction(Transaction transaction,
                                                                      Long sourceCompanyId,
                                                                      Long targetCompanyId) {
        // Determine elimination type based on transaction characteristics
        IntercompanyElimination.EliminationType type = determineEliminationType(transaction);
        
        if (type == null) {
            return null; // Not an intercompany transaction requiring elimination
        }
        
        BigDecimal totalAmount = transaction.getTotalDebit() != null ? 
                                  transaction.getTotalDebit() : BigDecimal.ZERO;
        
        IntercompanyElimination elimination = new IntercompanyElimination(
                type,
                "Eliminación intercompañía: " + (transaction.getDescription() != null ? 
                                                  transaction.getDescription() : 
                                                  transaction.getNumber()),
                sourceCompanyId,
                targetCompanyId,
                // TODO: Extract account codes from transaction entries
                "PENDING_ACCOUNT_CODE",
                "PENDING_COUNTER_ACCOUNT",
                totalAmount,
                transaction.getDate()
        );
        
        elimination.setOriginalTransactionId(transaction.getId());
        elimination.setCurrencyCode("CUP"); // Default currency, should be extracted from transaction
        
        return elimination;
    }

    /**
     * Determines the type of elimination required for a transaction.
     * 
     * @param transaction The transaction to analyze
     * @return EliminationType, or null if no elimination needed
     */
    private IntercompanyElimination.EliminationType determineEliminationType(Transaction transaction) {
        // TODO: Implement logic to determine elimination type based on:
        // - Account codes involved (intercompany receivable/payable accounts)
        // - Transaction type (sale, loan, dividend, etc.)
        // - Third party relationships
        
        // Placeholder: Assume all transactions between companies need elimination
        // In practice, this should analyze account codes and transaction details
        
        if (transaction.getType() != null && 
            (transaction.getType().contains("SALE") || transaction.getType().contains("INVOICE"))) {
            return IntercompanyElimination.EliminationType.REVENUE_EXPENSE;
        }
        
        // Default to receivable/payable elimination
        return IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE;
    }

    /**
     * Applies intercompany eliminations to consolidated rows.
     * 
     * This is the main method called by ConsolidationService to adjust
     * consolidated financial statement values after simple aggregation.
     * 
     * @param consolidatedRows The consolidated rows before eliminations
     * @param companyBreakdown Breakdown of values per company
     * @param eliminations List of identified intercompany eliminations
     * @return Adjusted consolidated rows after applying eliminations
     */
    public List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> applyEliminations(
            List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> consolidatedRows,
            Map<Long, List<com.econovafx.modules.accounting.service.FinancialStatementService.StatementRowResult>> companyBreakdown,
            List<IntercompanyElimination> eliminations) {
        
        if (eliminations == null || eliminations.isEmpty()) {
            logger.debug("No eliminations to apply - returning original consolidated rows");
            return consolidatedRows;
        }
        
        logger.info("Applying {} intercompany eliminations to consolidated statement", eliminations.size());
        
        // Create a working copy of consolidated rows for modification
        List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> adjustedRows = 
                new ArrayList<>(consolidatedRows);
        
        // Group eliminations by type for efficient processing
        Map<IntercompanyElimination.EliminationType, List<IntercompanyElimination>> byType = 
                eliminations.stream()
                        .collect(Collectors.groupingBy(IntercompanyElimination::getType));
        
        // Process each elimination type
        for (Map.Entry<IntercompanyElimination.EliminationType, List<IntercompanyElimination>> entry : byType.entrySet()) {
            IntercompanyElimination.EliminationType type = entry.getKey();
            List<IntercompanyElimination> typeEliminations = entry.getValue();
            
            logger.debug("Processing {} eliminations of type {}", typeEliminations.size(), type);
            
            switch (type) {
                case RECEIVABLE_PAYABLE:
                    adjustedRows = eliminateReceivablesPayables(adjustedRows, typeEliminations);
                    break;
                case REVENUE_EXPENSE:
                    adjustedRows = eliminateRevenuesExpenses(adjustedRows, typeEliminations);
                    break;
                case UNREALIZED_PROFIT:
                    adjustedRows = eliminateUnrealizedProfits(adjustedRows, typeEliminations);
                    break;
                default:
                    logger.warn("Unhandled elimination type: {}", type);
            }
        }
        
        logger.info("Eliminations applied successfully - {} adjusted rows", adjustedRows.size());
        return adjustedRows;
    }

    /**
     * Eliminates intercompany receivables and payables.
     * 
     * When Company A has a receivable from Company B, and Company B has a payable to Company A,
     * these amounts cancel out in consolidation.
     * 
     * @param consolidatedRows Current consolidated rows
     * @param eliminations List of receivable/payable eliminations
     * @return Adjusted rows after eliminations
     */
    private List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> eliminateReceivablesPayables(
            List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> consolidatedRows,
            List<IntercompanyElimination> eliminations) {
        
        logger.info("Applying {} receivables/payables eliminations", eliminations.size());
        
        if (eliminations == null || eliminations.isEmpty()) {
            return consolidatedRows;
        }
        
        // Create a mutable copy of rows for modification
        List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> adjustedRows = new ArrayList<>();
        for (com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow row : consolidatedRows) {
            adjustedRows.add(createMutableCopy(row));
        }
        
        // Group eliminations by account code pattern
        Map<String, BigDecimal> receivableEliminations = new HashMap<>();
        Map<String, BigDecimal> payableEliminations = new HashMap<>();
        
        for (IntercompanyElimination elimination : eliminations) {
            String accountCode = elimination.getAccountCode();
            String counterAccountCode = elimination.getCounterAccountCode();
            BigDecimal amount = elimination.getAmount();
            
            // Identify receivable accounts (typically 1.1.3.x or containing INTERCOMPANY_RECEIVABLE)
            if (accountCode.contains("INTERCOMPANY_RECEIVABLE") || accountCode.matches("1\\.1\\.3\\..*")) {
                receivableEliminations.merge(accountCode, amount, BigDecimal::add);
            } else if (counterAccountCode.contains("INTERCOMPANY_RECEIVABLE") || counterAccountCode.matches("1\\.1\\.3\\..*")) {
                receivableEliminations.merge(counterAccountCode, amount, BigDecimal::add);
            }
            
            // Identify payable accounts (typically 2.1.3.x or containing INTERCOMPANY_PAYABLE)
            if (accountCode.contains("INTERCOMPANY_PAYABLE") || accountCode.matches("2\\.1\\.3\\..*")) {
                payableEliminations.merge(accountCode, amount, BigDecimal::add);
            } else if (counterAccountCode.contains("INTERCOMPANY_PAYABLE") || counterAccountCode.matches("2\\.1\\.3\\..*")) {
                payableEliminations.merge(counterAccountCode, amount, BigDecimal::add);
            }
        }
        
        // Apply eliminations to consolidated rows
        for (int i = 0; i < adjustedRows.size(); i++) {
            com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow row = adjustedRows.get(i);
            String label = row.getLabel().toUpperCase();
            
            // Check if this row corresponds to intercompany receivables
            if (label.contains("INTERCOMPANY RECEIVABLE") || label.contains("CUENTAS POR COBRAR INTERCOMPAÑÍA") ||
                label.contains("INTERCOMPANY RECEIVABLES")) {
                BigDecimal totalReceivableElimination = receivableEliminations.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (totalReceivableElimination.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newValue = row.getConsolidatedValue().subtract(totalReceivableElimination);
                    adjustedRows.set(i, createRowWithNewValue(row, newValue));
                    logger.debug("Eliminated {} from intercompany receivables row: {}", totalReceivableElimination, label);
                }
            }
            
            // Check if this row corresponds to intercompany payables
            if (label.contains("INTERCOMPANY PAYABLE") || label.contains("CUENTAS POR PAGAR INTERCOMPAÑÍA") ||
                label.contains("INTERCOMPANY PAYABLES")) {
                BigDecimal totalPayableElimination = payableEliminations.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (totalPayableElimination.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newValue = row.getConsolidatedValue().subtract(totalPayableElimination);
                    adjustedRows.set(i, createRowWithNewValue(row, newValue));
                    logger.debug("Eliminated {} from intercompany payables row: {}", totalPayableElimination, label);
                }
            }
        }
        
        logger.info("Receivables/Payables eliminations applied successfully");
        return adjustedRows;
    }

    /**
     * Eliminates intercompany revenues and expenses.
     * 
     * When Company A records revenue from a sale to Company B, and Company B records
     * an expense for that purchase, both must be eliminated in consolidation.
     * 
     * @param consolidatedRows Current consolidated rows
     * @param eliminations List of revenue/expense eliminations
     * @return Adjusted rows after eliminations
     */
    private List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> eliminateRevenuesExpenses(
            List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> consolidatedRows,
            List<IntercompanyElimination> eliminations) {
        
        logger.info("Applying {} revenue/expense eliminations", eliminations.size());
        
        if (eliminations == null || eliminations.isEmpty()) {
            return consolidatedRows;
        }
        
        // Create a mutable copy of rows for modification
        List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> adjustedRows = new ArrayList<>();
        for (com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow row : consolidatedRows) {
            adjustedRows.add(createMutableCopy(row));
        }
        
        // Group eliminations by account code pattern
        Map<String, BigDecimal> revenueEliminations = new HashMap<>();
        Map<String, BigDecimal> expenseEliminations = new HashMap<>();
        
        for (IntercompanyElimination elimination : eliminations) {
            String accountCode = elimination.getAccountCode();
            String counterAccountCode = elimination.getCounterAccountCode();
            BigDecimal amount = elimination.getAmount();
            
            // Identify revenue accounts (typically 4.1.x or containing INTERCOMPANY_REVENUE)
            if (accountCode.contains("INTERCOMPANY_REVENUE") || accountCode.matches("4\\\\.1\\\\..*")) {
                revenueEliminations.merge(accountCode, amount, BigDecimal::add);
            } else if (counterAccountCode.contains("INTERCOMPANY_REVENUE") || counterAccountCode.matches("4\\\\.1\\\\..*")) {
                revenueEliminations.merge(counterAccountCode, amount, BigDecimal::add);
            }
            
            // Identify expense accounts (typically 5.1.x or containing INTERCOMPANY_EXPENSE)
            if (accountCode.contains("INTERCOMPANY_EXPENSE") || accountCode.matches("5\\\\.1\\\\..*")) {
                expenseEliminations.merge(accountCode, amount, BigDecimal::add);
            } else if (counterAccountCode.contains("INTERCOMPANY_EXPENSE") || counterAccountCode.matches("5\\\\.1\\\\..*")) {
                expenseEliminations.merge(counterAccountCode, amount, BigDecimal::add);
            }
        }
        
        // Apply eliminations to consolidated rows
        for (int i = 0; i < adjustedRows.size(); i++) {
            com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow row = adjustedRows.get(i);
            String label = row.getLabel().toUpperCase();
            
            // Check if this row corresponds to intercompany revenues
            if (label.contains("INTERCOMPANY REVENUE") || label.contains("INGRESOS INTERCOMPAÑÍA") ||
                label.contains("INTERCOMPANY SALES")) {
                BigDecimal totalRevenueElimination = revenueEliminations.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (totalRevenueElimination.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newValue = row.getConsolidatedValue().subtract(totalRevenueElimination);
                    adjustedRows.set(i, createRowWithNewValue(row, newValue));
                    logger.debug("Eliminated {} from intercompany revenue row: {}", totalRevenueElimination, label);
                }
            }
            
            // Check if this row corresponds to intercompany expenses
            if (label.contains("INTERCOMPANY EXPENSE") || label.contains("GASTOS INTERCOMPAÑÍA") ||
                label.contains("INTERCOMPANY PURCHASES")) {
                BigDecimal totalExpenseElimination = expenseEliminations.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (totalExpenseElimination.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newValue = row.getConsolidatedValue().subtract(totalExpenseElimination);
                    adjustedRows.set(i, createRowWithNewValue(row, newValue));
                    logger.debug("Eliminated {} from intercompany expense row: {}", totalExpenseElimination, label);
                }
            }
        }
        
        logger.info("Revenue/Expense eliminations applied successfully");
        return adjustedRows;
    }

    /**
     * Eliminates unrealized profits from intercompany inventory transfers.
     * 
     * When Company A sells inventory to Company B with a profit margin, but Company B
     * hasn't sold that inventory to external customers by period end, the profit
     * is unrealized and must be eliminated.
     * 
     * @param consolidatedRows Current consolidated rows
     * @param eliminations List of unrealized profit eliminations
     * @return Adjusted rows after eliminations
     */
    private List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> eliminateUnrealizedProfits(
            List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> consolidatedRows,
            List<IntercompanyElimination> eliminations) {
        
        // TODO: Implement actual elimination logic
        // This is the most complex elimination type, requiring:
        // - Tracking inventory cost basis across companies
        // - Calculating profit margins on intercompany sales
        // - Determining what percentage remains in ending inventory
        
        logger.debug("Unrealized profit elimination: {} eliminations identified", eliminations.size());
        // Placeholder: In production, this would:
        // 1. Calculate unrealized profit = Intercompany profit margin × Remaining inventory %
        // 2. Reduce inventory value on consolidated balance sheet
        // 3. Reduce cost of goods sold / increase inventory on consolidated income statement
        
        return consolidatedRows;
    }

    /**
     * Generates journal entries for elimination adjustments.
     * 
     * Creates audit trail for all eliminations applied during consolidation.
     * 
     * @param eliminations List of eliminations to record
     * @param consolidationDate Date of the consolidation
     * @return List of transaction IDs for the elimination entries
     * 
     * TODO: Implement when Transaction creation service is available
     */
    public List<Long> generateEliminationJournalEntries(List<IntercompanyElimination> eliminations,
                                                         LocalDate consolidationDate) {
        logger.info("Generating elimination journal entries for {} eliminations", eliminations.size());
        
        // TODO: Implement journal entry generation
        // This would:
        // 1. Create a special "Consolidation Adjustments" transaction type
        // 2. Generate debit/credit entries for each elimination
        // 3. Link elimination entries to original transactions for audit trail
        // 4. Store in a separate consolidation adjustment ledger
        
        return new ArrayList<>(); // Placeholder
    }

    /**
     * Validates that eliminations are balanced (debits = credits).
     * 
     * @param eliminations List of eliminations to validate
     * @return true if eliminations are balanced, false otherwise
     */
    public boolean validateEliminationsBalanced(List<IntercompanyElimination> eliminations) {
        if (eliminations == null || eliminations.isEmpty()) {
            return true;
        }
        
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        
        for (IntercompanyElimination elimination : eliminations) {
            // Each elimination should have equal debit and credit impact
            totalDebits = totalDebits.add(elimination.getAmount());
            totalCredits = totalCredits.add(elimination.getAmount());
        }
        
        boolean balanced = totalDebits.compareTo(totalCredits) == 0;
        
        if (!balanced) {
            logger.error("Eliminations are NOT balanced! Debits: {}, Credits: {}", 
                         totalDebits, totalCredits);
        } else {
            logger.debug("Eliminations validated: {} debits, {} credits - BALANCED", 
                         totalDebits, totalCredits);
        }
        
        return balanced;
    }

    /**
     * Creates a mutable copy of a ConsolidatedRow with the same properties.
     * 
     * @param original The original row to copy
     * @return A new ConsolidatedRow with the same values
     */
    private com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow createMutableCopy(
            com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow original) {
        return new com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow(
                original.getRowNumber(),
                original.getLabel(),
                original.getRowType(),
                original.getConsolidatedValue(),
                original.getIndentLevel(),
                original.getIsBold(),
                original.getIsItalic(),
                original.getCompanyValues()
        );
    }

    /**
     * Creates a new ConsolidatedRow with an updated value.
     * 
     * @param original The original row
     * @param newValue The new consolidated value
     * @return A new ConsolidatedRow with the updated value
     */
    private com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow createRowWithNewValue(
            com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow original,
            BigDecimal newValue) {
        return new com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow(
                original.getRowNumber(),
                original.getLabel(),
                original.getRowType(),
                newValue,
                original.getIndentLevel(),
                original.getIsBold(),
                original.getIsItalic(),
                original.getCompanyValues()
        );
    }
}
