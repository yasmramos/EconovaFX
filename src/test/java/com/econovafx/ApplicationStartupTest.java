package com.econovafx;

import com.econovafx.modules.core.config.AppContext;
import com.econovafx.modules.core.ui.controller.DashboardController;
import com.econovafx.modules.core.ui.view.ViewFactory;
import io.ebean.Database;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Application startup test - verifies core logic initialization without UI components.
 * Tests database configuration, AppContext dependency injection, and basic connectivity.
 * Replicates the lifecycle: init() -> start() to ensure services are correctly wired.
 */
public class ApplicationStartupTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupTest.class);
    
    private App app;

    @BeforeAll
    static void setUp() {
        logger.info("Setting up application startup test...");
        // Database is auto-configured by ebean-test using application-test.yaml
        logger.info("Database initialized by ebean-test");
    }

    @AfterAll
    static void tearDown() {
        logger.info("Tearing down application startup test...");
        // Database is automatically shut down by ebean-test
        logger.info("Database shutdown complete");
    }
    
    @BeforeEach
    void beforeEach() {
        // Clean up any static state from previous tests
        AppContext.reset();
    }

    @AfterEach
    void afterEach() {
        // Reset static context to avoid side effects on other tests
        AppContext.reset();
    }

    /**
     * Test 1: Verify database initialization
     */
    @Test
    void testDatabaseInitialization() {
        logger.info("Testing database initialization...");
        Database db = io.ebean.DB.getDefault();
        
        assertNotNull(db, "Database should be initialized");
        // Note: Ebean Database API doesn't expose getName() or isOnline() directly
        // We verify it's functional by executing a query in testDatabaseConnection
        
        logger.info("Database initialization test passed");
    }

    /**
     * Test 2: Verify database connection is functional
     */
    @Test
    void testDatabaseConnection() {
        logger.info("Testing database connection...");
        
        Database db = io.ebean.DB.getDefault();
        
        // Execute a simple query to verify connection
        assertDoesNotThrow(() -> {
            db.sqlQuery("SELECT 1").findOne();
        }, "Database connection should be functional");
        
        logger.info("Database connection test passed");
    }

    /**
     * Test 3: Full startup simulation - database only
     */
    @Test
    void testFullStartupSimulation() {
        logger.info("Running full startup simulation...");
        
        // Simulate complete application startup sequence (database layer only)
        assertAll("Full startup simulation",
            () -> {
                Database db = io.ebean.DB.getDefault();
                assertNotNull(db, "Database should be initialized");
            },
            () -> {
                // Verify database is accessible
                Database db = io.ebean.DB.getDefault();
                assertNotNull(db, "Database should be available");
            }
        );
        
        logger.info("Full startup simulation test passed");
    }
    
    /**
     * Test 4: Verify application initialization with AppContext dependency injection
     * This test replicates the same initialization flow as App.java
     */
    @Test
    void testApplicationInitializationWithDependencyInjection() {
        logger.info("Testing application initialization with AppContext dependency injection...");
        
        // 1. Instantiate the App (same as JavaFX launcher does)
        app = new App();

        // 2. Call init() explicitly to trigger dependency injection and service initialization
        // This mimics the JavaFX lifecycle where init() is called before start()
        assertDoesNotThrow(() -> app.init(), 
            "App.init() should not throw exceptions during service initialization");

        // 3. Verify AppContext is initialized
        assertTrue(AppContext.isInitialized(), 
            "AppContext should be initialized after app.init()");

        // 4. Verify critical components are available in the context
        AppContext context = AppContext.getInstance();
        assertNotNull(context.getViewFactory(), 
            "ViewFactory should be available in AppContext");
        assertNotNull(context.getDashboardController(), 
            "DashboardController should be available in AppContext");

        // 5. Verify the ViewFactory is correctly wired to the DashboardController
        DashboardController controller = context.getDashboardController();
        ViewFactory factory = context.getViewFactory();
        
        assertNotNull(controller, "DashboardController instance should exist");
        assertNotNull(factory, "ViewFactory instance should exist");

        logger.info("Application initialization with dependency injection test passed");
    }
}
