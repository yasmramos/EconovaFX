package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
    }

    @Test
    void testGetAccountById() {
        Account account = createTestAccount();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccountById(1L);

        assertTrue(result.isPresent());
        assertEquals("1001", result.get().getCode());
        verify(accountRepository).findById(1L);
    }

    @Test
    void testGetAccountByIdNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.getAccountById(999L);

        assertFalse(result.isPresent());
        verify(accountRepository).findById(999L);
    }

    @Test
    void testGetAccountByCode() {
        Account account = createTestAccount();
        when(accountRepository.findByCode("1001")).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccountByCode("1001");

        assertTrue(result.isPresent());
        assertEquals("1001", result.get().getCode());
        verify(accountRepository).findByCode("1001");
    }

    @Test
    void testGetAllAccounts() {
        List<Account> accounts = Arrays.asList(createTestAccount(), createTestAccount2());
        when(accountRepository.findAll()).thenReturn(accounts);

        List<Account> result = accountService.getAllAccounts();

        assertEquals(2, result.size());
        verify(accountRepository).findAll();
    }

    @Test
    void testGetAccountsByType() {
        List<Account> accounts = Arrays.asList(createTestAccount());
        when(accountRepository.findByType(AccountType.ASSET)).thenReturn(accounts);

        List<Account> result = accountService.getAccountsByType(AccountType.ASSET);

        assertEquals(1, result.size());
        verify(accountRepository).findByType(AccountType.ASSET);
    }

    @Test
    void testGetRootAccounts() {
        List<Account> accounts = Arrays.asList(createTestAccount());
        when(accountRepository.findRootAccounts()).thenReturn(accounts);

        List<Account> result = accountService.getRootAccounts();

        assertEquals(1, result.size());
        verify(accountRepository).findRootAccounts();
    }

    @Test
    void testGetChildAccounts() {
        List<Account> accounts = Arrays.asList(createTestAccount2());
        when(accountRepository.findByParentAccount(1L)).thenReturn(accounts);

        List<Account> result = accountService.getChildAccounts(1L);

        assertEquals(1, result.size());
        verify(accountRepository).findByParentAccount(1L);
    }

    @Test
    void testSearchAccounts() {
        List<Account> accounts = Arrays.asList(createTestAccount());
        when(accountRepository.searchByName("Cash")).thenReturn(accounts);

        List<Account> result = accountService.searchAccounts("Cash");

        assertEquals(1, result.size());
        verify(accountRepository).searchByName("Cash");
    }

    @Test
    void testCreateAccount() {
        Account account = createTestAccount();
        when(accountRepository.existsByCode("1001")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(account);

        assertNotNull(result);
        assertEquals("1001", result.getCode());
        verify(accountRepository).save(any(Account.class));
        verify(accountRepository, never()).update(any(Account.class));
    }

    @Test
    void testCreateAccountDuplicateCode() {
        Account account = createTestAccount();
        when(accountRepository.existsByCode("1001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccountNullCode() {
        Account account = createTestAccount();
        account.setCode(null);

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccountEmptyCode() {
        Account account = createTestAccount();
        account.setCode("  ");

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccountNullName() {
        Account account = createTestAccount();
        account.setName(null);

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccountNullType() {
        Account account = createTestAccount();
        account.setType(null);

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testUpdateAccount() {
        Account account = createTestAccount();
        account.setId(1L);
        when(accountRepository.existsById(1L)).thenReturn(true);
        doNothing().when(accountRepository).update(account);

        Account result = accountService.updateAccount(account);

        assertNotNull(result);
        verify(accountRepository).update(account);
    }

    @Test
    void testUpdateAccountNotFound() {
        Account account = createTestAccount();
        account.setId(999L);
        when(accountRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> accountService.updateAccount(account));
        verify(accountRepository, never()).update(any(Account.class));
    }

    @Test
    void testDeleteAccount() {
        Account account = createTestAccount();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        doNothing().when(accountRepository).delete(account);

        accountService.deleteAccount(1L);

        verify(accountRepository).delete(account);
    }

    @Test
    void testDeleteAccountWithChildren() {
        Account account = createTestAccount();
        account.setId(1L);
        Account child = createTestAccount2();
        List<Account> children = new ArrayList<>();
        children.add(child);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        // Don't mock getChildAccounts, just set the list directly
        account.getChildAccounts().clear();
        account.getChildAccounts().addAll(children);

        assertThrows(IllegalArgumentException.class, () -> accountService.deleteAccount(1L));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    void testDeleteAccountNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.deleteAccount(999L));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    void testGetAccountsCount() {
        when(accountRepository.count()).thenReturn(42L);

        long result = accountService.getAccountsCount();

        assertEquals(42L, result);
        verify(accountRepository).count();
    }

    @Test
    void testGetAccountsCountByType() {
        when(accountRepository.countByType(AccountType.ASSET)).thenReturn(15L);

        long result = accountService.getAccountsCountByType(AccountType.ASSET);

        assertEquals(15L, result);
        verify(accountRepository).countByType(AccountType.ASSET);
    }

    private Account createTestAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setCode("1001");
        account.setName("Cash");
        account.setType(AccountType.ASSET);
        return account;
    }

    private Account createTestAccount2() {
        Account account = new Account();
        account.setId(2L);
        account.setCode("1002");
        account.setName("Bank");
        account.setType(AccountType.ASSET);
        return account;
    }
}
