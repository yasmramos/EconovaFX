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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private Transaction mockTransaction;

    @Mock
    private Account mockAccount;

    private DepreciationService service;

    private FixedAsset asset;
    private FixedAssetCategory category;

    @BeforeEach
    void setUp() {
        service = new DepreciationService(
            fixedAssetRepository,
            depreciationRecordRepository,
            transactionService,
            accountRepository,
            auditService
        );

        category = new FixedAssetCategory();
        category.setId(1L);
        category.setName("Vehicles");
        category.setUsefulLifeYears(5);
        category.setDepreciationExpenseAccountCode("599-001");
        category.setAccumulatedDepreciationAccountCode("189-001");

        asset = new FixedAsset();
        asset.setId(1L);
        asset.setCode("VEH-001");
        asset.setName("Toyota Hilux");
        asset.setCategory(category);
        asset.setAcquisitionCost(BigDecimal.valueOf(50000));
        asset.setAccumulatedDepreciation(BigDecimal.ZERO);
        asset.setNetBookValue(BigDecimal.valueOf(50000));
        asset.setStatus(FixedAsset.AssetStatus.ACTIVE);
    }

    @Test
    void testCalculateMonthlyDepreciation() {
        BigDecimal monthlyDepreciation = service.calculateMonthlyDepreciation(asset);
        assertNotNull(monthlyDepreciation);
        assertEquals(new BigDecimal("833.33"), monthlyDepreciation);
    }

    @Test
    void testCalculateMonthlyDepreciation_NoUsefulLife() {
        category.setUsefulLifeYears(null);
        asset.setCategory(category);
        assertThrows(IllegalArgumentException.class, () -> 
            service.calculateMonthlyDepreciation(asset)
        );
    }

    @Test
    void testProcessMonthlyDepreciation_Success() {
        Integer year = 2024;
        Integer month = 1;
        String username = "testuser";

        when(fixedAssetRepository.findAssetsForDepreciation()).thenReturn(List.of(asset));
        when(depreciationRecordRepository.findByAssetAndPeriod(asset.getId(), year, month))
            .thenReturn(Optional.empty());
        when(depreciationRecordRepository.save(any(DepreciationRecord.class)))
            .thenAnswer(invocation -> {
                DepreciationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return record;
            });
        when(fixedAssetRepository.update(any(FixedAsset.class))).thenReturn(true);
        when(depreciationRecordRepository.findByYearAndMonth(year, month)).thenReturn(List.of());

        List<DepreciationRecord> result = service.processMonthlyDepreciation(year, month, username);

        assertNotNull(result);
        verify(fixedAssetRepository, times(1)).findAssetsForDepreciation();
        verify(depreciationRecordRepository, times(1)).save(any(DepreciationRecord.class));
        verify(fixedAssetRepository, times(1)).update(any(FixedAsset.class));
    }

    @Test
    void testProcessMonthlyDepreciation_AlreadyProcessed() {
        Integer year = 2024;
        Integer month = 1;
        String username = "testuser";

        DepreciationRecord existingRecord = new DepreciationRecord();
        existingRecord.setId(1L);
        when(depreciationRecordRepository.findByYearAndMonth(year, month))
            .thenReturn(List.of(existingRecord));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            service.processMonthlyDepreciation(year, month, username)
        );
        assertTrue(exception.getMessage().contains("ya fue procesada"));
    }

    @Test
    void testPostDepreciationToAccounting_Success() {
        DepreciationRecord record = new DepreciationRecord();
        record.setId(1L);
        record.setFixedAsset(asset);
        record.setDepreciationAmount(BigDecimal.valueOf(833.33));
        record.setPosted(false);
        record.setYear(2024);
        record.setMonth(1);

        when(depreciationRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.of(mockAccount));
        when(transactionService.createTransaction(any(), any(), anyString())).thenReturn(mockTransaction);

        Transaction result = service.postDepreciationToAccounting(1L, "testuser");

        assertNotNull(result);
        verify(depreciationRecordRepository, times(1)).findById(1L);
        verify(transactionService, times(1)).createTransaction(any(), any(), anyString());
        verify(depreciationRecordRepository, times(1)).update(record);
        assertTrue(record.isPosted());
    }

    @Test
    void testPostDepreciationToAccounting_NotFound() {
        when(depreciationRecordRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> 
            service.postDepreciationToAccounting(999L, "testuser")
        );
    }

    @Test
    void testPostDepreciationToAccounting_AlreadyPosted() {
        DepreciationRecord record = new DepreciationRecord();
        record.setId(1L);
        record.setPosted(true);
        when(depreciationRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        assertThrows(IllegalStateException.class, () -> 
            service.postDepreciationToAccounting(1L, "testuser")
        );
    }

    @Test
    void testGetDepreciationRecordsByAsset() {
        DepreciationRecord record1 = new DepreciationRecord();
        record1.setId(1L);
        DepreciationRecord record2 = new DepreciationRecord();
        record2.setId(2L);
        when(depreciationRecordRepository.findByAssetId(1L)).thenReturn(List.of(record1, record2));

        List<DepreciationRecord> result = service.getDepreciationRecordsByAsset(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(depreciationRecordRepository, times(1)).findByAssetId(1L);
    }

    @Test
    void testGetTotalDepreciationByAsset() {
        when(depreciationRecordRepository.getTotalDepreciationByAsset(1L))
            .thenReturn(BigDecimal.valueOf(5000));

        BigDecimal result = service.getTotalDepreciationByAsset(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(5000), result);
        verify(depreciationRecordRepository, times(1)).getTotalDepreciationByAsset(1L);
    }

    @Test
    void testGetAllDepreciationRecords() {
        DepreciationRecord record1 = new DepreciationRecord();
        record1.setId(1L);
        when(depreciationRecordRepository.findAll()).thenReturn(List.of(record1));

        List<DepreciationRecord> result = service.getAllDepreciationRecords();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(depreciationRecordRepository, times(1)).findAll();
    }

    @Test
    void testCalculateMonthlyDepreciation_WithZeroCost() {
        asset.setAcquisitionCost(BigDecimal.ZERO);
        BigDecimal monthlyDepreciation = service.calculateMonthlyDepreciation(asset);
        assertNotNull(monthlyDepreciation);
        assertEquals(BigDecimal.ZERO, monthlyDepreciation);
    }
}
