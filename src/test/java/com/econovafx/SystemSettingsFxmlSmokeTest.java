package com.econovafx;

import com.econovafx.modules.core.ui.controller.SystemSettingsController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test: verifies system-settings.fxml loads and that initialize() does not
 * throw. The FXML only declares the shell (rootContainer, sidebarContainer,
 * contentArea); the form controls are built programmatically, so a missing
 * fx:id used to surface as a NullPointerException at runtime.
 */
class SystemSettingsFxmlSmokeTest {

    @Test
    void systemSettingsFxmlLoadsWithoutErrors() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Runnable body = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                    SystemSettingsFxmlSmokeTest.class.getResource(
                        "/com/econovafx/ui/view/system-settings.fxml"));
                // Mirror ViewFactory: the FXML declares fx:controller, so the
                // instance is supplied through a controller factory.
                loader.setControllerFactory(cls -> new SystemSettingsController());
                Parent root = loader.load();

                assertNotNull(root, "FXML root should not be null");

                Stage stage = new Stage();
                stage.setScene(new Scene(root, 900, 650));
                stage.show();

                // Header "Cerrar" button resolves #closeWindow at load time; make
                // sure it is present and wired without throwing when invoked.
                Button close = (Button) root.lookup(".close-button");
                assertNotNull(close, "close-button should exist in the header");
                close.fire();

                stage.close();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        };

        // Another FX test in the same JVM may already have started the toolkit;
        // Platform.startup then throws IllegalStateException, so reuse it instead.
        boolean startedHere = false;
        try {
            Platform.startup(body);
            startedHere = true;
        } catch (IllegalStateException alreadyRunning) {
            Platform.runLater(body);
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS), "FXML load timed out");
        if (failure.get() != null) {
            throw new AssertionError("system-settings.fxml failed to load", failure.get());
        }
        if (startedHere) {
            Platform.exit();
        }
    }
}
