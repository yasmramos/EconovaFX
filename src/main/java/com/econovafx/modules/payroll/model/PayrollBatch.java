package com.econovafx.modules.payroll.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Payroll batch entity for processing payroll runs.
 * Groups multiple payroll details for a specific period.
 */
@Entity
@Table(name = "payroll_batches")
public class PayrollBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_code", unique = true, nullable = false)
    private String batchCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private PayrollPeriod period;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BatchStatus status = BatchStatus.DRAFT;

    @Column(name = "creation_date", nullable = false, columnDefinition = "DATE")
    private LocalDate creationDate = LocalDate.now();

    @Column(name = "processed_date", columnDefinition = "DATE")
    private LocalDate processedDate;

    @Column(name = "payment_date", columnDefinition = "DATE")
    private LocalDate paymentDate;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollDetail> details = new ArrayList<>();

    @Column(name = "total_employees")
    private Integer totalEmployees = 0;

    @Column(name = "total_gross", precision = 15, scale = 2)
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(name = "total_deductions", precision = 15, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "total_net", precision = 15, scale = 2)
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(name = "total_social_security", precision = 15, scale = 2)
    private BigDecimal totalSocialSecurity = BigDecimal.ZERO;

    @Column(name = "total_tax_withheld", precision = 15, scale = 2)
    private BigDecimal totalTaxWithheld = BigDecimal.ZERO;

    @Column(name = "accounting_entry_posted")
    private boolean accountingEntryPosted = false;

    @Column(name = "accounting_entry_number")
    private String accountingEntryNumber;

    @Column(name = "notes")
    private String notes;

    // Constructors

    public PayrollBatch() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public PayrollPeriod getPeriod() {
        return period;
    }

    public void setPeriod(PayrollPeriod period) {
        this.period = period;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDate processedDate) {
        this.processedDate = processedDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public List<PayrollDetail> getDetails() {
        return details;
    }

    /**
     * Get payroll details (alias for getDetails).
     */
    public List<PayrollDetail> getPayrollDetails() {
        return details;
    }

    public void setDetails(List<PayrollDetail> details) {
        this.details = details;
    }

    public Integer getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Integer totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public BigDecimal getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(BigDecimal totalGross) {
        this.totalGross = totalGross;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getTotalNet() {
        return totalNet;
    }

    public void setTotalNet(BigDecimal totalNet) {
        this.totalNet = totalNet;
    }

    public BigDecimal getTotalSocialSecurity() {
        return totalSocialSecurity;
    }

    public void setTotalSocialSecurity(BigDecimal totalSocialSecurity) {
        this.totalSocialSecurity = totalSocialSecurity;
    }

    public BigDecimal getTotalTaxWithheld() {
        return totalTaxWithheld;
    }

    public void setTotalTaxWithheld(BigDecimal totalTaxWithheld) {
        this.totalTaxWithheld = totalTaxWithheld;
    }

    public boolean isAccountingEntryPosted() {
        return accountingEntryPosted;
    }

    public void setAccountingEntryPosted(boolean accountingEntryPosted) {
        this.accountingEntryPosted = accountingEntryPosted;
    }

    public String getAccountingEntryNumber() {
        return accountingEntryNumber;
    }

    public void setAccountingEntryNumber(String accountingEntryNumber) {
        this.accountingEntryNumber = accountingEntryNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Add a payroll detail to the batch.
     */
    public void addDetail(PayrollDetail detail) {
        details.add(detail);
        detail.setBatch(this);
        recalculateTotals();
    }

    /**
     * Remove a payroll detail from the batch.
     */
    public void removeDetail(PayrollDetail detail) {
        details.remove(detail);
        detail.setBatch(null);
        recalculateTotals();
    }

    /**
     * Recalculate all totals based on details.
     */
    public void recalculateTotals() {
        totalEmployees = details.size();
        totalGross = BigDecimal.ZERO;
        totalDeductions = BigDecimal.ZERO;
        totalNet = BigDecimal.ZERO;
        totalSocialSecurity = BigDecimal.ZERO;
        totalTaxWithheld = BigDecimal.ZERO;

        for (PayrollDetail detail : details) {
            if (detail.getGrossSalary() != null) {
                totalGross = totalGross.add(detail.getGrossSalary());
            }
            if (detail.getTotalDeductions() != null) {
                totalDeductions = totalDeductions.add(detail.getTotalDeductions());
            }
            if (detail.getNetSalary() != null) {
                totalNet = totalNet.add(detail.getNetSalary());
            }
            if (detail.getSocialSecurityAmount() != null) {
                totalSocialSecurity = totalSocialSecurity.add(detail.getSocialSecurityAmount());
            }
            if (detail.getTaxWithheld() != null) {
                totalTaxWithheld = totalTaxWithheld.add(detail.getTaxWithheld());
            }
        }
    }

    /**
     * Batch status enumeration.
     */
    public enum BatchStatus {
        DRAFT,          // Being prepared
        CALCULATED,     // Calculations complete
        APPROVED,       // Approved for payment
        PROCESSED,      // Payment processed
        POSTED,         // Accounting entry posted
        CANCELLED       // Cancelled batch
    }
}
