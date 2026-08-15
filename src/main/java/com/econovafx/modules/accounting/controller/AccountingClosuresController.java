package com.econovafx.modules.accounting.controller;

import com.econovafx.modules.accounting.model.AccountingPeriod;
import com.econovafx.modules.accounting.service.AccountingPeriodService;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.security.SecurityUtil;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for accounting closures management view.
 * Resolution 340/2004 Compliance: Manages monthly and annual period closures.
 */
public class AccountingClosuresController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AccountingClosuresController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AccountingPeriodService accountingPeriodService;

    @FXML
    private VBox contentArea;

    // Buttons
    @FXML
    private Button btnVerPeriodos;
    @FXML
    private Button btnCierreMensual;
    @FXML
    private Button btnCierreAnual;

    // TableView and Columns
    @FXML
    private TableView<AccountingPeriod> closuresTable;
    @FXML
    private TableColumn<AccountingPeriod, String> colTipo;
    @FXML
    private TableColumn<AccountingPeriod, String> colPeriodo;
    @FXML
    private TableColumn<AccountingPeriod, String> colFechaCierre;
    @FXML
    private TableColumn<AccountingPeriod, String> colUsuario;
    @FXML
    private TableColumn<AccountingPeriod, String> colEstado;
    @FXML
    private TableColumn<AccountingPeriod, Void> colAcciones;

    @Inject
    public AccountingClosuresController(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("AccountingClosuresController initialized");
        setupTableColumns();
        loadClosuresData();
    }

    /**
     * Setup table columns with cell value factories.
     */
    private void setupTableColumns() {
        colTipo.setCellValueFactory(cellData -> {
            String typeStr = cellData.getValue().getType() != null ? 
                cellData.getValue().getType().toString() : "";
            return new javafx.beans.property.SimpleStringProperty(typeStr);
        });
        
        colPeriodo.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        colFechaCierre.setCellValueFactory(cellData -> {
            LocalDate closedDate = cellData.getValue().getClosedDate();
            String dateStr = closedDate != null ? closedDate.format(DATE_FORMATTER) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        
        colUsuario.setCellValueFactory(cellData -> {
            String closedBy = cellData.getValue().getClosedBy();
            return new javafx.beans.property.SimpleStringProperty(closedBy != null ? closedBy : "");
        });
        
        colEstado.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));

        // Setup action buttons column
        setupActionColumn();
    }

    /**
     * Setup action column with buttons for each row.
     */
    private void setupActionColumn() {
        colAcciones.setCellFactory(param -> new TableCell<AccountingPeriod, Void>() {
            private final HBox hbox = new HBox(5);
            private final Button btnView = createActionButton("Ver", "bg-blue-500");

            {
                hbox.getChildren().add(btnView);
                
                btnView.setOnAction(event -> {
                    AccountingPeriod period = getTableView().getItems().get(getIndex());
                    handleViewPeriod(period);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    private Button createActionButton(String text, String colorClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll(colorClass, "text-white", "px-2", "py-1", "rounded-md", "cursor-hand");
        return button;
    }

    /**
     * Load and display accounting closures data from all periods.
     */
    private void loadClosuresData() {
        logger.debug("Loading accounting closures data");
        try {
            List<AccountingPeriod> allPeriods = accountingPeriodService.getAllPeriods();
            closuresTable.getItems().setAll(allPeriods);
            logger.info("Loaded {} accounting periods", allPeriods.size());
        } catch (Exception e) {
            logger.error("Error loading closures data", e);
            showAlert("Error", "Failed to load closures data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle view open periods action.
     */
    @FXML
    private void handleVerPeriodos() {
        logger.info("Viewing open periods");
        try {
            Optional<AccountingPeriod> currentOpen = accountingPeriodService.getCurrentOpenPeriod();
            
            if (currentOpen.isPresent()) {
                AccountingPeriod period = currentOpen.get();
                StringBuilder message = new StringBuilder();
                message.append("Current Open Period:\n\n");
                message.append("Name: ").append(period.getName()).append("\n");
                message.append("Type: ").append(period.getType()).append("\n");
                message.append("Start Date: ").append(period.getStartDate().format(DATE_FORMATTER)).append("\n");
                message.append("End Date: ").append(period.getEndDate().format(DATE_FORMATTER)).append("\n");
                message.append("Status: ").append(period.getStatus()).append("\n");
                
                showAlert("Open Period Information", message.toString(), Alert.AlertType.INFORMATION);
            } else {
                showAlert("No Open Period", "There is currently no open accounting period.", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            logger.error("Error viewing open periods", e);
            showAlert("Error", "Failed to retrieve open period information: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle monthly closure execution.
     */
    @FXML
    private void handleCierreMensual() {
        logger.info("Initiating monthly closure process");
        
        // Show backup warning as per Resolution 340/2004 requirements
        Alert backupWarning = new Alert(Alert.AlertType.WARNING);
        backupWarning.setTitle("Backup Warning");
        FontIcon warningIcon = new FontIcon(MaterialDesignA.ALERT_CIRCLE);
        warningIcon.setIconSize(24);
        warningIcon.setIconColor(javafx.scene.paint.Color.ORANGE);
        backupWarning.setGraphic(new Label("Important: Backup Required", warningIcon));
        backupWarning.setContentText("Before executing a monthly closure, ensure you have created a backup of the database. " +
                                   "Resolution 340/2004 requires data integrity preservation.\n\n" +
                                   "Do you want to proceed?");
        
        Optional<ButtonType> result = backupWarning.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            logger.info("Monthly closure cancelled by user");
            return;
        }

        // Get current open period
        Optional<AccountingPeriod> currentOpenOpt = accountingPeriodService.getCurrentOpenPeriod();
        
        if (currentOpenOpt.isEmpty()) {
            showAlert("No Open Period", "There is no open period to close.", Alert.AlertType.WARNING);
            return;
        }

        AccountingPeriod period = currentOpenOpt.get();
        
        // Validate it's a monthly period
        if (!period.isMonthly()) {
            showAlert("Invalid Period Type", 
                     "The current open period is not a monthly period: " + period.getType(), 
                     Alert.AlertType.WARNING);
            return;
        }

        // Confirm closure
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Monthly Closure");
        confirmAlert.setHeaderText("Execute Monthly Closure");
        confirmAlert.setContentText("Are you sure you want to close the monthly period '" + period.getName() + 
                                   "'?\n\nThis will:\n" +
                                   "- Validate that Cash/Bank and Inventory modules are closed\n" +
                                   "- Generate closing entries\n" +
                                   "- Lock the period against new transactions\n\n" +
                                   "This action cannot be undone per Resolution 340/2004.");
        
        Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            logger.info("Monthly closure cancelled by user confirmation");
            return;
        }

        try {
            // Get authenticated user from security context
            User currentUser = SecurityUtil.getCurrentUser();
            String username = (currentUser != null) ? currentUser.getUsername() : "system";
            
            // Execute monthly closure
            accountingPeriodService.closeMonthlyPeriod(period.getId(), username, "Monthly closure executed from UI");
            
            logger.info("Monthly period closed successfully: {} by {}", period.getName(), username);
            showAlert("Success", "Monthly period '" + period.getName() + "' has been closed successfully.", Alert.AlertType.INFORMATION);
            
            // Refresh data
            loadClosuresData();
            
        } catch (IllegalStateException e) {
            logger.error("Monthly closure failed - validation error: {}", e.getMessage());
            showAlert("Closure Failed", 
                     "Cannot close period: " + e.getMessage() + 
                     "\n\nPlease ensure all dependent modules (Cash/Bank, Inventory) are closed first.", 
                     Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Monthly closure failed", e);
            showAlert("Error", "Failed to execute monthly closure: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle annual closure execution.
     */
    @FXML
    private void handleCierreAnual() {
        logger.info("Initiating annual closure process");
        
        // Show backup warning as per Resolution 340/2004 requirements
        Alert backupWarning = new Alert(Alert.AlertType.WARNING);
        backupWarning.setTitle("Backup Warning");
        FontIcon warningIconAnnual = new FontIcon(MaterialDesignA.ALERT_CIRCLE);
        warningIconAnnual.setIconSize(24);
        warningIconAnnual.setIconColor(javafx.scene.paint.Color.ORANGE);
        backupWarning.setGraphic(new Label("Important: Backup Required", warningIconAnnual));
        backupWarning.setContentText("Before executing an annual closure, ensure you have created a backup of the database. " +
                                   "Resolution 340/2004 requires data integrity preservation.\n\n" +
                                   "Do you want to proceed?");
        
        Optional<ButtonType> result = backupWarning.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            logger.info("Annual closure cancelled by user");
            return;
        }

        // For annual closure, we need to find or select an annual period
        // For simplicity, we'll check if there's an open annual period for the current year
        int currentYear = LocalDate.now().getYear();
        
        List<AccountingPeriod> allPeriods = accountingPeriodService.getAllPeriods();
        Optional<AccountingPeriod> annualPeriodOpt = allPeriods.stream()
            .filter(p -> p.getType() == AccountingPeriod.PeriodType.ANNUAL && 
                        p.getStartDate().getYear() == currentYear &&
                        p.isOpen())
            .findFirst();
        
        if (annualPeriodOpt.isEmpty()) {
            showAlert("No Annual Period", 
                     "No open annual period found for year " + currentYear + ".\n" +
                     "Please create an annual period first.", 
                     Alert.AlertType.WARNING);
            return;
        }

        AccountingPeriod period = annualPeriodOpt.get();

        // Confirm closure
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Annual Closure");
        confirmAlert.setHeaderText("Execute Annual Closure");
        confirmAlert.setContentText("Are you sure you want to close the annual period '" + period.getName() + 
                                   "'?\n\nThis will:\n" +
                                   "- Optionally close all monthly periods within this year\n" +
                                   "- Generate annual closing entries\n" +
                                   "- Lock the period against new transactions\n\n" +
                                   "This action cannot be undone per Resolution 340/2004.");
        
        Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            logger.info("Annual closure cancelled by user confirmation");
            return;
        }

        try {
            // Get authenticated user from security context
            User currentUser = SecurityUtil.getCurrentUser();
            String username = (currentUser != null) ? currentUser.getUsername() : "system";
            
            // Execute annual closure with auto-close of related months
            accountingPeriodService.closeAnnualPeriod(period.getId(), username, "Annual closure executed from UI", true);
            
            logger.info("Annual period closed successfully: {} by {}", period.getName(), username);
            showAlert("Success", "Annual period '" + period.getName() + "' has been closed successfully.", Alert.AlertType.INFORMATION);
            
            // Refresh data
            loadClosuresData();
            
        } catch (IllegalStateException e) {
            logger.error("Annual closure failed - validation error: {}", e.getMessage());
            showAlert("Closure Failed", 
                     "Cannot close period: " + e.getMessage() + 
                     "\n\nPlease ensure all dependent modules are closed first.", 
                     Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Annual closure failed", e);
            showAlert("Error", "Failed to execute annual closure: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle view period details action.
     */
    private void handleViewPeriod(AccountingPeriod period) {
        logger.info("Viewing period details: {}", period.getName());
        
        StringBuilder message = new StringBuilder();
        message.append("Period Details:\n\n");
        message.append("Name: ").append(period.getName()).append("\n");
        message.append("Type: ").append(period.getType()).append("\n");
        message.append("Start Date: ").append(period.getStartDate().format(DATE_FORMATTER)).append("\n");
        message.append("End Date: ").append(period.getEndDate().format(DATE_FORMATTER)).append("\n");
        message.append("Status: ").append(period.getStatus()).append("\n");
        
        if (period.getClosedBy() != null) {
            message.append("Closed By: ").append(period.getClosedBy()).append("\n");
        }
        if (period.getClosedDate() != null) {
            message.append("Closed Date: ").append(period.getClosedDate().format(DATE_FORMATTER)).append("\n");
        }
        if (period.getClosingNotes() != null && !period.getClosingNotes().isEmpty()) {
            message.append("Notes: ").append(period.getClosingNotes()).append("\n");
        }
        
        showAlert("Period Details", message.toString(), Alert.AlertType.INFORMATION);
    }

    /**
     * Display an alert dialog.
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
