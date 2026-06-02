package com.econovafx.ui.visual;

import com.econovafx.ui.util.ModernDialog;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import static com.econovafx.ui.visual.VisualTestUtils.captureNode;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Visual tests for ModernDialog component.
 * Captures screenshots and saves them to docs/images/
 */
public class ModernDialogVisualTest extends ApplicationTest {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        VBox mainLayout = new VBox(10);
        mainLayout.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5;");
        mainLayout.getChildren().add(new Label("Main Application Window"));
        
        Button openDialogBtn = new Button("Open Dialog");
        openDialogBtn.setOnAction(e -> {
            VBox dialogContent = createDialogContent();
            ModernDialog.showModal(primaryStage, dialogContent, "Test Dialog");
        });
        mainLayout.getChildren().add(openDialogBtn);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("ModernDialog Visual Test");
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(TestApp.class);
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
    public void testModernDialogAppearance() throws Exception {
        // Click button to open dialog
        clickOn("Open Dialog");
        
        // Wait for dialog to appear
        Thread.sleep(500);
        
        // Verify dialog is visible
        verifyThat(".dialog-content", isVisible());
        
        // Capture screenshot
        Scene scene = primaryStage.getScene();
        if (scene != null && scene.getRoot() != null) {
            VisualTestUtils.captureNode(scene.getRoot(), "modern-dialog-open");
        }
    }

    @Test
    public void testModernDialogClosed() throws Exception {
        // Open dialog
        clickOn("Open Dialog");
        Thread.sleep(300);
        
        // Close dialog by clicking close button
        clickOn("Close");
        Thread.sleep(500);
        
        // Capture screenshot of closed state
        Scene scene = primaryStage.getScene();
        if (scene != null && scene.getRoot() != null) {
            VisualTestUtils.captureNode(scene.getRoot(), "modern-dialog-closed");
        }
    }

    private VBox createDialogContent() {
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-background-color: white;");
        content.setPrefSize(400, 300);
        content.getStyleClass().add("dialog-content");
        
        Label title = new Label("Test Dialog Title");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label message = new Label("This is a modern modal dialog with backdrop blur effect.");
        message.setWrapText(true);
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 8 16;");
        closeButton.setOnAction(e -> {
            Stage dialogStage = (Stage) ((Button) e.getSource()).getScene().getWindow();
            ModernDialog.closeDialog(dialogStage, primaryStage.getScene().getRoot());
        });
        
        content.getChildren().addAll(title, message, closeButton);
        return content;
    }
}
