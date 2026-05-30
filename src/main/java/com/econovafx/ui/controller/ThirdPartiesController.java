package com.econovafx.ui.controller;

import com.econovafx.domain.ThirdParty;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    @FXML
    private TextField searchField;
    
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
    
    public ThirdPartiesController(ThirdPartyService thirdPartyService, ViewFactory viewFactory) {
        this.thirdPartyService = thirdPartyService;
        this.viewFactory = viewFactory;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("ThirdPartiesController initialized");
        initializeTableColumns();
        initializeTypeFilter();
        loadThirdParties();
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
    }
    
    private void filterByType(ThirdParty.ThirdPartyType type) {
        List<ThirdParty> thirdParties = thirdPartyService.getThirdPartiesByType(type);
        ObservableList<ThirdParty> observableList = FXCollections.observableArrayList(thirdParties);
        thirdPartiesTable.setItems(observableList);
    }
    
    @FXML
    private void searchThirdParties() {
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<ThirdParty> thirdParties = thirdPartyService.searchThirdParties(searchTerm.trim());
            ObservableList<ThirdParty> observableList = FXCollections.observableArrayList(thirdParties);
            thirdPartiesTable.setItems(observableList);
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
        
        logger.debug("View transactions for third party: {}", selected.getName());
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
                "Third party transactions view is under development");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
