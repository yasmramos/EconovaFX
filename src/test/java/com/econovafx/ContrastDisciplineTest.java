package com.econovafx;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the contrast of the design system.
 *
 * <p>Colour discipline ({@link StyleTokenDisciplineTest}) keeps the literals in
 * one file, but it says nothing about whether those literals are readable. A
 * hue that looks fine on a swatch can be 3:1 as 11px badge text, and the focus
 * ring is the one indicator a keyboard user depends on - if it drops below 3:1
 * against its surface, focus becomes invisible. Both had drifted.
 *
 * <p>This test reads the tokens straight from {@code theme-tokens.css},
 * resolves the light block and the {@code .root.dark} overrides, and checks the
 * pairs the UI actually puts together. Text pairs must clear 4.5:1 (WCAG 2.1 AA
 * for normal text); the focus ring must clear 3:1 (WCAG 1.4.11, non-text).
 */
class ContrastDisciplineTest {

    private static final Path TOKENS =
            Path.of("src", "main", "resources", "css", "theme-tokens.css");

    private static final Pattern LIGHT_BLOCK = Pattern.compile("\\.root\\s*\\{(.*?)}", Pattern.DOTALL);
    private static final Pattern DARK_BLOCK = Pattern.compile("\\.root\\.dark\\s*\\{(.*?)}", Pattern.DOTALL);
    private static final Pattern DECLARATION =
            Pattern.compile("(-[a-z0-9-]+)\\s*:\\s*([^;]+);");
    private static final Pattern COLOUR =
            Pattern.compile("#([0-9a-fA-F]{6})|rgba?\\(([^)]*)\\)");
    private static final Pattern COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    private static final double TEXT_MIN = 4.5;
    private static final double RING_MIN = 3.0;

    /** A foreground/background pairing the UI actually renders. */
    private record Pair(String label, String fg, String bg, double min) {
        static Pair text(String fg, String bg) {
            return new Pair(fg + " on " + bg, fg, bg, TEXT_MIN);
        }

        static Pair ring(String fg, String bg) {
            return new Pair(fg + " on " + bg, fg, bg, RING_MIN);
        }

        static Pair graphic(String fg, String bg) {
            return new Pair(fg + " on " + bg, fg, bg, RING_MIN);
        }
    }

    private static final List<Pair> PAIRS = List.of(
            // Body and helper text on the surfaces it sits on.
            Pair.text("-foreground", "-card"),
            Pair.text("-foreground", "-background"),
            Pair.text("-foreground", "-page"),
            Pair.text("-muted-foreground", "-card"),
            Pair.text("-muted-foreground", "-page"),
            Pair.text("-muted-foreground", "-muted"),   // table headers, neutral badge
            Pair.text("-fg-body", "-card"),
            Pair.text("-fg-subtle", "-card"),
            Pair.text("-fg-faint", "-card"),            // text-slate-400
            Pair.text("-fg-faint", "-page"),
            Pair.text("-secondary-foreground", "-secondary"),
            Pair.text("-accent-foreground", "-accent"),

            // Primary action: inverted foreground on the near-black fill.
            Pair.text("-primary-foreground", "-primary"),

            // Status hue as text - on a card and on its own soft chip.
            Pair.text("-success", "-card"),
            Pair.text("-success", "-success-soft"),
            Pair.text("-warning", "-card"),
            Pair.text("-warning", "-warning-soft"),
            Pair.text("-info", "-card"),
            Pair.text("-info", "-info-soft"),
            Pair.text("-danger", "-card"),
            Pair.text("-danger", "-danger-soft"),
            Pair.text("-destructive", "-card"),
            Pair.text("-destructive", "-danger-soft"),
            Pair.text("-purple", "-card"),
            Pair.text("-purple", "-purple-soft"),

            // The hover step of a soft chip must stay readable too.
            Pair.text("-success", "-success-soft-hover"),
            Pair.text("-warning", "-warning-soft-hover"),
            Pair.text("-info", "-info-soft-hover"),
            Pair.text("-danger", "-danger-soft-hover"),

            // Brand ink sits on the brand badge tint.
            Pair.text("-brand-ink", "-brand-tint"),

            // Status hue as a solid fill, under its own foreground.
            Pair.text("-success-foreground", "-success"),
            Pair.text("-warning-foreground", "-warning"),
            Pair.text("-info-foreground", "-info"),
            Pair.text("-destructive-fore", "-destructive"),

            // Focus ring: an indicator, so 3:1 rather than 4.5:1. It is only
            // ever drawn on a neutral surface - solid fills get an outer glow.
            Pair.ring("-ring", "-card"),
            Pair.ring("-ring", "-page"),
            Pair.ring("-ring", "-muted"),

            // The navigation rail is -zinc-950 in both themes, so its text must
            // come from the raw zinc scale rather than a semantic token (which
            // would resolve to a value tuned for a white page in light mode).
            Pair.text("-zinc-400", "-zinc-950"),   // nav label, section title
            Pair.text("-zinc-100", "-zinc-900"),   // nav label on hover
            Pair.text("-zinc-0", "-zinc-800"),     // nav label on the active row

            // The chart palette does not flip with the theme, so every series
            // hue must clear the 3:1 non-text target against BOTH card surfaces:
            // white in light, zinc-900 in dark. A series mark is a graphical
            // object (WCAG 1.4.11), not text.
            Pair.graphic("-series-1", "-card"),
            Pair.graphic("-series-2", "-card"),
            Pair.graphic("-series-3", "-card"),
            Pair.graphic("-series-4", "-card"),
            Pair.graphic("-series-5", "-card")
    );

    @Test
    void tokensMeetTheirContrastTarget() throws IOException {
        String text = COMMENT.matcher(Files.readString(TOKENS)).replaceAll("");
        Map<String, String> light = declarations(block(LIGHT_BLOCK, text));
        Map<String, String> dark = new LinkedHashMap<>(light);
        dark.putAll(declarations(block(DARK_BLOCK, text)));

        assertFalse(light.isEmpty(), "the light token block did not parse");
        assertFalse(dark.equals(light),
                "the dark block must override some tokens; the parse looks broken");

        List<String> offenders = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> theme :
                Map.of("light", light, "dark", dark).entrySet()) {
            for (Pair pair : PAIRS) {
                double ratio = contrast(pair, theme.getValue());
                if (ratio < pair.min()) {
                    offenders.add(String.format(
                            "%-5s %.2f:1 (needs %.1f) %s",
                            theme.getKey(), ratio, pair.min(), pair.label()));
                }
            }
        }

        assertTrue(offenders.isEmpty(),
                "these token pairs fall below their WCAG contrast target:\n  "
                        + String.join("\n  ", offenders));
    }

    private static String block(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        assertTrue(matcher.find(), "could not find block " + pattern);
        return matcher.group(1);
    }

    private static Map<String, String> declarations(String block) {
        Map<String, String> values = new LinkedHashMap<>();
        Matcher matcher = DECLARATION.matcher(block);
        while (matcher.find()) {
            values.put(matcher.group(1), matcher.group(2).strip());
        }
        return values;
    }

    /** WCAG relative-luminance contrast; the foreground is composited if it is translucent. */
    private static double contrast(Pair pair, Map<String, String> tokens) {
        double[] bg = resolve(pair.bg(), tokens, null);
        double[] fg = resolve(pair.fg(), tokens, bg);
        double light = luminance(fg);
        double dark = luminance(bg);
        double hi = Math.max(light, dark);
        double lo = Math.min(light, dark);
        return (hi + 0.05) / (lo + 0.05);
    }

    private static double[] resolve(String token, Map<String, String> tokens, double[] over) {
        String value = tokens.get(token);
        assertTrue(value != null, "token " + token + " is not declared in theme-tokens.css");
        Matcher matcher = COLOUR.matcher(value);
        assertTrue(matcher.find(), "token " + token + " is not a colour: " + value);
        if (matcher.group(1) != null) {
            String hex = matcher.group(1);
            return new double[]{
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16),
                    1.0};
        }
        String[] parts = matcher.group(2).split(",");
        double[] rgb = {
                Double.parseDouble(parts[0].strip()),
                Double.parseDouble(parts[1].strip()),
                Double.parseDouble(parts[2].strip()),
                parts.length > 3 ? Double.parseDouble(parts[3].strip()) : 1.0};
        if (rgb[3] < 1.0) {
            assertTrue(over != null, "cannot flatten " + token + " without a background");
            for (int i = 0; i < 3; i++) {
                rgb[i] = rgb[i] * rgb[3] + over[i] * (1 - rgb[3]);
            }
            rgb[3] = 1.0;
        }
        return rgb;
    }

    private static double luminance(double[] rgb) {
        double r = channel(rgb[0]);
        double g = channel(rgb[1]);
        double b = channel(rgb[2]);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double channel(double value) {
        double c = value / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
