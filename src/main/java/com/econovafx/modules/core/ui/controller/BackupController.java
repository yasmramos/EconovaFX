package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.service.backup.TenantBackupService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Backup Controller - manages backup and restore operations
 */
public class BackupController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);
    
    private final TenantBackupService tenantBackupService;
    
    @FXML
    private VBox contentArea;
    
    @FXML
    private Button btnBackupNow;
    
    @FXML
    private CheckBox chkIncludeLogs;
    
    @FXML
    private CheckBox chkCompress;
    
    @FXML
    private TextField txtBackupPath;
    
    @FXML
    private Button btnBrowse;
    
    @FXML
    private Button btnRestore;
    
    @FXML
    private TextField txtRestorePath;
    
    @FXML
    private Button btnSelectFile;
    
    @FXML
    private TableView<?> backupTable;
    
    @FXML
    private TableColumn<?, ?> colDate;
    
    @FXML
    private TableColumn<?, ?> colType;
    
    @FXML
    private TableColumn<?, ?> colSize;
    
    @FXML
    private TableColumn<?, ?> colLocation;
    
    @FXML
    private TableColumn<?, ?> colStatus;
    
    public BackupController(TenantBackupService tenantBackupService) {
        this.tenantBackupService = tenantBackupService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("BackupController initialized");
    }
    
    @FXML
    private void handleBackupNow() {
        logger.debug("Backup now clicked");
        boolean includeLogs = chkIncludeLogs.isSelected();
        boolean compress = chkCompress.isSelected();
        String path = txtBackupPath.getText();
        
        showAlert(Alert.AlertType.INFORMATION, "Backup Started", 
                "Creating backup at: " + path + "\nInclude logs: " + includeLogs + "\nCompress: " + compress);
    }
    
    @FXML
    private void handleBrowse() {
        logger.debug("Browse for backup directory clicked");
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Backup Directory");
        Stage stage = (Stage) contentArea.getScene().getWindow();
        File selectedDir = chooser.showDialog(stage);
        if (selectedDir != null) {
            txtBackupPath.setText(selectedDir.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleRestore() {
        logger.debug("Restore backup clicked");
        showAlert(Alert.AlertType.CONFIRMATION, "Confirm Restore", 
                "Are you sure you want to restore from: " + txtRestorePath.getText() + 
                "\n\nWarning: This will overwrite current data!");
    }
    
    @FXML
    private void handleSelectFile() {
        logger.debug("Select backup file clicked");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Backup File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Backup Files", "*.sql", "*.zip", "*.bak"));
        Stage stage = (Stage) contentArea.getScene().getWindow();
        File selectedFile = chooser.showOpenDialog(stage);
        if (selectedFile != null) {
            txtRestorePath.setText(selectedFile.getAbsolutePath());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
