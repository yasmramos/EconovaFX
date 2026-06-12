package com.econovafx.service;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Service for displaying modern toast notifications.
 */
public class NotificationService {

    private VBox notificationContainer;

    public NotificationService(VBox container) {
        this.notificationContainer = container;
    }

    public void showSuccess(String message) {
        showNotification(message, "success");
    }

    public void showError(String message) {
        showNotification(message, "error");
    }

    public void showWarning(String message) {
        showNotification(message, "warning");
    }

    public void showInfo(String message) {
        showNotification(message, "info");
    }

    private void showNotification(String message, String type) {
        if (notificationContainer == null) {
            System.out.println("Notification container not initialized: " + message);
            return;
        }

        HBox toast = createToast(message, type);
        
        // Add to container
        notificationContainer.getChildren().add(toast);
        
        // Animate in
        fadeIn(toast);
        
        // Auto remove after 4 seconds
        javafx.application.Platform.runLater(() -> {
            javafx.util.Duration delay = javafx.util.Duration.seconds(4);
            javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(delay, e -> {
                fadeOutAndRemove(toast);
            });
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(kf);
            timeline.play();
        });
    }

    private HBox createToast(String message, String type) {
        HBox toast = new HBox(10);
        toast.getStyleClass().addAll("toast", "toast-" + type);
        toast.setPrefHeight(50);
        toast.setMaxWidth(Double.MAX_VALUE);
        
        Label icon = new Label(getIconForType(type));
        icon.getStyleClass().add("toast-icon");
        
        Label text = new Label(message);
        text.setWrapText(true);
        text.getStyleClass().add("toast-message");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toast.getChildren().addAll(icon, text, spacer);
        
        return toast;
    }

    private String getIconForType(String type) {
        return switch (type) {
            case "success" -> "✓";
            case "error" -> "✕";
            case "warning" -> "⚠";
            default -> "ℹ";
        };
    }

    private void fadeIn(Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(1);
        ft.play();
    }

    private void fadeOutAndRemove(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            if (notificationContainer != null && notificationContainer.getChildren().contains(node)) {
                notificationContainer.getChildren().remove(node);
            }
        });
        ft.play();
    }
}
