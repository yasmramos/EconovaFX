package com.econovafx;

import com.econovafx.modules.core.config.DatabaseConfig;
import io.ebean.Database;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Application startup test - verifies core logic initialization without UI components.
 * Tests database configuration and basic connectivity.
 */
public class ApplicationStartupTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupTest.class);

    @BeforeAll
    static void setUp() {
        logger.info("Setting up application startup test...");
        // Initialize database for testing (master only, no multi-tenant)
        DatabaseConfig.initializeForTest();
        logger.info("Database initialized for test");
    }

    @AfterAll
    static void tearDown() {
        logger.info("Tearing down application startup test...");
        DatabaseConfig.shutdown();
        logger.info("Database shutdown complete");
    }

    /**
     * Test 1: Verify database initialization
     */
    @Test
    void testDatabaseInitialization() {
        logger.info("Testing database initialization...");
        Database db = DatabaseConfig.getMasterDatabase();
        
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
        
        Database db = DatabaseConfig.getMasterDatabase();
        
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
                Database db = DatabaseConfig.getMasterDatabase();
                assertNotNull(db, "Database should be initialized");
            },
            () -> {
                // Verify database is accessible
                Database db = DatabaseConfig.getMasterDatabase();
                assertNotNull(db, "Database should be available");
            }
        );
        
        logger.info("Full startup simulation test passed");
    }
}
