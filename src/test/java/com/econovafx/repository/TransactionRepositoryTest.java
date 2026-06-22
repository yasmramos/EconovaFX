package com.econovafx.repository;

import com.econovafx.config.DatabaseConfig;
import com.econovafx.model.*;
import com.econovafx.model.User.UserRole;
import io.ebean.Database;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TransactionRepository with proper isolation
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionRepositoryTest {

    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private Database db;

    @BeforeAll
    void setUpAll() {
        DatabaseConfig.initialize();
        db = DatabaseConfig.getServer();
        
        companyRepository = new CompanyRepository();
        userRepository = new UserRepository(db);
        accountRepository = new AccountRepository(db);
        transactionRepository = new TransactionRepository(db);
    }
    
    @BeforeEach
    void setUp() {
        // Clean database using delete for proper isolation while keeping schema
        // Disable foreign key constraints temporarily
        try {
            db.sqlUpdate("SET REFERENTIAL_INTEGRITY FALSE").execute();
        } catch (Exception e) {
            // Ignore if already disabled
        }
        
        // Delete all data in reverse order of dependencies
        try { db.sqlUpdate("DELETE FROM transaction_entries").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM transactions").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM third_parties").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM accounts").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM user_accounts").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM users").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM companies").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM accounting_periods").execute(); } catch (Exception e) {}
        
        // Re-enable foreign key constraints
        try {
            db.sqlUpdate("SET REFERENTIAL_INTEGRITY TRUE").execute();
        } catch (Exception e) {
            // Ignore if already enabled
        }
    }
    
    @AfterEach
    void tearDown() {
        // Additional cleanup after each test to ensure isolation
        try {
            db.sqlUpdate("SET REFERENTIAL_INTEGRITY FALSE").execute();
        } catch (Exception e) {
            // Ignore if already disabled
        }
        
        try { db.sqlUpdate("DELETE FROM transaction_entries").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM transactions").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM third_parties").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM accounts").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM user_accounts").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM users").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM companies").execute(); } catch (Exception e) {}
        try { db.sqlUpdate("DELETE FROM accounting_periods").execute(); } catch (Exception e) {}
        
        try {
            db.sqlUpdate("SET REFERENTIAL_INTEGRITY TRUE").execute();
        } catch (Exception e) {
            // Ignore if already enabled
        }
    }
    
    // Helper methods to create test data
    private Company createTestCompany(String name) {
        Company company = new Company();
        company.setName(name);
        company.setCode("CODE-" + name.replace(" ", "-"));
        company.setNif("NIF-" + name.replace(" ", "-"));
        company.setStatus("ACTIVE");
        return company;
    }
    
    private User createTestUser(String username) {
        Company company = createTestCompany("Test Company for " + username);
        companyRepository.save(company);
        
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setEmail(username + "@test.com");
        user.setFullName(username.replace("_", " ").toUpperCase());
        user.setRole(UserRole.ADMIN);
        user.setCompany(company);
        return user;
    }
    
    private Account createTestAccount(String code, String name, AccountType type) {
        Company company = createTestCompany("Test Company for Account " + code);
        companyRepository.save(company);
        
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setType(type);
        account.setBalance(BigDecimal.ZERO);
        return account;
    }
    
    private com.econovafx.model.Transaction createTestTransaction(String number, Account account) {
        com.econovafx.model.Transaction transaction = new com.econovafx.model.Transaction();
        transaction.setNumber(number);
        transaction.setDate(LocalDate.now());
        transaction.setType("INCOME");
        transaction.setDescription("Test transaction");
        transaction.setReference("REF-" + number);
        return transaction;
    }

    @Test
    @DisplayName("Save transaction successfully")
    void testSave_Success() {
        // Arrange
        User user = createTestUser("testuser1");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-001", "Test Account", AccountType.ASSET);
        accountRepository.save(account);
        
        com.econovafx.model.Transaction transaction = createTestTransaction("TRX-001", account);
        
        // Act
        transactionRepository.save(transaction);
        
        // Assert
        assertNotNull(transaction.getId());
        assertTrue(transaction.getId() > 0);
    }

    @Test
    @DisplayName("Find transaction by existing ID")
    void testFindById_Exists() {
        // Arrange
        User user = createTestUser("testuser2");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-002", "Test Account", AccountType.ASSET);
        accountRepository.save(account);
        
        com.econovafx.model.Transaction transaction = createTestTransaction("TRX-002", account);
        transactionRepository.save(transaction);
        
        // Act
        var found = transactionRepository.findById(transaction.getId());
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("TRX-002", found.get().getNumber());
    }

    @Test
    @DisplayName("Find transaction by non-existing ID returns empty")
    void testFindById_NotExists() {
        // Act
        var found = transactionRepository.findById(999L);
        
        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Find all transactions")
    void testFindAll() {
        // Arrange
        User user = createTestUser("testuser3");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-003", "Test Account", AccountType.ASSET);
        accountRepository.save(account);
        
        transactionRepository.save(createTestTransaction("TRX-003", account));
        transactionRepository.save(createTestTransaction("TRX-004", account));
        
        // Act
        List<com.econovafx.model.Transaction> transactions = transactionRepository.findAll();
        
        // Assert
        assertTrue(transactions.size() >= 2);
    }

    @Test
    @DisplayName("Delete transaction successfully")
    void testDelete_Success() {
        // Arrange
        User user = createTestUser("testuser4");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-004", "Test Account", AccountType.ASSET);
        accountRepository.save(account);
        
        com.econovafx.model.Transaction transaction = createTestTransaction("TRX-005", account);
        transactionRepository.save(transaction);
        Long id = transaction.getId();
        
        // Act
        transactionRepository.deleteById(id);
        
        // Assert
        assertFalse(transactionRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("Count transactions")
    void testCount() {
        // Arrange - create fresh data for this test only
        User user = createTestUser("testuser_count");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-COUNT", "Test Account Count", AccountType.ASSET);
        accountRepository.save(account);
        
        transactionRepository.save(createTestTransaction("TRX-COUNT-1", account));
        transactionRepository.save(createTestTransaction("TRX-COUNT-2", account));
        transactionRepository.save(createTestTransaction("TRX-COUNT-3", account));
        transactionRepository.save(createTestTransaction("TRX-COUNT-4", account));
        
        // Act
        long count = transactionRepository.count();
        
        // Assert
        assertEquals(4, count);
    }

    @Test
    @DisplayName("Find transactions by type")
    void testFindByType() {
        // Arrange - create fresh data for this test only
        User user = createTestUser("testuser_type");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-TYPE", "Test Account Type", AccountType.ASSET);
        accountRepository.save(account);
        
        com.econovafx.model.Transaction t1 = createTestTransaction("TRX-TYPE-1", account);
        t1.setType("INCOME");
        com.econovafx.model.Transaction t2 = createTestTransaction("TRX-TYPE-2", account);
        t2.setType("EXPENSE");
        com.econovafx.model.Transaction t3 = createTestTransaction("TRX-TYPE-3", account);
        t3.setType("INCOME");
        
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        
        // Act
        List<com.econovafx.model.Transaction> incomeTransactions = transactionRepository.findByType("INCOME");
        
        // Assert
        assertEquals(2, incomeTransactions.size());
        assertTrue(incomeTransactions.stream().allMatch(t -> "INCOME".equals(t.getType())));
    }

    @Test
    @DisplayName("Find transactions by date range")
    void testFindByDateRange() {
        // Arrange - create fresh data for this test only
        User user = createTestUser("testuser_date");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-DATE", "Test Account Date", AccountType.ASSET);
        accountRepository.save(account);
        
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        
        com.econovafx.model.Transaction t1 = createTestTransaction("TRX-DATE-1", account);
        t1.setDate(startDate);
        com.econovafx.model.Transaction t2 = createTestTransaction("TRX-DATE-2", account);
        t2.setDate(LocalDate.now());
        com.econovafx.model.Transaction t3 = createTestTransaction("TRX-DATE-3", account);
        t3.setDate(endDate);
        
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        
        // Act
        List<com.econovafx.model.Transaction> result = transactionRepository.findByDateRange(startDate, endDate);
        
        // Assert
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Search transactions by description")
    void testSearchByDescription() {
        // Arrange
        User user = createTestUser("testuser8");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-008", "Test Account", AccountType.ASSET);
        accountRepository.save(account);
        
        com.econovafx.model.Transaction t1 = createTestTransaction("TRX-016", account);
        t1.setDescription("Payment for services");
        com.econovafx.model.Transaction t2 = createTestTransaction("TRX-017", account);
        t2.setDescription("Office supplies purchase");
        com.econovafx.model.Transaction t3 = createTestTransaction("TRX-018", account);
        t3.setDescription("Service payment received");
        
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        
        // Act
        List<com.econovafx.model.Transaction> result = transactionRepository.searchByDescription("payment");
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getDescription().toLowerCase().contains("payment")));
    }

    @Test
    @DisplayName("Find transactions by third party")
    void testFindByThirdParty() {
        // Arrange
        User user = createTestUser("testuser9");
        userRepository.save(user);
        
        Account account1 = createTestAccount("ACC-009", "Test Account 1", AccountType.ASSET);
        Account account2 = createTestAccount("ACC-010", "Test Account 2", AccountType.ASSET);
        accountRepository.save(account1);
        accountRepository.save(account2);
        
        ThirdParty thirdParty = new ThirdParty();
        thirdParty.setName("Test Third Party");
        thirdParty.setIdentificationNumber("ID-001");
        thirdParty.setType(ThirdParty.ThirdPartyType.CUSTOMER);
        thirdParty.setEmail("test@thirdparty.com");
        thirdParty.setIsActive(true);
        
        // Save third party using a direct database insert since we don't have a repository in this test
        db.insert(thirdParty);
        
        com.econovafx.model.Transaction t1 = createTestTransaction("TRX-019", account1);
        t1.setThirdParty(thirdParty);
        com.econovafx.model.Transaction t2 = createTestTransaction("TRX-020", account1);
        t2.setThirdParty(thirdParty);
        com.econovafx.model.Transaction t3 = createTestTransaction("TRX-021", account1);
        t3.setThirdParty(thirdParty);
        com.econovafx.model.Transaction t4 = createTestTransaction("TRX-022", account2);
        // t4 has no third party
        
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        transactionRepository.save(t4);
        
        // Act
        List<com.econovafx.model.Transaction> result = transactionRepository.findByThirdPartyId(thirdParty.getId());
        
        // Assert
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(t -> t.getThirdParty() != null && t.getThirdParty().getId().equals(thirdParty.getId())));
    }
}
