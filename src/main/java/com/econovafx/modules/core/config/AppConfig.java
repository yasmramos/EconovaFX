package com.econovafx.modules.core.config;

import io.avaje.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Centralized configuration management using Avaje Config.
 * Provides type-safe access to all application configuration properties.
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    // Application Configuration
    public static final String APP_NAME;
    public static final String APP_VERSION;
    
    // Database Configuration
    public static final String DB_DRIVER;
    public static final String DB_URL;
    public static final String DB_USERNAME;
    public static final String DB_PASSWORD;
    public static final String DB_PATH;
    
    // Master Database Configuration
    public static final String MASTER_DB_DRIVER;
    public static final String MASTER_DB_URL;
    public static final String MASTER_DB_USERNAME;
    public static final String MASTER_DB_PASSWORD;
    
    // Ebean Configuration
    public static final boolean EBEAN_DDL_GENERATE;
    public static final boolean EBEAN_DDL_RUN;
    public static final boolean EBEAN_MIGRATION_AUTO;
    public static final boolean EBEAN_MIGRATION_RUN;
    public static final String EBEAN_MIGRATION_PATH;
    
    // UI Configuration
    public static final String UI_THEME;
    public static final int UI_WIDTH;
    public static final int UI_HEIGHT;
    
    // Exchange Rate Configuration
    public static final int EXCHANGE_RATE_CACHE_TTL_MINUTES;
    public static final boolean EXCHANGE_RATE_SCHEDULER_ENABLED;
    public static final String EXCHANGE_RATE_SCHEDULER_CRON;
    
    // BCC API Configuration
    public static final String BCC_API_BASE_URL;
    public static final int BCC_API_TIMEOUT_SECONDS;
    public static final int BCC_API_RETRY_MAX_ATTEMPTS;
    public static final long BCC_API_RETRY_DELAY_MS;
    
    // Security Configuration
    public static final String SESSION_TIMEOUT;
    public static final int MAX_LOGIN_ATTEMPTS;
    
    static {
        logger.info("Loading application configuration with Avaje Config...");
        
        // Application
        APP_NAME = Config.get("app.name", "EconoNova FX");
        APP_VERSION = Config.get("app.version", "1.0.0");
        
        // Database
        DB_DRIVER = Config.get("database.driver", "org.h2.Driver");
        DB_URL = Config.get("database.url", "jdbc:h2:./db/econovadb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE");
        DB_USERNAME = Config.get("database.username", "sa");
        DB_PASSWORD = Config.get("database.password", "");
        DB_PATH = Config.get("app.database.path", "./db/econovadb");
        
        // Master Database
        MASTER_DB_DRIVER = Config.get("ebean.datasource.master.driver", "org.h2.Driver");
        MASTER_DB_URL = Config.get("ebean.datasource.master.url", "jdbc:h2:./db/econova_master;DB_CLOSE_DELAY=-1");
        MASTER_DB_USERNAME = Config.get("ebean.datasource.master.username", "sa");
        MASTER_DB_PASSWORD = Config.get("ebean.datasource.master.password", "");
        
        // Ebean
        EBEAN_DDL_GENERATE = Config.getBool("ebean.ddl.generate", true);
        EBEAN_DDL_RUN = Config.getBool("ebean.ddl.run", true);
        EBEAN_MIGRATION_AUTO = Config.getBool("ebean.migration.auto", true);
        EBEAN_MIGRATION_RUN = Config.getBool("ebean.migration.run", true);
        EBEAN_MIGRATION_PATH = Config.get("ebean.migration.resourcePath", "dbmigration");
        
        // UI
        UI_THEME = Config.get("ui.theme", "modern");
        UI_WIDTH = Config.getInt("ui.width", 1200);
        UI_HEIGHT = Config.getInt("ui.height", 800);
        
        // Exchange Rate
        EXCHANGE_RATE_CACHE_TTL_MINUTES = Config.getInt("exchange.rate.cache.ttl.minutes", 60);
        EXCHANGE_RATE_SCHEDULER_ENABLED = Config.getBool("exchange.rate.scheduler.enabled", true);
        EXCHANGE_RATE_SCHEDULER_CRON = Config.get("exchange.rate.scheduler.cron", "0 0 6 * * ?");
        
        // BCC API
        BCC_API_BASE_URL = Config.get("bcc.api.base.url", "https://api.bc.gob.cu/v1/tasas-de-cambio");
        BCC_API_TIMEOUT_SECONDS = Config.getInt("bcc.api.timeout.seconds", 30);
        BCC_API_RETRY_MAX_ATTEMPTS = Config.getInt("bcc.api.retry.max.attempts", 3);
        BCC_API_RETRY_DELAY_MS = Config.getLong("bcc.api.retry.delay.ms", 2000L);
        
        // Security
        SESSION_TIMEOUT = Config.get("security.session.timeout", "30m");
        MAX_LOGIN_ATTEMPTS = Config.getInt("security.login.max.attempts", 5);
        
        logger.info("Configuration loaded successfully");
        logger.debug("App: {} v{}", APP_NAME, APP_VERSION);
        logger.debug("Database: {}", DB_URL);
        logger.debug("UI: {} ({}x{})", UI_THEME, UI_WIDTH, UI_HEIGHT);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private AppConfig() {
    }

    /**
     * Gets a string configuration value.
     * @param key Configuration key
     * @param defaultValue Default value if key not found
     * @return Configuration value or default
     */
    public static String getString(String key, String defaultValue) {
        return Config.get(key, defaultValue);
    }

    /**
     * Gets an integer configuration value.
     * @param key Configuration key
     * @param defaultValue Default value if key not found
     * @return Configuration value or default
     */
    public static int getInt(String key, int defaultValue) {
        return Config.getInt(key, defaultValue);
    }

    /**
     * Gets a boolean configuration value.
     * @param key Configuration key
     * @param defaultValue Default value if key not found
     * @return Configuration value or default
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return Config.getBool(key, defaultValue);
    }

    /**
     * Gets a long configuration value.
     * @param key Configuration key
     * @param defaultValue Default value if key not found
     * @return Configuration value or default
     */
    public static long getLong(String key, long defaultValue) {
        return Config.getLong(key, defaultValue);
    }

    /**
     * Gets a duration configuration value.
     * @param key Configuration key
     * @param defaultValue Default value if key not found
     * @return Duration value or default
     */
    public static Duration getDuration(String key, String defaultValue) {
        return Config.getDuration(key, defaultValue);
    }
}
