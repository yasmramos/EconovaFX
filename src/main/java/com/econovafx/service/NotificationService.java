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
        showSuccess(message, null);
    }
    
    public void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            com.econovafx.core.service.NotificationService core = new com.econovafx.core.service.NotificationService(notificationContainer);
            core.showSuccess(message != null ? message : title);
        });
    }
    
    public void showError(String message) {
        showError(message, null);
    }
    
    public void showError(String title, String message) {
        Platform.runLater(() -> {
            com.econovafx.core.service.NotificationService core = new com.econovafx.core.service.NotificationService(notificationContainer);
            core.showError(message != null ? message : title);
        });
    }
    
    public void showWarning(String message) {
        showWarning(message, null);
    }
    
    public void showWarning(String title, String message) {
        Platform.runLater(() -> {
            com.econovafx.core.service.NotificationService core = new com.econovafx.core.service.NotificationService(notificationContainer);
            core.showWarning(message != null ? message : title);
        });
    }
    
    public void showInfo(String message) {
        showInfo(message, null);
    }
    
    public void showInfo(String title, String message) {
        Platform.runLater(() -> {
            com.econovafx.core.service.NotificationService core = new com.econovafx.core.service.NotificationService(notificationContainer);
            core.showInfo(message != null ? message : title);
        });
    }
}
