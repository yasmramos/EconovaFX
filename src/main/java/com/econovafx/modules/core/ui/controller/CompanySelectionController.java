package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.service.CompanyService;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    private TextField searchField;

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
    private ObservableList<Company> masterData = FXCollections.observableArrayList();
    private javafx.collections.FilteredList<Company> filteredData;

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
        
        // Setup search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredData != null) {
                filteredData.setPredicate(company -> {
                    if (newVal == null || newVal.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newVal.toLowerCase();
                    return company.getName().toLowerCase().contains(lowerCaseFilter)
                            || company.getCode().toLowerCase().contains(lowerCaseFilter)
                            || (company.getNif() != null && company.getNif().toLowerCase().contains(lowerCaseFilter));
                });
            }
        });
        
        // Configure ListView with enriched cells
        companyListView.setCellFactory(param -> new ListCell<Company>() {
            private final HBox container = new HBox();
            private final VBox content = new VBox();
            private final Label nameLabel = new Label();
            private final Label codeLabel = new Label();
            private final Label avatarLabel = new Label();
            
            {
                container.getStyleClass().add("company-cell-container");
                avatarLabel.getStyleClass().add("company-avatar");
                nameLabel.getStyleClass().add("company-name");
                codeLabel.getStyleClass().add("company-code");
                
                avatarLabel.setMinSize(40, 40);
                avatarLabel.setMaxSize(40, 40);
                avatarLabel.setAlignment(javafx.geometry.Pos.CENTER);
                
                content.setSpacing(4);
                HBox.setHgrow(content, Priority.ALWAYS);
                container.setSpacing(12);
                container.getChildren().addAll(avatarLabel, content);
                content.getChildren().addAll(nameLabel, codeLabel);
            }
            
            @Override
            protected void updateItem(Company company, boolean empty) {
                super.updateItem(company, empty);
                if (empty || company == null) {
                    setGraphic(null);
                } else {
                    // Set avatar with initials
                    String initials = getInitials(company.getName());
                    avatarLabel.setText(initials);
                    
                    // Set company info
                    nameLabel.setText(company.getName());
                    codeLabel.setText(company.getCode() + " • NIF: " + (company.getNif() != null ? company.getNif() : "N/A"));
                    
                    setGraphic(container);
                    
                    // Handle selection styling via CSS classes
                    if (isSelected()) {
                        container.getStyleClass().add("selected");
                    } else {
                        container.getStyleClass().remove("selected");
                    }
                    
                    // Highlight previously selected company
                    if (selectedCompany != null && selectedCompany.getId().equals(company.getId())) {
                        container.getStyleClass().add("previously-selected");
                    } else {
                        container.getStyleClass().remove("previously-selected");
                    }
                }
            }
            
            private String getInitials(String name) {
                if (name == null || name.isEmpty()) {
                    return "C";
                }
                String[] parts = name.split("\\s+");
                StringBuilder initials = new StringBuilder();
                for (int i = 0; i < Math.min(parts.length, 2); i++) {
                    if (!parts[i].isEmpty()) {
                        initials.append(parts[i].charAt(0));
                    }
                }
                return initials.toString().toUpperCase();
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
                        masterData.setAll(companies);
                        filteredData = new javafx.collections.FilteredList<>(masterData, p -> true);
                        companyListView.setItems(filteredData);
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
                    errorLabel.setText("Error al cargar las empresas: " + e.getMessage());
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
            alert.setTitle("Advertencia");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecciona una empresa para continuar.");
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
