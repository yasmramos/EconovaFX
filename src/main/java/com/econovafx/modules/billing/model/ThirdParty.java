package com.econovafx.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Third Party entity for managing Customers and Suppliers
 */
@Entity
@Table(name = "third_parties")
public class ThirdParty extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String identificationNumber; // RUC, DNI, NIT, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThirdPartyType type = ThirdPartyType.CUSTOMER;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String address;

    private String city;

    private String country = "Perú";

    @Column(name = "tax_id")
    private String taxId; // RUC or tax identification number

    @Column(name = "credit_limit", precision = 19, scale = 4)
    private Double creditLimit = 0.0;

    @Column(name = "current_balance", precision = 19, scale = 4)
    private Double currentBalance = 0.0;

    @Column(name = "payment_days")
    private Integer paymentDays = 30;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "notes", length = 2000)
    @Lob
    private String notes;

    @OneToMany(mappedBy = "thirdParty", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    

    public enum ThirdPartyType {
        CUSTOMER,      // Cliente
        SUPPLIER,      // Proveedor
        BOTH           // Both (Cliente y Proveedor)
    }

    

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public ThirdPartyType getType() {
        return type;
    }

    public void setType(ThirdPartyType type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Integer getPaymentDays() {
        return paymentDays;
    }

    public void setPaymentDays(Integer paymentDays) {
        this.paymentDays = paymentDays;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // `createdAt`, `updatedAt` and `isActive` are inherited from BaseEntity

    @Override
    public String toString() {
        return name + " (" + identificationNumber + ")";
    }
}
