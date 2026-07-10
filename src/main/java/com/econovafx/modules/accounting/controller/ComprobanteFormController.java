package com.econovafx.modules.accounting.controller;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.core.service.ThirdPartyService;
import com.econovafx.modules.core.service.TransactionService;
import com.econovafx.modules.core.ui.util.ModernDialog;
import com.econovafx.modules.core.ui.util.NotificationService;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Comprobante Form Dialog
 */
public class ComprobanteFormController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ComprobanteFormController.class);

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ThirdPartyService thirdPartyService;

    // Header fields
    @FXML
    private Label titleLabel;
    @FXML
    private Label numberLabel;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField descriptionField;
    @FXML
    private ComboBox<ThirdParty> thirdPartyComboBox;

    // Entries table
    @FXML
    private TableView<EntryRow> entriesTable;
    @FXML
    private TableColumn<EntryRow, Account> colCta;
    @FXML
    private TableColumn<EntryRow, Account> colSbcta;
    @FXML
    private TableColumn<EntryRow, Account> colSctro;
    @FXML
    private TableColumn<EntryRow, Account> colAnal;
    @FXML
    private TableColumn<EntryRow, Account> colEpig;
    @FXML
    private TableColumn<EntryRow, String> colDebit;
    @FXML
    private TableColumn<EntryRow, String> colCredit;

    // Totals
    @FXML
    private Label totalDebitLabel;
    @FXML
    private Label totalCreditLabel;
    @FXML
    private Label differenceLabel;
    @FXML
    private Label validationLabel;

    private final ObservableList<EntryRow> entriesData = FXCollections.observableArrayList();
    private List<Account> allAccounts;

    private Transaction editingTransaction;
    private boolean saved = false;

    public ComprobanteFormController(AccountService accountService, 
                                     TransactionService transactionService,
                                     ThirdPartyService thirdPartyService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.thirdPartyService = thirdPartyService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("ComprobanteFormController initialized");
        setupTableColumns();
        loadAccounts();
        loadThirdParties();
        
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Add listeners for real-time validation
        entriesData.addListener((javafx.collections.ListChangeListener.Change<? extends EntryRow> c) -> updateTotals());
    }

    private void setupTableColumns() {
        // CTA column - Always visible, root accounts only
        colCta.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCtaAccount()));
        colCta.setCellFactory(column -> createAccountComboBoxCell(
                row -> row.getCtaAccount(),
                row -> account -> {
                    row.setCtaAccount(account);
                    // Reset all child accounts when parent changes
                    row.setSbctaAccount(null);
                    row.setSctroAccount(null);
                    row.setAnalAccount(null);
                    row.setEpigAccount(null);
                    // Refresh table to show/hide next column
                    entriesTable.refresh();
                },
                null
        ));

        // SBCTA column - Only shows if CTA has children
        colSbcta.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSbctaAccount()));
        colSbcta.setCellFactory(column -> createAccountComboBoxCell(
                row -> row.getSbctaAccount(),
                row -> account -> {
                    row.setSbctaAccount(account);
                    // Reset all child accounts
                    row.setSctroAccount(null);
                    row.setAnalAccount(null);
                    row.setEpigAccount(null);
                    entriesTable.refresh();
                },
                row -> row.getCtaAccount()
        ));

        // SCTRO column - Only shows if SBCTA has children
        colSctro.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSctroAccount()));
        colSctro.setCellFactory(column -> createAccountComboBoxCell(
                row -> row.getSctroAccount(),
                row -> account -> {
                    row.setSctroAccount(account);
                    row.setAnalAccount(null);
                    row.setEpigAccount(null);
                    entriesTable.refresh();
                },
                row -> row.getSbctaAccount()
        ));

        // ANAL column - Only shows if SCTRO has children
        colAnal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAnalAccount()));
        colAnal.setCellFactory(column -> createAccountComboBoxCell(
                row -> row.getAnalAccount(),
                row -> account -> {
                    row.setAnalAccount(account);
                    row.setEpigAccount(null);
                    entriesTable.refresh();
                },
                row -> row.getSctroAccount()
        ));

        // EPIG column - Only shows if ANAL has children
        colEpig.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEpigAccount()));
        colEpig.setCellFactory(column -> createAccountComboBoxCell(
                row -> row.getEpigAccount(),
                row -> account -> row.setEpigAccount(account),
                row -> row.getAnalAccount()
        ));

        // Update column visibility based on row data
        updateColumnVisibility();

        // Debit column
        colDebit.setCellValueFactory(cellData -> cellData.getValue().debitProperty());
        colDebit.setCellFactory(TextFieldTableCell.forTableColumn());
        colDebit.setOnEditCommit(event -> {
            EntryRow row = event.getRowValue();
            try {
                String newValue = event.getNewValue().replace(",", "");
                BigDecimal debit = new BigDecimal(newValue);
                row.setDebit(newValue);
                row.setCredit("0.00");
                updateTotals();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Formato de monto inválido");
                entriesTable.refresh();
            }
        });

        // Credit column
        colCredit.setCellValueFactory(cellData -> cellData.getValue().creditProperty());
        colCredit.setCellFactory(TextFieldTableCell.forTableColumn());
        colCredit.setOnEditCommit(event -> {
            EntryRow row = event.getRowValue();
            try {
                String newValue = event.getNewValue().replace(",", "");
                BigDecimal credit = new BigDecimal(newValue);
                row.setCredit(newValue);
                row.setDebit("0.00");
                updateTotals();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Formato de monto inválido");
                entriesTable.refresh();
            }
        });

        entriesTable.setItems(entriesData);
        entriesTable.setEditable(true);
    }

    /**
     * Update column visibility based on selected accounts in all rows
     */
    private void updateColumnVisibility() {
        // Check if any row has accounts at each level
        boolean showSbcta = entriesData.stream().anyMatch(r -> r.getCtaAccount() != null && hasChildren(r.getCtaAccount()));
        boolean showSctro = entriesData.stream().anyMatch(r -> r.getSbctaAccount() != null && hasChildren(r.getSbctaAccount()));
        boolean showAnal = entriesData.stream().anyMatch(r -> r.getSctroAccount() != null && hasChildren(r.getSctroAccount()));
        boolean showEpig = entriesData.stream().anyMatch(r -> r.getAnalAccount() != null && hasChildren(r.getAnalAccount()));

        colSbcta.setVisible(showSbcta);
        colSctro.setVisible(showSctro);
        colAnal.setVisible(showAnal);
        colEpig.setVisible(showEpig);
    }

    /**
     * Check if an account has child accounts
     */
    private boolean hasChildren(Account account) {
        if (account == null) return false;
        return allAccounts.stream()
                .anyMatch(a -> a.getParentAccount() != null && 
                               a.getParentAccount().getId().equals(account.getId()));
    }

    /**
     * Create ComboBox cell factory for accounts with parent filtering
     */
    private TableCell<EntryRow, Account> createAccountComboBoxCell(
            java.util.function.Function<EntryRow, Account> getter,
            java.util.function.Function<EntryRow, java.util.function.Consumer<Account>> setterFactory,
            java.util.function.Function<EntryRow, Account> parentGetter) {
        
        return new TableCell<>() {
            private final ComboBox<Account> comboBox = new ComboBox<>();
            private final javafx.scene.layout.StackPane emptyCell = new javafx.scene.layout.StackPane();
            
            {
                comboBox.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(Account account) {
                        return account != null ? account.getCode() + " - " + account.getName() : "";
                    }

                    @Override
                    public Account fromString(String string) {
                        return null;
                    }
                });
                
                comboBox.setOnAction(e -> {
                    if (!isEmpty()) {
                        EntryRow row = getTableView().getItems().get(getIndex());
                        java.util.function.Consumer<Account> setter = setterFactory.apply(row);
                        setter.accept(comboBox.getValue());
                        updateColumnVisibility();
                    }
                });
                
                emptyCell.setStyle("-fx-background-color: #f9fafb;");
            }

            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EntryRow row = getTableView().getItems().get(getIndex());
                    
                    // If this column requires a parent and parent is not selected, show empty cell
                    if (parentGetter != null) {
                        Account parent = parentGetter.apply(row);
                        if (parent == null) {
                            // Parent not selected, show empty gray cell
                            setGraphic(emptyCell);
                            return;
                        }
                        
                        // Filter accounts based on parent
                        List<Account> filteredAccounts = allAccounts.stream()
                                .filter(a -> a.getParentAccount() != null && 
                                             a.getParentAccount().getId().equals(parent.getId()))
                                .toList();
                        
                        if (filteredAccounts.isEmpty()) {
                            // No children available, show empty cell
                            setGraphic(emptyCell);
                            return;
                        }
                        
                        comboBox.setItems(FXCollections.observableArrayList(filteredAccounts));
                    } else {
                        // Root level - show all accounts with no parent
                        List<Account> filteredAccounts = allAccounts.stream()
                                .filter(a -> a.getParentAccount() == null)
                                .toList();
                        
                        comboBox.setItems(FXCollections.observableArrayList(filteredAccounts));
                    }
                    
                    comboBox.setValue(item);
                    setGraphic(comboBox);
                }
            }
        };
    }

    private void loadAccounts() {
        allAccounts = accountService.getAllAccounts();
        if (allAccounts.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", 
                    "No accounts configured. Please create accounts first.");
        }
    }
    
    private void loadThirdParties() {
        List<ThirdParty> thirdParties = thirdPartyService.getAllThirdParties();
        ObservableList<ThirdParty> observableList = FXCollections.observableArrayList(thirdParties);
        thirdPartyComboBox.setItems(observableList);
        
        // Add "None" option at the beginning
        ThirdParty noneOption = new ThirdParty();
        noneOption.setName("-- Select Third Party --");
        observableList.add(0, noneOption);
        
        thirdPartyComboBox.setValue(noneOption);
        
        // Custom converter to display third party name
        thirdPartyComboBox.setConverter(new StringConverter<ThirdParty>() {
            @Override
            public String toString(ThirdParty tp) {
                if (tp == null || tp.getId() == null) {
                    return "-- Select Third Party --";
                }
                return tp.getName() + " (" + tp.getIdentificationNumber() + ")";
            }

            @Override
            public ThirdParty fromString(String string) {
                return null;
            }
        });
    }

    public void setEditingTransaction(Transaction transaction) {
        this.editingTransaction = transaction;
        if (transaction != null) {
            // Edit mode
            titleLabel.setText("Edit Voucher");
            numberLabel.setText(transaction.getNumber());
            datePicker.setValue(transaction.getDate());
            descriptionField.setText(transaction.getDescription());
            
            // Load third party if exists
            if (transaction.getThirdParty() != null) {
                thirdPartyComboBox.setValue(transaction.getThirdParty());
            } else {
                // Find and set the "None" option
                ThirdParty noneOption = thirdPartyComboBox.getItems().stream()
                        .filter(tp -> tp.getId() == null)
                        .findFirst()
                        .orElse(null);
                thirdPartyComboBox.setValue(noneOption);
            }
            
            // Load entries
            entriesData.clear();
            for (TransactionEntry entry : transaction.getEntries()) {
                entriesData.add(new EntryRow(
                        entry.getDebitAmount().toPlainString(),
                        entry.getCreditAmount().toPlainString()
                ));
                // Set the account from the entry
                EntryRow row = entriesData.get(entriesData.size() - 1);
                row.setSbctaAccount(entry.getAccount());
            }
            updateTotals();
        } else {
            // New mode
            titleLabel.setText("New Voucher");
            numberLabel.setText("(Automatic)");
        }
    }

    public Transaction getResult() {
        return saved ? editingTransaction : null;
    }

    @FXML
    private void addEntry() {
        entriesData.add(new EntryRow("0.00", "0.00"));
        updateTotals();
    }

    @FXML
    private void removeEntry() {
        EntryRow selected = entriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Eliminar", "Seleccione una línea para eliminar");
            return;
        }
        entriesData.remove(selected);
        updateTotals();
    }

    @FXML
    private void save() {
        try {
            // Validate required fields
            LocalDate date = datePicker.getValue();
            String description = descriptionField.getText().trim();

            if (date == null) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Date is required");
                return;
            }
            if (description.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Description is required");
                return;
            }

            // Validate entries
            if (entriesData.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation", "You must add at least one accounting entry");
                return;
            }

            if (entriesData.size() < 2) {
                showAlert(Alert.AlertType.ERROR, "Validation", 
                        "You must have at least 2 entries (double-entry bookkeeping)");
                return;
            }

            // Validate all accounts are selected
            for (EntryRow row : entriesData) {
                if (row.getFinalAccount() == null) {
                    showAlert(Alert.AlertType.ERROR, "Validation", 
                            "All rows must have an account selected in CTA/SBCTA/SCTRO/ANAL/EPIG columns");
                    return;
                }
            }

            // Validate balanced
            BigDecimal totalDebit = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;
            List<TransactionService.TransactionEntryData> entryDataList = new ArrayList<>();

            for (EntryRow row : entriesData) {
                BigDecimal debit = new BigDecimal(row.getDebit().replace(",", ""));
                BigDecimal credit = new BigDecimal(row.getCredit().replace(",", ""));

                if (debit.compareTo(BigDecimal.ZERO) > 0 && credit.compareTo(BigDecimal.ZERO) > 0) {
                    showAlert(Alert.AlertType.ERROR, "Validation", 
                            "Each row must have only Debit OR Credit, not both");
                    return;
                }

                totalDebit = totalDebit.add(debit);
                totalCredit = totalCredit.add(credit);

                entryDataList.add(new TransactionService.TransactionEntryData(
                        row.getFinalAccount().getId(),
                        debit,
                        credit,
                        "Accounting entry"
                ));
            }

            if (totalDebit.compareTo(totalCredit) != 0) {
                validationLabel.setVisible(true);
                validationLabel.setManaged(true);
                showAlert(Alert.AlertType.ERROR, "Validation", 
                        "Voucher is not balanced.\nDebit: " + totalDebit + 
                        "\nCredit: " + totalCredit);
                return;
            }

            validationLabel.setVisible(false);
            validationLabel.setManaged(false);

            // Create or update transaction
            if (editingTransaction == null) {
                editingTransaction = new Transaction();
                editingTransaction.setDate(date);
                editingTransaction.setType("JOURNAL");
                editingTransaction.setDescription(description);
                editingTransaction.setIsPosted(false);
                
                // Set third party if selected
                ThirdParty selectedThirdParty = thirdPartyComboBox.getValue();
                if (selectedThirdParty != null && selectedThirdParty.getId() != null) {
                    editingTransaction.setThirdParty(selectedThirdParty);
                }

                transactionService.createTransaction(editingTransaction, entryDataList);
            } else {
                // Update existing (simplified - in production would need more complex logic)
                editingTransaction.setDate(date);
                editingTransaction.setDescription(description);
                
                // Update third party
                ThirdParty selectedThirdParty = thirdPartyComboBox.getValue();
                if (selectedThirdParty != null && selectedThirdParty.getId() != null) {
                    editingTransaction.setThirdParty(selectedThirdParty);
                } else {
                    editingTransaction.setThirdParty(null);
                }
            }

            saved = true;
            closeDialog();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Formato numérico inválido en montos");
        } catch (Exception e) {
            logger.error("Error saving comprobante", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        saved = false;
        closeDialog();
    }

    private void updateTotals() {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (EntryRow row : entriesData) {
            try {
                if (!row.getDebit().isEmpty() && !row.getDebit().equals("0.00")) {
                    totalDebit = totalDebit.add(new BigDecimal(row.getDebit().replace(",", "")));
                }
                if (!row.getCredit().isEmpty() && !row.getCredit().equals("0.00")) {
                    totalCredit = totalCredit.add(new BigDecimal(row.getCredit().replace(",", "")));
                }
            } catch (NumberFormatException e) {
                // Ignore invalid numbers during editing
            }
        }

        totalDebitLabel.setText(totalDebit.toPlainString());
        totalCreditLabel.setText(totalCredit.toPlainString());
        
        BigDecimal difference = totalDebit.subtract(totalCredit);
        differenceLabel.setText(difference.toPlainString());
        
        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            differenceLabel.getStyleClass().removeAll("value-warning", "value-negative");
            differenceLabel.getStyleClass().add("value-positive");
            validationLabel.setVisible(false);
            validationLabel.setManaged(false);
        } else {
            differenceLabel.getStyleClass().removeAll("value-positive", "value-negative");
            differenceLabel.getStyleClass().add("value-warning");
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Data model for entry rows
     */
    public static class EntryRow {
        private Account ctaAccount;
        private Account sbctaAccount;
        private Account sctroAccount;
        private Account analAccount;
        private Account epigAccount;
        private final SimpleStringProperty debit;
        private final SimpleStringProperty credit;

        public EntryRow(String debit, String credit) {
            this.ctaAccount = null;
            this.sbctaAccount = null;
            this.sctroAccount = null;
            this.analAccount = null;
            this.epigAccount = null;
            this.debit = new SimpleStringProperty(debit);
            this.credit = new SimpleStringProperty(credit);
        }

        public Account getCtaAccount() { return ctaAccount; }
        public void setCtaAccount(Account account) { this.ctaAccount = account; }

        public Account getSbctaAccount() { return sbctaAccount; }
        public void setSbctaAccount(Account account) { this.sbctaAccount = account; }

        public Account getSctroAccount() { return sctroAccount; }
        public void setSctroAccount(Account account) { this.sctroAccount = account; }

        public Account getAnalAccount() { return analAccount; }
        public void setAnalAccount(Account account) { this.analAccount = account; }

        public Account getEpigAccount() { return epigAccount; }
        public void setEpigAccount(Account account) { this.epigAccount = account; }

        public Account getFinalAccount() {
            // Return the most specific account selected
            if (epigAccount != null) return epigAccount;
            if (analAccount != null) return analAccount;
            if (sctroAccount != null) return sctroAccount;
            if (sbctaAccount != null) return sbctaAccount;
            return ctaAccount;
        }

        public String getDebit() { return debit.get(); }
        public void setDebit(String debit) { this.debit.set(debit); }
        public SimpleStringProperty debitProperty() { return debit; }

        public String getCredit() { return credit.get(); }
        public void setCredit(String credit) { this.credit.set(credit); }
        public SimpleStringProperty creditProperty() { return credit; }
    }
}
