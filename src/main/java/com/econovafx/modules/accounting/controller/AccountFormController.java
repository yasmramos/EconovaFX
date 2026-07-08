package com.econovafx.ui.controller;

import com.econovafx.model.Account;
import com.econovafx.model.AccountType;
import com.econovafx.service.AccountService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Account form controller
 */
public class AccountFormController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountFormController.class);
    
    private final AccountService accountService;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private TextField codeField;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private ComboBox<AccountType> typeComboBox;
    
    @FXML
    private ComboBox<Account> parentAccountComboBox;
    
    @FXML
    private TextField balanceField;
    
    @FXML
    private TextArea descriptionArea;
    
    private Account editingAccount;
    private boolean saved = false;
    
    public AccountFormController(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("AccountFormController initialized");
        initializeComboBoxes();
    }
    
    private void initializeComboBoxes() {
        typeComboBox.setItems(FXCollections.observableArrayList(AccountType.values()));
        typeComboBox.setConverter(new StringConverter<AccountType>() {
            @Override
            public String toString(AccountType type) {
                return type != null ? type.getDisplayName() : "";
            }
            
            @Override
            public AccountType fromString(String string) {
                for (AccountType type : AccountType.values()) {
                    if (type.getDisplayName().equals(string)) {
                        return type;
                    }
                }
                return null;
            }
        });
        
        typeComboBox.setValue(AccountType.ASSET);
        
        List<Account> accounts = accountService.getAllAccounts();
        parentAccountComboBox.setItems(FXCollections.observableArrayList(accounts));
        parentAccountComboBox.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                return account != null ? account.getCode() + " - " + account.getName() : "";
            }
            
            @Override
            public Account fromString(String string) {
                return null;
            }
        });
    }
    
    public void setEditingAccount(Account account) {
        this.editingAccount = account;
        if (account != null) {
            titleLabel.setText("Editar Cuenta");
            codeField.setText(account.getCode());
            nameField.setText(account.getName());
            typeComboBox.setValue(account.getType());
            parentAccountComboBox.setValue(account.getParentAccount());
            balanceField.setText(account.getBalance().toPlainString());
            descriptionArea.setText(account.getDescription());
        } else {
            titleLabel.setText("Nueva Cuenta");
        }
    }
    
    public Account getResult() {
        return saved ? editingAccount : null;
    }
    
    @FXML
    private void save() {
        try {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            AccountType type = typeComboBox.getValue();
            Account parent = parentAccountComboBox.getValue();
            BigDecimal balance = balanceField.getText().trim().isEmpty() ? 
                    BigDecimal.ZERO : new BigDecimal(balanceField.getText().trim());
            String description = descriptionArea.getText().trim();
            
            if (code.isEmpty() || name.isEmpty() || type == null) {
                showAlert(Alert.AlertType.ERROR, "Validación", 
                        "Por favor complete los campos requeridos");
                return;
            }
            
            if (editingAccount == null) {
                editingAccount = new Account(code, name, type);
            }
            
            editingAccount.setCode(code);
            editingAccount.setName(name);
            editingAccount.setType(type);
            editingAccount.setParentAccount(parent);
            editingAccount.setBalance(balance);
            editingAccount.setDescription(description);
            
            saved = true;
            closeDialog();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Formato de saldo inválido. Use números decimales.");
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
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
