package com.econovafx.modules.reporting.service;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.reporting.model.FinancialReport;
import com.econovafx.modules.reporting.model.ReportLine;
import com.econovafx.modules.reporting.repository.FinancialReportRepository;
import io.ebean.annotation.Transactional;
import io.avaje.inject.Component;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for generating financial reports required by Resolution 340/2004.
 * Implements Balance Sheet, Income Statement, Cash Flow, and Trial Balance
 * generation.
 */
@Component
public class FinancialReportingService {

    private final FinancialReportRepository reportRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Lower bound for cumulative balance queries. LocalDate.MIN overflows the
    // JDBC DATE mapping, so use an early but representable date.
    private static final LocalDate EPOCH = LocalDate.of(1900, 1, 1);

    @Inject
    public FinancialReportingService(FinancialReportRepository reportRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {
        this.reportRepository = reportRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generate a Balance Sheet report.
     *
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
        List<Account> assetAccounts = accountRepository.findByType(AccountType.ASSET);
        BigDecimal totalAssets = BigDecimal.ZERO;

        for (Account account : assetAccounts) {
            BigDecimal balance = calculateAccountBalance(account, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                        account.getCode(),
                        account.getName(),
                        balance.compareTo(BigDecimal.ZERO) > 0 ? balance : BigDecimal.ZERO,
                        balance.compareTo(BigDecimal.ZERO) < 0 ? balance.abs() : BigDecimal.ZERO,
                        balance
                ));
                totalAssets = totalAssets.add(balance);
            }
        }

        // Add total assets line
        lines.add(new ReportLine("", "TOTAL ASSETS", totalAssets, BigDecimal.ZERO, totalAssets));

        // Get liability and equity accounts
        List<Account> liabilityAccounts = accountRepository.findByType(AccountType.LIABILITY);
        List<Account> equityAccounts = accountRepository.findByType(AccountType.EQUITY);

        BigDecimal totalLiabilities = BigDecimal.ZERO;
        for (Account account : liabilityAccounts) {
            BigDecimal balance = calculateAccountBalance(account, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                        account.getCode(),
                        account.getName(),
                        BigDecimal.ZERO,
                        balance,
                        balance.negate()
                ));
                totalLiabilities = totalLiabilities.add(balance);
            }
        }

        BigDecimal totalEquity = BigDecimal.ZERO;
        for (Account account : equityAccounts) {
            BigDecimal balance = calculateAccountBalance(account, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                        account.getCode(),
                        account.getName(),
                        BigDecimal.ZERO,
                        balance,
                        balance.negate()
                ));
                totalEquity = totalEquity.add(balance);
            }
        }

        lines.add(new ReportLine("", "TOTAL LIABILITIES", BigDecimal.ZERO, totalLiabilities, totalLiabilities.negate()));
        lines.add(new ReportLine("", "TOTAL EQUITY", BigDecimal.ZERO, totalEquity, totalEquity.negate()));
        lines.add(new ReportLine("", "TOTAL LIABILITIES + EQUITY", BigDecimal.ZERO, totalLiabilities.add(totalEquity),
                totalLiabilities.add(totalEquity).negate()));

        report.setLines(lines);
        report.setGeneratedData("Balance Sheet generated successfully");

        return reportRepository.save(report);
    }

    /**
     * Generate an Income Statement report.
     *
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
        List<Account> revenueAccounts = accountRepository.findByType(AccountType.REVENUE);
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Account account : revenueAccounts) {
            BigDecimal balance = calculateAccountBalancePeriod(account, startDate, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                        account.getCode(),
                        account.getName(),
                        BigDecimal.ZERO,
                        balance,
                        balance.negate()
                ));
                totalRevenue = totalRevenue.add(balance);
            }
        }

        lines.add(new ReportLine("", "TOTAL REVENUE", BigDecimal.ZERO, totalRevenue, totalRevenue.negate()));

        // Get expense accounts
        List<Account> expenseAccounts = accountRepository.findByType(AccountType.EXPENSE);
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Account account : expenseAccounts) {
            BigDecimal balance = calculateAccountBalancePeriod(account, startDate, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ReportLine(
                        account.getCode(),
                        account.getName(),
                        balance,
                        BigDecimal.ZERO,
                        balance
                ));
                totalExpenses = totalExpenses.add(balance);
            }
        }

        lines.add(new ReportLine("", "TOTAL EXPENSES", totalExpenses, BigDecimal.ZERO, totalExpenses));

        BigDecimal netIncome = totalRevenue.subtract(totalExpenses);
        lines.add(new ReportLine("", "NET INCOME/(LOSS)",
                netIncome.compareTo(BigDecimal.ZERO) < 0 ? netIncome.abs() : BigDecimal.ZERO,
                netIncome.compareTo(BigDecimal.ZERO) >= 0 ? netIncome : BigDecimal.ZERO,
                netIncome));

        report.setLines(lines);
        report.setGeneratedData("Income Statement generated successfully");

        return reportRepository.save(report);
    }

    /**
     * Generate a Trial Balance report.
     *
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
        List<Account> allAccounts = accountRepository.findAll();

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Account account : allAccounts) {
            BigDecimal balance = calculateAccountBalance(account, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal debit = balance.compareTo(BigDecimal.ZERO) > 0 ? balance : BigDecimal.ZERO;
                BigDecimal credit = balance.compareTo(BigDecimal.ZERO) < 0 ? balance.abs() : BigDecimal.ZERO;

                lines.add(new ReportLine(
                        account.getCode(),
                        account.getName(),
                        debit,
                        credit,
                        balance
                ));

                totalDebits = totalDebits.add(debit);
                totalCredits = totalCredits.add(credit);
            }
        }

        lines.add(new ReportLine("", "TOTALS", totalDebits, totalCredits,
                totalDebits.subtract(totalCredits)));

        report.setLines(lines);
        report.setGeneratedData("Trial Balance generated successfully - Debits: " + totalDebits + ", Credits: " + totalCredits);

        return reportRepository.save(report);
    }

    /**
     * Generate a Cash Flow Statement report.
     *
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

        // Find cash and bank accounts - filter by code prefix for cash accounts
        List<Account> allAccounts = accountRepository.findAll();
        BigDecimal operatingCashFlow = BigDecimal.ZERO;
        BigDecimal investingCashFlow = BigDecimal.ZERO;
        BigDecimal financingCashFlow = BigDecimal.ZERO;

        // Simplified cash flow calculation based on transaction analysis
        List<Transaction> transactions = transactionRepository.findByDateRange(startDate, endDate);

        for (Transaction transaction : transactions) {
            // Analyze transaction type and categorize cash flow
            // This is a simplified implementation - real implementation would need more detailed analysis
            if (transaction.getDescription().toLowerCase().contains("operating")) {
                for (TransactionEntry entry : transaction.getEntries()) {
                    operatingCashFlow = operatingCashFlow.add(entry.getDebitAmount().subtract(entry.getCreditAmount()));
                }
            } else if (transaction.getDescription().toLowerCase().contains("investing")) {
                for (TransactionEntry entry : transaction.getEntries()) {
                    investingCashFlow = investingCashFlow.add(entry.getDebitAmount().subtract(entry.getCreditAmount()));
                }
            } else if (transaction.getDescription().toLowerCase().contains("financing")) {
                for (TransactionEntry entry : transaction.getEntries()) {
                    financingCashFlow = financingCashFlow.add(entry.getDebitAmount().subtract(entry.getCreditAmount()));
                }
            }
        }

        lines.add(new ReportLine("", "Cash Flow from Operating Activities", operatingCashFlow, BigDecimal.ZERO, operatingCashFlow));
        lines.add(new ReportLine("", "Cash Flow from Investing Activities", investingCashFlow, BigDecimal.ZERO, investingCashFlow));
        lines.add(new ReportLine("", "Cash Flow from Financing Activities", financingCashFlow, BigDecimal.ZERO, financingCashFlow));

        BigDecimal netCashFlow = operatingCashFlow.add(investingCashFlow).add(financingCashFlow);
        lines.add(new ReportLine("", "NET CASH FLOW", netCashFlow, BigDecimal.ZERO, netCashFlow));

        report.setLines(lines);
        report.setGeneratedData("Cash Flow Statement generated successfully");

        return reportRepository.save(report);
    }

    /**
     * Generate a Statement of Changes in Equity report.
     * Resolution 340/2004 requires this statement alongside the balance sheet
     * and income statement. It shows the opening equity, the result of the
     * period (net income) and the resulting closing equity.
     *
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @param fiscalYear fiscal year
     * @return generated FinancialReport entity
     */
    @Transactional(readOnly = true)
    public FinancialReport generateChangesInEquityStatement(LocalDate startDate, LocalDate endDate, Integer fiscalYear) {
        FinancialReport report = new FinancialReport();
        report.setReportType(FinancialReport.ReportType.CHANGES_IN_EQUITY);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setFiscalYear(fiscalYear);
        report.setFinal(false);

        List<ReportLine> lines = new ArrayList<>();

        // Opening equity: equity accounts balance accumulated before the period.
        BigDecimal openingEquity = BigDecimal.ZERO;
        for (Account account : accountRepository.findByType(AccountType.EQUITY)) {
            openingEquity = openingEquity.add(calculateAccountBalanceBefore(account, startDate));
        }
        lines.add(new ReportLine("", "Opening Equity", BigDecimal.ZERO, openingEquity, openingEquity.negate()));

        // Result of the period: revenue minus expenses within the period.
        BigDecimal periodResult = BigDecimal.ZERO;
        for (Account account : accountRepository.findByType(AccountType.REVENUE)) {
            periodResult = periodResult.add(calculateAccountBalancePeriod(account, startDate, endDate));
        }
        for (Account account : accountRepository.findByType(AccountType.EXPENSE)) {
            periodResult = periodResult.subtract(calculateAccountBalancePeriod(account, startDate, endDate));
        }
        lines.add(new ReportLine("", "Result of the Period", BigDecimal.ZERO, periodResult, periodResult.negate()));

        BigDecimal closingEquity = openingEquity.add(periodResult);
        lines.add(new ReportLine("", "Closing Equity", BigDecimal.ZERO, closingEquity, closingEquity.negate()));

        report.setLines(lines);
        report.setGeneratedData("Statement of Changes in Equity generated successfully");

        return reportRepository.save(report);
    }

    /**
     * Calculate the equity balance accumulated strictly before a date.
     *
     * @param account the equity account
     * @param beforeDate exclusive upper bound
     * @return accumulated balance before {@code beforeDate}
     */
    private BigDecimal calculateAccountBalanceBefore(Account account, LocalDate beforeDate) {
        List<Transaction> transactions = transactionRepository.findPostedByDateRange(EPOCH, beforeDate.minusDays(1));
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            for (TransactionEntry entry : transaction.getEntries()) {
                if (entry.getAccount().getId().equals(account.getId())) {
                    balance = balance.add(entry.getCreditAmount().subtract(entry.getDebitAmount()));
                }
            }
        }
        return balance;
    }

    /**
     * Finalize a report, making it immutable.
     *
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
     * Calculate account balance up to a specific date. Resolution 340/2004
     * compliance: Calculates real balance from posted transactions.
     *
     * @param account the account
     * @param endDate end date for calculation
     * @return account balance
     */
    private BigDecimal calculateAccountBalance(Account account, LocalDate endDate) {
        // Get all posted transactions up to endDate
        List<Transaction> transactions = transactionRepository.findPostedByDateRange(
                EPOCH, endDate);

        BigDecimal balance = BigDecimal.ZERO;
        AccountType type = account.getType();

        for (Transaction transaction : transactions) {
            for (TransactionEntry entry : transaction.getEntries()) {
                if (entry.getAccount().getId().equals(account.getId())) {
                    // Apply debit amounts
                    if (entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                        if (type == AccountType.ASSET || type == AccountType.EXPENSE) {
                            balance = balance.add(entry.getDebitAmount());
                        } else {
                            balance = balance.subtract(entry.getDebitAmount());
                        }
                    }

                    // Apply credit amounts
                    if (entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                        if (type == AccountType.LIABILITY || type == AccountType.EQUITY
                                || type == AccountType.REVENUE) {
                            balance = balance.add(entry.getCreditAmount());
                        } else {
                            balance = balance.subtract(entry.getCreditAmount());
                        }
                    }
                }
            }
        }

        return balance;
    }

    /**
     * Calculate account balance for a specific period. Resolution 340/2004
     * compliance: Calculates balance from posted transactions within period.
     *
     * @param account the account
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @return account balance for the period
     */
    private BigDecimal calculateAccountBalancePeriod(Account account, LocalDate startDate, LocalDate endDate) {
        // Get all posted transactions within the period
        List<Transaction> transactions = transactionRepository.findPostedByDateRange(startDate, endDate);

        BigDecimal balance = BigDecimal.ZERO;
        AccountType type = account.getType();

        for (Transaction transaction : transactions) {
            for (TransactionEntry entry : transaction.getEntries()) {
                if (entry.getAccount().getId().equals(account.getId())) {
                    // Apply debit amounts
                    if (entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                        if (type == AccountType.ASSET || type == AccountType.EXPENSE) {
                            balance = balance.add(entry.getDebitAmount());
                        } else {
                            balance = balance.subtract(entry.getDebitAmount());
                        }
                    }

                    // Apply credit amounts
                    if (entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                        if (type == AccountType.LIABILITY || type == AccountType.EQUITY
                                || type == AccountType.REVENUE) {
                            balance = balance.add(entry.getCreditAmount());
                        } else {
                            balance = balance.subtract(entry.getCreditAmount());
                        }
                    }
                }
            }
        }

        return balance;
    }

    /**
     * Get all reports.
     *
     * @return list of all reports
     */
    public List<FinancialReport> getAllReports() {
        return reportRepository.findAll();
    }

    /**
     * Get report by ID.
     *
     * @param id the report ID
     * @return optional containing the report
     */
    public Optional<FinancialReport> getReportById(Long id) {
        return reportRepository.findById(id);
    }
}
