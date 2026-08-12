package com.econovafx.modules.payroll.service;

import com.econovafx.modules.payroll.model.*;
import com.econovafx.modules.payroll.repository.EmployeeRepository;
import com.econovafx.modules.payroll.repository.PayrollConceptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for payroll processing and calculations.
 * Handles salary calculations, deductions, bonuses, and payroll batch processing.
 */
@Service
public class PayrollService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollConceptRepository conceptRepository;

    /**
     * Calculate gross salary for an employee in a period.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateGrossSalary(Employee employee, PayrollPeriod period) {
        if (employee.getBaseSalary() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseSalary = employee.getBaseSalary();
        
        // Prorate if necessary based on hire/termination date
        if (employee.getHireDate() != null && employee.getHireDate().isAfter(period.getStartDate())) {
            baseSalary = calculateProratedSalary(baseSalary, period.getDaysInPeriod(), 
                (int) ChronoUnit.DAYS.between(employee.getHireDate(), period.getEndDate()));
        }

        return baseSalary;
    }

    /**
     * Calculate deductions based on active concepts.
     */
    @Transactional(readOnly = true)
    public List<PayrollDetail.PayrollConceptValue> calculateDeductions(Employee employee, 
                                                                        BigDecimal grossSalary, 
                                                                        PayrollPeriod period) {
        List<PayrollDetail.PayrollConceptValue> conceptValues = new ArrayList<>();
        List<PayrollConcept> concepts = conceptRepository.findAllActiveOrderedByPriority();

        for (PayrollConcept concept : concepts) {
            if (concept.getConceptType() == PayrollConcept.ConceptType.EARNING || 
                concept.getConceptType() == PayrollConcept.ConceptType.ALLOWANCE) {
                continue; // Skip earnings, only process deductions
            }

            PayrollDetail.PayrollConceptValue conceptValue = new PayrollDetail.PayrollConceptValue();
            conceptValue.setConcept(concept);

            BigDecimal amount = BigDecimal.ZERO;

            switch (concept.getCalculationType()) {
                case FIXED_AMOUNT:
                    amount = concept.getFixedAmount() != null ? concept.getFixedAmount() : BigDecimal.ZERO;
                    break;
                case PERCENTAGE:
                    if (concept.getPercentage() != null && grossSalary != null) {
                        amount = grossSalary.multiply(concept.getPercentage());
                    }
                    break;
                case FORMULA:
                    // Formula evaluation would be handled by PayrollFormulaEvaluator
                    amount = evaluateFormula(concept.getFormula(), employee, grossSalary);
                    break;
                default:
                    amount = BigDecimal.ZERO;
            }

            conceptValue.setAmount(amount);
            conceptValues.add(conceptValue);
        }

        return conceptValues;
    }

    /**
     * Calculate net salary from gross and deductions.
     */
    public BigDecimal calculateNetSalary(BigDecimal grossSalary, BigDecimal totalDeductions) {
        if (grossSalary == null) grossSalary = BigDecimal.ZERO;
        if (totalDeductions == null) totalDeductions = BigDecimal.ZERO;
        return grossSalary.subtract(totalDeductions);
    }

    /**
     * Create a payroll batch for a period.
     */
    @Transactional
    public PayrollBatch createPayrollBatch(PayrollPeriod period) {
        List<Employee> employees = employeeRepository.findActiveEmployees();
        
        if (employees.isEmpty()) {
            throw new IllegalStateException("No active employees found for payroll processing");
        }

        PayrollBatch batch = new PayrollBatch();
        batch.setBatchCode("BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        batch.setPeriod(period);
        batch.setName("Payroll " + period.getName());
        batch.setStatus(PayrollBatch.BatchStatus.DRAFT);
        batch.setCreationDate(LocalDate.now());

        List<PayrollConcept> concepts = conceptRepository.findAllActiveOrderedByPriority();

        for (Employee employee : employees) {
            PayrollDetail detail = new PayrollDetail();
            detail.setEmployee(employee);
            detail.setBatch(batch);
            
            // Calculate base salary
            BigDecimal baseSalary = calculateGrossSalary(employee, period);
            detail.setBaseSalary(baseSalary);

            // Calculate deductions
            List<PayrollDetail.PayrollConceptValue> conceptValues = calculateDeductions(employee, baseSalary, period);
            
            // Calculate totals
            BigDecimal totalEarnings = BigDecimal.ZERO;
            BigDecimal totalDeductions = BigDecimal.ZERO;

            for (PayrollDetail.PayrollConceptValue cv : conceptValues) {
                detail.addConceptValue(cv);
                if (cv.getConcept().getConceptType() == PayrollConcept.ConceptType.EARNING ||
                    cv.getConcept().getConceptType() == PayrollConcept.ConceptType.ALLOWANCE) {
                    totalEarnings = totalEarnings.add(cv.getAmount());
                } else {
                    totalDeductions = totalDeductions.add(cv.getAmount());
                }
            }

            detail.setTotalEarnings(totalEarnings);
            detail.setTotalDeductions(totalDeductions);
            detail.setGrossSalary(baseSalary.add(totalEarnings));
            detail.calculateNetSalary();

            batch.addDetail(detail);
        }

        batch.recalculateTotals();
        return employeeRepository.save(new Employee()); // Placeholder - would need PayrollBatchRepository
    }

    /**
     * Process a payroll batch (mark as processed).
     */
    @Transactional
    public PayrollBatch processPayrollBatch(Long batchId) {
        // Implementation would require PayrollBatchRepository
        PayrollBatch batch = new PayrollBatch(); // Placeholder
        batch.setStatus(PayrollBatch.BatchStatus.PROCESSED);
        batch.setProcessedDate(LocalDate.now());
        return batch;
    }

    /**
     * Calculate overtime pay.
     */
    public BigDecimal calculateOvertimePay(BigDecimal hourlyRate, double overtimeHours, BigDecimal multiplier) {
        if (hourlyRate == null || multiplier == null) {
            return BigDecimal.ZERO;
        }
        return hourlyRate.multiply(new BigDecimal(String.valueOf(overtimeHours))).multiply(multiplier);
    }

    /**
     * Calculate prorated salary based on days worked.
     */
    public BigDecimal calculateProratedSalary(BigDecimal monthlySalary, int workingDaysInMonth, int daysWorked) {
        if (monthlySalary == null || workingDaysInMonth == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal dailySalary = monthlySalary.divide(new BigDecimal(workingDaysInMonth), 6, BigDecimal.ROUND_HALF_UP);
        return dailySalary.multiply(new BigDecimal(daysWorked));
    }

    /**
     * Calculate social security contribution.
     */
    public BigDecimal calculateSocialSecurity(BigDecimal grossSalary, BigDecimal rate) {
        if (grossSalary == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return grossSalary.multiply(rate);
    }

    /**
     * Generate accounting entry number for a payroll batch.
     */
    @Transactional
    public String generateAccountingEntry(PayrollBatch batch) {
        String entryNumber = "PAYROLL-" + LocalDate.now().getYear() + "-" + 
                             String.format("%06d", batch.getId());
        batch.setAccountingEntryNumber(entryNumber);
        batch.setAccountingEntryPosted(true);
        return entryNumber;
    }

    /**
     * Get employees by department.
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    /**
     * Terminate an employee.
     */
    @Transactional
    public Employee terminateEmployee(Long employeeId, LocalDate terminationDate, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalStateException("Employee not found"));
        
        employee.setEmploymentStatus(Employee.EmploymentStatus.TERMINATED);
        employee.setTerminationDate(terminationDate);
        return employeeRepository.save(employee);
    }

    /**
     * Calculate annual bonus (one month salary for full year).
     */
    public BigDecimal calculateAnnualBonus(BigDecimal monthlySalary, int monthsWorked) {
        if (monthlySalary == null) {
            return BigDecimal.ZERO;
        }
        if (monthsWorked >= 12) {
            return monthlySalary;
        }
        return monthlySalary.multiply(new BigDecimal(monthsWorked)).divide(new BigDecimal(12), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate vacation pay.
     */
    public BigDecimal calculateVacationPay(BigDecimal dailySalary, int vacationDays) {
        if (dailySalary == null) {
            return BigDecimal.ZERO;
        }
        return dailySalary.multiply(new BigDecimal(vacationDays));
    }

    /**
     * Check if employee is eligible for bonus.
     */
    public boolean isEligibleForBonus(Employee employee, LocalDate bonusDate) {
        if (employee.getHireDate() == null) {
            return false;
        }
        Period period = Period.between(employee.getHireDate(), bonusDate);
        return period.getYears() >= 1;
    }

    /**
     * Calculate total payroll cost including employer contributions.
     */
    public BigDecimal calculateTotalPayrollCost(BigDecimal grossSalary, 
                                                 BigDecimal employerSocialSecurity, 
                                                 BigDecimal otherBenefits) {
        if (grossSalary == null) grossSalary = BigDecimal.ZERO;
        if (employerSocialSecurity == null) employerSocialSecurity = BigDecimal.ZERO;
        if (otherBenefits == null) otherBenefits = BigDecimal.ZERO;
        
        return grossSalary.add(employerSocialSecurity).add(otherBenefits);
    }

    /**
     * Reverse/cancel a payroll batch.
     */
    @Transactional
    public PayrollBatch reversePayrollBatch(Long batchId) {
        PayrollBatch batch = new PayrollBatch(); // Placeholder - would need repository
        batch.setStatus(PayrollBatch.BatchStatus.CANCELLED);
        return batch;
    }

    /**
     * Get payroll statistics for a period.
     */
    @Transactional(readOnly = true)
    public PayrollStatistics getPayrollStatistics(PayrollPeriod period) {
        List<Employee> employees = employeeRepository.findActiveEmployees();
        
        PayrollStatistics stats = new PayrollStatistics();
        stats.setTotalEmployees(employees.size());
        stats.setPeriod(period);
        
        return stats;
    }

    /**
     * Evaluate a formula for a payroll concept.
     */
    private BigDecimal evaluateFormula(String formula, Employee employee, BigDecimal grossSalary) {
        // Simplified formula evaluation - would use PayrollFormulaEvaluator in production
        if (formula == null || formula.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Replace variables with actual values
        String evaluated = formula.replace("{BASE_SALARY}", employee.getBaseSalary() != null ? 
            employee.getBaseSalary().toString() : "0");
        evaluated = evaluated.replace("{GROSS_SALARY}", grossSalary != null ? 
            grossSalary.toString() : "0");
        
        // Simple evaluation (would use expression engine in production)
        try {
            return new BigDecimal(evaluated);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Payroll statistics DTO.
     */
    public static class PayrollStatistics {
        private PayrollPeriod period;
        private Integer totalEmployees;
        private BigDecimal totalGross;
        private BigDecimal totalDeductions;
        private BigDecimal totalNet;

        public PayrollPeriod getPeriod() {
            return period;
        }

        public void setPeriod(PayrollPeriod period) {
            this.period = period;
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
    }
}
