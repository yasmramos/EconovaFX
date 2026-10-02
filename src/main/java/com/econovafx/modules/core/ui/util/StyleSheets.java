package com.econovafx.modules.core.ui.util;

import java.net.URL;
import java.util.Objects;

import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Single source of truth for the stylesheet load order.
 *
 * <p>Load order matters more than it looks in JavaFX CSS. At equal specificity
 * the sheet added last wins, and {@code primitives.css} ends every sheet with
 * the Tailwind-compatible utilities ({@code p-4}, {@code text-2xl},
 * {@code bg-blue-500}). If it were loaded first, a component rule such as
 * {@code .card}'s padding would beat the utility the FXML author asked for.
 *
 * <p>The order is therefore always:
 *
 * <ol>
 *   <li>{@code theme-tokens.css} - colours and Modena base overrides</li>
 *   <li>the screen's own sheets (main, sidebar, dashboard, dialog, login...)</li>
 *   <li>{@code primitives.css} - controls and utilities, last</li>
 * </ol>
 *
 * <p>Callers no longer have to remember any of that, and a missing sheet fails
 * loudly instead of silently rendering an unstyled screen.
 */
public final class StyleSheets {

    /** Design tokens. Must be first - everything else looks colours up here. */
    private static final String TOKENS = "/css/theme-tokens.css";

    /** Control primitives + utilities. Must be last. */
    private static final String PRIMITIVES = "/css/primitives.css";

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
        if (screenSheets != null) {
            for (String sheet : screenSheets) {
                addTo(scene.getStylesheets(), sheet);
            }
        }
        addTo(scene.getStylesheets(), PRIMITIVES);
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
        if (screenSheets != null) {
            for (String sheet : screenSheets) {
                addTo(root.getStylesheets(), sheet);
            }
        }
        addTo(root.getStylesheets(), PRIMITIVES);
    }

    private static void addTo(javafx.collections.ObservableList<String> target, String resource) {
        URL url = StyleSheets.class.getResource(resource);
        if (url == null) {
            throw new IllegalStateException("Stylesheet not found on classpath: " + resource);
        }
        target.add(url.toExternalForm());
    }
}