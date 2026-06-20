package com.econovafx.ui.view;

import com.econovafx.domain.Account;
import com.econovafx.domain.ThirdParty;
import com.econovafx.domain.Transaction;
import com.econovafx.service.AccountService;
import com.econovafx.service.AccountingPeriodService;
import com.econovafx.service.ExportService;
import com.econovafx.service.NotificationService;
import com.econovafx.service.ThirdPartyService;
import com.econovafx.service.TransactionService;
import com.econovafx.ui.controller.AccountFormController;
import com.econovafx.ui.controller.AccountingClosuresController;
import com.econovafx.ui.controller.AccountingPeriodsController;
import com.econovafx.ui.controller.AccountsController;
import com.econovafx.ui.controller.ComprobanteFormController;
import com.econovafx.ui.controller.ComprobantesController;
import com.econovafx.ui.controller.DashboardController;
import com.econovafx.ui.controller.ExchangeRatesController;
import com.econovafx.ui.controller.SystemSettingsController;
import com.econovafx.ui.controller.ThirdPartiesController;
import com.econovafx.ui.controller.ThirdPartyFormController;
import com.econovafx.ui.controller.TransactionEntryController;
import com.econovafx.ui.controller.TransactionsController;
import com.econovafx.ui.util.ModernDialog;
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
    private final ThirdPartiesController thirdPartiesController;
    private final AccountingPeriodsController accountingPeriodsController;
    private final AccountingClosuresController accountingClosuresController;
    private final ExchangeRatesController exchangeRatesController;
    private final AccountFormController accountFormController;
    private final ThirdPartyFormController thirdPartyFormController;
    private final TransactionEntryController transactionEntryController;
    private final ComprobantesController comprobantesController;
    private final SystemSettingsController systemSettingsController;
    private final AccountService accountService;
    private final ThirdPartyService thirdPartyService;
    private final TransactionService transactionService;
    private final ExportService exportService;
    private final AccountingPeriodService accountingPeriodService;

    public ViewFactory(DashboardController dashboardController,
                      AccountsController accountsController,
                      TransactionsController transactionsController,
                      ThirdPartiesController thirdPartiesController,
                      AccountingPeriodsController accountingPeriodsController,
                      AccountingClosuresController accountingClosuresController,
                      ExchangeRatesController exchangeRatesController,
                      AccountFormController accountFormController,
                      ThirdPartyFormController thirdPartyFormController,
                      TransactionEntryController transactionEntryController,
                      ComprobantesController comprobantesController,
                      SystemSettingsController systemSettingsController,
                      AccountService accountService,
                      ThirdPartyService thirdPartyService,
                      TransactionService transactionService,
                      ExportService exportService,
                      AccountingPeriodService accountingPeriodService,
                      NotificationService notificationService) {
        this.dashboardController = dashboardController;
        this.accountsController = accountsController;
        this.transactionsController = transactionsController;
        this.thirdPartiesController = thirdPartiesController;
        this.accountingPeriodsController = accountingPeriodsController;
        this.accountingClosuresController = accountingClosuresController;
        this.exchangeRatesController = exchangeRatesController;
        this.accountFormController = accountFormController;
        this.thirdPartyFormController = thirdPartyFormController;
        this.transactionEntryController = transactionEntryController;
        this.comprobantesController = comprobantesController;
        this.systemSettingsController = systemSettingsController;
        this.accountService = accountService;
        this.thirdPartyService = thirdPartyService;
        this.transactionService = transactionService;
        this.exportService = exportService;
        this.accountingPeriodService = accountingPeriodService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public ThirdPartyService getThirdPartyService() {
        return thirdPartyService;
    }

    public ExportService getExportService() {
        return exportService;
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

    public Node loadFXML(String url , Class<?> clazz){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
            loader.setControllerFactory(cls -> clazz);
            return loader.load();
        } catch (IOException e) {
            logger.error("Error loading view", e);
            throw new RuntimeException("Failed to load view", e);
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
    
    public Node createThirdPartiesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/third-parties.fxml"));
            loader.setControllerFactory(cls -> thirdPartiesController);
            Node view = loader.load();
            // Store reference for dialog owner lookup
            this.currentThirdPartiesView = view;
            return view;
        } catch (IOException e) {
            logger.error("Error loading third parties view", e);
            throw new RuntimeException("Failed to load third parties view", e);
        }
    }
    
    private Node currentThirdPartiesView;
    
    public Node createAccountingPeriodsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accounting-periods.fxml"));
            loader.setControllerFactory(cls -> accountingPeriodsController);
            return loader.load();
        } catch (IOException e) {
            logger.error("Error loading accounting periods view", e);
            throw new RuntimeException("Failed to load accounting periods view", e);
        }
    }
    
    public Node createAccountingClosuresView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accounting-closures.fxml"));
            loader.setControllerFactory(cls -> accountingClosuresController);
            return loader.load();
        } catch (IOException e) {
            logger.error("Error loading accounting closures view", e);
            throw new RuntimeException("Failed to load accounting closures view", e);
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
            ComprobanteFormController controller = new ComprobanteFormController(accountService, transactionService, thirdPartyService);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/comprobante-form.fxml"));
            loader.setControllerFactory(cls -> controller);
            Parent root = loader.load();

            controller.setEditingTransaction(transaction);

            // Use ModernDialog for web-style modal with backdrop blur
            Stage ownerStage = comprobantesController != null && comprobantesController.getRootNode() != null ? 
                (Stage) comprobantesController.getRootNode().getScene().getWindow() : null;
            if (ownerStage == null) {
                logger.warn("Could not determine owner stage for comprobante dialog");
                return Optional.empty();
            }
            
            ModernDialog.showAndWait(ownerStage, root, transaction == null ? "New Voucher" : "Edit Voucher");

            return Optional.ofNullable(controller.getResult());

        } catch (IOException e) {
            logger.error("Error showing comprobante form dialog", e);
            return Optional.empty();
        }
    }
    
    public Optional<ThirdParty> showThirdPartyFormDialog(ThirdParty thirdParty) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/third-party-form.fxml"));
            loader.setControllerFactory(cls -> thirdPartyFormController);
            Parent root = loader.load();

            thirdPartyFormController.setEditingThirdParty(thirdParty);

            // Use ModernDialog for web-style modal with backdrop blur
            Stage ownerStage = currentThirdPartiesView != null ? 
                (Stage) currentThirdPartiesView.getScene().getWindow() : null;
            if (ownerStage == null) {
                logger.warn("Could not determine owner stage for dialog");
                return Optional.empty();
            }
            ModernDialog.showAndWait(ownerStage, root, thirdParty == null ? "New Third Party" : "Edit Third Party");

            return Optional.ofNullable(thirdPartyFormController.getResult());

        } catch (IOException e) {
            logger.error("Error showing third party form dialog", e);
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

    /**
     * Load System Settings view in a new window
     */
    public void loadSystemSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/econovafx/ui/view/system-settings.fxml"));
            loader.setControllerFactory(cls -> systemSettingsController);
            Parent root = loader.load();

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Configuración del Sistema");
            stage.setScene(new Scene(root, 900, 650));
            
            // Add styles
            stage.getScene().getStylesheets().add(getClass().getResource("/com/econovafx/ui/css/system-settings.css").toExternalForm());
            
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading system settings view", e);
            throw new RuntimeException("Failed to load system settings view", e);
        }
    }

    /**
     * Create Exchange Rates view
     */
    public Node createExchangeRatesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exchange-rates.fxml"));
            loader.setControllerFactory(cls -> exchangeRatesController);
            return loader.load();
        } catch (IOException e) {
            logger.error("Error loading exchange rates view", e);
            throw new RuntimeException("Failed to load exchange rates view", e);
        }
    }
}
