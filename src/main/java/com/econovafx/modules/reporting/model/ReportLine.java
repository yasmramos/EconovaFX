package com.econovafx.modules.reporting.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.math.BigDecimal;

/**
 * Represents a line item in a financial report.
 * This is an embeddable class used as part of FinancialReport's element collection.
 */
@Embeddable
public class ReportLine {

    @Column(name = "account_code")
    private String accountCode;

    @Column(name = "description")
    private String description;

    @Column(name = "debit_amount", precision = 19, scale = 4)
    private BigDecimal debitAmount;

    @Column(name = "credit_amount", precision = 19, scale = 4)
    private BigDecimal creditAmount;

    @Column(name = "balance", precision = 19, scale = 4)
    private BigDecimal balance;

    public ReportLine() {}

    public ReportLine(String accountCode, String description, BigDecimal debitAmount, BigDecimal creditAmount, BigDecimal balance) {
        this.accountCode = accountCode;
        this.description = description;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
        this.balance = balance;
    }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDebitAmount() { return debitAmount; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }
    public BigDecimal getCreditAmount() { return creditAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
