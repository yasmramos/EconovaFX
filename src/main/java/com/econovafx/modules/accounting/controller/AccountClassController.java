package com.econovafx.modules.accounting.controller;

import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.core.ui.view.ViewFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Account Classification Controller - manages the chart of accounts hierarchy
 */
public class AccountClassController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountClassController.class);
    
    private final AccountService accountService;
    private ViewFactory viewFactory;
    
    @FXML
    private VBox contentArea;
    
    @FXML
    private TreeView<String> classTree;
    
    @FXML
    private Button btnNuevo;
    
    @FXML
    private Button btnEditar;
    
    @FXML
    private Button btnEliminar;
    
    @FXML
    private TextField txtBuscar;
    
    public AccountClassController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Initialize ViewFactory reference (two-phase initialization pattern)
     */
    public void initializeViewFactory(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    /**
     * Complete initialization after ViewFactory is fully constructed
     */
    public void completeInitialization(ViewFactory viewFactory) {
        // Additional initialization if needed
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("AccountClassController initialized");
        initializeTree();
    }
    
    private void initializeTree() {
        TreeItem<String> root = new TreeItem<>("Account Classification");
        root.setExpanded(true);
        
        TreeItem<String> assets = new TreeItem<>("Assets");
        TreeItem<String> liabilities = new TreeItem<>("Liabilities");
        TreeItem<String> equity = new TreeItem<>("Equity");
        TreeItem<String> revenue = new TreeItem<>("Revenue");
        TreeItem<String> expenses = new TreeItem<>("Expenses");
        
        assets.getChildren().addAll(
            new TreeItem<>("Current Assets"),
            new TreeItem<>("Non-Current Assets")
        );
        
        liabilities.getChildren().addAll(
            new TreeItem<>("Current Liabilities"),
            new TreeItem<>("Non-Current Liabilities")
        );
        
        root.getChildren().addAll(assets, liabilities, equity, revenue, expenses);
        classTree.setRoot(root);
        classTree.setShowRoot(false);
    }
    
    @FXML
    private void handleNuevo() {
        logger.debug("New classification clicked");
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
                "New classification form will be implemented soon");
    }
    
    @FXML
    private void handleEditar() {
        TreeItem<String> selected = classTree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", 
                    "Please select a classification to edit");
            return;
        }
        logger.debug("Edit classification: {}", selected.getValue());
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
                "Edit classification form will be implemented soon");
    }
    
    @FXML
    private void handleEliminar() {
        TreeItem<String> selected = classTree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", 
                    "Please select a classification to delete");
            return;
        }
        logger.debug("Delete classification: {}", selected.getValue());
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", 
                "Delete classification will be implemented soon");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
