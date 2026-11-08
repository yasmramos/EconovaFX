package com.econovafx.modules.core.ui.util;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Duration;

/**
 * The application's real motion layer.
 *
 * <p>JavaFX CSS has no {@code -fx-transition}, {@code @keyframes} or
 * {@code -fx-animation}. The stylesheets used to declare them, so every hover
 * snapped instantly and the "fade-in" on the KPI cards never ran - the rules
 * were dead text. Anything that should actually move is animated here, in code,
 * on the nodes themselves.
 *
 * <p>Motion is kept short and subtle (a web app's feel, not a game's): a
 * fade-and-rise when a view enters, a small stagger so a grid of cards settles
 * rather than appearing all at once, and a short lift on hover.
 *
 * <p>Honours a reduced-motion preference: when set, every method becomes a
 * no-op and nodes keep their resting state. Tests flip it off so the
 * animations stay deterministic.
 *
 * <p>A node that is not in a scene yet (an FXML view built before it is added
 * to its parent) is animated as soon as it attaches, so the entrance never
 * silently drops.
 */
public final class UiMotion {

    /** Quick feedback: hover lifts, small state changes. */
    public static final Duration FAST = Duration.millis(140);

    /** View entrance and card stagger. */
    public static final Duration BASE = Duration.millis(220);

    /** How far a node rises while fading in. */
    private static final double ENTER_RISE = 8;

    /** Gap between successive cards in a stagger. */
    private static final Duration STAGGER_STEP = Duration.millis(45);

    private static volatile boolean reducedMotion = Boolean.getBoolean("econovafx.reducedMotion");

    private UiMotion() {
    }

    /** Reduced motion is on by default only when {@code -Deconovafx.reducedMotion=true}. */
    public static boolean isReducedMotion() {
        return reducedMotion;
    }

    /** Lets tests (and a future settings toggle) drive the preference. */
    public static void setReducedMotion(boolean reduced) {
        reducedMotion = reduced;
    }

    /** Fades a view in and lets it rise into place. */
    public static void enter(Node node) {
        if (node == null || reducedMotion) {
            return;
        }
        whenInScene(node, () -> {
            node.setOpacity(0);
            node.setTranslateY(ENTER_RISE);

            FadeTransition fade = new FadeTransition(BASE, node);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition rise = new TranslateTransition(BASE, node);
            rise.setFromY(ENTER_RISE);
            rise.setToY(0);
            rise.setInterpolator(Interpolator.EASE_OUT);

            new ParallelTransition(fade, rise).play();
        });
    }

    /**
     * Runs the entrance over a set of nodes with a small delay between each, so
     * a grid of cards settles in sequence instead of all at once.
     */
    public static void enterStaggered(List<? extends Node> nodes) {
        if (nodes == null || reducedMotion) {
            return;
        }
        List<Node> targets = new ArrayList<>();
        for (Node node : nodes) {
            if (node != null) {
                targets.add(node);
            }
        }
        if (targets.isEmpty()) {
            return;
        }
        whenInScene(targets.get(0), () -> playStagger(targets));
    }

    /**
     * Lifts a card by {@code dy} pixels on hover and eases it back on exit.
     * The stylesheet must not also set {@code -fx-translate-y} on the same node,
     * or the sheet value would override the animation every frame.
     */
    public static void installHoverLift(Node node, double dy) {
        if (node == null || reducedMotion) {
            return;
        }
        node.setOnMouseEntered(e -> glide(node, -dy));
        node.setOnMouseExited(e -> glide(node, 0));
    }

    private static void playStagger(List<Node> targets) {
        Timeline timeline = new Timeline();
        Duration at = Duration.ZERO;
        for (Node node : targets) {
            node.setOpacity(0);
            node.setTranslateY(ENTER_RISE);
            timeline.getKeyFrames().addAll(
                    new KeyFrame(at, new KeyValue(node.opacityProperty(), 0, Interpolator.LINEAR)),
                    new KeyFrame(at.add(BASE), new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(node.translateYProperty(), 0, Interpolator.EASE_OUT)));
            at = at.add(STAGGER_STEP);
        }
        timeline.play();
    }

    private static void glide(Node node, double toY) {
        TranslateTransition lift = new TranslateTransition(FAST, node);
        lift.setToY(toY);
        lift.setInterpolator(Interpolator.EASE_OUT);
        lift.play();
    }

    /** Runs {@code action} now if {@code node} is in a scene, otherwise on attach. */
    private static void whenInScene(Node node, Runnable action) {
        if (node.getScene() != null) {
            action.run();
            return;
        }
        ChangeListener<Scene> onScene = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Scene> obs, Scene was, Scene now) {
                if (now != null) {
                    obs.removeListener(this);
                    action.run();
                }
            }
        };
        node.sceneProperty().addListener(onScene);
    }
}
