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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.javafx.FontIcon;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression guard for the navigation rail geometry.
 *
 * <p>Two bugs are covered here:
 * <ul>
 *   <li>A nav button that keeps its preferred width makes
 *       {@code -fx-alignment: center-left} a no-op, and the icon lands in the
 *       middle of the rail instead of at its left edge.</li>
 *   <li>{@code .sidebar-btn-wrapper} is a StackPane, which centres a child that
 *       stays at its preferred width. Without a wide preferred width the wrapped
 *       buttons (the ones with a chevron) drifted to the centre of the rail.</li>
 * </ul>
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

    private VBox rail;

    @Override
    public void start(Stage stage) {
        rail = new VBox();
        rail.getStyleClass().addAll("sidebar-container", "bg-slate-800");
        rail.setPrefWidth(260);
        rail.setMinWidth(260);
        rail.setMaxWidth(260);

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

        ScrollPane scroll = new ScrollPane(rail);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("sidebar-scroll");

        Scene scene = new Scene(scroll, 260, 620);
        for (String sheet : STYLESHEETS) {
            java.net.URL url = getClass().getResource(sheet);
            assertNotNull(url, "missing stylesheet " + sheet);
            scene.getStylesheets().add(url.toExternalForm());
        }
        stage.setScene(scene);
        stage.show();
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

    @BeforeEach
    public void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(SidebarNavLayoutTest.TestApp.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }

    public static class TestApp extends javafx.application.Application {
        @Override
        public void start(Stage stage) {
            // scene is built by the test itself
        }
    }

    @Test
    public void everyNavButtonIsLeftAligned() {
        List<Button> buttons = new ArrayList<>();
        rail.lookupAll(".sidebar-btn").forEach(n -> buttons.add((Button) n));
        assertEquals(6, buttons.size(), "expected six nav buttons in the fixture");

        for (Button b : buttons) {
            assertEquals(Pos.CENTER_LEFT, b.getAlignment(),
                    b.getText() + " must inherit -fx-alignment: center-left");
            assertEquals(ContentDisplay.LEFT, b.getContentDisplay(),
                    b.getText() + " must place the icon left of the label");
            assertNotNull(b.getGraphic(), b.getText() + " must keep its graphic");
        }
    }

    /**
     * A nav item must span the whole rail, not just its own text. A button that
     * stays at its preferred width also makes -fx-alignment: center-left a no-op.
     */
    @Test
    public void everyNavButtonFillsTheRail() {
        double contentWidth = rail.getWidth()
                - rail.getInsets().getLeft() - rail.getInsets().getRight();

        List<Button> buttons = new ArrayList<>();
        rail.lookupAll(".sidebar-btn").forEach(n -> buttons.add((Button) n));

        for (Button b : buttons) {
            System.out.printf(
                    "%-16s width=%8.1f  pref=%8.1f  min=%6.1f  max=%s%n",
                    b.getText(), b.getWidth(), b.prefWidth(-1),
                    b.getMinWidth(),
                    b.getMaxWidth() == Double.MAX_VALUE ? "Infinity" : String.valueOf(b.getMaxWidth()));
        }

        for (Button b : buttons) {
            assertEquals(contentWidth, b.getWidth(), 1.0,
                    b.getText() + " must span the full rail width");
        }
    }

    /**
     * The wrapped buttons must fill their StackPane; a centred content-sized
     * button is what pushed the chevron rows into the middle of the rail.
     */
    @Test
    public void wrappedButtonsStretchToTheRail() {
        int checked = 0;
        for (Node wrapper : rail.lookupAll(".sidebar-btn-wrapper")) {
            double wrapperWidth = wrapper.getBoundsInLocal().getWidth();
            for (Node child : ((javafx.scene.Parent) wrapper).getChildrenUnmodifiable()) {
                if (child instanceof Button button) {
                    assertEquals(wrapperWidth, button.getWidth(), 1.0,
                            button.getText() + " must stretch across its wrapper");
                    checked++;
                }
            }
        }
        assertEquals(2, checked, "both chevron buttons should have been checked");
    }

    /** Every nav icon must start at the same left edge, icon beside label. */
    @Test
    public void everyNavIconSharesTheSameLeftEdge() {
        List<Button> buttons = new ArrayList<>();
        rail.lookupAll(".sidebar-btn").forEach(n -> buttons.add((Button) n));

        double reference = Double.NaN;
        String referenceLabel = "";
        for (Button b : buttons) {
            Node graphic = (Node) b.getGraphic();
            double offset = graphic.localToScene(graphic.getBoundsInLocal()).getMinX()
                    - b.localToScene(b.getBoundsInLocal()).getMinX();
            if (Double.isNaN(reference)) {
                reference = offset;
                referenceLabel = b.getText();
            } else {
                assertEquals(reference, offset, 1.5,
                        b.getText() + " icon is " + offset
                                + "px from the button's left edge but "
                                + referenceLabel + " is " + reference + "px");
            }
            assertTrue(offset < 16,
                    b.getText() + " icon should sit near the left padding, was at "
                            + offset + "px");
        }
    }
}