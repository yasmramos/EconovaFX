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
 * Guards the corner radius scale of the design system.
 *
 * <p>The scale is 0 / 4 / 6 / 8 / 10 / 12 / 9999, the steps {@code theme-tokens.css}
 * documents in its GEOMETRY REFERENCE. 4 is the nested radius a control takes
 * inside a 8px popover with 4px padding; 12 is the large surface (dashboard hero,
 * login card); 9999 is a pill/circle.
 *
 * <p>Left unchecked, the radius had drifted to one-off values - a 3px slider
 * track beside 4px controls, a 5px menu item beside a 4px nested control, a 16px
 * login card that was neither the 12px large surface nor the 8px card. Those
 * corners do not line up, and the drift is invisible until two shapes sit side by
 * side. This test fails the build on any {@code -fx-background-radius} or
 * {@code -fx-border-radius} outside the scale, including each value of a
 * per-corner radius such as {@code 8 8 0 0}.
 *
 * <p>{@code theme-tokens.css} is out of scope: that is where the scale is
 * declared. Comments are blanked before the scan.
 */
class RadiusDisciplineTest {

    private static final Path CSS_ROOT = Path.of("src", "main", "resources");

    private static final List<String> ALLOWED = List.of(
            "css/theme-tokens.css",
            "templates/reports/"
    );

    /** The published radius scale, in px. */
    private static final Set<String> SCALE = Set.of("0", "4", "6", "8", "10", "12", "9999");

    private static final Pattern RADIUS =
            Pattern.compile("-fx-(?:background|border)-radius:\\s*([0-9.]+(?:\\s+[0-9.]+)*)\\s*;");

    @Test
    void everyRadiusIsAStepOfTheRadiusScale() throws IOException {
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
                    Matcher matcher = RADIUS.matcher(stripped.get(i));
                    while (matcher.find()) {
                        for (String token : matcher.group(1).split("\\s+")) {
                            if (!SCALE.contains(token)) {
                                offenders.add(rel + ":" + (i + 1) + " -> " + raw.get(i).strip());
                                break;
                            }
                        }
                    }
                }
            }
        }

        assertTrue(checked > 0, "no stylesheets found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these stylesheets use a radius that is not a step of the radius scale"
                        + " (" + SCALE + "); snap it to the nearest step so the corners"
                        + " line up:\n  "
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
