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
 * Guards against dead animation CSS.
 *
 * <p>JavaFX CSS has no {@code -fx-transition}, {@code -fx-animation} or
 * {@code @keyframes}. The stylesheets used to declare all three, so the KPI
 * card "fade-in" and every hover transition looked like they were defined but
 * never ran - the app felt static while the sheet claimed otherwise. Real motion
 * now lives in {@code UiMotion}; this test fails the build if a stylesheet
 * reintroduces a property the engine silently ignores.
 */
class StyleMotionDisciplineTest {

    private static final Path CSS_ROOT = Path.of("src", "main", "resources");

    /** CSS for the WebView/PDF renderer is a real browser engine and may animate. */
    private static final List<String> ALLOWED = List.of("templates/reports/");

    private static final Pattern DEAD_ANIMATION = Pattern.compile(
            "-fx-transition\\b|-fx-animation\\b|@keyframes\\b");

    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    @Test
    void javaFxStylesheetsDoNotDeclareAnimationPropertiesTheEngineIgnores() throws IOException {
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

                // Comments may name the dead properties; only real declarations count.
                String withoutComments = BLOCK_COMMENT.matcher(Files.readString(css)).replaceAll("");
                Matcher matcher = DEAD_ANIMATION.matcher(withoutComments);
                if (matcher.find()) {
                    offenders.add(rel + " -> " + matcher.group());
                }
            }
        }

        assertTrue(checked > 0, "no stylesheets found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "JavaFX CSS ignores these properties, so the animation they describe"
                        + " never runs. Move the motion to UiMotion instead:\n  "
                        + String.join("\n  ", offenders));
    }
}
