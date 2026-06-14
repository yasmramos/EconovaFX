package com.econovafx.ui.visual;

import com.econovafx.config.AppContext;
import com.econovafx.repository.*;
import com.econovafx.service.*;
import com.econovafx.ui.controller.*;
import com.econovafx.ui.view.ViewFactory;
import io.ebean.Database;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.net.URL;

import static com.econovafx.ui.visual.VisualTestUtils.captureAndSave;

public class MainViewVisualTest extends ApplicationTest {

    private Stage primaryStage;
    private Parent rootNode;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        AppContext appContext = AppContext.getInstance();
        
        // Create mock repositories for visual test (using null database - will fail gracefully for UI-only test)
        Database mockDb = null;
        AccountRepository accountRepo = new AccountRepository(mockDb);
        TransactionRepository transactionRepo = new TransactionRepository(mockDb);
        ThirdPartyRepository thirdPartyRepo = new ThirdPartyRepository(mockDb);
        AccountingPeriodRepository periodRepo = new AccountingPeriodRepository();
        
        // Create services with mock repositories
        AccountService accountService = new AccountService(accountRepo);
        TransactionService transactionService = new TransactionService(transactionRepo, accountRepo);
        ThirdPartyService thirdPartyService = new ThirdPartyService(thirdPartyRepo);
        ExportService exportService = new ExportService();
        AccountingPeriodService periodService = new AccountingPeriodService();
        
        // Create minimal controllers for visual test
        DashboardController dashboardController = new DashboardController(accountService, transactionService, null);
        AccountsController accountsController = new AccountsController(accountService, null);
        TransactionsController transactionsController = new TransactionsController(transactionService, accountService, null);
        ThirdPartiesController thirdPartiesController = new ThirdPartiesController(thirdPartyService, null, exportService, null);
        AccountingPeriodsController periodsController = new AccountingPeriodsController(periodService);
        AccountingClosuresController closuresController = new AccountingClosuresController(periodService);
        AccountFormController accountFormController = new AccountFormController(accountService);
        ThirdPartyFormController thirdPartyFormController = new ThirdPartyFormController(thirdPartyService);
        TransactionEntryController entryController = new TransactionEntryController(accountService, transactionService);
        ComprobantesController comprobantesController = new ComprobantesController(transactionService, accountService, exportService, null);
        
        ViewFactory viewFactory = new ViewFactory(
            dashboardController, accountsController, transactionsController,
            thirdPartiesController, periodsController, closuresController,
            accountFormController, thirdPartyFormController, entryController, comprobantesController,
            accountService, thirdPartyService, transactionService, exportService, periodService
        );
        
        MainViewController controller = new MainViewController(accountService, transactionService, null, viewFactory);
        
        // Load FXML - controller is already specified in FXML file via fx:controller
        URL fxmlUrl = getClass().getResource("/fxml/main-view.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML file not found: /fxml/main-view.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setControllerFactory(cls -> {
            if (cls == MainViewController.class) {
                return controller;
            }
            return null;
        });
        rootNode = loader.load();
        
        Scene scene = new Scene(rootNode, 1280, 800);
        stage.setScene(scene);
        stage.show();
        
        // Force initialization and layout
        WaitForAsyncUtils.waitForFxEvents();
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testMainView() throws IOException {
        captureAndSave(primaryStage, "main-view");
    }
}
