package com.econovafx.ui.visual;

import com.econovafx.modules.core.config.AppContext;
import com.econovafx.modules.core.repository.*;
import com.econovafx.service.*;
import com.econovafx.modules.core.ui.controller.*;
import com.econovafx.modules.core.ui.view.ViewFactory;
import io.ebean.Database;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.net.URL;

import static com.econovafx.ui.visual.VisualTestUtils.captureAndSave;

public class MainViewVisualTest extends ApplicationTest {

    private Stage primaryStage;
    private Parent rootNode;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Create minimal ViewFactory with null services for visual-only test
        ViewFactory viewFactory = new ViewFactory(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        
        MainViewController controller = new MainViewController(null, null, null, viewFactory);
        
        // Load FXML - controller is already specified in FXML file via fx:controller
        URL fxmlUrl = getClass().getResource("/fxml/main-view.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML file not found: /fxml/main-view.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setControllerFactory(cls -> {
            if (cls == MainViewController.class) {
                return controller;
            }
            return null;
        });
        rootNode = loader.load();
        
        Scene scene = new Scene(rootNode, 1280, 800);
        stage.setScene(scene);
        stage.show();
        
        // Force initialization and layout
        WaitForAsyncUtils.waitForFxEvents();
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testMainView() throws IOException {
        captureAndSave(primaryStage, "main-view");
    }
}
