package com.econovafx.modules.billing.service;

import com.econovafx.modules.billing.model.BillingSeries;
import com.econovafx.modules.billing.repository.BillingSeriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequentialNumberServiceTest {

    @Mock
    private BillingSeriesRepository billingSeriesRepository;

    private SequentialNumberService sequentialNumberService;

    @BeforeEach
    void setUp() {
        sequentialNumberService = new SequentialNumberService(billingSeriesRepository);
    }

    @Test
    void testGetNextSequentialNumber_WhenSeriesExists_Success() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 1, 1, 1000, true);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        doNothing().when(billingSeriesRepository).update(any(BillingSeries.class));
        
        int result = sequentialNumberService.getNextSequentialNumber(seriesId);
        
        assertEquals(1, result);
        assertEquals(2, series.getCurrentNumber());
        verify(billingSeriesRepository).update(series);
    }

    @Test
    void testGetNextSequentialNumber_WhenSeriesNotExists_ThrowsException() {
        Long seriesId = 999L;
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sequentialNumberService.getNextSequentialNumber(seriesId);
        });
        
        assertTrue(exception.getMessage().contains("Serie de facturación no encontrada"));
        verify(billingSeriesRepository, never()).update(any());
    }

    @Test
    void testGetNextSequentialNumber_WhenSeriesInactive_ThrowsException() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 1, 1, 1000, false);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            sequentialNumberService.getNextSequentialNumber(seriesId);
        });
        
        assertTrue(exception.getMessage().contains("inactiva"));
        verify(billingSeriesRepository, never()).update(any());
    }

    @Test
    void testGetNextSequentialNumber_WhenLimitReached_ThrowsException() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 1000, 1, 1000, true);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            sequentialNumberService.getNextSequentialNumber(seriesId);
        });
        
        assertTrue(exception.getMessage().contains("Se alcanzó el límite"));
        verify(billingSeriesRepository, never()).update(any());
    }

    @Test
    void testGenerateDocumentNumber_WhenSeriesExists_Success() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 123, 1, 1000, true);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        doNothing().when(billingSeriesRepository).update(any(BillingSeries.class));
        
        String result = sequentialNumberService.generateDocumentNumber(seriesId);
        
        assertEquals("A-000123", result);
        assertEquals(124, series.getCurrentNumber());
        verify(billingSeriesRepository).update(series);
    }

    @Test
    void testGenerateDocumentNumber_WithDifferentSeriesCode_Success() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "B", 50, 1, 1000, true);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        doNothing().when(billingSeriesRepository).update(any(BillingSeries.class));
        
        String result = sequentialNumberService.generateDocumentNumber(seriesId);
        
        assertEquals("B-000050", result);
    }

    @Test
    void testIsValidNumberForSeries_WhenNumberInRange_ReturnsTrue() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 100, 1, 1000, true);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        boolean result = sequentialNumberService.isValidNumberForSeries(seriesId, 500);
        
        assertTrue(result);
    }

    @Test
    void testIsValidNumberForSeries_WhenNumberBelowRange_ReturnsFalse() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 100, 100, 1000, true);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        boolean result = sequentialNumberService.isValidNumberForSeries(seriesId, 50);
        
        assertFalse(result);
    }

    @Test
    void testIsValidNumberForSeries_WhenNumberAboveRange_ReturnsFalse() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 100, 1, 1000, true);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        boolean result = sequentialNumberService.isValidNumberForSeries(seriesId, 1500);
        
        assertFalse(result);
    }

    @Test
    void testIsValidNumberForSeries_WhenSeriesNotExists_ReturnsFalse() {
        Long seriesId = 999L;
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.empty());
        
        boolean result = sequentialNumberService.isValidNumberForSeries(seriesId, 100);
        
        assertFalse(result);
    }

    @Test
    void testResetCounter_WithValidNumber_Success() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 500, 1, 1000, true);
        int newStartNumber = 100;
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        doNothing().when(billingSeriesRepository).update(any(BillingSeries.class));
        
        sequentialNumberService.resetCounter(seriesId, newStartNumber);
        
        assertEquals(100, series.getCurrentNumber());
        verify(billingSeriesRepository).update(series);
    }

    @Test
    void testResetCounter_WithNumberBelowRange_ThrowsException() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 500, 100, 1000, true);
        int invalidNumber = 50;
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sequentialNumberService.resetCounter(seriesId, invalidNumber);
        });
        
        assertTrue(exception.getMessage().contains("fuera del rango permitido"));
        verify(billingSeriesRepository, never()).update(any());
    }

    @Test
    void testResetCounter_WithNumberAboveRange_ThrowsException() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 500, 100, 1000, true);
        int invalidNumber = 1500;
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sequentialNumberService.resetCounter(seriesId, invalidNumber);
        });
        
        assertTrue(exception.getMessage().contains("fuera del rango permitido"));
        verify(billingSeriesRepository, never()).update(any());
    }

    @Test
    void testResetCounter_WhenSeriesNotExists_ThrowsException() {
        Long seriesId = 999L;
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sequentialNumberService.resetCounter(seriesId, 100);
        });
        
        assertTrue(exception.getMessage().contains("Serie de facturación no encontrada"));
        verify(billingSeriesRepository, never()).update(any());
    }

    @Test
    void testGetSeriesStatus_WhenSeriesExists_ReturnsStatus() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 123, 1, 1000, true);
        series.setDocumentType(BillingSeries.DocumentType.INVOICE);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        String result = sequentialNumberService.getSeriesStatus(seriesId);
        
        assertTrue(result.contains("Serie: A"));
        assertTrue(result.contains("Actual: 123"));
        assertTrue(result.contains("Rango: [1, 1000]"));
        assertTrue(result.contains("Activa: true"));
    }

    @Test
    void testGetSeriesStatus_WhenSeriesNotExists_ReturnsNotFound() {
        Long seriesId = 999L;
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.empty());
        
        String result = sequentialNumberService.getSeriesStatus(seriesId);
        
        assertEquals("Serie no encontrada", result);
    }

    @Test
    void testGetSeriesStatus_WithInactiveSeries_ReturnsStatus() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "B", 50, 1, 500, false);
        series.setDocumentType(BillingSeries.DocumentType.CREDIT_NOTE);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        
        String result = sequentialNumberService.getSeriesStatus(seriesId);
        
        assertTrue(result.contains("Serie: B"));
        assertTrue(result.contains("Tipo: Nota de Crédito"));
        assertTrue(result.contains("Activa: false"));
    }

    @Test
    void testGetNextSequentialNumber_IncrementsCorrectly() {
        Long seriesId = 1L;
        BillingSeries series = createBillingSeries(seriesId, "A", 100, 1, 1000, true);
        
        when(billingSeriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        doNothing().when(billingSeriesRepository).update(any(BillingSeries.class));
        
        int first = sequentialNumberService.getNextSequentialNumber(seriesId);
        int second = sequentialNumberService.getNextSequentialNumber(seriesId);
        int third = sequentialNumberService.getNextSequentialNumber(seriesId);
        
        assertEquals(100, first);
        assertEquals(101, second);
        assertEquals(102, third);
        assertEquals(103, series.getCurrentNumber());
        verify(billingSeriesRepository, times(3)).update(series);
    }

    private BillingSeries createBillingSeries(Long id, String seriesCode, int currentNumber, 
                                               int startNumber, int endNumber, boolean active) {
        BillingSeries series = new BillingSeries();
        series.setId(id);
        series.setSeriesCode(seriesCode);
        series.setCurrentNumber(currentNumber);
        series.setStartNumber(startNumber);
        series.setEndNumber(endNumber);
        series.setActive(active);
        series.setDescription("Test Series");
        series.setDocumentType(BillingSeries.DocumentType.INVOICE);
        return series;
    }
}
