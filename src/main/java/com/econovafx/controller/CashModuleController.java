package com.econovafx.controller;

import com.econovafx.model.*;
import com.econovafx.service.BankAccountService;
import com.econovafx.service.CashMovementService;
import com.econovafx.service.BankReconciliationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Cash Module (Bank & Cash Management).
 */
public class CashModuleController {
    
    private final BankAccountService bankAccountService = new BankAccountService();
    private final CashMovementService cashMovementService = new CashMovementService();
    private final BankReconciliationService reconciliationService = new BankReconciliationService();
    
    private ObservableList<BankAccount> bankAccountsData = FXCollections.observableArrayList();
    private ObservableList<CashBox> cashBoxesData = FXCollections.observableArrayList();
    private ObservableList<CashMovement> movementsData = FXCollections.observableArrayList();
    private ObservableList<ReconciliationItem> systemItemsData = FXCollections.observableArrayList();
    private ObservableList<ReconciliationItem> bankItemsData = FXCollections.observableArrayList();
    
    private BankReconciliation currentReconciliation;

    // Bank Accounts Tab
    @FXML private TableView<BankAccount> bankAccountsTable;
    @FXML private TableColumn<BankAccount, String> colBankCode;
    @FXML private TableColumn<BankAccount, String> colBankDescription;
    @FXML private TableColumn<BankAccount, String> colBankNumber;
    @FXML private TableColumn<BankAccount, String> colBankEntity;
    @FXML private TableColumn<BankAccount, String> colBankCurrency;
    @FXML private TableColumn<BankAccount, String> colBankAccounting;
    @FXML private TableColumn<BankAccount, BigDecimal> colBankBalance;
    @FXML private TableColumn<BankAccount, Boolean> colBankActive;
    @FXML private CheckBox chkShowInactiveBanks;

    // Cash Boxes Tab
    @FXML private TableView<CashBox> cashBoxesTable;
    @FXML private TableColumn<CashBox, String> colBoxCode;
    @FXML private TableColumn<CashBox, String> colBoxDescription;
    @FXML private TableColumn<CashBox, String> colBoxCurrency;
    @FXML private TableColumn<CashBox, String> colBoxAccounting;
    @FXML private TableColumn<CashBox, BigDecimal> colBoxBalance;
    @FXML private TableColumn<CashBox, Boolean> colBoxOpen;
    @FXML private CheckBox chkShowClosedBoxes;

    // Movements Tab
    @FXML private TableView<CashMovement> movementsTable;
    @FXML private TableColumn<CashMovement, LocalDate> colMoveDate;
    @FXML private TableColumn<CashMovement, String> colMoveDocument;
    @FXML private TableColumn<CashMovement, String> colMoveType;
    @FXML private TableColumn<CashMovement, String> colMoveDescription;
    @FXML private TableColumn<CashMovement, String> colMoveSource;
    @FXML private TableColumn<CashMovement, String> colMoveDestination;
    @FXML private TableColumn<CashMovement, BigDecimal> colMoveAmount;
    @FXML private TableColumn<CashMovement, String> colMoveStatus;
    @FXML private TableColumn<CashMovement, Boolean> colMoveReconciled;
    @FXML private ComboBox<String> cbMovementFilter;

    // Reconciliation Tab
    @FXML private ComboBox<BankAccount> cbReconciliationBank;
    @FXML private TableView<ReconciliationItem> systemItemsTable;
    @FXML private TableColumn<ReconciliationItem, LocalDate> colSysItemDate;
    @FXML private TableColumn<ReconciliationItem, String> colSysItemDesc;
    @FXML private TableColumn<ReconciliationItem, BigDecimal> colSysItemAmount;
    @FXML private TableColumn<ReconciliationItem, Boolean> colSysItemReconciled;
    @FXML private TableView<ReconciliationItem> bankItemsTable;
    @FXML private TableColumn<ReconciliationItem, LocalDate> colBankItemDate;
    @FXML private TableColumn<ReconciliationItem, String> colBankItemRef;
    @FXML private TableColumn<ReconciliationItem, String> colBankItemDesc;
    @FXML private TableColumn<ReconciliationItem, BigDecimal> colBankItemAmount;
    @FXML private TableColumn<ReconciliationItem, Boolean> colBankItemReconciled;
    @FXML private TextField txtBankBalance;
    @FXML private TextField txtSystemBalance;
    @FXML private TextField txtDifference;

    @FXML
    public void initialize() {
        setupBankAccountsTable();
        setupCashBoxesTable();
        setupMovementsTable();
        setupReconciliationTables();
        loadBankAccounts();
        loadCashBoxes();
        loadMovements();
        setupFilters();
    }

    private void setupBankAccountsTable() {
        colBankCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colBankDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colBankNumber.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colBankEntity.setCellValueFactory(new PropertyValueFactory<>("bankEntity"));
        colBankCurrency.setCellValueFactory(new PropertyValueFactory<>("currency"));
        colBankAccounting.setCellValueFactory(new PropertyValueFactory<>("accountingAccount"));
        colBankBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colBankActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        bankAccountsTable.setItems(bankAccountsData);
    }

    private void setupCashBoxesTable() {
        colBoxCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colBoxDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colBoxCurrency.setCellValueFactory(new PropertyValueFactory<>("currency"));
        colBoxAccounting.setCellValueFactory(new PropertyValueFactory<>("accountingAccount"));
        colBoxBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colBoxOpen.setCellValueFactory(new PropertyValueFactory<>("open"));
        cashBoxesTable.setItems(cashBoxesData);
    }

    private void setupMovementsTable() {
        colMoveDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMoveDocument.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        colMoveType.setCellValueFactory(cellData -> 
            cellData.getValue().getMovementType().toString());
        colMoveDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colMoveAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMoveStatus.setCellValueFactory(cellData -> 
            cellData.getValue().getStatus().toString());
        colMoveReconciled.setCellValueFactory(new PropertyValueFactory<>("reconciled"));
        movementsTable.setItems(movementsData);
    }

    private void setupReconciliationTables() {
        colSysItemDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colSysItemDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colSysItemAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colSysItemReconciled.setCellValueFactory(new PropertyValueFactory<>("reconciled"));
        systemItemsTable.setItems(systemItemsData);

        colBankItemDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colBankItemRef.setCellValueFactory(new PropertyValueFactory<>("bankReference"));
        colBankItemDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colBankItemAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colBankItemReconciled.setCellValueFactory(new PropertyValueFactory<>("reconciled"));
        bankItemsTable.setItems(bankItemsData);
    }

    private void setupFilters() {
        cbMovementFilter.getItems().addAll("All", "PENDING", "POSTED", "CANCELLED");
        cbMovementFilter.setValue("All");
        cbMovementFilter.setOnAction(e -> filterMovements());
        
        chkShowInactiveBanks.setOnAction(e -> loadBankAccounts());
        chkShowClosedBoxes.setOnAction(e -> loadCashBoxes());
    }

    private void loadBankAccounts() {
        bankAccountsData.clear();
        if (chkShowInactiveBanks.isSelected()) {
            bankAccountsData.addAll(bankAccountService.getAllAccounts());
        } else {
            bankAccountsData.addAll(bankAccountService.getActiveAccounts());
        }
    }

    private void loadCashBoxes() {
        cashBoxesData.clear();
        List<CashBox> allBoxes = new java.util.ArrayList<>();
        // Simulating repository call - in real app would have CashBoxService
        cashBoxesData.addAll(allBoxes);
    }

    private void loadMovements() {
        movementsData.clear();
        movementsData.addAll(cashMovementService.getPendingMovements());
    }

    private void filterMovements() {
        String selected = cbMovementFilter.getValue();
        movementsData.clear();
        if ("All".equals(selected)) {
            movementsData.addAll(cashMovementService.getPendingMovements());
        } else {
            // Filter by status - implementation pending
        }
    }

    @FXML
    private void handleRefresh() {
        loadBankAccounts();
        loadCashBoxes();
        loadMovements();
    }

    @FXML
    private void handleNewBankAccount() {
        // Open dialog to create new bank account
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Bank Account");
        alert.setHeaderText("Create Bank Account");
        alert.setContentText("Dialog implementation pending");
        alert.showAndWait();
    }

    @FXML
    private void handleEditBankAccount() {
        BankAccount selected = bankAccountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a bank account to edit.");
            return;
        }
        // Open edit dialog
    }

    @FXML
    private void handleDeleteBankAccount() {
        BankAccount selected = bankAccountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a bank account to delete.");
            return;
        }
        // Confirm and delete
    }

    @FXML
    private void handleNewCashBox() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Cash Box");
        alert.setHeaderText("Create Cash Box");
        alert.setContentText("Dialog implementation pending");
        alert.showAndWait();
    }

    @FXML
    private void handleEditCashBox() {
        CashBox selected = cashBoxesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a cash box to edit.");
            return;
        }
    }

    @FXML
    private void handleDeleteCashBox() {
        CashBox selected = cashBoxesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a cash box to delete.");
            return;
        }
    }

    @FXML
    private void handleNewMovement() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Movement");
        alert.setHeaderText("Register Cash Movement");
        alert.setContentText("Dialog implementation pending");
        alert.showAndWait();
    }

    @FXML
    private void handlePostMovement() {
        CashMovement selected = movementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a movement to post.");
            return;
        }
        if (selected.getStatus() != CashMovement.Status.PENDING) {
            showAlert("Invalid Status", "Only pending movements can be posted.");
            return;
        }
        try {
            cashMovementService.postMovement(selected.getId(), "currentUser");
            loadMovements();
            showAlert("Success", "Movement posted successfully.");
        } catch (Exception e) {
            showAlert("Error", "Failed to post movement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelMovement() {
        CashMovement selected = movementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a movement to cancel.");
            return;
        }
        try {
            cashMovementService.cancelMovement(selected.getId(), "currentUser");
            loadMovements();
            showAlert("Success", "Movement cancelled successfully.");
        } catch (Exception e) {
            showAlert("Error", "Failed to cancel movement: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewReconciliation() {
        BankAccount selectedBank = cbReconciliationBank.getValue();
        if (selectedBank == null) {
            showAlert("No Bank Selected", "Please select a bank account first.");
            return;
        }
        
        currentReconciliation = new BankReconciliation();
        currentReconciliation.setBankAccountId(selectedBank.getId());
        currentReconciliation.setStatementDate(LocalDate.now());
        currentReconciliation.setBankBalance(BigDecimal.ZERO);
        currentReconciliation.setSystemBalance(selectedBank.getBalance());
        
        reconciliationService.createReconciliation(currentReconciliation);
        systemItemsData.clear();
        bankItemsData.clear();
        updateReconciliationBalances();
    }

    @FXML
    private void handleCompleteReconciliation() {
        if (currentReconciliation == null) {
            showAlert("No Reconciliation", "No active reconciliation found.");
            return;
        }
        try {
            reconciliationService.completeReconciliation(currentReconciliation.getId(), "currentUser");
            showAlert("Success", "Reconciliation completed successfully.");
            currentReconciliation = null;
            systemItemsData.clear();
            bankItemsData.clear();
        } catch (Exception e) {
            showAlert("Error", "Failed to complete reconciliation: " + e.getMessage());
        }
    }

    @FXML
    private void handlePrintReconciliation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Report");
        alert.setHeaderText("Bank Reconciliation Report");
        alert.setContentText("Report generation pending - will export to PDF/Printer");
        alert.showAndWait();
    }

    private void updateReconciliationBalances() {
        if (currentReconciliation != null) {
            txtBankBalance.setText(currentReconciliation.getBankBalance().toString());
            txtSystemBalance.setText(currentReconciliation.getSystemBalance().toString());
            BigDecimal diff = currentReconciliation.getBankBalance().subtract(currentReconciliation.getSystemBalance());
            txtDifference.setText(diff.toString());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
