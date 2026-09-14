package com.econovafx.modules.core.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Modern Modal Dialog with overlay scrim effect, similar to web applications.
 * Uses an in-scene overlay (StackPane) added to the owner's root instead of creating a separate Stage.
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

    private static final Color SCRIM_COLOR = new Color(0.051, 0.067, 0.09, 0.58); // rgba(13,17,23,0.58)
    private static final Duration ANIMATION_DURATION = Duration.millis(250);

    /**
     * Handle for controlling a dialog programmatically.
     * Provides access to close the dialog and clean up resources properly.
     */
    public static class DialogHandle {
        private final StackPane overlay;
        private final Node ownerRoot;
        private final Node modalCard;
        private final ObjectProperty<Void> closeProperty;
        private final Object nestedLoopKey;
        private boolean isDismissing = false;

        private DialogHandle(StackPane overlay, Node ownerRoot, Node modalCard, 
                            ObjectProperty<Void> closeProperty) {
            this(overlay, ownerRoot, modalCard, closeProperty, null);
        }

        private DialogHandle(StackPane overlay, Node ownerRoot, Node modalCard, 
                            ObjectProperty<Void> closeProperty, Object nestedLoopKey) {
            this.overlay = overlay;
            this.ownerRoot = ownerRoot;
            this.modalCard = modalCard;
            this.closeProperty = closeProperty;
            this.nestedLoopKey = nestedLoopKey;
        }

        /**
         * Gets the overlay pane.
         * @return The overlay pane
         */
        public StackPane getOverlay() {
            return overlay;
        }

        /**
         * Gets the owner's root node (for reference).
         * @return The owner's root node
         */
        public Node getOwnerRoot() {
            return ownerRoot;
        }

        /**
         * Closes this dialog gracefully with exit animation.
         * This method ensures proper cleanup: removes overlay from root,
         * completes the close property, and exits the nested event loop if applicable.
         */
        public void close() {
            dismiss(overlay, ownerRoot, modalCard, closeProperty, nestedLoopKey);
        }

        /**
         * Gets the close property for observing dialog closure.
         * @return The close property
         */
        public ObjectProperty<Void> closeProperty() {
            return closeProperty;
        }
    }

    /**
     * Shows a node as a modern modal dialog with overlay scrim effect.
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
     * Shows a node as a modern modal dialog with overlay scrim effect.
     * Non-blocking method - returns immediately.
     * 
     * @param ownerStage The owner stage (main window)
     * @param content The content node to display (e.g., a form loaded from FXML)
     * @param title The dialog title (displayed as a header label inside the dialog)
     * @param nestedLoopKey Optional key for nested event loop (used by showAndWait)
     * @return DialogHandle for controlling the dialog programmatically
     */
    private static DialogHandle showModal(Stage ownerStage, Node content, String title, Object nestedLoopKey) {
        Scene scene = ownerStage.getScene();
        if (scene == null) {
            throw new IllegalStateException("Owner stage must have a scene");
        }
        
        Node ownerRoot = scene.getRoot();
        StackPane rootStackPane;
        
        // Check if root is already a StackPane (like rootStackPane in main-view.fxml)
        if (ownerRoot instanceof StackPane) {
            rootStackPane = (StackPane) ownerRoot;
        } else {
            // If root is not a StackPane, we need to find or create one
            // Try to find rootStackPane by looking for a StackPane child
            rootStackPane = findRootStackPane(ownerRoot);
            if (rootStackPane == null) {
                // Wrap the root in a temporary StackPane
                rootStackPane = new StackPane(ownerRoot);
                scene.setRoot(rootStackPane);
            }
        }

        // Create overlay (full coverage)
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("modern-overlay");
        overlay.setBackground(new Background(new BackgroundFill(
            SCRIM_COLOR, CornerRadii.EMPTY, null)));
        overlay.setPickOnBounds(true); // Capture clicks to prevent interaction with main window
        overlay.setVisible(false); // Start invisible for animation

        // Create modal card (content container)
        VBox modalCard = createModalCard(content, title);
        modalCard.getStyleClass().add("modern-modal-card");
        
        // Add nodes to overlay (card centered automatically by StackPane)
        overlay.getChildren().add(modalCard);
        StackPane.setAlignment(modalCard, javafx.geometry.Pos.CENTER);

        // Add overlay to root stack pane
        rootStackPane.getChildren().add(overlay);
        
        // Bind overlay size to root stack pane
        overlay.prefWidthProperty().bind(rootStackPane.widthProperty());
        overlay.prefHeightProperty().bind(rootStackPane.heightProperty());
        overlay.maxWidthProperty().bind(rootStackPane.widthProperty());
        overlay.maxHeightProperty().bind(rootStackPane.heightProperty());

        // Create close property
        ObjectProperty<Void> closeProperty = new SimpleObjectProperty<>();

        // Click-outside-to-close: click on scrim (outside card) closes dialog
        overlay.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Check if click was directly on overlay (not on card)
                Node target = event.getPickResult().getIntersectedNode();
                if (target == overlay || !modalCard.getBoundsInParent().contains(event.getX(), event.getY())) {
                    dismiss(overlay, ownerRoot, modalCard, closeProperty, nestedLoopKey);
                }
            }
        });

        // ESC key to close
        scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && overlay.isVisible()) {
                event.consume();
                dismiss(overlay, ownerRoot, modalCard, closeProperty, nestedLoopKey);
            }
        });

        // Store cleanup handler in overlay's user data
        overlay.setUserData(new CleanupHandler(ownerRoot, closeProperty, nestedLoopKey));

        // Show the overlay
        overlay.setVisible(true);

        // Play entrance animations
        playEntranceAnimation(overlay, modalCard);

        // Create and return handle
        return new DialogHandle(overlay, ownerRoot, modalCard, closeProperty, nestedLoopKey);
    }
    
    /**
     * Finds the root StackPane in the scene graph.
     * @param root The root node to search from
     * @return The root StackPane, or null if not found
     */
    private static StackPane findRootStackPane(Node root) {
        if (root instanceof StackPane) {
            return (StackPane) root;
        }
        if (root instanceof Parent) {
            for (Node child : ((Parent) root).getChildrenUnmodifiable()) {
                StackPane result = findRootStackPane(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * Creates the modal card containing title and content.
     * @param content The content node
     * @param title The optional title
     * @return The modal card VBox
     */
    private static VBox createModalCard(Node content, String title) {
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

        // Create content container pane
        Pane contentContainer = new Pane(content);
        contentContainer.setStyle("-fx-background-color: transparent;");
        
        // Add title label if title is provided
        Label titleLabel = null;
        if (title != null && !title.trim().isEmpty()) {
            titleLabel = new Label(title);
            titleLabel.getStyleClass().add("modern-modal-title");
            titleLabel.setMaxWidth(Double.MAX_VALUE);
        }

        // Create VBox to hold title and content
        VBox modalCard;
        if (titleLabel != null) {
            modalCard = new VBox(5, titleLabel, contentContainer);
        } else {
            modalCard = new VBox(contentContainer);
        }
        modalCard.setStyle("-fx-background-color: transparent;");
        
        // Prevent card clicks from propagating to overlay
        modalCard.setOnMousePressed(event -> event.consume());
        
        return modalCard;
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
     * Cleanup handler stored in overlay's user data for proper cleanup.
     */
    private static class CleanupHandler {
        private final Node ownerRoot;
        private final ObjectProperty<Void> closeProperty;
        private final Object nestedLoopKey;
        
        CleanupHandler(Node ownerRoot, ObjectProperty<Void> closeProperty, Object nestedLoopKey) {
            this.ownerRoot = ownerRoot;
            this.closeProperty = closeProperty;
            this.nestedLoopKey = nestedLoopKey;
        }
    }

    /**
     * Unified dismiss method that handles all cleanup.
     * This is the single point of truth for closing dialogs.
     * 
     * @param overlay The overlay to remove
     * @param ownerRoot The owner's root node (reference only, no blur to remove)
     * @param modalCard The modal card for animation
     * @param closeProperty The close property to complete
     */
    private static void dismiss(StackPane overlay, Node ownerRoot, Node modalCard, 
                               ObjectProperty<Void> closeProperty) {
        dismiss(overlay, ownerRoot, modalCard, closeProperty, null);
    }
    
    /**
     * Unified dismiss method that handles all cleanup.
     * This is the single point of truth for closing dialogs.
     * 
     * @param overlay The overlay to remove
     * @param ownerRoot The owner's root node (reference only, no blur to remove)
     * @param modalCard The modal card for animation
     * @param closeProperty The close property to complete
     * @param nestedLoopKey Optional key for exiting nested event loop
     */
    private static void dismiss(StackPane overlay, Node ownerRoot, Node modalCard, 
                               ObjectProperty<Void> closeProperty, Object nestedLoopKey) {
        if (overlay == null || overlay.getParent() == null || !overlay.isVisible()) {
            return;
        }

        // Mark as dismissing to prevent double-cleanup
        overlay.setVisible(false);

        // Fade out overlay
        FadeTransition fadeOverlay = new FadeTransition(ANIMATION_DURATION, overlay);
        fadeOverlay.setFromValue(1.0);
        fadeOverlay.setToValue(0.0);

        // Scale and fade out modal card
        ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, modalCard);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.95);
        scale.setToY(0.95);

        FadeTransition fadeContent = new FadeTransition(ANIMATION_DURATION, modalCard);
        fadeContent.setFromValue(1.0);
        fadeContent.setToValue(0.0);

        ParallelTransition parallel = new ParallelTransition(fadeOverlay, scale, fadeContent);
        parallel.setOnFinished(event -> {
            // Remove overlay from parent
            if (overlay.getParent() instanceof StackPane) {
                ((StackPane) overlay.getParent()).getChildren().remove(overlay);
            }
            
            // Complete the close property
            if (closeProperty != null) {
                closeProperty.setValue(null);
            }
            
            // Exit nested event loop if showAndWait is waiting
            if (nestedLoopKey != null) {
                try {
                    Platform.exitNestedEventLoop(nestedLoopKey, null);
                } catch (IllegalArgumentException e) {
                    // Event loop already exited or key invalid - ignore in test environments
                    if (!"test".equals(System.getProperty("env"))) {
                        System.err.println("Warning: Could not exit nested event loop: " + e.getMessage());
                    }
                }
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
        // Legacy method - no longer supported with overlay-based approach
        // Users should use DialogHandle.close() instead
        if (dialogStage != null && dialogStage.isShowing()) {
            dialogStage.close();
        }
    }

    private static void playEntranceAnimation(StackPane overlay, Node modalCard) {
        // Fade in overlay
        FadeTransition fadeOverlay = new FadeTransition(ANIMATION_DURATION, overlay);
        fadeOverlay.setFromValue(0.0);
        fadeOverlay.setToValue(1.0);

        // Scale and fade in modal card with smooth bounce effect
        ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION.multiply(1.2), modalCard);
        scale.setFromX(0.85);
        scale.setFromY(0.85);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition fadeContent = new FadeTransition(ANIMATION_DURATION, modalCard);
        fadeContent.setFromValue(0.0);
        fadeContent.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(fadeOverlay, scale, fadeContent);
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
        
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(messageLabel);
        content.setStyle("-fx-background-color: white; -fx-padding: 20px;");
        
        showAndWait(ownerStage, content, title);
    }
}
