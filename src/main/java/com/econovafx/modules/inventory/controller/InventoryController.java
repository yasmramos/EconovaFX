package com.econovafx.modules.inventory.controller;

import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.config.UserContext;
import com.econovafx.modules.inventory.model.InventoryItem;
import com.econovafx.modules.inventory.model.InventoryMovement;
import com.econovafx.modules.inventory.model.Warehouse;
import com.econovafx.modules.inventory.service.InventoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for inventory management view.
 * Handles CRUD operations for products and inventory movements.
 */
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @FXML
    private VBox contentArea;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnSalida;

    @FXML
    private Button btnAjuste;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<InventoryItem> productsTable;

    @FXML
    private TableColumn<InventoryItem, String> colCodigo;

    @FXML
    private TableColumn<InventoryItem, String> colNombre;

    @FXML
    private TableColumn<InventoryItem, String> colCategoria;

    @FXML
    private TableColumn<InventoryItem, String> colAlmacen;

    @FXML
    private TableColumn<InventoryItem, BigDecimal> colStock;

    @FXML
    private TableColumn<InventoryItem, BigDecimal> colCosto;

    @FXML
    private TableColumn<InventoryItem, String> colEstado;

    @FXML
    private Label lblTotalProductos;

    @FXML
    private Label lblStockBajo;

    private final InventoryService inventoryService;
    private final UserContext userContext;
    private ObservableList<InventoryItem> productList;

    public InventoryController(InventoryService inventoryService, UserContext userContext) {
        this.inventoryService = inventoryService;
        this.userContext = userContext;
        this.productList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        logger.debug("Initializing InventoryController");
        
        // Setup icons
        btnNuevo.setGraphic(FontIcon.of(MaterialDesignP.PLUS_CIRCLE, 18));
        btnEditar.setGraphic(FontIcon.of(MaterialDesignP.PENCIL, 18));
        btnEliminar.setGraphic(FontIcon.of(MaterialDesignD.DELETE_OUTLINE, 18));
        btnSalida.setGraphic(FontIcon.of(MaterialDesignA.ARROW_DOWN_BOLD_CIRCLE, 18));
        btnAjuste.setGraphic(FontIcon.of(MaterialDesignS.SCALE_BALANCE, 18));

        // Configure table columns
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategoria.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategory().getName()));
        colAlmacen.setCellValueFactory(cellData -> {
            // InventoryItem doesn't have a warehouse field in this version
            return new javafx.beans.property.SimpleStringProperty("Main Warehouse");
        });
        colStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        colEstado.setCellValueFactory(cellData -> {
            InventoryItem item = cellData.getValue();
            String status = determineStatus(item);
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        // Setup search functionality
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() >= 2) {
                searchProducts(newVal);
            } else {
                loadProducts();
            }
        });

        txtBuscar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String query = txtBuscar.getText();
                if (query != null && query.length() >= 2) {
                    searchProducts(query);
                }
            }
        });

        // Load initial data
        loadProducts();
    }

    private String determineStatus(InventoryItem item) {
        if (item.getCurrentStock().compareTo(BigDecimal.ZERO) <= 0) {
            return "Out of Stock";
        } else if (item.getCurrentStock().compareTo(item.getMinimumStock()) < 0) {
            return "Low Stock";
        } else {
            return "Normal";
        }
    }

    private void loadProducts() {
        try {
            logger.debug("Loading all products");
            List<InventoryItem> items = inventoryService.getAllItems();
            productList.setAll(items);
            productsTable.setItems(productList);
            
            updateLabels(items);
            logger.debug("Loaded {} products", items.size());
        } catch (Exception e) {
            logger.error("Error loading products", e);
            showAlert("Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void searchProducts(String query) {
        try {
            logger.debug("Searching products with query: {}", query);
            List<InventoryItem> items = inventoryService.searchItems(query);
            productList.setAll(items);
            productsTable.setItems(productList);
            
            updateLabels(items);
            logger.debug("Found {} products matching '{}'", items.size(), query);
        } catch (Exception e) {
            logger.error("Error searching products", e);
            showAlert("Error", "Failed to search products: " + e.getMessage());
        }
    }

    private void updateLabels(List<InventoryItem> items) {
        lblTotalProductos.setText("Total Products: " + items.size());
        
        long lowStockCount = items.stream()
            .filter(item -> item.getCurrentStock().compareTo(item.getMinimumStock()) < 0)
            .count();
        lblStockBajo.setText("Low Stock Items: " + lowStockCount);
    }

    @FXML
    private void handleNuevo() {
        logger.debug("Creating new product");
        // TODO: Open dialog to create new product
        showAlert("Info", "New product dialog - To be implemented");
    }

    @FXML
    private void handleEditar() {
        InventoryItem selectedItem = productsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Warning", "Please select a product to edit");
            return;
        }
        logger.debug("Editing product: {}", selectedItem.getName());
        // TODO: Open dialog to edit product
        showAlert("Info", "Edit product dialog - To be implemented for: " + selectedItem.getName());
    }

    @FXML
    private void handleEliminar() {
        InventoryItem selectedItem = productsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Warning", "Please select a product to delete");
            return;
        }
        
        logger.debug("Deleting product: {}", selectedItem.getName());
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Product");
        alert.setContentText("Are you sure you want to delete the product '" + selectedItem.getName() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                User currentUser = userContext.getCurrentUser();
                inventoryService.deleteItem(selectedItem.getId(), currentUser);
                loadProducts();
                showAlert("Success", "Product deleted successfully");
            } catch (Exception e) {
                logger.error("Error deleting product", e);
                showAlert("Error", "Failed to delete product: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSalida() {
        InventoryItem selectedItem = productsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Warning", "Please select a product to register output");
            return;
        }
        logger.debug("Registering output for product: {}", selectedItem.getName());
        // TODO: Open dialog to register output
        showAlert("Info", "Register output dialog - To be implemented for: " + selectedItem.getName());
    }

    @FXML
    private void handleAjuste() {
        InventoryItem selectedItem = productsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Warning", "Please select a product to adjust");
            return;
        }
        logger.debug("Adjusting inventory for product: {}", selectedItem.getName());
        // TODO: Open dialog to register adjustment
        showAlert("Info", "Adjustment dialog - To be implemented for: " + selectedItem.getName());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
