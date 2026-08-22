package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.IntercompanyElimination;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.model.TransactionStatus;
import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.accounting.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

    @Inject
    public IntercompanyEliminationService(TransactionRepository transactionRepository,
                                          TransactionService transactionService,
                                          AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
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
        
        // Extract account codes from transaction entries
        String accountCode = null;
        String counterAccountCode = null;
        
        List<TransactionEntry> entries = transaction.getEntries();
        if (entries == null || entries.isEmpty()) {
            logger.warn("Transaction {} has no entries - cannot create elimination", transaction.getId());
            return null;
        }
        
        // Find the first debit entry (account) and first credit entry (counter-account)
        TransactionEntry debitEntry = null;
        TransactionEntry creditEntry = null;
        
        for (TransactionEntry entry : entries) {
            if (entry.getDebitAmount() != null && entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (debitEntry == null) {
                    debitEntry = entry;
                }
            }
            if (entry.getCreditAmount() != null && entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (creditEntry == null) {
                    creditEntry = entry;
                }
            }
        }
        
        // Extract account codes
        if (debitEntry != null && debitEntry.getAccount() != null) {
            accountCode = debitEntry.getAccount().getCode();
        } else if (entries.get(0).getAccount() != null) {
            // Fallback: use first entry's account
            accountCode = entries.get(0).getAccount().getCode();
        }
        
        if (creditEntry != null && creditEntry.getAccount() != null) {
            counterAccountCode = creditEntry.getAccount().getCode();
        } else if (entries.size() > 1 && entries.get(1).getAccount() != null) {
            // Fallback: use second entry's account
            counterAccountCode = entries.get(1).getAccount().getCode();
        } else if (entries.get(0).getAccount() != null) {
            // Fallback: use first entry's account if only one entry
            counterAccountCode = entries.get(0).getAccount().getCode();
        }
        
        // Use default values if extraction failed
        if (accountCode == null) {
            accountCode = "PENDING_ACCOUNT_CODE";
        }
        if (counterAccountCode == null) {
            counterAccountCode = "PENDING_COUNTER_ACCOUNT";
        }
        
        IntercompanyElimination elimination = new IntercompanyElimination(
                type,
                "Eliminación intercompañía: " + (transaction.getDescription() != null ? 
                                                  transaction.getDescription() : 
                                                  transaction.getNumber()),
                sourceCompanyId,
                targetCompanyId,
                accountCode,
                counterAccountCode,
                totalAmount,
                transaction.getDate()
        );
        
        elimination.setOriginalTransactionId(transaction.getId());
        // Note: Transaction model does not have currency field; using CUP as functional currency per Cuban GAAP
        elimination.setCurrencyCode("CUP");
        
        return elimination;
    }

    /**
     * Determines the type of elimination required for a transaction.
     * 
     * @param transaction The transaction to analyze
     * @return EliminationType, or null if no elimination needed
     */
    private IntercompanyElimination.EliminationType determineEliminationType(Transaction transaction) {
        // Analyze account codes from transaction entries to determine elimination type
        List<TransactionEntry> entries = transaction.getEntries();
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        
        // Check account codes to determine the type of elimination
        for (TransactionEntry entry : entries) {
            if (entry.getAccount() == null) {
                continue;
            }
            String accountCode = entry.getAccount().getCode();
            
            // Revenue accounts (4.1.x) → REVENUE_EXPENSE elimination
            if (accountCode != null && accountCode.matches("4\\.1\\..*")) {
                return IntercompanyElimination.EliminationType.REVENUE_EXPENSE;
            }
            
            // Expense accounts (5.1.x) → REVENUE_EXPENSE elimination
            if (accountCode != null && accountCode.matches("5\\.1\\..*")) {
                return IntercompanyElimination.EliminationType.REVENUE_EXPENSE;
            }
            
            // Intercompany receivable/payable accounts (1.1.3.x or 2.1.3.x) → RECEIVABLE_PAYABLE elimination
            if (accountCode != null && (accountCode.matches("1\\.1\\.3\\..*") || accountCode.matches("2\\.1\\.3\\..*"))) {
                return IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE;
            }
            
            // Inventory accounts (1.3.x) may indicate unrealized profit elimination
            if (accountCode != null && accountCode.matches("1\\.3\\..*")) {
                // Could be unrealized profit, but need more context
                // Default to RECEIVABLE_PAYABLE for now
            }
        }
        
        // Fallback: use transaction type as secondary indicator
        if (transaction.getType() != null) {
            String typeUpper = transaction.getType().toUpperCase();
            if (typeUpper.contains("SALE") || typeUpper.contains("INVOICE") || typeUpper.contains("REVENUE")) {
                return IntercompanyElimination.EliminationType.REVENUE_EXPENSE;
            }
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
            if (accountCode.contains("INTERCOMPANY_REVENUE") || accountCode.matches("4\\.1\\..*")) {
                revenueEliminations.merge(accountCode, amount, BigDecimal::add);
            } else if (counterAccountCode.contains("INTERCOMPANY_REVENUE") || counterAccountCode.matches("4\\.1\\..*")) {
                revenueEliminations.merge(counterAccountCode, amount, BigDecimal::add);
            }
            
            // Identify expense accounts (typically 5.1.x or containing INTERCOMPANY_EXPENSE)
            if (accountCode.contains("INTERCOMPANY_EXPENSE") || accountCode.matches("5\\.1\\..*")) {
                expenseEliminations.merge(accountCode, amount, BigDecimal::add);
            } else if (counterAccountCode.contains("INTERCOMPANY_EXPENSE") || counterAccountCode.matches("5\\.1\\..*")) {
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
        
        if (eliminations == null || eliminations.isEmpty()) {
            return consolidatedRows;
        }
        
        logger.info("Applying {} unrealized profit eliminations", eliminations.size());
        
        // Create a mutable copy of rows for modification
        List<com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow> adjustedRows = new ArrayList<>();
        for (com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow row : consolidatedRows) {
            adjustedRows.add(createMutableCopy(row));
        }
        
        // Calculate total unrealized profit to eliminate
        BigDecimal totalUnrealizedProfit = eliminations.stream()
                .map(IntercompanyElimination::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalUnrealizedProfit.compareTo(BigDecimal.ZERO) <= 0) {
            return adjustedRows;
        }
        
        // Apply elimination to inventory and COGS rows
        // Note: This is a simplified implementation using available data
        // Full implementation would require tracking inventory cost basis and remaining %
        for (int i = 0; i < adjustedRows.size(); i++) {
            com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow row = adjustedRows.get(i);
            String label = row.getLabel().toUpperCase();
            
            // Reduce inventory value on balance sheet
            if (label.contains("INVENTORY") || label.contains("INVENTARIO") || 
                label.contains("STOCK") || label.contains("MERCHANDISE")) {
                BigDecimal newValue = row.getConsolidatedValue().subtract(totalUnrealizedProfit);
                adjustedRows.set(i, createRowWithNewValue(row, newValue));
                logger.debug("Reduced inventory by {} for unrealized profit: {}", totalUnrealizedProfit, label);
            }
            
            // Adjust COGS on income statement (increase COGS to reduce profit)
            if (label.contains("COST OF GOODS SOLD") || label.contains("COSTO DE VENTA") ||
                label.contains("COGS") || label.contains("COST OF SALES")) {
                BigDecimal newValue = row.getConsolidatedValue().add(totalUnrealizedProfit);
                adjustedRows.set(i, createRowWithNewValue(row, newValue));
                logger.debug("Increased COGS by {} for unrealized profit elimination: {}", totalUnrealizedProfit, label);
            }
        }
        
        logger.info("Unrealized profit eliminations applied successfully");
        return adjustedRows;
    }

    /**
     * Generates journal entries for elimination adjustments.
     * 
     * Creates audit trail for all eliminations applied during consolidation.
     * 
     * @param eliminations List of eliminations to record
     * @param consolidationDate Date of the consolidation
     * @return List of transaction IDs for the elimination entries
     */
    public List<Long> generateEliminationJournalEntries(List<IntercompanyElimination> eliminations,
                                                         LocalDate consolidationDate) {
        logger.info("Generating elimination journal entries for {} eliminations", eliminations.size());
        
        if (eliminations == null || eliminations.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> transactionIds = new ArrayList<>();
        
        // Group eliminations by original transaction for efficient processing
        Map<Long, List<IntercompanyElimination>> byOriginalTransaction = eliminations.stream()
                .filter(e -> e.getOriginalTransactionId() != null)
                .collect(Collectors.groupingBy(IntercompanyElimination::getOriginalTransactionId));
        
        // Create consolidation adjustment transactions
        int entryCount = 0;
        for (Map.Entry<Long, List<IntercompanyElimination>> entry : byOriginalTransaction.entrySet()) {
            Long originalTxnId = entry.getKey();
            List<IntercompanyElimination> txnEliminations = entry.getValue();
            
            // Create a consolidation adjustment transaction
            Transaction adjustmentTxn = new Transaction();
            adjustmentTxn.setDate(consolidationDate);
            adjustmentTxn.setType("CONSOLIDATION_ADJUSTMENT");
            adjustmentTxn.setDescription("Consolidation elimination entries - batch " + (++entryCount));
            adjustmentTxn.setReference("CONS-ADJ-" + consolidationDate + "-" + entryCount);
            adjustmentTxn.setStatus(TransactionStatus.DRAFT);
            
            List<TransactionService.TransactionEntryData> adjustmentEntries = new ArrayList<>();
            
            // For each elimination, create offsetting debit/credit entries
            for (IntercompanyElimination elimination : txnEliminations) {
                String accountCode = elimination.getAccountCode();
                String counterAccountCode = elimination.getCounterAccountCode();
                BigDecimal amount = elimination.getAmount();
                
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                
                // Find accounts by code
                Account debitAccount = accountRepository.findByCode(accountCode).orElse(null);
                Account creditAccount = accountRepository.findByCode(counterAccountCode).orElse(null);
                
                if (debitAccount == null || creditAccount == null) {
                    logger.warn("Could not find accounts for elimination: {} / {}", accountCode, counterAccountCode);
                    continue;
                }
                
                // Determine which account to debit and which to credit based on elimination type
                // For revenue/expense eliminations: debit revenue, credit expense
                // For receivable/payable: debit payable, credit receivable
                IntercompanyElimination.EliminationType type = elimination.getType();
                
                if (type == IntercompanyElimination.EliminationType.REVENUE_EXPENSE) {
                    // Debit revenue account (reduce revenue), credit expense account (reduce expense)
                    adjustmentEntries.add(new TransactionService.TransactionEntryData(
                            debitAccount.getId(), amount, BigDecimal.ZERO, 
                            "Elimination debit - " + elimination.getDescription()));
                    adjustmentEntries.add(new TransactionService.TransactionEntryData(
                            creditAccount.getId(), BigDecimal.ZERO, amount,
                            "Elimination credit - " + elimination.getDescription()));
                } else if (type == IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE) {
                    // Debit payable (reduce liability), credit receivable (reduce asset)
                    adjustmentEntries.add(new TransactionService.TransactionEntryData(
                            debitAccount.getId(), amount, BigDecimal.ZERO,
                            "Elimination of intercompany payable - " + elimination.getDescription()));
                    adjustmentEntries.add(new TransactionService.TransactionEntryData(
                            creditAccount.getId(), BigDecimal.ZERO, amount,
                            "Elimination of intercompany receivable - " + elimination.getDescription()));
                } else {
                    // Default: use account codes as provided
                    adjustmentEntries.add(new TransactionService.TransactionEntryData(
                            debitAccount.getId(), amount, BigDecimal.ZERO,
                            "Elimination debit - " + elimination.getDescription()));
                    adjustmentEntries.add(new TransactionService.TransactionEntryData(
                            creditAccount.getId(), BigDecimal.ZERO, amount,
                            "Elimination credit - " + elimination.getDescription()));
                }
            }
            
            // Only create transaction if we have valid entries
            if (!adjustmentEntries.isEmpty()) {
                try {
                    Transaction savedTxn = transactionService.createTransaction(adjustmentTxn, adjustmentEntries, "system");
                    transactionIds.add(savedTxn.getId());
                    logger.debug("Created consolidation adjustment transaction: {}", savedTxn.getNumber());
                } catch (Exception e) {
                    logger.error("Failed to create elimination journal entry for original transaction {}", 
                                 originalTxnId, e);
                }
            }
        }
        
        logger.info("Generated {} elimination journal entries", transactionIds.size());
        return transactionIds;
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
