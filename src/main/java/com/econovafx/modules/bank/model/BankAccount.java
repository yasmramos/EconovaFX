package com.econovafx.modules.bank.model;

import com.econovafx.modules.core.model.BaseEntity;
import io.ebean.annotation.DbEnumValue;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entity representing a Bank Account according to Resolution 340/2004.
 */
@Entity
@Table(name = "bank_account")
public class BankAccount extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "bank_entity", nullable = false)
    private String bankEntity;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "accounting_account")
    private String accountingAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;

    public BankAccount() {
        super();
        this.balance = BigDecimal.ZERO;
        this.active = true;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankEntity() {
        return bankEntity;
    }

    public void setBankEntity(String bankEntity) {
        this.bankEntity = bankEntity;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountingAccount() {
        return accountingAccount;
    }

    public void setAccountingAccount(String accountingAccount) {
        this.accountingAccount = accountingAccount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
