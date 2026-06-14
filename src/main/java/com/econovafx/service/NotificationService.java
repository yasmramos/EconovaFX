package com.econovafx.service;

import io.avaje.inject.Component;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

/**
 * Service wrapper for notifications.
 * Delegates to core service for backward compatibility.
 */
@Component
public class NotificationService {
    
    private final VBox notificationContainer;
    
    public NotificationService() {
        this.notificationContainer = new VBox();
    }
    
    public VBox getNotificationContainer() {
        return notificationContainer;
    }
    
    public void showSuccess(String message) {
        Platform.runLater(() -> {
            com.econovafx.core.service.NotificationService core = new com.econovafx.core.service.NotificationService(notificationContainer);
            core.showSuccess(message);
        });
    }
    
    public void showError(String message) {
        Platform.runLater(() -> {
            com.econovafx.core.service.NotificationService core = new com.econovafx.core.service.NotificationService(notificationContainer);
            core.showError(message);
        });
    }
    
    public void showWarning(String message) {
        Platform.runLater(() -> {
            com.econovafx.core.service.NotificationService core = new com.econovafx.core.service.NotificationService(notificationContainer);
            core.showWarning(message);
        });
    }
    
    public void showInfo(String message) {
        Platform.runLater(() -> {
            com.econovafx.core.service.NotificationService core = new com.econovafx.core.service.NotificationService(notificationContainer);
            core.showInfo(message);
        });
    }
}
