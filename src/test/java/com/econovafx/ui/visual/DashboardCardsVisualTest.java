package com.econovafx.ui.visual;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.math.BigDecimal;

/**
 * Visual tests for Dashboard KPI Cards component.
 * Captures screenshots of the dashboard cards and saves them to docs/images/
 */
public class DashboardCardsVisualTest extends ApplicationTest {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        VBox mainLayout = new VBox(30);
        mainLayout.setStyle("-fx-padding: 30; -fx-background-color: #f8fafc;");
        mainLayout.setSpacing(20);
        
        Label title = new Label("Dashboard KPI Cards Visual Test");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Create sample KPI cards grid
        GridPane cardsGrid = createSampleKpiCards();
        
        mainLayout.getChildren().addAll(title, cardsGrid);
        
        Scene scene = new Scene(mainLayout, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Dashboard Cards Visual Test");
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(DashboardCardsVisualTest.TestApp.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }

    // Simple test application class
    public static class TestApp extends javafx.application.Application {
        @Override
        public void start(Stage stage) throws Exception {
            // Will be overridden by tests
        }
    }

    @Test
    public void testDashboardCardsAppearance() throws Exception {
        Thread.sleep(500); // Wait for rendering
        
        captureCurrentState("dashboard-cards");
    }

    private GridPane createSampleKpiCards() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setStyle("-fx-padding: 10;");
        
        // Card 1: Total Assets
        grid.add(createKpiCard("Total Assets", "$125,450.00", "USD", "+12.5%", true), 0, 0);
        
        // Card 2: Total Liabilities
        grid.add(createKpiCard("Total Liabilities", "$45,230.00", "USD", "-3.2%", false), 1, 0);
        
        // Card 3: Revenue
        grid.add(createKpiCard("Revenue", "$89,750.00", "USD", "+18.3%", true), 2, 0);
        
        // Card 4: Expenses
        grid.add(createKpiCard("Expenses", "$34,120.00", "USD", "+5.7%", false), 0, 1);
        
        // Card 5: Net Profit
        grid.add(createKpiCard("Net Profit", "$55,630.00", "USD", "+22.1%", true), 1, 1);
        
        // Card 6: Cash Flow
        grid.add(createKpiCard("Cash Flow", "$28,940.00", "USD", "+8.9%", true), 2, 1);
        
        return grid;
    }

    private VBox createKpiCard(String title, String value, String currency, String trend, boolean positive) {
        VBox card = new VBox(10);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0.5, 0, 4);" +
            "-fx-padding: 20;"
        );
        card.setPrefSize(280, 140);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #64748b;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label trendLabel = new Label(trend);
        trendLabel.setStyle(String.format(
            "-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: %s;",
            positive ? "#10b981" : "#ef4444"
        ));
        
        card.getChildren().addAll(titleLabel, valueLabel, trendLabel);
        return card;
    }

    private void captureCurrentState(String filename) {
        try {
            javafx.application.Platform.runLater(() -> {
                try {
                    Scene scene = primaryStage.getScene();
                    if (scene != null && scene.getRoot() != null) {
                        VisualTestUtils.captureNode(scene.getRoot(), filename);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to capture screenshot: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            // Wait for the capture to complete
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Capture failed: " + e.getMessage());
        }
    }
}
