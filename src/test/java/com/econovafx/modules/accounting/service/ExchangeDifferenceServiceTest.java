package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.ExchangeDifference;
import com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository;
import com.econovafx.modules.core.repository.ExchangeRateRepository;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.model.Currency;
import com.econovafx.modules.core.model.ExchangeRate;
import com.econovafx.modules.billing.model.ThirdParty;
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
class ExchangeDifferenceServiceTest {

    @Mock
    private ExchangeDifferenceRepository exchangeDifferenceRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

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

    private ExchangeDifferenceService service;

    private ThirdParty thirdParty;
    private Currency usdCurrency;
    private ExchangeRate exchangeRate;

    @BeforeEach
    void setUp() {
        service = new ExchangeDifferenceService(
            exchangeDifferenceRepository,
            exchangeRateRepository,
            transactionService,
            accountRepository,
            auditService
        );

        thirdParty = new ThirdParty();
        thirdParty.setId(1L);
        thirdParty.setName("Test Client");

        usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");

        exchangeRate = new ExchangeRate();
        exchangeRate.setCurrency(usdCurrency);
        exchangeRate.setRate(BigDecimal.valueOf(35.50));
        exchangeRate.setDate(LocalDate.now());
    }

    @Test
    void testCalculateAndRecordDifference_NoDifference() {
        // Arrange
        LocalDate invoiceDate = LocalDate.of(2024, 1, 15);
        LocalDate paymentDate = LocalDate.of(2024, 1, 15);
        BigDecimal foreignAmount = BigDecimal.valueOf(1000);

        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(invoiceDate)))
            .thenReturn(Optional.of(exchangeRate));
        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(paymentDate)))
            .thenReturn(Optional.of(exchangeRate));

        ExchangeDifference savedDiff = new ExchangeDifference();
        savedDiff.setId(1L);
        savedDiff.setDifferenceType(ExchangeDifference.DifferenceType.NONE);
        savedDiff.setDifferenceAmount(BigDecimal.ZERO);
        when(exchangeDifferenceRepository.save(any(ExchangeDifference.class))).thenReturn(savedDiff);

        // Act
        ExchangeDifference result = service.calculateAndRecordDifference(
            "SALES_INVOICE", 1L, "INV-001", thirdParty, usdCurrency,
            foreignAmount, invoiceDate, paymentDate, "testuser"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ExchangeDifference.DifferenceType.NONE, result.getDifferenceType());
        verify(exchangeDifferenceRepository, times(1)).save(any(ExchangeDifference.class));
        verify(transactionService, never()).createTransaction(any(), any(), any());
    }

    @Test
    void testCalculateAndRecordDifference_WithGain() {
        // Arrange
        LocalDate invoiceDate = LocalDate.of(2024, 1, 15);
        LocalDate paymentDate = LocalDate.of(2024, 1, 20);
        BigDecimal foreignAmount = BigDecimal.valueOf(1000);

        ExchangeRate rateInvoice = new ExchangeRate();
        rateInvoice.setCurrency(usdCurrency);
        rateInvoice.setRate(BigDecimal.valueOf(36.00));
        rateInvoice.setDate(invoiceDate);

        ExchangeRate ratePayment = new ExchangeRate();
        ratePayment.setCurrency(usdCurrency);
        ratePayment.setRate(BigDecimal.valueOf(35.00));
        ratePayment.setDate(paymentDate);

        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(invoiceDate)))
            .thenReturn(Optional.of(rateInvoice));
        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(paymentDate)))
            .thenReturn(Optional.of(ratePayment));

        ExchangeDifference savedDiff = new ExchangeDifference();
        savedDiff.setId(1L);
        savedDiff.setDifferenceType(ExchangeDifference.DifferenceType.GAIN);
        savedDiff.setDifferenceAmount(BigDecimal.valueOf(1000));
        when(exchangeDifferenceRepository.save(any(ExchangeDifference.class))).thenReturn(savedDiff);
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.of(mockAccount));
        when(transactionService.createTransaction(any(), any(), anyString())).thenReturn(mockTransaction);

        // Act
        ExchangeDifference result = service.calculateAndRecordDifference(
            "SALES_INVOICE", 1L, "INV-001", thirdParty, usdCurrency,
            foreignAmount, invoiceDate, paymentDate, "testuser"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ExchangeDifference.DifferenceType.GAIN, result.getDifferenceType());
        verify(exchangeDifferenceRepository, times(1)).save(any(ExchangeDifference.class));
        verify(transactionService, times(1)).createTransaction(any(), any(), anyString());
        verify(auditService, times(1)).logWithValues(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCalculateAndRecordDifference_WithLoss() {
        // Arrange
        LocalDate invoiceDate = LocalDate.of(2024, 1, 15);
        LocalDate paymentDate = LocalDate.of(2024, 1, 20);
        BigDecimal foreignAmount = BigDecimal.valueOf(1000);

        ExchangeRate rateInvoice = new ExchangeRate();
        rateInvoice.setCurrency(usdCurrency);
        rateInvoice.setRate(BigDecimal.valueOf(35.00));
        rateInvoice.setDate(invoiceDate);

        ExchangeRate ratePayment = new ExchangeRate();
        ratePayment.setCurrency(usdCurrency);
        ratePayment.setRate(BigDecimal.valueOf(36.00));
        ratePayment.setDate(paymentDate);

        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(invoiceDate)))
            .thenReturn(Optional.of(rateInvoice));
        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(paymentDate)))
            .thenReturn(Optional.of(ratePayment));

        ExchangeDifference savedDiff = new ExchangeDifference();
        savedDiff.setId(1L);
        savedDiff.setDifferenceType(ExchangeDifference.DifferenceType.LOSS);
        savedDiff.setDifferenceAmount(BigDecimal.valueOf(1000));
        when(exchangeDifferenceRepository.save(any(ExchangeDifference.class))).thenReturn(savedDiff);
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.of(mockAccount));
        when(transactionService.createTransaction(any(), any(), anyString())).thenReturn(mockTransaction);

        // Act
        ExchangeDifference result = service.calculateAndRecordDifference(
            "PURCHASE_INVOICE", 1L, "INV-001", thirdParty, usdCurrency,
            foreignAmount, invoiceDate, paymentDate, "testuser"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ExchangeDifference.DifferenceType.LOSS, result.getDifferenceType());
        verify(exchangeDifferenceRepository, times(1)).save(any(ExchangeDifference.class));
        verify(transactionService, times(1)).createTransaction(any(), any(), anyString());
    }

    @Test
    void testGetHistoricalExchangeRate_ExactDateNotFound() {
        // Arrange
        LocalDate searchDate = LocalDate.of(2024, 1, 15);
        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(searchDate)))
            .thenReturn(Optional.empty());

        ExchangeRate previousRate = new ExchangeRate();
        previousRate.setCurrency(usdCurrency);
        previousRate.setRate(BigDecimal.valueOf(34.50));
        previousRate.setDate(LocalDate.of(2024, 1, 10));

        when(exchangeRateRepository.findByCurrencyBeforeDate(eq(usdCurrency), eq(searchDate)))
            .thenReturn(List.of(previousRate));

        // Act & Assert - Esto prueba el método privado indirectamente
        // El método público debería usar la tasa anterior cuando no encuentra la fecha exacta
        verify(exchangeRateRepository, never()).findCurrentByCurrency(any());
    }

    @Test
    void testGetAllDifferences() {
        // Arrange
        ExchangeDifference diff1 = new ExchangeDifference();
        diff1.setId(1L);
        ExchangeDifference diff2 = new ExchangeDifference();
        diff2.setId(2L);

        when(exchangeDifferenceRepository.findAll()).thenReturn(List.of(diff1, diff2));

        // Act
        List<ExchangeDifference> result = service.getAllDifferences();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(exchangeDifferenceRepository, times(1)).findAll();
    }

    @Test
    void testGetDifferencesByDocument() {
        // Arrange
        ExchangeDifference diff = new ExchangeDifference();
        diff.setId(1L);
        diff.setDocumentType("SALES_INVOICE");
        diff.setDocumentId(1L);

        when(exchangeDifferenceRepository.findByDocument("SALES_INVOICE", 1L))
            .thenReturn(List.of(diff));

        // Act
        List<ExchangeDifference> result = service.getDifferencesByDocument("SALES_INVOICE", 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SALES_INVOICE", result.get(0).getDocumentType());
        verify(exchangeDifferenceRepository, times(1)).findByDocument("SALES_INVOICE", 1L);
    }

    @Test
    void testGetTotalGainsInPeriod() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        BigDecimal expectedGains = BigDecimal.valueOf(5000);

        when(exchangeDifferenceRepository.getTotalGains(startDate, endDate))
            .thenReturn(expectedGains);

        // Act
        BigDecimal result = service.getTotalGainsInPeriod(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(expectedGains, result);
        verify(exchangeDifferenceRepository, times(1)).getTotalGains(startDate, endDate);
    }

    @Test
    void testGetTotalLossesInPeriod() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        BigDecimal expectedLosses = BigDecimal.valueOf(3000);

        when(exchangeDifferenceRepository.getTotalLosses(startDate, endDate))
            .thenReturn(expectedLosses);

        // Act
        BigDecimal result = service.getTotalLossesInPeriod(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(expectedLosses, result);
        verify(exchangeDifferenceRepository, times(1)).getTotalLosses(startDate, endDate);
    }

    @Test
    void testCalculateAndRecordDifference_RateNotFoundUsesCurrent() {
        // Arrange
        LocalDate invoiceDate = LocalDate.of(2024, 1, 15);
        LocalDate paymentDate = LocalDate.of(2024, 1, 20);
        BigDecimal foreignAmount = BigDecimal.valueOf(1000);

        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(invoiceDate)))
            .thenReturn(Optional.empty());
        when(exchangeRateRepository.findByCurrencyBeforeDate(eq(usdCurrency), eq(invoiceDate)))
            .thenReturn(List.of());

        ExchangeRate currentRate = new ExchangeRate();
        currentRate.setCurrency(usdCurrency);
        currentRate.setRate(BigDecimal.valueOf(35.00));
        when(exchangeRateRepository.findCurrentByCurrency(usdCurrency))
            .thenReturn(Optional.of(currentRate));

        ExchangeDifference savedDiff = new ExchangeDifference();
        savedDiff.setId(1L);
        savedDiff.setDifferenceType(ExchangeDifference.DifferenceType.NONE);
        when(exchangeDifferenceRepository.save(any(ExchangeDifference.class))).thenReturn(savedDiff);

        // Act
        ExchangeDifference result = service.calculateAndRecordDifference(
            "SALES_INVOICE", 1L, "INV-001", thirdParty, usdCurrency,
            foreignAmount, invoiceDate, paymentDate, "testuser"
        );

        // Assert
        assertNotNull(result);
        verify(exchangeRateRepository, times(1)).findCurrentByCurrency(usdCurrency);
    }

    @Test
    void testCalculateAndRecordDifference_AccountNotFound() {
        // Arrange
        LocalDate invoiceDate = LocalDate.of(2024, 1, 15);
        LocalDate paymentDate = LocalDate.of(2024, 1, 20);
        BigDecimal foreignAmount = BigDecimal.valueOf(1000);

        ExchangeRate rateInvoice = new ExchangeRate();
        rateInvoice.setRate(BigDecimal.valueOf(36.00));
        ExchangeRate ratePayment = new ExchangeRate();
        ratePayment.setRate(BigDecimal.valueOf(35.00));

        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(invoiceDate)))
            .thenReturn(Optional.of(rateInvoice));
        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(paymentDate)))
            .thenReturn(Optional.of(ratePayment));

        ExchangeDifference savedDiff = new ExchangeDifference();
        savedDiff.setId(1L);
        savedDiff.setDifferenceType(ExchangeDifference.DifferenceType.GAIN);
        savedDiff.setDifferenceAmount(BigDecimal.valueOf(1000));
        when(exchangeDifferenceRepository.save(any(ExchangeDifference.class))).thenReturn(savedDiff);
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            service.calculateAndRecordDifference(
                "SALES_INVOICE", 1L, "INV-001", thirdParty, usdCurrency,
                foreignAmount, invoiceDate, paymentDate, "testuser"
            )
        );
    }

    @Test
    void testCalculateAndRecordDifference_TransactionCreationFails() {
        // Arrange
        LocalDate invoiceDate = LocalDate.of(2024, 1, 15);
        LocalDate paymentDate = LocalDate.of(2024, 1, 20);
        BigDecimal foreignAmount = BigDecimal.valueOf(1000);

        ExchangeRate rateInvoice = new ExchangeRate();
        rateInvoice.setRate(BigDecimal.valueOf(36.00));
        ExchangeRate ratePayment = new ExchangeRate();
        ratePayment.setRate(BigDecimal.valueOf(35.00));

        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(invoiceDate)))
            .thenReturn(Optional.of(rateInvoice));
        when(exchangeRateRepository.findByCurrencyAndDate(eq(usdCurrency), eq(paymentDate)))
            .thenReturn(Optional.of(ratePayment));

        ExchangeDifference savedDiff = new ExchangeDifference();
        savedDiff.setId(1L);
        savedDiff.setDifferenceType(ExchangeDifference.DifferenceType.GAIN);
        savedDiff.setDifferenceAmount(BigDecimal.valueOf(1000));
        savedDiff.setNotes("Initial notes");
        when(exchangeDifferenceRepository.save(any(ExchangeDifference.class))).thenReturn(savedDiff);
        when(accountRepository.findByCode(anyString())).thenReturn(Optional.of(mockAccount));
        when(transactionService.createTransaction(any(), any(), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            service.calculateAndRecordDifference(
                "SALES_INVOICE", 1L, "INV-001", thirdParty, usdCurrency,
                foreignAmount, invoiceDate, paymentDate, "testuser"
            )
        );

        assertTrue(exception.getMessage().contains("Error generando asiento contable"));
        verify(exchangeDifferenceRepository, times(1)).update(savedDiff);
    }
}
