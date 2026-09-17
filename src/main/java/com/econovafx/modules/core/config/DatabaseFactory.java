package com.econovafx.modules.core.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.ebean.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to provide Database instance for dependency injection
 */
@Factory
public class DatabaseFactory {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseFactory.class);

    @Bean
    public Database database() {
        logger.info("Initializing database for dependency injection...");

        // Initialize master database first (always needed)
        DatabaseConfig.initializeMaster();

        // Only initialize multi-tenant if we have tenant data
        // For production startup, we only need the master database
        // Multi-tenant will be initialized on demand when switching tenants
        try {
            // Check if there are companies in the master database
            // If yes, initialize multi-tenant
            Database masterDb = DatabaseConfig.getMasterDatabase();
            long companyCount = masterDb.find(com.econovafx.modules.core.model.Company.class).findCount();
            
            if (companyCount > 0) {
                logger.info("Found {} companies, initializing multi-tenant support", companyCount);
                DatabaseConfig.initializeMultiTenant();
            } else {
                logger.info("No companies found, multi-tenant initialization skipped (will be initialized on demand)");
            }
        } catch (Exception e) {
            logger.warn("Multi-tenant initialization skipped (will be initialized on demand): {}", e.getMessage());
        }

        Database database = DatabaseConfig.getServer();
        logger.info("Database initialized successfully");
        return database;
    }
}