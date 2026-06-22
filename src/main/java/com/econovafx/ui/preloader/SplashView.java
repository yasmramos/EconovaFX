package com.econovafx.ui.preloader;

import com.econovafx.ui.controller.SplashController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Vista del splash screen cargada desde FXML.
 */
public class SplashView {

    private final StackPane root;
    private final SplashController controller;

    public SplashView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/splash.fxml")
            );
            root = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            throw new RuntimeException("Error loading splash view", e);
        }
    }

    public StackPane getView() {
        return root;
    }

    public SplashController getController() {
        return controller;
    }
}
