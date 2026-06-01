package com.econovafx.ui.controller;

import com.econovafx.controller.ThirdPartyTransactionsController;
import com.econovafx.domain.ThirdParty;
import com.econovafx.service.ExportService;
import com.econovafx.service.ThirdPartyService;
import com.econovafx.ui.view.ViewFactory;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Third Parties controller for managing Customers and Suppliers
 */
public class ThirdPartiesController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartiesController.class);
    
    private final ThirdPartyService thirdPartyService;
    private final ViewFactory viewFactory;
    private final ExportService exportService;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private ComboBox<ThirdParty.ThirdPartyType> typeFilter;
    
    @FXML
    private TableView<ThirdParty> thirdPartiesTable;
    
    @FXML
    private TableColumn<ThirdParty, String> colName;
    
    @FXML
    private TableColumn<ThirdParty, String> colIdentification;
    
    @FXML
    private TableColumn<ThirdParty, ThirdParty.ThirdPartyType> colType;
    
    @FXML
    private TableColumn<ThirdParty, String> colEmail;
    
    @FXML
    private TableColumn<ThirdParty, String> colPhone;
    
    @FXML
    private TableColumn<ThirdParty, String> colCity;
    
    @FXML
    private TableColumn<ThirdParty, Double> colBalance;
    
    @FXML
    private TableColumn<ThirdParty, Boolean> colActive;
    
    @FXML
    private TableColumn<ThirdParty, Void> colActions;
    
    @FXML
    private Label totalThirdPartiesLabel;
    
    @FXML
    private Label totalCustomersLabel;
    
    @FXML
    private Label totalSuppliersLabel;
    
    @FXML
    private Label activeThirdPartiesLabel;
    
    @FXML
    private Label resultsCountLabel;
    
    public ThirdPartiesController(ThirdPartyService thirdPartyService, ViewFactory viewFactory, ExportService exportService) {
        this.thirdPartyService = thirdPartyService;
        this.viewFactory = viewFactory;
        this.exportService = exportService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("ThirdPartiesController initialized");
        initializeTableColumns();
        initializeTypeFilter();
        loadThirdParties();
        setupActionColumn();
    }
    
    private void initializeTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colIdentification.setCellValueFactory(new PropertyValueFactory<>("identificationNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("currentBalance"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        
        // Custom cell factory for Type column
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(ThirdParty.ThirdPartyType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getTypeDisplayName(item));
                }
            }
        });
        
        // Custom cell factory for Active column
        colActive.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Active" : "Inactive");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }
    
    private String getTypeDisplayName(ThirdParty.ThirdPartyType type) {
        return switch (type) {
            case CUSTOMER -> "Customer";
            case SUPPLIER -> "Supplier";
            case BOTH -> "Both";
        };
    }
    
    private void initializeTypeFilter() {
        ObservableList<ThirdParty.ThirdPartyType> types = FXCollections.observableArrayList(
            ThirdParty.ThirdPartyType.values()
        );
        typeFilter.setItems(types);
        typeFilter.setValue(ThirdParty.ThirdPartyType.CUSTOMER);
        
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterByType(newVal);
            }
        });
    }
    
    private void loadThirdParties() {
        List<ThirdParty> thirdParties = thirdPartyService.getAllThirdParties();
        ObservableList<ThirdParty> observableList = FXCollections.observableArrayList(thirdParties);
        thirdPartiesTable.setItems(observableList);
        updateStatistics(thirdParties);
    }
    
    private void filterByType(ThirdParty.ThirdPartyType type) {
        List<ThirdParty> thirdParties = thirdPartyService.getThirdPartiesByType(type);
        ObservableList<ThirdParty> observableList = FXCollections.observableArrayList(thirdParties);
        thirdPartiesTable.setItems(observableList);
        updateStatistics(thirdParties);
    }
    
    @FXML
    private void searchThirdParties() {
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<ThirdParty> thirdParties = thirdPartyService.searchThirdParties(searchTerm.trim());
            ObservableList<ThirdParty> observableList = FXCollections.observableArrayList(thirdParties);
            thirdPartiesTable.setItems(observableList);
            updateStatistics(thirdParties);
        } else {
            loadThirdParties();
        }
    }
    
    @FXML
    private void newThirdParty() {
        logger.debug("New third party clicked");
        Optional<ThirdParty> result = viewFactory.showThirdPartyFormDialog(null);
        result.ifPresent(thirdParty -> {
            thirdPartyService.createThirdParty(thirdParty);
            loadThirdParties();
            updateStatistics(thirdPartyService.getAllThirdParties());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Third party created successfully");
        });
    }
    
    @FXML
    private void editThirdParty() {
        ThirdParty selected = thirdPartiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", 
                    "Please select a third party to edit");
            return;
        }
        
        logger.debug("Edit third party: {}", selected.getIdentificationNumber());
        Optional<ThirdParty> result = viewFactory.showThirdPartyFormDialog(selected);
        result.ifPresent(thirdParty -> {
            thirdPartyService.updateThirdParty(thirdParty);
            loadThirdParties();
            updateStatistics(thirdPartyService.getAllThirdParties());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Third party updated successfully");
        });
    }
    
    @FXML
    private void deleteThirdParty() {
        ThirdParty selected = thirdPartiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", 
                    "Please select a third party to delete");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this third party?");
        alert.setContentText("Third Party: " + selected.getName() + " (" + selected.getIdentificationNumber() + ")");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                thirdPartyService.deleteThirdParty(selected.getId());
                loadThirdParties();
                updateStatistics(thirdPartyService.getAllThirdParties());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Third party deleted successfully");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }
    
    @FXML
    private void viewTransactions() {
        ThirdParty selected = thirdPartiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", 
                    "Please select a third party to view transactions");
            return;
        }
        
        logger.info("Opening transactions view for third party: {}", selected.getName());
        
        try {
            // Load the FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/econovafx/view/third-party-transactions.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            // Get controller and set dependencies
            ThirdPartyTransactionsController controller = loader.getController();
            controller.setServices(thirdPartyService, viewFactory.getTransactionService(), exportService);
            controller.initData(selected);
            
            // Create and show stage
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Transactions - " + selected.getName());
            stage.setScene(new javafx.scene.Scene(root, 900, 600));
            stage.initOwner(thirdPartiesTable.getScene().getWindow());
            stage.setResizable(true);
            
            controller.setStage(stage);
            stage.showAndWait();
            
            logger.info("Closed transactions view for third party: {}", selected.getName());
        } catch (Exception e) {
            logger.error("Error opening transactions view", e);
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to open transactions view: " + e.getMessage());
        }
    }
    
    @FXML
    private void exportToExcel() {
        List<ThirdParty> thirdParties = thirdPartiesTable.getItems();
        if (thirdParties.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", 
                    "There are no third parties to export");
            return;
        }
        
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Third Parties to Excel");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            fileChooser.setInitialFileName("third_parties_" + System.currentTimeMillis() + ".xlsx");
            
            File file = fileChooser.showSaveDialog(thirdPartiesTable.getScene().getWindow());
            if (file != null) {
                exportService.exportThirdPartiesToExcel(thirdParties, file);
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Third parties exported successfully to:\n" + file.getAbsolutePath());
                logger.info("Third parties exported to Excel: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error exporting third parties to Excel", e);
            showAlert(Alert.AlertType.ERROR, "Export Error", 
                    "Failed to export third parties: " + e.getMessage());
        }
    }
    
    /**
     * Setup the Actions column with Edit and Delete buttons for each row
     */
    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);
            
            {
                btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;"));
                
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;"));
                btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;"));
                
                btnEdit.setOnAction(event -> {
                    ThirdParty thirdParty = getTableView().getItems().get(getIndex());
                    if (thirdParty != null) {
                        thirdPartiesTable.getSelectionModel().select(thirdParty);
                        editThirdParty();
                    }
                });
                
                btnDelete.setOnAction(event -> {
                    ThirdParty thirdParty = getTableView().getItems().get(getIndex());
                    if (thirdParty != null) {
                        thirdPartiesTable.getSelectionModel().select(thirdParty);
                        deleteThirdParty();
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }
    
    /**
     * Update statistics labels based on current data
     */
    private void updateStatistics(List<ThirdParty> thirdParties) {
        int total = thirdParties.size();
        long customers = thirdParties.stream().filter(tp -> tp.getType() == ThirdParty.ThirdPartyType.CUSTOMER || tp.getType() == ThirdParty.ThirdPartyType.BOTH).count();
        long suppliers = thirdParties.stream().filter(tp -> tp.getType() == ThirdParty.ThirdPartyType.SUPPLIER || tp.getType() == ThirdParty.ThirdPartyType.BOTH).count();
        long active = thirdParties.stream().filter(ThirdParty::getIsActive).count();
        
        totalThirdPartiesLabel.setText(String.valueOf(total));
        totalCustomersLabel.setText(String.valueOf(customers));
        totalSuppliersLabel.setText(String.valueOf(suppliers));
        activeThirdPartiesLabel.setText(String.valueOf(active));
        resultsCountLabel.setText(total + " result" + (total != 1 ? "s" : ""));
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
