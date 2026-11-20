package com.econovafx;

import com.econovafx.modules.billing.controller.ThirdPartyFormController;
import com.econovafx.modules.billing.service.ThirdPartyService;
import com.econovafx.modules.core.service.CompanyService;
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
 * Smoke test: verifies third-party-form.fxml loads and that initialize() does
 * not throw. A missing or misspelled fx:id surfaces as a NullPointerException
 * the first time the form is opened, because initialize() touches every field.
 */
class ThirdPartyFormFxmlSmokeTest {

    @Test
    void thirdPartyFormFxmlLoadsWithoutErrors() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicReference<Object> controllerRef = new AtomicReference<>();

        ThirdPartyService thirdPartyService = mock(ThirdPartyService.class);
        CompanyService companyService = mock(CompanyService.class);
        when(companyService.findAllActive()).thenReturn(List.of());

        Runnable body = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        ThirdPartyFormFxmlSmokeTest.class.getResource("/fxml/third-party-form.fxml"));
                loader.setControllerFactory(cls ->
                        new ThirdPartyFormController(thirdPartyService, companyService));
                Parent root = loader.load();

                assertNotNull(root, "FXML root should not be null");
                controllerRef.set(loader.getController());

                Stage stage = new Stage();
                stage.setScene(new Scene(root, 600, 550));
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

        assertTrue(latch.await(30, TimeUnit.SECONDS), "the form should load within 30s");
        if (failure.get() != null) {
            throw new AssertionError("FXML load failed", failure.get());
        }
        assertNotNull(controllerRef.get(), "the controller should be wired");
        if (startedHere) {
            Platform.exit();
        }
    }
}
