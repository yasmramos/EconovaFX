package com.econovafx.modules.bank.repository;

import com.econovafx.modules.bank.model.BankAccount;
import io.ebean.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link BankAccountRepository#deleteById(Long)} against a
 * real H2 database. The delete button in the cash module used to report success
 * without ever reaching the database; this pins the repository behaviour that
 * the fixed handler now relies on.
 */
class BankAccountRepositoryDeleteTest {

    private Database db;
    private BankAccountRepository repository;

    @BeforeEach
    void setUp() {
        db = io.ebean.DB.getDefault();
        repository = new BankAccountRepository(db);
        try {
            db.sqlUpdate("DELETE FROM bank_account").execute();
        } catch (Exception ignored) {
            // Table may be empty on first run.
        }
    }

    private BankAccount newAccount(String code) {
        BankAccount account = new BankAccount();
        account.setCode(code);
        account.setDescription("Cuenta " + code);
        account.setAccountNumber("ES00-" + code);
        account.setBankEntity("Banco Test");
        account.setCurrency("EUR");
        account.setBalance(new BigDecimal("100.00"));
        return account;
    }

    @Test
    void deleteByIdRemovesRowAndReturnsTrue() {
        BankAccount saved = repository.save(newAccount("ACC-DEL-1"));
        Long id = saved.getId();
        assertNotNull(id);
        assertTrue(repository.findById(id).isPresent());

        assertTrue(repository.deleteById(id));
        assertFalse(repository.findById(id).isPresent());
    }

    @Test
    void deleteByIdReturnsFalseForUnknownId() {
        assertFalse(repository.deleteById(999999L));
    }
}
