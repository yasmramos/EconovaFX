package com.econovafx.modules.core.service;

import io.avaje.inject.Singleton;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
        showNotification(container, message, Color.BLUE, "INFO");
    }

    public void showSuccess(VBox container, String message) {
        showNotification(container, message, Color.GREEN, "SUCCESS");
    }

    public void showWarning(VBox container, String message) {
        showNotification(container, message, Color.ORANGE, "WARNING");
    }

    public void showError(VBox container, String message) {
        showNotification(container, message, Color.RED, "ERROR");
    }

    private void showNotification(VBox container, String message, Color color, String type) {
        Platform.runLater(() -> {
            if (container == null) {
                return; // Skip if no container provided
            }
            
            Label notification = new Label(type + ": " + message);
            notification.setTextFill(Color.WHITE);
            notification.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
            notification.setMaxWidth(Double.MAX_VALUE);
            notification.setStyle("-fx-background-radius: 5px; -fx-font-weight: bold;");
            
            BackgroundFill bgFill = new BackgroundFill(color, CornerRadii.EMPTY, null);
            notification.setBackground(new Background(bgFill));

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
