package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.model.BusinessUnit;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.service.BusinessUnitService;
import io.avaje.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller for the business unit selection dialog.
 * Allows users to select which business unit they want to work with (if company has multiple units).
 */
@io.avaje.inject.Component
public class UnitSelectionController {
    private static final Logger logger = LoggerFactory.getLogger(UnitSelectionController.class);

    @FXML
    private VBox unitSelectionRoot;

    @FXML
    private ListView<BusinessUnit> unitListView;

    @FXML
    private Button selectButton;

    @FXML
    private Button skipButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label errorLabel;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private VBox selectedUnitInfo;

    @FXML
    private Label selectedUnitLabel;

    @FXML
    private Label companyNameLabel;

    private BusinessUnitService businessUnitService;
    private Company currentCompany;
    private Runnable onUnitSelected;
    private Runnable onUnitSkipped;
    private Runnable onCancel;
    private BusinessUnit selectedUnit;

    @Inject
    public void setBusinessUnitService(BusinessUnitService businessUnitService) {
        this.businessUnitService = businessUnitService;
    }

    public UnitSelectionController() {
        // Default constructor - BusinessUnitService will be injected via setter
    }

    @FXML
    public void initialize() {
        logger.info("Initializing unit selection controller");
        
        // Initialize UI state
        progressBar.setVisible(false);
        progressBar.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        emptyStateLabel.setVisible(false);
        emptyStateLabel.setManaged(false);
        selectedUnitInfo.setVisible(false);
        selectedUnitInfo.setManaged(false);
        
        // Configure ListView
        unitListView.setCellFactory(param -> new ListCell<BusinessUnit>() {
            @Override
            protected void updateItem(BusinessUnit unit, boolean empty) {
                super.updateItem(unit, empty);
                if (empty || unit == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(unit.getName() + " (" + unit.getCode() + ")");
                    // Highlight if previously selected
                    if (selectedUnit != null && selectedUnit.getId().equals(unit.getId())) {
                        setStyle("-fx-background-color: #e3f2fd; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Handle selection
        unitListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUnit = newVal;
                selectedUnitLabel.setText(selectedUnit.getName() + " - " + 
                    (selectedUnit.getAddress() != null ? selectedUnit.getAddress() : "No address"));
                selectedUnitInfo.setVisible(true);
                selectedUnitInfo.setManaged(true);
                selectButton.setDisable(false);
            } else {
                selectedUnit = null;
                selectedUnitInfo.setVisible(false);
                selectedUnitInfo.setManaged(false);
                selectButton.setDisable(true);
            }
        });
    }

    /**
     * Set the company for which to load units.
     */
    public void setCompany(Company company) {
        this.currentCompany = company;
        if (company != null) {
            companyNameLabel.setText("Company: " + company.getName());
            loadUnits();
        }
    }

    public void setOnUnitSelected(Runnable onUnitSelected) {
        this.onUnitSelected = onUnitSelected;
    }

    public void setOnUnitSkipped(Runnable onUnitSkipped) {
        this.onUnitSkipped = onUnitSkipped;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    private void loadUnits() {
        if (currentCompany == null) {
            return;
        }

        setLoading(true);
        
        new Thread(() -> {
            try {
                Thread.sleep(300); // Simulate loading
                
                List<BusinessUnit> units = businessUnitService.findByCompanyId(currentCompany.getId());
                
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    
                    if (units.isEmpty()) {
                        emptyStateLabel.setVisible(true);
                        emptyStateLabel.setManaged(true);
                        selectButton.setDisable(true);
                        logger.info("No business units found for company: {}", currentCompany.getName());
                    } else {
                        unitListView.getItems().setAll(units);
                        logger.info("Loaded {} business units for company: {}", units.size(), currentCompany.getName());
                        
                        // Auto-select if only one unit
                        if (units.size() == 1) {
                            unitListView.getSelectionModel().select(0);
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("Error loading business units", e);
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    errorLabel.setText("Error loading units: " + e.getMessage());
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    selectButton.setDisable(true);
                });
            }
        }).start();
    }

    @FXML
    private void handleSelect() {
        if (selectedUnit == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Unit Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a business unit to continue.");
            alert.showAndWait();
            return;
        }
        
        logger.info("Business Unit selected: {} ({})", selectedUnit.getName(), selectedUnit.getCode());
        
        // Store unit in context or session as needed
        // For now, we just notify the callback
        // The actual tenant context is already set with the company
        
        if (onUnitSelected != null) {
            onUnitSelected.run();
        }
    }

    @FXML
    private void handleSkip() {
        logger.info("Business unit selection skipped for company: {}", 
            currentCompany != null ? currentCompany.getName() : "unknown");
        
        if (onUnitSkipped != null) {
            onUnitSkipped.run();
        }
    }

    @FXML
    private void handleCancel() {
        logger.info("Unit selection cancelled");
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        progressBar.setManaged(loading);
        unitListView.setDisable(loading);
        selectButton.setDisable(loading || selectedUnit == null);
        skipButton.setDisable(loading);
        cancelButton.setDisable(loading);
        
        if (loading) {
            progressBar.setProgress(-1); // Indeterminate
        }
    }
}
