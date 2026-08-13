package com.econovafx.modules.payroll.service;

import com.econovafx.modules.payroll.model.*;
import com.econovafx.modules.payroll.repository.EmployeeRepository;
import com.econovafx.modules.payroll.repository.PayrollConceptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PayrollService.
 * Tests salary calculations, deductions, and payroll processing.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PayrollConceptRepository conceptRepository;

    @InjectMocks
    private PayrollService payrollService;

    private Employee testEmployee;
    private PayrollConcept salaryConcept;
    private PayrollConcept taxConcept;
    private PayrollPeriod testPeriod;

    @BeforeEach
    void setUp() {
        // Create test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setEmployeeCode("EMP001");
        testEmployee.setBaseSalary(new BigDecimal("5000.00"));
        testEmployee.setSalaryType(Employee.SalaryType.MONTHLY);
        testEmployee.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);

        // Create salary concept
        salaryConcept = new PayrollConcept();
        salaryConcept.setId(1L);
        salaryConcept.setConceptCode("SALARY");
        salaryConcept.setName("Base Salary");
        salaryConcept.setConceptType(PayrollConcept.ConceptType.EARNING);
        salaryConcept.setCalculationType(PayrollConcept.CalculationType.FIXED_AMOUNT);
        salaryConcept.setFixedAmount(new BigDecimal("5000.00"));
        salaryConcept.setTaxable(true);
        salaryConcept.setActive(true);

        // Create tax concept
        taxConcept = new PayrollConcept();
        taxConcept.setId(2L);
        taxConcept.setConceptCode("TAX");
        taxConcept.setName("Income Tax");
        taxConcept.setConceptType(PayrollConcept.ConceptType.WITHHOLDING);
        taxConcept.setCalculationType(PayrollConcept.CalculationType.PERCENTAGE);
        taxConcept.setPercentage(new BigDecimal("0.10")); // 10%
        taxConcept.setTaxable(false);
        taxConcept.setActive(true);

        // Create test period
        testPeriod = new PayrollPeriod();
        testPeriod.setId(1L);
        testPeriod.setPeriodCode("2024-01");
        testPeriod.setName("January 2024");
        testPeriod.setStartDate(LocalDate.of(2024, 1, 1));
        testPeriod.setEndDate(LocalDate.of(2024, 1, 31));
        testPeriod.setPaymentDate(LocalDate.of(2024, 1, 31));
        testPeriod.setFrequency(PayrollPeriod.FrequencyType.MONTHLY);
    }

    @Test
    void testCalculateGrossSalary_Success() {
        // Arrange
        List<PayrollConcept> concepts = new ArrayList<>();
        concepts.add(salaryConcept);
        when(conceptRepository.findAllActiveOrderedByPriority()).thenReturn(concepts);

        // Act
        BigDecimal grossSalary = payrollService.calculateGrossSalary(testEmployee, testPeriod);

        // Assert
        assertNotNull(grossSalary);
        assertEquals(new BigDecimal("5000.00"), grossSalary);
    }

    @Test
    void testCalculateDeductions_Percentage_Success() {
        // Arrange
        List<PayrollConcept> concepts = new ArrayList<>();
        concepts.add(taxConcept);
        when(conceptRepository.findAllActiveOrderedByPriority()).thenReturn(concepts);

        BigDecimal grossSalary = new BigDecimal("5000.00");

        // Act
        List<PayrollDetail.PayrollConceptValue> deductions = 
            payrollService.calculateDeductions(testEmployee, grossSalary, testPeriod);

        // Assert
        assertNotNull(deductions);
        assertFalse(deductions.isEmpty());
        assertEquals(1, deductions.size());
        assertEquals(new BigDecimal("500.00"), deductions.get(0).getAmount()); // 10% of 5000
    }

    @Test
    void testCalculateNetSalary_Success() {
        // Arrange
        BigDecimal grossSalary = new BigDecimal("5000.00");
        BigDecimal totalDeductions = new BigDecimal("500.00");

        // Act
        BigDecimal netSalary = payrollService.calculateNetSalary(grossSalary, totalDeductions);

        // Assert
        assertNotNull(netSalary);
        assertEquals(new BigDecimal("4500.00"), netSalary);
    }

    @Test
    void testCreatePayrollBatch_Success() {
        // Arrange
        List<Employee> employees = new ArrayList<>();
        employees.add(testEmployee);
        when(employeeRepository.findActiveEmployees()).thenReturn(employees);

        List<PayrollConcept> concepts = new ArrayList<>();
        concepts.add(salaryConcept);
        concepts.add(taxConcept);
        when(conceptRepository.findAllActiveOrderedByPriority()).thenReturn(concepts);

        // Act
        PayrollBatch batch = payrollService.createPayrollBatch(testPeriod);

        // Assert
        assertNotNull(batch);
        assertEquals(PayrollBatch.BatchStatus.DRAFT, batch.getStatus());
        assertFalse(batch.getDetails().isEmpty());
        assertEquals(1, batch.getTotalEmployees());
    }

    @Test
    void testProcessPayrollBatch_ChangesStatus() {
        // Arrange
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setBatchCode("BATCH-001");
        batch.setStatus(PayrollBatch.BatchStatus.CALCULATED);
        batch.setPeriod(testPeriod);

        // Act
        PayrollBatch processed = payrollService.processPayrollBatch(batch.getId());

        // Assert
        assertNotNull(processed);
        assertEquals(PayrollBatch.BatchStatus.PROCESSED, processed.getStatus());
        assertNotNull(processed.getProcessedDate());
    }

    @Test
    void testCalculateOvertimePay_Success() {
        // Arrange
        BigDecimal hourlyRate = new BigDecimal("25.00");
        double overtimeHours = 10.0;
        BigDecimal overtimeMultiplier = new BigDecimal("1.5");

        // Act
        BigDecimal overtimePay = payrollService.calculateOvertimePay(hourlyRate, overtimeHours, overtimeMultiplier);

        // Assert
        assertNotNull(overtimePay);
        assertEquals(new BigDecimal("375.00"), overtimePay); // 25 * 10 * 1.5
    }

    @Test
    void testCalculateProratedSalary_Success() {
        // Arrange
        BigDecimal monthlySalary = new BigDecimal("3000.00");
        int workingDaysInMonth = 30;
        int daysWorked = 15;

        // Act
        BigDecimal proratedSalary = payrollService.calculateProratedSalary(monthlySalary, workingDaysInMonth, daysWorked);

        // Assert
        assertNotNull(proratedSalary);
        assertEquals(new BigDecimal("1500.00"), proratedSalary); // Half month
    }

    @Test
    void testCalculateSocialSecurity_Success() {
        // Arrange
        BigDecimal grossSalary = new BigDecimal("5000.00");
        BigDecimal socialSecurityRate = new BigDecimal("0.04"); // 4%

        // Act
        BigDecimal socialSecurity = payrollService.calculateSocialSecurity(grossSalary, socialSecurityRate);

        // Assert
        assertNotNull(socialSecurity);
        assertEquals(new BigDecimal("200.00"), socialSecurity);
    }

    @Test
    void testValidatePayrollBatch_NoActiveEmployees_ThrowsException() {
        // Arrange
        when(employeeRepository.findActiveEmployees()).thenReturn(new ArrayList<>());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payrollService.createPayrollBatch(testPeriod);
        });
        assertTrue(exception.getMessage().contains("active employees"));
    }

    @Test
    void testGenerateAccountingEntry_Success() {
        // Arrange
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setTotalGross(new BigDecimal("5000.00"));
        batch.setTotalDeductions(new BigDecimal("500.00"));
        batch.setTotalNet(new BigDecimal("4500.00"));
        batch.setTotalSocialSecurity(new BigDecimal("200.00"));
        batch.setTotalTaxWithheld(new BigDecimal("300.00"));

        // Act
        String entryNumber = payrollService.generateAccountingEntry(batch);

        // Assert
        assertNotNull(entryNumber);
        assertTrue(entryNumber.startsWith("PAYROLL-"));
    }

    @Test
    void testGetEmployeesByDepartment_Success() {
        // Arrange
        List<Employee> employees = new ArrayList<>();
        employees.add(testEmployee);
        when(employeeRepository.findByDepartment("IT")).thenReturn(employees);

        // Act
        List<Employee> result = payrollService.getEmployeesByDepartment("IT");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testTerminateEmployee_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        LocalDate terminationDate = LocalDate.of(2024, 6, 30);

        // Act
        Employee terminated = payrollService.terminateEmployee(1L, terminationDate, "Resignation");

        // Assert
        assertNotNull(terminated);
        assertEquals(Employee.EmploymentStatus.TERMINATED, terminated.getEmploymentStatus());
        assertEquals(terminationDate, terminated.getTerminationDate());
    }

    @Test
    void testCalculateAnnualBonus_Success() {
        // Arrange
        BigDecimal monthlySalary = new BigDecimal("5000.00");
        int monthsWorked = 12;

        // Act
        BigDecimal bonus = payrollService.calculateAnnualBonus(monthlySalary, monthsWorked);

        // Assert
        assertNotNull(bonus);
        assertEquals(new BigDecimal("5000.00"), bonus); // One month salary
    }

    @Test
    void testCalculateVacationPay_Success() {
        // Arrange
        BigDecimal dailySalary = new BigDecimal("166.67");
        int vacationDays = 15;

        // Act
        BigDecimal vacationPay = payrollService.calculateVacationPay(dailySalary, vacationDays);

        // Assert
        assertNotNull(vacationPay);
        assertEquals(new BigDecimal("2500.05"), vacationPay.setScale(2));
    }

    @Test
    void testIsEligibleForBonus_FullYear_True() {
        // Arrange
        testEmployee.setHireDate(LocalDate.of(2023, 1, 1));
        LocalDate bonusDate = LocalDate.of(2024, 1, 1);

        // Act
        boolean eligible = payrollService.isEligibleForBonus(testEmployee, bonusDate);

        // Assert
        assertTrue(eligible);
    }

    @Test
    void testIsEligibleForBonus_LessThanYear_False() {
        // Arrange
        testEmployee.setHireDate(LocalDate.of(2023, 12, 1));
        LocalDate bonusDate = LocalDate.of(2024, 1, 1);

        // Act
        boolean eligible = payrollService.isEligibleForBonus(testEmployee, bonusDate);

        // Assert
        assertFalse(eligible);
    }

    @Test
    void testCalculateTotalPayrollCost_Success() {
        // Arrange
        BigDecimal grossSalary = new BigDecimal("5000.00");
        BigDecimal employerSocialSecurity = new BigDecimal("250.00");
        BigDecimal otherBenefits = new BigDecimal("300.00");

        // Act
        BigDecimal totalCost = payrollService.calculateTotalPayrollCost(grossSalary, employerSocialSecurity, otherBenefits);

        // Assert
        assertNotNull(totalCost);
        assertEquals(new BigDecimal("5550.00"), totalCost);
    }

    @Test
    void testReversePayrollBatch_Success() {
        // Arrange
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setStatus(PayrollBatch.BatchStatus.PROCESSED);
        batch.setAccountingEntryPosted(true);

        // Act
        PayrollBatch reversed = payrollService.reversePayrollBatch(batch.getId());

        // Assert
        assertNotNull(reversed);
        assertEquals(PayrollBatch.BatchStatus.CANCELLED, reversed.getStatus());
    }

    @Test
    void testGetPayrollStatistics_Success() {
        // Arrange
        List<Employee> employees = new ArrayList<>();
        employees.add(testEmployee);
        when(employeeRepository.findActiveEmployees()).thenReturn(employees);

        // Act
        PayrollService.PayrollStatistics stats = payrollService.getPayrollStatistics(testPeriod);

        // Assert
        assertNotNull(stats);
        assertNotNull(stats.getTotalEmployees());
    }
}
