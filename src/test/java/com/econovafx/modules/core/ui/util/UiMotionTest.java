package com.econovafx.modules.core.ui.util;

import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behavioural tests for the motion layer.
 *
 * <p>These replace the dead CSS the app used to carry: instead of a
 * {@code -fx-transition} the engine ignored, motion is code and is exercised
 * here on real nodes in a real scene.
 */
public class UiMotionTest extends ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
    }

    @BeforeEach
    public void setUp() throws Exception {
        UiMotion.setReducedMotion(false);
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(ChartsAndThemeTest.TestApp.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        UiMotion.setReducedMotion(false);
        FxToolkit.cleanupStages();
    }

    @Test
    public void enterFadesTheViewInToItsRestingState() throws Exception {
        Region node = new Region();
        onFxThread(() -> {
            stage.setScene(new Scene(new StackPane(node), 200, 200));
            stage.show();
            UiMotion.enter(node);
            // Same-runnable read: a separate runnable can observe a frame later.
            assertEquals(0.0, node.getOpacity(), 0.001, "enter starts from transparent");
            assertTrue(node.getTranslateY() > 0, "enter starts slightly below its resting spot");
        });

        await(() -> opacity(node) == 1.0 && translateY(node) == 0.0,
                "the entrance must settle at full opacity and zero offset");
    }

    @Test
    public void enterDefersUntilTheViewIsAttachedToAScene() throws Exception {
        Region node = new Region();
        onFxThread(() -> UiMotion.enter(node));

        assertEquals(1.0, opacity(node), 0.001, "a detached node must not be hidden early");

        onFxThread(() -> {
            stage.setScene(new Scene(new StackPane(node), 200, 200));
            stage.show();
        });

        await(() -> opacity(node) == 1.0 && translateY(node) == 0.0,
                "the deferred entrance must run once the view attaches");
    }

    @Test
    public void staggeredEntranceSettlesEveryCard() throws Exception {
        Region a = new Region();
        Region b = new Region();
        Region c = new Region();
        onFxThread(() -> {
            stage.setScene(new Scene(new VBox(a, b, c), 200, 200));
            stage.show();
            UiMotion.enterStaggered(List.of(a, b, c));
            // Read the start state in the same runnable that kicks the animation.
            // From another runnable a frame may already have advanced on a busy
            // machine, which is exactly how this flaked in CI.
            assertEquals(0.0, a.getOpacity(), 0.001, "the stagger starts hidden");
            assertTrue(a.getTranslateY() > 0, "the stagger starts below its resting spot");
        });

        await(() -> opacity(a) == 1.0 && opacity(b) == 1.0 && opacity(c) == 1.0
                        && translateY(a) == 0.0 && translateY(c) == 0.0,
                "every card must reach its resting state");
    }

    @Test
    public void hoverLiftMovesTheCardAndReleasesIt() throws Exception {
        Region card = new Region();
        onFxThread(() -> {
            stage.setScene(new Scene(new StackPane(card), 200, 200));
            stage.show();
            UiMotion.installHoverLift(card, 2);
            card.fireEvent(mouse(MouseEvent.MOUSE_ENTERED));
        });
        await(() -> translateY(card) < 0, "hovering must lift the card");

        onFxThread(() -> card.fireEvent(mouse(MouseEvent.MOUSE_EXITED)));
        await(() -> translateY(card) == 0.0, "leaving must ease the card back");
    }

    @Test
    public void reducedMotionLeavesNodesUntouched() throws Exception {
        Region card = new Region();
        onFxThread(() -> {
            stage.setScene(new Scene(new StackPane(card), 200, 200));
            stage.show();
            UiMotion.setReducedMotion(true);
            UiMotion.enter(card);
            UiMotion.installHoverLift(card, 2);
            card.fireEvent(mouse(MouseEvent.MOUSE_ENTERED));
        });

        assertEquals(1.0, opacity(card), 0.001);
        assertEquals(0.0, translateY(card), 0.001, "reduced motion must not move the node");
    }

    private void onFxThread(Runnable action) throws Exception {
        WaitForAsyncUtils.asyncFx(action).get(2, TimeUnit.SECONDS);
    }

    private static double opacity(Region node) {
        return read(node::getOpacity);
    }

    private static double translateY(Region node) {
        return read(node::getTranslateY);
    }

    private static double read(java.util.function.DoubleSupplier supplier) {
        try {
            return WaitForAsyncUtils.asyncFx(supplier::getAsDouble).get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static MouseEvent mouse(javafx.event.EventType<MouseEvent> type) {
        return new MouseEvent(type, 0, 0, 0, 0, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false, null);
    }

    private static void await(BooleanSupplier condition, String message) {
        try {
            WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS, condition::getAsBoolean);
        } catch (Exception e) {
            throw new AssertionError(message, e);
        }
    }
}
