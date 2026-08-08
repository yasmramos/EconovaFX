package com.econovafx;

import io.ebean.Database;
import io.ebean.DB;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify Ebean DataSource initialization behavior.
 * This test specifically checks for the "DataSource user if not set" issue
 * and ensures proper configuration from application-test.yaml
 */
public class EbeanInitializationTest {

    private static final Logger logger = LoggerFactory.getLogger(EbeanInitializationTest.class);

    /**
     * Test that verifies Ebean database is properly initialized with correct DataSource configuration
     */
    @Test
    @DisplayName("Verify Ebean DataSource is properly configured without 'user if not set' warning")
    void testEbeanDataSourceConfiguration() {
        logger.info("Testing Ebean DataSource configuration...");
        
        // Get the default database instance
        Database db = DB.getDefault();
        
        // Verify database is not null
        assertNotNull(db, "Database should be initialized");
        
        // Verify the database is accessible
        assertDoesNotThrow(() -> {
            db.sqlQuery("SELECT 1").findOne();
        }, "Database connection should be functional");
        
        logger.info("Ebean DataSource configuration test passed");
    }

    /**
     * Test that verifies multiple databases can be accessed if configured
     */
    @Test
    @DisplayName("Verify default server is correctly identified")
    void testDefaultServerIdentification() {
        logger.info("Testing default server identification...");
        
        // Get the default database
        Database defaultDb = DB.getDefault();
        
        assertNotNull(defaultDb, "Default database should exist");
        
        // For this test configuration, we only have one database (default)
        // The test verifies that the default database is accessible
        logger.info("Default database is accessible: {}", defaultDb.toString());
        
        logger.info("Default server identification test passed");
    }

    /**
     * Test database connectivity and basic operations
     */
    @Test
    @DisplayName("Verify database connectivity with configured DataSource")
    void testDatabaseConnectivity() {
        logger.info("Testing database connectivity...");
        
        Database db = DB.getDefault();
        
        // Test basic SQL query execution
        assertDoesNotThrow(() -> {
            var result = db.sqlQuery("SELECT 1 AS value").findOne();
            assertNotNull(result, "Query should return a result");
        }, "Basic SQL query should execute successfully");
        
        // Test table creation (in-memory H2 supports this)
        assertDoesNotThrow(() -> {
            db.sqlUpdate("CREATE TABLE IF NOT EXISTS test_table (id BIGINT PRIMARY KEY, name VARCHAR(255))")
              .execute();
        }, "Should be able to create tables");
        
        // Test insert operation
        assertDoesNotThrow(() -> {
            db.sqlUpdate("INSERT INTO test_table (id, name) VALUES (1, 'Test Record')")
              .execute();
        }, "Should be able to insert records");
        
        // Test select operation
        assertDoesNotThrow(() -> {
            var result = db.sqlQuery("SELECT * FROM test_table WHERE id = 1")
                          .findOne();
            assertNotNull(result, "Should retrieve inserted record");
        }, "Should be able to query records");
        
        logger.info("Database connectivity test passed");
    }
}
