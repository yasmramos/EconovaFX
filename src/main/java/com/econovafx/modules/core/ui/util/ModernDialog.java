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
import javafx.scene.layout.StackPane;
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
 * // Show as modal dialog and get handle for programmatic control
 * DialogHandle handle = ModernDialog.showModal(ownerStage, form, "My Form Title");
 * 
 * // Or show and wait for closure (blocking with proper nested event loop)
 * ModernDialog.showAndWait(ownerStage, form, "My Form Title");
 * </pre>
 */
public class ModernDialog {

    private static final Color BACKDROP_COLOR = new Color(0, 0, 0, 0.5);
    private static final Duration ANIMATION_DURATION = Duration.millis(250);

    /**
     * Handle for controlling a dialog programmatically.
     * Provides access to close the dialog and clean up resources properly.
     */
    public static class DialogHandle {
        private final Stage dialogStage;
        private final Node ownerContent;
        private final Region backdrop;
        private final Node contentContainer;
        private final ObjectProperty<Void> closeProperty;
        private final Object nestedLoopKey;
        private boolean isDismissing = false;

        private DialogHandle(Stage dialogStage, Node ownerContent, Region backdrop, 
                            Node contentContainer, ObjectProperty<Void> closeProperty) {
            this(dialogStage, ownerContent, backdrop, contentContainer, closeProperty, null);
        }

        private DialogHandle(Stage dialogStage, Node ownerContent, Region backdrop, 
                            Node contentContainer, ObjectProperty<Void> closeProperty, 
                            Object nestedLoopKey) {
            this.dialogStage = dialogStage;
            this.ownerContent = ownerContent;
            this.backdrop = backdrop;
            this.contentContainer = contentContainer;
            this.closeProperty = closeProperty;
            this.nestedLoopKey = nestedLoopKey;
        }

        /**
         * Gets the dialog stage.
         * @return The dialog stage
         */
        public Stage getDialogStage() {
            return dialogStage;
        }

        /**
         * Gets the owner's root node (for reference).
         * @return The owner's root node
         */
        public Node getOwnerContent() {
            return ownerContent;
        }

        /**
         * Closes this dialog gracefully with exit animation.
         * This method ensures proper cleanup: removes blur from owner,
         * completes the close property, and exits the nested event loop if applicable.
         */
        public void close() {
            dismiss(dialogStage, ownerContent, backdrop, contentContainer, closeProperty, nestedLoopKey);
        }

        /**
         * Checks if the dialog is currently being dismissed.
         * @return true if dismissal is in progress
         */
        boolean isDismissing() {
            return isDismissing;
        }
        
        /**
         * Sets the dismissing flag.
         * @param dismissing true if dismissal is in progress
         */
        void setDismissing(boolean dismissing) {
            isDismissing = dismissing;
        }
    }

    /**
     * Shows a node as a modern modal dialog with backdrop blur effect.
     * Non-blocking method - returns immediately.
     * 
     * @param ownerStage The owner stage (main window)
     * @param content The content node to display (e.g., a form loaded from FXML)
     * @param title The dialog title (displayed as a header label inside the dialog)
     * @return DialogHandle for controlling the dialog programmatically
     */
    public static DialogHandle showModal(Stage ownerStage, Node content, String title) {
        return showModal(ownerStage, content, title, null);
    }
    
    /**
     * Shows a node as a modern modal dialog with backdrop blur effect.
     * Non-blocking method - returns immediately.
     * 
     * @param ownerStage The owner stage (main window)
     * @param content The content node to display (e.g., a form loaded from FXML)
     * @param title The dialog title (displayed as a header label inside the dialog)
     * @param nestedLoopKey Optional key for nested event loop (used by showAndWait)
     * @return DialogHandle for controlling the dialog programmatically
     */
    private static DialogHandle showModal(Stage ownerStage, Node content, String title, Object nestedLoopKey) {
        // Create the dialog stage
        Stage dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        // Create the root pane with transparency (StackPane for proper centering)
        StackPane rootPane = new StackPane();
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
        // Use Pane to hold the content, but wrap in StackPane for centering
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

        // Add title label if title is provided
        Label titleLabel = null;
        if (title != null && !title.trim().isEmpty()) {
            titleLabel = new Label(title);
            titleLabel.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 15 20 10 20;" +
                "-fx-text-fill: #333333; -fx-alignment: center;"
            );
            titleLabel.setMaxWidth(Double.MAX_VALUE);
        }

        // Create a VBox to hold title and content if title exists
        Node displayContent;
        if (titleLabel != null) {
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(5, titleLabel, contentContainer);
            vbox.setStyle("-fx-background-color: transparent;");
            displayContent = vbox;
        } else {
            displayContent = contentContainer;
        }

        // Add nodes to root (backdrop first, then centered content)
        rootPane.getChildren().add(backdrop);
        rootPane.getChildren().add(displayContent);
        StackPane.setAlignment(displayContent, javafx.geometry.Pos.CENTER);

        // Sync root size with owner using listeners (avoid bind on ReadOnlyProperty)
        ownerStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            rootPane.setPrefWidth(width);
            backdrop.setPrefWidth(width);
            // No need to manually position contentContainer - StackPane handles centering
        });
        ownerStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            double height = newVal.doubleValue();
            rootPane.setPrefHeight(height);
            backdrop.setPrefHeight(height);
            // No need to manually position contentContainer - StackPane handles centering
        });
        
        // Initialize sizes
        double ownerWidth = ownerStage.getWidth();
        double ownerHeight = ownerStage.getHeight();
        
        // Handle case where owner is not yet dimensioned
        if (Double.isNaN(ownerWidth) || ownerWidth <= 0) {
            ownerWidth = 800; // Default fallback
        }
        if (Double.isNaN(ownerHeight) || ownerHeight <= 0) {
            ownerHeight = 600; // Default fallback
        }
        
        rootPane.setPrefWidth(ownerWidth);
        rootPane.setPrefHeight(ownerHeight);
        backdrop.setPrefWidth(ownerWidth);
        backdrop.setPrefHeight(ownerHeight);

        // Create scene
        Scene scene = new Scene(rootPane, ownerWidth, ownerHeight, Color.TRANSPARENT);
        dialogStage.setScene(scene);

        // Create close property
        ObjectProperty<Void> closeProperty = new SimpleObjectProperty<>();

        // Unified close handler - delegate to dismiss method
        dialogStage.setOnCloseRequest(event -> {
            dismiss(dialogStage, ownerContent, backdrop, contentContainer, closeProperty);
        });

        // Safety net: ensure blur is removed when stage is hidden by any means
        dialogStage.setOnHidden(event -> {
            ownerContent.setEffect(null);
        });

        // Show the dialog
        dialogStage.show();

        // Play entrance animations
        playEntranceAnimation(backdrop, displayContent);

        // Create and return handle
        return new DialogHandle(dialogStage, ownerContent, backdrop, contentContainer, closeProperty, nestedLoopKey);
    }

    /**
     * Shows a node as a modern modal dialog and waits for it to close.
     * Blocking method - uses nested event loop to keep UI responsive while waiting.
     * 
     * @param ownerStage The owner stage
     * @param content The content node to display
     * @param title The dialog title
     */
    public static void showAndWait(Stage ownerStage, Node content, String title) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("showAndWait must be called on the FX Application Thread");
        }
        
        Object nestedLoopKey = new Object();
        DialogHandle handle = showModal(ownerStage, content, title, nestedLoopKey);
        
        // Add listener BEFORE entering nested loop to avoid race conditions
        handle.closeProperty.addListener((obs, oldVal, newVal) -> {
            // Exit the nested loop when dialog is closed
            Platform.exitNestedEventLoop(nestedLoopKey, null);
        });
        
        // Enter nested event loop - blocks until exitNestedEventLoop is called
        Platform.enterNestedEventLoop(nestedLoopKey);
    }

    /**
     * Unified dismiss method that handles all cleanup.
     * This is the single point of truth for closing dialogs.
     * 
     * @param dialogStage The dialog stage to close
     * @param ownerContent The owner's root node (to remove blur)
     * @param backdrop The backdrop region for animation
     * @param contentContainer The content container for animation
     * @param closeProperty The close property to complete
     */
    private static void dismiss(Stage dialogStage, Node ownerContent, Region backdrop, 
                               Node contentContainer, ObjectProperty<Void> closeProperty) {
        dismiss(dialogStage, ownerContent, backdrop, contentContainer, closeProperty, null);
    }
    
    /**
     * Unified dismiss method that handles all cleanup.
     * This is the single point of truth for closing dialogs.
     * 
     * @param dialogStage The dialog stage to close
     * @param ownerContent The owner's root node (to remove blur)
     * @param backdrop The backdrop region for animation
     * @param contentContainer The content container for animation
     * @param closeProperty The close property to complete
     * @param nestedLoopKey Optional key for exiting nested event loop
     */
    private static void dismiss(Stage dialogStage, Node ownerContent, Region backdrop, 
                               Node contentContainer, ObjectProperty<Void> closeProperty, 
                               Object nestedLoopKey) {
        if (dialogStage == null || !dialogStage.isShowing()) {
            return;
        }

        // Mark as dismissing to prevent double-cleanup
        // Find the handle if possible to check this flag
        // For now, we proceed with cleanup
        
        // Fade out backdrop
        FadeTransition fadeBackdrop = new FadeTransition(ANIMATION_DURATION, backdrop);
        fadeBackdrop.setFromValue(1.0);
        fadeBackdrop.setToValue(0.0);

        // Scale and fade out content
        ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, contentContainer);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.95);
        scale.setToY(0.95);

        FadeTransition fadeContent = new FadeTransition(ANIMATION_DURATION, contentContainer);
        fadeContent.setFromValue(1.0);
        fadeContent.setToValue(0.0);

        ParallelTransition parallel = new ParallelTransition(fadeBackdrop, scale, fadeContent);
        parallel.setOnFinished(event -> {
            // Remove blur from owner
            if (ownerContent != null) {
                ownerContent.setEffect(null);
            }
            // Complete the close property
            if (closeProperty != null) {
                closeProperty.setValue(null);
            }
            // Close the dialog stage
            dialogStage.close();
            // Exit nested event loop if showAndWait is waiting
            if (nestedLoopKey != null) {
                Platform.exitNestedEventLoop(nestedLoopKey, null);
            } else {
                // Fallback: try using dialogStage as key (for backward compatibility with deprecated closeDialog)
                Platform.runLater(() -> Platform.exitNestedEventLoop(dialogStage, null));
            }
        });
        parallel.play();
    }

    /**
     * Closes the dialog gracefully with exit animation.
     * Call this method from within the dialog content when you want to close it.
     * 
     * @param dialogStage The dialog stage to close
     * @param ownerContent The original owner's root node (to remove blur effect)
     * @deprecated Use DialogHandle.close() instead. This method is kept for backward compatibility
     *             but may not properly exit nested event loops. Prefer obtaining a DialogHandle
     *             from showModal() or accessing the handle from within your controller.
     */
    @Deprecated
    public static void closeDialog(Stage dialogStage, Node ownerContent) {
        if (dialogStage == null || !dialogStage.isShowing()) {
            return;
        }

        Node backdrop = null;
        Node contentContainer = null;

        if (dialogStage.getScene() != null && dialogStage.getScene().getRoot() instanceof StackPane) {
            StackPane root = (StackPane) dialogStage.getScene().getRoot();
            if (root.getChildren().size() >= 2) {
                backdrop = root.getChildren().get(0);
                // Content might be wrapped in VBox if there's a title
                Node potentialContent = root.getChildren().get(1);
                if (potentialContent instanceof javafx.scene.layout.VBox) {
                    javafx.scene.layout.VBox vbox = (javafx.scene.layout.VBox) potentialContent;
                    // Find the Pane contentContainer within the VBox
                    for (Node child : vbox.getChildren()) {
                        if (child instanceof Pane) {
                            contentContainer = child;
                            break;
                        }
                    }
                } else {
                    contentContainer = potentialContent;
                }
            }
        }

        if (backdrop != null && contentContainer != null) {
            // Use the unified dismiss method
            dismiss(dialogStage, ownerContent, (Region) backdrop, contentContainer, null);
        } else {
            // Fallback: just close the dialog
            if (ownerContent != null) {
                ownerContent.setEffect(null);
            }
            dialogStage.close();
            Platform.runLater(() -> Platform.exitNestedEventLoop(dialogStage, null));
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
