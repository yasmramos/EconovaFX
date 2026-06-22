package com.econovafx.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Transaction Entry entity (individual debit/credit entry)
 */
@Entity
@Table(name = "transaction_entries")
public class TransactionEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(length = 255)
    private String description;

    public TransactionEntry() {
    }

    public TransactionEntry(Transaction transaction, Account account,
            BigDecimal debitAmount, BigDecimal creditAmount) {
        this.transaction = transaction;
        this.account = account;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
    }

    // Getters and Setters
    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getNetAmount() {
        return debitAmount.subtract(creditAmount);
    }
}
