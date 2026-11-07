package com.econovafx.modules.core.ui.util;

import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
