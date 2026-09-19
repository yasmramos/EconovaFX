package com.econovafx.modules.core.ui.view;

import com.econovafx.modules.core.config.DatabaseConfig;
import com.econovafx.modules.core.config.DatabaseSeeder;
import com.econovafx.modules.core.security.AuthService;
import com.econovafx.modules.core.service.CompanyService;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import io.ebean.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;

public class SplashController {

    private static final Logger logger = LoggerFactory.getLogger(SplashController.class);

    @FXML
    private StackPane rootPane;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    @FXML
    private Label versionLabel;

    private Runnable onInitializationComplete;
    
    @Inject
    private DatabaseSeeder databaseSeeder;

    public void setOnInitializationComplete(Runnable callback) {
        this.onInitializationComplete = callback;
    }

    @FXML
    public void initialize() {
        versionLabel.setText("EconoNova FX v1.0.0");
        progressBar.setProgress(0);
        statusLabel.setText("Iniciando aplicación...");
        
        // Iniciar inicialización en segundo plano
        startInitialization();
    }

    private void startInitialization() {
        // Create a CompletableFuture with timeout support
        CompletableFuture<Void> initializationFuture = CompletableFuture.runAsync(() -> {
            try {
                updateProgress(0.1, "Loading database configuration...");
                logger.info("Starting database initialization...");
                DatabaseConfig.initialize();
                logger.info("Database initialization completed");
                
                updateProgress(0.3, "Verifying database connection...");
                io.ebean.DB.getDefault();
                logger.info("Database connection verified");
                
                updateProgress(0.5, "Seeding initial data...");
                // Seed database with default data (company, currencies, admin user)
                if (databaseSeeder != null) {
                    databaseSeeder.seed();
                } else {
                    logger.warn("DatabaseSeeder not injected, using fallback");
                    DatabaseSeeder seeder = new DatabaseSeeder();
                    seeder.seed();
                }
                logger.info("Database seeding completed");
                
                updateProgress(0.7, "Loading core modules...");
                Thread.sleep(500); // Small pause to allow rendering
                
                updateProgress(0.9, "Preparing user interface...");
                Thread.sleep(300);
                
                updateProgress(1.0, "Application ready!");
                
                // Small pause before showing the main window
                Thread.sleep(500);
                
                // Smooth transition to the main app
                javafx.application.Platform.runLater(() -> {
                    try {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), rootPane);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(e -> {
                            try {
                                if (onInitializationComplete != null) {
                                    logger.info("Executing initialization complete callback...");
                                    onInitializationComplete.run();
                                } else {
                                    logger.error("ERROR: onInitializationComplete is null!");
                                }
                            } catch (Exception ex) {
                                logger.error("Error executing callback: " + ex.getMessage(), ex);
                            }
                        });
                        fadeOut.play();
                    } catch (Exception e) {
                        logger.error("Error in fade transition: " + e.getMessage(), e);
                        // Try calling directly if animation fails
                        if (onInitializationComplete != null) {
                            onInitializationComplete.run();
                        }
                    }
                });
                
            } catch (Exception e) {
                logger.error("Error during initialization: " + e.getMessage(), e);
                // Log full stack trace to help diagnose
                logger.error("Full stack trace:", e);
                if (e.getCause() != null) {
                    logger.error("Root cause:", e.getCause());
                }
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    // Show alert dialog with option to exit or retry
                    showInitializationErrorDialog(e);
                });
            }
        });
        
        // Add timeout handling (30 seconds max for initialization)
        initializationFuture.orTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .exceptionally(throwable -> {
                logger.error("Initialization timeout or error: " + throwable.getMessage(), throwable);
                if (throwable.getCause() != null) {
                    logger.error("Root cause:", throwable.getCause());
                }
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Error: Initialization timeout");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    showInitializationErrorDialog(throwable instanceof Exception ? (Exception) throwable : new Exception(throwable));
                });
                return null;
            });
    }
    
    /**
     * Shows an error dialog when initialization fails, offering options to exit or retry.
     * @param ex The exception that caused the failure
     */
    private void showInitializationErrorDialog(Exception ex) {
        try {
            javafx.application.Platform.runLater(() -> {
                try {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Initialization Error");
                    alert.setHeaderText("Failed to initialize application");
                    
                    // Include full stack trace in content
                    StringBuilder sb = new StringBuilder();
                    sb.append(ex.getMessage()).append("\n\n");
                    sb.append("Please check your database configuration and try again.\n\n");
                    sb.append("Technical details:\n");
                    for (StackTraceElement element : ex.getStackTrace()) {
                        sb.append("  at ").append(element.toString()).append("\n");
                    }
                    
                    javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(sb.toString());
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setMaxWidth(Double.MAX_VALUE);
                    textArea.setMaxHeight(Double.MAX_VALUE);
                    alert.getDialogPane().setContent(textArea);
                    
                    // Add Exit and Retry buttons
                    javafx.scene.control.ButtonType retryButton = new javafx.scene.control.ButtonType("Retry");
                    javafx.scene.control.ButtonType exitButton = new javafx.scene.control.ButtonType("Exit", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                    alert.getButtonTypes().setAll(retryButton, exitButton);
                    
                    alert.showAndWait().ifPresent(response -> {
                        if (response == exitButton) {
                            logger.info("User chose to exit after initialization error");
                            javafx.application.Platform.exit();
                        } else if (response == retryButton) {
                            logger.info("User chose to retry initialization");
                            updateProgress(0, "Retrying initialization...");
                            statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                            startInitialization();
                        }
                    });
                } catch (Exception e) {
                    logger.error("Error showing error dialog: " + e.getMessage(), e);
                    // Fallback: just exit
                    javafx.application.Platform.exit();
                }
            });
        } catch (Exception e) {
            logger.error("Failed to show error dialog: " + e.getMessage(), e);
            // Ultimate fallback
            System.exit(1);
        }
    }

    private void updateProgress(double progress, String message) {
        javafx.application.Platform.runLater(() -> {
            progressBar.setProgress(progress);
            statusLabel.setText(message);
        });
    }
}
