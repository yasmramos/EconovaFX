package com.econovafx.modules.cashbank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cash Register entity for petty cash management.
 * Complies with Resolution 340/2004 requirements for cash control.
 */
@Entity
@Table(name = "cash_registers")
public class CashRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String registerName;

    @Column(length = 50, unique = true)
    private String registerCode;

    @Column(nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private BigDecimal maximumBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate openingDate;

    @Column(length = 255)
    private String responsiblePerson;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegisterName() { return registerName; }
    public void setRegisterName(String registerName) { this.registerName = registerName; }

    public String getRegisterCode() { return registerCode; }
    public void setRegisterCode(String registerCode) { this.registerCode = registerCode; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getMaximumBalance() { return maximumBalance; }
    public void setMaximumBalance(BigDecimal maximumBalance) { this.maximumBalance = maximumBalance; }

    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public void setMinimumBalance(BigDecimal minimumBalance) { this.minimumBalance = minimumBalance; }

    public LocalDate getOpeningDate() { return openingDate; }
    public void setOpeningDate(LocalDate openingDate) { this.openingDate = openingDate; }

    public String getResponsiblePerson() { return responsiblePerson; }
    public void setResponsiblePerson(String responsiblePerson) { this.responsiblePerson = responsiblePerson; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
