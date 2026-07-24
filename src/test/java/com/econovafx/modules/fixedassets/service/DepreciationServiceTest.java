package com.econovafx.modules.fixedassets.service;

import com.econovafx.modules.fixedassets.model.*;
import com.econovafx.modules.fixedassets.repository.*;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.core.service.AuditService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepreciationServiceTest {

    @Mock
    private FixedAssetRepository fixedAssetRepository;

    @Mock
    private DepreciationRecordRepository depreciationRecordRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditService auditService;

    private DepreciationService depreciationService;

    @BeforeEach
    void setUp() {
        depreciationService = new DepreciationService(
            fixedAssetRepository,
            depreciationRecordRepository,
            transactionService,
            accountRepository,
            auditService
        );
    }

    @Test
    void testCalculateMonthlyDepreciation_ValidAsset_ReturnsCorrectAmount() {
        // Arrange
        FixedAssetCategory category = new FixedAssetCategory();
        category.setName("Vehículos");
        category.setUsefulLifeYears(5); // 60 meses

        FixedAsset asset = new FixedAsset();
        asset.setAcquisitionCost(new BigDecimal("60000.00"));
        asset.setCategory(category);

        // Act
        BigDecimal monthlyDepreciation = depreciationService.calculateMonthlyDepreciation(asset);

        // Assert
        assertEquals(new BigDecimal("1000.00"), monthlyDepreciation);
    }

    @Test
    void testCalculateMonthlyDepreciation_WithZeroUsefulLife_ThrowsException() {
        // Arrange
        FixedAssetCategory category = new FixedAssetCategory();
        category.setName("Categoría Inválida");
        category.setUsefulLifeYears(0);

        FixedAsset asset = new FixedAsset();
        asset.setAcquisitionCost(new BigDecimal("10000.00"));
        asset.setCategory(category);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> depreciationService.calculateMonthlyDepreciation(asset)
        );
        assertTrue(exception.getMessage().contains("vida útil"));
    }

    @Test
    void testCalculateMonthlyDepreciation_WithNullUsefulLife_ThrowsException() {
        // Arrange
        FixedAssetCategory category = new FixedAssetCategory();
        category.setName("Categoría Sin Vida Útil");
        category.setUsefulLifeYears(null);

        FixedAsset asset = new FixedAsset();
        asset.setAcquisitionCost(new BigDecimal("10000.00"));
        asset.setCategory(category);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> depreciationService.calculateMonthlyDepreciation(asset)
        );
        assertTrue(exception.getMessage().contains("vida útil"));
    }

    @Test
    void testProcessMonthlyDepreciation_Success_CreatesRecords() {
        // Arrange
        Integer year = 2024;
        Integer month = 6;
        String username = "testuser";

        FixedAssetCategory category = new FixedAssetCategory();
        category.setName("Equipos");
        category.setUsefulLifeYears(10);
        category.setDepreciationExpenseAccountCode("6.1.1");
        category.setAccumulatedDepreciationAccountCode("1.2.2");

        FixedAsset asset1 = new FixedAsset();
        asset1.setId(1L);
        asset1.setCode("ACT-001");
        asset1.setName("Computadora");
        asset1.setAcquisitionCost(new BigDecimal("12000.00"));
        asset1.setCategory(category);
        asset1.setAccumulatedDepreciation(BigDecimal.ZERO);
        asset1.setPurchaseDate(LocalDate.of(2024, 1, 1));
        asset1.setStatus("ACTIVE");

        FixedAsset asset2 = new FixedAsset();
        asset2.setId(2L);
        asset2.setCode("ACT-002");
        asset2.setName("Impresora");
        asset2.setAcquisitionCost(new BigDecimal("6000.00"));
        asset2.setCategory(category);
        asset2.setAccumulatedDepreciation(BigDecimal.ZERO);
        asset2.setPurchaseDate(LocalDate.of(2024, 1, 1));
        asset2.setStatus("ACTIVE");

        when(depreciationRecordRepository.findByYearAndMonth(year, month)).thenReturn(List.of());
        when(fixedAssetRepository.findAllActive()).thenReturn(List.of(asset1, asset2));
        when(fixedAssetRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(depreciationRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<DepreciationRecord> records = depreciationService.processMonthlyDepreciation(year, month, username);

        // Assert
        assertNotNull(records);
        assertEquals(2, records.size());
        verify(depreciationRecordRepository, times(2)).save(any(DepreciationRecord.class));
        verify(fixedAssetRepository, times(2)).update(any(FixedAsset.class));
    }

    @Test
    void testProcessMonthlyDepreciation_AlreadyProcessed_ThrowsException() {
        // Arrange
        Integer year = 2024;
        Integer month = 6;
        String username = "testuser";

        DepreciationRecord existingRecord = new DepreciationRecord();
        existingRecord.setId(1L);

        when(depreciationRecordRepository.findByYearAndMonth(year, month))
            .thenReturn(List.of(existingRecord));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> depreciationService.processMonthlyDepreciation(year, month, username)
        );
        assertTrue(exception.getMessage().contains("ya fue procesada"));
    }

    @Test
    void testPostDepreciationToAccounting_Success_CreatesTransaction() {
        // Arrange
        Long recordId = 1L;
        String username = "accountant";

        FixedAssetCategory category = new FixedAssetCategory();
        category.setDepreciationExpenseAccountCode("6.1.1");
        category.setAccumulatedDepreciationAccountCode("1.2.2");

        FixedAsset asset = new FixedAsset();
        asset.setId(1L);
        asset.setCode("ACT-001");
        asset.setName("Vehículo");
        asset.setCategory(category);

        DepreciationRecord record = new DepreciationRecord();
        record.setId(recordId);
        record.setFixedAsset(asset);
        record.setDepreciationAmount(new BigDecimal("500.00"));
        record.setProcessingDate(LocalDate.of(2024, 6, 1));
        record.setYear(2024);
        record.setMonth(6);
        record.setPosted(false);

        Account expenseAccount = new Account();
        expenseAccount.setId(100L);
        expenseAccount.setCode("6.1.1");
        expenseAccount.setName("Gasto Depreciación");

        Account accumulatedAccount = new Account();
        accumulatedAccount.setId(101L);
        accumulatedAccount.setCode("1.2.2");
        accumulatedAccount.setName("Depreciación Acumulada");

        Transaction mockTransaction = new Transaction();
        mockTransaction.setId(999L);

        when(depreciationRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(accountRepository.findByCode("6.1.1")).thenReturn(Optional.of(expenseAccount));
        when(accountRepository.findByCode("1.2.2")).thenReturn(Optional.of(accumulatedAccount));
        when(transactionService.createTransaction(any(), any(), eq(username))).thenReturn(mockTransaction);
        when(depreciationRecordRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Transaction result = depreciationService.postDepreciationToAccounting(recordId, username);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getId());
        assertTrue(record.isPosted());
        verify(depreciationRecordRepository).update(record);
        verify(transactionService).createTransaction(any(), any(), eq(username));
    }

    @Test
    void testPostDepreciationToAccounting_NotFound_ThrowsException() {
        // Arrange
        Long recordId = 999L;
        String username = "user";

        when(depreciationRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> depreciationService.postDepreciationToAccounting(recordId, username)
        );
        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    void testPostDepreciationToAccounting_AlreadyPosted_ThrowsException() {
        // Arrange
        Long recordId = 1L;
        String username = "user";

        DepreciationRecord record = new DepreciationRecord();
        record.setId(recordId);
        record.setPosted(true);

        when(depreciationRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> depreciationService.postDepreciationToAccounting(recordId, username)
        );
        assertTrue(exception.getMessage().contains("ya fue contabilizado"));
    }

    @Test
    void testPostDepreciationToAccounting_MissingExpenseAccount_ThrowsException() {
        // Arrange
        Long recordId = 1L;
        String username = "user";

        FixedAssetCategory category = new FixedAssetCategory();
        category.setDepreciationExpenseAccountCode(null);
        category.setAccumulatedDepreciationAccountCode("1.2.2");

        FixedAsset asset = new FixedAsset();
        asset.setCategory(category);

        DepreciationRecord record = new DepreciationRecord();
        record.setId(recordId);
        record.setFixedAsset(asset);
        record.setPosted(false);

        when(depreciationRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> depreciationService.postDepreciationToAccounting(recordId, username)
        );
        assertTrue(exception.getMessage().contains("cuenta de gasto"));
    }

    @Test
    void testPostDepreciationToAccounting_MissingAccumulatedAccount_ThrowsException() {
        // Arrange
        Long recordId = 1L;
        String username = "user";

        FixedAssetCategory category = new FixedAssetCategory();
        category.setDepreciationExpenseAccountCode("6.1.1");
        category.setAccumulatedDepreciationAccountCode(null);

        FixedAsset asset = new FixedAsset();
        asset.setCategory(category);

        DepreciationRecord record = new DepreciationRecord();
        record.setId(recordId);
        record.setFixedAsset(asset);
        record.setPosted(false);

        when(depreciationRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(accountRepository.findByCode("6.1.1")).thenReturn(Optional.of(new Account()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> depreciationService.postDepreciationToAccounting(recordId, username)
        );
        assertTrue(exception.getMessage().contains("depreciación acumulada"));
    }

    @Test
    void testPostAllDepreciationForPeriod_Success_PostsAllRecords() {
        // Arrange
        Integer year = 2024;
        Integer month = 6;
        String username = "accountant";

        DepreciationRecord record1 = new DepreciationRecord();
        record1.setId(1L);
        record1.setPosted(false);

        DepreciationRecord record2 = new DepreciationRecord();
        record2.setId(2L);
        record2.setPosted(false);

        DepreciationRecord record3 = new DepreciationRecord();
        record3.setId(3L);
        record3.setPosted(true); // Ya contabilizado

        when(depreciationRecordRepository.findByYearAndMonth(year, month))
            .thenReturn(List.of(record1, record2, record3));

        when(depreciationRecordRepository.findById(1L)).thenReturn(Optional.of(record1));
        when(depreciationRecordRepository.findById(2L)).thenReturn(Optional.of(record2));

        // Mock para evitar errores al crear transacciones
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.of(new Account()));
        when(transactionService.createTransaction(any(), any(), any())).thenReturn(new Transaction());

        // Act
        int postedCount = depreciationService.postAllDepreciationForPeriod(year, month, username);

        // Assert
        assertEquals(2, postedCount); // Solo 2 no estaban contabilizados
        assertTrue(record1.isPosted());
        assertTrue(record2.isPosted());
        verify(depreciationRecordRepository, times(2)).update(any(DepreciationRecord.class));
    }

    @Test
    void testGetDepreciationReport_ReturnsRecordsForPeriod() {
        // Arrange
        Integer year = 2024;
        Integer month = 6;

        DepreciationRecord record1 = new DepreciationRecord();
        record1.setId(1L);
        record1.setYear(year);
        record1.setMonth(month);

        DepreciationRecord record2 = new DepreciationRecord();
        record2.setId(2L);
        record2.setYear(year);
        record2.setMonth(month);

        when(depreciationRecordRepository.findByYearAndMonth(year, month))
            .thenReturn(List.of(record1, record2));

        // Act
        List<DepreciationRecord> result = depreciationService.getDepreciationReport(year, month);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(depreciationRecordRepository).findByYearAndMonth(year, month);
    }

    @Test
    void testGetAssetDepreciationHistory_ReturnsAllRecordsForAsset() {
        // Arrange
        Long assetId = 1L;

        DepreciationRecord record1 = new DepreciationRecord();
        record1.setId(1L);
        record1.setFixedAssetId(assetId);

        DepreciationRecord record2 = new DepreciationRecord();
        record2.setId(2L);
        record2.setFixedAssetId(assetId);

        when(depreciationRecordRepository.findByFixedAssetId(assetId))
            .thenReturn(List.of(record1, record2));

        // Act
        List<DepreciationRecord> result = depreciationService.getAssetDepreciationHistory(assetId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(depreciationRecordRepository).findByFixedAssetId(assetId);
    }

    @Test
    void testCalculateMonthlyDepreciation_Rounding_HalfUp() {
        // Arrange
        FixedAssetCategory category = new FixedAssetCategory();
        category.setName("Muebles");
        category.setUsefulLifeYears(7); // 84 meses

        FixedAsset asset = new FixedAsset();
        asset.setAcquisitionCost(new BigDecimal("10000.00"));
        asset.setCategory(category);

        // Act
        BigDecimal monthlyDepreciation = depreciationService.calculateMonthlyDepreciation(asset);

        // Assert
        // 10000 / 84 = 119.047619... debe redondear a 119.05 (HALF_UP)
        assertEquals(new BigDecimal("119.05"), monthlyDepreciation);
    }

    @Test
    void testProcessMonthlyDepreciation_InactiveAsset_Skipped() {
        // Arrange
        Integer year = 2024;
        Integer month = 6;
        String username = "testuser";

        FixedAssetCategory category = new FixedAssetCategory();
        category.setName("Equipos");
        category.setUsefulLifeYears(5);

        FixedAsset activeAsset = new FixedAsset();
        activeAsset.setId(1L);
        activeAsset.setStatus("ACTIVE");
        activeAsset.setAcquisitionCost(new BigDecimal("5000.00"));
        activeAsset.setCategory(category);

        FixedAsset inactiveAsset = new FixedAsset();
        inactiveAsset.setId(2L);
        inactiveAsset.setStatus("INACTIVE");
        inactiveAsset.setAcquisitionCost(new BigDecimal("3000.00"));
        inactiveAsset.setCategory(category);

        when(depreciationRecordRepository.findByYearAndMonth(year, month)).thenReturn(List.of());
        when(fixedAssetRepository.findAllActive()).thenReturn(List.of(activeAsset));

        // Act
        List<DepreciationRecord> records = depreciationService.processMonthlyDepreciation(year, month, username);

        // Assert
        assertEquals(1, records.size());
        assertEquals(1L, records.get(0).getFixedAsset().getId());
    }
}
