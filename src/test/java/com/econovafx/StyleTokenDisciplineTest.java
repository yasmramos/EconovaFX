package com.econovafx;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the colour discipline of the design system.
 *
 * <p>Every colour must be a token declared in {@code theme-tokens.css}. A raw
 * hex or {@code rgba(...)} literal in any other JavaFX stylesheet evades the
 * theme, so it stops flipping in dark mode and it drifts away from the palette.
 * That is exactly how the login, splash and dashboard sheets had accumulated
 * their own private blues and greens.
 *
 * <p>Two kinds of file are intentionally out of scope:
 *
 * <ul>
 *   <li>{@code theme-tokens.css} - that is where the literals are meant to live.</li>
 *   <li>{@code templates/reports/*} - plain HTML/CSS for the WebView and
 *       OpenHTMLToPDF, a different rendering engine with its own print palette.</li>
 * </ul>
 *
 * <p>The same rule is applied to FXML. An {@code iconColor="#..."} attribute is
 * an inline property, so it beats every stylesheet and freezes that icon on the
 * light palette. The sheets cannot fix it - the markup has to name a token class
 * instead (see the ICONS section of {@code controls.css}).
 */
class StyleTokenDisciplineTest {

    private static final Path CSS_ROOT = Path.of("src", "main", "resources");

    /** Where raw colours are allowed to live. */
    private static final List<String> ALLOWED = List.of(
            "css/theme-tokens.css",
            "templates/reports/"
    );

    private static final Pattern RAW_COLOUR = Pattern.compile(
            "#[0-9a-fA-F]{3,8}\\b|rgba?\\s*\\(");

    @Test
    void javafxStylesheetsUseTokensNotRawColours() throws IOException {
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        try (Stream<Path> files = Files.walk(CSS_ROOT)) {
            for (Path css : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".css"))
                    .toList()) {
                String rel = relative(css);
                if (ALLOWED.stream().anyMatch(rel::contains)) {
                    continue;
                }
                checked++;

                List<String> lines = Files.readAllLines(css);
                for (int i = 0; i < lines.size(); i++) {
                    Matcher matcher = RAW_COLOUR.matcher(lines.get(i));
                    if (matcher.find()) {
                        offenders.add(rel + ":" + (i + 1) + " -> " + lines.get(i).strip());
                    }
                }
            }
        }

        assertTrue(checked > 0, "no stylesheets found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these stylesheets hard-code a colour instead of using a token from"
                        + " theme-tokens.css, so they will not follow the theme:\n  "
                        + String.join("\n  ", offenders));
    }

    @Test
    void fxmlUsesTokenClassesNotRawColourAttributes() throws IOException {
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        try (Stream<Path> files = Files.walk(CSS_ROOT)) {
            for (Path fxml : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".fxml"))
                    .toList()) {
                checked++;
                List<String> lines = Files.readAllLines(fxml);
                for (int i = 0; i < lines.size(); i++) {
                    Matcher matcher = RAW_COLOUR.matcher(lines.get(i));
                    if (matcher.find()) {
                        offenders.add(relative(fxml) + ":" + (i + 1) + " -> " + lines.get(i).strip());
                    }
                }
            }
        }

        assertTrue(checked > 0, "no FXML found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these FXML files set a raw colour (iconColor=\"#...\") that beats the"
                        + " stylesheets and will not flip in dark mode; use a token class"
                        + " such as .icon-success or .icon-brand instead:\n  "
                        + String.join("\n  ", offenders));
    }

    private static String relative(Path path) {
        return path.toString().replace('\\', '/');
    }
}
