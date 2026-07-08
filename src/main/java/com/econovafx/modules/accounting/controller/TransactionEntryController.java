package com.econovafx.ui.controller;

import com.econovafx.model.Account;
import com.econovafx.model.Transaction;
import com.econovafx.service.AccountService;
import com.econovafx.service.TransactionService;
import com.econovafx.service.TransactionService.TransactionEntryData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Transaction entry controller
 */
public class TransactionEntryController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionEntryController.class);
    
    private final AccountService accountService;
    private final TransactionService transactionService;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private ComboBox<String> typeComboBox;
    
    @FXML
    private TextField descriptionField;
    
    @FXML
    private TextField referenceField;
    
    @FXML
    private TableView<EntryRow> entriesTable;
    
    @FXML
    private TableColumn<EntryRow, Account> colAccount;
    
    @FXML
    private TableColumn<EntryRow, String> colDescription;
    
    @FXML
    private TableColumn<EntryRow, BigDecimal> colDebit;
    
    @FXML
    private TableColumn<EntryRow, BigDecimal> colCredit;
    
    @FXML
    private TableColumn<EntryRow, Void> colActions;
    
    @FXML
    private Label totalDebitLabel;
    
    @FXML
    private Label totalCreditLabel;
    
    @FXML
    private Label differenceLabel;
    
    private final ObservableList<EntryRow> entryRows = FXCollections.observableArrayList();
    private boolean saved = false;
    private Transaction resultTransaction;
    
    public TransactionEntryController(AccountService accountService,
                                      TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("TransactionEntryController initialized");
        initializeTableColumns();
        initializeComboBoxes();
        datePicker.setValue(LocalDate.now());
    }
    
    private void initializeTableColumns() {
        colAccount.setCellValueFactory(new PropertyValueFactory<>("account"));
        colAccount.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCode() + " - " + item.getName());
                }
            }
        });
        
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDebit.setCellValueFactory(new PropertyValueFactory<>("debit"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑️");
            
            {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                deleteButton.setOnAction(event -> {
                    EntryRow row = getTableView().getItems().get(getIndex());
                    entryRows.remove(row);
                    updateTotals();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        
        entriesTable.setItems(entryRows);
    }
    
    private void initializeComboBoxes() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "INGRESO", "GASTO", "TRANSFERENCIA", "ASIENTO", "NOTA_CREDITO", "NOTA_DEBITO"));
    }
    
    @FXML
    private void addEntry() {
        List<Account> accounts = accountService.getAllAccounts();
        if (accounts.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sin cuentas", 
                    "No hay cuentas registradas. Cree una cuenta primero.");
            return;
        }
        
        ChoiceDialog<Account> dialog = new ChoiceDialog<>(accounts.get(0), accounts);
        dialog.setTitle("Seleccionar Cuenta");
        dialog.setHeaderText("Seleccione una cuenta contable");
        dialog.setContentText("Cuenta:");
        
        Optional<Account> result = dialog.showAndWait();
        result.ifPresent(account -> {
            EntryRow row = new EntryRow(account, "", BigDecimal.ZERO, BigDecimal.ZERO);
            entryRows.add(row);
            updateTotals();
        });
    }
    
    private void updateTotals() {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (EntryRow row : entryRows) {
            totalDebit = totalDebit.add(row.getDebit());
            totalCredit = totalCredit.add(row.getCredit());
        }
        
        totalDebitLabel.setText(totalDebit.toPlainString());
        totalCreditLabel.setText(totalCredit.toPlainString());
        
        BigDecimal difference = totalDebit.subtract(totalCredit);
        differenceLabel.setText(difference.toPlainString());
        
        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            differenceLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            differenceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }
    
    @FXML
    private void save() {
        try {
            String type = typeComboBox.getValue();
            String description = descriptionField.getText().trim();
            LocalDate date = datePicker.getValue();
            String reference = referenceField.getText().trim();
            
            if (type == null || description.isEmpty() || date == null) {
                showAlert(Alert.AlertType.WARNING, "Validación", 
                        "Por favor complete los campos requeridos");
                return;
            }
            
            if (entryRows.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validación", 
                        "Debe agregar al menos un asiento contable");
                return;
            }
            
            BigDecimal totalDebit = entryRows.stream()
                    .map(EntryRow::getDebit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCredit = entryRows.stream()
                    .map(EntryRow::getCredit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (totalDebit.compareTo(totalCredit) != 0) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "La transacción no está cuadrada. Débito y Crédito deben ser iguales.");
                return;
            }
            
            com.econovafx.model.Transaction transaction = new com.econovafx.model.Transaction();
            transaction.setDate(date);
            transaction.setType(type);
            transaction.setDescription(description);
            transaction.setReference(reference);
            
            List<TransactionEntryData> entryDataList = new ArrayList<>();
            for (EntryRow row : entryRows) {
                entryDataList.add(new TransactionEntryData(
                        row.getAccount().getId(),
                        row.getDebit(),
                        row.getCredit(),
                        row.getDescription()));
            }
            
            resultTransaction = transactionService.createTransaction(transaction, entryDataList);
            saved = true;
            
            closeDialog();
            
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
    
    @FXML
    private void cancel() {
        saved = false;
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
    
    public Transaction getResult() {
        return saved ? resultTransaction : null;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static class EntryRow {
        private final ObjectProperty<Account> account;
        private final StringProperty description;
        private final ObjectProperty<BigDecimal> debit;
        private final ObjectProperty<BigDecimal> credit;
        
        public EntryRow(Account account, String description, BigDecimal debit, BigDecimal credit) {
            this.account = new SimpleObjectProperty<>(account);
            this.description = new SimpleStringProperty(description);
            this.debit = new SimpleObjectProperty<>(debit);
            this.credit = new SimpleObjectProperty<>(credit);
        }
        
        public Account getAccount() { return account.get(); }
        public void setAccount(Account account) { this.account.set(account); }
        public String getDescription() { return description.get(); }
        public void setDescription(String description) { this.description.set(description); }
        public BigDecimal getDebit() { return debit.get(); }
        public void setDebit(BigDecimal debit) { this.debit.set(debit); }
        public BigDecimal getCredit() { return credit.get(); }
        public void setCredit(BigDecimal credit) { this.credit.set(credit); }
        
        public ObjectProperty<Account> accountProperty() { return account; }
        public StringProperty descriptionProperty() { return description; }
        public ObjectProperty<BigDecimal> debitProperty() { return debit; }
        public ObjectProperty<BigDecimal> creditProperty() { return credit; }
    }
}
