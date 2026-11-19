package com.econovafx.modules.accounting.controller;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.accounting.service.AccountingReportService;
import com.econovafx.modules.core.ui.util.ModernDialog;
import com.econovafx.modules.core.ui.view.ViewFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Accounts controller
 */
public class AccountsController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);

    private final AccountService accountService;
    private final AccountingReportService accountingReportService;
    private ViewFactory viewFactory;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<AccountType> accountTypeList;

    @FXML
    private TableView<Account> accountsTable;

    @FXML
    private TableColumn<Account, String> colCode;

    @FXML
    private TableColumn<Account, String> colName;

    @FXML
    private TableColumn<Account, AccountType> colType;

    @FXML
    private TableColumn<Account, BigDecimal> colBalance;

    @FXML
    private TableColumn<Account, Account> colParent;

    @FXML
    private TableColumn<Account, String> colDescription;

    public AccountsController(AccountService accountService, AccountingReportService accountingReportService) {
        this.accountService = accountService;
        this.accountingReportService = accountingReportService;
    }

    /**
     * Initialize ViewFactory reference (two-phase initialization pattern)
     */
    public void initializeViewFactory(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("AccountsController initialized");
        initializeTableColumns();
        initializeAccountTypeList();
        loadAccounts();
    }

    private void initializeTableColumns() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colParent.setCellValueFactory(new PropertyValueFactory<>("parentAccount"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(AccountType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        colParent.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    private void initializeAccountTypeList() {
        ObservableList<AccountType> accountTypes = FXCollections.observableArrayList(AccountType.values());
        accountTypeList.setItems(accountTypes);

        accountTypeList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterByType(newVal);
            }
        });
    }

    private void loadAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        ObservableList<Account> observableAccounts = FXCollections.observableArrayList(accounts);
        accountsTable.setItems(observableAccounts);
    }

    private void filterByType(AccountType type) {
        List<Account> accounts = accountService.getAccountsByType(type);
        ObservableList<Account> observableAccounts = FXCollections.observableArrayList(accounts);
        accountsTable.setItems(observableAccounts);
    }

    @FXML
    private void searchAccounts() {
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<Account> accounts = accountService.searchAccounts(searchTerm.trim());
            ObservableList<Account> observableAccounts = FXCollections.observableArrayList(accounts);
            accountsTable.setItems(observableAccounts);
        } else {
            loadAccounts();
        }
    }

    @FXML
    private void newAccount() {
        logger.debug("New account clicked");
        Optional<Account> result = viewFactory.showAccountFormDialog(null);
        result.ifPresent(account -> {
            accountService.createAccount(account);
            loadAccounts();
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Cuenta creada correctamente");
        });
    }

    @FXML
    private void editAccount() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor seleccione una cuenta para editar");
            return;
        }

        logger.debug("Edit account: {}", selected.getCode());
        Optional<Account> result = viewFactory.showAccountFormDialog(selected);
        result.ifPresent(account -> {
            accountService.updateAccount(account);
            loadAccounts();
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Cuenta actualizada correctamente");
        });
    }

    @FXML
    private void deleteAccount() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor seleccione una cuenta para eliminar");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta cuenta?");
        alert.setContentText("Cuenta: " + selected.getCode() + " - " + selected.getName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                accountService.deleteAccount(selected.getId());
                loadAccounts();
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Cuenta eliminada correctamente");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    @FXML
    private void viewTransactions() {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor seleccione una cuenta para ver movimientos");
            return;
        }

        logger.debug("View transactions for account: {}", selected.getCode());
        showAccountLedger(selected);
    }

    /**
     * Shows the general ledger of an account (its movements) in a modal dialog,
     * with a date range the user can widen or narrow.
     */
    private void showAccountLedger(Account account) {
        LocalDate today = LocalDate.now();
        DatePicker startPicker = new DatePicker(today.withDayOfMonth(1).minusMonths(11));
        DatePicker endPicker = new DatePicker(today);

        TableView<AccountingReportService.LedgerLine> ledgerTable = new TableView<>();
        ledgerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ledgerTable.setPrefHeight(360);

        TableColumn<AccountingReportService.LedgerLine, String> colDate = new TableColumn<>("Fecha");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().date())));
        TableColumn<AccountingReportService.LedgerLine, String> colVoucher = new TableColumn<>("Comprobante");
        colVoucher.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().voucherNumber()));
        TableColumn<AccountingReportService.LedgerLine, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().description()));
        TableColumn<AccountingReportService.LedgerLine, String> colDebit = new TableColumn<>("Débito");
        colDebit.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().debit())));
        TableColumn<AccountingReportService.LedgerLine, String> colCredit = new TableColumn<>("Crédito");
        colCredit.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().credit())));
        TableColumn<AccountingReportService.LedgerLine, String> colRunning = new TableColumn<>("Saldo");
        colRunning.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().runningBalance())));
        ledgerTable.getColumns().addAll(List.of(colDate, colVoucher, colDesc, colDebit, colCredit, colRunning));

        Label openingLabel = new Label();
        Label closingLabel = new Label();

        Runnable refresh = () -> {
            try {
                AccountingReportService.GeneralLedger ledger = accountingReportService.generateGeneralLedger(
                        account.getId(), startPicker.getValue(), endPicker.getValue());
                ledgerTable.setItems(FXCollections.observableArrayList(ledger.lines()));
                openingLabel.setText("Saldo inicial: " + format(ledger.openingBalance()));
                closingLabel.setText("Saldo final: " + format(ledger.closingBalance()));
            } catch (RuntimeException e) {
                logger.error("Error generating account ledger", e);
                openingLabel.setText("Error al generar: " + e.getMessage());
            }
        };

        Button refreshButton = new Button("Actualizar");
        refreshButton.getStyleClass().addAll("btn", "btn-info");
        refreshButton.setOnAction(e -> refresh.run());

        HBox controls = new HBox(10, new Label("Desde:"), startPicker, new Label("Hasta:"), endPicker, refreshButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(15,
                new Label(account.getCode() + " - " + account.getName()),
                controls,
                new HBox(20, openingLabel, closingLabel),
                ledgerTable);
        content.setPadding(new Insets(10));
        content.setPrefWidth(760);

        refresh.run();
        ModernDialog.showAndWait((Stage) accountsTable.getScene().getWindow(), content, "Movimientos de Cuenta");
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
