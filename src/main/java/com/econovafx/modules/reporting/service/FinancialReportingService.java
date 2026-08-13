package com.econovafx.modules.reporting.service;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.reporting.model.FinancialReport;
import com.econovafx.modules.reporting.model.FinancialReport.ReportLine;
import com.econovafx.modules.reporting.repository.FinancialReportRepository;
import io.ebean.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for generating financial reports required by Resolution 340/2004.
 * Implements Balance Sheet, Income Statement, Cash Flow, and Trial Balance generation.
 */
public class FinancialReportingService {

    private FinancialReportRepository reportRepository;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    public FinancialReportingService(FinancialReportRepository reportRepository,
                                     AccountRepository accountRepository,
                                     TransactionRepository transactionRepository) {
        this.reportRepository = reportRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generate a Balance Sheet report.
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    @Transactional(readOnly = true)
    public FinancialReport generateBalanceSheet(LocalDate startDate, LocalDate endDate, Integer fiscalYear) {
        FinancialReport report = new FinancialReport();
        report.setReportType(FinancialReport.ReportType.BALANCE_SHEET);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setFiscalYear(fiscalYear);
        report.setFinal(false);

        List<ReportLine> lines = new ArrayList<>();
        
        // Get all asset accounts
        List<Account> assetAccounts = accountRepository.findByAccountType(Account.AccountType.ASSET);
        BigDecimal totalAssets = BigDecimal.ZERO;
        
        for (Account account : assetAccounts) {
            BigDecimal balance = calculateAccountBalance(account, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                    account.getAccountCode(),
                    account.getDescription(),
                    balance.compareTo(BigDecimal.ZERO) > 0 ? balance.doubleValue() : 0.0,
                    balance.compareTo(BigDecimal.ZERO) < 0 ? balance.abs().doubleValue() : 0.0,
                    balance.doubleValue()
                ));
                totalAssets = totalAssets.add(balance);
            }
        }
        
        // Add total assets line
        lines.add(new ReportLine("", "TOTAL ASSETS", totalAssets.doubleValue(), 0.0, totalAssets.doubleValue()));
        
        // Get liability and equity accounts
        List<Account> liabilityAccounts = accountRepository.findByAccountType(Account.AccountType.LIABILITY);
        List<Account> equityAccounts = accountRepository.findByAccountType(Account.AccountType.EQUITY);
        
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        for (Account account : liabilityAccounts) {
            BigDecimal balance = calculateAccountBalance(account, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                    account.getAccountCode(),
                    account.getDescription(),
                    0.0,
                    balance.doubleValue(),
                    balance.negate().doubleValue()
                ));
                totalLiabilities = totalLiabilities.add(balance);
            }
        }
        
        BigDecimal totalEquity = BigDecimal.ZERO;
        for (Account account : equityAccounts) {
            BigDecimal balance = calculateAccountBalance(account, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                    account.getAccountCode(),
                    account.getDescription(),
                    0.0,
                    balance.doubleValue(),
                    balance.negate().doubleValue()
                ));
                totalEquity = totalEquity.add(balance);
            }
        }
        
        lines.add(new ReportLine("", "TOTAL LIABILITIES", 0.0, totalLiabilities.doubleValue(), totalLiabilities.negate().doubleValue()));
        lines.add(new ReportLine("", "TOTAL EQUITY", 0.0, totalEquity.doubleValue(), totalEquity.negate().doubleValue()));
        lines.add(new ReportLine("", "TOTAL LIABILITIES + EQUITY", 0.0, totalLiabilities.add(totalEquity).doubleValue(), 
            totalLiabilities.add(totalEquity).negate().doubleValue()));

        report.setLines(lines);
        report.setGeneratedData("Balance Sheet generated successfully");
        
        return reportRepository.save(report);
    }

    /**
     * Generate an Income Statement report.
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    @Transactional(readOnly = true)
    public FinancialReport generateIncomeStatement(LocalDate startDate, LocalDate endDate, Integer fiscalYear) {
        FinancialReport report = new FinancialReport();
        report.setReportType(FinancialReport.ReportType.INCOME_STATEMENT);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setFiscalYear(fiscalYear);
        report.setFinal(false);

        List<ReportLine> lines = new ArrayList<>();
        
        // Get revenue accounts
        List<Account> revenueAccounts = accountRepository.findByAccountType(Account.AccountType.REVENUE);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Account account : revenueAccounts) {
            BigDecimal balance = calculateAccountBalancePeriod(account, startDate, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                    account.getAccountCode(),
                    account.getDescription(),
                    0.0,
                    balance.doubleValue(),
                    balance.negate().doubleValue()
                ));
                totalRevenue = totalRevenue.add(balance);
            }
        }
        
        lines.add(new ReportLine("", "TOTAL REVENUE", 0.0, totalRevenue.doubleValue(), totalRevenue.negate().doubleValue()));
        
        // Get expense accounts
        List<Account> expenseAccounts = accountRepository.findByAccountType(Account.AccountType.EXPENSE);
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        for (Account account : expenseAccounts) {
            BigDecimal balance = calculateAccountBalancePeriod(account, startDate, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                    account.getAccountCode(),
                    account.getDescription(),
                    balance.doubleValue(),
                    0.0,
                    balance.doubleValue()
                ));
                totalExpenses = totalExpenses.add(balance);
            }
        }
        
        lines.add(new ReportLine("", "TOTAL EXPENSES", totalExpenses.doubleValue(), 0.0, totalExpenses.doubleValue()));
        
        BigDecimal netIncome = totalRevenue.subtract(totalExpenses);
        lines.add(new ReportLine("", "NET INCOME/(LOSS)", 
            netIncome.compareTo(BigDecimal.ZERO) < 0 ? netIncome.abs().doubleValue() : 0.0,
            netIncome.compareTo(BigDecimal.ZERO) >= 0 ? netIncome.doubleValue() : 0.0,
            netIncome.doubleValue()));

        report.setLines(lines);
        report.setGeneratedData("Income Statement generated successfully");
        
        return reportRepository.save(report);
    }

    /**
     * Generate a Trial Balance report.
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    @Transactional(readOnly = true)
    public FinancialReport generateTrialBalance(LocalDate endDate, Integer fiscalYear) {
        FinancialReport report = new FinancialReport();
        report.setReportType(FinancialReport.ReportType.TRIAL_BALANCE);
        report.setStartDate(endDate);
        report.setEndDate(endDate);
        report.setFiscalYear(fiscalYear);
        report.setFinal(false);

        List<ReportLine> lines = new ArrayList<>();
        List<Account> allAccounts = accountRepository.findAllByOrderByAccountCode();
        
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        
        for (Account account : allAccounts) {
            BigDecimal balance = calculateAccountBalance(account, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                double debit = balance.compareTo(BigDecimal.ZERO) > 0 ? balance.doubleValue() : 0.0;
                double credit = balance.compareTo(BigDecimal.ZERO) < 0 ? balance.abs().doubleValue() : 0.0;
                
                lines.add(new ReportLine(
                    account.getAccountCode(),
                    account.getDescription(),
                    debit,
                    credit,
                    balance.doubleValue()
                ));
                
                totalDebits = totalDebits.add(BigDecimal.valueOf(debit));
                totalCredits = totalCredits.add(BigDecimal.valueOf(credit));
            }
        }
        
        lines.add(new ReportLine("", "TOTALS", totalDebits.doubleValue(), totalCredits.doubleValue(), 
            totalDebits.subtract(totalCredits).doubleValue()));

        report.setLines(lines);
        report.setGeneratedData("Trial Balance generated successfully - Debits: " + totalDebits + ", Credits: " + totalCredits);
        
        return reportRepository.save(report);
    }

    /**
     * Generate a Cash Flow Statement report.
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    @Transactional(readOnly = true)
    public FinancialReport generateCashFlowStatement(LocalDate startDate, LocalDate endDate, Integer fiscalYear) {
        FinancialReport report = new FinancialReport();
        report.setReportType(FinancialReport.ReportType.CASH_FLOW);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setFiscalYear(fiscalYear);
        report.setFinal(false);

        List<ReportLine> lines = new ArrayList<>();
        
        // Find cash and bank accounts
        List<Account> cashAccounts = accountRepository.findByAccountType(Account.AccountType.ASSET);
        BigDecimal operatingCashFlow = BigDecimal.ZERO;
        BigDecimal investingCashFlow = BigDecimal.ZERO;
        BigDecimal financingCashFlow = BigDecimal.ZERO;
        
        // Simplified cash flow calculation based on transaction analysis
        List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(startDate, endDate);
        
        for (Transaction transaction : transactions) {
            // Analyze transaction type and categorize cash flow
            // This is a simplified implementation - real implementation would need more detailed analysis
            if (transaction.getDescription().toLowerCase().contains("operating")) {
                operatingCashFlow = operatingCashFlow.add(transaction.getAmount());
            } else if (transaction.getDescription().toLowerCase().contains("investing")) {
                investingCashFlow = investingCashFlow.add(transaction.getAmount());
            } else if (transaction.getDescription().toLowerCase().contains("financing")) {
                financingCashFlow = financingCashFlow.add(transaction.getAmount());
            }
        }
        
        lines.add(new ReportLine("", "Cash Flow from Operating Activities", operatingCashFlow.doubleValue(), 0.0, operatingCashFlow.doubleValue()));
        lines.add(new ReportLine("", "Cash Flow from Investing Activities", investingCashFlow.doubleValue(), 0.0, investingCashFlow.doubleValue()));
        lines.add(new ReportLine("", "Cash Flow from Financing Activities", financingCashFlow.doubleValue(), 0.0, financingCashFlow.doubleValue()));
        
        BigDecimal netCashFlow = operatingCashFlow.add(investingCashFlow).add(financingCashFlow);
        lines.add(new ReportLine("", "NET CASH FLOW", netCashFlow.doubleValue(), 0.0, netCashFlow.doubleValue()));

        report.setLines(lines);
        report.setGeneratedData("Cash Flow Statement generated successfully");
        
        return reportRepository.save(report);
    }

    /**
     * Finalize a report, making it immutable.
     * @param reportId ID of the report to finalize
     * @return finalized report
     */
    @Transactional
    public FinancialReport finalizeReport(Long reportId) {
        FinancialReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        
        if (report.isFinal()) {
            throw new IllegalStateException("Report is already finalized");
        }
        
        report.setFinal(true);
        return reportRepository.save(report);
    }

    /**
     * Calculate account balance up to a specific date.
     * @param account the account
     * @param endDate end date for calculation
     * @return account balance
     */
    private BigDecimal calculateAccountBalance(Account account, LocalDate endDate) {
        // Implementation depends on the actual transaction structure
        // This is a placeholder for the actual balance calculation logic
        return account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
    }

    /**
     * Calculate account balance for a specific period.
     * @param account the account
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @return account balance for the period
     */
    private BigDecimal calculateAccountBalancePeriod(Account account, LocalDate startDate, LocalDate endDate) {
        // Implementation depends on the actual transaction structure
        // This is a placeholder for the actual period balance calculation logic
        return BigDecimal.ZERO;
    }
}
