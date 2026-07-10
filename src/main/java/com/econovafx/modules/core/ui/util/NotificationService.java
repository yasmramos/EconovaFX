package com.econovafx.modules.core.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Service for displaying toast notifications to the user.
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
        showNotification(stage, message, Color.web("#28a745"), "✓");
    }

    /**
     * Shows an error notification.
     * @param stage The owner stage.
     * @param message The message to display.
     */
    public static void showError(Stage stage, String message) {
        showNotification(stage, message, Color.web("#dc3545"), "✕");
    }

    /**
     * Shows an info notification.
     * @param stage The owner stage.
     * @param message The message to display.
     */
    public static void showInfo(Stage stage, String message) {
        showNotification(stage, message, Color.web("#17a2b8"), "ℹ");
    }

    /**
     * Shows a warning notification.
     * @param stage The owner stage.
     * @param message The message to display.
     */
    public static void showWarning(Stage stage, String message) {
        showNotification(stage, message, Color.web("#ffc107"), "⚠");
    }

    private static void showNotification(Stage stage, String message, Color color, String icon) {
        if (stage == null || stage.getScene() == null) return;

        // Create content
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: normal;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(WIDTH - 60);

        VBox content = new VBox(5, iconLabel, messageLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10, 15, 10, 15));
        content.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-background-radius: 8px;");
        content.setPrefSize(WIDTH, HEIGHT);

        // Overlay background for the specific notification
        VBox notificationBox = new VBox(content);
        notificationBox.setStyle("-fx-background-color: " + toHex(color) + "; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);");
        notificationBox.setPickOnBounds(false);

        // Find root pane
        Pane root = getRootPane(stage);
        if (root == null) return;

        // Set initial position (top-right, hidden above)
        notificationBox.setLayoutX(root.getWidth() - WIDTH - 20);
        notificationBox.setLayoutY(-HEIGHT);

        root.getChildren().add(notificationBox);

        // Animate
        TranslateTransition slideIn = new TranslateTransition(SLIDE_DURATION, notificationBox);
        slideIn.setToY(20);
        
        slideIn.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(DISPLAY_DURATION);
            pause.setOnFinished(p -> {
                TranslateTransition slideOut = new TranslateTransition(SLIDE_DURATION, notificationBox);
                slideOut.setToY(-HEIGHT);
                
                FadeTransition fadeOut = new FadeTransition(FADE_DURATION, notificationBox);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                
                slideOut.play();
                fadeOut.play();
                
                slideOut.setOnFinished(f -> root.getChildren().remove(notificationBox));
            });
            pause.play();
        });
        
        slideIn.play();
    }

    private static Pane getRootPane(Stage stage) {
        if (stage.getScene() == null) return null;
        
        javafx.scene.Node root = stage.getScene().getRoot();
        if (root instanceof Pane) {
            return (Pane) root;
        }
        // Fallback: return null if root is not a Pane
        return null;
    }

    private static String toHex(Color color) {
        return String.format("#%02X%02X%02X", 
            (int)(color.getRed() * 255), 
            (int)(color.getGreen() * 255), 
            (int)(color.getBlue() * 255));
    }
}
