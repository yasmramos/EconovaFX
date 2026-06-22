package com.econovafx.model;

import com.econovafx.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entity representing a Cash Box according to Resolution 340/2004.
 */
@Entity
@Table(name = "cash_box")
public class CashBox extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "accounting_account")
    private String accountingAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean open = true;

    public CashBox() {
        super();
        this.balance = BigDecimal.ZERO;
        this.open = true;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getAccountingAccount() { return accountingAccount; }
    public void setAccountingAccount(String accountingAccount) { this.accountingAccount = accountingAccount; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Boolean getOpen() { return open; }
    public void setOpen(Boolean open) { this.open = open; }
}
