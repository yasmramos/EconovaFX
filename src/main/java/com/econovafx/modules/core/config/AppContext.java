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
import com.econovafx.modules.core.ui.controller.*;
import com.econovafx.modules.core.ui.view.ViewFactory;
import io.avaje.inject.BeanScope;
import io.ebean.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application context - wrapper around Avaje Inject BeanScope Manages all
 * dependencies automatically via dependency injection
 */
public final class AppContext {

    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);
    private static AppContext instance;

    private final BeanScope beanScope;

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

    // View Factory
    private ViewFactory viewFactory;

    private AppContext() {
        logger.info("Initializing application context with Avaje Inject...");

        // Build dependency injection container
        beanScope = BeanScope.builder().build();

        // Get beans from DI container
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

        // Create controllers with DI-injected services
        // ViewFactory is null initially, will be recreated below
        accountFormController = new AccountFormController(accountService);
        thirdPartyFormController = new ThirdPartyFormController(thirdPartyService);
        transactionEntryController = new TransactionEntryController(accountService, transactionService);
        comprobantesController = new ComprobantesController(transactionService, accountService, exportService, null);
        dashboardController = new DashboardController(accountService, transactionService, null);
        accountsController = new AccountsController(accountService, null);
        transactionsController = new TransactionsController(transactionService, accountService, null);
        thirdPartiesController = new ThirdPartiesController(thirdPartyService, null, exportService, null);
        accountingPeriodsController = new AccountingPeriodsController(accountingPeriodService);

        // Create view factory with controllers
        accountingClosuresController = new AccountingClosuresController(accountingPeriodService);
        exchangeRatesController = new ExchangeRatesController();
        
        // Get notification service instance from bean scope
        NotificationService notificationService = beanScope.get(NotificationService.class);
        ExchangeRateService exchangeRateService = beanScope.get(ExchangeRateService.class);
        
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
                accountService,
                thirdPartyService,
                transactionService,
                exportService,
                accountingPeriodService,
                notificationService
        );

        // Re-create controllers that need viewFactory
        dashboardController = new DashboardController(accountService, transactionService, viewFactory);
        accountsController = new AccountsController(accountService, viewFactory);
        transactionsController = new TransactionsController(transactionService, accountService, viewFactory);
        thirdPartiesController = new ThirdPartiesController(thirdPartyService, viewFactory, exportService, null);
        comprobantesController = new ComprobantesController(transactionService, accountService, exportService, viewFactory);
        accountingPeriodsController = new AccountingPeriodsController(accountingPeriodService);
        accountingClosuresController = new AccountingClosuresController(accountingPeriodService);
        exchangeRatesController = new ExchangeRatesController();

        // Re-create view factory with updated controllers
        NotificationService notificationService2 = beanScope.get(NotificationService.class);
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
                accountService,
                thirdPartyService,
                transactionService,
                exportService,
                accountingPeriodService,
                notificationService2
        );

        logger.info("Application context initialized successfully with Avaje Inject");
    }

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
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
