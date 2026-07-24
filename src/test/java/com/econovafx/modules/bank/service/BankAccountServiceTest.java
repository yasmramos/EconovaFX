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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository repository;

    private BankAccountService service;

    @BeforeEach
    void setUp() {
        service = new BankAccountService();
        try {
            java.lang.reflect.Field repoField = BankAccountService.class.getDeclaredField("repository");
            repoField.setAccessible(true);
            repoField.set(service, repository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock", e);
        }
    }

    @Test
    void testCreateAccount_Success() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK001");
        account.setAccountNumber("123456789");
        account.setAccountingAccount("101-001");
        account.setBankName("Test Bank");

        when(repository.save(any(BankAccount.class))).thenReturn(account);

        // Act
        BankAccount result = service.createAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals("BK001", result.getCode());
        verify(repository, times(1)).save(account);
    }

    @Test
    void testCreateAccount_NullCode() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode(null);
        account.setAccountNumber("123456789");
        account.setAccountingAccount("101-001");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(account));
    }

    @Test
    void testCreateAccount_EmptyCode() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("");
        account.setAccountNumber("123456789");
        account.setAccountingAccount("101-001");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(account));
    }

    @Test
    void testCreateAccount_NullAccountNumber() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK001");
        account.setAccountNumber(null);
        account.setAccountingAccount("101-001");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(account));
    }

    @Test
    void testCreateAccount_NullAccountingAccount() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK001");
        account.setAccountNumber("123456789");
        account.setAccountingAccount(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(account));
    }

    @Test
    void testGetAccount_Exists() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setCode("BK001");
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        Optional<BankAccount> result = service.getAccount(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("BK001", result.get().getCode());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testGetAccount_NotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<BankAccount> result = service.getAccount(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void testGetAllAccounts() {
        // Arrange
        BankAccount account1 = new BankAccount();
        account1.setId(1L);
        BankAccount account2 = new BankAccount();
        account2.setId(2L);
        when(repository.findAll()).thenReturn(List.of(account1, account2));

        // Act
        List<BankAccount> result = service.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetActiveAccounts() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setActive(true);
        when(repository.findActiveAccounts()).thenReturn(List.of(account));

        // Act
        List<BankAccount> result = service.getActiveAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findActiveAccounts();
    }

    @Test
    void testUpdateAccount_Success() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setCode("BK001");
        when(repository.save(any(BankAccount.class))).thenReturn(account);

        // Act
        BankAccount result = service.updateAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).save(account);
    }

    @Test
    void testUpdateAccount_NullId() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateAccount(account));
    }

    @Test
    void testDeleteAccount_Success() {
        // Arrange
        when(repository.deleteById(1L)).thenReturn(true);

        // Act
        boolean result = service.deleteAccount(1L);

        // Assert
        assertTrue(result);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAccount_NotFound() {
        // Arrange
        when(repository.deleteById(999L)).thenReturn(false);

        // Act
        boolean result = service.deleteAccount(999L);

        // Assert
        assertFalse(result);
        verify(repository, times(1)).deleteById(999L);
    }

    @Test
    void testUpdateBalance() {
        // Arrange
        BigDecimal newBalance = BigDecimal.valueOf(10000.50);

        // Act
        service.updateBalance(1L, newBalance);

        // Assert
        verify(repository, times(1)).updateBalance(1L, newBalance);
    }

    @Test
    void testCreateAccount_WhitespaceCode() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("   ");
        account.setAccountNumber("123456789");
        account.setAccountingAccount("101-001");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(account));
    }

    @Test
    void testCreateAccount_WhitespaceAccountNumber() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK001");
        account.setAccountNumber("   ");
        account.setAccountingAccount("101-001");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(account));
    }

    @Test
    void testCreateAccount_WhitespaceAccountingAccount() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCode("BK001");
        account.setAccountNumber("123456789");
        account.setAccountingAccount("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(account));
    }

    @Test
    void testGetAllAccounts_EmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of());

        // Act
        List<BankAccount> result = service.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetActiveAccounts_EmptyList() {
        // Arrange
        when(repository.findActiveAccounts()).thenReturn(List.of());

        // Act
        List<BankAccount> result = service.getActiveAccounts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findActiveAccounts();
    }
}
