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
        CompletableFuture.runAsync(() -> {
            try {
                updateProgress(0.1, "Loading database configuration...");
                DatabaseConfig.initialize();
                
                updateProgress(0.3, "Verifying database connection...");
                io.ebean.DB.getDefault();
                
                updateProgress(0.5, "Seeding initial data...");
                // Seed database with default data (company, currencies, admin user)
                if (databaseSeeder != null) {
                    databaseSeeder.seed();
                } else {
                    logger.warn("DatabaseSeeder not injected, using fallback");
                    DatabaseSeeder seeder = new DatabaseSeeder();
                    seeder.seed();
                }
                
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
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                });
            }
        });
    }

    private void updateProgress(double progress, String message) {
        javafx.application.Platform.runLater(() -> {
            progressBar.setProgress(progress);
            statusLabel.setText(message);
        });
    }
}
