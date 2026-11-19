package com.econovafx;

import com.econovafx.modules.accounting.repository.FinancialStatementModelRepository;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.core.service.CompanyService;
import com.econovafx.modules.core.service.ExportService;
import com.econovafx.modules.reporting.controller.FinancialReportingController;
import com.econovafx.modules.reporting.controller.FinancialReportingViewController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Smoke test: verifies financial-reporting.fxml loads and that initialize()
 * does not throw. A missing or misspelled fx:id surfaces as a
 * NullPointerException the first time the view is opened, because initialize()
 * touches every field.
 */
class FinancialReportingFxmlSmokeTest {

    @Test
    void financialReportingFxmlLoadsWithoutErrors() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicReference<Object> controllerRef = new AtomicReference<>();

        CompanyService companyService = mock(CompanyService.class);
        when(companyService.findAllActive()).thenReturn(List.of());
        FinancialStatementModelRepository modelRepository = mock(FinancialStatementModelRepository.class);
        when(modelRepository.findAll()).thenReturn(List.of());

        Runnable body = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        FinancialReportingFxmlSmokeTest.class.getResource("/fxml/financial-reporting.fxml"));
                loader.setControllerFactory(cls -> new FinancialReportingViewController(
                        mock(FinancialReportingController.class),
                        mock(com.econovafx.modules.accounting.service.AccountingReportService.class),
                        mock(AccountService.class),
                        mock(ExportService.class),
                        companyService,
                        modelRepository));
                Parent root = loader.load();

                assertNotNull(root, "FXML root should not be null");
                controllerRef.set(loader.getController());

                Stage stage = new Stage();
                stage.setScene(new Scene(root, 1200, 800));
                stage.show();
                stage.close();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        };

        boolean startedHere = false;
        try {
            Platform.startup(body);
            startedHere = true;
        } catch (IllegalStateException alreadyRunning) {
            Platform.runLater(body);
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "the view should load within 30s");
        if (failure.get() != null) {
            throw new AssertionError("FXML load failed", failure.get());
        }
        assertNotNull(controllerRef.get(), "the controller should be wired");
        if (startedHere) {
            Platform.exit();
        }
    }
}
