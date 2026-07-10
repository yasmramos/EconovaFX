package com.econovafx.modules.core.service;

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
 */
public class NotificationService {

    private final VBox notificationContainer;

    public NotificationService(VBox container) {
        this.notificationContainer = container;
    }

    public void showInfo(String message) {
        showNotification(message, Color.BLUE, "INFO");
    }

    public void showSuccess(String message) {
        showNotification(message, Color.GREEN, "SUCCESS");
    }

    public void showWarning(String message) {
        showNotification(message, Color.ORANGE, "WARNING");
    }

    public void showError(String message) {
        showNotification(message, Color.RED, "ERROR");
    }

    private void showNotification(String message, Color color, String type) {
        Platform.runLater(() -> {
            Label notification = new Label(type + ": " + message);
            notification.setTextFill(Color.WHITE);
            notification.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
            notification.setMaxWidth(Double.MAX_VALUE);
            notification.setStyle("-fx-background-radius: 5px; -fx-font-weight: bold;");
            
            BackgroundFill bgFill = new BackgroundFill(color, CornerRadii.EMPTY, null);
            notification.setBackground(new Background(bgFill));

            notificationContainer.getChildren().add(notification);

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
                    fadeOut.setOnFinished(ev -> notificationContainer.getChildren().remove(notification));
                    fadeOut.play();
                })
            );
            timeline.play();
        });
    }
}
