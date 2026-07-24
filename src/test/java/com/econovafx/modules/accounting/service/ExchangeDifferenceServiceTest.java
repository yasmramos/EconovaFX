package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.ExchangeDifference;
import com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository;
import com.econovafx.modules.core.repository.ExchangeRateRepository;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.Currency;
import com.econovafx.modules.core.model.ExchangeRate;
import com.econovafx.modules.billing.model.ThirdParty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExchangeDifferenceService with manual stub mocks
 */
class ExchangeDifferenceServiceTest {

    private StubExchangeDifferenceRepository exchangeDifferenceRepository;
    private StubExchangeRateRepository exchangeRateRepository;
    private StubTransactionService transactionService;
    private StubAccountRepository accountRepository;
    private StubAuditService auditService;
    private ExchangeDifferenceService exchangeDifferenceService;

    @BeforeEach
    void setUp() {
        exchangeDifferenceRepository = new StubExchangeDifferenceRepository();
        exchangeRateRepository = new StubExchangeRateRepository();
        transactionService = new StubTransactionService();
        accountRepository = new StubAccountRepository();
        auditService = new StubAuditService();
        
        exchangeDifferenceService = new ExchangeDifferenceService(
            exchangeDifferenceRepository,
            exchangeRateRepository,
            transactionService,
            accountRepository,
            auditService
        );
    }

    @Test
    void testCalculateAndRecordDifference_WithGainScenario() {
        Currency usd = new Currency();
        usd.setCode("USD");
        
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        
        LocalDate invoiceDate = LocalDate.of(2024, 1, 15);
        LocalDate paymentDate = LocalDate.of(2024, 1, 30);
        BigDecimal foreignAmount = new BigDecimal("1000.00");
        
        exchangeRateRepository.addRate(createExchangeRate(usd, invoiceDate, new BigDecimal("30.0")));
        exchangeRateRepository.addRate(createExchangeRate(usd, paymentDate, new BigDecimal("29.0")));
        
        ExchangeDifference result = exchangeDifferenceService.calculateAndRecordDifference(
            "SALES_INVOICE", 1L, "FAC-001", customer, usd,
            foreignAmount, invoiceDate, paymentDate, "testuser"
        );
        
        assertNotNull(result);
        assertEquals(ExchangeDifference.DifferenceType.GAIN, result.getDifferenceType());
    }

    @Test
    void testCalculateAndRecordDifference_WithLossScenario() {
        Currency usd = new Currency();
        usd.setCode("USD");
        
        ThirdParty supplier = new ThirdParty();
        supplier.setId(2L);
        
        LocalDate invoiceDate = LocalDate.of(2024, 2, 1);
        LocalDate paymentDate = LocalDate.of(2024, 2, 15);
        BigDecimal foreignAmount = new BigDecimal("500.00");
        
        exchangeRateRepository.addRate(createExchangeRate(usd, invoiceDate, new BigDecimal("30.0")));
        exchangeRateRepository.addRate(createExchangeRate(usd, paymentDate, new BigDecimal("31.0")));
        
        ExchangeDifference result = exchangeDifferenceService.calculateAndRecordDifference(
            "PURCHASE_INVOICE", 2L, "FAC-002", supplier, usd,
            foreignAmount, invoiceDate, paymentDate, "testuser"
        );
        
        assertNotNull(result);
        assertEquals(ExchangeDifference.DifferenceType.LOSS, result.getDifferenceType());
    }

    @Test
    void testCalculateAndRecordDifference_WithNoDifference() {
        Currency usd = new Currency();
        usd.setCode("USD");
        
        ThirdParty customer = new ThirdParty();
        customer.setId(3L);
        
        LocalDate invoiceDate = LocalDate.of(2024, 3, 1);
        LocalDate paymentDate = LocalDate.of(2024, 3, 10);
        BigDecimal sameRate = new BigDecimal("28.5");
        
        exchangeRateRepository.addRate(createExchangeRate(usd, invoiceDate, sameRate));
        exchangeRateRepository.addRate(createExchangeRate(usd, paymentDate, sameRate));
        
        ExchangeDifference result = exchangeDifferenceService.calculateAndRecordDifference(
            "SALES_INVOICE", 3L, "FAC-003", customer, usd,
            new BigDecimal("200.00"), invoiceDate, paymentDate, "testuser"
        );
        
        assertNotNull(result);
        assertEquals(ExchangeDifference.DifferenceType.NONE, result.getDifferenceType());
    }

    @Test
    void testGetAllDifferences_ReturnsAllRecords() {
        Currency usd = new Currency();
        usd.setCode("USD");
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        
        LocalDate date = LocalDate.of(2024, 1, 15);
        exchangeRateRepository.addRate(createExchangeRate(usd, date, new BigDecimal("30.0")));
        
        exchangeDifferenceService.calculateAndRecordDifference(
            "SALES_INVOICE", 1L, "FAC-001", customer, usd,
            new BigDecimal("100.00"), date, date, "user1"
        );
        
        List<ExchangeDifference> allDifferences = exchangeDifferenceService.getAllDifferences();
        
        assertNotNull(allDifferences);
        assertTrue(allDifferences.size() >= 1);
    }

    @Test
    void testGetCount_ReturnsTotalNumberOfDifferences() {
        Currency usd = new Currency();
        usd.setCode("USD");
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        
        LocalDate date = LocalDate.of(2024, 1, 15);
        exchangeRateRepository.addRate(createExchangeRate(usd, date, new BigDecimal("30.0")));
        
        exchangeDifferenceService.calculateAndRecordDifference(
            "SALES_INVOICE", 1L, "FAC-001", customer, usd,
            new BigDecimal("100.00"), date, date, "user1"
        );
        
        long count = exchangeDifferenceService.getCount();
        
        assertTrue(count >= 1);
    }

    private ExchangeRate createExchangeRate(Currency currency, LocalDate date, BigDecimal rate) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setCurrency(currency);
        exchangeRate.setDate(date);
        exchangeRate.setRate(rate);
        return exchangeRate;
    }

    static class StubExchangeDifferenceRepository implements ExchangeDifferenceRepository {
        private Long nextId = 1L;
        
        @Override
        public ExchangeDifference save(ExchangeDifference entity) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            return entity;
        }
        
        @Override
        public void update(ExchangeDifference entity) {}
        
        @Override
        public Optional<ExchangeDifference> findById(Long id) { return Optional.empty(); }
        
        @Override
        public List<ExchangeDifference> findAll() { return Arrays.asList(); }
        
        @Override
        public List<ExchangeDifference> findByDocumentType(String documentType) { return Arrays.asList(); }
        
        @Override
        public List<ExchangeDifference> findByThirdPartyId(Long thirdPartyId) { return Arrays.asList(); }
        
        @Override
        public List<ExchangeDifference> findByDateRange(LocalDate startDate, LocalDate endDate) { return Arrays.asList(); }
        
        @Override
        public long count() { return 0; }
        
        @Override
        public void deleteById(Long id) {}
    }

    static class StubExchangeRateRepository implements ExchangeRateRepository {
        @Override
        public Optional<ExchangeRate> findByCurrencyAndDate(Currency currency, LocalDate date) { return Optional.empty(); }
        
        @Override
        public List<ExchangeRate> findByCurrencyBeforeDate(Currency currency, LocalDate date) { return Arrays.asList(); }
        
        @Override
        public Optional<ExchangeRate> findCurrentByCurrency(Currency currency) { return Optional.empty(); }
        
        public void addRate(ExchangeRate rate) {}
    }

    static class StubTransactionService extends TransactionService {
        public StubTransactionService() { super(null, null, null); }
        
        @Override
        public Transaction createTransaction(TransactionEntryData entry1, TransactionEntryData entry2, 
                                           String type, String description, String reference, 
                                           LocalDate date, String username) {
            Transaction transaction = new Transaction();
            transaction.setId(1L);
            return transaction;
        }
    }

    static class StubAccountRepository implements AccountRepository {
        @Override
        public Optional<Account> findByCode(String code) {
            Account account = new Account();
            account.setCode(code);
            account.setName("Test Account");
            return Optional.of(account);
        }
        
        @Override public Optional<Account> findById(Long id) { return Optional.empty(); }
        @Override public List<Account> findAll() { return Arrays.asList(); }
        @Override public List<Account> findByParentId(Long parentId) { return Arrays.asList(); }
        @Override public List<Account> findByType(Account.AccountType type) { return Arrays.asList(); }
        @Override public Account save(Account entity) { return entity; }
        @Override public void update(Account entity) {}
        @Override public void deleteById(Long id) {}
        @Override public long count() { return 0; }
        @Override public long countByParentId(Long parentId) { return 0; }
    }

    static class StubAuditService extends AuditService {
        public int logCount = 0;
        public StubAuditService() { super(null); }
        
        @Override
        public void logWithValues(String username, AuditLog.OperationType operationType, 
                                 String entityType, Long entityId, String description, 
                                 Object oldValue, Object newValue) {
            logCount++;
        }
    }
}
