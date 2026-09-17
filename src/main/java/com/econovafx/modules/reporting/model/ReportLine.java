package com.econovafx.modules.reporting.model;

/**
 * Represents a line item in a financial report.
 * This is a simple POJO used for report data transfer, not a persistent entity.
 */
public class ReportLine {
    
    private String accountCode;
    private String description;
    
    private Double debitAmount;
    
    private Double creditAmount;
    
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
