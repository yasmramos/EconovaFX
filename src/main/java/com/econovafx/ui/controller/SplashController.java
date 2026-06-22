package com.econovafx.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class SplashController {

    @FXML
    private StackPane root;

    @FXML
    private ImageView logoView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label versionLabel;

    public void updateProgress(double progress, String message) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    public void setVersion(String version) {
        if (versionLabel != null) {
            versionLabel.setText("v" + version);
        }
    }
}
