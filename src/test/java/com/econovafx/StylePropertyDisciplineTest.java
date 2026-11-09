package com.econovafx;

import javafx.beans.property.Property;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards against CSS declarations JavaFX silently ignores.
 *
 * <p>JavaFX parses a stylesheet permissively: a property it does not know is
 * dropped without a warning. So a rule can read as if it styles something while
 * the engine discards it. The sheets had accumulated nine of them - a bare
 * {@code -fx-height} (the real names are {@code -fx-min/pref/max-height}), the
 * layout flags {@code -fx-grow} / {@code -fx-hgrow} / {@code -fx-vgrow} (grow is
 * a layout constraint, not a CSS property), {@code -fx-margin}, the browser-only
 * {@code -fx-overflow} / {@code -fx-text-overflow} (JavaFX spells it
 * {@code -fx-text-overrun}), and {@code -fx-display} / {@code -fx-visibility}.
 * They are the same class of bug as the dead {@code -fx-transition} that
 * {@code StyleMotionDisciplineTest} already covers.
 *
 * <p>The set of names JavaFX accepts is read from the JavaFX and Ikonli jars on
 * the test classpath rather than copied by hand, so the guard tracks the real
 * engine and cannot drift.
 */
class StylePropertyDisciplineTest {

    private static final Path CSS_ROOT = Path.of("src", "main", "resources");

    /** CSS for the WebView/PDF renderer is a real browser engine with its own set. */
    private static final List<String> ALLOWED = List.of("templates/reports/");

    /**
     * Classes anchored in each styling jar so the scan reaches them all:
     * {@code javafx-base}, {@code -graphics}, {@code -controls}, {@code -fxml},
     * {@code -web} and {@code ikonli-javafx}.
     */
    private static final Class<?>[] JAR_ANCHORS = {
            Property.class, Node.class, Label.class, Slider.class,
            FXMLLoader.class, WebView.class, FontIcon.class,
    };

    private static final Pattern DECLARED_PROPERTY =
            Pattern.compile("(-fx-[a-z][a-z0-9-]*)\\s*:");

    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    @Test
    void everyDeclaredPropertyIsKnownToJavaFx() throws IOException {
        Set<String> known = knownJavaFxProperties();
        assertTrue(known.contains("-fx-background-color") && known.contains("-fx-text-fill"),
                "the JavaFX property scan found nothing; the anchors or jars changed");

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

                // A comment may name a property; only a real declaration counts.
                String withoutComments = BLOCK_COMMENT.matcher(Files.readString(css)).replaceAll("");
                Matcher matcher = DECLARED_PROPERTY.matcher(withoutComments);
                while (matcher.find()) {
                    String property = matcher.group(1);
                    if (!known.contains(property)) {
                        offenders.add(rel + " -> " + property);
                    }
                }
            }
        }

        assertTrue(checked > 0, "no stylesheets found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "JavaFX does not recognise these properties and silently drops them,"
                        + " so the rule has no effect. Use a real property or remove the"
                        + " declaration:\n  " + String.join("\n  ", offenders));
    }

    /**
     * Collects every {@code -fx-*} name the styling jars mention, whether as a
     * styleable-property string in a class file or as a declaration in a bundled
     * stylesheet such as Modena.
     */
    private static Set<String> knownJavaFxProperties() {
        Set<String> names = new LinkedHashSet<>();
        Set<String> scanned = new LinkedHashSet<>();
        for (Class<?> anchor : JAR_ANCHORS) {
            String location = codeSource(anchor);
            if (location == null || !location.startsWith("file:") || !scanned.add(location)) {
                continue;
            }
            try {
                scanJar(Path.of(URI.create(location)), names);
            } catch (IOException | RuntimeException ignored) {
                // A module read from a directory or an unexpected jar simply adds
                // nothing; the assertion on the result catches a total failure.
            }
        }
        return names;
    }

    private static String codeSource(Class<?> type) {
        try {
            var source = type.getProtectionDomain().getCodeSource();
            return source == null ? null : source.getLocation().toString();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void scanJar(Path jar, Set<String> names) throws IOException {
        if (!Files.isRegularFile(jar)) {
            return;
        }
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !(name.endsWith(".class") || name.endsWith(".css"))) {
                    continue;
                }
                byte[] data = zip.getInputStream(entry).readAllBytes();
                Matcher matcher = Pattern.compile("-fx-[a-z][a-z0-9-]*")
                        .matcher(new String(data, StandardCharsets.ISO_8859_1));
                while (matcher.find()) {
                    names.add(matcher.group());
                }
            }
        }
    }
}
