package com.econovafx.modules.payroll.service;

import com.econovafx.modules.payroll.model.*;
import com.econovafx.modules.payroll.repository.EmployeeRepository;
import com.econovafx.modules.payroll.repository.PayrollConceptRepository;
import com.econovafx.modules.core.model.SystemConfiguration;
import com.econovafx.modules.core.service.SystemConfigService;
import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.accounting.repository.AccountRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for payroll processing and calculations.
 */
@Component
public class PayrollService {

    private static final Logger logger = LoggerFactory.getLogger(PayrollService.class);

    @Inject
    EmployeeRepository employeeRepository;

    @Inject
    PayrollConceptRepository conceptRepository;

    @Inject
    SystemConfigService systemConfigService;

    @Inject
    TransactionService transactionService;

    @Inject
    AccountRepository accountRepository;

    public BigDecimal calculateGrossSalary(Employee employee, PayrollPeriod period) {
        if (employee.getBaseSalary() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseSalary = employee.getBaseSalary();
        
        switch (period.getFrequency()) {
            case WEEKLY:
                return baseSalary.divide(BigDecimal.valueOf(52), 2, RoundingMode.HALF_UP);
            case BIWEEKLY:
                return baseSalary.divide(BigDecimal.valueOf(26), 2, RoundingMode.HALF_UP);
            case MONTHLY:
                return baseSalary.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
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
        
        PayrollFormulaEvaluator evaluator = new PayrollFormulaEvaluator(systemConfigService);
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
            .divide(BigDecimal.valueOf(160), 4, RoundingMode.HALF_UP);
        
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
            .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        
        return dailyRate.multiply(BigDecimal.valueOf(vacationDays));
    }

    public BigDecimal calculateSocialSecurity(Employee employee, PayrollPeriod period) {
        SystemConfiguration config = systemConfigService.getCurrentConfig();
        BigDecimal socialSecurityRate = config.getSocialSecurityRateEmployee();
        BigDecimal grossSalary = calculateGrossSalary(employee, period);
        
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
        
        SystemConfiguration config = systemConfigService.getCurrentConfig();
        
        // Create payroll transaction with double-entry bookkeeping
        Transaction payrollTransaction = new Transaction();
        payrollTransaction.setNumber(generatePayrollTransactionNumber(batch));
        payrollTransaction.setDate(batch.getPeriod().getEndDate());
        payrollTransaction.setDescription("Nómina - Período: " + batch.getPeriod().getStartDate() + " a " + batch.getPeriod().getEndDate());
        payrollTransaction.setStatus(TransactionStatus.DRAFT);
        
        List<TransactionEntryData> entries = new ArrayList<>();
        
        // Calculate totals for employer social security contributions
        BigDecimal totalGross = batch.getTotalGross();
        BigDecimal totalEmployeeSocialSecurity = BigDecimal.ZERO;
        BigDecimal totalNet = batch.getTotalNet();
        
        for (PayrollDetail detail : batch.getPayrollDetails()) {
            totalEmployeeSocialSecurity = totalEmployeeSocialSecurity.add(
                calculateSocialSecurity(detail.getEmployee(), batch.getPeriod())
            );
        }
        
        BigDecimal employerSocialSecurity = totalGross.multiply(config.getSocialSecurityRateEmployer());
        
        // Entry 1: Debit Salary Expense (gasto de salarios)
        Account expenseAccount = findAccountByCode(config.getPayrollExpenseAccountCode());
        if (expenseAccount != null) {
            TransactionEntryData expenseEntry = new TransactionEntryData();
            expenseEntry.setAccountId(expenseAccount.getId());
            expenseEntry.setDebitAmount(totalGross.setScale(4, RoundingMode.HALF_UP));
            expenseEntry.setCreditAmount(BigDecimal.ZERO);
            entries.add(expenseEntry);
        }
        
        // Entry 2: Debit Employer Social Security Expense
        Account employerSSExpenseAccount = findAccountByCode(config.getEmployerSocialSecurityExpenseAccountCode());
        if (employerSSExpenseAccount != null) {
            TransactionEntryData employerSSEntry = new TransactionEntryData();
            employerSSEntry.setAccountId(employerSSExpenseAccount.getId());
            employerSSEntry.setDebitAmount(employerSocialSecurity.setScale(4, RoundingMode.HALF_UP));
            employerSSEntry.setCreditAmount(BigDecimal.ZERO);
            entries.add(employerSSEntry);
        }
        
        // Entry 3: Credit Employee Social Security Payable (retenciones)
        Account employeeSSPayableAccount = findAccountByCode(config.getSocialSecurityPayableAccountCode());
        if (employeeSSPayableAccount != null) {
            TransactionEntryData employeeSSPayableEntry = new TransactionEntryData();
            employeeSSPayableEntry.setAccountId(employeeSSPayableAccount.getId());
            employeeSSPayableEntry.setDebitAmount(BigDecimal.ZERO);
            employeeSSPayableEntry.setCreditAmount(totalEmployeeSocialSecurity.setScale(4, RoundingMode.HALF_UP));
            entries.add(employeeSSPayableEntry);
        }
        
        // Entry 4: Credit Employer Social Security Payable
        Account employerSSPayableAccount = findAccountByCode(config.getSocialSecurityPayableAccountCode());
        if (employerSSPayableAccount != null) {
            TransactionEntryData employerSSPayableEntry = new TransactionEntryData();
            employerSSPayableEntry.setAccountId(employerSSPayableAccount.getId());
            employerSSPayableEntry.setDebitAmount(BigDecimal.ZERO);
            employerSSPayableEntry.setCreditAmount(employerSocialSecurity.setScale(4, RoundingMode.HALF_UP));
            entries.add(employerSSPayableEntry);
        }
        
        // Entry 5: Credit Salaries Payable (líquido a pagar)
        Account salariesPayableAccount = findAccountByCode(config.getPayrollPayableAccountCode());
        if (salariesPayableAccount != null) {
            TransactionEntryData salariesPayableEntry = new TransactionEntryData();
            salariesPayableEntry.setAccountId(salariesPayableAccount.getId());
            salariesPayableEntry.setDebitAmount(BigDecimal.ZERO);
            salariesPayableEntry.setCreditAmount(totalNet.setScale(4, RoundingMode.HALF_UP));
            entries.add(salariesPayableEntry);
        }
        
        // Validate that debits equal credits
        BigDecimal totalDebits = entries.stream()
            .map(TransactionEntryData::getDebitAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = entries.stream()
            .map(TransactionEntryData::getCreditAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new IllegalStateException(
                String.format("Payroll entries are not balanced. Debits: %s, Credits: %s", totalDebits, totalCredits)
            );
        }
        
        // Create and post the transaction
        try {
            transactionService.createTransaction(payrollTransaction, entries, "system");
            logger.info("Generated accounting entries for payroll batch: {}", batch.getPeriod());
        } catch (Exception e) {
            logger.error("Error generating accounting entries for payroll batch: {}", batch.getPeriod(), e);
            throw new RuntimeException("Failed to generate accounting entries for payroll", e);
        }
    }

    public boolean validatePayrollConcepts(PayrollBatch batch) {
        SystemConfiguration config = systemConfigService.getCurrentConfig();
        BigDecimal minimumWage = config.getMinimumWage();

        for (PayrollDetail detail : batch.getPayrollDetails()) {
            Employee employee = detail.getEmployee();
            
            if (detail.getGrossSalary().compareTo(minimumWage) < 0) {
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

    /**
     * Generate a unique transaction number for payroll entries.
     */
    private String generatePayrollTransactionNumber(PayrollBatch batch) {
        LocalDate periodEnd = batch.getPeriod().getEndDate();
        String yearMonth = String.format("%04d%02d", periodEnd.getYear(), periodEnd.getMonthValue());
        return "NOM-" + yearMonth + "-" + System.currentTimeMillis();
    }
    
    /**
     * Find an account by its code using the AccountRepository.
     */
    private Account findAccountByCode(String accountCode) {
        try {
            return accountRepository.findByCode(accountCode).orElse(null);
        } catch (Exception e) {
            logger.warn("Could not find account with code: {}", accountCode, e);
            return null;
        }
    }
}
