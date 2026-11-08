package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Genera los reportes contables obligatorios de la Resolución 340/2004:
 * Balance de Comprobación, Libro Mayor y Fichero Histórico de Comprobantes.
 *
 * <p>Todos los importes se calculan sobre comprobantes en estado POSTED, que es
 * lo que la norma considera movimiento definitivo.
 */
@Component
public class AccountingReportService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final LocalDate EPOCH = LocalDate.of(1900, 1, 1);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Inject
    public AccountingReportService(AccountRepository accountRepository,
                                   TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Balance de Comprobación a nivel de cuenta mayor.
     *
     * @param startDate fecha inicial del período
     * @param endDate   fecha final del período
     * @param includeSubAccounts cuando es {@code false} las subcuentas se
     *                           agregan en su cuenta mayor; cuando es {@code true}
     *                           se presentan como líneas independientes
     * @return una fila por cuenta con movimientos (débito, crédito y saldo)
     */
    public List<TrialBalanceRow> generateTrialBalance(LocalDate startDate, LocalDate endDate,
                                                      boolean includeSubAccounts) {
        Map<Long, BigDecimal[]> movements = movementsByAccount(startDate, endDate);
        List<Account> accounts = accountRepository.findAll();
        Map<Long, List<Account>> childrenByParent = childrenByParent(accounts);

        List<TrialBalanceRow> rows = new ArrayList<>();

        if (includeSubAccounts) {
            for (Account account : accounts) {
                BigDecimal[] move = movements.getOrDefault(account.getId(), new BigDecimal[]{ZERO, ZERO});
                if (isZero(move)) {
                    continue;
                }
                rows.add(toTrialBalanceRow(account, move));
            }
        } else {
            for (Account account : accounts) {
                if (account.getParentAccount() != null) {
                    continue; // rolled up into its major account
                }
                BigDecimal[] total = new BigDecimal[]{ZERO, ZERO};
                accumulate(account, movements, childrenByParent, total);
                if (isZero(total)) {
                    continue;
                }
                rows.add(toTrialBalanceRow(account, total));
            }
        }

        return rows;
    }

    /**
     * Libro Mayor de una cuenta: saldo inicial (movimientos anteriores al
     * período), movimientos del período con saldo corrido y saldo final.
     *
     * @param accountId cuenta a consultar
     * @param startDate fecha inicial del período
     * @param endDate   fecha final del período
     * @return el libro mayor de la cuenta
     */
    public GeneralLedger generateGeneralLedger(Long accountId, LocalDate startDate, LocalDate endDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal openingBalance = ZERO;
        for (Transaction transaction : transactionRepository.findPostedByDateRange(EPOCH, startDate.minusDays(1))) {
            for (TransactionEntry entry : transaction.getEntries()) {
                if (entry.getAccount().getId().equals(accountId)) {
                    openingBalance = openingBalance.add(
                            signedAmount(account.getType(), entry.getDebitAmount(), entry.getCreditAmount()));
                }
            }
        }

        List<LedgerLine> lines = new ArrayList<>();
        BigDecimal running = openingBalance;
        for (Transaction transaction : transactionRepository.findPostedByDateRange(startDate, endDate)) {
            for (TransactionEntry entry : transaction.getEntries()) {
                if (entry.getAccount().getId().equals(accountId)) {
                    running = running.add(
                            signedAmount(account.getType(), entry.getDebitAmount(), entry.getCreditAmount()));
                    lines.add(new LedgerLine(
                            transaction.getId(),
                            transaction.getNumber(),
                            transaction.getDate(),
                            entry.getDescription() != null ? entry.getDescription() : transaction.getDescription(),
                            entry.getDebitAmount(),
                            entry.getCreditAmount(),
                            running));
                }
            }
        }

        return new GeneralLedger(account.getId(), account.getCode(), account.getName(),
                openingBalance, lines, running);
    }

    /**
     * Fichero Histórico de Comprobantes del período.
     *
     * @param startDate         fecha inicial
     * @param endDate           fecha final
     * @param accountCodeFilter filtro opcional por código de cuenta (contiene)
     * @return comprobantes con sus partidas
     */
    public List<VoucherRecord> generateVoucherHistory(LocalDate startDate, LocalDate endDate,
                                                      String accountCodeFilter) {
        boolean filter = accountCodeFilter != null && !accountCodeFilter.isBlank();
        List<VoucherRecord> result = new ArrayList<>();

        for (Transaction transaction : transactionRepository.findPostedByDateRange(startDate, endDate)) {
            List<VoucherEntryLine> entries = new ArrayList<>();
            BigDecimal totalDebit = ZERO;
            BigDecimal totalCredit = ZERO;

            for (TransactionEntry entry : transaction.getEntries()) {
                Account entryAccount = entry.getAccount();
                if (filter && !entryAccount.getCode().contains(accountCodeFilter)) {
                    continue;
                }
                entries.add(new VoucherEntryLine(entryAccount.getCode(), entryAccount.getName(),
                        entry.getDebitAmount(), entry.getCreditAmount()));
                totalDebit = totalDebit.add(entry.getDebitAmount());
                totalCredit = totalCredit.add(entry.getCreditAmount());
            }

            if (filter && entries.isEmpty()) {
                continue;
            }

            result.add(new VoucherRecord(transaction.getId(), transaction.getNumber(), transaction.getDate(),
                    transaction.getDescription(), entries, totalDebit, totalCredit));
        }

        return result;
    }

    /**
     * Verifica que un comprobante cuadre (débito = crédito).
     *
     * @param transactionId comprobante a validar
     * @return {@code true} si el total de débitos iguala el total de créditos
     */
    public boolean validateVoucherBalance(Long transactionId) {
        BigDecimal debit = ZERO;
        BigDecimal credit = ZERO;
        for (TransactionEntry entry : transactionRepository.findEntriesByTransaction(transactionId)) {
            debit = debit.add(entry.getDebitAmount());
            credit = credit.add(entry.getCreditAmount());
        }
        return debit.compareTo(credit) == 0;
    }

    private Map<Long, BigDecimal[]> movementsByAccount(LocalDate startDate, LocalDate endDate) {
        Map<Long, BigDecimal[]> movements = new HashMap<>();
        for (Transaction transaction : transactionRepository.findPostedByDateRange(startDate, endDate)) {
            for (TransactionEntry entry : transaction.getEntries()) {
                BigDecimal[] move = movements.computeIfAbsent(entry.getAccount().getId(),
                        k -> new BigDecimal[]{ZERO, ZERO});
                move[0] = move[0].add(entry.getDebitAmount());
                move[1] = move[1].add(entry.getCreditAmount());
            }
        }
        return movements;
    }

    private Map<Long, List<Account>> childrenByParent(List<Account> accounts) {
        Map<Long, List<Account>> children = new HashMap<>();
        for (Account account : accounts) {
            if (account.getParentAccount() != null) {
                children.computeIfAbsent(account.getParentAccount().getId(), k -> new ArrayList<>()).add(account);
            }
        }
        return children;
    }

    private void accumulate(Account account, Map<Long, BigDecimal[]> movements,
                            Map<Long, List<Account>> childrenByParent, BigDecimal[] total) {
        BigDecimal[] own = movements.get(account.getId());
        if (own != null) {
            total[0] = total[0].add(own[0]);
            total[1] = total[1].add(own[1]);
        }
        for (Account child : childrenByParent.getOrDefault(account.getId(), List.of())) {
            accumulate(child, movements, childrenByParent, total);
        }
    }

    private TrialBalanceRow toTrialBalanceRow(Account account, BigDecimal[] move) {
        BigDecimal net = signedAmount(account.getType(), move[0], move[1]);
        return new TrialBalanceRow(account.getCode(), account.getName(), account.getType(),
                move[0], move[1], net);
    }

    private BigDecimal signedAmount(AccountType type, BigDecimal debit, BigDecimal credit) {
        return switch (type) {
            case ASSET, EXPENSE -> debit.subtract(credit);
            case LIABILITY, EQUITY, REVENUE -> credit.subtract(debit);
        };
    }

    private boolean isZero(BigDecimal[] move) {
        return move[0].compareTo(ZERO) == 0 && move[1].compareTo(ZERO) == 0;
    }

    /** Fila del Balance de Comprobación. */
    public record TrialBalanceRow(String accountCode, String accountName, AccountType accountType,
                                  BigDecimal totalDebit, BigDecimal totalCredit, BigDecimal netBalance) {
    }

    /** Línea del Libro Mayor. */
    public record LedgerLine(Long transactionId, String voucherNumber, LocalDate date, String description,
                             BigDecimal debit, BigDecimal credit, BigDecimal runningBalance) {
    }

    /** Libro Mayor de una cuenta. */
    public record GeneralLedger(Long accountId, String accountCode, String accountName,
                                BigDecimal openingBalance, List<LedgerLine> lines, BigDecimal closingBalance) {
    }

    /** Partida dentro de un comprobante. */
    public record VoucherEntryLine(String accountCode, String accountName,
                                   BigDecimal debit, BigDecimal credit) {
    }

    /** Comprobante del fichero histórico. */
    public record VoucherRecord(Long transactionId, String voucherNumber, LocalDate date, String description,
                                List<VoucherEntryLine> entries, BigDecimal totalDebit, BigDecimal totalCredit) {
    }
}
