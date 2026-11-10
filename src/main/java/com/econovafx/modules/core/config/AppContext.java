package com.econovafx.modules.core.config;

import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.repository.TransactionRepository;
import com.econovafx.modules.billing.repository.ThirdPartyRepository;
import com.econovafx.modules.core.repository.UserRepository;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.UserService;
import com.econovafx.modules.billing.service.ThirdPartyService;
import com.econovafx.modules.core.service.ExportService;
import com.econovafx.modules.accounting.service.AccountingPeriodService;
import com.econovafx.modules.core.service.NotificationService;
import com.econovafx.modules.core.service.ExchangeRateService;
import com.econovafx.modules.core.service.SystemConfigService;
import com.econovafx.modules.core.ui.controller.*;
import com.econovafx.modules.core.ui.view.ViewFactory;
import com.econovafx.modules.accounting.controller.AccountsController;
import com.econovafx.modules.accounting.controller.TransactionsController;
import com.econovafx.modules.accounting.controller.AccountFormController;
import com.econovafx.modules.accounting.controller.TransactionEntryController;
import com.econovafx.modules.accounting.controller.ComprobantesController;
import com.econovafx.modules.billing.controller.ThirdPartiesController;
import com.econovafx.modules.billing.controller.ThirdPartyFormController;
import com.econovafx.modules.accounting.controller.AccountingPeriodsController;
import com.econovafx.modules.accounting.controller.AccountingClosuresController;
import com.econovafx.modules.inventory.controller.InventoryController;
import com.econovafx.modules.core.ui.controller.WebViewController;
import com.econovafx.modules.inventory.service.InventoryService;
import io.avaje.inject.BeanScope;
import io.ebean.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Application context - wrapper around Avaje Inject BeanScope Manages all
 * dependencies automatically via dependency injection
 */
public final class AppContext {

    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);
    private static AppContext instance;

    private final BeanScope beanScope;

    // Configuration
    private final AppConfig appConfig;

    // Database
    private final Database database;

    // Repositories (auto-injected)
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // Services (auto-injected)
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;

    // Controllers (manual wiring for JavaFX FXML)
    private DashboardController dashboardController;
    private AccountsController accountsController;
    private TransactionsController transactionsController;
    private ThirdPartiesController thirdPartiesController;
    private AccountingPeriodsController accountingPeriodsController;
    private AccountingClosuresController accountingClosuresController;
    private ExchangeRatesController exchangeRatesController;
    private final AccountFormController accountFormController;
    private final ThirdPartyFormController thirdPartyFormController;
    private final TransactionEntryController transactionEntryController;
    private ComprobantesController comprobantesController;
    private SystemSettingsController systemSettingsController;
    private InventoryController inventoryController;
    private WebViewController webViewController;

    // View Factory
    private ViewFactory viewFactory;

    private AppContext() {
        logger.info("Initializing application context with Avaje Inject...");

        // Build dependency injection container
        beanScope = BeanScope.builder().build();

        // Get AppConfig bean first (it's auto-injected as a Singleton)
        appConfig = beanScope.get(AppConfig.class);
        logger.info("AppConfig bean retrieved from context: {}", appConfig.appName);

        // Get beans from DI container (NotificationService is now auto-injected as @Singleton)
        database = beanScope.get(Database.class);
        accountRepository = beanScope.get(AccountRepository.class);
        transactionRepository = beanScope.get(TransactionRepository.class);
        userRepository = beanScope.get(UserRepository.class);
        accountService = beanScope.get(AccountService.class);
        transactionService = beanScope.get(TransactionService.class);
        userService = beanScope.get(UserService.class);
        ThirdPartyService thirdPartyService = beanScope.get(ThirdPartyService.class);
        ExportService exportService = beanScope.get(ExportService.class);
        AccountingPeriodService accountingPeriodService = beanScope.get(AccountingPeriodService.class);
        NotificationService notificationService = beanScope.get(NotificationService.class);
        SystemConfigService systemConfigService = beanScope.get(SystemConfigService.class);
        
        ExchangeRateService exchangeRateService = beanScope.get(ExchangeRateService.class);

        // Create controllers without ViewFactory initially
        // Controllers that don't need ViewFactory
        accountFormController = new AccountFormController(accountService);
        thirdPartyFormController = new ThirdPartyFormController(thirdPartyService);
        transactionEntryController = new TransactionEntryController(accountService, transactionService);
        accountingPeriodsController = new AccountingPeriodsController(accountingPeriodService);
        accountingClosuresController = new AccountingClosuresController(accountingPeriodService);
        exchangeRatesController = new ExchangeRatesController(exchangeRateService);
        
        // Get InventoryService and UserContext for InventoryController
        InventoryService inventoryService = beanScope.get(InventoryService.class);
        UserContext userContext = beanScope.get(UserContext.class);
        inventoryController = new InventoryController(inventoryService, userContext);

        // Financial reporting view (statements + Trial Balance + General Ledger)
        com.econovafx.modules.reporting.controller.FinancialReportingViewController financialReportingViewController =
                new com.econovafx.modules.reporting.controller.FinancialReportingViewController(
                        beanScope.get(com.econovafx.modules.reporting.controller.FinancialReportingController.class),
                        beanScope.get(com.econovafx.modules.accounting.service.AccountingReportService.class),
                        accountService,
                        exportService);

        // Create every controller first. They only need the ViewFactory through
        // initializeViewFactory below, so nothing here depends on the factory yet.
        dashboardController = new DashboardController(accountService, transactionService, systemConfigService);
        accountsController = new AccountsController(accountService);
        transactionsController = new TransactionsController(transactionService, accountService);
        thirdPartiesController = new ThirdPartiesController(thirdPartyService, exportService, notificationService);
        comprobantesController = new ComprobantesController(transactionService, accountService, exportService);

        // Create SystemSettingsController using DI to inject BackupSchedulerService
        systemSettingsController = beanScope.get(SystemSettingsController.class);

        // Create WebViewController with injected LocalWebServer
        webViewController = beanScope.get(WebViewController.class);

        // Build the single ViewFactory over the finished controllers.
        viewFactory = new ViewFactory(
                dashboardController,
                accountsController,
                transactionsController,
                thirdPartiesController,
                accountingPeriodsController,
                accountingClosuresController,
                exchangeRatesController,
                accountFormController,
                thirdPartyFormController,
                transactionEntryController,
                comprobantesController,
                systemSettingsController,
                inventoryController,
                webViewController,
                financialReportingViewController,
                accountService,
                thirdPartyService,
                transactionService,
                exportService,
                accountingPeriodService,
                inventoryService
        );

        // Hand that one instance to every controller that navigates through it.
        // Building a second ViewFactory here used to leave the controllers holding
        // the first, half-empty one, so dialogs and navigation silently no-oped.
        dashboardController.initializeViewFactory(viewFactory);
        accountsController.initializeViewFactory(viewFactory);
        transactionsController.initializeViewFactory(viewFactory);
        thirdPartiesController.initializeViewFactory(viewFactory);
        comprobantesController.initializeViewFactory(viewFactory);
        systemSettingsController.initializeViewFactory(viewFactory);

        logger.info("Application context initialized successfully with Avaje Inject");
    }

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    /**
     * Check if the application context is initialized
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Reset the application context (for testing purposes)
     */
    public static void reset() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    /**
     * Close the BeanScope and release all resources
     */
    public void close() {
        if (beanScope != null) {
            beanScope.close();
            logger.info("BeanScope closed");
        }
    }

    // Getters
    public BeanScope getBeanScope() {
        return beanScope;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public Database getDatabase() {
        return database;
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }

    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public UserService getUserService() {
        return userService;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public DashboardController getDashboardController() {
        return dashboardController;
    }

    public AccountsController getAccountsController() {
        return accountsController;
    }

    public TransactionsController getTransactionsController() {
        return transactionsController;
    }

    public AccountFormController getAccountFormController() {
        return accountFormController;
    }

    public TransactionEntryController getTransactionEntryController() {
        return transactionEntryController;
    }

    public ComprobantesController getComprobantesController() {
        return comprobantesController;
    }
}
