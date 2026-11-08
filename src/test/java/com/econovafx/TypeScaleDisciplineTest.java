package com.econovafx;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the type scale of the design system.
 *
 * <p>The scale is the one {@code utilities.css} publishes as the Tailwind-style
 * {@code .text-*} classes: 10 / 11 / 12 / 13 / 14 / 16 / 18 / 22 / 30. 13 has no
 * named class because it is the dense step the tables, form labels and dialogs
 * share; it is still a step, so it is in the set.
 *
 * <p>Nothing enforced that set, so {@code -fx-font-size} had drifted to a dozen
 * values - 15px on a dialog close glyph, 17px on one dialog title while its
 * siblings were 16px, 20px and 24px on two headings. Those are not choices, they
 * are accidents, and they read as slightly-off typography. This test fails the
 * build on any {@code -fx-font-size} outside the scale.
 *
 * <p>{@code theme-tokens.css} is out of scope: that is where the scale is
 * declared. Comments are blanked before the scan, because the sheet documents
 * the scale in prose.
 */
class TypeScaleDisciplineTest {

    private static final Path CSS_ROOT = Path.of("src", "main", "resources");

    private static final List<String> ALLOWED = List.of(
            "css/theme-tokens.css",
            "templates/reports/"
    );

    /** The published scale, in px. Every -fx-font-size must be one of these. */
    private static final Set<Integer> SCALE = Set.of(10, 11, 12, 13, 14, 16, 18, 22, 30);

    private static final Pattern FONT_SIZE =
            Pattern.compile("-fx-font-size:\\s*(\\d+(?:\\.\\d+)?)px");

    @Test
    void everyFontSizeIsAStepOfTheTypeScale() throws IOException {
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

                List<String> raw = Files.readAllLines(css);
                List<String> stripped = stripComments(String.join("\n", raw)).lines().toList();
                for (int i = 0; i < stripped.size(); i++) {
                    Matcher matcher = FONT_SIZE.matcher(stripped.get(i));
                    if (matcher.find() && !SCALE.contains((int) Double.parseDouble(matcher.group(1)))) {
                        offenders.add(rel + ":" + (i + 1) + " -> " + raw.get(i).strip());
                    }
                }
            }
        }

        assertTrue(checked > 0, "no stylesheets found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these stylesheets use a -fx-font-size that is not a step of the type"
                        + " scale (" + SCALE + "); snap it to the nearest step so the"
                        + " typography stays on one grid:\n  "
                        + String.join("\n  ", offenders));
    }

    /** Blanks CSS comments, keeping newlines so line numbers stay aligned. */
    private static String stripComments(String source) {
        return Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL)
                .matcher(source)
                .replaceAll(m -> m.group().replaceAll("[^\\n]", " "));
    }

    private static String relative(Path path) {
        return path.toString().replace('\\', '/');
    }
}
