package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.service.UserService;
import com.econovafx.modules.core.ui.util.ModernDialog;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Profile Controller - displays and manages user profile information
 */
public class ProfileController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    
    private final UserService userService;
    
    @FXML
    private VBox contentArea;
    
    @FXML
    private Label lblUserName;
    
    @FXML
    private Label lblUserEmail;
    
    @FXML
    private Label lblUserRole;
    
    @FXML
    private Label lblFullName;
    
    @FXML
    private Label lblEmail;
    
    @FXML
    private Label lblRole;
    
    @FXML
    private Label lblDepartment;
    
    @FXML
    private Label lblLastLogin;
    
    @FXML
    private Button btnEditProfile;
    
    @FXML
    private Button btnChangePassword;
    
    public ProfileController(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("ProfileController initialized");
        loadUserProfile();
    }
    
    private void loadUserProfile() {
        // Load current user profile information
        // For now, using placeholder data
        lblUserName.setText("System Administrator");
        lblUserEmail.setText("admin@econovafx.com");
        lblUserRole.setText("System Administrator");
        lblFullName.setText("System Administrator");
        lblEmail.setText("admin@econovafx.com");
        lblRole.setText("Administrator");
        lblDepartment.setText("IT Department");
        lblLastLogin.setText("N/A");
    }
    
    @FXML
    private void handleEditProfile() {
        logger.debug("Edit profile clicked");
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
                "Profile editing will be implemented soon");
    }
    
    @FXML
    private void handleChangePassword() {
        logger.debug("Change password clicked");
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
                "Password change dialog will be implemented soon");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
