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
        DatabaseConfig.initialize();
        Database database = DatabaseConfig.getServer();
        logger.info("Database initialized successfully");
        return database;
    }
}
