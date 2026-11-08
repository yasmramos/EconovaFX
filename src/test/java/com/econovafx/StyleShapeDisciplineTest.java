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
 * Guards the corner shape of the design system.
 *
 * <p>JavaFX paints a region's background and its border as two independent
 * shapes. A rule that rounds the background but leaves the border square draws a
 * rectangular hairline that pokes out past the rounded corners - the "corner
 * tip". Every rounded surface with a visible border must therefore declare the
 * same {@code -fx-border-radius} as its {@code -fx-background-radius}. The
 * login, selection-dialog and card sheets had each patched this by hand; this
 * test keeps the rest of the system from regressing.
 *
 * <p>Only a uniform border (one colour, or four identical sides) is checked: a
 * rule that paints a single hairline edge on purpose (for example a table
 * header's bottom rule) is not a rounded box and is left alone.
 */
class StyleShapeDisciplineTest {

    private static final Path CSS_ROOT = Path.of("src", "main", "resources");

    /** Where the literals live and the print stylesheet, both out of scope. */
    private static final List<String> ALLOWED = List.of(
            "css/theme-tokens.css",
            "templates/reports/"
    );

    private static final Pattern RULE = Pattern.compile("([^{}]+)\\{([^{}]*)}");
    private static final Pattern COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern BG_RADIUS =
            Pattern.compile("-fx-background-radius:\\s*([0-9]+)\\s*;");
    private static final Pattern BORDER_RADIUS = Pattern.compile("-fx-border-radius:");
    private static final Pattern BORDER_COLOUR =
            Pattern.compile("-fx-border-color:\\s*([^;]+);");
    private static final Pattern BORDER_WIDTH =
            Pattern.compile("-fx-border-width:\\s*([0-9.]+)\\s*;");

    @Test
    void roundedSurfacesDeclareAMatchingBorderRadius() throws IOException {
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

                String text = COMMENT.matcher(Files.readString(css)).replaceAll("");
                Matcher rule = RULE.matcher(text);
                while (rule.find()) {
                    checked++;
                    String selector = rule.group(1).strip().lines().reduce((a, b) -> b).orElse("");
                    String body = rule.group(2);

                    Matcher bg = BG_RADIUS.matcher(body);
                    Matcher bw = BORDER_WIDTH.matcher(body);
                    Matcher bc = BORDER_COLOUR.matcher(body);
                    if (!bg.find() || !bw.find() || !bc.find()) {
                        continue;
                    }
                    if (Double.parseDouble(bw.group(1)) <= 0) {
                        continue;
                    }
                    if (!isUniformBorder(bc.group(1))) {
                        continue;
                    }
                    if (BORDER_RADIUS.matcher(body).find()) {
                        continue;
                    }

                    offenders.add(rel + " -> " + selector.strip()
                            + "  (background-radius " + bg.group(1)
                            + " but no border-radius)");
                }
            }
        }

        assertTrue(checked > 0, "no CSS rules parsed; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these rules round the background but not the border, so the hairline"
                        + " draws square corners that poke out of the shape:\n  "
                        + String.join("\n  ", offenders));
    }

    /** One colour, or four sides that all name the same colour. */
    private static boolean isUniformBorder(String value) {
        String[] parts = value.strip().split("\\s+");
        if (parts.length == 1) {
            return true;
        }
        if (parts.length == 4) {
            return parts[0].equals(parts[1]) && parts[1].equals(parts[2]) && parts[2].equals(parts[3]);
        }
        return false;
    }

    private static String relative(Path path) {
        return path.toString().replace('\\', '/');
    }
}
