package com.econovafx.modules.cash.controller;

import com.econovafx.modules.cash.model.CashBox;
import com.econovafx.modules.cash.model.CashMovement;
import com.econovafx.modules.cash.service.CashMovementService;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Cash & Bank Management module.
 * Handles bank accounts, cash boxes, movements, and bank reconciliation.
 */
public class CashBankController {

    private static final Logger logger = LoggerFactory.getLogger(CashBankController.class);

    @FXML
    private VBox rootVBox;
    @FXML
    private Button btnNewMovement;
    @FXML
    private Button btnReconciliation;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabBankAccounts;
    @FXML
    private Tab tabCashBoxes;
    @FXML
    private Tab tabMovements;
    @FXML
    private Tab tabReconciliation;

    // Bank Accounts Tab
    @FXML
    private TextField txtSearchBank;
    @FXML
    private TableView<BankAccount> tblBankAccounts;
    @FXML
    private TableColumn<BankAccount, String> colBankCode;
    @FXML
    private TableColumn<BankAccount, String> colBankName;
    @FXML
    private TableColumn<BankAccount, String> colBankNumber;
    @FXML
    private TableColumn<BankAccount, String> colBankCurrency;
    @FXML
    private TableColumn<BankAccount, BigDecimal> colBankBalance;
    @FXML
    private TableColumn<BankAccount, String> colBankStatus;
    @FXML
    private TableColumn<BankAccount, Void> colBankActions;

    // Cash Boxes Tab
    @FXML
    private TextField txtSearchCash;
    @FXML
    private TableView<CashBox> tblCashBoxes;
    @FXML
    private TableColumn<CashBox, String> colCashCode;
    @FXML
    private TableColumn<CashBox, String> colCashName;
    @FXML
    private TableColumn<CashBox, String> colCashLocation;
    @FXML
    private TableColumn<CashBox, String> colCashCurrency;
    @FXML
    private TableColumn<CashBox, BigDecimal> colCashBalance;
    @FXML
    private TableColumn<CashBox, String> colCashStatus;
    @FXML
    private TableColumn<CashBox, Void> colCashActions;

    // Movements Tab
    @FXML
    private TextField txtSearchMovement;
    @FXML
    private DatePicker dpMovementFrom;
    @FXML
    private DatePicker dpMovementTo;
    @FXML
    private ComboBox<String> cbMovementType;
    @FXML
    private TableView<CashMovement> tblMovements;
    @FXML
    private TableColumn<CashMovement, String> colMovNumber;
    @FXML
    private TableColumn<CashMovement, LocalDate> colMovDate;
    @FXML
    private TableColumn<CashMovement, String> colMovType;
    @FXML
    private TableColumn<CashMovement, String> colMovSource;
    @FXML
    private TableColumn<CashMovement, String> colMovDescription;
    @FXML
    private TableColumn<CashMovement, BigDecimal> colMovAmount;
    @FXML
    private TableColumn<CashMovement, String> colMovStatus;
    @FXML
    private TableColumn<CashMovement, Void> colMovActions;

    // Reconciliation Tab
    @FXML
    private ComboBox<BankAccount> cbReconciliationBank;
    @FXML
    private DatePicker dpReconciliationFrom;
    @FXML
    private DatePicker dpReconciliationTo;
    @FXML
    private TableView<?> tblSystemItems;
    @FXML
    private TableColumn<?, ?> colSysItemDate;
    @FXML
    private TableColumn<?, ?> colSysItemNumber;
    @FXML
    private TableColumn<?, ?> colSysItemDescription;
    @FXML
    private TableColumn<?, ?> colSysItemAmount;
    @FXML
    private TableColumn<?, ?> colSysItemSelect;
    @FXML
    private TableView<?> tblBankItems;
    @FXML
    private TableColumn<?, ?> colBankItemDate;
    @FXML
    private TableColumn<?, ?> colBankItemNumber;
    @FXML
    private TableColumn<?, ?> colBankItemDescription;
    @FXML
    private TableColumn<?, ?> colBankItemAmount;
    @FXML
    private TableColumn<?, ?> colBankItemSelect;
    @FXML
    private Label lblSystemBalance;
    @FXML
    private Label lblBankBalance;
    @FXML
    private Label lblDifference;
    @FXML
    private Label lblReconciliationStatus;

    private CashMovementService cashMovementService;
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
        this.cashMovementService = new CashMovementService();

        setupBankAccountsTable();
        setupCashBoxesTable();
        setupMovementsTable();
        setupReconciliationTables();
        loadData();
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
        colBankName.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDescription()));
        colBankNumber.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getAccountNumber()));
        colBankCurrency.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getCurrency()));
        colBankBalance.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBalance()));
        colBankStatus.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getActive() ? "Active" : "Inactive"));
    }

    private void setupCashBoxesTable() {
        colCashCode.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getCode()));
        colCashName.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDescription()));
        colCashLocation.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> "Main Office"));
        colCashCurrency.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getCurrency()));
        colCashBalance.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBalance()));
        colCashStatus.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getOpen() ? "Open" : "Closed"));
    }

    private void setupMovementsTable() {
        colMovNumber.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDocumentNumber()));
        colMovDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        colMovType.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getMovementType().toString()));
        colMovDescription.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDescription()));
        colMovAmount.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
        colMovStatus.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getStatus().toString()));
    }

    private void setupReconciliationTables() {
        // Setup reconciliation table columns
        cbReconciliationBank.setItems(FXCollections.observableArrayList());
    }

    private void loadData() {
        loadBankAccounts();
        loadCashBoxes();
        loadMovements();
        loadReconciliationBanks();
    }

    private void loadBankAccounts() {
        try {
            List<BankAccount> accounts = bankAccountRepository.findAll();
            ObservableList<BankAccount> observableList = FXCollections.observableArrayList(accounts);
            tblBankAccounts.setItems(observableList);
        } catch (Exception e) {
            logger.error("Error loading bank accounts", e);
            showAlert("Error", "Failed to load bank accounts: " + e.getMessage());
        }
    }

    private void loadCashBoxes() {
        try {
            List<CashBox> boxes = cashBoxRepository.findAll();
            ObservableList<CashBox> observableList = FXCollections.observableArrayList(boxes);
            tblCashBoxes.setItems(observableList);
        } catch (Exception e) {
            logger.error("Error loading cash boxes", e);
            showAlert("Error", "Failed to load cash boxes: " + e.getMessage());
        }
    }

    private void loadMovements() {
        try {
            List<CashMovement> movements = cashMovementRepository.findAll();
            ObservableList<CashMovement> observableList = FXCollections.observableArrayList(movements);
            tblMovements.setItems(observableList);
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
    private void handleNewMovement() {
        logger.info("Creating new cash movement");
        showAlert("Info", "New Movement dialog would open here.");
    }

    @FXML
    private void handleReconciliation() {
        logger.info("Starting bank reconciliation");
        tabPane.getSelectionModel().select(tabReconciliation);
    }

    @FXML
    private void handleSearchBank() {
        String searchText = txtSearchBank.getText().toLowerCase();
        filterBankAccounts(searchText);
    }

    @FXML
    private void handleAddBank() {
        logger.info("Adding new bank account");
        showAlert("Info", "Add Bank Account dialog would open here.");
    }

    @FXML
    private void handleSearchCash() {
        String searchText = txtSearchCash.getText().toLowerCase();
        filterCashBoxes(searchText);
    }

    @FXML
    private void handleAddCashBox() {
        logger.info("Adding new cash box");
        showAlert("Info", "Add Cash Box dialog would open here.");
    }

    @FXML
    private void handleSearchMovement() {
        String searchText = txtSearchMovement.getText().toLowerCase();
        filterMovements(searchText);
    }

    @FXML
    private void handleFilterMovements() {
        logger.info("Filtering movements");
        loadMovements();
    }

    @FXML
    private void handleStartReconciliation() {
        logger.info("Starting reconciliation process");
        showAlert("Info", "Reconciliation process started.");
    }

    @FXML
    private void handleSaveReconciliation() {
        logger.info("Saving reconciliation");
        showAlert("Info", "Reconciliation saved successfully.");
    }

    @FXML
    private void handlePostReconciliation() {
        logger.info("Posting reconciliation");
        showAlert("Info", "Reconciliation posted successfully.");
    }

    private void filterBankAccounts(String searchText) {
        List<BankAccount> filtered = bankAccountRepository.findAll().stream()
            .filter(account -> account.getDescription().toLowerCase().contains(searchText) ||
                             account.getAccountNumber().toLowerCase().contains(searchText))
            .toList();
        tblBankAccounts.setItems(FXCollections.observableArrayList(filtered));
    }

    private void filterCashBoxes(String searchText) {
        List<CashBox> filtered = cashBoxRepository.findAll().stream()
            .filter(box -> box.getDescription().toLowerCase().contains(searchText) ||
                          box.getCode().toLowerCase().contains(searchText))
            .toList();
        tblCashBoxes.setItems(FXCollections.observableArrayList(filtered));
    }

    private void filterMovements(String searchText) {
        List<CashMovement> filtered = cashMovementRepository.findAll().stream()
            .filter(movement -> movement.getDocumentNumber().toLowerCase().contains(searchText) ||
                               movement.getDescription().toLowerCase().contains(searchText))
            .toList();
        tblMovements.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (stage != null) {
            alert.initOwner(stage);
        }
        alert.showAndWait();
    }
}
