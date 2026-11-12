package com.econovafx.modules.cash.controller;

import com.econovafx.modules.bank.model.BankAccount;
import com.econovafx.modules.bank.repository.BankAccountRepository;
import com.econovafx.modules.cash.model.CashBox;
import com.econovafx.modules.cash.repository.CashBoxRepository;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Regression tests for {@link CashModuleController} delete actions and the
 * audit username helper. Both used to lie to the user: the delete handlers
 * reported success without touching the database, and the movement handlers
 * recorded every action as the literal "system".
 */
class CashModuleControllerTest {

    private final BankAccountRepository bankAccountRepository = mock(BankAccountRepository.class);
    private final CashBoxRepository cashBoxRepository = mock(CashBoxRepository.class);

    private CashModuleController controller;

    @AfterEach
    void clearUserContext() {
        SecurityUtil.clearCurrentUser();
    }

    private CashModuleController controller() {
        if (controller == null) {
            controller = new CashModuleController(
                bankAccountRepository, cashBoxRepository, null, null, null, null, null);
        }
        return controller;
    }

    @Test
    void deleteBankAccountDelegatesToRepository() {
        BankAccount account = new BankAccount();
        account.setId(7L);
        when(bankAccountRepository.deleteById(7L)).thenReturn(true);

        assertTrue(controller().deleteBankAccount(account));
        verify(bankAccountRepository).deleteById(7L);
    }

    @Test
    void deleteBankAccountReportsFailureWhenNothingDeleted() {
        BankAccount account = new BankAccount();
        account.setId(7L);
        when(bankAccountRepository.deleteById(7L)).thenReturn(false);

        assertFalse(controller().deleteBankAccount(account));
    }

    @Test
    void deleteCashBoxDelegatesToRepository() {
        CashBox box = new CashBox();
        box.setId(3L);
        when(cashBoxRepository.deleteById(3L)).thenReturn(true);

        assertTrue(controller().deleteCashBox(box));
        verify(cashBoxRepository).deleteById(3L);
    }

    @Test
    void deleteCashBoxReportsFailureWhenNothingDeleted() {
        CashBox box = new CashBox();
        box.setId(3L);
        when(cashBoxRepository.deleteById(3L)).thenReturn(false);

        assertFalse(controller().deleteCashBox(box));
    }

    @Test
    void currentUsernameReturnsAuthenticatedUser() {
        User user = new User();
        user.setUsername("ana.lopez");
        SecurityUtil.setCurrentUser(user);

        assertEquals("ana.lopez", controller().currentUsername());
    }

    @Test
    void currentUsernameFallsBackToSystemWhenNoUser() {
        assertEquals("system", controller().currentUsername());
    }

    @Test
    void currentUsernameFallsBackToSystemWhenUsernameIsBlank() {
        User user = new User();
        user.setUsername("");
        SecurityUtil.setCurrentUser(user);

        assertEquals("system", controller().currentUsername());
    }
}
