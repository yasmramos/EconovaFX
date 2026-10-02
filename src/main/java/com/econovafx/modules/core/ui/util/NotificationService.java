package com.econovafx.modules.core.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Service for displaying toast notifications to the user.
 * Notifications are displayed in a container anchored to the top-right corner,
 * stacked vertically without overlapping, and do not push or resize content.
 */
public class NotificationService {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 70;
    private static final Duration SLIDE_DURATION = Duration.millis(400);
    private static final Duration DISPLAY_DURATION = Duration.seconds(3);
    private static final Duration FADE_DURATION = Duration.millis(300);

    /**
     * Shows a success notification.
     * @param stage The owner stage.
     * @param message The message to display.
     */
    public static void showSuccess(Stage stage, String message) {
        showNotification(stage, message, "toast-success", "✓");
    }

    /**
     * Shows an error notification.
     * @param stage The owner stage.
     * @param message The message to display.
     */
    public static void showError(Stage stage, String message) {
        showNotification(stage, message, "toast-error", "✕");
    }

    /**
     * Shows an info notification.
     * @param stage The owner stage.
     * @param message The message to display.
     */
    public static void showInfo(Stage stage, String message) {
        showNotification(stage, message, "toast-info", "ℹ");
    }

    /**
     * Shows a warning notification.
     * @param stage The owner stage.
     * @param message The message to display.
     */
    public static void showWarning(Stage stage, String message) {
        showNotification(stage, message, "toast-warning", "⚠");
    }

    /**
     * Builds one toast. Its whole appearance comes from the {@code toast} and
     * {@code toast-<variant>} style classes, so there is no per-type colour code
     * here and the toast matches the app palette (see utilities.css).
     *
     * @param stage   the owner stage, used to find the toast container
     * @param message the message to display
     * @param variant style class selecting the accent colour
     * @param icon    leading glyph
     */
    private static void showNotification(Stage stage, String message, String variant, String icon) {
        if (stage == null || stage.getScene() == null) return;

        // Create content
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("toast-icon");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("toast-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(WIDTH - 60);

        VBox content = new VBox(5, iconLabel, messageLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10, 15, 10, 15));
        content.getStyleClass().add("toast-body");
        content.setPrefSize(WIDTH, HEIGHT);

        // Overlay background for the specific notification
        VBox notificationBox = new VBox(content);
        notificationBox.getStyleClass().addAll("toast", variant);
        notificationBox.setPickOnBounds(false);
        notificationBox.setMaxWidth(Region.USE_PREF_SIZE);

        // Find toast container
        VBox toastContainer = getToastContainer(stage);
        if (toastContainer == null) return;

        // Add to toast container (will be stacked vertically by VBox)
        toastContainer.getChildren().add(notificationBox);

        // Animate: slide in from right
        TranslateTransition slideIn = new TranslateTransition(SLIDE_DURATION, notificationBox);
        slideIn.setFromX(WIDTH + 20);
        slideIn.setToX(0);
        
        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, notificationBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        slideIn.play();
        fadeIn.play();
        
        slideIn.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(DISPLAY_DURATION);
            pause.setOnFinished(p -> {
                // Slide out to right
                TranslateTransition slideOut = new TranslateTransition(SLIDE_DURATION, notificationBox);
                slideOut.setFromX(0);
                slideOut.setToX(WIDTH + 20);
                
                FadeTransition fadeOut = new FadeTransition(FADE_DURATION, notificationBox);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                
                slideOut.play();
                fadeOut.play();
                
                slideOut.setOnFinished(f -> {
                    if (toastContainer.getChildren().contains(notificationBox)) {
                        toastContainer.getChildren().remove(notificationBox);
                    }
                });
            });
            pause.play();
        });
    }

    /**
     * Finds or creates the toast container in the scene.
     * The container is expected to be a VBox with fx:id="toastContainer" 
     * as a child of the root StackPane, aligned to TOP_RIGHT.
     * @param stage The owner stage.
     * @return The toast container VBox, or null if not found.
     */
    private static VBox getToastContainer(Stage stage) {
        if (stage.getScene() == null) return null;
        
        javafx.scene.Node root = stage.getScene().getRoot();
        
        // Look for toastContainer as a direct child of root (StackPane)
        if (root instanceof javafx.scene.Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                if (child instanceof VBox vBox && "toastContainer".equals(vBox.getId())) {
                    return vBox;
                }
            }
        }
        
        // Fallback: return null if toast container not found
        return null;
    }
}
