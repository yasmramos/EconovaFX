package com.econovafx.modules.core.ui.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.javafx.FontIcon;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression guard for the navigation rail geometry.
 *
 * <p>Four bugs are covered here:
 * <ul>
 *   <li>A nav button that keeps its preferred width makes
 *       {@code -fx-alignment: center-left} a no-op, so the icon lands in the
 *       middle of the rail instead of at its left edge.</li>
 *   <li>{@code .sidebar-btn-wrapper} is a StackPane, which centres a child that
 *       stays at its preferred width. Without a wide preferred width the wrapped
 *       buttons (the ones with a chevron) drifted to the centre of the rail.</li>
 *   <li>A StackPane stretches a child past its {@code max-width}, so the
 *       collapsed rail has to lower the preferred width as well or the chevron
 *       buttons stay full-rail wide.</li>
 *   <li>The base {@code .button} rule once forced {@code contentDisplay}, which
 *       beat the markup and stacked the icon on top of the label.</li>
 * </ul>
 *
 * <p>Every measurement is taken on the JavaFX thread right after the tree is
 * laid out and rendered; each test builds its own scene so results never depend
 * on the order the other tests happen to run in.
 */
public class SidebarNavLayoutTest extends ApplicationTest {

    private static final String[] STYLESHEETS = {
            "/css/theme-tokens.css",
            "/css/controls.css",
            "/css/main-styles.css",
            "/styles/sidebar.css",
            "/styles/dashboard.css",
            "/css/utilities.css"
    };

    private static final int EXPANDED_RAIL = 260;
    private static final int COLLAPSED_RAIL = 64;

    private Stage stage;

    /** What the stylesheets produced for one nav button, read after layout. */
    private record Measurement(
            String text,
            double width,
            double parentWidth,
            Pos alignment,
            ContentDisplay contentDisplay,
            boolean hasGraphic,
            double iconOffset) {
    }

    private record Snapshot(double railContentWidth, List<Measurement> buttons) {
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
    }

    @Test
    public void everyNavButtonIsLeftAligned() throws Exception {
        Snapshot snapshot = snapshot(false);
        assertEquals(6, snapshot.buttons().size(), "expected six nav buttons in the fixture");

        for (Measurement m : snapshot.buttons()) {
            assertEquals(Pos.CENTER_LEFT, m.alignment(),
                    m.text() + " must inherit -fx-alignment: center-left");
            assertEquals(ContentDisplay.LEFT, m.contentDisplay(),
                    m.text() + " must place the icon left of the label");
            assertTrue(m.hasGraphic(), m.text() + " must keep its graphic");
        }
    }

    /**
     * A nav item spans the whole rail, not just its own text. Without this the
     * button keeps its preferred width, which leaves -fx-alignment: center-left
     * with nothing to align.
     */
    @Test
    public void everyNavButtonFillsTheRail() throws Exception {
        Snapshot snapshot = snapshot(false);

        for (Measurement m : snapshot.buttons()) {
            assertEquals(snapshot.railContentWidth(), m.width(), 1.0,
                    m.text() + " must span the full rail width");
        }
    }

    /** The chevron buttons must fill their wrapper rather than sit centred. */
    @Test
    public void wrappedButtonsStretchToTheirWrapper() throws Exception {
        Snapshot snapshot = snapshot(false);

        List<Measurement> wrapped = snapshot.buttons().stream()
                .filter(m -> Math.abs(m.parentWidth() - m.width()) > 1.0)
                .toList();
        assertTrue(wrapped.isEmpty(),
                "these buttons do not fill their container, so center-left cannot "
                        + "apply: " + wrapped.stream().map(Measurement::text).toList());
    }

    /** Every nav icon must start at the same left edge, icon beside label. */
    @Test
    public void everyNavIconSharesTheSameLeftEdge() throws Exception {
        Snapshot snapshot = snapshot(false);

        double reference = Double.NaN;
        String referenceLabel = "";
        for (Measurement m : snapshot.buttons()) {
            if (Double.isNaN(reference)) {
                reference = m.iconOffset();
                referenceLabel = m.text();
            } else {
                assertEquals(reference, m.iconOffset(), 1.5,
                        m.text() + " icon is " + m.iconOffset()
                                + "px from the button's left edge but "
                                + referenceLabel + " is " + reference + "px");
            }
            assertTrue(m.iconOffset() < 16,
                    m.text() + " icon should sit near the left padding, was at "
                            + m.iconOffset() + "px");
        }
    }

    /**
     * The wide preferred width on .sidebar-btn must not survive into the
     * collapsed rail, where the label is hidden and each item becomes a 48px
     * icon tile.
     */
    @Test
    public void collapsedRailKeepsIconOnlyButtons() throws Exception {
        Snapshot snapshot = snapshot(true);

        assertEquals(48, snapshot.railContentWidth(), 1.0,
                "a 64px rail with \"12 8\" padding leaves a 48px content column");
        for (Measurement m : snapshot.buttons()) {
            assertEquals(48, m.width(), 1.0,
                    m.text() + " must stay a square icon tile when collapsed");
            assertEquals(Pos.CENTER, m.alignment(),
                    m.text() + " must centre its icon when collapsed");
        }
    }

    // ---------------------------------------------------------------- fixture

    private Snapshot snapshot(boolean collapsed) throws Exception {
        AtomicReference<Snapshot> result = new AtomicReference<>();
        FxToolkit.setupFixture(() -> {
            VBox rail = buildRail(collapsed);
            install(rail, collapsed ? COLLAPSED_RAIL : EXPANDED_RAIL, 620);

            double contentWidth = rail.getWidth()
                    - rail.getInsets().getLeft() - rail.getInsets().getRight();

            List<Measurement> buttons = new ArrayList<>();
            for (Button b : rail.lookupAll(".sidebar-btn")) {
                Node graphic = (Node) b.getGraphic();
                double offset = Double.NaN;
                if (graphic != null) {
                    offset = graphic.localToScene(graphic.getBoundsInLocal()).getMinX()
                            - b.localToScene(b.getBoundsInLocal()).getMinX();
                }
                double parentWidth = b.getParent() == null
                        ? Double.NaN
                        : b.getParent().getBoundsInLocal().getWidth();
                buttons.add(new Measurement(
                        b.getText(),
                        b.getWidth(),
                        parentWidth,
                        b.getAlignment(),
                        b.getContentDisplay(),
                        graphic != null,
                        offset));
            }
            result.set(new Snapshot(contentWidth, List.copyOf(buttons)));
        });
        Snapshot snapshot = result.get();
        assertNotNull(snapshot, "the fixture never ran on the JavaFX thread");
        return snapshot;
    }

    /**
     * Puts the rail on screen with the project's stylesheets and settles layout,
     * so measurements come from a rendered tree rather than pre-layout state.
     */
    private void install(VBox rail, double width, double height) {
        ScrollPane scroll = new ScrollPane(rail);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("sidebar-scroll");

        Scene scene = new Scene(scroll, width, height);
        for (String sheet : STYLESHEETS) {
            java.net.URL url = getClass().getResource(sheet);
            assertNotNull(url, "missing stylesheet " + sheet);
            scene.getStylesheets().add(url.toExternalForm());
        }
        stage.setScene(scene);
        stage.show();
        rail.applyCss();
        rail.layout();
    }

    /** Mirrors the navigation nesting of main-view.fxml. */
    private VBox buildRail(boolean collapsed) {
        VBox rail = new VBox();
        rail.getStyleClass().addAll("sidebar-container", "bg-slate-800");
        if (collapsed) {
            rail.getStyleClass().add("sidebar-collapsed");
        } else {
            // Matches the explicit width main-view.fxml puts on the rail.
            rail.setPrefWidth(EXPANDED_RAIL);
            rail.setMinWidth(EXPANDED_RAIL);
            rail.setMaxWidth(EXPANDED_RAIL);
        }

        Label section = new Label("MODULOS");
        section.getStyleClass().add("sidebar-section-title");
        rail.getChildren().add(section);

        rail.getChildren().add(nav("Dashboard", "mdi2v-view-dashboard", 20));
        rail.getChildren().add(wrappedGroup("Contabilidad", "mdi2b-book-open", 20));
        rail.getChildren().add(nav("Finanzas", "mdi2t-trending-up", 20));
        rail.getChildren().add(nav("AFT", "mdi2f-file-document-outline", 20));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        rail.getChildren().add(spacer);

        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-separator");
        rail.getChildren().add(sep);
        rail.getChildren().add(wrappedGroup("Configuracion", "mdi2c-cog", 20));
        rail.getChildren().add(nav("Ayuda", "mdi2h-help-circle", 20));

        return rail;
    }

    private static Button nav(String text, String icon, int size) {
        Button b = new Button(text);
        b.getStyleClass().addAll("btn", "btn-ghost", "sidebar-btn");
        b.setContentDisplay(ContentDisplay.LEFT);
        FontIcon fi = new FontIcon(icon);
        fi.setIconSize(size);
        b.setGraphic(fi);
        return b;
    }

    private static VBox wrappedGroup(String text, String icon, int size) {
        StackPane wrap = new StackPane();
        wrap.getStyleClass().add("sidebar-btn-wrapper");
        wrap.getChildren().add(nav(text, icon, size));
        FontIcon chevron = new FontIcon("mdi2c-chevron-down");
        chevron.setIconSize(16);
        chevron.setIconColor(Color.WHITE);
        StackPane.setAlignment(chevron, Pos.CENTER_RIGHT);
        chevron.getStyleClass().add("sidebar-chevron-float");
        wrap.getChildren().add(chevron);
        VBox group = new VBox();
        group.getStyleClass().add("sidebar-menu-group");
        group.getChildren().add(wrap);
        return group;
    }
}