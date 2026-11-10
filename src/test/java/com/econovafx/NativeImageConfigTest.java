package com.econovafx;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the GluonFX reflection and resource configuration in {@code pom.xml}.
 *
 * <p>GluonFX auto-registers only {@code mainClass} (here
 * {@code com.econovafx.Launcher}). Everything the application instantiates
 * reflectively - the {@code Application} subclass and every FXML controller named
 * by {@code fx:controller} - has to be listed by hand. A missing entry does not
 * fail the build: the native image links fine and then the process dies at
 * startup. On Windows the result is a windowed {@code .exe} with no console, so
 * nothing is printed and the app simply does not open.
 *
 * <p>This test reads the configuration from the same files the build uses, so it
 * tracks the real sources and cannot drift. The companion guard on the runtime
 * side is {@link Launcher}, which writes a crash log when startup throws.
 */
class NativeImageConfigTest {

    private static final Path POM = Path.of("pom.xml");
    private static final Path FXML_ROOT = Path.of("src", "main", "resources");
    private static final Path JAVA_ROOT = Path.of("src", "main", "java");

    private static final Pattern REFLECTION_LIST =
            Pattern.compile("<reflectionList>(.*?)</reflectionList>", Pattern.DOTALL);
    private static final Pattern REFLECTION_ENTRY =
            Pattern.compile("<list>\\s*([^<\\s]+)\\s*</list>");
    private static final Pattern BUNDLES_LIST =
            Pattern.compile("<bundlesList>(.*?)</bundlesList>", Pattern.DOTALL);
    private static final Pattern BUNDLE_ENTRY =
            Pattern.compile("<list>\\s*([^<\\s]+)\\s*</list>");
    private static final Pattern FXML_CONTROLLER =
            Pattern.compile("fx:controller=\"([^\"]+)\"");
    private static final Pattern ENTITY_ANNOTATION = Pattern.compile("@Entity\\b");
    private static final Pattern PACKAGE =
            Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern TYPE_DECL =
            Pattern.compile("(?:public\\s+)?(?:abstract\\s+)?class\\s+(\\w+)");
    private static final Pattern BUNDLE_NAME_CONSTANT =
            Pattern.compile("BUNDLE_NAME\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern GET_BUNDLE_CALL =
            Pattern.compile("ResourceBundle\\.getBundle\\(\\s*\"([^\"]+)\"");

    @Test
    void applicationSubclassIsRegisteredForReflection() throws IOException {
        Set<String> registered = reflectionEntries();

        assertTrue(registered.contains("com.econovafx.App"),
                "com.econovafx.App (the Application subclass) must be in the GluonFX"
                        + " <reflectionList>. LauncherImpl builds it with"
                        + " getConstructor().newInstance(); without the entry the native"
                        + " image fails before the first window and shows nothing.");
    }

    @Test
    void everyFxmlControllerIsRegisteredForReflection() throws IOException {
        Set<String> registered = reflectionEntries();
        assertFalse(registered.isEmpty(), "no reflection entries found; the pom scan is broken");

        List<String> offenders = new ArrayList<>();
        int checked = 0;

        try (Stream<Path> files = Files.walk(FXML_ROOT)) {
            for (Path fxml : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".fxml"))
                    .toList()) {
                String source = Files.readString(fxml, StandardCharsets.UTF_8);
                Matcher matcher = FXML_CONTROLLER.matcher(source);
                while (matcher.find()) {
                    String controller = matcher.group(1);
                    checked++;
                    if (!registered.contains(controller)) {
                        offenders.add(relative(fxml) + " -> " + controller);
                    }
                }
            }
        }

        assertTrue(checked > 0, "no fx:controller found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these FXML controllers are not in the GluonFX <reflectionList>, so"
                        + " FXMLLoader cannot instantiate them in the native image and the"
                        + " app dies at startup:\n  " + String.join("\n  ", offenders));
    }

    @Test
    void everyEntityIsRegisteredForReflection() throws IOException {
        Set<String> registered = reflectionEntries();
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        try (Stream<Path> files = Files.walk(JAVA_ROOT)) {
            for (Path java : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> p.getParent().getFileName().toString().equals("model"))
                    .toList()) {
                String source = Files.readString(java, StandardCharsets.UTF_8);
                if (!ENTITY_ANNOTATION.matcher(source).find()) {
                    continue;
                }
                Matcher pkg = PACKAGE.matcher(source);
                Matcher type = TYPE_DECL.matcher(source);
                if (!pkg.find() || !type.find()) {
                    continue;
                }
                String fqcn = pkg.group(1) + "." + type.group(1);
                checked++;
                if (!registered.contains(fqcn)) {
                    offenders.add(fqcn);
                }
            }
        }

        assertTrue(checked > 0, "no @Entity classes found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these @Entity classes are not in the GluonFX <reflectionList>; Ebean"
                        + " reads their generated _ebean_props field reflectively:\n  "
                        + String.join("\n  ", offenders));
    }

    @Test
    void resourceBundleBaseNameMatchesTheOneI18nManagerLoads() throws IOException {
        String bundleName = i18nBundleName();
        List<String> bundles = sectionEntries(BUNDLES_LIST, BUNDLE_ENTRY);

        assertTrue(bundles.contains(bundleName),
                "the GluonFX <bundlesList> must contain \"" + bundleName + "\" (the base"
                        + " name I18nManager passes to ResourceBundle.getBundle). A name"
                        + " that matches no file on the classpath leaves the bundle out of"
                        + " the image and getBundle throws at startup. Found: " + bundles);
    }

    @Test
    void noCodeLoadsTheBundleUnderADifferentName() throws IOException {
        String bundleName = i18nBundleName();
        List<String> offenders = new ArrayList<>();

        try (Stream<Path> files = Files.walk(JAVA_ROOT)) {
            for (Path java : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList()) {
                Matcher matcher = GET_BUNDLE_CALL.matcher(
                        Files.readString(java, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    String name = matcher.group(1);
                    if (!name.equals(bundleName)) {
                        offenders.add(relative(java) + " -> \"" + name + "\"");
                    }
                }
            }
        }

        assertTrue(offenders.isEmpty(),
                "these call sites load the resource bundle under a name other than \""
                        + bundleName + "\". ResourceBundle treats \"/\" and \".\" as"
                        + " equivalent on the JVM, but the native image registers the bundle"
                        + " under one name only, so the other throws at runtime:\n  "
                        + String.join("\n  ", offenders));
    }

    private static Set<String> reflectionEntries() throws IOException {
        return new LinkedHashSet<>(sectionEntries(REFLECTION_LIST, REFLECTION_ENTRY));
    }

    private static List<String> sectionEntries(Pattern section, Pattern entry) throws IOException {
        Matcher block = section.matcher(Files.readString(POM, StandardCharsets.UTF_8));
        List<String> entries = new ArrayList<>();
        while (block.find()) {
            Matcher item = entry.matcher(block.group(1));
            while (item.find()) {
                entries.add(item.group(1));
            }
        }
        return entries;
    }

    /** Reads the base name from the constant so the guard follows the real code. */
    private static String i18nBundleName() throws IOException {
        Path manager = JAVA_ROOT.resolve(Path.of("com", "econovafx", "core", "i18n", "I18nManager.java"));
        Matcher matcher = BUNDLE_NAME_CONSTANT.matcher(Files.readString(manager, StandardCharsets.UTF_8));
        assertTrue(matcher.find(), "I18nManager.BUNDLE_NAME constant not found; the scan is broken");
        return matcher.group(1);
    }

    private static String relative(Path path) {
        return path.toString().replace('\\', '/');
    }
}
