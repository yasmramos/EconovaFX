package com.econovafx.modules.reporting.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

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

    @Column(name = "debit_amount", columnDefinition = "DOUBLE")
    private Double debitAmount;

    @Column(name = "credit_amount", columnDefinition = "DOUBLE")
    private Double creditAmount;

    @Column(name = "balance", columnDefinition = "DOUBLE")
    private Double balance;

    public ReportLine() {}

    public ReportLine(String accountCode, String description, Double debitAmount, Double creditAmount, Double balance) {
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
    public Double getDebitAmount() { return debitAmount; }
    public void setDebitAmount(Double debitAmount) { this.debitAmount = debitAmount; }
    public Double getCreditAmount() { return creditAmount; }
    public void setCreditAmount(Double creditAmount) { this.creditAmount = creditAmount; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}
