package com.econovafx.modules.inventory.ui;

import com.econovafx.modules.inventory.model.InventoryCategory;
import com.econovafx.modules.inventory.model.InventoryItem;
import com.econovafx.modules.inventory.model.Warehouse;
import com.econovafx.modules.inventory.service.InventoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for inventory item dialog (create/edit).
 */
public class InventoryItemDialogController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryItemDialogController.class);

    @FXML
    private Label titleLabel;

    @FXML
    private TextField codeField;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<InventoryCategory> categoryCombo;

    @FXML
    private ComboBox<Warehouse> warehouseCombo;

    @FXML
    private TextField unitOfMeasureField;

    @FXML
    private TextField unitCostField;

    @FXML
    private TextField salePriceField;

    @FXML
    private TextField minStockField;

    @FXML
    private TextField maxStockField;

    @FXML
    private TextField barcodeField;

    @FXML
    private TextField contraAccountField;

    @FXML
    private TextField costCenterField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private final InventoryService inventoryService;
    private InventoryItem currentItem;
    private boolean saved = false;

    public InventoryItemDialogController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Initialize the dialog for creating a new item.
     */
    public void initNew() {
        titleLabel.setText("New Product");
        currentItem = new InventoryItem();
        loadCombos();
    }

    /**
     * Initialize the dialog for editing an existing item.
     */
    public void initEdit(InventoryItem item) {
        titleLabel.setText("Edit Product");
        this.currentItem = item;
        loadCombos();
        populateFields();
    }

    private void loadCombos() {
        try {
            List<InventoryCategory> categories = inventoryService.getAllCategories();
            categoryCombo.getItems().addAll(categories);

            List<Warehouse> warehouses = inventoryService.getAllWarehouses();
            warehouseCombo.getItems().addAll(warehouses);

            if (!categories.isEmpty()) {
                categoryCombo.getSelectionModel().selectFirst();
            }
            if (!warehouses.isEmpty()) {
                warehouseCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error loading combos", e);
        }
    }

    private void populateFields() {
        codeField.setText(currentItem.getCode());
        nameField.setText(currentItem.getName());
        descriptionField.setText(currentItem.getDescription());
        
        if (currentItem.getCategory() != null) {
            categoryCombo.getSelectionModel().select(currentItem.getCategory());
        }
        
        if (currentItem.getSupplier() != null) {
            // Supplier combo would be added here if needed
        }
        
        unitOfMeasureField.setText(currentItem.getUnitOfMeasure());
        unitCostField.setText(currentItem.getUnitCost().toString());
        salePriceField.setText(currentItem.getSalePrice().toString());
        minStockField.setText(currentItem.getMinimumStock().toString());
        maxStockField.setText(currentItem.getMaximumStock().toString());
        barcodeField.setText(currentItem.getBarcode());
        contraAccountField.setText(currentItem.getContraAccountCode());
        costCenterField.setText(currentItem.getCostCenterCode());
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
            if (codeField.getText() == null || codeField.getText().trim().isEmpty()) {
                showError("Code is required");
                return;
            }
            if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                showError("Name is required");
                return;
            }

            // Parse numeric fields
            BigDecimal unitCost, salePrice, minStock, maxStock;
            try {
                unitCost = new BigDecimal(unitCostField.getText().trim());
                salePrice = new BigDecimal(salePriceField.getText().trim());
                minStock = new BigDecimal(minStockField.getText().trim());
                maxStock = new BigDecimal(maxStockField.getText().trim());
            } catch (NumberFormatException e) {
                showError("Invalid numeric value. Please check cost, price, and stock fields.");
                return;
            }

            // Get selected category and warehouse
            InventoryCategory category = categoryCombo.getValue();
            Warehouse warehouse = warehouseCombo.getValue();

            if (category == null) {
                showError("Please select a category");
                return;
            }
            if (warehouse == null) {
                showError("Please select a warehouse");
                return;
            }

            // Populate item
            currentItem.setCode(codeField.getText().trim());
            currentItem.setName(nameField.getText().trim());
            currentItem.setDescription(descriptionField.getText().trim());
            currentItem.setCategory(category);
            currentItem.setUnitOfMeasure(unitOfMeasureField.getText().trim());
            currentItem.setUnitCost(unitCost);
            currentItem.setSalePrice(salePrice);
            currentItem.setMinimumStock(minStock);
            currentItem.setMaximumStock(maxStock);
            currentItem.setBarcode(barcodeField.getText().trim());
            currentItem.setContraAccountCode(contraAccountField.getText().trim());
            currentItem.setCostCenterCode(costCenterField.getText().trim());

            // Save via service
            if (currentItem.getId() == null) {
                inventoryService.saveItem(currentItem, null);
                logger.info("Product created: {}", currentItem.getCode());
            } else {
                inventoryService.updateItem(currentItem, null);
                logger.info("Product updated: {}", currentItem.getId());
            }

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Error saving product", e);
            showError("Error saving product: " + e.getMessage());
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

    public InventoryItem getSavedItem() {
        return saved ? currentItem : null;
    }
}
