package com.econovafx.modules.core.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Help Controller - provides access to help resources and documentation
 */
public class HelpController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(HelpController.class);
    
    @FXML
    private VBox contentArea;
    
    @FXML
    private TextField txtSearch;
    
    @FXML
    private Button btnSearch;
    
    @FXML
    private Hyperlink linkUserGuide;
    
    @FXML
    private Hyperlink linkVideoTutorials;
    
    @FXML
    private Hyperlink linkFAQ;
    
    @FXML
    private Hyperlink linkKeyboardShortcuts;
    
    @FXML
    private Hyperlink linkReleaseNotes;
    
    @FXML
    private Hyperlink linkContactSupport;
    
    @FXML
    private Hyperlink linkCommunityForum;
    
    @FXML
    private Hyperlink linkAPIReference;
    
    @FXML
    private Hyperlink linkReportBug;
    
    @FXML
    private Accordion helpAccordion;
    
    public HelpController() {
        // Default constructor for FXML loading
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("HelpController initialized");
    }
    
    @FXML
    private void handleSearch() {
        logger.debug("Help search: {}", txtSearch.getText());
        showAlert(Alert.AlertType.INFORMATION, "Search", 
                "Searching for: " + txtSearch.getText());
    }
    
    @FXML
    private void handleUserGuide() {
        logger.debug("Opening user guide");
        showAlert(Alert.AlertType.INFORMATION, "User Guide", 
                "Opening user guide documentation...");
    }
    
    @FXML
    private void handleVideoTutorials() {
        logger.debug("Opening video tutorials");
        showAlert(Alert.AlertType.INFORMATION, "Video Tutorials", 
                "Opening video tutorials...");
    }
    
    @FXML
    private void handleFAQ() {
        logger.debug("Opening FAQ");
        showAlert(Alert.AlertType.INFORMATION, "FAQ", 
                "Opening frequently asked questions...");
    }
    
    @FXML
    private void handleKeyboardShortcuts() {
        logger.debug("Showing keyboard shortcuts");
        showAlert(Alert.AlertType.INFORMATION, "Keyboard Shortcuts", 
                "Showing keyboard shortcuts reference...");
    }
    
    @FXML
    private void handleReleaseNotes() {
        logger.debug("Showing release notes");
        showAlert(Alert.AlertType.INFORMATION, "Release Notes", 
                "EconoNova FX v1.0.0\n\nInitial release with core accounting features.");
    }
    
    @FXML
    private void handleContactSupport() {
        logger.debug("Contacting support");
        showAlert(Alert.AlertType.INFORMATION, "Contact Support", 
                "Email: support@econovafx.com\nPhone: +1-800-ECONOVA");
    }
    
    @FXML
    private void handleCommunityForum() {
        logger.debug("Opening community forum");
        showAlert(Alert.AlertType.INFORMATION, "Community Forum", 
                "Opening community forum...");
    }
    
    @FXML
    private void handleAPIReference() {
        logger.debug("Opening API reference");
        showAlert(Alert.AlertType.INFORMATION, "API Reference", 
                "Opening API documentation...");
    }
    
    @FXML
    private void handleReportBug() {
        logger.debug("Reporting bug");
        showAlert(Alert.AlertType.INFORMATION, "Report Bug", 
                "Opening bug report form...");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
