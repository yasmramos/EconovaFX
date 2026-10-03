package com.econovafx.modules.payroll.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Payroll detail entity for individual employee payroll calculations.
 * Contains earnings, deductions, and net pay for one employee in a batch.
 */
@Entity
@Table(name = "payroll_details")
public class PayrollDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private PayrollBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "working_days")
    private Integer workingDays = 0;

    @Column(name = "worked_days")
    private Integer workedDays = 0;

    @Column(name = "overtime_hours", columnDefinition = "DOUBLE")
    private Double overtimeHours = 0.0;

    @Column(name = "leave_days", columnDefinition = "DOUBLE")
    private Double leaveDays = 0.0;

    @Column(name = "gross_salary", precision = 12, scale = 2)
    private BigDecimal grossSalary = BigDecimal.ZERO;

    @Column(name = "base_salary", precision = 12, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "total_earnings", precision = 12, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_salary", precision = 12, scale = 2)
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(name = "social_security_amount", precision = 12, scale = 2)
    private BigDecimal socialSecurityAmount = BigDecimal.ZERO;

    @Column(name = "tax_withheld", precision = 12, scale = 2)
    private BigDecimal taxWithheld = BigDecimal.ZERO;

    @Column(name = "other_deductions", precision = 12, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "bonus_amount", precision = 12, scale = 2)
    private BigDecimal bonusAmount = BigDecimal.ZERO;

    @Column(name = "allowance_amount", precision = 12, scale = 2)
    private BigDecimal allowanceAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "detail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollConceptValue> conceptValues = new ArrayList<>();

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_paid")
    private boolean paid = false;

    @Column(name = "payment_date", columnDefinition = "DATE")
    private java.time.LocalDate paymentDate;

    // Constructors

    public PayrollDetail() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PayrollBatch getBatch() {
        return batch;
    }

    public void setBatch(PayrollBatch batch) {
        this.batch = batch;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Integer workingDays) {
        this.workingDays = workingDays;
    }

    public Integer getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(Integer workedDays) {
        this.workedDays = workedDays;
    }

    public Double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(Double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public Double getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(Double leaveDays) {
        this.leaveDays = leaveDays;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public BigDecimal getSocialSecurityAmount() {
        return socialSecurityAmount;
    }

    public void setSocialSecurityAmount(BigDecimal socialSecurityAmount) {
        this.socialSecurityAmount = socialSecurityAmount;
    }

    public BigDecimal getTaxWithheld() {
        return taxWithheld;
    }

    public void setTaxWithheld(BigDecimal taxWithheld) {
        this.taxWithheld = taxWithheld;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public BigDecimal getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(BigDecimal bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public BigDecimal getAllowanceAmount() {
        return allowanceAmount;
    }

    public void setAllowanceAmount(BigDecimal allowanceAmount) {
        this.allowanceAmount = allowanceAmount;
    }

    public List<PayrollConceptValue> getConceptValues() {
        return conceptValues;
    }

    public void setConceptValues(List<PayrollConceptValue> conceptValues) {
        this.conceptValues = conceptValues;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public java.time.LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(java.time.LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    /**
     * Add a concept value to this detail.
     */
    public void addConceptValue(PayrollConceptValue conceptValue) {
        conceptValues.add(conceptValue);
        conceptValue.setDetail(this);
    }

    /**
     * Remove a concept value from this detail.
     */
    public void removeConceptValue(PayrollConceptValue conceptValue) {
        conceptValues.remove(conceptValue);
        conceptValue.setDetail(null);
    }

    /**
     * Calculate net salary: gross - deductions.
     */
    public void calculateNetSalary() {
        if (grossSalary == null) {
            grossSalary = BigDecimal.ZERO;
        }
        if (totalDeductions == null) {
            totalDeductions = BigDecimal.ZERO;
        }
        netSalary = grossSalary.subtract(totalDeductions);
    }

    /**
     * Recalculate totals from concept values.
     */
    public void recalculateFromConcepts() {
        totalEarnings = BigDecimal.ZERO;
        totalDeductions = BigDecimal.ZERO;
        socialSecurityAmount = BigDecimal.ZERO;
        taxWithheld = BigDecimal.ZERO;
        otherDeductions = BigDecimal.ZERO;
        bonusAmount = BigDecimal.ZERO;
        allowanceAmount = BigDecimal.ZERO;

        for (PayrollConceptValue conceptValue : conceptValues) {
            BigDecimal amount = conceptValue.getAmount() != null ? conceptValue.getAmount() : BigDecimal.ZERO;
            
            switch (conceptValue.getConcept().getConceptType()) {
                case EARNING:
                case ALLOWANCE:
                    totalEarnings = totalEarnings.add(amount);
                    if (conceptValue.getConcept().getConceptType() == PayrollConcept.ConceptType.ALLOWANCE) {
                        allowanceAmount = allowanceAmount.add(amount);
                    }
                    break;
                case DEDUCTION:
                    totalDeductions = totalDeductions.add(amount);
                    if (conceptValue.getConcept().isSocialSecurity()) {
                        socialSecurityAmount = socialSecurityAmount.add(amount);
                    } else {
                        otherDeductions = otherDeductions.add(amount);
                    }
                    break;
                case WITHHOLDING:
                    totalDeductions = totalDeductions.add(amount);
                    taxWithheld = taxWithheld.add(amount);
                    break;
                default:
                    break;
            }
        }

        grossSalary = baseSalary != null ? baseSalary.add(totalEarnings) : totalEarnings;
        calculateNetSalary();
    }

    /**
     * Nested entity for storing concept values.
     */
    @Entity
    @Table(name = "payroll_concept_values")
    public static class PayrollConceptValue {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "detail_id", nullable = false)
        private PayrollDetail detail;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "concept_id", nullable = false)
        private PayrollConcept concept;

        @Column(name = "amount", precision = 12, scale = 2)
        private BigDecimal amount = BigDecimal.ZERO;

        @Column(name = "quantity", columnDefinition = "DOUBLE")
        private Double quantity;

        @Column(name = "rate", precision = 12, scale = 4)
        private BigDecimal rate;

        @Column(name = "formula_result")
        private String formulaResult;

        @Column(name = "notes")
        private String notes;

        // Getters and Setters

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public PayrollDetail getDetail() {
            return detail;
        }

        public void setDetail(PayrollDetail detail) {
            this.detail = detail;
        }

        public PayrollConcept getConcept() {
            return concept;
        }

        public void setConcept(PayrollConcept concept) {
            this.concept = concept;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Double getQuantity() {
            return quantity;
        }

        public void setQuantity(Double quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getRate() {
            return rate;
        }

        public void setRate(BigDecimal rate) {
            this.rate = rate;
        }

        public String getFormulaResult() {
            return formulaResult;
        }

        public void setFormulaResult(String formulaResult) {
            this.formulaResult = formulaResult;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
