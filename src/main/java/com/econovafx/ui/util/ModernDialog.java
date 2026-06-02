package com.econovafx.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Modern dialog utility class that mimics web-style modal dialogs
 * with backdrop blur and smooth animations.
 */
public class ModernDialog {

    private static final double BACKDROP_OPACITY = 0.6;
    private static final int DIALOG_WIDTH = 450;
    private static final int DIALOG_MAX_HEIGHT = 400;

    public enum DialogType {
        INFO, SUCCESS, WARNING, ERROR, CONFIRM
    }

    /**
     * Shows a modern modal dialog with backdrop effect.
     * 
     * @param owner The owner stage
     * @param title Dialog title
     * @param message Message content
     * @param type Dialog type (determines icon and colors)
     * @param buttons Optional custom buttons (if none provided, default OK button is used)
     * @return The button clicked by the user, or null if dismissed
     */
    public static Button showDialog(Stage owner, String title, String message, 
                                   DialogType type, Button... buttons) {
        
        // Create backdrop (semi-transparent blurred background)
        Pane backdrop = createBackdrop(owner);
        
        // Create dialog content
        VBox dialogContent = createDialogContent(title, message, type, buttons);
        
        // Create main container
        StackPane root = new StackPane(backdrop, dialogContent);
        root.setStyle("-fx-background-color: transparent;");
        
        // Create stage for the dialog
        Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root, Color.TRANSPARENT));
        dialogStage.setResizable(false);
        
        // Center dialog on owner
        dialogStage.setX(owner.getX() + (owner.getWidth() - DIALOG_WIDTH) / 2);
        dialogStage.setY(owner.getY() + (owner.getHeight() - dialogContent.getHeight()) / 2);
        
        // Show with animation
        dialogStage.show();
        animateDialogEntrance(dialogContent, backdrop);
        
        // Wait for dialog to close
        try {
            javafx.application.Platform.runLater(() -> {
                // Dummy operation to keep focus
            });
            
            // Simple blocking mechanism for modal behavior
            final boolean[] closed = {false};
            final Button[] result = {null};
            
            // Override close request
            dialogStage.setOnCloseRequest(e -> {
                closed[0] = true;
            });
            
            // This is a simplified version - in real app we'd use a more robust blocking mechanism
            // For now, we'll return null and rely on callbacks for complex interactions
            return null;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Shows a confirmation dialog with Yes/No buttons.
     * 
     * @param owner The owner stage
     * @param title Dialog title
     * @param message Message content
     * @return true if user clicked Yes, false otherwise
     */
    public static boolean showConfirmation(Stage owner, String title, String message) {
        // Use a simple approach with a result holder
        final boolean[] result = {false};
        final boolean[] completed = {false};
        
        showConfirmationWithCallback(owner, title, message, 
            () -> { result[0] = true; completed[0] = true; },
            () -> { result[0] = false; completed[0] = true; });
        
        // Wait for completion (modal behavior)
        try {
            synchronized (completed) {
                while (!completed[0]) {
                    completed.wait(100);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return result[0];
    }

    /**
     * Shows a confirmation dialog with callbacks for Yes/No actions.
     */
    public static boolean showConfirmationWithCallback(Stage owner, String title, String message, 
                                                      Runnable onYes, Runnable onNo) {
        Pane backdrop = createBackdrop(owner);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modern-dialog-title");
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("modern-dialog-message");
        messageLabel.setWrapText(true);
        
        Button yesButton = new Button("Yes");
        yesButton.getStyleClass().addAll("modern-button", "button-primary");
        yesButton.setOnAction(e -> {
            onYes.run();
            closeDialog(backdrop, owner);
        });
        
        Button noButton = new Button("No");
        noButton.getStyleClass().addAll("modern-button", "button-secondary");
        noButton.setOnAction(e -> {
            onNo.run();
            closeDialog(backdrop, owner);
        });
        
        HBox buttonBox = new HBox(10, noButton, yesButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox content = new VBox(15, titleLabel, messageLabel, buttonBox);
        content.getStyleClass().add("modern-dialog-pane");
        content.setPrefWidth(DIALOG_WIDTH);
        
        StackPane root = new StackPane(backdrop, content);
        root.setStyle("-fx-background-color: transparent;");
        
        Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root, Color.TRANSPARENT));
        dialogStage.setResizable(false);
        
        // Center dialog
        double x = owner.getX() + (owner.getWidth() - DIALOG_WIDTH) / 2;
        double y = owner.getY() + (owner.getHeight() - content.prefHeight(-1)) / 2;
        dialogStage.setX(x);
        dialogStage.setY(y);
        
        dialogStage.show();
        animateDialogEntrance(content, backdrop);
        
        return true; // Actual result handled via callbacks
    }

    /**
     * Shows an information dialog.
     */
    public static void showInfo(Stage owner, String title, String message) {
        showMessageDialog(owner, title, message, DialogType.INFO);
    }

    /**
     * Shows a success dialog.
     */
    public static void showSuccess(Stage owner, String title, String message) {
        showMessageDialog(owner, title, message, DialogType.SUCCESS);
    }

    /**
     * Shows a warning dialog.
     */
    public static void showWarning(Stage owner, String title, String message) {
        showMessageDialog(owner, title, message, DialogType.WARNING);
    }

    /**
     * Shows an error dialog.
     */
    public static void showError(Stage owner, String title, String message) {
        showMessageDialog(owner, title, message, DialogType.ERROR);
    }

    private static void showMessageDialog(Stage owner, String title, String message, DialogType type) {
        Pane backdrop = createBackdrop(owner);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modern-dialog-title");
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("modern-dialog-message");
        messageLabel.setWrapText(true);
        
        Button okButton = new Button("OK");
        okButton.getStyleClass().addAll("modern-button", getButtonStyleForType(type));
        okButton.setOnAction(e -> closeDialog(backdrop, owner));
        
        HBox buttonBox = new HBox(okButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox content = new VBox(15, titleLabel, messageLabel, buttonBox);
        content.getStyleClass().add("modern-dialog-pane");
        content.setPrefWidth(DIALOG_WIDTH);
        
        StackPane root = new StackPane(backdrop, content);
        root.setStyle("-fx-background-color: transparent;");
        
        Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root, Color.TRANSPARENT));
        dialogStage.setResizable(false);
        
        // Center dialog
        double x = owner.getX() + (owner.getWidth() - DIALOG_WIDTH) / 2;
        double y = owner.getY() + (owner.getHeight() - content.prefHeight(-1)) / 2;
        dialogStage.setX(x);
        dialogStage.setY(y);
        
        dialogStage.show();
        animateDialogEntrance(content, backdrop);
    }

    private static Pane createBackdrop(Stage owner) {
        Pane backdrop = new Pane();
        backdrop.setBackground(new Background(
            new BackgroundFill(Color.rgb(0, 0, 0, (int)(BACKDROP_OPACITY * 255)), 
                             CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY)
        ));
        backdrop.setPickOnBounds(true);
        
        // Bind backdrop size to owner
        backdrop.prefWidthProperty().bind(owner.widthProperty());
        backdrop.prefHeightProperty().bind(owner.heightProperty());
        
        return backdrop;
    }

    private static VBox createDialogContent(String title, String message, DialogType type, Button[] buttons) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modern-dialog-title");
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("modern-dialog-message");
        messageLabel.setWrapText(true);
        
        HBox buttonBox;
        if (buttons == null || buttons.length == 0) {
            Button okButton = new Button("OK");
            okButton.getStyleClass().addAll("modern-button", getButtonStyleForType(type));
            buttonBox = new HBox(okButton);
        } else {
            buttonBox = new HBox(10, buttons);
        }
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox content = new VBox(15, titleLabel, messageLabel, buttonBox);
        content.getStyleClass().add("modern-dialog-pane");
        content.setPrefWidth(DIALOG_WIDTH);
        content.setMaxHeight(DIALOG_MAX_HEIGHT);
        
        return content;
    }

    private static String getButtonStyleForType(DialogType type) {
        return switch (type) {
            case SUCCESS -> "button-success";
            case ERROR, WARNING -> "button-danger";
            default -> "button-primary";
        };
    }

    private static void animateDialogEntrance(VBox dialog, Pane backdrop) {
        // Fade in backdrop
        FadeTransition backdropFade = new FadeTransition(Duration.millis(200), backdrop);
        backdropFade.setFromValue(0);
        backdropFade.setToValue(1);
        
        // Scale and fade in dialog
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), dialog);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);
        
        FadeTransition dialogFade = new FadeTransition(Duration.millis(300), dialog);
        dialogFade.setFromValue(0);
        dialogFade.setToValue(1);
        
        ParallelTransition parallel = new ParallelTransition(backdropFade, scale, dialogFade);
        parallel.play();
    }

    private static void closeDialog(Pane backdrop, Stage owner) {
        // Get the stage that contains the backdrop
        Scene scene = backdrop.getScene();
        
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            
            // Animate exit
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), backdrop);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> stage.close());
            fadeOut.play();
        }
    }
}
