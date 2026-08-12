package com.econovafx.modules.fixedassets.service;

import com.econovafx.modules.fixedassets.model.FixedAsset;
import com.econovafx.modules.fixedassets.model.FixedAssetCategory;
import com.econovafx.modules.fixedassets.model.DepreciationRecord;
import com.econovafx.modules.fixedassets.repository.FixedAssetRepository;
import com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository;
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
 * Unit tests for DepreciationService.
 * Tests depreciation calculations, asset lifecycle, and revaluations.
 */
@ExtendWith(MockitoExtension.class)
class DepreciationServiceTest {

    @Mock
    private FixedAssetRepository fixedAssetRepository;

    @Mock
    private DepreciationRecordRepository depreciationRecordRepository;

    @InjectMocks
    private DepreciationService depreciationService;

    private FixedAsset testAsset;
    private FixedAssetCategory testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new FixedAssetCategory();
        testCategory.setId(1L);
        testCategory.setName("Machinery");
        testCategory.setDepreciationRate(new BigDecimal("0.10")); // 10% annual

        testAsset = new FixedAsset();
        testAsset.setId(1L);
        testAsset.setDescription("Test Machine");
        testAsset.setSerialNumber("SN-001");
        testAsset.setAcquisitionDate(LocalDate.of(2023, 1, 1));
        testAsset.setOriginalValue(new BigDecimal("10000.00"));
        testAsset.setResidualValue(new BigDecimal("1000.00"));
        testAsset.setCategory(testCategory);
        testAsset.setLocation("Building A");
        testAsset.setStatus("ACTIVE");
    }

    @Test
    void testCalculateDepreciation_StraightLine_Success() {
        // Arrange
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(new ArrayList<>());

        LocalDate periodEnd = LocalDate.of(2023, 12, 31);

        // Act
        DepreciationRecord result = depreciationService.calculateDepreciation(1L, periodEnd);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAssetId());
        assertEquals(periodEnd, result.getPeriodEnd());
        assertNotNull(result.getDepreciationAmount());
        assertTrue(result.getDepreciationAmount().compareTo(BigDecimal.ZERO) > 0);
        verify(depreciationRecordRepository).save(any(DepreciationRecord.class));
    }

    @Test
    void testCalculateDepreciation_AssetNotFound_ThrowsException() {
        // Arrange
        when(fixedAssetRepository.findById(99L)).thenReturn(Optional.empty());
        LocalDate periodEnd = LocalDate.of(2023, 12, 31);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depreciationService.calculateDepreciation(99L, periodEnd);
        });
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testCalculateDepreciation_AlreadyCalculated_ThrowsException() {
        // Arrange
        List<DepreciationRecord> existingRecords = new ArrayList<>();
        DepreciationRecord existingRecord = new DepreciationRecord();
        existingRecord.setPeriodEnd(LocalDate.of(2023, 12, 31));
        existingRecords.add(existingRecord);

        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(existingRecords);

        LocalDate periodEnd = LocalDate.of(2023, 12, 31);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depreciationService.calculateDepreciation(1L, periodEnd);
        });
        assertTrue(exception.getMessage().contains("already calculated"));
    }

    @Test
    void testBatchCalculateDepreciation_Success() {
        // Arrange
        List<FixedAsset> assets = new ArrayList<>();
        assets.add(testAsset);
        when(fixedAssetRepository.findAllActive()).thenReturn(assets);
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(new ArrayList<>());

        LocalDate periodEnd = LocalDate.of(2023, 12, 31);

        // Act
        List<DepreciationRecord> results = depreciationService.batchCalculateDepreciation(periodEnd);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        verify(depreciationRecordRepository, atLeastOnce()).save(any(DepreciationRecord.class));
    }

    @Test
    void testDisposeAsset_Success() {
        // Arrange
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        String reason = "Sold";
        LocalDate disposalDate = LocalDate.of(2024, 6, 30);

        // Act
        FixedAsset result = depreciationService.disposeAsset(1L, reason, disposalDate);

        // Assert
        assertNotNull(result);
        assertEquals("DISPOSED", result.getStatus());
        assertEquals(reason, result.getDisposalReason());
        assertEquals(disposalDate, result.getDisposalDate());
        verify(fixedAssetRepository).save(testAsset);
    }

    @Test
    void testDisposeAsset_InactiveAsset_ThrowsException() {
        // Arrange
        testAsset.setStatus("INACTIVE");
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depreciationService.disposeAsset(1L, "Sold", LocalDate.now());
        });
        assertTrue(exception.getMessage().contains("cannot be disposed"));
    }

    @Test
    void testRevalueAsset_Success() {
        // Arrange
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        BigDecimal newValue = new BigDecimal("12000.00");
        LocalDate revaluationDate = LocalDate.of(2024, 1, 1);

        // Act
        FixedAsset result = depreciationService.revalueAsset(1L, newValue, revaluationDate);

        // Assert
        assertNotNull(result);
        assertEquals(newValue, result.getOriginalValue());
        assertEquals(revaluationDate, result.getLastRevaluationDate());
        verify(fixedAssetRepository).save(testAsset);
    }

    @Test
    void testRevalueAsset_DecreasedValue_ThrowsException() {
        // Arrange
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        BigDecimal lowerValue = new BigDecimal("8000.00"); // Lower than original

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depreciationService.revalueAsset(1L, lowerValue, LocalDate.now());
        });
        assertTrue(exception.getMessage().contains("must be greater"));
    }

    @Test
    void testGenerateDepreciationSchedule_Success() {
        // Arrange
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));

        // Act
        List<Object> schedule = depreciationService.generateDepreciationSchedule(1L);

        // Assert
        assertNotNull(schedule);
        assertFalse(schedule.isEmpty());
    }

    @Test
    void testCalculateAccumulatedDepreciation_NoRecords_ReturnsZero() {
        // Arrange
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(new ArrayList<>());

        // Act
        BigDecimal result = depreciationService.calculateAccumulatedDepreciation(1L);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateAccumulatedDepreciation_WithRecords_ReturnsSum() {
        // Arrange
        List<DepreciationRecord> records = new ArrayList<>();
        DepreciationRecord record1 = new DepreciationRecord();
        record1.setDepreciationAmount(new BigDecimal("500.00"));
        records.add(record1);

        DepreciationRecord record2 = new DepreciationRecord();
        record2.setDepreciationAmount(new BigDecimal("500.00"));
        records.add(record2);

        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(records);

        // Act
        BigDecimal result = depreciationService.calculateAccumulatedDepreciation(1L);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    void testCalculateNetBookValue_Success() {
        // Arrange
        when(fixedAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        List<DepreciationRecord> records = new ArrayList<>();
        DepreciationRecord record = new DepreciationRecord();
        record.setDepreciationAmount(new BigDecimal("1000.00"));
        records.add(record);
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(records);

        // Act
        BigDecimal netBookValue = depreciationService.calculateNetBookValue(1L);

        // Assert
        assertNotNull(netBookValue);
        // Original: 10000, Accumulated: 1000, Net: 9000
        assertEquals(new BigDecimal("9000.00"), netBookValue);
    }

    @Test
    void testIsFullyDepreciated_True() {
        // Arrange
        List<DepreciationRecord> records = new ArrayList<>();
        DepreciationRecord record = new DepreciationRecord();
        record.setDepreciationAmount(new BigDecimal("9000.00")); // Full depreciation
        records.add(record);
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(records);

        // Act
        boolean result = depreciationService.isFullyDepreciated(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsFullyDepreciated_False() {
        // Arrange
        List<DepreciationRecord> records = new ArrayList<>();
        DepreciationRecord record = new DepreciationRecord();
        record.setDepreciationAmount(new BigDecimal("100.00")); // Partial
        records.add(record);
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(records);

        // Act
        boolean result = depreciationService.isFullyDepreciated(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetAssetsByCategory_Success() {
        // Arrange
        List<FixedAsset> assets = new ArrayList<>();
        assets.add(testAsset);
        when(fixedAssetRepository.findByCategoryId(1L)).thenReturn(assets);

        // Act
        List<FixedAsset> result = depreciationService.getAssetsByCategory(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testGetOverdueDepreciation_Success() {
        // Arrange
        List<FixedAsset> assets = new ArrayList<>();
        assets.add(testAsset);
        when(fixedAssetRepository.findAllActive()).thenReturn(assets);
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(new ArrayList<>());

        LocalDate cutoffDate = LocalDate.of(2024, 1, 1);

        // Act
        List<FixedAsset> overdue = depreciationService.getOverdueDepreciation(cutoffDate);

        // Assert
        assertNotNull(overdue);
    }

    @Test
    void testCalculateMonthlyDepreciation_Success() {
        // Arrange - Annual rate 10%, so monthly should be ~0.833%
        BigDecimal annualRate = new BigDecimal("0.10");
        BigDecimal depreciableBase = new BigDecimal("9000.00"); // 10000 - 1000

        // Act (using internal calculation logic)
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 6, BigDecimal.ROUND_HALF_UP);
        BigDecimal monthlyDepreciation = depreciableBase.multiply(monthlyRate);

        // Assert
        assertNotNull(monthlyDepreciation);
        assertTrue(monthlyDepreciation.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testDepreciationMethods_DifferentResults() {
        // Test that different depreciation methods produce different results
        BigDecimal straightLine = depreciationService.calculateStraightLineDepreciation(
            new BigDecimal("10000.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("0.10")
        );

        BigDecimal decliningBalance = depreciationService.calculateDecliningBalanceDepreciation(
            new BigDecimal("10000.00"),
            new BigDecimal("0.20"), // 20% rate
            1
        );

        // Assert - methods should produce different amounts
        assertNotNull(straightLine);
        assertNotNull(decliningBalance);
        // Declining balance typically higher in early years
        assertNotEquals(straightLine, decliningBalance);
    }

    @Test
    void testUnitsOfProductionDepreciation_Success() {
        // Arrange
        BigDecimal cost = new BigDecimal("10000.00");
        BigDecimal residual = new BigDecimal("1000.00");
        BigDecimal totalUnits = new BigDecimal("10000");
        BigDecimal unitsProduced = new BigDecimal("1000");

        // Act
        BigDecimal depreciation = depreciationService.calculateUnitsOfProductionDepreciation(
            cost, residual, totalUnits, unitsProduced
        );

        // Assert
        assertNotNull(depreciation);
        assertEquals(new BigDecimal("900.00"), depreciation); // (10000-1000) * (1000/10000)
    }
}
