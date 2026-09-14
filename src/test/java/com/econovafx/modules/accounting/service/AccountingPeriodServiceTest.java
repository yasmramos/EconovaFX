package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.AccountingPeriod;
import com.econovafx.modules.accounting.repository.AccountingPeriodRepository;
import com.econovafx.modules.cash.service.CashMovementService;
import com.econovafx.modules.inventory.service.InventoryService;
import com.econovafx.modules.reporting.model.FinancialReport;
import com.econovafx.modules.reporting.repository.FinancialReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountingPeriodServiceTest {

    @Mock
    private AccountingPeriodRepository repository;

    @Mock
    private CashMovementService cashMovementService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private FinancialReportRepository financialReportRepository;

    @Mock
    private com.econovafx.modules.accounting.repository.TransactionRepository transactionRepository;

    @Mock
    private com.econovafx.modules.accounting.repository.ClosingEntryRepository closingEntryRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private FinancialStatementService financialStatementService;

    private AccountingPeriodService service;

    @BeforeEach
    void setUp() {
        service = new AccountingPeriodService();
        // Inject mocks using reflection or setter if available
        try {
            java.lang.reflect.Field repoField = AccountingPeriodService.class.getDeclaredField("repository");
            repoField.setAccessible(true);
            repoField.set(service, repository);

            java.lang.reflect.Field cashField = AccountingPeriodService.class.getDeclaredField("cashMovementService");
            cashField.setAccessible(true);
            cashField.set(service, cashMovementService);

            java.lang.reflect.Field invField = AccountingPeriodService.class.getDeclaredField("inventoryService");
            invField.setAccessible(true);
            invField.set(service, inventoryService);

            java.lang.reflect.Field reportRepoField = AccountingPeriodService.class.getDeclaredField("financialReportRepository");
            reportRepoField.setAccessible(true);
            reportRepoField.set(service, financialReportRepository);

            java.lang.reflect.Field transRepoField = AccountingPeriodService.class.getDeclaredField("transactionRepository");
            transRepoField.setAccessible(true);
            transRepoField.set(service, transactionRepository);

            java.lang.reflect.Field closingEntryRepoField = AccountingPeriodService.class.getDeclaredField("closingEntryRepository");
            closingEntryRepoField.setAccessible(true);
            closingEntryRepoField.set(service, closingEntryRepository);

            java.lang.reflect.Field transServiceField = AccountingPeriodService.class.getDeclaredField("transactionService");
            transServiceField.setAccessible(true);
            transServiceField.set(service, transactionService);

            java.lang.reflect.Field finStmtServiceField = AccountingPeriodService.class.getDeclaredField("financialStatementService");
            finStmtServiceField.setAccessible(true);
            finStmtServiceField.set(service, financialStatementService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocks", e);
        }
    }

    @Test
    void testGetAllPeriods() {
        // Arrange
        AccountingPeriod period1 = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        AccountingPeriod period2 = new AccountingPeriod("Feb 2024", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findAll()).thenReturn(List.of(period1, period2));

        // Act
        List<AccountingPeriod> result = service.getAllPeriods();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetPeriodById() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findById(1L)).thenReturn(Optional.of(period));

        // Act
        Optional<AccountingPeriod> result = service.getPeriodById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Jan 2024", result.get().getName());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testGetCurrentOpenPeriod() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findCurrentOpenPeriod()).thenReturn(Optional.of(period));

        // Act
        Optional<AccountingPeriod> result = service.getCurrentOpenPeriod();

        // Assert
        assertTrue(result.isPresent());
        verify(repository, times(1)).findCurrentOpenPeriod();
    }

    @Test
    void testGetPeriodByDate() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findPeriodByDate(LocalDate.of(2024, 1, 15))).thenReturn(Optional.of(period));

        // Act
        Optional<AccountingPeriod> result = service.getPeriodByDate(LocalDate.of(2024, 1, 15));

        // Assert
        assertTrue(result.isPresent());
        verify(repository, times(1)).findPeriodByDate(LocalDate.of(2024, 1, 15));
    }

    @Test
    void testCreatePeriod_ValidDates() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.save(any(AccountingPeriod.class))).thenReturn(period);

        // Act
        AccountingPeriod result = service.createPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        // Assert
        assertNotNull(result);
        assertEquals("Jan 2024", result.getName());
        verify(repository, times(1)).save(any(AccountingPeriod.class));
    }

    @Test
    void testCreatePeriod_InvalidDates() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            service.createPeriod("Invalid", LocalDate.of(2024, 1, 31), LocalDate.of(2024, 1, 1))
        );
    }

    @Test
    void testCreatePeriod_OverlappingDates() {
        // Arrange
        AccountingPeriod existing = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findAll()).thenReturn(List.of(existing));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            service.createPeriod("Overlap", LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 15))
        );
    }

    @Test
    void testCreateMonthlyPeriod() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("January 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.save(any(AccountingPeriod.class))).thenReturn(period);

        // Act
        AccountingPeriod result = service.createMonthlyPeriod(2024, 1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isMonthly());
        verify(repository, times(1)).save(any(AccountingPeriod.class));
    }

    @Test
    void testCreateAnnualPeriod() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Fiscal Year 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), AccountingPeriod.PeriodType.ANNUAL);
        when(repository.save(any(AccountingPeriod.class))).thenReturn(period);

        // Act
        AccountingPeriod result = service.createAnnualPeriod(2024);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAnnual());
        verify(repository, times(1)).save(any(AccountingPeriod.class));
    }

    @Test
    void testValidateDependentModulesClosed_AllClosed() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(cashMovementService.isModuleClosedForPeriod(period)).thenReturn(true);
        when(inventoryService.isModuleClosedForPeriod(period)).thenReturn(true);

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> service.validateDependentModulesClosed(period));
    }

    @Test
    void testValidateDependentModulesClosed_CashNotClosed() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(cashMovementService.isModuleClosedForPeriod(period)).thenReturn(false);
        when(inventoryService.isModuleClosedForPeriod(period)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            service.validateDependentModulesClosed(period)
        );
        assertTrue(exception.getMessage().contains("Cash/Bank module is not closed"));
    }

    @Test
    void testValidateDependentModulesClosed_InventoryNotClosed() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(cashMovementService.isModuleClosedForPeriod(period)).thenReturn(true);
        when(inventoryService.isModuleClosedForPeriod(period)).thenReturn(false);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            service.validateDependentModulesClosed(period)
        );
        assertTrue(exception.getMessage().contains("Inventory module is not closed"));
    }

    @Test
    void testCloseMonthlyPeriod_Success() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findById(1L)).thenReturn(Optional.of(period));
        when(cashMovementService.isModuleClosedForPeriod(period)).thenReturn(true);
        when(inventoryService.isModuleClosedForPeriod(period)).thenReturn(true);
        when(repository.save(any(AccountingPeriod.class))).thenReturn(period);

        // Act
        AccountingPeriod result = service.closeMonthlyPeriod(1L, "testuser", "Closing notes");

        // Assert
        assertNotNull(result);
        assertEquals(AccountingPeriod.PeriodStatus.CLOSED, result.getStatus());
        assertEquals("testuser", result.getClosedBy());
        verify(repository, times(1)).save(period);
    }

    @Test
    void testCloseMonthlyPeriod_PeriodNotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            service.closeMonthlyPeriod(999L, "testuser", "Notes")
        );
    }

    @Test
    void testCloseMonthlyPeriod_AlreadyClosed() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        period.setStatus(AccountingPeriod.PeriodStatus.CLOSED);
        when(repository.findById(1L)).thenReturn(Optional.of(period));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            service.closeMonthlyPeriod(1L, "testuser", "Notes")
        );
    }

    @Test
    void testCloseMonthlyPeriod_NotMonthly() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), AccountingPeriod.PeriodType.ANNUAL);
        when(repository.findById(1L)).thenReturn(Optional.of(period));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            service.closeMonthlyPeriod(1L, "testuser", "Notes")
        );
    }

    @Test
    void testCloseAnnualPeriod_Success() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Fiscal Year 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), AccountingPeriod.PeriodType.ANNUAL);
        when(repository.findById(1L)).thenReturn(Optional.of(period));
        when(repository.save(any(AccountingPeriod.class))).thenReturn(period);
        
        // Mock nominal accounts as closed
        when(closingEntryRepository.areNominalAccountsClosed(2024)).thenReturn(true);
        
        // Mock December month as closed
        AccountingPeriod december = new AccountingPeriod("Dec 2024", LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 31), AccountingPeriod.PeriodType.MONTHLY);
        december.setStatus(AccountingPeriod.PeriodStatus.CLOSED);
        when(repository.findPeriodsByYearAndType(2024, AccountingPeriod.PeriodType.MONTHLY))
            .thenReturn(List.of(december));
        
        // Mock financial statements as issued
        FinancialReport balanceSheet = new FinancialReport();
        balanceSheet.setReportType(FinancialReport.ReportType.BALANCE_SHEET);
        balanceSheet.setFiscalYear(2024);
        balanceSheet.setFinal(true);
        FinancialReport incomeStatement = new FinancialReport();
        incomeStatement.setReportType(FinancialReport.ReportType.INCOME_STATEMENT);
        incomeStatement.setFiscalYear(2024);
        incomeStatement.setFinal(true);
        when(financialReportRepository.findByFiscalYear(2024)).thenReturn(List.of(balanceSheet, incomeStatement));

        // Act
        AccountingPeriod result = service.closeAnnualPeriod(1L, "testuser", "Closing notes", false);

        // Assert
        assertNotNull(result);
        assertEquals(AccountingPeriod.PeriodStatus.CLOSED, result.getStatus());
        verify(repository, times(1)).save(period);
    }

    @Test
    void testClosePeriod_Success() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findById(1L)).thenReturn(Optional.of(period));
        when(repository.save(any(AccountingPeriod.class))).thenReturn(period);

        // Act
        AccountingPeriod result = service.closePeriod(1L, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(AccountingPeriod.PeriodStatus.CLOSED, result.getStatus());
    }

    @Test
    void testLockPeriod_Success() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        period.setStatus(AccountingPeriod.PeriodStatus.CLOSED);
        when(repository.findById(1L)).thenReturn(Optional.of(period));
        when(repository.save(any(AccountingPeriod.class))).thenReturn(period);

        // Act
        AccountingPeriod result = service.lockPeriod(1L);

        // Assert
        assertNotNull(result);
        assertEquals(AccountingPeriod.PeriodStatus.LOCKED, result.getStatus());
    }

    @Test
    void testLockPeriod_OpenPeriod() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findById(1L)).thenReturn(Optional.of(period));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.lockPeriod(1L));
    }

    @Test
    void testReopenPeriod_LockedPeriod() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        period.setStatus(AccountingPeriod.PeriodStatus.LOCKED);
        when(repository.findById(1L)).thenReturn(Optional.of(period));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.reopenPeriod(1L));
    }

    @Test
    void testReopenPeriod_ClosedPeriod() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        period.setStatus(AccountingPeriod.PeriodStatus.CLOSED);
        when(repository.findById(1L)).thenReturn(Optional.of(period));

        // Act & Assert - Resolution 340/2004: Cannot reopen closed periods
        assertThrows(IllegalStateException.class, () -> service.reopenPeriod(1L));
    }

    @Test
    void testIsValidTransactionDate_Valid() {
        // Arrange
        AccountingPeriod period = new AccountingPeriod("Jan 2024", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), AccountingPeriod.PeriodType.MONTHLY);
        when(repository.findPeriodByDate(LocalDate.of(2024, 1, 15))).thenReturn(Optional.of(period));

        // Act
        boolean result = service.isValidTransactionDate(LocalDate.of(2024, 1, 15));

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsValidTransactionDate_NoPeriod() {
        // Arrange
        when(repository.findPeriodByDate(LocalDate.of(2024, 6, 15))).thenReturn(Optional.empty());

        // Act
        boolean result = service.isValidTransactionDate(LocalDate.of(2024, 6, 15));

        // Assert
        assertFalse(result);
    }

    @Test
    void testHasOpenPeriod() {
        // Arrange
        when(repository.hasOpenPeriod()).thenReturn(true);

        // Act
        boolean result = service.hasOpenPeriod();

        // Assert
        assertTrue(result);
        verify(repository, times(1)).hasOpenPeriod();
    }

    @Test
    void testValidateFinancialStatementsIssued_Success() {
        // Arrange - All required statements are present and final
        FinancialReport balanceSheet = new FinancialReport();
        balanceSheet.setReportType(FinancialReport.ReportType.BALANCE_SHEET);
        balanceSheet.setFiscalYear(2024);
        balanceSheet.setFinal(true);

        FinancialReport incomeStatement = new FinancialReport();
        incomeStatement.setReportType(FinancialReport.ReportType.INCOME_STATEMENT);
        incomeStatement.setFiscalYear(2024);
        incomeStatement.setFinal(true);

        when(financialReportRepository.findByFiscalYear(2024))
            .thenReturn(List.of(balanceSheet, incomeStatement));

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> {
            try {
                java.lang.reflect.Method method = AccountingPeriodService.class
                    .getDeclaredMethod("validateFinancialStatementsIssued", Integer.class);
                method.setAccessible(true);
                method.invoke(service, 2024);
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Unwrap the actual exception
                throw e.getCause();
            }
        });
    }

    @Test
    void testValidateFinancialStatementsIssued_MissingBalanceSheet() {
        // Arrange - Only income statement is present
        FinancialReport incomeStatement = new FinancialReport();
        incomeStatement.setReportType(FinancialReport.ReportType.INCOME_STATEMENT);
        incomeStatement.setFiscalYear(2024);
        incomeStatement.setFinal(true);

        when(financialReportRepository.findByFiscalYear(2024))
            .thenReturn(List.of(incomeStatement));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            try {
                java.lang.reflect.Method method = AccountingPeriodService.class
                    .getDeclaredMethod("validateFinancialStatementsIssued", Integer.class);
                method.setAccessible(true);
                method.invoke(service, 2024);
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Unwrap the actual exception
                throw e.getCause();
            }
        });
        assertTrue(exception.getMessage().contains("Balance General"));
        assertTrue(exception.getMessage().contains("MC.6.b"));
    }

    @Test
    void testValidateFinancialStatementsIssued_MissingIncomeStatement() {
        // Arrange - Only balance sheet is present
        FinancialReport balanceSheet = new FinancialReport();
        balanceSheet.setReportType(FinancialReport.ReportType.BALANCE_SHEET);
        balanceSheet.setFiscalYear(2024);
        balanceSheet.setFinal(true);

        when(financialReportRepository.findByFiscalYear(2024))
            .thenReturn(List.of(balanceSheet));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            try {
                java.lang.reflect.Method method = AccountingPeriodService.class
                    .getDeclaredMethod("validateFinancialStatementsIssued", Integer.class);
                method.setAccessible(true);
                method.invoke(service, 2024);
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Unwrap the actual exception
                throw e.getCause();
            }
        });
        assertTrue(exception.getMessage().contains("Estado de Resultados"));
        assertTrue(exception.getMessage().contains("MC.6.b"));
    }

    @Test
    void testValidateFinancialStatementsIssued_NotFinalized() {
        // Arrange - Statements exist but are not finalized
        FinancialReport balanceSheet = new FinancialReport();
        balanceSheet.setReportType(FinancialReport.ReportType.BALANCE_SHEET);
        balanceSheet.setFiscalYear(2024);
        balanceSheet.setFinal(false); // Not final

        FinancialReport incomeStatement = new FinancialReport();
        incomeStatement.setReportType(FinancialReport.ReportType.INCOME_STATEMENT);
        incomeStatement.setFiscalYear(2024);
        incomeStatement.setFinal(false); // Not final

        when(financialReportRepository.findByFiscalYear(2024))
            .thenReturn(List.of(balanceSheet, incomeStatement));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            try {
                java.lang.reflect.Method method = AccountingPeriodService.class
                    .getDeclaredMethod("validateFinancialStatementsIssued", Integer.class);
                method.setAccessible(true);
                method.invoke(service, 2024);
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Unwrap the actual exception
                throw e.getCause();
            }
        });
        assertTrue(exception.getMessage().contains("falta emitir"));
        assertTrue(exception.getMessage().contains("MC.6.b"));
    }

    @Test
    void testValidateFinancialStatementsIssued_NoReportsAtAll() {
        // Arrange - No reports for the fiscal year
        when(financialReportRepository.findByFiscalYear(2024))
            .thenReturn(List.of());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            try {
                java.lang.reflect.Method method = AccountingPeriodService.class
                    .getDeclaredMethod("validateFinancialStatementsIssued", Integer.class);
                method.setAccessible(true);
                method.invoke(service, 2024);
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Unwrap the actual exception
                throw e.getCause();
            }
        });
        assertTrue(exception.getMessage().contains("falta emitir"));
        assertTrue(exception.getMessage().contains("MC.6.b"));
    }
}
