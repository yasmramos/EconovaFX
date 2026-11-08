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

    /**
     * The status palette has semantic twins that flip with the theme
     * ({@code -blue-500} -> {@code -info}, {@code -red-50} -> {@code -danger-soft}).
     * A sheet that reaches for the raw ramp entry instead freezes that colour on
     * the light palette. Neutrals ({@code -zinc-*}) are deliberately exempt: the
     * sidebar rail is zinc by design and stays dark in both themes.
     */
    private static final Pattern RAW_STATUS_PALETTE = Pattern.compile(
            "-(?:blue|green|red|orange|amber|emerald|yellow|purple)-\\d+\\b");

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

    @Test
    void stylesheetsUseSemanticStatusTokensNotTheRawRamp() throws IOException {
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
                    String line = lines.get(i);
                    // Only the declaration is inspected: a class name such as
                    // ".bg-blue-500" legitimately contains the ramp spelling.
                    int colon = line.indexOf(':');
                    String declaration = colon >= 0 ? line.substring(colon + 1) : "";
                    Matcher matcher = RAW_STATUS_PALETTE.matcher(declaration);
                    if (matcher.find()) {
                        offenders.add(rel + ":" + (i + 1) + " -> " + line.strip());
                    }
                }
            }
        }

        assertTrue(checked > 0, "no stylesheets found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these stylesheets reach for a raw status-ramp entry instead of its"
                        + " semantic token, so the colour will not flip in dark mode; use"
                        + " -info/-info-soft, -success/-success-soft, -warning/-warning-soft,"
                        + " -danger/-danger-soft or -purple/-purple-soft:\n  "
                        + String.join("\n  ", offenders));
    }

    @Test
    void glyphsAreColouredByIconClassesNotTextUtilities() throws IOException {
        java.util.Set<String> textColourUtilities = textColourUtilities();
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        try (Stream<Path> files = Files.walk(CSS_ROOT)) {
            for (Path fxml : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".fxml"))
                    .toList()) {
                String source = Files.readString(fxml);
                for (Glyph glyph : Glyph.parse(source)) {
                    checked++;
                    // A FontIcon is a Text node and an SVGPath is a Shape: both
                    // paint from -fx-fill. A .text-* colour utility only sets
                    // -fx-text-fill, so it leaves the glyph on the default black
                    // - invisible on dark surfaces. Colour glyphs with an
                    // .icon-* or .fill-* class instead.
                    if (hasTextColourUtility(glyph.styleClass(), textColourUtilities)) {
                        offenders.add(relative(fxml) + " line " + glyph.line()
                                + ": " + glyph.oneLine());
                    }
                }
            }
        }

        assertTrue(checked > 0, "no glyphs found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these FontIcon/SVGPath nodes carry a text-* colour class, which cannot"
                        + " paint a glyph (-fx-text-fill does not apply to Text or Shape);"
                        + " use an .icon-* or .fill-* class:\n  "
                        + String.join("\n  ", offenders));
    }

    /** A FontIcon or SVGPath open tag, whose styleClass may sit on a later line. */
    private record Glyph(String styleClass, String oneLine, int line) {

        private static final Pattern TAG = Pattern.compile("<(FontIcon|SVGPath)\\b[^>]*>");
        private static final Pattern CLASS_ATTR = Pattern.compile("styleClass=\"([^\"]*)\"");

        static List<Glyph> parse(String source) {
            List<Glyph> glyphs = new ArrayList<>();
            Matcher tag = TAG.matcher(source);
            while (tag.find()) {
                String body = tag.group();
                Matcher classAttr = CLASS_ATTR.matcher(body);
                String styleClass = classAttr.find() ? classAttr.group(1) : "";
                glyphs.add(new Glyph(styleClass, body.strip(),
                        (int) source.substring(0, tag.start()).lines().count()));
            }
            return glyphs;
        }
    }

    /**
     * The {@code .text-*} utilities that actually set {@code -fx-text-fill},
     * read from the sheets themselves. A type-scale class such as
     * {@code .text-sm} sets {@code -fx-font-size} instead and is harmless on a
     * glyph, so it must not be flagged; a colour class silently fails.
     */
    private static java.util.Set<String> textColourUtilities() throws IOException {
        java.util.Set<String> names = new java.util.HashSet<>();
        try (Stream<Path> files = Files.walk(CSS_ROOT)) {
            for (Path css : files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".css"))
                    .toList()) {
                if (ALLOWED.stream().anyMatch(relative(css)::contains)) {
                    continue;
                }
                String sheet = Files.readString(css).replaceAll("/\\*.*?\\*/", " ");
                Matcher rule = CSS_RULE.matcher(sheet);
                while (rule.find()) {
                    if (!rule.group(2).contains("-fx-text-fill")) {
                        continue;
                    }
                    Matcher cls = CLASS_IN_SELECTOR.matcher(rule.group(1));
                    while (cls.find()) {
                        if (cls.group(1).startsWith("text-")) {
                            names.add(cls.group(1));
                        }
                    }
                }
            }
        }
        assertTrue(!names.isEmpty(), "no .text-* colour utilities found; the scan is broken");
        return names;
    }

    private static boolean hasTextColourUtility(String styleClass, java.util.Set<String> names) {
        for (String token : styleClass.split("[,\\s]+")) {
            if (names.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static final Pattern CSS_RULE = Pattern.compile("([^{}]+)\\{([^}]*)}");
    private static final Pattern CLASS_IN_SELECTOR = Pattern.compile("\\.([a-z][a-z0-9-]*)");

    private static String relative(Path path) {
        return path.toString().replace('\\', '/');
    }
}
