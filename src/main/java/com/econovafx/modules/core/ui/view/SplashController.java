package com.econovafx.modules.core.ui.view;

import com.econovafx.modules.core.config.DatabaseConfig;
import com.econovafx.modules.core.security.AuthService;
import com.econovafx.modules.core.service.CompanyService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import io.ebean.DB;
import java.util.concurrent.CompletableFuture;

public class SplashController {

    @FXML
    private StackPane rootPane;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    @FXML
    private Label versionLabel;

    private Runnable onInitializationComplete;

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
                updateProgress(0.1, "Cargando configuración de base de datos...");
                DatabaseConfig.initialize();
                
                updateProgress(0.3, "Verificando conexión a la base de datos...");
                DB.getDefault();
                
                updateProgress(0.5, "Inicializando servicios del sistema...");
                // Forzar inicialización de servicios
                CompanyService companyService = new CompanyService();
                
                updateProgress(0.7, "Cargando módulos principales...");
                Thread.sleep(500); // Pequeña pausa para permitir renderizado
                
                updateProgress(0.9, "Preparando interfaz de usuario...");
                Thread.sleep(300);
                
                updateProgress(1.0, "Aplicación lista!");
                
                // Transición suave hacia la app principal
                javafx.application.Platform.runLater(() -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(800), rootPane);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> {
                        if (onInitializationComplete != null) {
                            onInitializationComplete.run();
                        }
                    });
                    fadeOut.play();
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                });
                e.printStackTrace();
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
