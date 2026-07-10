package com.econovafx.security;

import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.core.model.ExchangeRate;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.billing.repository.ThirdPartyRepository;
import com.econovafx.modules.core.repository.ExchangeRateRepository;
import com.econovafx.modules.core.repository.AuditLogRepository;
import com.econovafx.modules.core.repository.CompanyRepository;
import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Pruebas de aislamiento entre tenants para verificar seguridad multi-tenant.
 * 
 * Estas pruebas validan que un usuario de la Empresa A NO pueda acceder 
 * a datos de la Empresa B, garantizando el aislamiento completo.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Pending fix for multi-tenancy context in test environment - DB separation issue")
public class TenantIsolationTest {

    private static final String TENANT_A_CODE = "TENANT_A_TEST";
    private static final String TENANT_B_CODE = "TENANT_B_TEST";
    
    private Database db;
    private Company companyA;
    private Company companyB;

    @BeforeAll
    static void setupTenants() {
        // Configurar bases de datos de prueba para ambos tenants
        System.setProperty("ebean.datasource.tenant_a_test.databaseUrl", "jdbc:h2:mem:tenant_a_test;DB_CLOSE_DELAY=-1");
        System.setProperty("ebean.datasource.tenant_a_test.username", "sa");
        System.setProperty("ebean.datasource.tenant_a_test.password", "");
        System.setProperty("ebean.datasource.tenant_a_test.driver", "org.h2.Driver");
        
        System.setProperty("ebean.datasource.tenant_b_test.databaseUrl", "jdbc:h2:mem:tenant_b_test;DB_CLOSE_DELAY=-1");
        System.setProperty("ebean.datasource.tenant_b_test.username", "sa");
        System.setProperty("ebean.datasource.tenant_b_test.password", "");
        System.setProperty("ebean.datasource.tenant_b_test.driver", "org.h2.Driver");
    }

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private ThirdPartyRepository thirdPartyRepository;
    private ExchangeRateRepository exchangeRateRepository;
    private AuditLogRepository auditLogRepository;
    
    @BeforeEach
    void setUp() {
        db = DB.getDefault();
        accountRepository = new AccountRepository(db);
        transactionRepository = new TransactionRepository(db);
        thirdPartyRepository = new ThirdPartyRepository(db);
        exchangeRateRepository = new ExchangeRateRepository(db);
        auditLogRepository = new AuditLogRepository(db);
        
        cleanupData();
        
        // Crear companies de prueba directamente con Ebean
        companyA = new Company();
        companyA.setName("Empresa A Test");
        companyA.setCode(TENANT_A_CODE);
        companyA.setNif("NIF-A-TEST");
        companyA.setDatabaseUrl("jdbc:h2:mem:tenant_a_test");
        companyA.setStatus("ACTIVE");
        db.save(companyA);
        
        companyB = new Company();
        companyB.setName("Empresa B Test");
        companyB.setCode(TENANT_B_CODE);
        companyB.setNif("NIF-B-TEST");
        companyB.setDatabaseUrl("jdbc:h2:mem:tenant_b_test");
        companyB.setStatus("ACTIVE");
        db.save(companyB);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        cleanupData();
    }

    private void cleanupData() {
        // Limpiar datos de prueba en orden inverso a dependencias
        try {
            db.sqlUpdate("DELETE FROM audit_log").execute();
            db.sqlUpdate("DELETE FROM transaction").execute();
            db.sqlUpdate("DELETE FROM account").execute();
            db.sqlUpdate("DELETE FROM third_party").execute();
            db.sqlUpdate("DELETE FROM exchange_rate").execute();
        } catch (Exception e) {
            // Ignorar errores si las tablas no existen aún
        }
    }

    /**
     * Test 1: Verificar que transacciones creadas en Tenant A 
     * no sean visibles desde Tenant B
     */
    @Test
    @Order(1)
    public void testTransactionIsolation() {
        // Crear transacción en Tenant A
        TenantContext.setCurrentTenant(companyA);
        Transaction transactionA = new Transaction();
        transactionA.setNumber("TRANS-A-001");
        transactionA.setDate(java.time.LocalDate.now());
        transactionA.setDescription("Transacción Tenant A");
        transactionA.setTotalDebit(BigDecimal.valueOf(1000));
        transactionRepository.save(transactionA);
        
        Long transactionAId = transactionA.getId();
        
        // Cambiar a Tenant B y verificar que NO puede ver la transacción de A
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyB);
        
        Optional<Transaction> foundTransaction = transactionRepository.findById(transactionAId);
        
        // La transacción NO debe ser encontrada porque pertenece a otro tenant
        assertFalse(foundTransaction.isPresent(), 
            "Tenant B no debería poder ver transacciones de Tenant A");
        
        // Crear transacción en Tenant B
        Transaction transactionB = new Transaction();
        transactionB.setNumber("TRANS-B-001");
        transactionB.setDate(java.time.LocalDate.now());
        transactionB.setDescription("Transacción Tenant B");
        transactionB.setTotalDebit(BigDecimal.valueOf(2000));
        transactionRepository.save(transactionB);
        
        // Verificar que Tenant B solo ve su propia transacción
        List<Transaction> transactionsB = transactionRepository.findAll();
        assertEquals(1, transactionsB.size(), 
            "Tenant B solo debería ver 1 transacción (la suya)");
        assertEquals("TRANS-B-001", transactionsB.get(0).getNumber());
        
        // Volver a Tenant A y verificar que solo ve su transacción
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyA);
        List<Transaction> transactionsA = transactionRepository.findAll();
        assertEquals(1, transactionsA.size(), 
            "Tenant A solo debería ver 1 transacción (la suya)");
        assertEquals("TRANS-A-001", transactionsA.get(0).getNumber());
    }

    /**
     * Test 2: Verificar que cuentas contables están aisladas entre tenants
     */
    @Test
    @Order(2)
    public void testAccountIsolation() {
        // Crear cuenta en Tenant A
        TenantContext.setCurrentTenant(companyA);
        Account accountA = new Account();
        accountA.setCode("1100-A");
        accountA.setName("Caja Tenant A");
        accountA.setType(AccountType.ASSET);
        accountRepository.save(accountA);
        
        Long accountAId = accountA.getId();
        
        // Cambiar a Tenant B y verificar aislamiento
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyB);
        
        Optional<Account> foundAccount = accountRepository.findById(accountAId);
        assertFalse(foundAccount.isPresent(), 
            "Tenant B no debería poder ver cuentas de Tenant A");
        
        // Crear cuenta en Tenant B
        Account accountB = new Account();
        accountB.setCode("1100-B");
        accountB.setName("Caja Tenant B");
        accountB.setType(AccountType.ASSET);
        accountRepository.save(accountB);
        
        // Verificar conteo correcto por tenant
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyA);
        assertEquals(1, accountRepository.findAll().size());
        
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyB);
        assertEquals(1, accountRepository.findAll().size());
    }

    /**
     * Test 3: Verificar que terceros (clientes/proveedores) están aislados
     */
    @Test
    @Order(3)
    public void testThirdPartyIsolation() {
        // Crear tercero en Tenant A
        TenantContext.setCurrentTenant(companyA);
        ThirdParty thirdPartyA = new ThirdParty();
        thirdPartyA.setName("Cliente Tenant A");
        thirdPartyA.setIdentificationNumber("900111222-A");
        thirdPartyA.setType(ThirdParty.ThirdPartyType.CUSTOMER);
        thirdPartyA.setEmail("cliente@tenant-a.com");
        thirdPartyRepository.save(thirdPartyA);
        
        Long thirdPartyAId = thirdPartyA.getId();
        
        // Cambiar a Tenant B
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyB);
        
        // Intentar buscar tercero de Tenant A por ID
        Optional<ThirdParty> found = thirdPartyRepository.findById(thirdPartyAId);
        assertFalse(found.isPresent(), 
            "Tenant B no debería poder ver terceros de Tenant A");
        
        // Intentar buscar por número de documento (debería retornar vacío)
        List<ThirdParty> searchResult = thirdPartyRepository
            .query()
            .eq("identificationNumber", "900111222-A")
            .findList();
        assertTrue(searchResult.isEmpty(), 
            "Búsqueda por documento no debería retornar resultados de otro tenant");
    }

    /**
     * Test 4: Verificar que tasas de cambio están aisladas
     */
    @Test
    @Order(4)
    public void testExchangeRateIsolation() {
        // Crear tasa en Tenant A
        TenantContext.setCurrentTenant(companyA);
        ExchangeRate rateA = new ExchangeRate();
        rateA.setRate(BigDecimal.valueOf(4000));
        rateA.setEffectiveDate(LocalDateTime.now());
        exchangeRateRepository.save(rateA);
        
        Long rateAId = rateA.getId();
        
        // Cambiar a Tenant B
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyB);
        
        // Verificar que no puede acceder a tasa de Tenant A
        Optional<ExchangeRate> found = exchangeRateRepository.findById(rateAId);
        assertFalse(found.isPresent(), 
            "Tenant B no debería poder ver tasas de cambio de Tenant A");
        
        // Crear tasa diferente en Tenant B
        ExchangeRate rateB = new ExchangeRate();
        rateB.setRate(BigDecimal.valueOf(4500)); // Tasa diferente
        rateB.setEffectiveDate(LocalDateTime.now());
        exchangeRateRepository.save(rateB);
        
        // Verificar que cada tenant tiene su propia tasa
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyA);
        List<ExchangeRate> ratesA = exchangeRateRepository.findAllActive();
        assertEquals(1, ratesA.size());
        assertEquals(BigDecimal.valueOf(4000), ratesA.get(0).getRate());
        
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyB);
        List<ExchangeRate> ratesB = exchangeRateRepository.findAllActive();
        assertEquals(1, ratesB.size());
        assertEquals(BigDecimal.valueOf(4500), ratesB.get(0).getRate());
    }

    /**
     * Test 5: Verificar que logs de auditoría están aislados
     */
    @Test
    @Order(5)
    public void testAuditLogIsolation() {
        // Crear log en Tenant A
        TenantContext.setCurrentTenant(companyA);
        AuditLog logA = new AuditLog();
        logA.setUsername("user_a");
        logA.setOperationType(AuditLog.OperationType.CREATE);
        logA.setEntityType("Transaction");
        logA.setEntityId(1L);
        logA.setDescription("Test log Tenant A");
        logA.setIpAddress("192.168.1.1");
        auditLogRepository.save(logA);
        
        Long logAId = logA.getId();
        
        // Cambiar a Tenant B
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyB);
        
        // Verificar aislamiento
        Optional<AuditLog> found = auditLogRepository.findById(logAId);
        assertFalse(found.isPresent(), 
            "Tenant B no debería poder ver logs de auditoría de Tenant A");
        
        // Verificar conteo
        assertEquals(0, auditLogRepository.findAll().size(), 
            "Tenant B no debería tener logs visibles");
    }

    /**
     * Test 6: Verificar comportamiento con contexto de tenant nulo
     */
    @Test
    @Order(6)
    public void testNullTenantContext() {
        // Limpiar contexto
        TenantContext.clear();
        
        // Intentar crear dato sin tenant activo debería fallar o no persistir
        // Esto depende de la configuración de Ebean con TenantMode.DB
        assertThrows(RuntimeException.class, () -> {
            Account account = new Account();
            account.setCode("FAIL-CODE");
            account.setName("Cuenta sin tenant");
            account.setType(AccountType.ASSET);
            accountRepository.save(account);
        }, "Debería fallar al intentar guardar sin tenant activo");
    }

    /**
     * Test 7: Verificar concurrencia - múltiples threads con diferentes tenants
     */
    @Test
    @Order(7)
    public void testConcurrentTenantAccess() throws InterruptedException {
        final int[] errors = {0};
        
        Thread threadA = new Thread(() -> {
            try {
                TenantContext.setCurrentTenant(companyA);
                for (int i = 0; i < 10; i++) {
                    Account account = new Account();
                    account.setCode("CONCURRENT-A-" + i);
                    account.setName("Cuenta Concurrente A" + i);
                    account.setType(AccountType.ASSET);
                    accountRepository.save(account);
                    
                    // Verificar que solo ve cuentas de Tenant A
                    long count = accountRepository.findAll().stream()
                        .filter(a -> a.getCode().startsWith("CONCURRENT-A"))
                        .count();
                    if (count != i + 1) {
                        errors[0]++;
                    }
                    
                    Thread.sleep(10); // Pequeña pausa para simular concurrencia
                }
            } catch (Exception e) {
                errors[0]++;
            } finally {
                TenantContext.clear();
            }
        });
        
        Thread threadB = new Thread(() -> {
            try {
                TenantContext.setCurrentTenant(companyB);
                for (int i = 0; i < 10; i++) {
                    Account account = new Account();
                    account.setCode("CONCURRENT-B-" + i);
                    account.setName("Cuenta Concurrente B" + i);
                    account.setType(AccountType.ASSET);
                    accountRepository.save(account);
                    
                    // Verificar que solo ve cuentas de Tenant B
                    long count = accountRepository.findAll().stream()
                        .filter(a -> a.getCode().startsWith("CONCURRENT-B"))
                        .count();
                    if (count != i + 1) {
                        errors[0]++;
                    }
                    
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                errors[0]++;
            } finally {
                TenantContext.clear();
            }
        });
        
        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
        
        assertEquals(0, errors[0], 
            "No debería haber errores de aislamiento en acceso concurrente");
        
        // Verificación final: cada tenant tiene exactamente 10 cuentas
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyA);
        long countA = accountRepository.findAll().stream()
            .filter(a -> a.getCode().startsWith("CONCURRENT-A"))
            .count();
        assertEquals(10, countA);
        
        TenantContext.clear();
        TenantContext.setCurrentTenant(companyB);
        long countB = accountRepository.findAll().stream()
            .filter(a -> a.getCode().startsWith("CONCURRENT-B"))
            .count();
        assertEquals(10, countB);
    }
}
