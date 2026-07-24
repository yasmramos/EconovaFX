package com.econovafx.modules.bank.service;

import com.econovafx.modules.bank.model.BankAccount;
import com.econovafx.modules.bank.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository repository;

    private BankAccountService bankAccountService;

    @BeforeEach
    void setUp() {
        // Usamos reflection para inyectar el mock en el campo final
        try {
            var field = BankAccountService.class.getDeclaredField("repository");
            field.setAccessible(true);
            bankAccountService = new BankAccountService();
            field.set(bankAccountService, repository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock", e);
        }
    }

    @Test
    void testCreateAccount_ValidAccount_Success() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK-001");
        account.setAccountNumber("1234567890");
        account.setAccountingAccount("1.1.1");
        account.setBankName("Banco Nacional");
        account.setBalance(new BigDecimal("10000.00"));

        when(repository.save(account)).thenReturn(account);

        // Act
        BankAccount result = bankAccountService.createAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals("BK-001", result.getCode());
        verify(repository).save(account);
    }

    @Test
    void testCreateAccount_NullCode_ThrowsException() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode(null);
        account.setAccountNumber("1234567890");
        account.setAccountingAccount("1.1.1");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bankAccountService.createAccount(account)
        );
        assertEquals("Account code is required", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateAccount_EmptyCode_ThrowsException() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("  ");
        account.setAccountNumber("1234567890");
        account.setAccountingAccount("1.1.1");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bankAccountService.createAccount(account)
        );
        assertEquals("Account code is required", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateAccount_NullAccountNumber_ThrowsException() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK-001");
        account.setAccountNumber(null);
        account.setAccountingAccount("1.1.1");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bankAccountService.createAccount(account)
        );
        assertEquals("Account number is required", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateAccount_EmptyAccountNumber_ThrowsException() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK-001");
        account.setAccountNumber("  ");
        account.setAccountingAccount("1.1.1");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bankAccountService.createAccount(account)
        );
        assertEquals("Account number is required", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateAccount_NullAccountingAccount_ThrowsException() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK-001");
        account.setAccountNumber("1234567890");
        account.setAccountingAccount(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bankAccountService.createAccount(account)
        );
        assertEquals("Accounting account is required per Resolution 340/2004", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateAccount_EmptyAccountingAccount_ThrowsException() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK-001");
        account.setAccountNumber("1234567890");
        account.setAccountingAccount("  ");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bankAccountService.createAccount(account)
        );
        assertEquals("Accounting account is required per Resolution 340/2004", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testGetAccount_ExistingId_ReturnsAccount() {
        // Arrange
        Long accountId = 1L;
        BankAccount account = new BankAccount();
        account.setId(accountId);
        account.setCode("BK-001");

        when(repository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        Optional<BankAccount> result = bankAccountService.getAccount(accountId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("BK-001", result.get().getCode());
        verify(repository).findById(accountId);
    }

    @Test
    void testGetAccount_NonExistentId_ReturnsEmpty() {
        // Arrange
        Long accountId = 999L;
        when(repository.findById(accountId)).thenReturn(Optional.empty());

        // Act
        Optional<BankAccount> result = bankAccountService.getAccount(accountId);

        // Assert
        assertFalse(result.isPresent());
        verify(repository).findById(accountId);
    }

    @Test
    void testGetAllAccounts_ReturnsAllAccounts() {
        // Arrange
        BankAccount account1 = new BankAccount();
        account1.setId(1L);
        account1.setCode("BK-001");

        BankAccount account2 = new BankAccount();
        account2.setId(2L);
        account2.setCode("BK-002");

        when(repository.findAll()).thenReturn(List.of(account1, account2));

        // Act
        List<BankAccount> result = bankAccountService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void testGetAllAccounts_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of());

        // Act
        List<BankAccount> result = bankAccountService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    void testGetActiveAccounts_ReturnsOnlyActiveAccounts() {
        // Arrange
        BankAccount activeAccount = new BankAccount();
        activeAccount.setId(1L);
        activeAccount.setCode("BK-001");
        activeAccount.setActive(true);

        BankAccount inactiveAccount = new BankAccount();
        inactiveAccount.setId(2L);
        inactiveAccount.setCode("BK-002");
        inactiveAccount.setActive(false);

        when(repository.findActiveAccounts()).thenReturn(List.of(activeAccount));

        // Act
        List<BankAccount> result = bankAccountService.getActiveAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(repository).findActiveAccounts();
    }

    @Test
    void testUpdateAccount_ValidAccount_Success() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setCode("BK-001");
        account.setAccountNumber("1234567890");

        when(repository.save(account)).thenReturn(account);

        // Act
        BankAccount result = bankAccountService.updateAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(account);
    }

    @Test
    void testUpdateAccount_NullId_ThrowsException() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(null);
        account.setCode("BK-001");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bankAccountService.updateAccount(account)
        );
        assertEquals("Account ID is required for update", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testDeleteAccount_ExistingId_Success() {
        // Arrange
        Long accountId = 1L;
        when(repository.deleteById(accountId)).thenReturn(true);

        // Act
        boolean result = bankAccountService.deleteAccount(accountId);

        // Assert
        assertTrue(result);
        verify(repository).deleteById(accountId);
    }

    @Test
    void testDeleteAccount_NonExistentId_ReturnsFalse() {
        // Arrange
        Long accountId = 999L;
        when(repository.deleteById(accountId)).thenReturn(false);

        // Act
        boolean result = bankAccountService.deleteAccount(accountId);

        // Assert
        assertFalse(result);
        verify(repository).deleteById(accountId);
    }

    @Test
    void testUpdateBalance_ValidBalance_Success() {
        // Arrange
        Long accountId = 1L;
        BigDecimal newBalance = new BigDecimal("50000.00");

        // Act
        bankAccountService.updateBalance(accountId, newBalance);

        // Assert
        verify(repository).updateBalance(accountId, newBalance);
    }

    @Test
    void testUpdateBalance_ZeroBalance_Success() {
        // Arrange
        Long accountId = 1L;
        BigDecimal newBalance = BigDecimal.ZERO;

        // Act
        bankAccountService.updateBalance(accountId, newBalance);

        // Assert
        verify(repository).updateBalance(accountId, newBalance);
    }

    @Test
    void testUpdateBalance_NegativeBalance_Success() {
        // Arrange
        Long accountId = 1L;
        BigDecimal newBalance = new BigDecimal("-1000.00");

        // Act
        bankAccountService.updateBalance(accountId, newBalance);

        // Assert
        verify(repository).updateBalance(accountId, newBalance);
    }

    @Test
    void testCreateAccount_WithAllFields_Success() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK-003");
        account.setAccountNumber("9876543210");
        account.setAccountingAccount("1.1.2");
        account.setBankName("Banco Internacional");
        account.setBranch("Sucursal Centro");
        account.setCurrency("USD");
        account.setBalance(new BigDecimal("25000.00"));
        account.setActive(true);
        account.setDescription("Cuenta en dólares");

        when(repository.save(account)).thenReturn(account);

        // Act
        BankAccount result = bankAccountService.createAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals("Banco Internacional", result.getBankName());
        assertEquals("USD", result.getCurrency());
        verify(repository).save(account);
    }

    @Test
    void testGetActiveAccounts_NoActiveAccounts_ReturnsEmptyList() {
        // Arrange
        when(repository.findActiveAccounts()).thenReturn(List.of());

        // Act
        List<BankAccount> result = bankAccountService.getActiveAccounts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findActiveAccounts();
    }
}
