package com.econovafx.ui.controller;

import com.econovafx.domain.Account;
import com.econovafx.domain.AccountType;
import com.econovafx.service.AccountService;
import com.econovafx.ui.view.ViewFactory;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Accounts controller
 */
public class AccountsController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);
    
    private final AccountService accountService;
    private final ViewFactory viewFactory;
    
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
    
    public AccountsController(AccountService accountService, ViewFactory viewFactory) {
        this.accountService = accountService;
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
        showAlert(Alert.AlertType.INFORMATION, "Próximamente", 
                "Vista de movimientos de cuenta en desarrollo");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
