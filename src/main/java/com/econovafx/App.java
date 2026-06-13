package com.econovafx;

import com.econovafx.config.AppContext;
import com.econovafx.config.DatabaseConfig;
import com.econovafx.ui.controller.MainViewController;
import com.econovafx.ui.view.ViewFactory;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for EconoNova FX Accounting System
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private AppContext context;

    @Override
    public void init() throws Exception {
        logger.info("Initializing EconoNova FX Application...");
        context = AppContext.getInstance();
        logger.info("Application initialization complete");
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting JavaFX application");

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

            primaryStage.setTitle("EconoNova FX - Sistema Contable");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);

            primaryStage.show();

            logger.info("Application started successfully");

        } catch (IOException e) {
            logger.error("Failed to start application", e);
            throw new RuntimeException("Failed to start application", e);
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
