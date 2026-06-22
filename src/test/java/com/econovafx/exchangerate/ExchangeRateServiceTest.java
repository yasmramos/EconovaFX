package com.econovafx.exchangerate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.econovafx.service.BCCExchangeRateClient;
import com.econovafx.repository.ExchangeRateRepository;
import com.econovafx.service.ExchangeRateService;
import com.econovafx.repository.CurrencyRepository;
import com.econovafx.service.BCCExchangeRateFetcher;

/**
 * Unit tests for ExchangeRateService.
 * Tests caching logic, persistence integration, and error handling.
 */
class ExchangeRateServiceTest {

    @Mock
    private BCCExchangeRateClient mockClient;

    @Mock
    private ExchangeRateRepository mockRepository;

    @Mock
    private CurrencyRepository mockCurrencyRepository;

    @Mock
    private BCCExchangeRateFetcher mockBccFetcher;

    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ExchangeRateService(mockRepository, mockCurrencyRepository, mockBccFetcher);
    }

    @Test
    void testFetchAndSaveRatesFromBCC_Success() {
        // Given: BCC fetcher returns valid rates
        List<BCCExchangeRateFetcher.BCCRate> mockRates = List.of(
            new BCCExchangeRateFetcher.BCCRate("USD", "Dólar Estadounidense", java.math.BigDecimal.valueOf(120.0), "$", LocalDate.now(), "BCC")
        );
        when(mockBccFetcher.fetchCurrentRates()).thenReturn(mockRates);
        
        // Mock currency repository to return existing CUP currency
        when(mockCurrencyRepository.findByCode("CUP")).thenReturn(Optional.empty());
        when(mockCurrencyRepository.save(any())).thenAnswer(invocation -> {
            com.econovafx.model.Currency c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });
        
        when(mockCurrencyRepository.findByCode("USD")).thenReturn(Optional.empty());
        
        // Mock exchange rate repository save
        when(mockRepository.save(any())).thenAnswer(invocation -> {
            com.econovafx.model.ExchangeRate er = invocation.getArgument(0);
            er.setId(1L);
            return er;
        });

        // When: fetchAndSaveRatesFromBCC is called
        List<com.econovafx.model.ExchangeRate> result = service.fetchAndSaveRatesFromBCC();

        // Then: Should save rates to repository
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockRepository, times(1)).save(any());
    }

    @Test
    void testFetchAndSaveRatesFromBCC_EmptyResponse() {
        // Given: BCC fetcher returns empty list
        when(mockBccFetcher.fetchCurrentRates()).thenReturn(List.of());

        // When: fetchAndSaveRatesFromBCC is called
        List<com.econovafx.model.ExchangeRate> result = service.fetchAndSaveRatesFromBCC();

        // Then: Should return empty list
        assertTrue(result.isEmpty());
        verify(mockRepository, never()).save(any());
    }

    @Test
    void testGetAllActiveRates_DelegatesToRepository() {
        // Given: Repository has active rates
        List<com.econovafx.model.ExchangeRate> mockRates = List.of(
            new com.econovafx.model.ExchangeRate(null, null, java.math.BigDecimal.ONE, java.time.LocalDateTime.now())
        );
        when(mockRepository.findAllActive()).thenReturn(mockRates);

        // When: getAllActiveRates is called
        List<com.econovafx.model.ExchangeRate> result = service.getAllActiveRates();

        // Then: Should return rates from repository
        assertEquals(1, result.size());
        verify(mockRepository, times(1)).findAllActive();
    }

    @Test
    void testDeactivateExchangeRate_DelegatesToRepository() {
        // Given: Exchange rate ID
        Long rateId = 1L;

        // When: deactivateExchangeRate is called
        service.deactivateExchangeRate(rateId);

        // Then: Should call repository deactivate
        verify(mockRepository, times(1)).deactivate(rateId);
    }

    @Test
    void testGetExchangeRateById_DelegatesToRepository() {
        // Given: Exchange rate ID
        Long rateId = 1L;
        com.econovafx.model.ExchangeRate mockRate = new com.econovafx.model.ExchangeRate(
            null, null, java.math.BigDecimal.ONE, java.time.LocalDateTime.now()
        );
        mockRate.setId(rateId);
        when(mockRepository.findById(rateId)).thenReturn(Optional.of(mockRate));

        // When: getExchangeRateById is called
        Optional<com.econovafx.model.ExchangeRate> result = service.getExchangeRateById(rateId);

        // Then: Should return rate from repository
        assertTrue(result.isPresent());
        assertEquals(rateId, result.get().getId());
        verify(mockRepository, times(1)).findById(rateId);
    }

    @Test
    void testFetchAndSaveLatestRates_AliasWorks() {
        // Given: BCC fetcher returns valid rates
        when(mockBccFetcher.fetchCurrentRates()).thenReturn(List.of());

        // When: fetchAndSaveLatestRates is called (alias method)
        service.fetchAndSaveLatestRates();

        // Then: Should delegate to fetchAndSaveRatesFromBCC
        verify(mockBccFetcher, times(1)).fetchCurrentRates();
    }

    @Test
    void testGetLatestRatesForAllCurrencies_DelegatesToGetAllActive() {
        // Given: Active rates exist
        List<com.econovafx.model.ExchangeRate> mockRates = List.of(
            new com.econovafx.model.ExchangeRate(null, null, java.math.BigDecimal.ONE, java.time.LocalDateTime.now())
        );
        when(mockRepository.findAllActive()).thenReturn(mockRates);

        // When: getLatestRatesForAllCurrencies is called
        List<com.econovafx.model.ExchangeRate> result = service.getLatestRatesForAllCurrencies();

        // Then: Should return all active rates
        assertEquals(1, result.size());
        verify(mockRepository, times(1)).findAllActive();
    }
}
