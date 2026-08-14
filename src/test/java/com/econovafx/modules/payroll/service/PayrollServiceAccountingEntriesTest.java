package com.econovafx.modules.payroll.service;

import com.econovafx.modules.payroll.model.*;
import com.econovafx.modules.core.model.SystemConfiguration;
import com.econovafx.modules.core.service.SystemConfigService;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.accounting.service.TransactionService.TransactionEntryData;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.payroll.repository.EmployeeRepository;
import com.econovafx.modules.payroll.repository.PayrollConceptRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PayrollService.generateAccountingEntries method.
 * Verifies that accounting entries are generated correctly and transferred to the accounting module.
 */
class PayrollServiceAccountingEntriesTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PayrollConceptRepository conceptRepository;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountRepository accountRepository;

    private PayrollService payrollService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        payrollService = new PayrollService();
        payrollService.employeeRepository = employeeRepository;
        payrollService.conceptRepository = conceptRepository;
        payrollService.systemConfigService = systemConfigService;
        payrollService.transactionService = transactionService;
        payrollService.accountRepository = accountRepository;
    }

    @Test
    void testGenerateAccountingEntries_ThrowsException_WhenBatchNotProcessed() {
        // Arrange
        PayrollBatch batch = new PayrollBatch();
        batch.setStatus(PayrollBatch.BatchStatus.DRAFT);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> payrollService.generateAccountingEntries(batch)
        );
        assertEquals("Batch must be processed before generating entries", exception.getMessage());
    }

    @Test
    void testGenerateAccountingEntries_CreatesBalancedEntries() {
        // Arrange
        PayrollBatch batch = createProcessedPayrollBatch();
        SystemConfiguration config = createSystemConfiguration();
        
        when(systemConfigService.getCurrentConfig()).thenReturn(config);
        when(accountRepository.findByCode(config.getPayrollExpenseAccountCode()))
            .thenReturn(Optional.of(createAccount("501-001")));
        when(accountRepository.findByCode(config.getEmployerSocialSecurityExpenseAccountCode()))
            .thenReturn(Optional.of(createAccount("503-001")));
        when(accountRepository.findByCode(config.getSocialSecurityPayableAccountCode()))
            .thenReturn(Optional.of(createAccount("205-001")));
        when(accountRepository.findByCode(config.getPayrollPayableAccountCode()))
            .thenReturn(Optional.of(createAccount("203-001")));

        // Act
        payrollService.generateAccountingEntries(batch);

        // Verify transaction was created
        verify(transactionService, times(1)).createTransaction(
            any(Transaction.class),
            any(List.class),
            eq("system")
        );
    }

    @Test
    void testGenerateAccountingEntries_VerifiesDoubleEntryBalance() {
        // Arrange
        PayrollBatch batch = createProcessedPayrollBatch();
        SystemConfiguration config = createSystemConfiguration();
        
        when(systemConfigService.getCurrentConfig()).thenReturn(config);
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.of(createAccount("TEST")));

        // Act
        payrollService.generateAccountingEntries(batch);

        // Verify that transactionService.createTransaction was called with balanced entries
        verify(transactionService).createTransaction(
            any(Transaction.class),
            argThat(entries -> {
                List<TransactionEntryData> entryList = (List<TransactionEntryData>) entries;
                BigDecimal totalDebits = entryList.stream()
                    .map(TransactionEntryData::getDebitAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalCredits = entryList.stream()
                    .map(TransactionEntryData::getCreditAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return totalDebits.compareTo(totalCredits) == 0;
            }),
            eq("system")
        );
    }

    @Test
    void testGenerateAccountingEntries_IncludesAllRequiredAccounts() {
        // Arrange
        PayrollBatch batch = createProcessedPayrollBatch();
        SystemConfiguration config = createSystemConfiguration();
        
        when(systemConfigService.getCurrentConfig()).thenReturn(config);
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.of(createAccount("TEST")));

        // Act
        payrollService.generateAccountingEntries(batch);

        // Verify all required account codes were looked up
        verify(accountRepository).findByCode(config.getPayrollExpenseAccountCode());
        verify(accountRepository).findByCode(config.getEmployerSocialSecurityExpenseAccountCode());
        verify(accountRepository, atLeastOnce()).findByCode(config.getSocialSecurityPayableAccountCode());
        verify(accountRepository).findByCode(config.getPayrollPayableAccountCode());
    }

    @Test
    void testGenerateAccountingEntries_UsesConfigurableRates() {
        // Arrange
        PayrollBatch batch = createProcessedPayrollBatch();
        SystemConfiguration config = createSystemConfiguration();
        config.setSocialSecurityRateEmployee(new BigDecimal("0.05"));
        config.setSocialSecurityRateEmployer(new BigDecimal("0.125"));
        
        when(systemConfigService.getCurrentConfig()).thenReturn(config);
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.of(createAccount("TEST")));

        // Act
        payrollService.generateAccountingEntries(batch);

        // Verify configuration was used (at least once)
        verify(systemConfigService, atLeastOnce()).getCurrentConfig();
    }

    private PayrollBatch createProcessedPayrollBatch() {
        PayrollBatch batch = new PayrollBatch();
        batch.setStatus(PayrollBatch.BatchStatus.PROCESSED);
        batch.setTotalGross(new BigDecimal("10000.00"));
        batch.setTotalNet(new BigDecimal("9400.00"));
        batch.setTotalDeductions(new BigDecimal("600.00"));
        
        PayrollPeriod period = new PayrollPeriod();
        period.setStartDate(LocalDate.of(2024, 1, 1));
        period.setEndDate(LocalDate.of(2024, 1, 31));
        batch.setPeriod(period);
        
        List<PayrollDetail> details = new ArrayList<>();
        PayrollDetail detail = new PayrollDetail();
        detail.setGrossSalary(new BigDecimal("5000.00"));
        detail.setNetSalary(new BigDecimal("4700.00"));
        detail.setTotalDeductions(new BigDecimal("300.00"));
        
        Employee employee = new Employee();
        employee.setBaseSalary(new BigDecimal("5000.00"));
        detail.setEmployee(employee);
        
        details.add(detail);
        batch.setDetails(details);
        
        return batch;
    }

    private SystemConfiguration createSystemConfiguration() {
        SystemConfiguration config = new SystemConfiguration();
        config.setSocialSecurityRateEmployee(new BigDecimal("0.05"));
        config.setSocialSecurityRateEmployer(new BigDecimal("0.125"));
        config.setMinimumWage(new BigDecimal("2100.00"));
        config.setPayrollExpenseAccountCode("501-001");
        config.setPayrollPayableAccountCode("203-001");
        config.setSocialSecurityPayableAccountCode("205-001");
        config.setEmployerSocialSecurityExpenseAccountCode("503-001");
        return config;
    }

    private com.econovafx.modules.accounting.model.Account createAccount(String code) {
        com.econovafx.modules.accounting.model.Account account = 
            new com.econovafx.modules.accounting.model.Account();
        account.setId(1L);
        account.setCode(code);
        account.setName("Test Account");
        return account;
    }

    // Helper matcher for argument verification
    @SuppressWarnings("unchecked")
    private <T> T argThat(org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
