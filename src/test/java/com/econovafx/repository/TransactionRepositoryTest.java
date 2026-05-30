package com.econovafx.repository;

import com.econovafx.config.DatabaseConfig;
import com.econovafx.domain.*;
import io.ebean.Database;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionRepositoryTest {

    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private UserRepository userRepository;
    private Database db;

    @BeforeAll
    void setUpAll() {
        DatabaseConfig.initialize();
        db = DatabaseConfig.getServer();
        
        userRepository = new UserRepository(db);
        accountRepository = new AccountRepository(db);
        transactionRepository = new TransactionRepository(db);
    }

    @Test
    @DisplayName("Guardar transacción exitosamente")
    void testSave_Success()  {
        // Arrange
        User user = createTestUser("testuser1");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-001", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        Transaction transaction = new Transaction();
        transaction.setNumber("TRX-001");
        transaction.setDate(LocalDate.now());
        transaction.setType("INCOME");
        transaction.setDescription("Pago de servicios");
        transaction.setReference("REF-001");
        
        // Act
        transactionRepository.save(transaction);
        
        // Assert
        assertNotNull(transaction.getId());
        assertTrue(transaction.getId() > 0);
    }

    @Test
    @DisplayName("Buscar transacción por ID existente")
    void testFindById_Exists()  {
        // Arrange
        User user = createTestUser("testuser2");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-002", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        Transaction transaction = new Transaction();
        transaction.setNumber("TRX-002");
        transaction.setDate(LocalDate.now());
        transaction.setType("INCOME");
        transaction.setDescription("Depósito");
        transaction.setReference("REF-002");
        transactionRepository.save(transaction);
        
        // Act
        Optional<Transaction> found = transactionRepository.findById(transaction.getId());
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("Depósito", found.get().getDescription());
        assertEquals("INCOME", found.get().getType());
    }

    @Test
    @DisplayName("Buscar transacción por ID inexistente")
    void testFindById_NotExists() {
        // Act
        Optional<Transaction> found = transactionRepository.findById(99999L);
        
        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Obtener todas las transacciones de una cuenta")
    void testFindByAccount()  {
        // Arrange
        User user = createTestUser("testuser3");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-003", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        Transaction t1 = createTransaction("T1", "EXPENSE");
        Transaction t2 = createTransaction("T2", "INCOME");
        Transaction t3 = createTransaction("T3", "EXPENSE");
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        
        // Act
        List<Transaction> transactions = transactionRepository.findAll();
        
        // Assert
        assertEquals(3, transactions.size());
    }

    @Test
    @DisplayName("Actualizar transacción")
    void testUpdate()  {
        // Arrange
        User user = createTestUser("testuser4");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-004", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        Transaction transaction = new Transaction();
        transaction.setNumber("TRX-004");
        transaction.setDate(LocalDate.now());
        transaction.setType("INCOME");
        transaction.setDescription("Transacción Original");
        transaction.setReference("REF-004");
        transactionRepository.save(transaction);
        
        // Act
        transaction.setDescription("Transacción Actualizada");
        transaction.setReference("REF-004-UPD");
        transactionRepository.update(transaction);
        
        // Assert
        Optional<Transaction> updated = transactionRepository.findById(transaction.getId());
        assertTrue(updated.isPresent());
        assertEquals("Transacción Actualizada", updated.get().getDescription());
        assertEquals("REF-004-UPD", updated.get().getReference());
    }

    @Test
    @DisplayName("Eliminar transacción exitosamente")
    void testDelete_Success()  {
        // Arrange
        User user = createTestUser("testuser5");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-005", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        Transaction transaction = new Transaction();
        transaction.setNumber("TRX-005");
        transaction.setDate(LocalDate.now());
        transaction.setType("INCOME");
        transaction.setDescription("Transacción a Eliminar");
        transaction.setReference("REF-005");
        transactionRepository.save(transaction);
        Long transactionId = transaction.getId();
        
        // Act
        transactionRepository.delete(transaction);
        
        // Assert
        Optional<Transaction> deleted = transactionRepository.findById(transactionId);
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("Buscar transacciones por tipo")
    void testFindByType()  {
        // Arrange
        User user = createTestUser("testuser6");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-006", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        Transaction t1 = createTransaction("Income 1", "INCOME");
        Transaction t2 = createTransaction("Expense 1", "EXPENSE");
        Transaction t3 = createTransaction("Income 2", "INCOME");
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        
        // Act
        List<Transaction> incomes = transactionRepository.findByType("INCOME");
        
        // Assert
        assertEquals(2, incomes.size());
        assertTrue(incomes.stream().allMatch(t -> "INCOME".equals(t.getType())));
    }

    @Test
    @DisplayName("Buscar transacciones por rango de fechas")
    void testFindByDateRange()  {
        // Arrange
        User user = createTestUser("testuser7");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-007", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusWeeks(1);
        
        Transaction t1 = createTransactionWithDate("Hoy", "INCOME", today);
        Transaction t2 = createTransactionWithDate("Ayer", "INCOME", yesterday);
        Transaction t3 = createTransactionWithDate("Semana pasada", "INCOME", lastWeek);
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        
        // Act
        List<Transaction> recentTransactions = transactionRepository.findByDateRange(yesterday, today);
        
        // Assert
        assertEquals(2, recentTransactions.size());
    }

    @Test
    @DisplayName("Contar transacciones")
    void testCount()  {
        // Arrange
        User user = createTestUser("testuser8");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-008", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        transactionRepository.save(createTransaction("T1", "INCOME"));
        transactionRepository.save(createTransaction("T2", "INCOME"));
        transactionRepository.save(createTransaction("T3", "INCOME"));
        transactionRepository.save(createTransaction("T4", "INCOME"));
        
        // Act
        long count = transactionRepository.count();
        
        // Assert
        assertEquals(4, count);
    }

    @Test
    @DisplayName("Buscar transacciones por descripción")
    void testSearchByDescription()  {
        // Arrange
        User user = createTestUser("testuser9");
        userRepository.save(user);
        
        Account account = createTestAccount("ACC-009", "Cuenta Test", AccountType.ASSET);
        accountRepository.save(account);
        
        transactionRepository.save(createTransaction("Pago de luz", "EXPENSE"));
        transactionRepository.save(createTransaction("Pago de agua", "EXPENSE"));
        transactionRepository.save(createTransaction("Salario", "INCOME"));
        
        // Act
        List<Transaction> pagos = transactionRepository.searchByDescription("Pago");
        
        // Assert
        assertEquals(2, pagos.size());
        assertTrue(pagos.stream().allMatch(t -> t.getDescription().contains("Pago")));
    }
    
    // Helper methods
    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        user.setFullName("Test User " + username);
        return user;
    }
    
    private Account createTestAccount(String code, String name, AccountType type) {
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setType(type);
        account.setBalance(BigDecimal.valueOf(1000.0));
        return account;
    }
    
    private Transaction createTransaction(String description, String type) {
        Transaction transaction = new Transaction();
        transaction.setNumber("TRX-" + System.currentTimeMillis());
        transaction.setDate(LocalDate.now());
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setReference("REF-" + System.currentTimeMillis());
        return transaction;
    }
    
    private Transaction createTransactionWithDate(String description, String type, LocalDate date) {
        Transaction transaction = createTransaction(description, type);
        transaction.setDate(date);
        return transaction;
    }
}
