package com.econovafx.modules.reporting.controller;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.service.AccountingReportService;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.reporting.model.FinancialReport;
import com.econovafx.modules.reporting.model.ReportLine;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiFunction;

/**
 * View controller for financial statements and the accounting reports required
 * by Resolution 340/2004: Balance Sheet, Income Statement, Cash Flow, Statement
 * of Changes in Equity, Trial Balance, General Ledger and Voucher History.
 */
public class FinancialReportingViewController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(FinancialReportingViewController.class);

    private final FinancialReportingController reportingController;
    private final AccountingReportService accountingReportService;
    private final AccountService accountService;

    @FXML private ComboBox<ReportKind> reportTypeCombo;
    @FXML private DatePicker statementStartDate;
    @FXML private DatePicker statementEndDate;
    @FXML private Spinner<Integer> fiscalYearSpinner;
    @FXML private Button btnGenerateStatement;
    @FXML private Button btnFinalizeStatement;
    @FXML private Label statementStatusLabel;
    @FXML private TableView<ReportLine> statementTable;
    @FXML private TableColumn<ReportLine, String> colStmtCode;
    @FXML private TableColumn<ReportLine, String> colStmtDescription;
    @FXML private TableColumn<ReportLine, String> colStmtDebit;
    @FXML private TableColumn<ReportLine, String> colStmtCredit;
    @FXML private TableColumn<ReportLine, String> colStmtBalance;

    @FXML private DatePicker trialStartDate;
    @FXML private DatePicker trialEndDate;
    @FXML private CheckBox includeSubAccountsCheck;
    @FXML private Button btnGenerateTrialBalance;
    @FXML private Label trialStatusLabel;
    @FXML private TableView<AccountingReportService.TrialBalanceRow> trialBalanceTable;
    @FXML private TableColumn<AccountingReportService.TrialBalanceRow, String> colTbCode;
    @FXML private TableColumn<AccountingReportService.TrialBalanceRow, String> colTbName;
    @FXML private TableColumn<AccountingReportService.TrialBalanceRow, String> colTbType;
    @FXML private TableColumn<AccountingReportService.TrialBalanceRow, String> colTbDebit;
    @FXML private TableColumn<AccountingReportService.TrialBalanceRow, String> colTbCredit;
    @FXML private TableColumn<AccountingReportService.TrialBalanceRow, String> colTbBalance;

    @FXML private ComboBox<Account> ledgerAccountCombo;
    @FXML private DatePicker ledgerStartDate;
    @FXML private DatePicker ledgerEndDate;
    @FXML private Button btnGenerateLedger;
    @FXML private Label ledgerOpeningLabel;
    @FXML private Label ledgerClosingLabel;
    @FXML private TableView<AccountingReportService.LedgerLine> ledgerTable;
    @FXML private TableColumn<AccountingReportService.LedgerLine, String> colLedgerDate;
    @FXML private TableColumn<AccountingReportService.LedgerLine, String> colLedgerVoucher;
    @FXML private TableColumn<AccountingReportService.LedgerLine, String> colLedgerDescription;
    @FXML private TableColumn<AccountingReportService.LedgerLine, String> colLedgerDebit;
    @FXML private TableColumn<AccountingReportService.LedgerLine, String> colLedgerCredit;
    @FXML private TableColumn<AccountingReportService.LedgerLine, String> colLedgerRunning;

    @FXML private DatePicker historyStartDate;
    @FXML private DatePicker historyEndDate;
    @FXML private TextField historyAccountFilter;
    @FXML private Button btnGenerateHistory;
    @FXML private Label historyStatusLabel;
    @FXML private TableView<VoucherHistoryRow> voucherHistoryTable;
    @FXML private TableColumn<VoucherHistoryRow, String> colHistVoucher;
    @FXML private TableColumn<VoucherHistoryRow, String> colHistDate;
    @FXML private TableColumn<VoucherHistoryRow, String> colHistAccount;
    @FXML private TableColumn<VoucherHistoryRow, String> colHistDescription;
    @FXML private TableColumn<VoucherHistoryRow, String> colHistDebit;
    @FXML private TableColumn<VoucherHistoryRow, String> colHistCredit;

    private Long lastStatementReportId;

    public FinancialReportingViewController(FinancialReportingController reportingController,
                                            AccountingReportService accountingReportService,
                                            AccountService accountService) {
        this.reportingController = reportingController;
        this.accountingReportService = accountingReportService;
        this.accountService = accountService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("FinancialReportingViewController initialized");
        setupDefaults();
        setupStatementTable();
        setupTrialBalanceTable();
        setupLedgerTable();
        setupHistoryTable();
        loadAccounts();
    }

    private void setupDefaults() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(ReportKind.values()));
        reportTypeCombo.getSelectionModel().selectFirst();

        LocalDate firstOfYear = LocalDate.now().withDayOfMonth(1).withMonth(1);
        statementStartDate.setValue(firstOfYear);
        statementEndDate.setValue(LocalDate.now());
        trialStartDate.setValue(firstOfYear);
        trialEndDate.setValue(LocalDate.now());
        ledgerStartDate.setValue(firstOfYear);
        ledgerEndDate.setValue(LocalDate.now());
        historyStartDate.setValue(firstOfYear);
        historyEndDate.setValue(LocalDate.now());

        fiscalYearSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, 2100, LocalDate.now().getYear()));
    }

    private void setupStatementTable() {
        colStmtCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccountCode()));
        colStmtDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colStmtDebit.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().getDebitAmount())));
        colStmtCredit.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().getCreditAmount())));
        colStmtBalance.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().getBalance())));
    }

    private void setupTrialBalanceTable() {
        colTbCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().accountCode()));
        colTbName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().accountName()));
        colTbType.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().accountType() != null ? d.getValue().accountType().getDisplayName() : ""));
        colTbDebit.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().totalDebit())));
        colTbCredit.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().totalCredit())));
        colTbBalance.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().netBalance())));
    }

    private void setupLedgerTable() {
        colLedgerDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().date())));
        colLedgerVoucher.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().voucherNumber()));
        colLedgerDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().description()));
        colLedgerDebit.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().debit())));
        colLedgerCredit.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().credit())));
        colLedgerRunning.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().runningBalance())));
    }

    private void setupHistoryTable() {
        colHistVoucher.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().voucherNumber()));
        colHistDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().date()));
        colHistAccount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().account()));
        colHistDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().description()));
        colHistDebit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().debit()));
        colHistCredit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().credit()));
    }

    private void loadAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        ledgerAccountCombo.setItems(FXCollections.observableArrayList(accounts));
        ledgerAccountCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Account account) {
                return account == null ? "" : account.getCode() + " - " + account.getName();
            }

            @Override
            public Account fromString(String string) {
                return null;
            }
        });
        if (!accounts.isEmpty()) {
            ledgerAccountCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onGenerateStatement() {
        try {
            ReportKind kind = reportTypeCombo.getValue();
            LocalDate start = statementStartDate.getValue();
            LocalDate end = statementEndDate.getValue();
            Integer year = fiscalYearSpinner.getValue();

            if (kind == null || start == null || end == null) {
                setStatus(statementStatusLabel, "Complete el tipo y el rango de fechas.", true);
                return;
            }

            FinancialReport report = kind.generate.apply(reportingController, new StatementRequest(start, end, year));
            lastStatementReportId = report.getId();
            statementTable.setItems(FXCollections.observableArrayList(report.getLines()));
            btnFinalizeStatement.setDisable(false);
            setStatus(statementStatusLabel, kind.label + " generado (informe #" + report.getId() + ").", false);
        } catch (RuntimeException e) {
            logger.error("Error generating financial statement", e);
            setStatus(statementStatusLabel, "Error al generar: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onFinalizeStatement() {
        if (lastStatementReportId == null) {
            setStatus(statementStatusLabel, "Genere un informe antes de finalizarlo.", true);
            return;
        }
        try {
            reportingController.finalizeReport(lastStatementReportId);
            btnFinalizeStatement.setDisable(true);
            setStatus(statementStatusLabel, "Informe #" + lastStatementReportId + " finalizado.", false);
        } catch (RuntimeException e) {
            logger.error("Error finalizing financial statement", e);
            setStatus(statementStatusLabel, "Error al finalizar: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onGenerateTrialBalance() {
        try {
            LocalDate start = trialStartDate.getValue();
            LocalDate end = trialEndDate.getValue();
            if (start == null || end == null) {
                setStatus(trialStatusLabel, "Complete el rango de fechas.", true);
                return;
            }
            boolean includeSubAccounts = includeSubAccountsCheck.isSelected();
            List<AccountingReportService.TrialBalanceRow> rows =
                    accountingReportService.generateTrialBalance(start, end, includeSubAccounts);
            trialBalanceTable.setItems(FXCollections.observableArrayList(rows));
            setStatus(trialStatusLabel, rows.size() + " cuentas con movimientos.", false);
        } catch (RuntimeException e) {
            logger.error("Error generating trial balance", e);
            setStatus(trialStatusLabel, "Error al generar: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onGenerateLedger() {
        try {
            Account account = ledgerAccountCombo.getValue();
            LocalDate start = ledgerStartDate.getValue();
            LocalDate end = ledgerEndDate.getValue();
            if (account == null || start == null || end == null) {
                ledgerOpeningLabel.setText("Seleccione una cuenta y un rango de fechas.");
                return;
            }
            AccountingReportService.GeneralLedger ledger =
                    accountingReportService.generateGeneralLedger(account.getId(), start, end);
            ledgerTable.setItems(FXCollections.observableArrayList(ledger.lines()));
            ledgerOpeningLabel.setText("Saldo inicial: " + format(ledger.openingBalance()));
            ledgerClosingLabel.setText("Saldo final: " + format(ledger.closingBalance()));
        } catch (RuntimeException e) {
            logger.error("Error generating general ledger", e);
            ledgerOpeningLabel.setText("Error al generar: " + e.getMessage());
        }
    }

    @FXML
    private void onGenerateVoucherHistory() {
        try {
            LocalDate start = historyStartDate.getValue();
            LocalDate end = historyEndDate.getValue();
            if (start == null || end == null) {
                setStatus(historyStatusLabel, "Complete el rango de fechas.", true);
                return;
            }
            List<AccountingReportService.VoucherRecord> records =
                    accountingReportService.generateVoucherHistory(start, end, historyAccountFilter.getText());
            voucherHistoryTable.setItems(FXCollections.observableArrayList(flatten(records)));
            setStatus(historyStatusLabel, records.size() + " comprobantes en el período.", false);
        } catch (RuntimeException e) {
            logger.error("Error generating voucher history", e);
            setStatus(historyStatusLabel, "Error al generar: " + e.getMessage(), true);
        }
    }

    private List<VoucherHistoryRow> flatten(List<AccountingReportService.VoucherRecord> records) {
        return records.stream()
                .flatMap(record -> record.entries().stream()
                        .map(entry -> new VoucherHistoryRow(
                                record.voucherNumber(),
                                String.valueOf(record.date()),
                                entry.accountCode() + " - " + entry.accountName(),
                                record.description(),
                                format(entry.debit()),
                                format(entry.credit()))))
                .toList();
    }

    private void setStatus(Label label, String message, boolean error) {
        label.setText(message);
        label.setStyle(error ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #16a34a;");
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Flat row model for the voucher history table. */
    public record VoucherHistoryRow(String voucherNumber, String date, String account,
                                    String description, String debit, String credit) {
    }

    private record StatementRequest(LocalDate start, LocalDate end, Integer year) {
    }

    /**
     * Report types offered by the statement generator, each paired with the
     * call that produces it.
     */
    private enum ReportKind {
        BALANCE_SHEET("Balance de Situación",
                (controller, req) -> controller.generateBalanceSheet(req.start(), req.end(), req.year())),
        INCOME_STATEMENT("Estado de Resultados",
                (controller, req) -> controller.generateIncomeStatement(req.start(), req.end(), req.year())),
        CASH_FLOW("Estado de Flujo de Efectivo",
                (controller, req) -> controller.generateCashFlowStatement(req.start(), req.end(), req.year())),
        CHANGES_IN_EQUITY("Estado de Cambios en el Patrimonio",
                (controller, req) -> controller.generateChangesInEquityStatement(req.start(), req.end(), req.year())),
        TRIAL_BALANCE("Balance de Comprobación",
                (controller, req) -> controller.generateTrialBalance(req.end(), req.year()));

        private final String label;
        private final BiFunction<FinancialReportingController, StatementRequest, FinancialReport> generate;

        ReportKind(String label,
                   BiFunction<FinancialReportingController, StatementRequest, FinancialReport> generate) {
            this.label = label;
            this.generate = generate;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
