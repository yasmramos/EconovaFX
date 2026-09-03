package com.econovafx.modules.cash.controller;

import com.econovafx.modules.cash.model.CashBox;
import com.econovafx.modules.cash.model.CashMovement;
import com.econovafx.modules.bank.model.BankAccount;
import com.econovafx.modules.bank.model.BankReconciliation;
import com.econovafx.modules.bank.model.ReconciliationItem;
import com.econovafx.modules.bank.repository.BankAccountRepository;
import com.econovafx.modules.bank.repository.BankReconciliationRepository;
import com.econovafx.modules.cash.repository.CashBoxRepository;
import com.econovafx.modules.cash.repository.CashMovementRepository;
import com.econovafx.modules.cash.service.CashMovementService;
import com.econovafx.modules.bank.service.BankReconciliationService;
import com.econovafx.modules.core.service.ExportService;
import com.econovafx.modules.core.security.SecurityUtil;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    private DatePicker dpReconciliationFrom;
    @FXML
    private DatePicker dpReconciliationTo;
    @FXML
    private TableView<ReconciliationItem> systemItemsTable;
    @FXML
    private TableColumn<ReconciliationItem, LocalDate> colSysItemDate;
    @FXML
    private TableColumn<ReconciliationItem, String> colSysItemDesc;
    @FXML
    private TableColumn<ReconciliationItem, BigDecimal> colSysItemAmount;
    @FXML
    private TableColumn<ReconciliationItem, Boolean> colSysItemReconciled;
    @FXML
    private TableView<ReconciliationItem> bankItemsTable;
    @FXML
    private TableColumn<ReconciliationItem, LocalDate> colBankItemDate;
    @FXML
    private TableColumn<ReconciliationItem, String> colBankItemRef;
    @FXML
    private TableColumn<ReconciliationItem, String> colBankItemDesc;
    @FXML
    private TableColumn<ReconciliationItem, BigDecimal> colBankItemAmount;
    @FXML
    private TableColumn<ReconciliationItem, Boolean> colBankItemReconciled;
    @FXML
    private TextField txtBankBalance;
    @FXML
    private TextField txtSystemBalance;
    @FXML
    private TextField txtDifference;
    @FXML
    private Label lblReconciliationStatus;

    @Inject
    private BankAccountRepository bankAccountRepository;
    @Inject
    private CashBoxRepository cashBoxRepository;
    @Inject
    private CashMovementRepository cashMovementRepository;
    @Inject
    private BankReconciliationRepository bankReconciliationRepository;
    @Inject
    private CashMovementService cashMovementService;
    @Inject
    private BankReconciliationService bankReconciliationService;
    @Inject
    private ExportService exportService;
    private Stage stage;
    
    // Current reconciliation being worked on
    private BankReconciliation currentReconciliation;

    /**
     * Constructor for dependency injection.
     */
    @Inject
    public CashModuleController(
            BankAccountRepository bankAccountRepository,
            CashBoxRepository cashBoxRepository,
            CashMovementRepository cashMovementRepository,
            BankReconciliationRepository bankReconciliationRepository,
            CashMovementService cashMovementService,
            BankReconciliationService bankReconciliationService,
            ExportService exportService) {
        this.bankAccountRepository = bankAccountRepository;
        this.cashBoxRepository = cashBoxRepository;
        this.cashMovementRepository = cashMovementRepository;
        this.bankReconciliationRepository = bankReconciliationRepository;
        this.cashMovementService = cashMovementService;
        this.bankReconciliationService = bankReconciliationService;
        this.exportService = exportService;
    }

    /**
     * Default constructor required by FXMLLoader.
     */
    public CashModuleController() {
        // Fields will be initialized by FXMLLoader via initialize()
    }

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Repositories and services are injected via constructor when using DI
        // For FXML-loaded controllers, Avaje Inject will inject the dependencies automatically
        if (cashMovementService == null) {
            // Fallback for manual instantiation - should not happen with proper DI setup
            logger.warn("Dependencies not injected. Ensure controller is loaded through DI container.");
            return;
        }

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
        
        // Setup system items table columns
        colSysItemDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        colSysItemDesc.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDescription()));
        colSysItemAmount.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
        colSysItemReconciled.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().getReconciled()));
        
        // Setup bank items table columns
        colBankItemDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));
        colBankItemRef.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getBankReference() != null ? cellData.getValue().getBankReference() : ""));
        colBankItemDesc.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                cellData.getValue().getDescription()));
        colBankItemAmount.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
        colBankItemReconciled.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().getReconciled()));
        
        // Add selection listeners to update difference in real-time
        systemItemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDifference();
            }
        });
        
        bankItemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDifference();
            }
        });

        // Parse the user-entered bank balance back into the current reconciliation
        // so the difference reflects the value typed from the bank statement.
        txtBankBalance.textProperty().addListener((obs, oldVal, newVal) -> applyBankBalance(newVal));
    }

    /**
     * Parses the value typed in {@link #txtBankBalance} into the current
     * reconciliation and recomputes the difference. Invalid input is ignored.
     */
    private void applyBankBalance(String value) {
        if (currentReconciliation == null) {
            return;
        }
        try {
            currentReconciliation.setBankBalance(new BigDecimal(value.trim()));
        } catch (NumberFormatException | NullPointerException e) {
            // Ignore invalid/partial input; keep previous balance.
            return;
        }
        updateDifference();
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
        BankAccount selected = bankAccountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a bank account to edit.");
            return;
        }
        logger.info("Editing bank account: {}", selected.getCode());
        showAlert("Info", "Edit Bank Account dialog would open here.");
    }

    @FXML
    private void handleDeleteBankAccount() {
        BankAccount selected = bankAccountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a bank account to delete.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete bank account " + selected.getDescription() + "?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // TODO: Implement delete via service when available
                showAlert("Success", "Bank account deleted successfully.");
                loadBankAccounts();
            } catch (Exception e) {
                logger.error("Error deleting bank account", e);
                showAlert("Error", "Failed to delete bank account: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNewCashBox() {
        logger.info("Creating new cash box");
        showAlert("Info", "New Cash Box dialog would open here.");
    }

    @FXML
    private void handleEditCashBox() {
        CashBox selected = cashBoxesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a cash box to edit.");
            return;
        }
        logger.info("Editing cash box: {}", selected.getCode());
        showAlert("Info", "Edit Cash Box dialog would open here.");
    }

    @FXML
    private void handleDeleteCashBox() {
        CashBox selected = cashBoxesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a cash box to delete.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete cash box " + selected.getDescription() + "?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // TODO: Implement delete via service when available
                showAlert("Success", "Cash box deleted successfully.");
                loadCashBoxes();
            } catch (Exception e) {
                logger.error("Error deleting cash box", e);
                showAlert("Error", "Failed to delete cash box: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNewMovement() {
        logger.info("Creating new cash movement");
        showAlert("Info", "New Movement dialog would open here.");
    }

    @FXML
    private void handlePostMovement() {
        CashMovement selected = movementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a movement to post.");
            return;
        }
        
        try {
            String currentUser = "system"; // TODO: Get from session
            CashMovement posted = cashMovementService.postMovement(selected.getId(), currentUser);
            showAlert("Success", "Movement posted successfully.");
            loadMovements();
        } catch (IllegalStateException e) {
            showAlert("Error", e.getMessage());
        } catch (Exception e) {
            logger.error("Error posting movement", e);
            showAlert("Error", "Failed to post movement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelMovement() {
        CashMovement selected = movementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a movement to cancel.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancel");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to cancel this movement?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String currentUser = "system"; // TODO: Get from session
                cashMovementService.cancelMovement(selected.getId(), currentUser);
                showAlert("Success", "Movement cancelled successfully.");
                loadMovements();
            } catch (IllegalStateException e) {
                showAlert("Error", e.getMessage());
            } catch (Exception e) {
                logger.error("Error cancelling movement", e);
                showAlert("Error", "Failed to cancel movement: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNewReconciliation() {
        BankAccount selectedBank = cbReconciliationBank.getValue();
        if (selectedBank == null) {
            showAlert("Warning", "Please select a bank account for reconciliation.");
            return;
        }
        
        LocalDate fromDate = dpReconciliationFrom.getValue();
        LocalDate toDate = dpReconciliationTo.getValue();
        
        if (fromDate == null || toDate == null) {
            showAlert("Warning", "Please select date range for reconciliation.");
            return;
        }
        
        try {
            BankReconciliation reconciliation = new BankReconciliation();
            reconciliation.setBankAccountId(selectedBank.getId());
            reconciliation.setStatementDate(toDate);
            reconciliation.setSystemBalance(selectedBank.getBalance());
            reconciliation.setBankBalance(BigDecimal.ZERO); // To be filled from statement
            reconciliation.setStatus(BankReconciliation.Status.IN_PROGRESS);
            
            BankReconciliation saved = bankReconciliationService.createReconciliation(reconciliation);
            
            // Set as current reconciliation and update UI
            currentReconciliation = saved;
            updateReconciliationUI();
            
            showAlert("Success", "Reconciliation process started.\n" +
                "Reconciliation ID: " + saved.getId() + "\n" +
                "System Balance: " + saved.getSystemBalance().toPlainString());
            logger.info("Created reconciliation: {}", saved.getId());
            
        } catch (Exception e) {
            logger.error("Error creating reconciliation", e);
            showAlert("Error", "Failed to start reconciliation: " + e.getMessage());
        }
    }

    @FXML
    private void handleCompleteReconciliation() {
        if (currentReconciliation == null) {
            showAlert("Warning", "No reconciliation in progress. Please create a new reconciliation first.");
            return;
        }
        
        if (currentReconciliation.getStatus() != BankReconciliation.Status.IN_PROGRESS) {
            showAlert("Warning", "This reconciliation is already " + currentReconciliation.getStatus());
            return;
        }
        
        try {
            // Persist the user-entered bank balance before validation, since the
            // service re-fetches the reconciliation from the repository.
            applyBankBalance(txtBankBalance.getText());
            bankReconciliationService.createReconciliation(currentReconciliation);

            // Get current user from security context
            com.econovafx.modules.core.model.User currentUserObj = SecurityUtil.getCurrentUser();
            String currentUser = (currentUserObj != null) ? currentUserObj.getUsername() : "system";
            if (currentUser == null || currentUser.isEmpty()) {
                currentUser = "system";
            }
            
            // Validate and complete the reconciliation
            BankReconciliation completed = bankReconciliationService.completeReconciliation(
                currentReconciliation.getId(), 
                currentUser
            );
            
            showAlert("Success", "Reconciliation completed successfully.\n" +
                "Reconciled Balance: " + completed.getReconciledBalance().toPlainString());
            
            // Update UI
            currentReconciliation = completed;
            updateReconciliationUI();
            loadAllData();
            
        } catch (IllegalStateException e) {
            // Reconciliation doesn't balance - show the difference
            BigDecimal difference = calculateDifference(currentReconciliation);
            showAlert("Error", "Reconciliation does not balance.\n" +
                "Difference: " + difference.toPlainString() + "\n\n" +
                "Please adjust items until the difference is zero.");
            logger.warn("Reconciliation balance validation failed: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error completing reconciliation", e);
            showAlert("Error", "Failed to complete reconciliation: " + e.getMessage());
        }
    }

    @FXML
    private void handlePrintReconciliation() {
        if (currentReconciliation == null) {
            showAlert("Warning", "No reconciliation selected to print.");
            return;
        }
        
        try {
            // Export reconciliation to PDF using ExportService
            byte[] pdfContent = exportService.exportBankReconciliationToPdf(currentReconciliation);
            
            // Show save dialog to user
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Bank Reconciliation Report");
            fileChooser.setInitialFileName("BankReconciliation_" + 
                currentReconciliation.getReconciliationNumber() + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            Stage stage = (Stage) btnPrintReconciliation.getScene().getWindow();
            java.io.File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    fos.write(pdfContent);
                    logger.info("Saved bank reconciliation report to: {}", file.getAbsolutePath());
                    showAlert("Success", "Bank reconciliation report saved successfully.\n" +
                        "File: " + file.getAbsolutePath());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error generating reconciliation report", e);
            showAlert("Error", "Failed to generate report: " + e.getMessage());
        }
    }
    
    /**
     * Calculates the difference between adjusted bank balance and adjusted system balance.
     * @param reconciliation the reconciliation to calculate difference for
     * @return the difference amount (positive if bank > system, negative if system > bank)
     */
    private BigDecimal calculateDifference(BankReconciliation reconciliation) {
        BigDecimal adjustedBankBalance = reconciliation.getBankBalance();
        for (ReconciliationItem item : reconciliation.getBankItems()) {
            adjustedBankBalance = adjustedBankBalance.add(item.getAmount());
        }
        
        BigDecimal adjustedSystemBalance = reconciliation.getSystemBalance();
        for (ReconciliationItem item : reconciliation.getSystemItems()) {
            adjustedSystemBalance = adjustedSystemBalance.subtract(item.getAmount());
        }
        
        return adjustedBankBalance.subtract(adjustedSystemBalance);
    }
    
    /**
     * Updates the difference display based on current reconciliation items.
     */
    private void updateDifference() {
        if (currentReconciliation == null) {
            return;
        }
        
        BigDecimal difference = calculateDifference(currentReconciliation);
        txtDifference.setText(difference.toPlainString());
        
        // Color code the difference
        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            txtDifference.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            txtDifference.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Updates the reconciliation UI with current reconciliation data.
     */
    private void updateReconciliationUI() {
        if (currentReconciliation == null) {
            return;
        }
        
        txtBankBalance.setText(currentReconciliation.getBankBalance().toPlainString());
        txtSystemBalance.setText(currentReconciliation.getSystemBalance().toPlainString());
        
        // Populate tables
        ObservableList<ReconciliationItem> systemItems = 
            FXCollections.observableArrayList(currentReconciliation.getSystemItems());
        systemItemsTable.setItems(systemItems);
        
        ObservableList<ReconciliationItem> bankItems = 
            FXCollections.observableArrayList(currentReconciliation.getBankItems());
        bankItemsTable.setItems(bankItems);
        
        // Update status label
        lblReconciliationStatus.setText("Status: " + currentReconciliation.getStatus());
        
        // Update difference
        updateDifference();
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
