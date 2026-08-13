package com.econovafx.modules.payroll.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Payroll concept entity for salary components.
 * Represents earnings, deductions, and other payroll items.
 */
@Entity
@Table(name = "payroll_concepts")
public class PayrollConcept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concept_code", unique = true, nullable = false)
    private String conceptCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "concept_type")
    @Enumerated(EnumType.STRING)
    private ConceptType conceptType;

    @Column(name = "calculation_type")
    @Enumerated(EnumType.STRING)
    private CalculationType calculationType;

    @Column(name = "formula")
    private String formula;

    @Column(name = "fixed_amount", precision = 12, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "percentage", precision = 5, scale = 4)
    private BigDecimal percentage;

    @Column(name = "is_taxable")
    private boolean taxable = true;

    @Column(name = "is_social_security")
    private boolean socialSecurity = false;

    @Column(name = "priority_order")
    private Integer priorityOrder = 100;

    @Column(name = "is_active")
    private boolean active = true;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
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

    public ConceptType getConceptType() {
        return conceptType;
    }

    public void setConceptType(ConceptType conceptType) {
        this.conceptType = conceptType;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public boolean isTaxable() {
        return taxable;
    }

    public void setTaxable(boolean taxable) {
        this.taxable = taxable;
    }

    public boolean isSocialSecurity() {
        return socialSecurity;
    }

    public void setSocialSecurity(boolean socialSecurity) {
        this.socialSecurity = socialSecurity;
    }

    public Integer getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(Integer priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Check if this concept is applicable to a specific employee.
     * 
     * @param employee The employee to check
     * @return true if the concept applies to the employee
     */
    public boolean isApplicableToEmployee(Employee employee) {
        // Basic implementation - can be extended with employee-specific rules
        if (!this.active) {
            return false;
        }
        
        // Check if employee meets minimum requirements (if any)
        // This can be extended based on employee category, department, etc.
        return true;
    }

    /**
     * Concept type enumeration.
     */
    public enum ConceptType {
        EARNING,      // Salary, bonus, overtime
        DEDUCTION,    // Tax, social security, loans
        ALLOWANCE,    // Transportation, meal allowance
        WITHHOLDING   // Income tax withholding
    }

    /**
     * Calculation type enumeration.
     */
    public enum CalculationType {
        FIXED_AMOUNT,     // Fixed monetary value
        PERCENTAGE,       // Percentage of base salary
        FORMULA,          // Custom formula evaluation
        HOURLY_RATE,      // Based on hours worked
        UNIT_BASED        // Based on production units
    }
}
