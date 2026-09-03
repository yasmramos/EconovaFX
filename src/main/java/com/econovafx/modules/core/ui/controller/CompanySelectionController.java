package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.service.CompanyService;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller for the company selection dialog.
 * Allows users to select which company (tenant) they want to work with.
 */
@Component
public class CompanySelectionController {
    private static final Logger logger = LoggerFactory.getLogger(CompanySelectionController.class);

    @FXML
    private VBox companySelectionRoot;

    @FXML
    private ListView<Company> companyListView;

    @FXML
    private Button selectButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label errorLabel;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private VBox selectedCompanyInfo;

    @FXML
    private Label selectedCompanyLabel;

    private CompanyService companyService;
    private Runnable onCompanySelected;
    private Runnable onCancel;
    private Company selectedCompany;

    public CompanySelectionController() {
        // Default constructor - services will be injected by Avaje Inject
    }

    @Inject
    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    @FXML
    public void initialize() {
        logger.info("Initializing company selection controller");
        
        // Initialize UI state
        progressBar.setVisible(false);
        progressBar.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        emptyStateLabel.setVisible(false);
        emptyStateLabel.setManaged(false);
        selectedCompanyInfo.setVisible(false);
        selectedCompanyInfo.setManaged(false);
        
        // Configure ListView
        companyListView.setCellFactory(param -> new ListCell<Company>() {
            @Override
            protected void updateItem(Company company, boolean empty) {
                super.updateItem(company, empty);
                if (empty || company == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(company.getName() + " (" + company.getCode() + ")");
                    // Highlight if previously selected
                    if (selectedCompany != null && selectedCompany.getId().equals(company.getId())) {
                        setStyle("-fx-background-color: #e3f2fd; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Handle selection
        companyListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedCompany = newVal;
                selectedCompanyLabel.setText(selectedCompany.getName() + " - NIF: " + selectedCompany.getNif());
                selectedCompanyInfo.setVisible(true);
                selectedCompanyInfo.setManaged(true);
                selectButton.setDisable(false);
            } else {
                selectedCompany = null;
                selectedCompanyInfo.setVisible(false);
                selectedCompanyInfo.setManaged(false);
                selectButton.setDisable(true);
            }
        });
        
        // Load companies
        loadCompanies();
    }

    public void setOnCompanySelected(Runnable onCompanySelected) {
        this.onCompanySelected = onCompanySelected;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    private void loadCompanies() {
        setLoading(true);
        
        new Thread(() -> {
            try {
                Thread.sleep(300); // Simulate loading
                
                List<Company> companies = companyService.findAllActive();
                
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    
                    if (companies.isEmpty()) {
                        emptyStateLabel.setVisible(true);
                        emptyStateLabel.setManaged(true);
                        selectButton.setDisable(true);
                        logger.warn("No active companies found");
                    } else {
                        companyListView.getItems().setAll(companies);
                        logger.info("Loaded {} companies", companies.size());
                        
                        // Auto-select if only one company
                        if (companies.size() == 1) {
                            companyListView.getSelectionModel().select(0);
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("Error loading companies", e);
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    errorLabel.setText("Error loading companies: " + e.getMessage());
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    selectButton.setDisable(true);
                });
            }
        }).start();
    }

    @FXML
    private void handleSelect() {
        if (selectedCompany == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Company Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a company to continue.");
            alert.showAndWait();
            return;
        }
        
        logger.info("Company selected: {} ({})", selectedCompany.getName(), selectedCompany.getCode());
        
        // Set as active tenant
        companyService.selectTenant(selectedCompany);
        
        if (onCompanySelected != null) {
            onCompanySelected.run();
        }
    }

    @FXML
    private void handleCancel() {
        logger.info("Company selection cancelled");
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        progressBar.setManaged(loading);
        companyListView.setDisable(loading);
        selectButton.setDisable(loading || selectedCompany == null);
        cancelButton.setDisable(loading);
        
        if (loading) {
            progressBar.setProgress(-1); // Indeterminate
        }
    }
}
