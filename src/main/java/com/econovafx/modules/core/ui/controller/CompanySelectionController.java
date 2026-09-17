package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.service.CompanyService;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ListWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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

    @FXML
    private TextField searchField;

    private CompanyService companyService;
    private Runnable onCompanySelected;
    private Runnable onCancel;
    private Company selectedCompany;
    private ObservableList<Company> companyObservableList;
    private FilteredList<Company> filteredCompanyList;

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
        searchField.setVisible(false);
        searchField.setManaged(false);
        
        // Setup search field filtering
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (filteredCompanyList != null) {
                filteredCompanyList.setPredicate(company -> {
                    if (newText == null || newText.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newText.toLowerCase();
                    return company.getName().toLowerCase().contains(lowerCaseFilter)
                            || company.getCode().toLowerCase().contains(lowerCaseFilter)
                            || (company.getNif() != null && company.getNif().toLowerCase().contains(lowerCaseFilter));
                });
            }
        });
        
        // Configure ListView with enriched cells
        companyListView.setCellFactory(param -> new ListCell<Company>() {
            private final HBox hbox = new HBox(10);
            private final Circle avatar = new Circle(20);
            private final Text initials = new Text();
            private final VBox textVBox = new VBox(4);
            private final Text nameText = new Text();
            private final Text codeText = new Text();
            
            {
                // Setup avatar circle
                avatar.setFill(Color.web("#2196F3"));
                initials.setFont(Font.font("System", FontWeight.BOLD, 14));
                initials.setFill(Color.WHITE);
                
                // Setup text elements
                nameText.setFont(Font.font("System", FontWeight.BOLD, 14));
                nameText.setFill(Color.web("#2c3e50"));
                codeText.setFont(Font.font("System", 12));
                codeText.setFill(Color.web("#7f8c8d"));
                
                textVBox.getChildren().addAll(nameText, codeText);
                hbox.getChildren().addAll(avatar, textVBox);
                hbox.setStyle("-fx-padding: 8px; -fx-alignment: center-left;");
                hbox.setMaxWidth(Double.MAX_VALUE);
            }
            
            @Override
            protected void updateItem(Company company, boolean empty) {
                super.updateItem(company, empty);
                if (empty || company == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    // Set initials
                    String companyName = company.getName();
                    String initialChars = companyName.length() > 0 ? 
                            companyName.substring(0, Math.min(2, companyName.length())).toUpperCase() : "EC";
                    initials.setText(initialChars);
                    
                    // Set texts
                    nameText.setText(companyName);
                    String codeDisplay = company.getNif() != null && !company.getNif().isEmpty() 
                            ? "NIF: " + company.getNif() 
                            : "Cód: " + company.getCode();
                    codeText.setText(codeDisplay);
                    
                    setGraphic(hbox);
                    setText(null);
                    
                    // Apply selected style via CSS class instead of inline style
                    getStyleClass().remove("previously-selected");
                    if (selectedCompany != null && selectedCompany.getId().equals(company.getId())) {
                        getStyleClass().add("previously-selected");
                    } else {
                        getStyleClass().remove("previously-selected");
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
                        searchField.setVisible(false);
                        searchField.setManaged(false);
                        logger.warn("No active companies found");
                    } else {
                        companyObservableList = FXCollections.observableArrayList(companies);
                        filteredCompanyList = new FilteredList<>(companyObservableList, p -> true);
                        companyListView.setItems(filteredCompanyList);
                        
                        // Show search field if more than 5 companies
                        if (companies.size() > 5) {
                            searchField.setVisible(true);
                            searchField.setManaged(true);
                        }
                        
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
        
        logger.info("Empresa seleccionada: {} ({})", selectedCompany.getName(), selectedCompany.getCode());
        
        // Set as active tenant
        companyService.selectTenant(selectedCompany);
        
        if (onCompanySelected != null) {
            onCompanySelected.run();
        }
    }

    @FXML
    private void handleCancel() {
        logger.info("Selección de empresa cancelada");
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
