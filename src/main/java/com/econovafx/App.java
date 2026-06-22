package com.econovafx;

import com.econovafx.config.AppContext;
import com.econovafx.config.DatabaseConfig;
import com.econovafx.ui.controller.MainViewController;
import com.econovafx.ui.view.ViewFactory;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for EconoNova FX Accounting System
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final String VERSION = "1.0.0";

    private AppContext context;
    private Stage primaryStage;
    private Stage splashStage;
    private Label progressLabel;

    @Override
    public void init() throws Exception {
        logger.info("Initializing EconoNova FX Application v{}", VERSION);
        context = AppContext.getInstance();
        logger.info("Application context initialized");
    }

    @Override
    public void start(Stage stage) {
        logger.info("Starting JavaFX application");
        this.primaryStage = stage;

        try {
            // Show splash screen first
            showSplashScreen();
            
            // Initialize main application in background
            new Thread(this::initializeMainApp).start();

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            throw new RuntimeException("Failed to start application", e);
        }
    }
    
    private void showSplashScreen() {
        splashStage = new Stage(StageStyle.TRANSPARENT);
        
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e3a8a, #3b82f6); -fx-border-radius: 15px; -fx-background-radius: 15px;");
        root.setPrefSize(500, 300);
        
        Label titleLabel = new Label("EconoNova FX");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("Accounting System");
        subtitleLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 16px;");
        
        progressLabel = new Label("Initializing...");
        progressLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px;");
        
        VBox content = new VBox(20, titleLabel, subtitleLabel, progressLabel);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        root.getChildren().add(content);
        
        Scene splashScene = new Scene(root, 500, 300);
        splashScene.setFill(null);
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();
        splashStage.show();
    }
    
    private void updateProgress(String message) {
        if (progressLabel != null) {
            javafx.application.Platform.runLater(() -> progressLabel.setText(message));
        }
    }
    
    private void closeSplash() {
        if (splashStage != null) {
            javafx.application.Platform.runLater(() -> {
                splashStage.close();
                splashStage.close();
            });
        }
    }

    private void initializeMainApp() {
        try {
            // Load database configuration
            logger.info("Loading database configuration...");
            updateProgress("Loading database configuration...");
            DatabaseConfig.initialize();
            Thread.sleep(200);

            // Initialize services
            logger.info("Initializing services...");
            updateProgress("Initializing services...");
            Thread.sleep(200);

            // Load user interface on JavaFX thread
            logger.info("Loading user interface...");
            updateProgress("Loading user interface...");
            javafx.application.Platform.runLater(this::loadMainView);

        } catch (Exception e) {
            logger.error("Error during initialization", e);
            updateProgress("Error during initialization");
            javafx.application.Platform.runLater(() -> {
                // Handle error appropriately
            });
        }
    }

    private void loadMainView() {
        try {
            ViewFactory viewFactory = context.getViewFactory();
            MainViewController mainController = new MainViewController(
                    context.getAccountService(),
                    context.getTransactionService(),
                    context.getUserService(),
                    viewFactory
            );

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            loader.setControllerFactory(cls -> mainController);

            Scene scene = new Scene(loader.load(), 1200, 800);

            // Add all stylesheets in correct order
            scene.getStylesheets().add(getClass().getResource("/css/main-styles.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

            primaryStage.setTitle("EconoNova FX - Accounting System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            primaryStage.centerOnScreen();
            
            // Close splash screen
            closeSplash();

            logger.info("Application started successfully");

        } catch (IOException e) {
            logger.error("Failed to load main view", e);
            throw new RuntimeException("Failed to load main view", e);
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Shutting down application...");
        if (context != null) {
            context.close();
        }
        DatabaseConfig.shutdown();
        logger.info("Application shutdown complete");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
