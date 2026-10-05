package com.econovafx;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Button whose graphic holds a FontIcon but which does not declare
 * {@code contentDisplay="LEFT"} falls back to TEXT_ONLY: the icon is not laid
 * out beside the label and paints on top of it. This guards every FXML in the
 * project against that regression.
 */
class FxmlIconMarkupTest {

    private static final String FXML_ROOT = "src/main/resources";

    @Test
    void everyIconButtonDeclaresContentDisplay() throws IOException {
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        try (Stream<Path> files = Files.walk(Path.of(FXML_ROOT))) {
            for (Path fxml : files.filter(p -> p.toString().endsWith(".fxml")).toList()) {
                String source = Files.readString(fxml, StandardCharsets.UTF_8);
                for (ButtonTag tag : ButtonTag.parse(source)) {
                    if (!tag.body().contains("FontIcon")) {
                        continue;
                    }
                    checked++;
                    if (!tag.attributes().contains("contentDisplay")) {
                        offenders.add(FXML_ROOT + "/"
                                + fxml.getFileName()
                                + " line " + tag.line()
                                + ": " + tag.oneLine());
                    }
                }
            }
        }

        assertTrue(checked > 0, "no icon buttons found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these buttons show their icon on top of the label; add "
                        + "contentDisplay=\"LEFT\":\n  " + String.join("\n  ", offenders));
    }

    /** A single {@code <Button ...>} open tag, which may span several lines. */
    private record ButtonTag(String attributes, String body, int line) {

        static List<ButtonTag> parse(String source) {
            List<ButtonTag> tags = new ArrayList<>();
            int index = 0;
            while (true) {
                int open = source.indexOf("<Button", index);
                if (open < 0) {
                    return tags;
                }
                int close = source.indexOf('>', open);
                if (close < 0) {
                    return tags;
                }
                String attributes = source.substring(open, close);
                // Skip the handful of characters that may legally precede a
                // "Button" tag so <ButtonBar> is not matched.
                char before = open > 0 ? source.charAt(open - 1) : ' ';
                if (!Character.isWhitespace(before)) {
                    index = open + 6;
                    continue;
                }
                int bodyStart = close + 1;
                int bodyEnd = source.indexOf("</Button>", bodyStart);
                String body = bodyEnd < 0 ? "" : source.substring(bodyStart, bodyEnd);
                int line = (int) source.substring(0, open).lines().count();
                tags.add(new ButtonTag(attributes, body, line));
                index = close + 1;
            }
        }

        String oneLine() {
            return attributes.replaceAll("\\s+", " ").trim();
        }
    }
}