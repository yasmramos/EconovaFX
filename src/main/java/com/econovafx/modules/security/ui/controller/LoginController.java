package com.econovafx.modules.security.ui.controller;

import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.security.AuthService;
import com.econovafx.modules.core.security.SecurityUtil;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the login dialog with modern web-style design
 */
@Component
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private VBox loginRoot;

    @FXML
    private ImageView logoImage;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button closeButton;

    @FXML
    private ProgressBar progressBar;

    private final AuthService authService;
    private Runnable onLoginSuccess;
    
    // For drag functionality
    private double xOffset = 0;
    private double yOffset = 0;

    @Inject
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        logger.info("Initializing login controller");
        
        // Initialize fields
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        progressBar.setVisible(false);
        progressBar.setManaged(false);
        
        // Setup drag functionality for undecorated window
        setupDraggableWindow();
        
        // Add Enter key support for login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        // Focus username field by default
        usernameField.requestFocus();
    }

    /**
     * Setup drag functionality to move the window when dragging the root container
     */
    private void setupDraggableWindow() {
        loginRoot.setOnMousePressed(event -> {
            Stage stage = (Stage) loginRoot.getScene().getWindow();
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        loginRoot.setOnMouseDragged(event -> {
            Stage stage = (Stage) loginRoot.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        logger.info("Login attempt for user: {}", username);

        // Validate input
        if (username.isEmpty()) {
            showError("Please enter your username or email address");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        // Show loading state
        setLoading(true);

        // Authenticate asynchronously against the database
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate network delay
                
                User user = authService.authenticate(username, password);
                
                javafx.application.Platform.runLater(() -> {
                    if (user != null) {
                        // Set the current user in the security context
                        SecurityUtil.setCurrentUser(user);
                        logger.info("Login successful for user: {}", user.getUsername());
                        hideError();
                        
                        if (onLoginSuccess != null) {
                            onLoginSuccess.run();
                        }
                    } else {
                        showError("Invalid username or email or password. Please try again.");
                        setLoading(false);
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                });
            } catch (Exception e) {
                logger.error("Login error", e);
                javafx.application.Platform.runLater(() -> {
                    showError("An error occurred during login. Please try again.");
                    setLoading(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) loginRoot.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleClear() {
        usernameField.clear();
        passwordField.clear();
        hideError();
        usernameField.requestFocus();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        progressBar.setManaged(loading);
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        
        if (loading) {
            progressBar.setProgress(-1); // Indeterminate
        }
    }
}
