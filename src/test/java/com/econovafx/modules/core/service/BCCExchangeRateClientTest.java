package com.econovafx.modules.core.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BCCExchangeRateClient.
 * These tests exercise DTO behavior, client configuration and the retry
 * mechanism. Network-dependent methods are pointed at a local address with no
 * server listening, so they are expected to fail with a connection error after
 * exhausting retries.
 */
class BCCExchangeRateClientTest {

    private BCCExchangeRateClient client;

    @BeforeEach
    void setUp() {
        // Create client with test configuration
        System.setProperty("bcc.api.base.url", "http://localhost:8080/v1/tasas-de-cambio");
        System.setProperty("bcc.api.timeout.seconds", "5");
        System.setProperty("bcc.api.retry.max.attempts", "2");
        System.setProperty("bcc.api.retry.delay.ms", "100");
        client = new BCCExchangeRateClient();
    }

    @AfterEach
    void tearDown() {
        // Restore global settings so other tests are not affected
        System.clearProperty("bcc.api.base.url");
        System.clearProperty("bcc.api.timeout.seconds");
        System.clearProperty("bcc.api.retry.max.attempts");
        System.clearProperty("bcc.api.retry.delay.ms");
    }

    @Test
    void testBCCRateDTO_RecordCreation() {
        // Given: Valid BCC rate data
        String codigoMoneda = "USD";
        String nombreMoneda = "Dólar Estadounidense";
        BigDecimal tasa = BigDecimal.valueOf(120.0);
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        String fuente = "Banco Central de Cuba";

        // When: Creating BCCRateDTO record
        BCCExchangeRateClient.BCCRateDTO rate = new BCCExchangeRateClient.BCCRateDTO(
                codigoMoneda, nombreMoneda, tasa, fecha, fuente
        );

        // Then: All getters should return correct values
        assertEquals(codigoMoneda, rate.getCodigoMoneda());
        assertEquals(nombreMoneda, rate.getNombreMoneda());
        assertEquals(tasa, rate.getTasa());
        assertEquals(fecha, rate.getFecha());
        assertEquals(fuente, rate.getFuente());
    }

    @Test
    void testBCCRateDTO_NullValues() {
        // Given: BCCRateDTO with null values
        BCCExchangeRateClient.BCCRateDTO rate = new BCCExchangeRateClient.BCCRateDTO(
                null, null, null, null, null
        );

        // Then: Getters should handle nulls gracefully
        assertNull(rate.getCodigoMoneda());
        assertNull(rate.getNombreMoneda());
        assertNull(rate.getTasa());
        assertNull(rate.getFecha());
        assertNull(rate.getFuente());
    }

    @Test
    void testFetchActiveRates_ClientInstantiation() {
        // Given: Client is instantiated
        assertNotNull(client);

        // When/Then: Should not throw exception on instantiation
        // Note: Actual HTTP calls require WireMock or similar for proper testing
        assertDoesNotThrow(() -> new BCCExchangeRateClient());
    }

    @Test
    void testFetchRatesByDate_WithNullDate() {
        // Given: Client with default configuration
        // When: Calling fetchRatesByDate with null (should use current date)
        // Then: The HTTP call fails without a mock server and surfaces a connection error
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> client.fetchRatesByDate(null));
        assertTrue(ex.getMessage().contains("No se pudo conectar"));
    }

    @Test
    void testFetchHistoricalRates_ParameterBuilding() {
        // Given: Date range and currency code
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        String currencyCode = "USD";

        // When/Then: The HTTP call fails without a mock server and surfaces a connection error
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> client.fetchHistoricalRates(start, end, currencyCode));
        assertTrue(ex.getMessage().contains("No se pudo conectar"));
    }

    @Test
    void testRetryMechanism_Configuration() {
        // Given: Client with retry configuration (3 attempts => 2 delays of 300ms)
        System.setProperty("bcc.api.retry.max.attempts", "3");
        System.setProperty("bcc.api.retry.delay.ms", "300");
        BCCExchangeRateClient retryClient = new BCCExchangeRateClient();

        // Then: Client should be created with retry settings
        assertNotNull(retryClient);

        // When: Making a call that will fail and trigger retries
        // Then: Should attempt multiple times before failing, waiting between attempts
        long startTime = System.currentTimeMillis();
        RuntimeException ex = assertThrows(RuntimeException.class, retryClient::fetchActiveRates);
        long elapsed = System.currentTimeMillis() - startTime;

        assertTrue(ex.getMessage().contains("No se pudo conectar"));
        // Two 300ms delays are expected; allow slack for scheduling jitter
        assertTrue(elapsed >= 500,
                "Retry mechanism should have waited between attempts, elapsed=" + elapsed + "ms");
    }

    @Test
    void testTimeoutHandling_Configuration() {
        // Given: Client with short timeout
        System.setProperty("bcc.api.timeout.seconds", "1");
        BCCExchangeRateClient timeoutClient = new BCCExchangeRateClient();

        // Then: Client should be created with timeout setting
        assertNotNull(timeoutClient);
    }
}
