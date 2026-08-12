package com.econovafx.modules.payroll.service;

import com.econovafx.modules.payroll.model.*;
import com.econovafx.modules.payroll.repository.EmployeeRepository;
import com.econovafx.modules.payroll.repository.PayrollConceptRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for payroll processing and calculations.
 */
@Component
public class PayrollService {

    @Inject
    EmployeeRepository employeeRepository;

    @Inject
    PayrollConceptRepository conceptRepository;

    public BigDecimal calculateGrossSalary(Employee employee, PayrollPeriod period) {
        if (employee.getBaseSalary() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseSalary = employee.getBaseSalary();
        
        switch (period.getFrequency()) {
            case WEEKLY:
                return baseSalary.divide(BigDecimal.valueOf(52), 2, BigDecimal.ROUND_HALF_UP);
            case BIWEEKLY:
                return baseSalary.divide(BigDecimal.valueOf(26), 2, BigDecimal.ROUND_HALF_UP);
            case MONTHLY:
                return baseSalary.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
            default:
                return baseSalary;
        }
    }

    public BigDecimal calculateTotalDeductions(Employee employee, PayrollPeriod period) {
        List<PayrollConcept> deductions = conceptRepository.findByTypeAndActive(PayrollConcept.ConceptType.DEDUCTION, true);
        
        BigDecimal totalDeductions = BigDecimal.ZERO;
        
        for (PayrollConcept concept : deductions) {
            if (concept.isApplicableToEmployee(employee)) {
                BigDecimal amount = evaluateConcept(concept, employee, period);
                totalDeductions = totalDeductions.add(amount);
            }
        }
        
        return totalDeductions;
    }

    public BigDecimal calculateNetSalary(Employee employee, PayrollPeriod period) {
        BigDecimal grossSalary = calculateGrossSalary(employee, period);
        BigDecimal totalDeductions = calculateTotalDeductions(employee, period);
        
        return grossSalary.subtract(totalDeductions).max(BigDecimal.ZERO);
    }

    public BigDecimal evaluateConcept(PayrollConcept concept, Employee employee, PayrollPeriod period) {
        if (concept.getFormula() == null || concept.getFormula().isEmpty()) {
            return concept.getFixedAmount() != null ? concept.getFixedAmount() : BigDecimal.ZERO;
        }
        
        PayrollFormulaEvaluator evaluator = new PayrollFormulaEvaluator();
        return evaluator.evaluate(concept.getFormula(), employee, period, concept);
    }

    public PayrollBatch processPayrollBatch(PayrollPeriod period, String processedBy) {
        PayrollBatch batch = new PayrollBatch();
        batch.setPeriod(period);
        batch.setProcessedDate(LocalDate.now());
        batch.setStatus(PayrollBatch.BatchStatus.PROCESSED);
        // // batch.setProcessedBy(processedBy); - Not in model - Not in model
        
        List<Employee> activeEmployees = employeeRepository.findByActiveTrue();
        List<PayrollDetail> details = new ArrayList<>();
        
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        
        for (Employee employee : activeEmployees) {
            PayrollDetail detail = new PayrollDetail();
            detail.setBatch(batch);
            detail.setEmployee(employee);
            
            BigDecimal gross = calculateGrossSalary(employee, period);
            BigDecimal deductions = calculateTotalDeductions(employee, period);
            BigDecimal net = gross.subtract(deductions);
            
            detail.setGrossSalary(gross);
            detail.setTotalDeductions(deductions);
            detail.setNetSalary(net);
            detail.setPaymentDate(period.getEndDate());
            
            details.add(detail);
            
            totalGross = totalGross.add(gross);
            totalDeductions = totalDeductions.add(deductions);
            totalNet = totalNet.add(net);
        }
        
        batch.setDetails(details);
        batch.setTotalGross(totalGross);
        batch.setTotalDeductions(totalDeductions);
        batch.setTotalNet(totalNet);
        
        return batch;
    }

    public BigDecimal calculateOvertimePay(Employee employee, int overtimeHours) {
        if (employee.getBaseSalary() == null || overtimeHours <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal hourlyRate = employee.getBaseSalary()
            .divide(BigDecimal.valueOf(160), 4, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal overtimeMultiplier = BigDecimal.valueOf(1.5);
        
        return hourlyRate
            .multiply(BigDecimal.valueOf(overtimeHours))
            .multiply(overtimeMultiplier);
    }

    public BigDecimal calculateVacationPay(Employee employee, int vacationDays) {
        if (employee.getBaseSalary() == null || vacationDays <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal dailyRate = employee.getBaseSalary()
            .divide(BigDecimal.valueOf(30), 4, BigDecimal.ROUND_HALF_UP);
        
        return dailyRate.multiply(BigDecimal.valueOf(vacationDays));
    }

    public BigDecimal calculateSocialSecurity(Employee employee, PayrollPeriod period) {
        BigDecimal grossSalary = calculateGrossSalary(employee, period);
        BigDecimal socialSecurityRate = BigDecimal.valueOf(0.05);
        
        return grossSalary.multiply(socialSecurityRate);
    }

    public List<Employee> getEligibleEmployees(PayrollPeriod period) {
        List<Employee> allActive = employeeRepository.findByActiveTrue();
        List<Employee> eligible = new ArrayList<>();
        
        LocalDate periodStart = period.getStartDate();
        LocalDate periodEnd = period.getEndDate();
        
        for (Employee employee : allActive) {
            if (employee.getHireDate() != null && 
                !employee.getHireDate().isAfter(periodEnd)) {
                
                if (employee.getTerminationDate() == null || 
                    !employee.getTerminationDate().isBefore(periodStart)) {
                    eligible.add(employee);
                }
            }
        }
        
        return eligible;
    }

    public void generateAccountingEntries(PayrollBatch batch) {
        if (batch.getStatus() != PayrollBatch.BatchStatus.PROCESSED) {
            throw new IllegalStateException("Batch must be processed before generating entries");
        }
    }

    public boolean validatePayrollConcepts(PayrollBatch batch) {
        for (PayrollDetail detail : batch.getPayrollDetails()) {
            Employee employee = detail.getEmployee();
            
            if (detail.getGrossSalary().compareTo(employee.getMinimumWage()) < 0) {
                return false;
            }
            
            BigDecimal maxDeductions = detail.getGrossSalary()
                .multiply(BigDecimal.valueOf(0.30));
            
            if (detail.getTotalDeductions().compareTo(maxDeductions) > 0) {
                return false;
            }
        }
        
        return true;
    }
}
