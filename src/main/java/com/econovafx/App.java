package com.econovafx;

import com.econovafx.core.i18n.I18nManager;
import com.econovafx.modules.core.config.AppContext;
import com.econovafx.modules.core.config.DatabaseConfig;
import com.econovafx.modules.core.config.TenantContext;
import com.econovafx.modules.core.model.Company;
import com.econovafx.modules.core.service.BusinessUnitService;
import com.econovafx.modules.core.service.backup.BackupSchedulerService;
import com.econovafx.modules.core.ui.controller.MainViewController;
import com.econovafx.modules.core.ui.controller.CompanySelectionController;
import com.econovafx.modules.core.ui.controller.UnitSelectionController;
import com.econovafx.modules.core.ui.controller.DashboardController;
import com.econovafx.modules.core.ui.controller.DatabaseSetupController;
import com.econovafx.modules.core.config.ConfigFileUtil;
import com.econovafx.modules.core.ui.util.ModernDialog;
import com.econovafx.modules.core.ui.view.SplashController;
import com.econovafx.modules.core.ui.view.ViewFactory;
import com.econovafx.modules.security.ui.controller.LoginController;
import java.io.IOException;
import java.util.Locale;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.text.Font;
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
    private Stage loginStage;
    private SplashController splashController;
    private LoginController loginController;
    private BackupSchedulerService backupSchedulerService;

    @Override
    public void init() throws Exception {
        logger.info("Initializing EconoNova FX Application v{}", VERSION);
        
        // Load Lato font from resources
        Font.loadFont(getClass().getResourceAsStream("/fonts/Lato-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Lato-Bold.ttf"), 12);
        logger.info("Lato font family loaded successfully");
        
        // Initialize internationalization with default locale (Spanish - Cuba)
        I18nManager.init(new Locale("es", "CU"));
        logger.info("I18n initialized with locale: {}", I18nManager.getCurrentLocale());

        // NOTE: Do NOT initialize AppContext here. AppConfig uses a static block
        // that reads Avaje Config values at class-load time. To allow an external
        // properties file to be provided by the database setup assistant before
        // AppConfig is loaded, the AppContext initialization is deferred until
        // after the assistant finishes (if required). See start().
    }

    @Override
    public void start(Stage stage) {
        logger.info("Starting JavaFX application");
        this.primaryStage = stage;

        try {
            // If this is the first run (no external config file present), show the
            // database setup assistant before doing anything that may trigger
            // AppConfig (Avaje Config) to load. The assistant will persist an
            // external properties file in the user's home directory and set the
            // system property so Avaje Config picks it up prior to class loading.
            if (!showDatabaseSetupIfNeeded()) {
                // User cancelled the setup assistant - exit application
                logger.info("Database setup cancelled by user. Exiting.");
                System.exit(0);
            }

            // Now it's safe to show the splash screen which will begin DB init
            showSplashScreen();

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            throw new RuntimeException("Failed to start application", e);
        }
    }

    /**
     * Called when the splash finishes initialization. Ensure AppContext is
     * created now (so controllers can be resolved from DI) and then show login.
     */
    private void onSplashInitializationComplete() {
        if (context == null) {
            logger.info("Creating application context after DB initialization...");
            context = AppContext.getInstance();
            logger.info("Application context initialized");
        }
        showLoginScreen();
    }

    /**
     * Shows the database setup assistant if the external config file does not
     * exist. Returns true when the app should continue, false if the user
     * cancelled and the app must exit.
     */
    private boolean showDatabaseSetupIfNeeded() {
        try {
            java.nio.file.Path configFile = ConfigFileUtil.getDefaultConfigFile();

            if (java.nio.file.Files.exists(configFile)) {
                // If external config exists, instruct Avaje Config to load it
                System.setProperty("config.file", configFile.toAbsolutePath().toString());
                logger.info("External config found: {}", configFile);
                return true;
            }

            // Show assistant as a modal dialog on primaryStage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/database-setup.fxml"));
            loader.setResources(I18nManager.getBundle());
            VBox root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/theme-tokens.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/login-styles.css").toExternalForm());

            Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(scene);
            dialog.centerOnScreen();

            DatabaseSetupController controller = loader.getController();
            controller.setDialogStage(dialog);

            dialog.showAndWait();

            if (!controller.isSaved()) {
                return false;
            }

            // After successful save, set system property so Avaje Config will
            // load the external file before AppConfig is referenced.
            java.nio.file.Path saved = controller.getSavedConfigPath();
            if (saved != null) {
                System.setProperty("config.file", saved.toAbsolutePath().toString());
                logger.info("External configuration written to {}", saved);
            }

            return true;

        } catch (IOException e) {
            logger.error("Failed to show database setup assistant", e);
            return false;
        }
    }    
    private void showSplashScreen() {
        try {
            splashStage = new Stage();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/splash.fxml"));
            loader.setResources(I18nManager.getBundle());
            StackPane root = loader.load();
            splashController = loader.getController();
            
            Scene splashScene = new Scene(root);
            // Load theme tokens first for global variables
            splashScene.getStylesheets().add(getClass().getResource("/css/theme-tokens.css").toExternalForm());
            splashScene.getStylesheets().add(getClass().getResource("/css/splash.css").toExternalForm());
            splashStage.setScene(splashScene);
            splashStage.setTitle("EconoNova FX - Loading");
            splashStage.setResizable(false);
            splashStage.initStyle(StageStyle.UNDECORATED);
            splashStage.centerOnScreen();
            splashStage.show();
            
            // Set callback for when initialization is complete. Ensure the DI
            // context is created right after DB initialization and before showing
            // the login screen so controllers can be resolved from the container.
            splashController.setOnInitializationComplete(this::onSplashInitializationComplete);
            
        } catch (IOException e) {
            logger.error("Failed to load splash screen", e);
            throw new RuntimeException("Failed to load splash screen", e);
        }
    }

    private void showLoginScreen() {
        try {
            logger.info("Showing login screen...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            loader.setResources(I18nManager.getBundle());
            // LoginController requires constructor injection (AuthService), so resolve it
            // from the DI container instead of letting FXMLLoader use the no-arg constructor.
            loader.setControllerFactory(cls -> context.getBeanScope().get(cls));
            VBox root = loader.load();
            loginController = loader.getController();
            
            Scene loginScene = new Scene(root);
            // Load theme tokens first for global variables
            loginScene.getStylesheets().add(getClass().getResource("/css/theme-tokens.css").toExternalForm());
            loginScene.getStylesheets().add(getClass().getResource("/css/login-styles.css").toExternalForm());
            
            loginStage = new Stage();
            loginStage.setScene(loginScene);
            loginStage.setTitle("EconoNova FX - Login");
            loginStage.setResizable(false);
            loginStage.initStyle(StageStyle.UNDECORATED);
            loginStage.show();
            loginStage.centerOnScreen();
            
            // Set callback for successful login
            loginController.setOnLoginSuccess(this::loadMainAppAndShowCompanySelection);
            
            // Close splash and show login
            if (splashStage != null) {
                splashStage.close();
            }
            
            logger.info("Login screen displayed successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load login screen", e);
            throw new RuntimeException("Failed to load login screen", e);
        }
    }

    /**
     * Loads the main app first, then shows company selection as a modal overlay.
     */
    private void loadMainAppAndShowCompanySelection() {
        // First load the main app with empty dashboard
        loadMainApp();
        
        // Then show company selection as modal overlay on top of the dashboard
        showCompanySelectionModal();
    }

    private Stage companySelectionStage;
    private Stage unitSelectionStage;
    private Company selectedCompany;

    /**
     * Shows company selection as a modal overlay on top of the main dashboard.
     */
    private void showCompanySelectionModal() {
        try {
            logger.info("Showing company selection modal overlay...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/company-selection.fxml"));
            loader.setResources(I18nManager.getBundle());
            loader.setControllerFactory(cls -> context.getBeanScope().get(cls));
            VBox root = loader.load();
            CompanySelectionController controller = loader.getController();
            
            // Apply styles - theme tokens first, then component styles
            root.getStylesheets().add(getClass().getResource("/css/theme-tokens.css").toExternalForm());
            root.getStylesheets().add(getClass().getResource("/css/selection-dialog-styles.css").toExternalForm());
            
            // Show as modal using ModernDialog and capture the handle for programmatic control
            ModernDialog.DialogHandle handle = ModernDialog.showModal(primaryStage, root, "Seleccionar Empresa");
            
            // Set callbacks
            controller.setOnCompanySelected(() -> {
                // Company selected, now check if it has units
                selectedCompany = TenantContext.getCurrentTenant();
                
                // Close the modal overlay before proceeding
                handle.close();
                
                checkAndShowUnitSelectionModal();
            });
            
            controller.setOnCancel(() -> {
                logger.info("Company selection cancelled, exiting application");
                // Close the modal overlay before exiting (cleaner, though exit handles it)
                handle.close();
                System.exit(0);
            });
            
            logger.info("Company selection modal displayed successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load company selection modal", e);
            throw new RuntimeException("Failed to load company selection modal", e);
        }
    }

    private void showCompanySelection() {
        try {
            logger.info("Showing company selection dialog...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/company-selection.fxml"));
            loader.setResources(I18nManager.getBundle());
            loader.setControllerFactory(cls -> context.getBeanScope().get(cls));
            VBox root = loader.load();
            CompanySelectionController controller = loader.getController();
            
            Scene selectionScene = new Scene(root);
            selectionScene.getStylesheets().add(getClass().getResource("/css/selection-dialog-styles.css").toExternalForm());
            
            companySelectionStage = new Stage();
            companySelectionStage.setScene(selectionScene);
            companySelectionStage.setTitle("Select Company");
            companySelectionStage.setResizable(false);
            companySelectionStage.initStyle(StageStyle.UNDECORATED);
            companySelectionStage.initModality(Modality.APPLICATION_MODAL);
            companySelectionStage.initOwner(loginStage);
            companySelectionStage.centerOnScreen();
            
            // Set callbacks
            controller.setOnCompanySelected(() -> {
                // Company selected, now check if it has units
                selectedCompany = TenantContext.getCurrentTenant();
                companySelectionStage.close();
                checkAndShowUnitSelection();
            });
            
            controller.setOnCancel(() -> {
                companySelectionStage.close();
                System.exit(0);
            });
            
            // Close login and show company selection
            if (loginStage != null) {
                loginStage.close();
            }
            companySelectionStage.show();
            
            logger.info("Company selection dialog displayed successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load company selection dialog", e);
            throw new RuntimeException("Failed to load company selection dialog", e);
        }
    }

    /**
     * Checks if the selected company has business units and shows unit selection modal if needed.
     */
    private void checkAndShowUnitSelectionModal() {
        if (selectedCompany == null) {
            logger.error("No company selected");
            refreshDashboard();
            return;
        }
        
        try {
            BusinessUnitService unitService = context.getBeanScope().get(BusinessUnitService.class);
            boolean hasUnits = unitService.hasUnits(selectedCompany.getId());
            
            if (hasUnits) {
                logger.info("Company {} has business units, showing unit selection modal", selectedCompany.getName());
                showUnitSelectionModal();
            } else {
                logger.info("Company {} has no business units, refreshing dashboard", selectedCompany.getName());
                refreshDashboard();
            }
        } catch (Exception e) {
            logger.error("Error checking business units, proceeding to refresh dashboard", e);
            refreshDashboard();
        }
    }

    /**
     * Shows business unit selection as a modal overlay on top of the main dashboard.
     */
    private void showUnitSelectionModal() {
        try {
            logger.info("Showing business unit selection modal overlay...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/unit-selection.fxml"));
            loader.setResources(I18nManager.getBundle());
            loader.setControllerFactory(cls -> context.getBeanScope().get(cls));
            VBox root = loader.load();
            UnitSelectionController controller = loader.getController();
            
            // Pass the selected company to the controller
            controller.setCompany(selectedCompany);
            
            // Apply styles - theme tokens first, then component styles
            root.getStylesheets().add(getClass().getResource("/css/theme-tokens.css").toExternalForm());
            root.getStylesheets().add(getClass().getResource("/css/selection-dialog-styles.css").toExternalForm());
            
            // Show as modal using ModernDialog and capture the handle for programmatic control
            ModernDialog.DialogHandle handle = ModernDialog.showModal(primaryStage, root, "Seleccionar Unidad de Negocio");
            
            // Set callbacks
            controller.setOnUnitSelected(() -> {
                logger.info("Business unit selected, refreshing dashboard");
                // Close the modal overlay before proceeding
                handle.close();
                refreshDashboard();
            });
            
            controller.setOnUnitSkipped(() -> {
                logger.info("Business unit skipped, refreshing dashboard");
                // Close the modal overlay before proceeding
                handle.close();
                refreshDashboard();
            });
            
            controller.setOnCancel(() -> {
                logger.info("Unit selection cancelled, returning to company selection");
                // Close the modal overlay before showing company selection to avoid stacking overlays
                handle.close();
                showCompanySelectionModal();
            });
            
            logger.info("Business unit selection modal displayed successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load unit selection modal", e);
            throw new RuntimeException("Failed to load unit selection modal", e);
        }
    }

    private void checkAndShowUnitSelection() {
        if (selectedCompany == null) {
            logger.error("No company selected");
            loadMainApp();
            return;
        }
        
        try {
            BusinessUnitService unitService = context.getBeanScope().get(BusinessUnitService.class);
            boolean hasUnits = unitService.hasUnits(selectedCompany.getId());
            
            if (hasUnits) {
                logger.info("Company {} has business units, showing unit selection", selectedCompany.getName());
                showUnitSelection();
            } else {
                logger.info("Company {} has no business units, proceeding to main app", selectedCompany.getName());
                loadMainApp();
            }
        } catch (Exception e) {
            logger.error("Error checking business units, proceeding to main app", e);
            loadMainApp();
        }
    }

    private void showUnitSelection() {
        try {
            logger.info("Showing business unit selection dialog...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/unit-selection.fxml"));
            loader.setResources(I18nManager.getBundle());
            loader.setControllerFactory(cls -> context.getBeanScope().get(cls));
            VBox root = loader.load();
            UnitSelectionController controller = loader.getController();
            
            // Pass the selected company to the controller
            controller.setCompany(selectedCompany);
            
            Scene selectionScene = new Scene(root);
            selectionScene.getStylesheets().add(getClass().getResource("/css/selection-dialog-styles.css").toExternalForm());
            
            unitSelectionStage = new Stage();
            unitSelectionStage.setScene(selectionScene);
            unitSelectionStage.setTitle("Select Business Unit");
            unitSelectionStage.setResizable(false);
            unitSelectionStage.initStyle(StageStyle.UNDECORATED);
            unitSelectionStage.initModality(Modality.APPLICATION_MODAL);
            unitSelectionStage.initOwner(companySelectionStage);
            unitSelectionStage.centerOnScreen();
            
            // Set callbacks
            controller.setOnUnitSelected(() -> {
                unitSelectionStage.close();
                loadMainApp();
            });
            
            controller.setOnUnitSkipped(() -> {
                unitSelectionStage.close();
                loadMainApp();
            });
            
            controller.setOnCancel(() -> {
                unitSelectionStage.close();
                // Return to company selection
                showCompanySelection();
            });
            
            companySelectionStage.close();
            unitSelectionStage.show();
            
            logger.info("Business unit selection dialog displayed successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load unit selection dialog", e);
            throw new RuntimeException("Failed to load unit selection dialog", e);
        }
    }

    private void loadMainApp() {
        try {
            logger.info("Loading main application view...");
            
            // Close login dialog
            if (loginStage != null) {
                loginStage.close();
            }
            
            ViewFactory viewFactory = context.getViewFactory();
            MainViewController mainController = new MainViewController(
                    context.getAccountService(),
                    context.getTransactionService(),
                    context.getUserService()
            );
            // Initialize the viewFactory reference in the controller
            mainController.initializeViewFactory(viewFactory);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            loader.setResources(I18nManager.getBundle());
            loader.setControllerFactory(cls -> mainController);

            Scene scene = new Scene(loader.load(), 1200, 800);

            // Set background color to match theme (bg-gray-50) to prevent black flash
            scene.setFill(javafx.scene.paint.Color.web("#f9fafb"));

            // Add all stylesheets in correct order - theme tokens first!
            scene.getStylesheets().add(getClass().getResource("/css/theme-tokens.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/main-styles.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

            primaryStage.setTitle("EconoNova FX - Accounting System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            primaryStage.centerOnScreen();
            
            // Force CSS resolution and layout before showing to prevent black flash
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            
            // Ensure root is fully opaque before showing to prevent empty background flash
            scene.getRoot().setOpacity(1.0);
            
            primaryStage.show(); // Explicitly show the primary stage
            
            logger.info("Main application window displayed successfully");
            logger.info("Application started successfully");
            
            // Start backup scheduler after main app is loaded
            startBackupScheduler();

        } catch (IOException e) {
            logger.error("Failed to load main view", e);
            throw new RuntimeException("Failed to load main view", e);
        }
    }
    
    /**
     * Starts the backup scheduler service if auto-backup is enabled.
     */
    private void startBackupScheduler() {
        try {
            // Get the BackupSchedulerService from the DI container
            backupSchedulerService = context.getBeanScope().get(BackupSchedulerService.class);
            if (backupSchedulerService != null) {
                backupSchedulerService.startScheduler();
                logger.info("Backup scheduler service started");
            } else {
                logger.warn("BackupSchedulerService not available in DI container");
            }
        } catch (Exception e) {
            logger.error("Error starting backup scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Refreshes the dashboard data after company/unit selection.
     * Gets the DashboardController from the ViewFactory and calls its refresh method.
     */
    private void refreshDashboard() {
        try {
            Scene scene = primaryStage.getScene();
            if (scene != null) {
                // Get the ViewFactory from context and retrieve the DashboardController
                ViewFactory viewFactory = context.getViewFactory();
                if (viewFactory != null) {
                    DashboardController dashboardController = viewFactory.getDashboardController();
                    if (dashboardController != null) {
                        dashboardController.loadDashboardData();
                        logger.info("Dashboard data refreshed");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error refreshing dashboard", e);
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Shutting down application...");
        
        // Stop local web server
        try {
            com.econovafx.modules.core.ui.web.LocalWebServer webServer = context.getBeanScope().get(com.econovafx.modules.core.ui.web.LocalWebServer.class);
            if (webServer != null && webServer.isRunning()) {
                webServer.stop(0);
                logger.info("Local web server stopped");
            }
        } catch (Exception e) {
            logger.warn("Could not stop local web server: {}", e.getMessage());
        }
        
        // Stop backup scheduler
        if (backupSchedulerService != null) {
            backupSchedulerService.stopScheduler();
            logger.info("Backup scheduler service stopped");
        }
        
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
