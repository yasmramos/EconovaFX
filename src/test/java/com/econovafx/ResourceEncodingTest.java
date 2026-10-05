package com.econovafx;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the source encoding of the text resources.
 *
 * <p>Editing these files with a tool that assumes the platform code page instead
 * of UTF-8 (PowerShell's {@code Get-Content}/{@code Set-Content} without an
 * explicit encoding, for instance) rewrites the bytes of every accented
 * character, so "Distribución" comes back as "DistribuciÃ³n". The file stays
 * valid UTF-8, which is why it survives a compiler and only shows up on screen.
 */
class ResourceEncodingTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");

    /**
     * Lead bytes of a character that only appears when UTF-8 was decoded as
     * Latin-1 and encoded again: Ã (C3 83), Â (C2 82), â (E2 80).
     */
    private static final Pattern MOJIBAKE = Pattern.compile(
            "[\\u00C3\\u00C2][\\u0080-\\u00BF]"
            + "|[\\u00E2][\\u20AC\\u0080-\\u00BF]"
            + "|[\\u00EF\\u00BF\\u00BD]");

    private static final List<String> TEXT_SUFFIXES =
            List.of(".fxml", ".css", ".properties", ".json", ".html", ".xml", ".sql", ".csv");

    @Test
    void resourcesAreStoredAsPlainUtf8() throws IOException {
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        for (Path file : textResources()) {
            byte[] bytes = Files.readAllBytes(file);
            String text;
            try {
                text = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            } catch (CharacterCodingException notUtf8) {
                offenders.add(relative(file) + ": not valid UTF-8");
                continue;
            }
            checked++;

            Matcher matcher = MOJIBAKE.matcher(text);
            int hits = 0;
            int firstLine = -1;
            while (matcher.find()) {
                hits++;
                if (firstLine < 0) {
                    firstLine = (int) text.substring(0, matcher.start()).lines().count();
                }
            }
            if (hits > 0) {
                offenders.add(relative(file) + ": " + hits
                        + " double-encoded character(s), first at line " + firstLine
                        + " -> " + text.lines().toList().get(firstLine - 1).strip());
            }
        }

        assertTrue(checked > 0, "no text resources found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these files were saved with the wrong code page, so accents render"
                        + " as mojibake. Rewrite them as UTF-8:\n  "
                        + String.join("\n  ", offenders));
    }

    /** Every FXML must still be well-formed XML. */
    @Test
    void fxmlIsWellFormed() throws Exception {
        List<String> broken = new ArrayList<>();
        int checked = 0;

        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try (Stream<Path> files = Files.walk(RESOURCE_ROOT)) {
            for (Path fxml : files.filter(p -> p.toString().endsWith(".fxml")).toList()) {
                checked++;
                try {
                    factory.newDocumentBuilder().parse(fxml.toFile());
                } catch (Exception e) {
                    broken.add(relative(fxml) + ": " + e.getMessage());
                }
            }
        }

        assertTrue(checked > 0, "no FXML found; the scan itself is broken");
        assertTrue(broken.isEmpty(),
                "malformed FXML:\n  " + String.join("\n  ", broken));
    }

    private static List<Path> textResources() throws IOException {
        try (Stream<Path> files = Files.walk(RESOURCE_ROOT)) {
            return files.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return TEXT_SUFFIXES.stream().anyMatch(name::endsWith);
                    })
                    .toList();
        }
    }

    private static String relative(Path path) {
        return path.toString().replace('\\', '/');
    }
}