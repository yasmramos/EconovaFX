package com.econovafx;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the page chrome of the module pages.
 *
 * <p>The shell ({@code contentArea} / {@code .workspace-content}) already paints
 * the page canvas and a 24px gutter, and every page root is dropped straight into
 * it. Pages that also set their own padding and a {@code bg-gray-50} fill used to
 * double that gutter and - because {@code bg-gray-50} resolves to {@code -muted},
 * not the page canvas - drop an opaque slab on the dark background. Their titles
 * landed at four different x offsets (40/44/48/56) for the same reason.
 *
 * <p>The rules this test enforces, so the pages stay on one baseline:
 *
 * <ul>
 *   <li>A page root carries no {@code p-*} / {@code bg-*} of its own; the shell
 *       owns the canvas and the gutter.</li>
 *   <li>A page root carries no {@code <padding>} block, for the same reason.</li>
 *   <li>Every reachable page opens with a single {@code .page-header} holding a
 *       single {@code .page-title}.</li>
 * </ul>
 */
class PageChromeDisciplineTest {

    private static final Path FXML_ROOT = Path.of("src", "main", "resources", "fxml");

    /** The pages {@code ViewFactory} can load into {@code contentArea}. */
    private static final List<String> REACHABLE_PAGES = List.of(
            "dashboard.fxml", "accounts.fxml", "transactions.fxml",
            "third-parties.fxml", "accounting-periods.fxml",
            "accounting-closures.fxml", "inventory.fxml",
            "financial-reporting.fxml", "exchange-rates.fxml",
            "comprobantes.fxml");

    /** The root {@code styleClass} of a page must not name one of these. */
    private static final Pattern ROOT_CHROME = Pattern.compile(
            "(^|[\"\\s,])?(p-\\d+|px-\\d+|py-\\d+|bg-(?:gray|slate|white)[\\w-]*)");

    private static final Pattern ROOT_TAG = Pattern.compile("<(?:VBox|StackPane|BorderPane|AnchorPane)\\b[^>]*?>");
    private static final Pattern PAGE_HEADER = Pattern.compile("styleClass=\"page-header\"");
    private static final Pattern PAGE_TITLE = Pattern.compile("styleClass=\"page-title\"");

    @Test
    void pageRootsLeaveCanvasAndGutterToTheShell() throws IOException {
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        for (String page : REACHABLE_PAGES) {
            Path fxml = FXML_ROOT.resolve(page);
            assertTrue(Files.exists(fxml), "reachable page is missing: " + page);
            String source = Files.readString(fxml, StandardCharsets.UTF_8);

            Matcher tag = ROOT_TAG.matcher(source);
            assertTrue(tag.find(), page + ": no root element found; the scan is broken");
            String root = tag.group();
            checked++;

            Matcher chrome = ROOT_CHROME.matcher(root);
            if (chrome.find()) {
                offenders.add(page + ": page root sets its own chrome (" + chrome.group().strip()
                        + "); the shell already paints the canvas and its 24px gutter");
            }
            if (source.contains("<padding>")) {
                offenders.add(page + ": page uses an inline <padding> block; spacing belongs in"
                        + " the p-* utility classes so the shell's gutter is not doubled");
            }
        }

        assertTrue(checked == REACHABLE_PAGES.size(), "not every reachable page was scanned");
        assertTrue(offenders.isEmpty(),
                "these pages double-pad or double-fill the shell's content area; drop the"
                        + " page-root padding/background and let .workspace-content own it:\n  "
                        + String.join("\n  ", offenders));
    }

    @Test
    void everyReachablePageOpensWithTheSameHeaderBlock() throws IOException {
        List<String> offenders = new ArrayList<>();
        int checked = 0;

        for (String page : REACHABLE_PAGES) {
            Path fxml = FXML_ROOT.resolve(page);
            String source = Files.readString(fxml, StandardCharsets.UTF_8);
            checked++;

            if (PAGE_HEADER.matcher(source).results().count() != 1) {
                offenders.add(page + ": expected exactly one styleClass=\"page-header\"");
            }
            if (PAGE_TITLE.matcher(source).results().count() != 1) {
                offenders.add(page + ": expected exactly one styleClass=\"page-title\"");
            }
        }

        assertTrue(checked > 0, "no pages found; the scan itself is broken");
        assertTrue(offenders.isEmpty(),
                "these pages do not open with the shared .page-header / .page-title block:\n  "
                        + String.join("\n  ", offenders));
    }
}
