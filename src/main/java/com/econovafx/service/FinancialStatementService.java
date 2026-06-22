package com.econovafx.service;

import com.econovafx.model.*;
import com.econovafx.repository.AccountRepository;
import com.econovafx.repository.FinancialStatementModelRepository;
import com.econovafx.repository.FinancialStatementRowRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating Financial Statements (Balance Sheet, Income Statement)
 */
@Component
public class FinancialStatementService {

    private static final Logger logger = LoggerFactory.getLogger(FinancialStatementService.class);

    private final FinancialStatementModelRepository modelRepository;
    private final FinancialStatementRowRepository rowRepository;
    private final AccountRepository accountRepository;

    @Inject
    public FinancialStatementService(FinancialStatementModelRepository modelRepository,
                                     FinancialStatementRowRepository rowRepository,
                                     AccountRepository accountRepository) {
        this.modelRepository = modelRepository;
        this.rowRepository = rowRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Generate a financial statement based on a model and date range
     */
    public FinancialStatementResult generateStatement(Long modelId, LocalDate startDate, LocalDate endDate) {
        FinancialStatementModel model = modelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelId));

        List<FinancialStatementRow> rows = rowRepository.findByModelId(modelId);
        rows.sort((r1, r2) -> r1.getRowNumber().compareTo(r2.getRowNumber()));

        List<Account> allAccounts = accountRepository.findAll();
        Map<String, BigDecimal> accountBalances = calculateAccountBalances(allAccounts, startDate, endDate);

        List<StatementRowResult> resultRows = new ArrayList<>();

        for (FinancialStatementRow row : rows) {
            StatementRowResult resultRow = calculateRowValue(row, accountBalances);
            resultRows.add(resultRow);
        }

        FinancialStatementResult result = new FinancialStatementResult();
        result.setModel(model);
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setRows(resultRows);
        result.setGeneratedAt(LocalDate.now());

        logger.info("Financial statement generated: {} for period {} to {}", 
                    model.getName(), startDate, endDate);

        return result;
    }

    /**
     * Calculate account balances for a given period
     */
    private Map<String, BigDecimal> calculateAccountBalances(List<Account> accounts, 
                                                             LocalDate startDate, 
                                                             LocalDate endDate) {
        // This should integrate with TransactionService to get real balances
        // For now, returning account current balance
        return accounts.stream()
                .collect(Collectors.toMap(
                        Account::getCode,
                        Account::getBalance
                ));
    }

    /**
     * Calculate the value for a specific row based on account patterns
     */
    private StatementRowResult calculateRowValue(FinancialStatementRow row, 
                                                  Map<String, BigDecimal> accountBalances) {
        StatementRowResult result = new StatementRowResult();
        result.setLabel(row.getLabel());
        result.setRowType(row.getRowType());
        result.setIndentLevel(row.getIndentLevel());
        result.setIsBold(row.getIsBold());
        result.setIsItalic(row.getIsItalic());

        if (row.getAccountCodesPattern() != null && !row.getAccountCodesPattern().isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            String[] patterns = row.getAccountCodesPattern().split(",");

            for (String pattern : patterns) {
                String cleanPattern = pattern.trim();
                
                for (Map.Entry<String, BigDecimal> entry : accountBalances.entrySet()) {
                    if (matchesPattern(entry.getKey(), cleanPattern)) {
                        BigDecimal value = entry.getValue().multiply(
                                BigDecimal.valueOf(row.getSignMultiplier()));
                        total = total.add(value);
                    }
                }
            }

            result.setValue(total);
        } else {
            result.setValue(BigDecimal.ZERO);
        }

        return result;
    }

    /**
     * Check if an account code matches a pattern (supports wildcards)
     */
    private boolean matchesPattern(String accountCode, String pattern) {
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return accountCode.matches(regex);
        }
        return accountCode.equals(pattern);
    }

    /**
     * Result class for financial statement generation
     */
    public static class FinancialStatementResult {
        private FinancialStatementModel model;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<StatementRowResult> rows;
        private LocalDate generatedAt;

        // Getters and Setters
        public FinancialStatementModel getModel() { return model; }
        public void setModel(FinancialStatementModel model) { this.model = model; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public List<StatementRowResult> getRows() { return rows; }
        public void setRows(List<StatementRowResult> rows) { this.rows = rows; }

        public LocalDate getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDate generatedAt) { this.generatedAt = generatedAt; }
    }

    /**
     * Result class for individual row calculation
     */
    public static class StatementRowResult {
        private String label;
        private FinancialStatementRow.RowType rowType;
        private BigDecimal value;
        private Integer indentLevel;
        private Boolean isBold;
        private Boolean isItalic;

        // Getters and Setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public FinancialStatementRow.RowType getRowType() { return rowType; }
        public void setRowType(FinancialStatementRow.RowType rowType) { this.rowType = rowType; }

        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }

        public Integer getIndentLevel() { return indentLevel; }
        public void setIndentLevel(Integer indentLevel) { this.indentLevel = indentLevel; }

        public Boolean getIsBold() { return isBold; }
        public void setIsBold(Boolean bold) { isBold = bold; }

        public Boolean getIsItalic() { return isItalic; }
        public void setIsItalic(Boolean italic) { isItalic = italic; }
    }
}
