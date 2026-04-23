package com.econovafx.config;

import io.ebean.Database;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration and initialization
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static Database database;

    public static void initialize() {
        try {
            Properties props = loadProperties();

            DataSourceConfig dsConfig = new DataSourceConfig();
            dsConfig.setDriver(props.getProperty("ebean.datasource.default.driver", "org.h2.Driver"));
            dsConfig.setUrl(props.getProperty("ebean.datasource.default.url",
                    "jdbc:h2:./db/econovadb;DB_CLOSE_DELAY=-1"));
            dsConfig.setUsername(props.getProperty("ebean.datasource.default.username", "sa"));
            dsConfig.setPassword(props.getProperty("ebean.datasource.default.password", ""));
            dsConfig.setMinConnections(1);
            dsConfig.setMaxConnections(10);

            DataSource dataSource = DataSourceFactory.create("econova-db", dsConfig);

            var dbConfig = new io.ebean.config.DatabaseConfig();
            dbConfig.setName("econova-db");
            dbConfig.setDataSource(dataSource);
            dbConfig.addPackage("com.econovafx.domain");
            dbConfig.setDdlGenerate(true);
            dbConfig.setDdlRun(true);

            database = io.ebean.DatabaseFactory.create(dbConfig);

            logger.info("Database initialized successfully");

        } catch (IOException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("ebean.properties")) {
            if (input != null) {
                props.load(input);
            }
        }
        return props;
    }

    public static Database getServer() {
        if (database == null) {
            initialize();
        }
        return database;
    }

    public static void shutdown() {
        if (database != null) {
            database.shutdown();
            logger.info("Database shutdown complete");
        }
    }
}
