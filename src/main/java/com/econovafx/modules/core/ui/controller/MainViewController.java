package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.UserService;
import com.econovafx.modules.core.ui.util.NotificationService;
import com.econovafx.modules.core.ui.view.ViewFactory;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.RotateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Interpolator;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main application controller
 */
@Component
public class MainViewController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainViewController.class);

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;
    private ViewFactory viewFactory;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private ScrollPane contentScrollPane;

    @FXML
    private ScrollPane sidebarScrollPane;

    @FXML
    private VBox sidebarVBox;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnContabilidad;

    @FXML
    private VBox contabilidadSubmenu;

    @FXML
    private FontIcon contabilidadChevron;

    @FXML
    private Button btnComprobantes;

    @FXML
    private Button btnCuentas;

    @FXML
    private Button btnClasificador;

    @FXML
    private Button btnTerceros;

    @FXML
    private Button btnPeriodos;

    @FXML
    private Button btnCierres;

    @FXML
    private Button btnCostos;

    @FXML
    private Button btnTasasCambio;

    @FXML
    private Button btnFinanzas;

    @FXML
    private Button btnAFT;

    @FXML
    private Button btnInventarios;

    @FXML
    private Button btnNominas;

    @FXML
    private Button btnSettings;

    @FXML
    private FontIcon settingsChevron;

    @FXML
    private VBox settingsSubmenu;

    @FXML
    private Button btnPerfil;

    @FXML
    private Button btnAppSettings;

    @FXML
    private Button btnBackup;

    @FXML
    private Button btnHelp;

    @FXML
    private Button btnAbout;

    @FXML
    private Button btnToggleSidebar;

    private boolean sidebarCollapsed = false;
    private static final double SIDEBAR_EXPANDED_WIDTH = 260;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 64;

    private Button activeButton;

    public MainViewController(AccountService accountService,
                             TransactionService transactionService,
                             UserService userService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    /**
     * Initialize ViewFactory reference (two-phase initialization pattern)
     */
    public void initializeViewFactory(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    /**
     * Complete initialization after ViewFactory is fully constructed
     */
    public void completeInitialization(ViewFactory viewFactory) {
        // Additional initialization if needed
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("MainViewController initialized");
        currentUserLabel.setText("User: Administrator");
        
        // Bind sidebar VBox minHeight to ScrollPane height so spacer works
        if (sidebarVBox != null && sidebarScrollPane != null) {
            sidebarVBox.minHeightProperty().bind(sidebarScrollPane.heightProperty());
        }
        
        // Defer dashboard loading to ensure scene is fully ready
        javafx.application.Platform.runLater(() -> {
            try {
                showDashboard();
                // Show welcome notification
                NotificationService.showInfo(getStage(), "Welcome to EconoNova FX v1.0.0");
            } catch (Exception e) {
                logger.error("Error during dashboard initialization", e);
            }
        });
    }

    /**
     * Toggle sidebar between expanded and collapsed state with animation.
     * When collapsed, only icons are visible; when expanded, icons + text are shown.
     */
    @FXML
    private void toggleSidebar() {
        if (sidebarVBox == null || sidebarScrollPane == null) {
            return;
        }

        sidebarCollapsed = !sidebarCollapsed;

        if (sidebarCollapsed) {
            collapseSidebar();
        } else {
            expandSidebar();
        }
    }

    /**
     * Collapse the sidebar to icon-only mode with animation.
     */
    private void collapseSidebar() {
        // Close any open submenus first
        if (contabilidadSubmenu != null && contabilidadSubmenu.isVisible()) {
            animateSubmenu(contabilidadSubmenu, contabilidadChevron, btnContabilidad);
        }
        if (settingsSubmenu != null && settingsSubmenu.isVisible()) {
            animateSubmenu(settingsSubmenu, settingsChevron, btnSettings);
        }

        // Add collapsed style class
        sidebarVBox.getStyleClass().add("sidebar-collapsed");

        // Animate width change
        double targetWidth = SIDEBAR_COLLAPSED_WIDTH;
        Timeline timeline = new Timeline();
        
        // Animate sidebarScrollPane width
        KeyValue scrollPanePrefWidthKV = new KeyValue(sidebarScrollPane.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyValue scrollPaneMinWidthKV = new KeyValue(sidebarScrollPane.minWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyValue scrollPaneMaxWidthKV = new KeyValue(sidebarScrollPane.maxWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyFrame scrollPaneKF = new KeyFrame(Duration.millis(200), scrollPanePrefWidthKV, scrollPaneMinWidthKV, scrollPaneMaxWidthKV);
        
        // Animate sidebarVBox width
        KeyValue vboxPrefWidthKV = new KeyValue(sidebarVBox.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyValue vboxMinWidthKV = new KeyValue(sidebarVBox.minWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyValue vboxMaxWidthKV = new KeyValue(sidebarVBox.maxWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyFrame vboxKF = new KeyFrame(Duration.millis(200), vboxPrefWidthKV, vboxMinWidthKV, vboxMaxWidthKV);
        
        timeline.getKeyFrames().addAll(scrollPaneKF, vboxKF);
        timeline.play();

        // Set all menu buttons to icon-only mode
        setAllButtonsContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);

        // Update toggle button icon
        if (btnToggleSidebar != null && btnToggleSidebar.getGraphic() instanceof FontIcon) {
            FontIcon icon = (FontIcon) btnToggleSidebar.getGraphic();
            icon.setIconLiteral("mdi2c-chevron-right");
        }
    }

    /**
     * Expand the sidebar to full width with animation.
     */
    private void expandSidebar() {
        // Remove collapsed style class
        sidebarVBox.getStyleClass().remove("sidebar-collapsed");

        // Animate width change
        double targetWidth = SIDEBAR_EXPANDED_WIDTH;
        Timeline timeline = new Timeline();
        
        // Animate sidebarScrollPane width
        KeyValue scrollPanePrefWidthKV = new KeyValue(sidebarScrollPane.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyValue scrollPaneMinWidthKV = new KeyValue(sidebarScrollPane.minWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyValue scrollPaneMaxWidthKV = new KeyValue(sidebarScrollPane.maxWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyFrame scrollPaneKF = new KeyFrame(Duration.millis(200), scrollPanePrefWidthKV, scrollPaneMinWidthKV, scrollPaneMaxWidthKV);
        
        // Animate sidebarVBox width
        KeyValue vboxPrefWidthKV = new KeyValue(sidebarVBox.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyValue vboxMinWidthKV = new KeyValue(sidebarVBox.minWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyValue vboxMaxWidthKV = new KeyValue(sidebarVBox.maxWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyFrame vboxKF = new KeyFrame(Duration.millis(200), vboxPrefWidthKV, vboxMinWidthKV, vboxMaxWidthKV);
        
        timeline.getKeyFrames().addAll(scrollPaneKF, vboxKF);
        timeline.play();

        // Restore all menu buttons to show icon + text
        setAllButtonsContentDisplay(javafx.scene.control.ContentDisplay.LEFT);

        // Update toggle button icon
        if (btnToggleSidebar != null && btnToggleSidebar.getGraphic() instanceof FontIcon) {
            FontIcon icon = (FontIcon) btnToggleSidebar.getGraphic();
            icon.setIconLiteral("mdi2c-chevron-left");
        }
    }

    /**
     * Set content display mode for all sidebar menu buttons.
     */
    private void setAllButtonsContentDisplay(javafx.scene.control.ContentDisplay display) {
        Button[] buttons = {
            btnDashboard, btnContabilidad, btnComprobantes, btnCuentas, btnClasificador,
            btnTerceros, btnPeriodos, btnCierres, btnCostos, btnTasasCambio,
            btnFinanzas, btnAFT, btnInventarios, btnNominas, btnSettings,
            btnPerfil, btnAppSettings, btnBackup, btnHelp, btnAbout
        };
        
        for (Button btn : buttons) {
            if (btn != null) {
                btn.setContentDisplay(display);
            }
        }
    }

    private void setActiveButton(Button button) {
        // Remove active class from previous button
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-btn-active");
        }

        // Add active class to new button
        if (button != null) {
            button.getStyleClass().add("sidebar-btn-active");
            activeButton = button;
        }
    }
    
    @FXML
    private void showDashboard() {
        logger.debug("Showing dashboard");
        setActiveButton(btnDashboard);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createDashboardView());
        updateStatus("Dashboard");
    }

    @FXML
    private void showTransactions() {
        logger.debug("Showing transactions");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createTransactionsView());
        updateStatus("Transacciones");
    }

    @FXML
    private void showAccounts() {
        logger.debug("Showing accounts");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createAccountsView());
        updateStatus("Cuentas");
    }

    @FXML
    private void showReports() {
        logger.debug("Showing reports");
        updateStatus("Reportes - Próximamente");
    }

    @FXML
    private void showUsers() {
        logger.debug("Showing users");
        updateStatus("Usuarios - Próximamente");
    }

    @FXML
    private void showSettings() {
        logger.debug("Showing settings");
        setActiveButton(btnSettings);
        updateStatus("Configuración - Próximamente");
    }

    /**
     * Helper method to animate submenu expansion/collapse with accordion effect
     */
    private void animateSubmenu(VBox submenu, FontIcon chevron, Button triggerButton) {
        if (submenu == null || triggerButton == null) {
            return;
        }
        
        boolean isNowExpanded = submenu.isVisible();
        
        // Stop any ongoing animation to prevent conflicts
        // (Timeline will be created fresh each time)
        
        if (!isNowExpanded) {
            // OPENING: expand accordion-style
            submenu.setManaged(true);
            submenu.setVisible(true);
            submenu.setOpacity(0);
            
            // Apply CSS and layout to get accurate preferred height
            submenu.applyCss();
            submenu.layout();
            double targetHeight = submenu.prefHeight(-1);
            
            // Set initial height to 0 for animation
            submenu.setMinHeight(0);
            submenu.setPrefHeight(0);
            
            // Create timeline to animate height and opacity
            Timeline timeline = new Timeline();
            
            // Animate height from 0 to targetHeight
            KeyValue heightKV = new KeyValue(submenu.prefHeightProperty(), targetHeight, Interpolator.EASE_BOTH);
            KeyFrame heightKF = new KeyFrame(Duration.millis(200), heightKV);
            
            // Animate opacity from 0 to 1
            KeyValue opacityKV = new KeyValue(submenu.opacityProperty(), 1, Interpolator.EASE_BOTH);
            KeyFrame opacityKF = new KeyFrame(Duration.millis(200), opacityKV);
            
            timeline.getKeyFrames().addAll(heightKF, opacityKF);
            
            // On finish: restore natural sizing
            timeline.setOnFinished(e -> {
                submenu.setPrefHeight(Region.USE_COMPUTED_SIZE);
                submenu.setMinHeight(Region.USE_COMPUTED_SIZE);
            });
            
            timeline.play();
            
            // Rotate chevron to 180 degrees (pointing up)
            if (chevron != null) {
                RotateTransition rotate = new RotateTransition(Duration.millis(200), chevron);
                rotate.setAxis(Rotate.Z_AXIS);
                rotate.setFromAngle(chevron.getRotate());
                rotate.setToAngle(180);
                rotate.setCycleCount(1);
                rotate.setAutoReverse(false);
                // NO setOnFinished that resets rotation - let it stay at 180
                rotate.play();
            }
            
            setActiveButton(triggerButton);
            
        } else {
            // CLOSING: collapse accordion-style
            double currentHeight = submenu.getHeight();
            
            // Create timeline to animate height to 0 and opacity to 0
            Timeline timeline = new Timeline();
            
            // Animate height from current to 0
            KeyValue heightKV = new KeyValue(submenu.prefHeightProperty(), 0, Interpolator.EASE_BOTH);
            KeyFrame heightKF = new KeyFrame(Duration.millis(200), heightKV);
            
            // Animate opacity from 1 to 0
            KeyValue opacityKV = new KeyValue(submenu.opacityProperty(), 0, Interpolator.EASE_BOTH);
            KeyFrame opacityKF = new KeyFrame(Duration.millis(200), opacityKV);
            
            timeline.getKeyFrames().addAll(heightKF, opacityKF);
            
            // On finish: hide and restore natural sizing
            timeline.setOnFinished(e -> {
                submenu.setVisible(false);
                submenu.setManaged(false);
                submenu.setPrefHeight(Region.USE_COMPUTED_SIZE);
                submenu.setMinHeight(Region.USE_COMPUTED_SIZE);
            });
            
            timeline.play();
            
            // Rotate chevron to 0 degrees (pointing down)
            if (chevron != null) {
                RotateTransition rotate = new RotateTransition(Duration.millis(200), chevron);
                rotate.setAxis(Rotate.Z_AXIS);
                rotate.setFromAngle(chevron.getRotate());
                rotate.setToAngle(0);
                rotate.setCycleCount(1);
                rotate.setAutoReverse(false);
                // NO setOnFinished that resets rotation - let it stay at 0
                rotate.play();
            }
        }
    }

    @FXML
    private void toggleSettingsMenu() {
        animateSubmenu(settingsSubmenu, settingsChevron, btnSettings);
    }

    @FXML
    private void showPerfil() {
        logger.debug("Showing perfil");
        setActiveButton(btnPerfil);
        updateStatus("Mi Perfil - Próximamente");
    }

    @FXML
    private void showAppSettings() {
        logger.debug("Showing system settings");
        setActiveButton(btnAppSettings);
        try {
            viewFactory.loadSystemSettings();
            updateStatus("Configuración del Sistema");
        } catch (Exception e) {
            logger.error("Error loading system settings", e);
            NotificationService.showError(getStage(), "Error al cargar la configuración: " + e.getMessage());
        }
    }

    @FXML
    private void showBackup() {
        logger.debug("Showing backup");
        setActiveButton(btnBackup);
        updateStatus("Copia de Seguridad - Próximamente");
    }

    @FXML
    private void showHelp() {
        logger.debug("Showing help");
        setActiveButton(btnHelp);
        updateStatus("Ayuda - Próximamente");
    }

    @FXML
    private void showAbout() {
        logger.debug("Showing about");
        setActiveButton(btnAbout);
        updateStatus("EconoNova FX v1.0.0");
    }

    @FXML
    private void toggleContabilidadMenu() {
        animateSubmenu(contabilidadSubmenu, contabilidadChevron, btnContabilidad);
    }

    @FXML
    private void showComprobantes() {
        logger.debug("Showing comprobantes de operaciones");
        setActiveButton(btnComprobantes);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createComprobantesView());
        updateStatus("Comprobantes de Operaciones");
    }

    @FXML
    private void showCuentas() {
        logger.debug("Showing cuentas contables");
        setActiveButton(btnCuentas);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createAccountsView());
        updateStatus("Cuentas Contables");
    }

    @FXML
    private void showClasificador() {
        logger.debug("Showing clasificador de cuentas");
        setActiveButton(btnClasificador);
        updateStatus("Clasificador de Cuentas - Coming Soon");
    }

    @FXML
    private void showTerceros() {
        logger.debug("Showing third parties management");
        setActiveButton(btnTerceros);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createThirdPartiesView());
        updateStatus("Third Parties (Customers/Suppliers)");
    }

    @FXML
    private void showPeriodos() {
        logger.debug("Showing accounting periods management");
        setActiveButton(btnPeriodos);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createAccountingPeriodsView());
        updateStatus("Accounting Periods Management");
    }

    @FXML
    private void showCierres() {
        logger.debug("Showing accounting closures management");
        setActiveButton(btnCierres);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createAccountingClosuresView());
        updateStatus("Accounting Closures Management");
    }

    @FXML
    private void showCostos() {
        logger.debug("Showing costos y procesos");
        setActiveButton(btnCostos);
        updateStatus("Costos y Procesos - Coming Soon");
    }

    @FXML
    private void showContabilidad() {
        logger.debug("Showing contabilidad");
        setActiveButton(btnContabilidad);
        updateStatus("Contabilidad - Próximamente");
    }

    @FXML
    private void showFinanzas() {
        logger.debug("Showing finanzas");
        setActiveButton(btnFinanzas);
        updateStatus("Finanzas - Próximamente");
    }

    @FXML
    private void showAFT() {
        logger.debug("Showing AFT");
        setActiveButton(btnAFT);
        updateStatus("AFT - Próximamente");
    }

    @FXML
    private void showInventarios() {
        logger.debug("Showing inventarios");
        setActiveButton(btnInventarios);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createInventoryView());
        updateStatus("Inventarios");
    }

    @FXML
    private void showNominas() {
        logger.debug("Showing nóminas");
        setActiveButton(btnNominas);
        updateStatus("Nóminas - Próximamente");
    }

    @FXML
    private void showTasasCambio() {
        logger.debug("Showing exchange rates");
        setActiveButton(btnTasasCambio);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(viewFactory.createExchangeRatesView());
        updateStatus("Tasas de Cambio");
    }

    @FXML
    private void handleLogout() {
        logger.info("User logged out");
        NotificationService.showInfo(getStage(), "Logging out...");
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    private javafx.stage.Stage getStage() {
        if (contentArea != null && contentArea.getScene() != null) {
            return (javafx.stage.Stage) contentArea.getScene().getWindow();
        }
        return null;
    }
    
    public AccountService getAccountService() {
        return accountService;
    }
    
    public TransactionService getTransactionService() {
        return transactionService;
    }
    
    public UserService getUserService() {
        return userService;
    }

    /**
     * Returns the root node of this view for visual testing
     */
    public javafx.scene.layout.VBox getRootNode() {
        return sidebarVBox;
    }
}
