package com.econovafx.ui.preloader;

import com.econovafx.ui.controller.SplashController;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Native JavaFX preloader that listens to real initialization events.
 */
public class EconoNovaPreloader extends Preloader {

    private Stage stage;
    private SplashView splashView;
    private SplashController controller;

    @Override
    public void init() {
        // Se ejecuta en un hilo separado antes de start()
        System.out.println("Preloader: Initializing application resources...");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        
        // Crear vista del splash
        this.splashView = new SplashView();
        StackPane root = splashView.getView();
        
        Scene scene = new Scene(root, 450, 350);
        String cssPath = getClass().getResource("/css/splash.css").toExternalForm();
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }
        
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Obtener controlador para actualizar UI
        this.controller = splashView.getController();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof ProgressNotification) {
            double progress = ((ProgressNotification) info).getProgress();
            String message = ((ProgressNotification) info).getMessage();
            
            if (controller != null) {
                controller.updateProgress(progress, message);
            }
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
            // La aplicación está lista para mostrarse, ocultar preloader con fade out
            if (stage != null) {
                var fadeTransition = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(500), 
                    stage.getScene().getRoot()
                );
                fadeTransition.setFromValue(1.0);
                fadeTransition.setToValue(0.0);
                fadeTransition.setOnFinished(e -> stage.close());
                fadeTransition.play();
            }
        }
    }

    /**
     * Notificación de progreso personalizada para enviar desde App.java
     */
    public static class ProgressNotification implements PreloaderNotification {
        private final double progress;
        private final String message;

        public ProgressNotification(double progress, String message) {
            this.progress = progress;
            this.message = message;
        }

        public double getProgress() {
            return progress;
        }

        public String getMessage() {
            return message;
        }
    }
}
