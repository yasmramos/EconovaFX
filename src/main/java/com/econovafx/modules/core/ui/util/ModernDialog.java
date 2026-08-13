package com.econovafx.modules.core.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Modern Modal Dialog with backdrop blur effect, similar to web applications.
 * Supports showing any Node (forms, custom UI) in a centered, animated modal window.
 * 
 * Usage example:
 * <pre>
 * // Load your form from FXML
 * Node form = FXMLLoader.load(getClass.getResource("my-form.fxml"));
 * 
 * // Show as modal dialog
 * ModernDialog.showModal(ownerStage, form, "My Form Title");
 * 
 * // Or show and wait for closure
 * ModernDialog.showAndWait(ownerStage, form, "My Form Title");
 * </pre>
 */
public class ModernDialog {

    private static final Color BACKDROP_COLOR = new Color(0, 0, 0, 0.5);
    private static final Duration ANIMATION_DURATION = Duration.millis(250);

    /**
     * Shows a node as a modern modal dialog with backdrop blur effect.
     * Non-blocking method - returns immediately.
     * 
     * @param ownerStage The owner stage (main window)
     * @param content The content node to display (e.g., a form loaded from FXML)
     * @param title The dialog title (optional, can be null - displayed in window title bar if Stage allows)
     * @return ObjectProperty that completes when the dialog is closed
     */
    public static ObjectProperty<Void> showModal(Stage ownerStage, Node content, String title) {
        // Create the dialog stage
        Stage dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        // Create the root pane with transparency
        Pane rootPane = new Pane();
        rootPane.setStyle("-fx-background-color: transparent;");

        // Create backdrop (semi-transparent dark overlay)
        Region backdrop = new Region();
        backdrop.setBackground(new Background(new BackgroundFill(
            BACKDROP_COLOR, CornerRadii.EMPTY, null)));
        backdrop.setMouseTransparent(false); // Capture clicks to prevent interaction with main window

        // Apply blur effect to the owner stage's scene content
        BoxBlur blur = new BoxBlur(10, 10, 3);
        Node ownerContent = ownerStage.getScene().getRoot();
        ownerContent.setEffect(blur);

        // Create content container with white background and shadow
        Pane contentContainer = new Pane(content);
        contentContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0.5, 0, 10);"
        );
        
        // Set preferred size if not already set
        if (content instanceof Region) {
            Region region = (Region) content;
            if (region.getPrefWidth() == 0 || region.getPrefWidth() < 300) {
                region.setPrefWidth(550);
            }
            if (region.getPrefHeight() == 0 || region.getPrefHeight() < 200) {
                region.setPrefHeight(450);
            }
        }

        // Add nodes to root
        rootPane.getChildren().addAll(backdrop, contentContainer);

        // Sync root size with owner using listeners (avoid bind on ReadOnlyProperty)
        ownerStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            rootPane.setPrefWidth(newVal.doubleValue());
            backdrop.setPrefWidth(newVal.doubleValue());
            contentContainer.setLayoutX((newVal.doubleValue() - contentContainer.getPrefWidth()) / 2);
        });
        ownerStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            rootPane.setPrefHeight(newVal.doubleValue());
            backdrop.setPrefHeight(newVal.doubleValue());
            contentContainer.setLayoutY((newVal.doubleValue() - contentContainer.getPrefHeight()) / 2);
        });
        
        // Initialize sizes
        rootPane.setPrefWidth(ownerStage.getWidth());
        rootPane.setPrefHeight(ownerStage.getHeight());
        backdrop.setPrefWidth(ownerStage.getWidth());
        backdrop.setPrefHeight(ownerStage.getHeight());
        contentContainer.setLayoutX((ownerStage.getWidth() - contentContainer.getPrefWidth()) / 2);
        contentContainer.setLayoutY((ownerStage.getHeight() - contentContainer.getPrefHeight()) / 2);

        // Create scene
        Scene scene = new Scene(rootPane, ownerStage.getWidth(), ownerStage.getHeight(), Color.TRANSPARENT);
        dialogStage.setScene(scene);

        // Handle close requests
        ObjectProperty<Void> closeProperty = new SimpleObjectProperty<>();
        dialogStage.setOnCloseRequest(event -> {
            ownerContent.setEffect(null); // Remove blur
            closeProperty.setValue(null);
        });

        // Show the dialog
        dialogStage.show();

        // Play entrance animations
        playEntranceAnimation(backdrop, contentContainer);

        return closeProperty;
    }

    /**
     * Shows a node as a modern modal dialog and waits for it to close.
     * Blocking method - use this when you need to wait for user input.
     * 
     * @param ownerStage The owner stage
     * @param content The content node to display
     * @param title The dialog title
     */
    public static void showAndWait(Stage ownerStage, Node content, String title) {
        ObjectProperty<Void> property = showModal(ownerStage, content, title);
        // Wait for the property to be set (dialog closed)
        while (property.getValue() == null) {
            try {
                Thread.sleep(50);
                Platform.runLater(() -> {}); // Pump events to keep UI responsive
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Closes the dialog gracefully with exit animation.
     * Call this method from within the dialog content when you want to close it.
     * 
     * @param dialogStage The dialog stage to close
     * @param ownerContent The original owner's root node (to remove blur effect)
     */
    public static void closeDialog(Stage dialogStage, Node ownerContent) {
        if (dialogStage == null || !dialogStage.isShowing()) {
            return;
        }

        Node backdrop = null;
        Node content = null;

        if (dialogStage.getScene() != null && dialogStage.getScene().getRoot() instanceof Pane) {
            Pane root = (Pane) dialogStage.getScene().getRoot();
            if (root.getChildren().size() >= 2) {
                backdrop = root.getChildren().get(0);
                content = root.getChildren().get(1);
            }
        }

        if (backdrop != null && content != null) {
            // Fade out backdrop
            FadeTransition fadeBackdrop = new FadeTransition(ANIMATION_DURATION, backdrop);
            fadeBackdrop.setFromValue(1.0);
            fadeBackdrop.setToValue(0.0);

            // Scale and fade out content
            ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, content);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(0.95);
            scale.setToY(0.95);

            FadeTransition fadeContent = new FadeTransition(ANIMATION_DURATION, content);
            fadeContent.setFromValue(1.0);
            fadeContent.setToValue(0.0);

            ParallelTransition parallel = new ParallelTransition(fadeBackdrop, scale, fadeContent);
            parallel.setOnFinished(event -> {
                if (ownerContent != null) {
                    ownerContent.setEffect(null); // Remove blur from owner
                }
                dialogStage.close();
            });
            parallel.play();
        } else {
            if (ownerContent != null) {
                ownerContent.setEffect(null);
            }
            dialogStage.close();
        }
    }

    private static void playEntranceAnimation(Node backdrop, Node content) {
        // Fade in backdrop
        FadeTransition fadeBackdrop = new FadeTransition(ANIMATION_DURATION, backdrop);
        fadeBackdrop.setFromValue(0.0);
        fadeBackdrop.setToValue(1.0);

        // Scale and fade in content with smooth bounce effect
        ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION.multiply(1.2), content);
        scale.setFromX(0.85);
        scale.setFromY(0.85);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition fadeContent = new FadeTransition(ANIMATION_DURATION, content);
        fadeContent.setFromValue(0.0);
        fadeContent.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(fadeBackdrop, scale, fadeContent);
        parallel.play();
    }
    
    /**
     * Shows a simple info dialog with a message.
     * @param ownerStage The owner stage
     * @param title The dialog title
     * @param message The message to display
     */
    public static void showInfoDialog(Stage ownerStage, String title, String message) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-padding: 20px;");
        
        VBox content = new VBox(messageLabel);
        content.setStyle("-fx-background-color: white; -fx-padding: 20px;");
        
        showAndWait(ownerStage, content, title);
    }
}
