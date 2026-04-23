package com.econovafx.service;

import com.econovafx.domain.Account;
import com.econovafx.domain.AccountType;
import com.econovafx.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountService with manual stub mocks
 */
class AccountServiceTest {

    private StubAccountRepository repository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        repository = new StubAccountRepository();
        accountService = new AccountService(repository);
        repository.reset(); // Ensure clean state
    }

    @Test
    void testCreateAccountSuccess() {
        Account account = new Account("1001", "Cash", AccountType.ASSET);

        Account result = accountService.createAccount(account);

        assertNotNull(result);
        assertEquals("1001", result.getCode());
        assertEquals("Cash", result.getName());
        assertEquals(AccountType.ASSET, result.getType());
        assertTrue(repository.saveCalled);
    }

    @Test
    void testCreateAccountFailsWhenCodeExists() {
        Account existing = new Account("1001", "Existing", AccountType.ASSET);
        repository.save(existing);
        
        Account account = new Account("1001", "Cash", AccountType.ASSET);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(account)
        );

        assertEquals("Account code already exists: 1001", exception.getMessage());
    }

    @Test
    void testCreateAccountFailsWithNullCode() {
        Account account = new Account();
        account.setName("Test Account");
        account.setType(AccountType.ASSET);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(account)
        );

        assertEquals("Account code is required", exception.getMessage());
    }

    @Test
    void testCreateAccountFailsWithEmptyCode() {
        Account account = new Account();
        account.setCode("");
        account.setName("Test Account");
        account.setType(AccountType.ASSET);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(account)
        );

        assertEquals("Account code is required", exception.getMessage());
    }

    @Test
    void testCreateAccountFailsWithNullName() {
        Account account = new Account();
        account.setCode("1001");
        account.setType(AccountType.ASSET);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(account)
        );

        assertEquals("Account name is required", exception.getMessage());
    }

    @Test
    void testCreateAccountFailsWithNullType() {
        Account account = new Account();
        account.setCode("1001");
        account.setName("Test Account");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(account)
        );

        assertEquals("Account type is required", exception.getMessage());
    }

    @Test
    void testUpdateAccountSuccess() {
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        Account saved = accountService.createAccount(account);
        
        saved.setName("Updated Cash Account");
        Account result = accountService.updateAccount(saved);

        assertNotNull(result);
        assertTrue(repository.updateCalled);
    }

    @Test
    void testUpdateAccountFailsWhenNotFound() {
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        account.setId(999L);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.updateAccount(account)
        );

        assertEquals("Account not found with ID: 999", exception.getMessage());
    }

    @Test
    void testUpdateAccountFailsValidation() {
        Account account = new Account();
        account.setId(1L);
        account.setName("Test");
        account.setType(AccountType.ASSET);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.updateAccount(account)
        );

        assertEquals("Account code is required", exception.getMessage());
    }

    @Test
    void testDeleteAccountSuccess() {
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        Account saved = accountService.createAccount(account);
        
        accountService.deleteAccount(saved.getId());

        assertTrue(repository.deleteCalled);
    }

    @Test
    void testDeleteAccountFailsWhenHasChildAccounts() {
        Account parent = new Account("1000", "Assets", AccountType.ASSET);
        Account savedParent = accountService.createAccount(parent);
        
        Account child = new Account("1001", "Cash", AccountType.ASSET);
        child.setParentAccount(savedParent);
        savedParent.getChildAccounts().add(child);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.deleteAccount(savedParent.getId())
        );

        assertEquals("Cannot delete account with child accounts", exception.getMessage());
        assertFalse(repository.deleteCalled);
    }

    @Test
    void testDeleteAccountFailsWhenNotFound() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.deleteAccount(999L)
        );

        assertEquals("Account not found with ID: 999", exception.getMessage());
    }

    @Test
    void testGetAccountById() {
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        Account saved = accountService.createAccount(account);

        Optional<Account> result = accountService.getAccountById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("1001", result.get().getCode());
    }

    @Test
    void testGetAccountByCode() {
        Account account = new Account("1001", "Cash", AccountType.ASSET);
        accountService.createAccount(account);

        Optional<Account> result = accountService.getAccountByCode("1001");

        assertTrue(result.isPresent());
        assertEquals("Cash", result.get().getName());
    }

    @Test
    void testGetAllAccounts() {
        accountService.createAccount(new Account("1001", "Cash", AccountType.ASSET));
        accountService.createAccount(new Account("2001", "Payable", AccountType.LIABILITY));

        List<Account> result = accountService.getAllAccounts();

        assertEquals(2, result.size());
    }

    @Test
    void testGetAccountsByType() {
        accountService.createAccount(new Account("1001", "Cash", AccountType.ASSET));
        accountService.createAccount(new Account("1002", "Bank", AccountType.ASSET));
        accountService.createAccount(new Account("2001", "Payable", AccountType.LIABILITY));

        List<Account> result = accountService.getAccountsByType(AccountType.ASSET);

        assertEquals(2, result.size());
    }

    @Test
    void testGetRootAccounts() {
        Account root = new Account("1000", "Assets", AccountType.ASSET);
        Account savedRoot = accountService.createAccount(root);
        
        Account child = new Account("1001", "Cash", AccountType.ASSET);
        child.setParentAccount(savedRoot);
        accountService.createAccount(child);
        savedRoot.getChildAccounts().add(child);
        
        accountService.createAccount(new Account("2001", "Payable", AccountType.LIABILITY));

        List<Account> result = accountService.getRootAccounts();

        // Both "Assets" and "Payable" are root accounts (no parent set on them)
        assertEquals(2, result.size());
    }

    @Test
    void testGetChildAccounts() {
        Account parent = new Account("1000", "Assets", AccountType.ASSET);
        Account savedParent = accountService.createAccount(parent);
        
        Account child1 = new Account("1001", "Cash", AccountType.ASSET);
        child1.setParentAccount(savedParent);
        Account savedChild = accountService.createAccount(child1);
        
        // Reload to get the child with the proper parent reference
        List<Account> result = accountService.getChildAccounts(savedParent.getId());

        assertEquals(1, result.size());
        assertEquals(savedParent.getId(), result.get(0).getParentAccount().getId());
    }

    @Test
    void testSearchAccounts() {
        accountService.createAccount(new Account("1001", "Cash Account", AccountType.ASSET));
        accountService.createAccount(new Account("2001", "Bank Account", AccountType.ASSET));

        List<Account> result = accountService.searchAccounts("Cash");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("Cash"));
    }

    @Test
    void testGetAccountsCount() {
        accountService.createAccount(new Account("1001", "Cash", AccountType.ASSET));
        accountService.createAccount(new Account("2001", "Payable", AccountType.LIABILITY));

        long count = accountService.getAccountsCount();

        assertEquals(2, count);
    }

    @Test
    void testGetAccountsCountByType() {
        accountService.createAccount(new Account("1001", "Cash", AccountType.ASSET));
        accountService.createAccount(new Account("1002", "Bank", AccountType.ASSET));
        accountService.createAccount(new Account("2001", "Payable", AccountType.LIABILITY));

        long count = accountService.getAccountsCountByType(AccountType.ASSET);

        assertEquals(2, count);
    }

    /**
     * Manual stub implementation for AccountRepository
     */
    private static class StubAccountRepository extends AccountRepository {
        boolean saveCalled = false;
        boolean updateCalled = false;
        boolean deleteCalled = false;
        private List<Account> accounts = new ArrayList<>();
        private Long nextId = 1L;

        public StubAccountRepository() {
            super(null); // Database is null since we're not using it
        }

        public void reset() {
            accounts.clear();
            nextId = 1L;
            saveCalled = false;
            updateCalled = false;
            deleteCalled = false;
        }

        @Override
        public Optional<Account> findById(Long id) {
            return accounts.stream()
                .filter(a -> a.getId() != null && a.getId().equals(id))
                .findFirst();
        }

        @Override
        public Optional<Account> findByCode(String code) {
            return accounts.stream()
                .filter(a -> code.equals(a.getCode()))
                .findFirst();
        }

        @Override
        public List<Account> findAll() {
            return new ArrayList<>(accounts);
        }

        @Override
        public List<Account> findByType(AccountType type) {
            return accounts.stream()
                .filter(a -> type == a.getType())
                .toList();
        }

        @Override
        public List<Account> findByParentAccount(Long parentAccountId) {
            return accounts.stream()
                .filter(a -> a.getParentAccount() != null && 
                           a.getParentAccount().getId().equals(parentAccountId))
                .toList();
        }

        @Override
        public List<Account> findRootAccounts() {
            return accounts.stream()
                .filter(a -> a.getParentAccount() == null)
                .toList();
        }

        @Override
        public List<Account> searchByName(String searchTerm) {
            return accounts.stream()
                .filter(a -> a.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
        }

        @Override
        public Account save(Account account) {
            if (account.getId() == null) {
                account.setId(nextId++);
            }
            accounts.add(account);
            saveCalled = true;
            return account;
        }

        @Override
        public void update(Account account) {
            int index = -1;
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getId().equals(account.getId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                accounts.set(index, account);
            }
            updateCalled = true;
        }

        @Override
        public void delete(Account account) {
            accounts.removeIf(a -> a.getId().equals(account.getId()));
            deleteCalled = true;
        }

        @Override
        public boolean existsByCode(String code) {
            return accounts.stream().anyMatch(a -> code.equals(a.getCode()));
        }

        @Override
        public boolean existsById(Long id) {
            return accounts.stream().anyMatch(a -> a.getId() != null && a.getId().equals(id));
        }

        @Override
        public long count() {
            return accounts.size();
        }

        @Override
        public long countByType(AccountType type) {
            return accounts.stream().filter(a -> type == a.getType()).count();
        }
    }
}
