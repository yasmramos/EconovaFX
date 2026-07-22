package com.econovafx.modules.core.ui.util;

import com.econovafx.modules.core.ui.util.NotificationService;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import static com.econovafx.modules.core.ui.util.VisualTestUtils.captureNode;

/**
 * Visual tests for Notification Toast component.
 * Captures screenshots of all notification types and saves them to docs/images/
 */
public class NotificationToastVisualTest extends ApplicationTest {

    private Stage primaryStage;
    private Pane notificationContainer;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        VBox mainLayout = new VBox(20);
        mainLayout.setStyle("-fx-padding: 30; -fx-background-color: #f0f2f5; -fx-alignment: center;");
        mainLayout.setSpacing(15);
        
        Label title = new Label("Notification Toast Visual Test");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button successBtn = createButton("Show Success", "#28a745");
        successBtn.setOnAction(e -> NotificationService.showSuccess(primaryStage, "Operation completed successfully!"));
        
        Button errorBtn = createButton("Show Error", "#dc3545");
        errorBtn.setOnAction(e -> NotificationService.showError(primaryStage, "An error occurred during processing."));
        
        Button warningBtn = createButton("Show Warning", "#ffc107");
        warningBtn.setOnAction(e -> NotificationService.showWarning(primaryStage, "Please review the input data."));
        
        Button infoBtn = createButton("Show Info", "#17a2b8");
        infoBtn.setOnAction(e -> NotificationService.showInfo(primaryStage, "New update available."));
        
        buttonContainer.getChildren().addAll(successBtn, errorBtn, warningBtn, infoBtn);
        mainLayout.getChildren().addAll(title, buttonContainer);
        
        Scene scene = new Scene(mainLayout, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Notification Toast Visual Test");
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(NotificationToastVisualTest.TestApp.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }

    // Simple test application class
    public static class TestApp extends javafx.application.Application {
        @Override
        public void start(Stage stage) throws Exception {
            // Will be overridden by tests
        }
    }

    @Test
    public void testSuccessNotification() throws Exception {
        clickOn("Show Success");
        Thread.sleep(600); // Wait for animation
        
        captureCurrentState("notification-success");
        Thread.sleep(3500); // Wait for auto-close
    }

    @Test
    public void testErrorNotification() throws Exception {
        clickOn("Show Error");
        Thread.sleep(600);
        
        captureCurrentState("notification-error");
        Thread.sleep(3500);
    }

    @Test
    public void testWarningNotification() throws Exception {
        clickOn("Show Warning");
        Thread.sleep(600);
        
        captureCurrentState("notification-warning");
        Thread.sleep(3500);
    }

    @Test
    public void testInfoNotification() throws Exception {
        clickOn("Show Info");
        Thread.sleep(600);
        
        captureCurrentState("notification-info");
        Thread.sleep(3500);
    }

    private Button createButton(String text, String colorHex) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-padding: 10 20; " +
            "-fx-background-radius: 6; -fx-cursor: hand;",
            colorHex
        ));
        return button;
    }

    private void captureCurrentState(String filename) {
        try {
            Scene scene = primaryStage.getScene();
            if (scene != null && scene.getRoot() != null) {
                VisualTestUtils.captureNode(scene.getRoot(), filename);
            }
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }
}
