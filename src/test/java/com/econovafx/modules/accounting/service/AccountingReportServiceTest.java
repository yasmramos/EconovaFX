package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.model.TransactionStatus;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import io.ebean.Database;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AccountingReportService against a real H2 database.
 * Verifies the Resolution 340/2004 reports (Trial Balance, General Ledger,
 * Voucher History) compute from posted transactions and real account codes.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountingReportServiceTest {

    private Database db;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private AccountingReportService service;

    private Account cash;
    private Account bank;
    private Account revenue;

    @BeforeAll
    void setUpAll() {
        db = io.ebean.DB.getDefault();
        accountRepository = new AccountRepository(db);
        transactionRepository = new TransactionRepository(db);
        service = new AccountingReportService(accountRepository, transactionRepository);
    }

    @BeforeEach
    void setUp() {
        try {
            db.sqlUpdate("SET REFERENTIAL_INTEGRITY FALSE").execute();
        } catch (Exception ignored) {
        }
        try { db.sqlUpdate("DELETE FROM transaction_entries").execute(); } catch (Exception ignored) {}
        try { db.sqlUpdate("DELETE FROM transactions").execute(); } catch (Exception ignored) {}
        try { db.sqlUpdate("DELETE FROM accounts").execute(); } catch (Exception ignored) {}
        try {
            db.sqlUpdate("SET REFERENTIAL_INTEGRITY TRUE").execute();
        } catch (Exception ignored) {
        }

        cash = saveAccount("1100", "Caja", AccountType.ASSET, null);
        bank = saveAccount("1200", "Banco", AccountType.ASSET, null);
        revenue = saveAccount("4000", "Ingresos", AccountType.REVENUE, null);
    }

    @AfterEach
    void tearDown() {
        try {
            db.sqlUpdate("SET REFERENTIAL_INTEGRITY FALSE").execute();
        } catch (Exception ignored) {
        }
        try { db.sqlUpdate("DELETE FROM transaction_entries").execute(); } catch (Exception ignored) {}
        try { db.sqlUpdate("DELETE FROM transactions").execute(); } catch (Exception ignored) {}
        try { db.sqlUpdate("DELETE FROM accounts").execute(); } catch (Exception ignored) {}
        try {
            db.sqlUpdate("SET REFERENTIAL_INTEGRITY TRUE").execute();
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("Trial balance aggregates debit and credit per account from posted transactions")
    void trialBalanceAggregatesPostedMovements() {
        post("TXN-2026-000001", LocalDate.of(2026, 3, 1), cash, BigDecimal.valueOf(1000), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(1000));
        post("TXN-2026-000002", LocalDate.of(2026, 3, 5), cash, BigDecimal.valueOf(500), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(500));

        List<AccountingReportService.TrialBalanceRow> rows = service.generateTrialBalance(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), false);

        AccountingReportService.TrialBalanceRow cashRow = rows.stream()
                .filter(r -> r.accountCode().equals("1100")).findFirst().orElseThrow();
        assertEquals(0, cashRow.totalDebit().compareTo(BigDecimal.valueOf(1500)));
        assertEquals(0, cashRow.netBalance().compareTo(BigDecimal.valueOf(1500)));

        AccountingReportService.TrialBalanceRow revenueRow = rows.stream()
                .filter(r -> r.accountCode().equals("4000")).findFirst().orElseThrow();
        assertEquals(0, revenueRow.totalCredit().compareTo(BigDecimal.valueOf(1500)));
        assertEquals(0, revenueRow.netBalance().compareTo(BigDecimal.valueOf(1500)));
    }

    @Test
    @DisplayName("Trial balance ignores draft transactions")
    void trialBalanceIgnoresDraftTransactions() {
        post("TXN-2026-000003", LocalDate.of(2026, 3, 2), cash, BigDecimal.valueOf(700), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(700));
        saveDraft("TXN-2026-000004", LocalDate.of(2026, 3, 3), cash, BigDecimal.valueOf(9999), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(9999));

        List<AccountingReportService.TrialBalanceRow> rows = service.generateTrialBalance(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), false);

        AccountingReportService.TrialBalanceRow cashRow = rows.stream()
                .filter(r -> r.accountCode().equals("1100")).findFirst().orElseThrow();
        assertEquals(0, cashRow.totalDebit().compareTo(BigDecimal.valueOf(700)));
    }

    @Test
    @DisplayName("Trial balance rolls child accounts into the parent when sub-accounts are excluded")
    void trialBalanceRollsUpSubAccounts() {
        Account assets = saveAccount("1000", "Activos", AccountType.ASSET, null);
        Account cashChild = saveAccount("1101", "Caja Chica", AccountType.ASSET, assets);

        post("TXN-2026-000005", LocalDate.of(2026, 3, 4), cashChild, BigDecimal.valueOf(250), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(250));

        List<AccountingReportService.TrialBalanceRow> rolledUp = service.generateTrialBalance(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), false);
        assertTrue(rolledUp.stream().anyMatch(r -> r.accountCode().equals("1000")
                && r.totalDebit().compareTo(BigDecimal.valueOf(250)) == 0));
        assertTrue(rolledUp.stream().noneMatch(r -> r.accountCode().equals("1101")));

        List<AccountingReportService.TrialBalanceRow> detailed = service.generateTrialBalance(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), true);
        assertTrue(detailed.stream().anyMatch(r -> r.accountCode().equals("1101")
                && r.totalDebit().compareTo(BigDecimal.valueOf(250)) == 0));
    }

    @Test
    @DisplayName("General ledger carries opening balance and running balance")
    void generalLedgerOpeningAndRunningBalance() {
        post("TXN-2026-000006", LocalDate.of(2026, 1, 15), cash, BigDecimal.valueOf(300), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(300));
        post("TXN-2026-000007", LocalDate.of(2026, 3, 10), cash, BigDecimal.valueOf(200), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(200));
        post("TXN-2026-000008", LocalDate.of(2026, 3, 20), revenue, BigDecimal.valueOf(50), BigDecimal.ZERO,
                cash, BigDecimal.ZERO, BigDecimal.valueOf(50));

        AccountingReportService.GeneralLedger ledger = service.generateGeneralLedger(
                cash.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertEquals(0, ledger.openingBalance().compareTo(BigDecimal.valueOf(300)));
        assertEquals(2, ledger.lines().size());
        assertEquals(0, ledger.lines().get(0).runningBalance().compareTo(BigDecimal.valueOf(500)));
        assertEquals(0, ledger.lines().get(1).runningBalance().compareTo(BigDecimal.valueOf(450)));
        assertEquals(0, ledger.closingBalance().compareTo(BigDecimal.valueOf(450)));
    }

    @Test
    @DisplayName("Voucher history groups entries by transaction and filters by account code")
    void voucherHistoryGroupsAndFilters() {
        post("TXN-2026-000009", LocalDate.of(2026, 3, 6), cash, BigDecimal.valueOf(400), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(400));
        post("TXN-2026-000010", LocalDate.of(2026, 3, 7), bank, BigDecimal.valueOf(100), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(100));

        List<AccountingReportService.VoucherRecord> all = service.generateVoucherHistory(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), null);
        assertEquals(2, all.size());
        AccountingReportService.VoucherRecord first = all.get(0);
        assertEquals(2, first.entries().size());
        assertEquals(0, first.totalDebit().compareTo(BigDecimal.valueOf(400)));
        assertEquals(0, first.totalCredit().compareTo(BigDecimal.valueOf(400)));

        List<AccountingReportService.VoucherRecord> filtered = service.generateVoucherHistory(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), "1200");
        assertEquals(1, filtered.size());
        assertEquals("TXN-2026-000010", filtered.get(0).voucherNumber());
    }

    @Test
    @DisplayName("Voucher balance validation detects balanced and unbalanced transactions")
    void validateVoucherBalance() {
        Transaction balanced = post("TXN-2026-000011", LocalDate.of(2026, 3, 8),
                cash, BigDecimal.valueOf(100), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(100));
        assertTrue(service.validateVoucherBalance(balanced.getId()));

        Transaction unbalanced = saveDraft("TXN-2026-000012", LocalDate.of(2026, 3, 9),
                cash, BigDecimal.valueOf(100), BigDecimal.ZERO,
                revenue, BigDecimal.ZERO, BigDecimal.valueOf(90));
        assertFalse(service.validateVoucherBalance(unbalanced.getId()));
    }

    private Account saveAccount(String code, String name, AccountType type, Account parent) {
        Account account = new Account(code, name, type);
        account.setBalance(BigDecimal.ZERO);
        account.setParentAccount(parent);
        accountRepository.save(account);
        return account;
    }

    private Transaction post(String number, LocalDate date,
                             Account debitAccount, BigDecimal debitAmount, BigDecimal debitCredit,
                             Account creditAccount, BigDecimal creditDebit, BigDecimal creditAmount) {
        Transaction transaction = saveDraft(number, date, debitAccount, debitAmount, debitCredit,
                creditAccount, creditDebit, creditAmount);
        transaction.setStatus(TransactionStatus.POSTED);
        transactionRepository.update(transaction);
        return transaction;
    }

    private Transaction saveDraft(String number, LocalDate date,
                                  Account debitAccount, BigDecimal debitAmount, BigDecimal debitCredit,
                                  Account creditAccount, BigDecimal creditDebit, BigDecimal creditAmount) {
        Transaction transaction = new Transaction();
        transaction.setNumber(number);
        transaction.setDate(date);
        transaction.setType("INCOME");
        transaction.setDescription("Test " + number);
        transaction.setReference("REF-" + number);
        transaction.setStatus(TransactionStatus.DRAFT);
        transaction.setTotalDebit(debitAmount);
        transaction.setTotalCredit(creditAmount);
        transaction.addEntry(new TransactionEntry(transaction, debitAccount, debitAmount, debitCredit));
        transaction.addEntry(new TransactionEntry(transaction, creditAccount, creditDebit, creditAmount));
        transactionRepository.save(transaction);
        return transaction;
    }
}
