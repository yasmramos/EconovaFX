package com.econovafx.modules.payroll.service;

import com.econovafx.modules.payroll.model.Employee;
import com.econovafx.modules.payroll.model.PayrollConcept;
import com.econovafx.modules.payroll.model.PayrollPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluator for payroll concept formulas.
 * Supports basic arithmetic operations and variables.
 */
public class PayrollFormulaEvaluator {

    /**
     * Evaluate a formula expression and return the result.
     * 
     * @param formula The formula expression to evaluate
     * @param employee The employee context
     * @param period The payroll period context
     * @param concept The payroll concept being evaluated
     * @return The calculated value
     */
    public BigDecimal evaluate(String formula, Employee employee, PayrollPeriod period, PayrollConcept concept) {
        if (formula == null || formula.trim().isEmpty()) {
            return concept.getFixedAmount() != null ? concept.getFixedAmount() : BigDecimal.ZERO;
        }

        // Replace variables with actual values
        String evaluatedFormula = replaceVariables(formula, employee, period, concept);

        try {
            // Simple expression evaluator (supports +, -, *, /, parentheses)
            return evaluateExpression(evaluatedFormula);
        } catch (Exception e) {
            // If evaluation fails, return fixed amount or zero
            return concept.getFixedAmount() != null ? concept.getFixedAmount() : BigDecimal.ZERO;
        }
    }

    /**
     * Replace variable placeholders with actual values.
     */
    private String replaceVariables(String formula, Employee employee, PayrollPeriod period, PayrollConcept concept) {
        String result = formula;

        // Replace employee-related variables
        result = result.replaceAll("\\$\\{baseSalary\\}", 
            employee.getBaseSalary() != null ? employee.getBaseSalary().toString() : "0");
        
        result = result.replaceAll("\\$\\{hoursPerWeek\\}", 
            String.valueOf(employee.getWorkingHoursPerWeek() != null ? employee.getWorkingHoursPerWeek() : 40));
        
        // Calculate hourly rate based on salary type and base salary
        BigDecimal hourlyRate = BigDecimal.ZERO;
        if (employee.getBaseSalary() != null) {
            switch (employee.getSalaryType()) {
                case HOURLY:
                    hourlyRate = employee.getBaseSalary();
                    break;
                case DAILY:
                    hourlyRate = employee.getBaseSalary().divide(new BigDecimal("8"), 2, RoundingMode.HALF_UP);
                    break;
                case WEEKLY:
                    hourlyRate = employee.getBaseSalary().divide(new BigDecimal("40"), 2, RoundingMode.HALF_UP);
                    break;
                case BIWEEKLY:
                    hourlyRate = employee.getBaseSalary().divide(new BigDecimal("80"), 2, RoundingMode.HALF_UP);
                    break;
                case MONTHLY:
                    hourlyRate = employee.getBaseSalary().divide(new BigDecimal("160"), 2, RoundingMode.HALF_UP);
                    break;
                case ANNUAL:
                    hourlyRate = employee.getBaseSalary().divide(new BigDecimal("2080"), 2, RoundingMode.HALF_UP);
                    break;
            }
        }
        result = result.replaceAll("\\$\\{hourlyRate\\}", hourlyRate.toString());

        // Replace period-related variables
        result = result.replaceAll("\\$\\{daysInPeriod\\}", 
            String.valueOf(period.getDaysInPeriod()));
        
        // Calculate working days based on frequency (simplified: assume 5-day work week)
        int workingDays = period.getDaysInPeriod();
        if (period.getFrequency() == PayrollPeriod.FrequencyType.WEEKLY) {
            workingDays = 5;
        } else if (period.getFrequency() == PayrollPeriod.FrequencyType.BIWEEKLY) {
            workingDays = 10;
        } else if (period.getFrequency() == PayrollPeriod.FrequencyType.MONTHLY) {
            workingDays = Math.min(workingDays, 22); // Average working days per month
        }
        result = result.replaceAll("\\$\\{workingDays\\}", String.valueOf(workingDays));

        // Replace concept-related variables
        if (concept.getFixedAmount() != null) {
            result = result.replaceAll("\\$\\{fixedAmount\\}", concept.getFixedAmount().toString());
        }
        
        if (concept.getPercentage() != null) {
            result = result.replaceAll("\\$\\{percentage\\}", concept.getPercentage().toString());
        }

        // Replace minimum wage from system configuration (Cuban standard: 2100.00 CUP as of 2024)
        // In production, this should be fetched from SystemConfiguration via a service
        result = result.replaceAll("\\$\\{minimumWage\\}", "2100.00");

        return result;
    }

    /**
     * Evaluate a mathematical expression.
     * Simple implementation supporting +, -, *, / and parentheses.
     */
    private BigDecimal evaluateExpression(String expression) {
        try {
            // Remove whitespace
            expression = expression.replaceAll("\\s+", "");
            
            // Use a simple expression evaluator
            return new ExpressionEvaluator(expression).evaluate();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid formula expression: " + expression, e);
        }
    }

    /**
     * Simple recursive descent parser for arithmetic expressions.
     */
    private static class ExpressionEvaluator {
        private String expression;
        private int pos = 0;

        ExpressionEvaluator(String expression) {
            this.expression = expression;
        }

        BigDecimal evaluate() {
            BigDecimal result = parseExpression();
            if (pos < expression.length()) {
                throw new RuntimeException("Unexpected character: " + expression.charAt(pos));
            }
            return result;
        }

        private BigDecimal parseExpression() {
            BigDecimal left = parseTerm();
            while (pos < expression.length()) {
                char op = expression.charAt(pos);
                if (op == '+' || op == '-') {
                    pos++;
                    BigDecimal right = parseTerm();
                    if (op == '+') {
                        left = left.add(right);
                    } else {
                        left = left.subtract(right);
                    }
                } else {
                    break;
                }
            }
            return left;
        }

        private BigDecimal parseTerm() {
            BigDecimal left = parseFactor();
            while (pos < expression.length()) {
                char op = expression.charAt(pos);
                if (op == '*' || op == '/') {
                    pos++;
                    BigDecimal right = parseFactor();
                    if (op == '*') {
                        left = left.multiply(right);
                    } else {
                        if (right.compareTo(BigDecimal.ZERO) == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        left = left.divide(right, 10, BigDecimal.ROUND_HALF_UP);
                    }
                } else {
                    break;
                }
            }
            return left;
        }

        private BigDecimal parseFactor() {
            if (pos >= expression.length()) {
                throw new RuntimeException("Unexpected end of expression");
            }

            char c = expression.charAt(pos);
            
            // Handle parentheses
            if (c == '(') {
                pos++;
                BigDecimal result = parseExpression();
                if (pos >= expression.length() || expression.charAt(pos) != ')') {
                    throw new RuntimeException("Missing closing parenthesis");
                }
                pos++;
                return result;
            }

            // Handle negative numbers
            if (c == '-') {
                pos++;
                return parseFactor().negate();
            }

            // Parse number
            int start = pos;
            while (pos < expression.length()) {
                c = expression.charAt(pos);
                if (Character.isDigit(c) || c == '.') {
                    pos++;
                } else {
                    break;
                }
            }

            if (start == pos) {
                throw new RuntimeException("Unexpected character: " + c);
            }

            return new BigDecimal(expression.substring(start, pos));
        }
    }
}
