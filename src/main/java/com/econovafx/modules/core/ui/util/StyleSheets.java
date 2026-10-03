package com.econovafx.modules.core.ui.util;

import java.net.URL;
import java.util.Objects;

import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Single source of truth for the stylesheet load order.
 *
 * <p>Load order matters more than it looks in JavaFX CSS. At equal specificity
 * the sheet added last wins. The shared design system is therefore split in
 * two so that both directions of the cascade work:
 *
 * <ul>
 *   <li>{@code controls.css} holds the generic control bases ({@code .button},
 *       {@code .label}, {@code .text-field}, {@code .table-view}...). It must be
 *       loaded <em>before</em> the screen sheets, otherwise a base rule such as
 *       {@code .button}'s background would override a screen rule of equal
 *       specificity such as {@code .login-button} - which is exactly how the
 *       login button ended up white on white.</li>
 *   <li>{@code utilities.css} holds the Tailwind-compatible utilities
 *       ({@code p-4}, {@code text-2xl}, {@code bg-blue-500}, ...). It must be
 *       loaded <em>last</em> so that the utility the FXML author asked for wins
 *       over a component rule such as {@code .card}'s padding.</li>
 * </ul>
 *
 * <p>The order is therefore always:
 *
 * <ol>
 *   <li>{@code theme-tokens.css} - colours and Modena base overrides</li>
 *   <li>{@code controls.css} - generic control bases</li>
 *   <li>the screen's own sheets (main, sidebar, dashboard, dialog, login...)</li>
 *   <li>{@code utilities.css} - Tailwind-compatible utilities, last</li>
 * </ol>
 *
 * <p>Callers no longer have to remember any of that, and a missing sheet fails
 * loudly instead of silently rendering an unstyled screen.
 */
public final class StyleSheets {

    /** Design tokens. Must be first - everything else looks colours up here. */
    private static final String TOKENS = "/css/theme-tokens.css";

    /** Generic control bases. Must come before the screen sheets. */
    private static final String CONTROLS = "/css/controls.css";

    /** Tailwind-compatible utilities. Must be last. */
    private static final String UTILITIES = "/css/utilities.css";

    private StyleSheets() {
    }

    /**
     * Applies the design system to a scene, wrapping {@code screenSheets}
     * between the tokens and the primitives.
     *
     * @param scene       target scene
     * @param screenSheets screen-specific stylesheets, in the order they should
     *                     cascade (first is the weakest)
     */
    public static void apply(Scene scene, String... screenSheets) {
        Objects.requireNonNull(scene, "scene");
        addTo(scene.getStylesheets(), TOKENS);
        addTo(scene.getStylesheets(), CONTROLS);
        if (screenSheets != null) {
            for (String sheet : screenSheets) {
                addTo(scene.getStylesheets(), sheet);
            }
        }
        addTo(scene.getStylesheets(), UTILITIES);
    }

    /**
     * Same ordering, applied at node level. Used for dialogs hosted in a scene
     * whose stylesheets are owned by somebody else.
     *
     * @param root         any {@link Parent} that owns stylesheets
     * @param screenSheets screen-specific stylesheets, weakest first
     */
    public static void apply(Parent root, String... screenSheets) {
        Objects.requireNonNull(root, "root");
        addTo(root.getStylesheets(), TOKENS);
        addTo(root.getStylesheets(), CONTROLS);
        if (screenSheets != null) {
            for (String sheet : screenSheets) {
                addTo(root.getStylesheets(), sheet);
            }
        }
        addTo(root.getStylesheets(), UTILITIES);
    }

    private static void addTo(javafx.collections.ObservableList<String> target, String resource) {
        URL url = StyleSheets.class.getResource(resource);
        if (url == null) {
            throw new IllegalStateException("Stylesheet not found on classpath: " + resource);
        }
        target.add(url.toExternalForm());
    }
}