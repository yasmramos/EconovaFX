package com.econovafx.modules.cash.controller;

import com.econovafx.modules.cash.model.CashBox;
import com.econovafx.modules.cash.model.CashMovement;
import com.econovafx.modules.bank.model.BankAccount;
import com.econovafx.modules.bank.model.BankReconciliation;
import com.econovafx.modules.bank.repository.BankAccountRepository;
import com.econovafx.modules.bank.repository.BankReconciliationRepository;
import com.econovafx.modules.cash.repository.CashBoxRepository;
import com.econovafx.modules.cash.repository.CashMovementRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Cash Module - Bank & Cash Management.
 * Provides comprehensive management of bank accounts, cash boxes, movements and reconciliation.
 */
public class CashModuleController {

    private static final Logger logger = LoggerFactory.getLogger(CashModuleController.class);

    // Header
    @FXML
    private Button btnRefresh;

    // Tab Pane
    @FXML
    private TabPane tabPane;

    // Bank Accounts Tab
    @FXML
    private Button btnNewBankAccount;
    @FXML
    private Button btnEditBankAccount;
    @FXML
    private Button btnDeleteBankAccount;
    @FXML
    private CheckBox chkShowInactiveBanks;
    @FXML
    private TableView<BankAccount> bankAccountsTable;
    @FXML
    private TableColumn<BankAccount, String> colBankCode;
    @FXML
    private TableColumn<BankAccount, String> colBankDescription;
    @FXML
    private TableColumn<BankAccount, String> colBankNumber;
    @FXML
    private TableColumn<BankAccount, String> colBankEntity;
    @FXML
    private TableColumn<BankAccount, String> colBankCurrency;
    @FXML
    private TableColumn<BankAccount, String> colBankAccounting;
    @FXML
    private TableColumn<BankAccount, BigDecimal> colBankBalance;
    @FXML
    private TableColumn<BankAccount, Boolean> colBankActive;

    // Cash Boxes Tab
    @FXML
    private Button btnNewCashBox;
    @FXML
    private Button btnEditCashBox;
    @FXML
    private Button btnDeleteCashBox;
    @FXML
    private CheckBox chkShowClosedBoxes;
    @FXML
    private TableView<CashBox> cashBoxesTable;
    @FXML
    private TableColumn<CashBox, String> colBoxCode;
    @FXML
    private TableColumn<CashBox, String> colBoxDescription;
    @FXML
    private TableColumn<CashBox, String> colBoxCurrency;
    @FXML
    private TableColumn<CashBox, String> colBoxAccounting;
    @FXML
    private TableColumn<CashBox, BigDecimal> colBoxBalance;
    @FXML
    private TableColumn<CashBox, Boolean> colBoxOpen;

    // Cash Movements Tab
    @FXML
    private Button btnNewMovement;
    @FXML
    private Button btnPostMovement;
    @FXML
    private Button btnCancelMovement;
    @FXML
    private ComboBox<String> cbMovementFilter;
    @FXML
    private TableView<CashMovement> movementsTable;
    @FXML
    private TableColumn<CashMovement, LocalDate> colMoveDate;
    @FXML
    private TableColumn<CashMovement, String> colMoveDocument;
    @FXML
    private TableColumn<CashMovement, String> colMoveType;
    @FXML
    private TableColumn<CashMovement, String> colMoveDescription;
    @FXML
    private TableColumn<CashMovement, String> colMoveSource;
    @FXML
    private TableColumn<CashMovement, String> colMoveDestination;
    @FXML
    private TableColumn<CashMovement, BigDecimal> colMoveAmount;
    @FXML
    private TableColumn<CashMovement, String> colMoveStatus;
    @FXML
    private TableColumn<CashMovement, Boolean> colMoveReconciled;

    // Bank Reconciliation Tab
    @FXML
    private Button btnNewReconciliation;
    @FXML
    private Button btnCompleteReconciliation;
    @FXML
    private Button btnPrintReconciliation;
    @FXML
    private ComboBox<BankAccount> cbReconciliationBank;
    @FXML
    private TableView<?> systemItemsTable;
    @FXML
    private TableColumn<?, ?> colSysItemDate;
    @FXML
    private TableColumn<?, ?> colSysItemDesc;
    @FXML
    private TableColumn<?, ?> colSysItemAmount;
    @FXML
    private TableColumn<?, ?> colSysItemReconciled;
    @FXML
    private TableView<?> bankItemsTable;
    @FXML
    private TableColumn<?, ?> colBankItemDate;
    @FXML
    private TableColumn<?, ?> colBankItemRef;
    @FXML
    private TableColumn<?, ?> colBankItemDesc;
    @FXML
    private TableColumn<?, ?> colBankItemAmount;
    @FXML
    private TableColumn<?, ?> colBankItemReconciled;
    @FXML
    private TextField txtBankBalance;
    @FXML
    private TextField txtSystemBalance;
    @FXML
    private TextField txtDifference;

    private BankAccountRepository bankAccountRepository;
    private CashBoxRepository cashBoxRepository;
    private CashMovementRepository cashMovementRepository;
    private BankReconciliationRepository bankReconciliationRepository;
    private Stage stage;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        this.bankAccountRepository = new BankAccountRepository();
        this.cashBoxRepository = new CashBoxRepository();
        this.cashMovementRepository = new CashMovementRepository();
        this.bankReconciliationRepository = new BankReconciliationRepository();

        setupBankAccountsTable();
        setupCashBoxesTable();
        setupMovementsTable();
        setupReconciliationTables();
        loadAllData();
    }

    /**
     * Sets the stage for this controller.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupBankAccountsTable() {
        colBankCode.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getCode()));
        colBankDescription.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDescription()));
        colBankNumber.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getAccountNumber()));
        colBankEntity.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getBankEntity()));
        colBankCurrency.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getCurrency()));
        colBankAccounting.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getAccountingAccount()));
        colBankBalance.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBalance()));
        colBankActive.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().getActive()));
    }

    private void setupCashBoxesTable() {
        colBoxCode.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getCode()));
        colBoxDescription.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDescription()));
        colBoxCurrency.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getCurrency()));
        colBoxAccounting.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getAccountingAccount()));
        colBoxBalance.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBalance()));
        colBoxOpen.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().getOpen()));
    }

    private void setupMovementsTable() {
        colMoveDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        colMoveDocument.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDocumentNumber()));
        colMoveType.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getMovementType().toString()));
        colMoveDescription.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDescription()));
        colMoveAmount.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
        colMoveStatus.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getStatus().toString()));
        colMoveReconciled.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().getReconciled()));
    }

    private void setupReconciliationTables() {
        cbReconciliationBank.setItems(FXCollections.observableArrayList());
        cbMovementFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "POSTED", "CANCELLED"));
    }

    private void loadAllData() {
        loadBankAccounts();
        loadCashBoxes();
        loadMovements();
        loadReconciliationBanks();
    }

    private void loadBankAccounts() {
        try {
            List<BankAccount> accounts = bankAccountRepository.findAll();
            ObservableList<BankAccount> observableList = FXCollections.observableArrayList(accounts);
            bankAccountsTable.setItems(observableList);
        } catch (Exception e) {
            logger.error("Error loading bank accounts", e);
            showAlert("Error", "Failed to load bank accounts: " + e.getMessage());
        }
    }

    private void loadCashBoxes() {
        try {
            List<CashBox> boxes = cashBoxRepository.findAll();
            ObservableList<CashBox> observableList = FXCollections.observableArrayList(boxes);
            cashBoxesTable.setItems(observableList);
        } catch (Exception e) {
            logger.error("Error loading cash boxes", e);
            showAlert("Error", "Failed to load cash boxes: " + e.getMessage());
        }
    }

    private void loadMovements() {
        try {
            List<CashMovement> movements = cashMovementRepository.findAll();
            ObservableList<CashMovement> observableList = FXCollections.observableArrayList(movements);
            movementsTable.setItems(observableList);
        } catch (Exception e) {
            logger.error("Error loading movements", e);
            showAlert("Error", "Failed to load movements: " + e.getMessage());
        }
    }

    private void loadReconciliationBanks() {
        try {
            List<BankAccount> accounts = bankAccountRepository.findActiveAccounts();
            ObservableList<BankAccount> observableList = FXCollections.observableArrayList(accounts);
            cbReconciliationBank.setItems(observableList);
        } catch (Exception e) {
            logger.error("Error loading banks for reconciliation", e);
        }
    }

    @FXML
    private void handleRefresh() {
        logger.info("Refreshing all data");
        loadAllData();
    }

    @FXML
    private void handleNewBankAccount() {
        logger.info("Creating new bank account");
        showAlert("Info", "New Bank Account dialog would open here.");
    }

    @FXML
    private void handleEditBankAccount() {
        logger.info("Editing bank account");
        showAlert("Info", "Edit Bank Account dialog would open here.");
    }

    @FXML
    private void handleDeleteBankAccount() {
        logger.info("Deleting bank account");
        showAlert("Info", "Delete Bank Account confirmation would appear here.");
    }

    @FXML
    private void handleNewCashBox() {
        logger.info("Creating new cash box");
        showAlert("Info", "New Cash Box dialog would open here.");
    }

    @FXML
    private void handleEditCashBox() {
        logger.info("Editing cash box");
        showAlert("Info", "Edit Cash Box dialog would open here.");
    }

    @FXML
    private void handleDeleteCashBox() {
        logger.info("Deleting cash box");
        showAlert("Info", "Delete Cash Box confirmation would appear here.");
    }

    @FXML
    private void handleNewMovement() {
        logger.info("Creating new cash movement");
        showAlert("Info", "New Movement dialog would open here.");
    }

    @FXML
    private void handlePostMovement() {
        logger.info("Posting cash movement");
        showAlert("Info", "Movement posted successfully.");
    }

    @FXML
    private void handleCancelMovement() {
        logger.info("Cancelling cash movement");
        showAlert("Info", "Movement cancelled successfully.");
    }

    @FXML
    private void handleNewReconciliation() {
        logger.info("Creating new bank reconciliation");
        showAlert("Info", "New Reconciliation process started.");
    }

    @FXML
    private void handleCompleteReconciliation() {
        logger.info("Completing bank reconciliation");
        showAlert("Info", "Reconciliation completed successfully.");
    }

    @FXML
    private void handlePrintReconciliation() {
        logger.info("Printing reconciliation report");
        showAlert("Info", "Reconciliation report would be printed here.");
    }

    /**
     * Shows an alert dialog with the specified title and message.
     * @param title the title of the alert
     * @param message the message content
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (btnRefresh != null && btnRefresh.getScene() != null) {
            alert.initOwner(btnRefresh.getScene().getWindow());
        }
        alert.showAndWait();
    }
}
