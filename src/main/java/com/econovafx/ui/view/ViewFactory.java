package com.econovafx.ui.view;

import com.econovafx.domain.Account;
import com.econovafx.domain.Transaction;
import com.econovafx.service.AccountService;
import com.econovafx.service.TransactionService;
import com.econovafx.ui.controller.AccountFormController;
import com.econovafx.ui.controller.AccountsController;
import com.econovafx.ui.controller.ComprobanteFormController;
import com.econovafx.ui.controller.ComprobantesController;
import com.econovafx.ui.controller.DashboardController;
import com.econovafx.ui.controller.TransactionEntryController;
import com.econovafx.ui.controller.TransactionsController;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Factory for creating UI views and dialogs
 */
public class ViewFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ViewFactory.class);
    
    private final DashboardController dashboardController;
    private final AccountsController accountsController;
    private final TransactionsController transactionsController;
    private final AccountFormController accountFormController;
    private final TransactionEntryController transactionEntryController;
    private final ComprobantesController comprobantesController;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public ViewFactory(DashboardController dashboardController,
                      AccountsController accountsController,
                      TransactionsController transactionsController,
                      AccountFormController accountFormController,
                      TransactionEntryController transactionEntryController,
                      ComprobantesController comprobantesController,
                      AccountService accountService,
                      TransactionService transactionService) {
        this.dashboardController = dashboardController;
        this.accountsController = accountsController;
        this.transactionsController = transactionsController;
        this.accountFormController = accountFormController;
        this.transactionEntryController = transactionEntryController;
        this.comprobantesController = comprobantesController;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }
    
    public Node createDashboardView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            loader.setControllerFactory(cls -> dashboardController);
            return loader.load();
        } catch (IOException e) {
            logger.error("Error loading dashboard view", e);
            throw new RuntimeException("Failed to load dashboard view", e);
        }
    }
    
    public Node createAccountsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accounts.fxml"));
            loader.setControllerFactory(cls -> accountsController);
            return loader.load();
        } catch (IOException e) {
            logger.error("Error loading accounts view", e);
            throw new RuntimeException("Failed to load accounts view", e);
        }
    }
    
    public Node createTransactionsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transactions.fxml"));
            loader.setControllerFactory(cls -> transactionsController);
            return loader.load();
        } catch (IOException e) {
            logger.error("Error loading transactions view", e);
            throw new RuntimeException("Failed to load transactions view", e);
        }
    }

    public Node createComprobantesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/comprobantes.fxml"));
            loader.setControllerFactory(cls -> comprobantesController);
            return loader.load();
        } catch (IOException e) {
            logger.error("Error loading comprobantes view", e);
            throw new RuntimeException("Failed to load comprobantes view", e);
        }
    }
    
    public Optional<Account> showAccountFormDialog(Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/account-form.fxml"));
            loader.setControllerFactory(cls -> accountFormController);
            Parent root = loader.load();

            accountFormController.setEditingAccount(account);

            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(account == null ? "Nueva Cuenta" : "Editar Cuenta");
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            
            // Add custom styles
            scene.getStylesheets().add(getClass().getResource("/styles/dialog-styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setResizable(false);
            
            // Make dialog draggable via FXML
            setupDraggable(stage, root);
            
            stage.showAndWait();

            return Optional.ofNullable(accountFormController.getResult());

        } catch (IOException e) {
            logger.error("Error showing account form dialog", e);
            return Optional.empty();
        }
    }
    
    public Optional<Transaction> showTransactionEntryDialog(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transaction-entry.fxml"));
            loader.setControllerFactory(cls -> transactionEntryController);
            Parent root = loader.load();

            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nueva Transacción");
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            
            // Add custom styles
            scene.getStylesheets().add(getClass().getResource("/styles/dialog-styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setResizable(true);
            
            // Make dialog draggable
            setupDraggable(stage, root);
            
            stage.showAndWait();

            return Optional.ofNullable(transactionEntryController.getResult());

        } catch (IOException e) {
            logger.error("Error showing transaction entry dialog", e);
            return Optional.empty();
        }
    }

    public Optional<Transaction> showComprobanteFormDialog(Transaction transaction) {
        try {
            ComprobanteFormController controller = new ComprobanteFormController(accountService, transactionService);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/comprobante-form.fxml"));
            loader.setControllerFactory(cls -> controller);
            Parent root = loader.load();

            controller.setEditingTransaction(transaction);

            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(transaction == null ? "Nuevo Comprobante" : "Editar Comprobante");
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            
            // Add custom styles
            scene.getStylesheets().add(getClass().getResource("/styles/dialog-styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setResizable(true);
            
            // Make dialog draggable
            setupDraggable(stage, root);
            
            stage.showAndWait();

            return Optional.ofNullable(controller.getResult());

        } catch (IOException e) {
            logger.error("Error showing comprobante form dialog", e);
            return Optional.empty();
        }
    }

    /**
     * Setup draggable window for undecorated stages
     */
    private void setupDraggable(Stage stage, Parent root) {
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];

        root.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset[0]);
            stage.setY(event.getScreenY() - yOffset[0]);
        });
    }
}
