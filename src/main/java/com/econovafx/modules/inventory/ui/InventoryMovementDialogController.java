package com.econovafx.modules.inventory.ui;

import com.econovafx.modules.core.config.UserContext;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.inventory.model.InventoryItem;
import com.econovafx.modules.inventory.model.InventoryMovement;
import com.econovafx.modules.inventory.model.Warehouse;
import com.econovafx.modules.inventory.service.InventoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for inventory movement dialog (output/adjustment).
 */
public class InventoryMovementDialogController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryMovementDialogController.class);

    @FXML
    private Label titleLabel;

    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField documentField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea notesField;

    @FXML
    private Label currentStockLabel;

    @FXML
    private ComboBox<Warehouse> warehouseCombo;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private final InventoryService inventoryService;
    private final UserContext userContext;
    private InventoryItem currentItem;
    private MovementType movementType;
    private boolean saved = false;

    public enum MovementType {
        OUTPUT,
        ADJUSTMENT
    }

    public InventoryMovementDialogController(InventoryService inventoryService, UserContext userContext) {
        this.inventoryService = inventoryService;
        this.userContext = userContext;
    }
    
    /**
     * Gets the current logged-in user from UserContext.
     * Returns null if no user is logged in.
     */
    private User getCurrentUser() {
        return userContext != null ? userContext.getCurrentUser() : null;
    }

    /**
     * Initialize the dialog for registering an output.
     */
    public void initOutput(InventoryItem item) {
        this.currentItem = item;
        this.movementType = MovementType.OUTPUT;
        
        titleLabel.setText("Register Output");
        typeCombo.getItems().addAll("Output");
        typeCombo.getSelectionModel().selectFirst();
        typeCombo.setDisable(true);
        
        currentStockLabel.setText(item.getCurrentStock().toString());
        
        loadWarehouses();
        datePicker.setValue(LocalDate.now());
    }

    /**
     * Initialize the dialog for registering an adjustment.
     */
    public void initAdjustment(InventoryItem item) {
        this.currentItem = item;
        this.movementType = MovementType.ADJUSTMENT;
        
        titleLabel.setText("Register Adjustment");
        typeCombo.getItems().addAll("Adjustment (+/-)");
        typeCombo.getSelectionModel().selectFirst();
        typeCombo.setDisable(true);
        
        currentStockLabel.setText(item.getCurrentStock().toString());
        
        loadWarehouses();
        datePicker.setValue(LocalDate.now());
    }

    private void loadWarehouses() {
        try {
            List<Warehouse> warehouses = inventoryService.getAllWarehouses();
            warehouseCombo.getItems().addAll(warehouses);
            
            if (!warehouses.isEmpty()) {
                warehouseCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error loading warehouses", e);
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage dialogStage = (Stage) titleLabel.getScene().getWindow();
        dialogStage.close();
    }

    @FXML
    private void handleSave() {
        try {
            // Validate required fields
            if (quantityField.getText() == null || quantityField.getText().trim().isEmpty()) {
                showError("Quantity is required");
                return;
            }

            BigDecimal quantity;
            try {
                quantity = new BigDecimal(quantityField.getText().trim());
                if (movementType == MovementType.OUTPUT && quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Quantity must be greater than zero");
                    return;
                }
                if (movementType == MovementType.ADJUSTMENT && quantity.compareTo(BigDecimal.ZERO) == 0) {
                    showError("Quantity must not be zero");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Invalid quantity value");
                return;
            }

            String documentNumber = documentField.getText() != null ? documentField.getText().trim() : "DOC-" + System.currentTimeMillis();
            String notes = notesField.getText() != null ? notesField.getText().trim() : "";
            LocalDate date = datePicker.getValue();
            
            if (date == null) {
                showError("Please select a date");
                return;
            }

            Warehouse warehouse = warehouseCombo.getValue();
            if (warehouse == null) {
                showError("Please select a warehouse");
                return;
            }

            // Get current user from UserContext (injected dependency)
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                showError("No user is currently logged in. Cannot register movement.");
                return;
            }

            // Register movement based on type
            InventoryMovement movement;
            if (movementType == MovementType.OUTPUT) {
                movement = inventoryService.registerOutput(
                    currentItem,
                    warehouse,
                    quantity,
                    documentNumber,
                    notes,
                    currentUser
                );
                logger.info("Output registered: {} - Quantity: {}", documentNumber, quantity);
            } else {
                // For adjustment, allow negative values for decreases
                movement = inventoryService.registerAdjustment(
                    currentItem,
                    warehouse,
                    quantity,
                    documentNumber,
                    notes,
                    currentUser
                );
                logger.info("Adjustment registered: {} - Quantity: {}", documentNumber, quantity);
            }

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Error registering movement", e);
            showError("Error registering movement: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaved() {
        return saved;
    }
}
