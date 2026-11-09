package com.econovafx.modules.core.ui.util;

import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * The {@code :error} and {@code :warning} pseudo-classes the sheets use for an
 * inline validation state.
 *
 * <p>JavaFX registers neither: they are not in Modena and no control exposes
 * them, so a rule such as {@code .text-field:error} matches nothing until the
 * state is turned on through {@link PseudoClass#getPseudoClass} - the only way
 * to activate a pseudo-class. A sheet that styles a state no code ever sets is
 * inert, the same way a {@code -fx-transition} declaration was.
 *
 * <p>{@code PseudoClassDisciplineTest} keeps the sheets and this class in step:
 * a pseudo-class that is neither a JavaFX built-in nor registered here fails the
 * build.
 */
public final class ErrorState {

    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");
    private static final PseudoClass WARNING = PseudoClass.getPseudoClass("warning");

    private ErrorState() {
    }

    /** Turns the {@code :error} pseudo-class on {@code node} on or off. */
    public static void setError(Node node, boolean error) {
        node.pseudoClassStateChanged(ERROR, error);
    }

    /** Turns the {@code :warning} pseudo-class on {@code node} on or off. */
    public static void setWarning(Node node, boolean warning) {
        node.pseudoClassStateChanged(WARNING, warning);
    }
}
