package com.econovafx.modules.core.ui.util;

import java.util.Objects;
import java.util.prefs.Preferences;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

/**
 * Single source of truth for the light/dark theme.
 *
 * <p>The theme is expressed entirely as the presence of the {@code dark} style
 * class on the scene root: {@code theme-tokens.css} redefines the semantic
 * tokens under {@code .root.dark}, and every rule that reads a token flips with
 * it. Nothing else in the CSS needs to know which theme is active.
 *
 * <p>The choice is remembered across runs in the user preferences. It is stored
 * in {@link Preferences} rather than in the external config file because
 * {@code ConfigFileUtil.saveProperties} rewrites that file whole and would drop
 * the database settings.
 */
public final class ThemeManager {

    /** Style class that switches the token block in theme-tokens.css. */
    private static final String DARK_CLASS = "dark";

    private static final Preferences PREFS = Preferences.userRoot().node("com/econovafx/ui");
    private static final String KEY_DARK = "theme.dark";

    /** Page fill used before CSS lands, so startup never flashes the wrong tone. */
    private static final Color LIGHT_PAGE = Color.web("#fafafa");
    private static final Color DARK_PAGE = Color.web("#09090b");

    private static boolean dark = PREFS.getBoolean(KEY_DARK, false);

    private ThemeManager() {
    }

    public static boolean isDark() {
        return dark;
    }

    /** Applies the current theme to a scene root and its backdrop fill. */
    public static void applyTo(Scene scene) {
        Objects.requireNonNull(scene, "scene");
        applyTo(scene.getRoot());
        scene.setFill(dark ? DARK_PAGE : LIGHT_PAGE);
    }

    /** Applies the current theme to a node tree (dialogs hosted in another scene). */
    public static void applyTo(Parent root) {
        Objects.requireNonNull(root, "root");
        if (dark) {
            if (!root.getStyleClass().contains(DARK_CLASS)) {
                root.getStyleClass().add(DARK_CLASS);
            }
        } else {
            root.getStyleClass().remove(DARK_CLASS);
        }
    }

    /** Flips the theme, remembers it and re-applies it to the scene. */
    public static boolean toggle(Scene scene) {
        dark = !dark;
        PREFS.putBoolean(KEY_DARK, dark);
        applyTo(scene);
        return dark;
    }
}
