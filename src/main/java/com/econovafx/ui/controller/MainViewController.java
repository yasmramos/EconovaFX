package com.econovafx.ui.controller;

import com.econovafx.service.AccountService;
import com.econovafx.service.TransactionService;
import com.econovafx.service.UserService;
import com.econovafx.ui.view.ViewFactory;
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
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main application controller
 */
public class MainViewController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainViewController.class);

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final ViewFactory viewFactory;

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
    private Label contabilidadChevron;

    @FXML
    private Button btnComprobantes;

    @FXML
    private Button btnClasificador;

    @FXML
    private Button btnCostos;

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

    private Button activeButton;

    public MainViewController(AccountService accountService,
                             TransactionService transactionService,
                             UserService userService,
                             ViewFactory viewFactory) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.viewFactory = viewFactory;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("MainViewController initialized");
        currentUserLabel.setText("Usuario: Administrador");
        
        // Bind sidebar VBox minHeight to ScrollPane height so spacer works
        if (sidebarVBox != null && sidebarScrollPane != null) {
            sidebarVBox.minHeightProperty().bind(sidebarScrollPane.heightProperty());
        }
        
        showDashboard();
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

    @FXML
    private void toggleSettingsMenu() {
        boolean isNowExpanded = settingsSubmenu.isVisible();
        settingsSubmenu.setVisible(!isNowExpanded);
        settingsSubmenu.setManaged(!isNowExpanded);
        if (!isNowExpanded) {
            setActiveButton(btnSettings);
        }
    }

    @FXML
    private void showPerfil() {
        logger.debug("Showing perfil");
        setActiveButton(btnPerfil);
        updateStatus("Mi Perfil - Próximamente");
    }

    @FXML
    private void showAppSettings() {
        logger.debug("Showing app settings");
        setActiveButton(btnAppSettings);
        updateStatus("Ajustes de la App - Próximamente");
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
        boolean isNowExpanded = contabilidadSubmenu.isVisible();
        
        if (!isNowExpanded) {
            // Opening: show with fade in
            contabilidadSubmenu.setManaged(true);
            contabilidadSubmenu.setOpacity(0);
            contabilidadSubmenu.setVisible(true);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), contabilidadSubmenu);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            // Closing: fade out then hide
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contabilidadSubmenu);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                contabilidadSubmenu.setVisible(false);
                contabilidadSubmenu.setManaged(false);
            });
            fadeOut.play();
        }
        
        if (contabilidadChevron != null) {
            // Rotate animation for chevron
            RotateTransition rotate = new RotateTransition(Duration.millis(200), contabilidadChevron);
            rotate.setAxis(Rotate.Z_AXIS);
            rotate.setFromAngle(isNowExpanded ? 180 : 0);
            rotate.setToAngle(isNowExpanded ? 0 : 180);
            rotate.setCycleCount(1);
            rotate.setAutoReverse(false);
            rotate.setOnFinished(e -> {
                contabilidadChevron.setText("▼");
                contabilidadChevron.setRotate(0);
            });
            rotate.play();
        }
        
        if (!isNowExpanded) {
            setActiveButton(btnContabilidad);
        }
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
    private void showClasificador() {
        logger.debug("Showing clasificador de cuentas");
        setActiveButton(btnClasificador);
        updateStatus("Clasificador de Cuentas - Próximamente");
    }

    @FXML
    private void showCostos() {
        logger.debug("Showing costos y procesos");
        setActiveButton(btnCostos);
        updateStatus("Costos y Procesos - Próximamente");
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
        updateStatus("Inventarios - Próximamente");
    }

    @FXML
    private void showNominas() {
        logger.debug("Showing nóminas");
        setActiveButton(btnNominas);
        updateStatus("Nóminas - Próximamente");
    }

    @FXML
    private void handleLogout() {
        logger.info("User logged out");
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
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
}
