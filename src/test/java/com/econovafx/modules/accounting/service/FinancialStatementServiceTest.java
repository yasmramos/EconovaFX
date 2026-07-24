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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialStatementServiceTest {

    @Mock
    private FinancialStatementModelRepository modelRepository;

    @Mock
    private FinancialStatementRowRepository rowRepository;

    @Mock
    private AccountRepository accountRepository;

    private FinancialStatementService service;

    @BeforeEach
    void setUp() {
        service = new FinancialStatementService(modelRepository, rowRepository, accountRepository);
    }

    @Test
    void testGenerateStatement_Success() {
        // Arrange
        Long modelId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Balance Sheet");
        model.setDescription("Standard Balance Sheet");

        FinancialStatementRow row1 = new FinancialStatementRow();
        row1.setId(1L);
        row1.setModelId(modelId);
        row1.setRowNumber(1);
        row1.setLabel("Assets");
        row1.setRowType(FinancialStatementRow.RowType.TOTAL);
        row1.setAccountCodesPattern("1*");
        row1.setSignMultiplier(1.0);
        row1.setIndentLevel(0);
        row1.setIsBold(true);
        row1.setIsItalic(false);

        Account account1 = new Account();
        account1.setCode("1001");
        account1.setName("Cash");
        account1.setBalance(new BigDecimal("10000.00"));
        account1.setType(Account.Type.ASSET);

        Account account2 = new Account();
        account2.setCode("1002");
        account2.setName("Bank");
        account2.setBalance(new BigDecimal("50000.00"));
        account2.setType(Account.Type.ASSET);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row1));
        when(accountRepository.findAll()).thenReturn(Arrays.asList(account1, account2));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(model, result.getModel());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertNotNull(result.getRows());
        assertEquals(1, result.getRows().size());
        assertNotNull(result.getGeneratedAt());
        
        FinancialStatementService.StatementRowResult rowResult = result.getRows().get(0);
        assertEquals("Assets", rowResult.getLabel());
        assertEquals(FinancialStatementRow.RowType.TOTAL, rowResult.getRowType());
        assertEquals(new BigDecimal("60000.00"), rowResult.getValue());
        assertEquals(0, rowResult.getIndentLevel());
        assertTrue(rowResult.getIsBold());
        assertFalse(rowResult.getIsItalic());

        verify(modelRepository).findById(modelId);
        verify(rowRepository).findByModelId(modelId);
        verify(accountRepository).findAll();
    }

    @Test
    void testGenerateStatement_ModelNotFound() {
        // Arrange
        Long modelId = 999L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        when(modelRepository.findById(modelId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.generateStatement(modelId, startDate, endDate)
        );
        assertTrue(exception.getMessage().contains("Model not found"));

        verify(modelRepository).findById(modelId);
        verifyNoInteractions(rowRepository, accountRepository);
    }

    @Test
    void testGenerateStatement_WithMultipleRows() {
        // Arrange
        Long modelId = 2L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Income Statement");

        FinancialStatementRow row1 = new FinancialStatementRow();
        row1.setId(1L);
        row1.setModelId(modelId);
        row1.setRowNumber(1);
        row1.setLabel("Revenue");
        row1.setRowType(FinancialStatementRow.RowType.TOTAL);
        row1.setAccountCodesPattern("4*");
        row1.setSignMultiplier(1.0);
        row1.setIndentLevel(0);

        FinancialStatementRow row2 = new FinancialStatementRow();
        row2.setId(2L);
        row2.setModelId(modelId);
        row2.setRowNumber(2);
        row2.setLabel("Cost of Goods Sold");
        row2.setRowType(FinancialStatementRow.RowType.TOTAL);
        row2.setAccountCodesPattern("5*");
        row2.setSignMultiplier(-1.0);
        row2.setIndentLevel(0);

        Account revenueAccount = new Account();
        revenueAccount.setCode("4001");
        revenueAccount.setName("Sales Revenue");
        revenueAccount.setBalance(new BigDecimal("100000.00"));
        revenueAccount.setType(Account.Type.REVENUE);

        Account cogsAccount = new Account();
        cogsAccount.setCode("5001");
        cogsAccount.setName("COGS");
        cogsAccount.setBalance(new BigDecimal("60000.00"));
        cogsAccount.setType(Account.Type.EXPENSE);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Arrays.asList(row1, row2));
        when(accountRepository.findAll()).thenReturn(Arrays.asList(revenueAccount, cogsAccount));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getRows().size());
        
        assertEquals(new BigDecimal("100000.00"), result.getRows().get(0).getValue());
        assertEquals(new BigDecimal("-60000.00"), result.getRows().get(1).getValue());

        verify(modelRepository).findById(modelId);
        verify(rowRepository).findByModelId(modelId);
        verify(accountRepository).findAll();
    }

    @Test
    void testGenerateStatement_WithWildcardPattern() {
        // Arrange
        Long modelId = 3L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Assets Summary");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Current Assets");
        row.setRowType(FinancialStatementRow.RowType.SUBTOTAL);
        row.setAccountCodesPattern("11*");
        row.setSignMultiplier(1.0);
        row.setIndentLevel(1);

        Account cash = new Account();
        cash.setCode("1101");
        cash.setName("Cash on Hand");
        cash.setBalance(new BigDecimal("5000.00"));
        cash.setType(Account.Type.ASSET);

        Account bank = new Account();
        bank.setCode("1102");
        bank.setName("Bank Account");
        bank.setBalance(new BigDecimal("25000.00"));
        bank.setType(Account.Type.ASSET);

        Account nonCurrent = new Account();
        nonCurrent.setCode("1201");
        nonCurrent.setName("Equipment");
        nonCurrent.setBalance(new BigDecimal("50000.00"));
        nonCurrent.setType(Account.Type.ASSET);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Arrays.asList(cash, bank, nonCurrent));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals(new BigDecimal("30000.00"), result.getRows().get(0).getValue());

        verify(modelRepository).findById(modelId);
        verify(rowRepository).findByModelId(modelId);
        verify(accountRepository).findAll();
    }

    @Test
    void testGenerateStatement_WithExactAccountCodePattern() {
        // Arrange
        Long modelId = 4L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Specific Account");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Main Cash Account");
        row.setRowType(FinancialStatementRow.RowType.DETAIL);
        row.setAccountCodesPattern("1001");
        row.setSignMultiplier(1.0);
        row.setIndentLevel(0);

        Account account = new Account();
        account.setCode("1001");
        account.setName("Main Cash");
        account.setBalance(new BigDecimal("15000.00"));
        account.setType(Account.Type.ASSET);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(account));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("15000.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithMultiplePatterns() {
        // Arrange
        Long modelId = 5L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Combined Accounts");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("All Cash Equivalents");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("1001, 1002, 11*");
        row.setSignMultiplier(1.0);
        row.setIndentLevel(0);

        Account cash1 = new Account();
        cash1.setCode("1001");
        cash1.setName("Petty Cash");
        cash1.setBalance(new BigDecimal("1000.00"));

        Account cash2 = new Account();
        cash2.setCode("1002");
        cash2.setName("Main Cash");
        cash2.setBalance(new BigDecimal("2000.00"));

        Account bank = new Account();
        bank.setCode("1101");
        bank.setName("Bank");
        bank.setBalance(new BigDecimal("10000.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Arrays.asList(cash1, cash2, bank));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("13000.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithNegativeSignMultiplier() {
        // Arrange
        Long modelId = 6L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Liabilities");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Total Liabilities");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("2*");
        row.setSignMultiplier(-1.0);
        row.setIndentLevel(0);

        Account liability = new Account();
        liability.setCode("2001");
        liability.setName("Accounts Payable");
        liability.setBalance(new BigDecimal("30000.00"));
        liability.setType(Account.Type.LIABILITY);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(liability));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("-30000.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithNoMatchingAccounts() {
        // Arrange
        Long modelId = 7L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Empty Section");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Investments");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("15*");
        row.setSignMultiplier(1.0);
        row.setIndentLevel(0);

        Account asset = new Account();
        asset.setCode("1001");
        asset.setName("Cash");
        asset.setBalance(new BigDecimal("5000.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(asset));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("0"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithNullAccountCodesPattern() {
        // Arrange
        Long modelId = 8L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Section without Pattern");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Manual Entry");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern(null);
        row.setSignMultiplier(1.0);
        row.setIndentLevel(0);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("0"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithEmptyAccountCodesPattern() {
        // Arrange
        Long modelId = 9L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Section with Empty Pattern");

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Empty Pattern Row");
        row.setRowType(FinancialStatementRow.RowType.TOTAL);
        row.setAccountCodesPattern("");
        row.setSignMultiplier(1.0);
        row.setIndentLevel(0);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("0"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_RowSorting() {
        // Arrange
        Long modelId = 10L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);
        model.setName("Sorted Rows");

        FinancialStatementRow row3 = new FinancialStatementRow();
        row3.setId(3L);
        row3.setModelId(modelId);
        row3.setRowNumber(3);
        row3.setLabel("Third Row");
        row3.setAccountCodesPattern("");

        FinancialStatementRow row1 = new FinancialStatementRow();
        row1.setId(1L);
        row1.setModelId(modelId);
        row1.setRowNumber(1);
        row1.setLabel("First Row");
        row1.setAccountCodesPattern("");

        FinancialStatementRow row2 = new FinancialStatementRow();
        row2.setId(2L);
        row2.setModelId(modelId);
        row2.setRowNumber(2);
        row2.setLabel("Second Row");
        row2.setAccountCodesPattern("");

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Arrays.asList(row3, row1, row2));
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getRows().size());
        assertEquals("First Row", result.getRows().get(0).getLabel());
        assertEquals("Second Row", result.getRows().get(1).getLabel());
        assertEquals("Third Row", result.getRows().get(2).getLabel());
    }

    @Test
    void testMatchesPattern_ExactMatch() {
        // This test verifies the private matchesPattern method indirectly
        Long modelId = 11L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Exact Match Test");
        row.setAccountCodesPattern("1234");
        row.setSignMultiplier(1.0);

        Account matchAccount = new Account();
        matchAccount.setCode("1234");
        matchAccount.setBalance(new BigDecimal("100.00"));

        Account nonMatchAccount = new Account();
        nonMatchAccount.setCode("1235");
        nonMatchAccount.setBalance(new BigDecimal("200.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Arrays.asList(matchAccount, nonMatchAccount));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertEquals(new BigDecimal("100.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testMatchesPattern_WildcardMatch() {
        // This test verifies wildcard pattern matching
        Long modelId = 12L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Wildcard Match Test");
        row.setAccountCodesPattern("1*");
        row.setSignMultiplier(1.0);

        Account match1 = new Account();
        match1.setCode("1000");
        match1.setBalance(new BigDecimal("100.00"));

        Account match2 = new Account();
        match2.setCode("1999");
        match2.setBalance(new BigDecimal("200.00"));

        Account nonMatch = new Account();
        nonMatch.setCode("2000");
        nonMatch.setBalance(new BigDecimal("300.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Arrays.asList(match1, match2, nonMatch));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertEquals(new BigDecimal("300.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testStatementRowResult_GettersSetters() {
        FinancialStatementService.StatementRowResult rowResult = 
            new FinancialStatementService.StatementRowResult();

        rowResult.setLabel("Test Label");
        rowResult.setRowType(FinancialStatementRow.RowType.TOTAL);
        rowResult.setValue(new BigDecimal("100.00"));
        rowResult.setIndentLevel(2);
        rowResult.setIsBold(true);
        rowResult.setIsItalic(false);

        assertEquals("Test Label", rowResult.getLabel());
        assertEquals(FinancialStatementRow.RowType.TOTAL, rowResult.getRowType());
        assertEquals(new BigDecimal("100.00"), rowResult.getValue());
        assertEquals(2, rowResult.getIndentLevel());
        assertTrue(rowResult.getIsBold());
        assertFalse(rowResult.getIsItalic());
    }

    @Test
    void testFinancialStatementResult_GettersSetters() {
        FinancialStatementService.FinancialStatementResult result = 
            new FinancialStatementService.FinancialStatementResult();

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(1L);
        model.setName("Test Model");

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        LocalDate generatedAt = LocalDate.of(2024, 2, 1);

        List<FinancialStatementService.StatementRowResult> rows = 
            Collections.singletonList(new FinancialStatementService.StatementRowResult());

        result.setModel(model);
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setRows(rows);
        result.setGeneratedAt(generatedAt);

        assertEquals(model, result.getModel());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertEquals(rows, result.getRows());
        assertEquals(generatedAt, result.getGeneratedAt());
    }

    @Test
    void testGenerateStatement_WithComplexWildcardPattern() {
        // Arrange
        Long modelId = 13L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Complex Pattern");
        row.setAccountCodesPattern("1*01");
        row.setSignMultiplier(1.0);

        Account match1 = new Account();
        match1.setCode("1001");
        match1.setBalance(new BigDecimal("100.00"));

        Account match2 = new Account();
        match2.setCode("1101");
        match2.setBalance(new BigDecimal("200.00"));

        Account match3 = new Account();
        match3.setCode("1201");
        match3.setBalance(new BigDecimal("300.00"));

        Account nonMatch = new Account();
        nonMatch.setCode("1002");
        nonMatch.setBalance(new BigDecimal("400.00"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Arrays.asList(match1, match2, match3, nonMatch));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertEquals(new BigDecimal("600.00"), result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithZeroBalance() {
        // Arrange
        Long modelId = 14L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Zero Balance");
        row.setAccountCodesPattern("1*");
        row.setSignMultiplier(1.0);

        Account zeroBalance = new Account();
        zeroBalance.setCode("1001");
        zeroBalance.setBalance(BigDecimal.ZERO);

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(zeroBalance));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertEquals(BigDecimal.ZERO, result.getRows().get(0).getValue());
    }

    @Test
    void testGenerateStatement_WithDecimalValues() {
        // Arrange
        Long modelId = 15L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        FinancialStatementModel model = new FinancialStatementModel();
        model.setId(modelId);

        FinancialStatementRow row = new FinancialStatementRow();
        row.setId(1L);
        row.setModelId(modelId);
        row.setRowNumber(1);
        row.setLabel("Decimal Values");
        row.setAccountCodesPattern("1*");
        row.setSignMultiplier(1.0);

        Account account1 = new Account();
        account1.setCode("1001");
        account1.setBalance(new BigDecimal("100.50"));

        Account account2 = new Account();
        account2.setCode("1002");
        account2.setBalance(new BigDecimal("200.75"));

        when(modelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(rowRepository.findByModelId(modelId)).thenReturn(Collections.singletonList(row));
        when(accountRepository.findAll()).thenReturn(Arrays.asList(account1, account2));

        // Act
        FinancialStatementService.FinancialStatementResult result = 
            service.generateStatement(modelId, startDate, endDate);

        // Assert
        assertEquals(new BigDecimal("301.25"), result.getRows().get(0).getValue());
    }
}
