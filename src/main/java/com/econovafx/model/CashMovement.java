package com.econovafx.model;

import com.econovafx.model.BaseEntity;
import io.ebean.annotation.DbEnumValue;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a Cash Movement (Income, Expense, Transfer).
 */
@Entity
@Table(name = "cash_movement")
public class CashMovement extends BaseEntity {

    public enum MovementType {
        INCOME, EXPENSE, TRANSFER
    }

    public enum Status {
        PENDING, POSTED, CANCELLED
    }

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_account_id", columnDefinition = "BIGINT")
    private Long sourceAccountId;

    @Column(name = "destination_account_id", columnDefinition = "BIGINT")
    private Long destinationAccountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "contra_account")
    private String contraAccount;

    @Column(name = "cost_center")
    private String costCenter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private Boolean reconciled = false;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "posted_by")
    private String postedBy;

    public CashMovement() {
        super();
        this.status = Status.PENDING;
        this.reconciled = false;
    }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }
    public Long getDestinationAccountId() { return destinationAccountId; }
    public void setDestinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getContraAccount() { return contraAccount; }
    public void setContraAccount(String contraAccount) { this.contraAccount = contraAccount; }
    public String getCostCenter() { return costCenter; }
    public void setCostCenter(String costCenter) { this.costCenter = costCenter; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Boolean getReconciled() { return reconciled; }
    public void setReconciled(Boolean reconciled) { this.reconciled = reconciled; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
}
