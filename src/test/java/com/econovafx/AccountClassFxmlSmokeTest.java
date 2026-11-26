package com.econovafx;

import com.econovafx.modules.accounting.controller.AccountClassController;
import com.econovafx.modules.accounting.service.AccountService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
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
 * Smoke test: verifies account-class.fxml loads and that initialize() does not
 * throw. The page declares fx:ids for its toolbar and tree, so a mismatch
 * between the FXML and the controller used to surface as a NullPointerException
 * the first time the classification page was opened.
 */
class AccountClassFxmlSmokeTest {

    @Test
    void accountClassFxmlLoadsWithoutErrors() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        AccountService accountService = mock(AccountService.class);
        when(accountService.getAllAccounts()).thenReturn(List.of());

        Runnable body = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        AccountClassFxmlSmokeTest.class.getResource("/fxml/account-class.fxml"));
                loader.setControllerFactory(cls -> new AccountClassController(accountService));
                Parent root = loader.load();

                assertNotNull(root, "FXML root should not be null");
                assertNotNull(root.lookup("#classTree"), "classTree should exist in the page");

                Stage stage = new Stage();
                stage.setScene(new Scene(root, 900, 650));
                stage.show();

                TreeView<?> tree = (TreeView<?>) root.lookup("#classTree");
                assertNotNull(tree.getRoot(), "the classification tree should be populated");

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

        assertTrue(latch.await(60, TimeUnit.SECONDS), "FXML load timed out");
        if (failure.get() != null) {
            throw new AssertionError("account-class.fxml failed to load", failure.get());
        }
        if (startedHere) {
            Platform.exit();
        }
    }
}
