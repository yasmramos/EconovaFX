package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.FinancialStatementModelRepository;
import com.econovafx.modules.accounting.repository.FinancialStatementRowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialStatementServiceTest {

    @Mock
    private FinancialStatementModelRepository modelRepository;

    @Mock
    private FinancialStatementRowRepository rowRepository;

    @Mock
    private AccountRepository accountRepository;

    private FinancialStatementService financialStatementService;

    @BeforeEach
    void setUp() {
        financialStatementService = new FinancialStatementService(
            modelRepository,
            rowRepository,
            accountRepository
        );
    }

    @Test
    void testGenerateStatement_ValidModel_ReturnsResult() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Balance Sheet");
        model.setStatementType(FinancialStatementModel.StatementType.BALANCE_SHEET);

        FinancialStatementRow row1 = new FinancialStatementRow();
        row1.setId(1L);
        row1.setModelId(modelId);
        row1.setRowNumber(1);
        row1.setLabel("Assets");
        row1.setRowType(FinancialStatementRow.RowType.TOTAL);
        row1.setAccountCodesPattern("1.*");
        row1.setSignMultiplier(1);
        row1.setIndentLevel(0);
        row1.setIsBold(true);
        row1.setIsItalic(false);

        Account account1 = new Account();
        account1.setCode("1.1.1");
        account1.setName("Cash");
        account1.setBalance(new BigDecimal("10000.00"));

        Account account2 = new Account();
        account2.setCode("1.1.2");
        account2.setName("Bank");
        account2.setBalance(new BigDecimal("50000.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row1));
        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(model, result.getModel());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertNotNull(result.getRows());
        assertEquals(1, result.getRows().size());
        verify(modelRepository).findById(modelId);
        verify(rowRepository).findByModelId(modelId);
        verify(accountRepository).findAll();
    }

    @Test
    void testGenerateStatement_ModelNotFound_ThrowsException() {
        // Arrange
        Long modelId = 999L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> financialStatementService.generateStatement(modelId, startDate, endDate)
        );
        assertTrue(exception.getMessage().contains("Model not found"));
        verify(modelRepository).findById(modelId);
    }

    @Test
    void testGenerateStatement_WithMultipleRows_ReturnsAllRows() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Income Statement");
        model.setStatementType(FinancialStatementModel.StatementType.INCOME_STATEMENT);

        FinancialStatementRow row1 = new FinancialStatementRow();
        row1.setId(1L);
        row1.setModelId(modelId);
        row1.setRowNumber(1);
        row1.setLabel("Revenue");
        row1.setRowType(FinancialStatementRow.RowType.SUBTOTAL);
        row1.setAccountCodesPattern("4.1.*");
        row1.setSignMultiplier(1);

        FinancialStatementRow row2 = new FinancialStatementRow();
        row2.setId(2L);
        row2.setModelId(modelId);
        row2.setRowNumber(2);
        row2.setLabel("Cost of Goods Sold");
        row2.setRowType(FinancialStatementRow.RowType.SUBTOTAL);
        row2.setAccountCodesPattern("5.1.*");
        row2.setSignMultiplier(-1);

        FinancialStatementRow row3 = new FinancialStatementRow();
        row3.setId(3L);
        row3.setModelId(modelId);
        row3.setRowNumber(3);
        row3.setLabel("Net Income");
        row3.setRowType(FinancialStatementRow.RowType.TOTAL);
        row3.setAccountCodesPattern(null);
        row3.setSignMultiplier(1);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row1, row2, row3));
        when(accountRepository.findAll()).thenReturn(List.of());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getRows().size());
        assertEquals("Revenue", result.getRows().get(0).getLabel());
        assertEquals("Cost of Goods Sold", result.getRows().get(1).getLabel());
        assertEquals("Net Income", result.getRows().get(2).getLabel());
    }

    @Test
    void testGenerateStatement_WithExactAccountCodeMatch_CalculatesCorrectly() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Test Model");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Cash Balance");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("1.1.1");
        row.setSignMultiplier(1);
        row.setIndentLevel(0);

        Account account = new Account();
        account.setCode("1.1.1");
        account.setName("Cash");
        account.setBalance(new BigDecimal("25000.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of(account));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals(new BigDecimal("25000.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithWildcardPattern_MatchesAccounts() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Test Model");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("All Assets");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("1.*");
        row.setSignMultiplier(1);

        Account account1 = new Account();
        account1.setCode("1.1.1");
        account1.setBalance(new BigDecimal("10000.00"));

        Account account2 = new Account();
        account2.setCode("1.1.2");
        account2.setBalance(new BigDecimal("20000.00"));

        Account account3 = new Account();
        account3.setCode("1.2.1");
        account3.setBalance(new BigDecimal("30000.00"));

        Account account4 = new Account();
        account4.setCode("2.1.1");
        account4.setBalance(new BigDecimal("40000.00")); // Should not match

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of(account1, account2, account3, account4));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        // Should sum accounts 1.1.1, 1.1.2, 1.2.1 (all starting with "1.")
        assertEquals(new BigDecimal("60000.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithNegativeMultiplier_AppliesCorrectly() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Expenses (negative)");
        row.setRowType(FinancialStatementRow.RowType.SUBTOTAL);
        row.setAccountCodesPattern("5.1.1");
        row.setSignMultiplier(-1);

        Account account = new Account();
        account.setCode("5.1.1");
        account.setBalance(new BigDecimal("15000.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of(account));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("-15000.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithNoAccountPattern_ReturnsZero() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Calculated Total");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern(null);
        row.setSignMultiplier(1);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithEmptyAccountPattern_ReturnsZero() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Empty Pattern");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("");
        row.setSignMultiplier(1);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithMultiplePatterns_SumsAllMatches() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Multiple Accounts");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("1.1.1, 1.1.2, 1.2.1");
        row.setSignMultiplier(1);

        Account account1 = new Account();
        account1.setCode("1.1.1");
        account1.setBalance(new BigDecimal("1000.00"));

        Account account2 = new Account();
        account2.setCode("1.1.2");
        account2.setBalance(new BigDecimal("2000.00"));

        Account account3 = new Account();
        account3.setCode("1.2.1");
        account3.setBalance(new BigDecimal("3000.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of(account1, account2, account3));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("6000.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_RowFormatting_PreservedInResult() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Bold Italic Row");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("1.1.1");
        row.setIndentLevel(2);
        row.setIsBold(true);
        row.setIsItalic(true);

        Account account = new Account();
        account.setCode("1.1.1");
        account.setBalance(new BigDecimal("5000.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of(account));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        FinancialStatementService.StatementRowResult resultRow = result.getRows().get(0);
        assertEquals("Bold Italic Row", resultRow.getLabel());
        assertEquals(FinancialStatementRow.RowType.TOTAL, resultRow.getRowType());
        assertEquals(2, resultRow.getIndentLevel());
        assertTrue(resultRow.getIsBold());
        assertTrue(resultRow.getIsItalic());
    }

    @Test
    void testGenerateStatement_GeneratedAt_IsSet() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Test");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("1.1.1");

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result.getGeneratedAt());
        assertTrue(result.getGeneratedAt().isBefore(LocalDate.now().plusDays(1)));
    }

    @Test
    void testGenerateStatement_EmptyAccountsList_ReturnsZeroValues() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("No Accounts");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("1.*");

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row));
        when(accountRepository.findAll()).thenReturn(List.of());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals(BigDecimal.ZERO, result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithSortedRows_ReturnsInOrder() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row3 = new FinancialStatementRow();
        row3.setId(3L);
        row3.setModelId(modelId);
        row3.setRowNumber(3);
        row3.setLabel("Third");

        FinancialStatementRow row1 = new FinancialStatementRow();
        row1.setId(1L);
        row1.setModelId(modelId);
        row1.setRowNumber(1);
        row1.setLabel("First");

        FinancialStatementRow row2 = new FinancialStatementRow();
        row2.setId(2L);
        row2.setModelId(modelId);
        row2.setRowNumber(2);
        row2.setLabel("Second");

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(List.of(row3, row1, row2));
        when(accountRepository.findAll()).thenReturn(List.of());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            financialStatementService.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getRows().size());
        assertEquals("First", result.getRows().get(0).getLabel());
        assertEquals("Second", result.getRows().get(1).getLabel());
        assertEquals("Third", result.getRows().get(2).getLabel());
    }
}
