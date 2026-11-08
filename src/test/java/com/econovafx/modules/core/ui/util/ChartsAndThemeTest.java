package com.econovafx.modules.core.ui.util;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the chart chrome, the progress indicator and the
 * light/dark theme.
 *
 * <p>These pin the behaviours that were wrong before: Modena painted a zinc-50
 * box behind every chart and a solid dark disc behind the progress indicator,
 * and there was no dark theme at all. Each assertion reads a resolved colour so
 * a missing or mis-scoped rule fails loudly instead of rendering silently.
 */
public class ChartsAndThemeTest extends ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
    }

    @BeforeEach
    public void setUp() throws Exception {
        // ThemeManager persists the choice; start every test from light so the
        // suite does not depend on method order or on a previous run.
        if (ThemeManager.isDark()) {
            ThemeManager.toggle(new Scene(new VBox(), 1, 1));
        }
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(TestApp.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // ThemeManager keeps the choice in a static field; leave the suite light.
        if (ThemeManager.isDark()) {
            ThemeManager.toggle(new Scene(new VBox(), 1, 1));
        }
        FxToolkit.cleanupStages();
    }

    public static class TestApp extends javafx.application.Application {
        @Override
        public void start(Stage stage) {
        }
    }

    @Test
    public void chartFrameAndPlotAreTransparentSoTheCardShowsThrough() throws Exception {
        FxToolkit.setupFixture(() -> {
            BarChart<String, Number> bar = new BarChart<>(new CategoryAxis(), new NumberAxis());
            bar.getData().add(new XYChart.Series<>("Ventas",
                    javafx.collections.FXCollections.observableArrayList(new XYChart.Data<>("Ene", 10))));

            VBox root = show(new VBox(bar));
            root.applyCss();
            root.layout();

            Region chart = (Region) bar.lookup(".chart");
            Region plot = (Region) bar.lookup(".chart-plot-background");
            assertNotNull(chart, ".chart must resolve");
            assertNotNull(plot, ".chart-plot-background must resolve");

            // Modena defaulted the plot area to -fx-background (zinc-50 here).
            assertEquals(Color.TRANSPARENT, fillOf(plot),
                    "chart plot area must not paint a grey box behind the series");
            assertEquals(Color.TRANSPARENT, fillOf(chart),
                    "chart frame must be transparent so the card surface shows");
        });
    }

    @Test
    public void progressIndicatorIsARingNotADisc() throws Exception {
        FxToolkit.setupFixture(() -> {
            ProgressIndicator indicator = new ProgressIndicator(0.4);

            VBox root = show(new VBox(indicator));
            root.applyCss();
            root.layout();

            Region track = (Region) indicator.lookup(".determinate-indicator > .indicator");
            Region progress = (Region) indicator.lookup(".determinate-indicator > .progress");
            assertNotNull(track, "the indicator track must resolve");
            assertNotNull(progress, "the indicator progress arc must resolve");

            assertEquals(Color.TRANSPARENT, fillOf(indicator),
                    "the progress indicator surface must be transparent, not a dark disc");
            assertEquals(Color.web("#f4f4f5"), fillOf(track),
                    "the track should be the muted token");
            assertEquals(Color.web("#18181b"), fillOf(progress),
                    "the arc should be the primary token, not Modena blue");
        });
    }

    @Test
    public void darkThemeFlipsTheSemanticTokens() throws Exception {
        FxToolkit.setupFixture(() -> {
            Label label = new Label("Hola");
            VBox root = show(label);
            root.applyCss();
            root.layout();

            assertEquals(Color.web("#09090b"), label.getTextFill(),
                    "light theme: text should be the light foreground token");

            boolean dark = ThemeManager.toggle(root.getScene());
            assertTrue(dark, "toggle should switch the theme on");
            root.applyCss();
            root.layout();

            assertTrue(root.getStyleClass().contains("dark"),
                    "the dark class must land on the scene root");
            assertEquals(Color.web("#fafafa"), label.getTextFill(),
                    "dark theme: text should flip to the dark foreground token");
            assertEquals(Color.web("#09090b"), root.getScene().getFill(),
                    "the page backdrop must follow the theme");
        });
    }

    @Test
    public void themeIsIdempotentWhenAppliedTwice() throws Exception {
        FxToolkit.setupFixture(() -> {
            VBox root = show(new Label("Hola"));

            ThemeManager.toggle(root.getScene());
            ThemeManager.applyTo(root.getScene());
            ThemeManager.applyTo(root.getScene());

            long darkCount = root.getStyleClass().stream().filter("dark"::equals).count();
            assertEquals(1, darkCount, "the dark class must never be added twice");
        });
    }

    @Test
    public void pageAndCardSurfacesAndLegacyUtilitiesFlipWithTheTheme() throws Exception {
        FxToolkit.setupFixture(() -> {
            Label title = new Label("Balance");
            title.getStyleClass().add("text-gray-700");
            VBox card = new VBox(title);
            card.getStyleClass().add("bg-white");
            VBox page = new VBox(card);
            page.getStyleClass().add("workspace-content");

            Scene scene = new Scene(page, 800, 600);
            StyleSheets.apply(scene, "/css/main-styles.css");
            stage.setScene(scene);
            stage.show();
            page.applyCss();
            page.layout();

            // Light: page zinc-50, card white, body text zinc-700.
            assertEquals(Color.web("#fafafa"), fillOf(page), "light page canvas");
            assertEquals(Color.web("#ffffff"), fillOf(card), "light card surface");
            assertEquals(Color.web("#3f3f46"), title.getTextFill(), "light body text");

            ThemeManager.toggle(scene);
            page.applyCss();
            page.layout();

            // Dark: the page and card must separate, and the text must not stay
            // dark-on-dark. This is what the raw zinc scale would have done.
            assertEquals(Color.web("#09090b"), fillOf(page), "dark page canvas");
            assertEquals(Color.web("#18181b"), fillOf(card), "dark card surface");
            assertEquals(Color.web("#d4d4d8"), title.getTextFill(), "dark body text");
        });
    }

    @Test
    public void chartSeriesUseTheTokenPaletteNotTheModenaDefaults() throws Exception {
        FxToolkit.setupFixture(() -> {
            BarChart<String, Number> bar = new BarChart<>(new CategoryAxis(), new NumberAxis());
            for (int i = 0; i < 3; i++) {
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName("S" + i);
                s.getData().add(new XYChart.Data<>("Ene", 10 + i));
                bar.getData().add(s);
            }

            VBox root = show(new VBox(bar));
            root.applyCss();
            root.layout();

            // The series node carries "default-color0" and "chart-bar" on itself,
            // so only a compound selector matches. The old descendant selector
            // (".chart-bar .default-color0.chart-bar") never did, and every bar
            // fell back to Modena's orange. Modena then paints a gradient derived
            // from -fx-bar-fill, whose middle stop is the base colour, so that
            // stop must sit on the matching -series token.
            Color[] expected = {Color.web("#3b82f6"), Color.web("#10b981"), Color.web("#f59e0b")};
            for (int i = 0; i < 3; i++) {
                Node series = seriesNode(bar, ".default-color" + i + ".chart-bar");
                Paint fill = fillOfNode(series);
                assertTrue(fill instanceof LinearGradient,
                        "bar series " + i + " should keep Modena's derived gradient, got " + fill);
                Color mid = ((LinearGradient) fill).getStops().get(2).getColor();
                assertColorNear(expected[i], mid, 0.02);
            }
        });
    }

    @Test
    public void lineAndAreaSeriesStrokeAndFillUseTheTokens() throws Exception {
        FxToolkit.setupFixture(() -> {
            LineChart<String, Number> line = new LineChart<>(new CategoryAxis(), new NumberAxis());
            XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
            lineSeries.setName("L");
            lineSeries.getData().add(new XYChart.Data<>("Ene", 5));
            line.getData().add(lineSeries);

            AreaChart<String, Number> area = new AreaChart<>(new CategoryAxis(), new NumberAxis());
            XYChart.Series<String, Number> areaSeries = new XYChart.Series<>();
            areaSeries.setName("A");
            areaSeries.getData().add(new XYChart.Data<>("Ene", 7));
            area.getData().add(areaSeries);

            VBox root = show(new VBox(line, area));
            root.applyCss();
            root.layout();

            assertEquals(Color.web("#3b82f6"),
                    strokeOf(seriesNode(line, ".chart-series-line.default-color0")),
                    "the line series must stroke -series-1, not Modena's CHART_COLOR_1");
            assertEquals(Color.web("#3b82f6"),
                    strokeOf(seriesNode(area, ".chart-series-area-line.default-color0")),
                    "the area line must stroke -series-1");
            assertEquals(Color.web("#3b82f6", 0.125),
                    fillOfNode(seriesNode(area, ".chart-series-area-fill.default-color0")),
                    "the area wash must be -series-1-wash");
        });
    }

    @Test
    public void indeterminateProgressBarKeepsItsSweepNotAFlatBlock() throws Exception {
        FxToolkit.setupFixture(() -> {
            ProgressBar bar = new ProgressBar(-1);

            VBox root = show(new VBox(bar));
            root.applyCss();
            root.layout();

            Region fill = (Region) bar.lookup(".bar");
            assertNotNull(fill, ".bar must resolve");
            // The base ".progress-bar .bar" rule paints a flat -primary fill and
            // would otherwise flatten Modena's sweeping indeterminate gradient.
            assertTrue(fillOf(fill) instanceof LinearGradient,
                    "an indeterminate bar must keep a sweeping gradient, not a flat block");
        });
    }

    @Test
    public void scrollBarThumbFollowsTheTheme() throws Exception {
        FxToolkit.setupFixture(() -> {
            ScrollBar bar = new ScrollBar();
            bar.setOrientation(Orientation.VERTICAL);

            VBox root = show(new VBox(bar));
            root.applyCss();
            root.layout();

            Region thumb = (Region) bar.lookup(".thumb");
            assertNotNull(thumb, ".thumb must resolve");
            assertEquals(Color.web("#d4d4d8"), fillOf(thumb), "light thumb is -thumb (zinc-300)");

            ThemeManager.toggle(root.getScene());
            root.applyCss();
            root.layout();
            assertEquals(Color.web("#3f3f46"), fillOf(thumb), "dark thumb flips to -thumb (zinc-700)");
        });
    }

    private Node seriesNode(Region chart, String selector) {
        Node node = chart.lookup(selector);
        assertNotNull(node, selector + " must resolve");
        return node;
    }

    private static Paint fillOfNode(Node node) {
        if (node instanceof Region region) {
            return fillOf(region);
        }
        if (node instanceof javafx.scene.shape.Shape shape) {
            return shape.getFill();
        }
        throw new AssertionError("node has no fill: " + node);
    }

    private static void assertColorNear(Color expected, Color actual, double tolerance) {
        assertTrue(Math.abs(expected.getRed() - actual.getRed()) <= tolerance
                        && Math.abs(expected.getGreen() - actual.getGreen()) <= tolerance
                        && Math.abs(expected.getBlue() - actual.getBlue()) <= tolerance,
                "expected " + expected + " but was " + actual);
    }

    private static Color strokeOf(Node node) {
        assertTrue(node instanceof javafx.scene.shape.Shape, "expected a shape, got " + node);
        Paint stroke = ((javafx.scene.shape.Shape) node).getStroke();
        assertTrue(stroke instanceof Color, "expected a flat stroke colour, got " + stroke);
        return (Color) stroke;
    }

    private VBox show(Region control) {
        VBox root = new VBox(control);
        Scene scene = new Scene(root, 800, 600);
        StyleSheets.apply(scene, "/styles/dashboard.css");
        stage.setScene(scene);
        stage.show();
        return root;
    }

    private static Paint fillOf(Region region) {
        assertNotNull(region.getBackground(), "region must have a background");
        assertFalse(region.getBackground().getFills().isEmpty(), "region must have a fill");
        return region.getBackground().getFills().get(0).getFill();
    }
}
