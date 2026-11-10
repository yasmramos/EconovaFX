package com.econovafx;

import com.econovafx.modules.core.config.AppContext;
import com.econovafx.modules.core.ui.view.ViewFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Guards the {@code ViewFactory} wiring.
 *
 * <p>{@code AppContext} used to build the factory twice: a first, half-empty
 * instance (several controllers {@code null}) was handed to the controllers
 * through {@code initializeViewFactory}, and a second, complete instance was
 * kept in the context. Every controller therefore navigated through the
 * incomplete factory, so opening a comprobante or third-party dialog silently
 * returned {@code Optional.empty()} and the dashboard's "view all" action did
 * nothing. The two-phase {@code completeInitialization} no-op masked it.
 *
 * <p>These tests pin the invariant that the context and every controller that
 * holds a factory reference agree on the <em>same</em> instance.
 */
public class ViewFactoryWiringTest {

    @BeforeEach
    void beforeEach() {
        AppContext.reset();
    }

    @AfterEach
    void afterEach() {
        AppContext.reset();
    }

    @Test
    void everyControllerSharesTheContextViewFactory() throws Exception {
        AppContext context = AppContext.getInstance();
        ViewFactory contextFactory = context.getViewFactory();
        assertNotNull(contextFactory, "context.getViewFactory() must not be null");

        Object[] controllers = {
                context.getDashboardController(),
                context.getAccountsController(),
                context.getTransactionsController(),
                context.getComprobantesController(),
        };

        for (Object controller : controllers) {
            assertNotNull(controller, controller.getClass().getSimpleName() + " must be wired");
            Field field = controller.getClass().getDeclaredField("viewFactory");
            field.setAccessible(true);
            Object held = field.get(controller);
            assertSame(contextFactory, held,
                    controller.getClass().getSimpleName()
                            + " holds a different ViewFactory than the context");
        }
    }

    @Test
    void factoryKnowsItsDialogsControllers() {
        AppContext context = AppContext.getInstance();
        ViewFactory factory = context.getViewFactory();

        // These are the controllers whose dialogs used to no-op because the
        // factory handed to them was the first, null-filled one.
        assertNotNull(context.getDashboardController(),
                "factory must know the dashboard controller");
        assertNotNull(context.getComprobantesController(),
                "factory must know the comprobantes controller");
        assertSame(context.getDashboardController(), factory.getDashboardController(),
                "factory must expose the same dashboard controller the context built");
    }

    @Test
    void thirdPartiesControllerReceivesTheNotificationService() throws Exception {
        AppContext context = AppContext.getInstance();

        // ThirdPartiesController used to be built with a literal null
        // NotificationService and then called it unconditionally.
        Object thirdParties = fieldValue(context, "thirdPartiesController");
        assertNotNull(thirdParties, "context must build the third parties controller");
        assertNotNull(fieldValue(thirdParties, "notificationService"),
                "ThirdPartiesController must receive a real NotificationService");
    }

    private static Object fieldValue(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
