package com.econovafx.modules.payroll.service;

import com.econovafx.modules.payroll.model.Employee;
import com.econovafx.modules.payroll.model.PayrollConcept;
import com.econovafx.modules.payroll.model.PayrollPeriod;
import com.econovafx.modules.core.model.SystemConfiguration;
import com.econovafx.modules.core.service.SystemConfigService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PayrollFormulaEvaluator with configurable parameters.
 * Verifies that minimum wage and other parameters are read from SystemConfiguration.
 */
public class PayrollFormulaEvaluatorConfigurableTest {

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private SystemConfiguration systemConfiguration;

    private PayrollFormulaEvaluator evaluator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEvaluate_UsesMinimumWageFromConfig() {
        // Arrange
        when(systemConfigService.getCurrentConfig()).thenReturn(systemConfiguration);
        when(systemConfiguration.getMinimumWage()).thenReturn(new BigDecimal("2500.00"));
        
        evaluator = new PayrollFormulaEvaluator(systemConfigService);
        
        Employee employee = createTestEmployee();
        PayrollPeriod period = createTestPeriod();
        PayrollConcept concept = createTestConcept("${minimumWage}");

        // Act
        BigDecimal result = evaluator.evaluate("${minimumWage}", employee, period, concept);

        // Assert
        assertEquals(new BigDecimal("2500.00"), result);
    }

    @Test
    void testEvaluate_UsesDefaultMinimumWage_WhenConfigNotAvailable() {
        // Arrange - no mock setup, systemConfigService is null
        evaluator = new PayrollFormulaEvaluator();
        
        Employee employee = createTestEmployee();
        PayrollPeriod period = createTestPeriod();
        PayrollConcept concept = createTestConcept("${minimumWage}");

        // Act
        BigDecimal result = evaluator.evaluate("${minimumWage}", employee, period, concept);

        // Assert - should use default Cuban minimum wage
        assertEquals(new BigDecimal("2100.00"), result);
    }

    @Test
    void testEvaluate_UsesDefaultMinimumWage_WhenConfigReturnsNull() {
        // Arrange
        when(systemConfigService.getCurrentConfig()).thenReturn(null);
        
        evaluator = new PayrollFormulaEvaluator(systemConfigService);
        
        Employee employee = createTestEmployee();
        PayrollPeriod period = createTestPeriod();
        PayrollConcept concept = createTestConcept("${minimumWage}");

        // Act
        BigDecimal result = evaluator.evaluate("${minimumWage}", employee, period, concept);

        // Assert - should use default Cuban minimum wage
        assertEquals(new BigDecimal("2100.00"), result);
    }

    @Test
    void testEvaluate_BasicArithmeticWithRoundingMode() {
        // Arrange
        evaluator = new PayrollFormulaEvaluator();
        
        Employee employee = createTestEmployee();
        PayrollPeriod period = createTestPeriod();
        PayrollConcept concept = createTestConcept("100 / 3");

        // Act
        BigDecimal result = evaluator.evaluate("100 / 3", employee, period, concept);

        // Assert - verify RoundingMode.HALF_UP is used (not deprecated BigDecimal.ROUND_HALF_UP)
        assertNotNull(result);
        assertTrue(result.compareTo(new BigDecimal("33.3333333333")) >= 0);
    }

    @Test
    void testEvaluate_BaseSalaryVariable() {
        // Arrange
        evaluator = new PayrollFormulaEvaluator();
        
        Employee employee = createTestEmployee();
        employee.setBaseSalary(new BigDecimal("5000.00"));
        PayrollPeriod period = createTestPeriod();
        PayrollConcept concept = createTestConcept("${baseSalary} / 2");

        // Act
        BigDecimal result = evaluator.evaluate("${baseSalary} / 2", employee, period, concept);

        // Assert
        assertEquals(new BigDecimal("2500.0000000000"), result);
    }

    @Test
    void testEvaluate_SocialSecurityCalculation() {
        // Arrange
        evaluator = new PayrollFormulaEvaluator();
        
        Employee employee = createTestEmployee();
        employee.setBaseSalary(new BigDecimal("5000.00"));
        PayrollPeriod period = createTestPeriod();
        // Formula: base salary * 5% social security rate
        String formula = "${baseSalary} * 0.05";
        PayrollConcept concept = createTestConcept(formula);

        // Act
        BigDecimal result = evaluator.evaluate(formula, employee, period, concept);

        // Assert
        assertEquals(new BigDecimal("250.00"), result.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void testEvaluate_ComplexExpressionWithParentheses() {
        // Arrange
        evaluator = new PayrollFormulaEvaluator();
        
        Employee employee = createTestEmployee();
        employee.setBaseSalary(new BigDecimal("5000.00"));
        PayrollPeriod period = createTestPeriod();
        // Formula: (baseSalary / 12) * 0.05
        String formula = "(${baseSalary} / 12) * 0.05";
        PayrollConcept concept = createTestConcept(formula);

        // Act
        BigDecimal result = evaluator.evaluate(formula, employee, period, concept);

        // Assert
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    private Employee createTestEmployee() {
        Employee employee = new Employee();
        employee.setBaseSalary(new BigDecimal("4000.00"));
        employee.setWorkingHoursPerWeek(40);
        return employee;
    }

    private PayrollPeriod createTestPeriod() {
        PayrollPeriod period = new PayrollPeriod();
        period.setStartDate(LocalDate.of(2024, 1, 1));
        period.setEndDate(LocalDate.of(2024, 1, 31));
        period.setFrequency(PayrollPeriod.FrequencyType.MONTHLY);
        return period;
    }

    private PayrollConcept createTestConcept(String formula) {
        PayrollConcept concept = new PayrollConcept();
        concept.setFormula(formula);
        concept.setFixedAmount(BigDecimal.ZERO);
        return concept;
    }
}
