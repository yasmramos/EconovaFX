package com.econovafx.ui.controller;

import com.econovafx.model.Transaction;
import com.econovafx.service.AccountService;
import com.econovafx.service.TransactionService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Transactions controller
 */
public class TransactionsController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionsController.class);
    
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final ViewFactory viewFactory;
    
    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private TableView<Transaction> transactionsTable;
    
    @FXML
    private TableColumn<Transaction, LocalDate> colDate;
    
    @FXML
    private TableColumn<Transaction, String> colNumber;
    
    @FXML
    private TableColumn<Transaction, String> colType;
    
    @FXML
    private TableColumn<Transaction, String> colDescription;
    
    @FXML
    private TableColumn<Transaction, String> colReference;
    
    @FXML
    private TableColumn<Transaction, BigDecimal> colDebit;
    
    @FXML
    private TableColumn<Transaction, BigDecimal> colCredit;
    
    @FXML
    private TableColumn<Transaction, Boolean> colStatus;
    
    @Inject
    public TransactionsController(TransactionService transactionService,
                                  AccountService accountService,
                                  ViewFactory viewFactory) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.viewFactory = viewFactory;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("TransactionsController initialized");
        initializeTableColumns();
        loadTransactions();
        
        fromDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        toDatePicker.setValue(LocalDate.now());
    }
    
    private void initializeTableColumns() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDebit.setCellValueFactory(new PropertyValueFactory<>("totalDebit"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("totalCredit"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("isPosted"));
        
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Publicado" : "Borrador");
                    setStyle(item ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #f39c12;");
                }
            }
        });
    }
    
    private void loadTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(transactions);
        transactionsTable.setItems(observableTransactions);
    }
    
    @FXML
    private void filterTransactions() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        if (fromDate != null && toDate != null) {
            List<Transaction> filtered = transactionService.getTransactionsByDateRange(fromDate, toDate);
            ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(filtered);
            transactionsTable.setItems(observableTransactions);
        } else {
            loadTransactions();
        }
    }
    
    @FXML
    private void newTransaction() {
        logger.debug("New transaction clicked");
        Optional<Transaction> result = viewFactory.showTransactionEntryDialog(null);
        result.ifPresent(transaction -> {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Transacción creada correctamente");
            loadTransactions();
        });
    }
    
    @FXML
    private void viewTransactionDetails() {
        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida", 
                    "Por favor seleccione una transacción para ver detalles");
            return;
        }
        
        showAlert(Alert.AlertType.INFORMATION, "Detalles", 
                "Transacción: " + selected.getNumber() + "\n" +
                "Fecha: " + selected.getDate() + "\n" +
                "Tipo: " + selected.getType() + "\n" +
                "Descripción: " + selected.getDescription() + "\n" +
                "Débito: " + selected.getTotalDebit() + "\n" +
                "Crédito: " + selected.getTotalCredit());
    }
    
    @FXML
    private void postTransaction() {
        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida", 
                    "Por favor seleccione una transacción para publicar");
            return;
        }
        
        if (selected.getIsPosted()) {
            showAlert(Alert.AlertType.WARNING, "Ya publicada", 
                    "Esta transacción ya está publicada");
            return;
        }
        
        try {
            transactionService.postTransaction(selected.getId());
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Transacción publicada correctamente");
            loadTransactions();
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }
    
    @FXML
    private void reverseTransaction() {
        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida", 
                    "Por favor seleccione una transacción para reversar");
            return;
        }
        
        if (!selected.getIsPosted()) {
            showAlert(Alert.AlertType.WARNING, "No publicada", 
                    "Solo se pueden reversar transacciones publicadas");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Motivo de Reversión");
        dialog.setHeaderText("Ingrese el motivo para reversar esta transacción");
        dialog.setContentText("Motivo:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(motivo -> {
            try {
                transactionService.reverseTransaction(selected.getId(), motivo);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Transacción reversada correctamente");
                loadTransactions();
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        });
    }
    
    @FXML
    private void deleteTransaction() {
        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida", 
                    "Por favor seleccione una transacción para eliminar");
            return;
        }
        
        if (selected.getIsPosted()) {
            showAlert(Alert.AlertType.WARNING, "No se puede eliminar", 
                    "Las transacciones publicadas no se pueden eliminar. Debe reversarlas primero.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta transacción?");
        alert.setContentText("Transacción: " + selected.getNumber());
        
        Optional<ButtonType> buttonResult = alert.showAndWait();
        if (buttonResult.isPresent() && buttonResult.get() == ButtonType.OK) {
            try {
                transactionService.deleteTransaction(selected.getId());
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Transacción eliminada correctamente");
                loadTransactions();
            } catch (IllegalStateException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
