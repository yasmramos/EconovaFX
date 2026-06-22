package com.econovafx;

import com.econovafx.config.AppContext;
import com.econovafx.config.DatabaseConfig;
import com.econovafx.ui.controller.MainViewController;
import com.econovafx.ui.preloader.EconoNovaPreloader;
import com.econovafx.ui.view.ViewFactory;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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

    @Override
    public void init() throws Exception {
        logger.info("Initializing EconoNova FX Application...");
        context = AppContext.getInstance();
        logger.info("Application initialization complete");
    }

    @Override
    public void start(Stage stage) {
        logger.info("Starting JavaFX application");
        this.primaryStage = stage;

        try {
            // Initialize main application in background with preloader
            notifyPreloader(new EconoNovaPreloader.ProgressNotification(0.1, "Initializing application..."));
            new Thread(this::initializeMainApp).start();

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            throw new RuntimeException("Failed to start application", e);
        }
    }

    private void initializeMainApp() {
        try {
            // Load database configuration
            notifyPreloader(new EconoNovaPreloader.ProgressNotification(0.2, "Loading database configuration..."));
            DatabaseConfig.initialize();
            Thread.sleep(300);

            // Initialize services (already initialized in AppContext constructor)
            notifyPreloader(new EconoNovaPreloader.ProgressNotification(0.4, "Initializing services..."));
            Thread.sleep(300);

            // Load user interface
            notifyPreloader(new EconoNovaPreloader.ProgressNotification(0.6, "Loading user interface..."));
            Thread.sleep(300);

            // Finalize setup
            notifyPreloader(new EconoNovaPreloader.ProgressNotification(0.8, "Finalizing setup..."));
            Thread.sleep(300);

            // Load main application on JavaFX thread
            notifyPreloader(new EconoNovaPreloader.ProgressNotification(1.0, "Ready!"));
            javafx.application.Platform.runLater(this::loadMainView);

        } catch (Exception e) {
            logger.error("Error during initialization", e);
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
