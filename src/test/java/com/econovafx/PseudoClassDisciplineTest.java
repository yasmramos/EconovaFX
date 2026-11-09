package com.econovafx;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards against dead pseudo-classes in the JavaFX stylesheets.
 *
 * <p>A selector such as {@code .text-field:error} only matches once something
 * turns that pseudo-class on through {@link javafx.css.PseudoClass}. JavaFX ships
 * a fixed set of them (Modena uses no {@code :error} or {@code :warning}); the
 * sheets used both, and a comment claimed the controllers set them, but no
 * {@code PseudoClass} existed in the code. Those rules were inert - the same
 * class of bug as a {@code -fx-transition} declaration the engine ignores.
 *
 * <p>{@code :error} and {@code :warning} are now registered in
 * {@code ErrorState} and toggled by the controllers. This test keeps the two
 * sides in step: every pseudo-class in a sheet must be a JavaFX built-in or one
 * the code registers, so a future sheet cannot invent a state nothing sets.
 */
class PseudoClassDisciplineTest {

    private static final Path CSS_ROOT = Path.of("src", "main", "resources");

    /** CSS for the WebView/PDF renderer is a real browser engine with its own set. */
    private static final List<String> ALLOWED = List.of("templates/reports/");

    /**
     * The pseudo-classes JavaFX defines, read from {@code modena.css} and the
     * control classes that register them. A sheet may use any of these freely.
     */
    private static final Set<String> JAVA_FX_BUILT_IN = Set.of(
            "armed", "bottom", "cancel", "cell-selection", "checked", "collapsed",
            "constrained-resize", "contains-focus", "default", "dir", "disabled",
            "editable", "even", "expanded", "filled", "focused", "floating",
            "header", "horizontal", "hover", "indeterminate", "last-visible",
            "left", "no-header", "odd", "openvertically", "pressed", "right",
            "row-selection", "selected", "show-mnemonics", "showing", "top",
            "vertical", "visited");

    /** Java source that registers the project's own pseudo-classes. */
    private static final Path SRC_ROOT = Path.of("src", "main", "java");

    private static final Pattern PSEUDO_CLASS_REGISTRATION =
            Pattern.compile("getPseudoClass\\(\\s*\"([a-z-]+)\"");

    private static final Pattern PSEUDO_IN_SELECTOR =
            Pattern.compile(":([a-z][a-z-]*)");

    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    @Test
    void everyPseudoClassInASheetIsRegisteredOrBuiltIn() throws IOException {
        Set<String> registered = registeredInCode();
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        try (Stream<Path> files = Files.walk(CSS_ROOT)) {
            for (Path css : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".css"))
                    .toList()) {
                String rel = css.toString().replace('\\', '/');
                if (ALLOWED.stream().anyMatch(rel::contains)) {
                    continue;
                }
                checked++;

                // Comments name pseudo-classes; only selectors count. Blank the
                // comments first, then keep the text before each rule's brace so
                // declarations (e.g. a url with a colon) cannot match.
                String stripped = BLOCK_COMMENT.matcher(Files.readString(css)).replaceAll("");
                for (String selector : selectors(stripped)) {
                    Matcher matcher = PSEUDO_IN_SELECTOR.matcher(selector);
                    while (matcher.find()) {
                        String pseudo = matcher.group(1);
                        if (!JAVA_FX_BUILT_IN.contains(pseudo) && !registered.contains(pseudo)) {
                            offenders.add(rel + " -> :" + pseudo);
                        }
                    }
                }
            }
        }

        assertTrue(checked > 0, "no stylesheets found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these pseudo-classes are neither built into JavaFX nor registered through"
                        + " PseudoClass, so the rules that use them match nothing. Register"
                        + " them in code (see ErrorState) or drop the rule:\n  "
                        + String.join("\n  ", offenders));
    }

    /** Collects the pseudo-classes the project registers via {@code PseudoClass}. */
    private static Set<String> registeredInCode() throws IOException {
        Set<String> names = new LinkedHashSet<>();
        try (Stream<Path> files = Files.walk(SRC_ROOT)) {
            for (Path java : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList()) {
                Matcher matcher = PSEUDO_CLASS_REGISTRATION.matcher(Files.readString(java));
                while (matcher.find()) {
                    names.add(matcher.group(1));
                }
            }
        }
        return names;
    }

    /**
     * Returns the selector text of every rule, i.e. what precedes each {@code {}}
     * once declarations have been removed. Nested braces are not used in these
     * flat sheets, so pairing every brace with the text before it is enough.
     */
    private static List<String> selectors(String css) {
        List<String> selectors = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < css.length(); i++) {
            char ch = css.charAt(i);
            if (ch == '{') {
                if (depth == 0) {
                    selectors.add(current.toString());
                }
                depth++;
                current.setLength(0);
            } else if (ch == '}') {
                depth = Math.max(0, depth - 1);
                current.setLength(0);
            } else if (depth == 0) {
                current.append(ch);
            }
        }
        return selectors;
    }
}
