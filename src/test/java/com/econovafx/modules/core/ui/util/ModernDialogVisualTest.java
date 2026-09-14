package com.econovafx.modules.core.ui.util;

import com.econovafx.modules.core.ui.util.ModernDialog;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import static com.econovafx.modules.core.ui.util.VisualTestUtils.captureNode;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Visual tests for ModernDialog component (overlay-based in-scene modal).
 * Captures screenshots and saves them to docs/images/
 */
public class ModernDialogVisualTest extends ApplicationTest {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        StackPane rootLayout = new StackPane();
        rootLayout.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5;");
        
        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().add(new Label("Main Application Window"));
        
        Button openDialogBtn = new Button("Open Dialog");
        openDialogBtn.setOnAction(e -> {
            VBox dialogContent = createDialogContent();
            ModernDialog.showModal(primaryStage, dialogContent, "Test Dialog");
        });
        mainLayout.getChildren().add(openDialogBtn);
        
        rootLayout.getChildren().add(mainLayout);
        Scene scene = new Scene(rootLayout, 800, 600);
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
        
        // Wait for dialog to appear and ensure we're on FX thread
        Thread.sleep(500);
        interact(() -> {
            // Verify overlay is visible (modern-overlay class)
            verifyThat(".modern-overlay", isVisible());
            
            // Capture screenshot
            Scene scene = primaryStage.getScene();
            if (scene != null && scene.getRoot() != null) {
                try {
                    VisualTestUtils.captureNode(scene.getRoot(), "modern-dialog-open");
                } catch (Exception e) {
                    System.err.println("Failed to capture dialog screenshot: " + e.getMessage());
                }
            }
        });
    }

    @Test
    public void testModernDialogClosed() throws Exception {
        // Open dialog
        clickOn("Open Dialog");
        Thread.sleep(500);
        
        // Close dialog by clicking outside (on scrim)
        interact(() -> {
            // Find the overlay and simulate click on scrim area
            Node overlay = lookup(".modern-overlay").query();
            if (overlay != null) {
                // Click on overlay (scrim) to close
                clickOn(overlay);
            }
        });
        Thread.sleep(500);
        
        // Capture screenshot of closed state
        interact(() -> {
            Scene scene = primaryStage.getScene();
            if (scene != null && scene.getRoot() != null) {
                try {
                    VisualTestUtils.captureNode(scene.getRoot(), "modern-dialog-closed");
                } catch (Exception e) {
                    System.err.println("Failed to capture closed dialog screenshot: " + e.getMessage());
                }
            }
        });
    }

    private VBox createDialogContent() {
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-background-color: white;");
        content.setPrefSize(400, 300);
        content.getStyleClass().add("dialog-content");
        
        Label title = new Label("Test Dialog Title");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label message = new Label("This is a modern modal dialog with overlay scrim effect. Click outside or press ESC to close.");
        message.setWrapText(true);
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 8 16;");
        closeButton.setOnAction(e -> {
            // Get the dialog handle from parent overlay's user data
            Node parent = closeButton.getParent();
            while (parent != null) {
                if (parent.getStyleClass().contains("modern-overlay")) {
                    Object userData = parent.getUserData();
                    if (userData instanceof ModernDialog.DialogHandle) {
                        // Find the DialogHandle by searching overlay's children
                        ModernDialog.DialogHandle handle = findDialogHandle(parent);
                        if (handle != null) {
                            handle.close();
                        }
                    }
                    break;
                }
                parent = parent.getParent();
            }
        });
        
        content.getChildren().addAll(title, message, closeButton);
        return content;
    }

    /**
     * Helper method to find the DialogHandle associated with an overlay.
     * The handle is stored in the overlay's properties map.
     */
    private ModernDialog.DialogHandle findDialogHandle(Node overlay) {
        // Try to get from properties (using a known key)
        Object prop = overlay.getProperties().get("dialogHandle");
        if (prop instanceof ModernDialog.DialogHandle) {
            return (ModernDialog.DialogHandle) prop;
        }
        
        // Fallback: search through children if needed
        if (overlay instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) overlay).getChildrenUnmodifiable()) {
                if (child.getStyleClass().contains("modern-modal-card")) {
                    // The handle should be accessible via the overlay itself
                    break;
                }
            }
        }
        return null;
    }
}
