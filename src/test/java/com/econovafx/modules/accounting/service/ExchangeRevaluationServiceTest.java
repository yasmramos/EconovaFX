package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.ExchangeDifference;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository;
import com.econovafx.modules.core.model.Currency;
import com.econovafx.modules.core.model.ExchangeRate;
import com.econovafx.modules.core.model.SystemConfiguration;
import com.econovafx.modules.core.repository.ExchangeRateRepository;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.service.SystemConfigService;
import com.econovafx.modules.payables.model.SupplierInvoice;
import com.econovafx.modules.payables.repository.SupplierInvoiceRepository;
import com.econovafx.modules.receivables.model.CustomerInvoice;
import com.econovafx.modules.receivables.repository.CustomerInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the revaluation result set: the service must return the difference
 * records it actually created, not a lookup by a fixed document id that drops
 * every loss and reports another document's gains.
 */
@ExtendWith(MockitoExtension.class)
class ExchangeRevaluationServiceTest {

    private static final LocalDate CUTOFF = LocalDate.of(2024, 12, 31);

    @Mock private ExchangeDifferenceRepository exchangeDifferenceRepository;
    @Mock private ExchangeRateRepository exchangeRateRepository;
    @Mock private TransactionService transactionService;
    @Mock private AccountRepository accountRepository;
    @Mock private AuditService auditService;
    @Mock private SystemConfigService systemConfigService;
    @Mock private SupplierInvoiceRepository supplierInvoiceRepository;
    @Mock private CustomerInvoiceRepository customerInvoiceRepository;

    private ExchangeRevaluationService service;

    @BeforeEach
    void setUp() {
        service = new ExchangeRevaluationService(
                exchangeDifferenceRepository,
                exchangeRateRepository,
                transactionService,
                accountRepository,
                auditService,
                systemConfigService,
                supplierInvoiceRepository,
                customerInvoiceRepository);

        SystemConfiguration config = new SystemConfiguration();
        config.setUnrealizedExchangeGainAccountCode("7700");
        config.setUnrealizedExchangeLossAccountCode("6700");
        config.setExchangeReceivableAccountCode("1200");
        config.setExchangePayableAccountCode("2200");
        lenient().when(systemConfigService.getCurrentConfig()).thenReturn(config);

        Account gain = new Account("7700", "Unrealized FX gain", AccountType.REVENUE);
        gain.setId(700L);
        Account loss = new Account("6700", "Unrealized FX loss", AccountType.EXPENSE);
        loss.setId(600L);
        Account receivable = new Account("1200", "FX receivable", AccountType.ASSET);
        receivable.setId(200L);
        Account payable = new Account("2200", "FX payable", AccountType.LIABILITY);
        payable.setId(300L);
        lenient().when(accountRepository.findByCode("7700")).thenReturn(Optional.of(gain));
        lenient().when(accountRepository.findByCode("6700")).thenReturn(Optional.of(loss));
        lenient().when(accountRepository.findByCode("1200")).thenReturn(Optional.of(receivable));
        lenient().when(accountRepository.findByCode("2200")).thenReturn(Optional.of(payable));

        // Persist returns the same instance with an id, as Ebean would after save.
        lenient().when(exchangeDifferenceRepository.save(any(ExchangeDifference.class)))
                .thenAnswer(inv -> {
                    ExchangeDifference diff = inv.getArgument(0);
                    if (diff.getId() == null) {
                        diff.setId(1L);
                    }
                    return diff;
                });
        lenient().when(transactionService.createTransaction(any(), any(), any()))
                .thenAnswer(inv -> {
                    com.econovafx.modules.accounting.model.Transaction txn = inv.getArgument(0);
                    txn.setId(900L);
                    return txn;
                });
    }

    private SupplierInvoice supplierInvoice(String number, BigDecimal pending, Currency currency) {
        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setId(11L);
        invoice.setInvoiceNumber(number);
        invoice.setInvoiceDate(LocalDate.of(2024, 6, 1));
        invoice.setPendingAmount(pending);
        invoice.setCurrency(currency);
        return invoice;
    }

    private CustomerInvoice customerInvoice(String number, BigDecimal pending, Currency currency) {
        CustomerInvoice invoice = new CustomerInvoice();
        invoice.setId(22L);
        invoice.setInvoiceNumber(number);
        invoice.setInvoiceDate(LocalDate.of(2024, 6, 1));
        invoice.setPendingAmount(pending);
        invoice.setCurrency(currency);
        return invoice;
    }

    private Currency usd() {
        return new Currency("USD", "US Dollar", "$");
    }

    private Currency eur() {
        return new Currency("EUR", "Euro", "EUR");
    }

    private ExchangeRate rate(String value) {
        ExchangeRate rate = new ExchangeRate();
        rate.setRate(new BigDecimal(value));
        return rate;
    }

    @Test
    void returnsEveryCreatedRecordIncludingLosses() {
        Currency usd = usd();
        Currency eur = eur();
        LocalDate invoiceDate = LocalDate.of(2024, 6, 1);

        when(supplierInvoiceRepository.findOpenForeignCurrencyInvoices())
                .thenReturn(List.of(supplierInvoice("F-1", new BigDecimal("100.00"), usd)));
        when(customerInvoiceRepository.findOpenForeignCurrencyInvoices())
                .thenReturn(List.of(customerInvoice("F-2", new BigDecimal("100.00"), eur)));

        // Supplier (USD): 100 * (1.20 - 1.00) = +20 -> unrealized gain
        when(exchangeRateRepository.findByCurrencyAndDate(usd, CUTOFF))
                .thenReturn(Optional.of(rate("1.20")));
        when(exchangeRateRepository.findByCurrencyAndDate(usd, invoiceDate))
                .thenReturn(Optional.of(rate("1.00")));

        // Customer (EUR): 100 * (0.90 - 1.00) = -10 -> unrealized loss
        when(exchangeRateRepository.findByCurrencyAndDate(eur, CUTOFF))
                .thenReturn(Optional.of(rate("0.90")));
        when(exchangeRateRepository.findByCurrencyAndDate(eur, invoiceDate))
                .thenReturn(Optional.of(rate("1.00")));

        List<ExchangeDifference> result = service.revalueOpenBalances(CUTOFF, "tester");

        assertEquals(2, result.size(), "both the gain and the loss record must be returned");
        assertTrue(result.stream().anyMatch(d -> d.getDifferenceType() == ExchangeDifference.DifferenceType.UNREALIZED_GAIN));
        assertTrue(result.stream().anyMatch(d -> d.getDifferenceType() == ExchangeDifference.DifferenceType.UNREALIZED_LOSS));
        verify(exchangeDifferenceRepository, never())
                .findByDocumentAndType(any(), any(), any());
    }

    @Test
    void skipsBaseCurrencyInvoicesAndReturnsEmpty() {
        Currency cup = new Currency("CUP", "Cuban Peso", "$");
        when(supplierInvoiceRepository.findOpenForeignCurrencyInvoices())
                .thenReturn(List.of(supplierInvoice("F-CUP", new BigDecimal("500.00"), cup)));
        when(customerInvoiceRepository.findOpenForeignCurrencyInvoices())
                .thenReturn(List.of());

        List<ExchangeDifference> result = service.revalueOpenBalances(CUTOFF, "tester");

        assertTrue(result.isEmpty());
        verify(exchangeDifferenceRepository, never()).save(any());
    }
}
