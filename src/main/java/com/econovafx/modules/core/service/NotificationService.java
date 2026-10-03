package com.econovafx.modules.core.service;

import jakarta.inject.Singleton;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Service for displaying temporary notifications to the user.
 * Supports INFO, SUCCESS, WARNING, and ERROR types.
 * The notification container must be provided when showing notifications.
 */
@Singleton
public class NotificationService {

    public NotificationService() {
        // No container needed - it will be provided when showing notifications
    }

    public void showInfo(VBox container, String message) {
        showNotification(container, message, "notification-info", "INFO");
    }

    public void showSuccess(VBox container, String message) {
        showNotification(container, message, "notification-success", "SUCCESS");
    }

    public void showWarning(VBox container, String message) {
        showNotification(container, message, "notification-warning", "WARNING");
    }

    public void showError(VBox container, String message) {
        showNotification(container, message, "notification-error", "ERROR");
    }

    /**
     * Shows a notification. Its appearance comes from the {@code notification}
     * and variant style classes (see utilities.css), so no colour is set in
     * code and the banner matches the rest of the app.
     *
     * @param container the VBox that hosts the notification
     * @param message   the message to display
     * @param variant   style class selecting the accent colour
     * @param type      short type label shown before the message
     */
    private void showNotification(VBox container, String message, String variant, String type) {
        Platform.runLater(() -> {
            if (container == null) {
                return; // Skip if no container provided
            }

            Label notification = new Label(type + ": " + message);
            notification.setMaxWidth(Double.MAX_VALUE);
            notification.getStyleClass().addAll("notification", variant);

            container.getChildren().add(notification);

            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), notification);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Auto remove after 5 seconds
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(5), e -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notification);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(ev -> container.getChildren().remove(notification));
                    fadeOut.play();
                })
            );
            timeline.play();
        });
    }
}
