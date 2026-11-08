package com.econovafx.modules.core.ui.util;

import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the design-system styling of the controls that used to
 * fall through to the Modena default theme: tab panes, titled panes, accordions,
 * tree views, hyperlinks and split panes. Each assertion pins the resolved
 * colour to a design token so a missing rule fails loudly instead of silently
 * rendering an unstyled control.
 */
public class ControlsThemeTest extends ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
    }

    @BeforeEach
    public void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(TestApp.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }

    public static class TestApp extends javafx.application.Application {
        @Override
        public void start(Stage stage) {
        }
    }

    @Test
    public void tabHeaderAndContentUseSurfaceTokens() throws Exception {
        FxToolkit.setupFixture(() -> {
            TabPane tabs = new TabPane();
            tabs.getTabs().add(new Tab("Uno", new VBox()));
            tabs.getTabs().add(new Tab("Dos", new VBox()));

            VBox root = showWithDesignSystem(tabs);
            root.applyCss();
            root.layout();

            Region header = (Region) tabs.lookup(".tab-header-background");
            Region content = (Region) tabs.lookup(".tab-content-area");
            assertNotNull(header, "tab-header-background must resolve");
            assertNotNull(content, "tab-content-area must resolve");

            assertEquals(Color.web("#f4f4f5"), fillOf(header),
                    "tab header track should be the muted token");
            assertEquals(Color.web("#ffffff"), fillOf(content),
                    "tab content should be the background token");
        });
    }

    @Test
    public void titledPaneIsACardAndHyperlinkIsInfoHued() throws Exception {
        FxToolkit.setupFixture(() -> {
            TitledPane titled = new TitledPane("Tasas", new VBox());
            Hyperlink link = new Hyperlink("Documentación");

            VBox root = showWithDesignSystem(new VBox(titled, link));
            root.applyCss();
            root.layout();

            Region title = (Region) titled.lookup(".title");
            assertNotNull(title, "titled-pane title must resolve");
            assertEquals(Color.web("#ffffff"), fillOf(title),
                    "titled-pane title should be the card token");

            assertEquals(Color.web("#2563eb"), link.getTextFill(),
                    "hyperlink should use the info token");
        });
    }

    @Test
    public void accordionTreeAndSplitPaneUseHairlineTokens() throws Exception {
        FxToolkit.setupFixture(() -> {
            Accordion accordion = new Accordion();
            accordion.getPanes().add(new TitledPane("A", new VBox()));
            accordion.getPanes().add(new TitledPane("B", new VBox()));

            TreeView<String> tree = new TreeView<>(new TreeItem<>("raíz"));
            SplitPane split = new SplitPane(new VBox(), new VBox());

            VBox root = showWithDesignSystem(new VBox(accordion, tree, split));
            root.applyCss();
            root.layout();

            Region divider = (Region) split.lookup(".split-pane-divider");
            assertNotNull(divider, "split-pane-divider must resolve");
            assertEquals(Color.web("#e4e4e7"), fillOf(divider),
                    "split divider should be the border token");

            assertEquals(Color.web("#ffffff"), fillOf(tree),
                    "tree-view surface should be the background token");
            assertTrue(accordion.getStyleClass().contains("accordion"));
        });
    }

    @Test
    public void statusLabelAndActionButtonAreStyled() throws Exception {
        FxToolkit.setupFixture(() -> {
            Label status = new Label("Estado: Desconocido");
            status.getStyleClass().add("status-label");

            Button action = new Button("Exportar");
            action.getStyleClass().add("action-btn");

            VBox root = showWithDesignSystem(new VBox(status, action));
            root.applyCss();
            root.layout();

            assertEquals(Color.web("#6e6e76"), status.getTextFill(),
                    "status-label should use the muted-foreground token");
            assertEquals(Color.web("#ffffff"), fillOf(action),
                    "action-btn background should be the background token");
            // -fx-graphic-text-gap is easy to mistype as -fx-graphic-gap, which
            // JavaFX ignores silently and leaves the default 4px.
            assertEquals(8.0, action.getGraphicTextGap(), 0.001,
                    "action-btn should set the icon/label gap");
        });
    }

    private VBox showWithDesignSystem(Region control) {
        VBox root = new VBox(control);
        Scene scene = new Scene(root, 800, 600);
        StyleSheets.apply(scene);
        stage.setScene(scene);
        stage.show();
        return root;
    }

    private static Paint fillOf(Region region) {
        assertNotNull(region.getBackground(), "region must have a background");
        assertTrue(!region.getBackground().getFills().isEmpty(), "region must have a fill");
        return region.getBackground().getFills().get(0).getFill();
    }
}
