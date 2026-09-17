package com.econovafx.modules.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Utility to persist external application properties in the user's home
 * directory (e.g., ~/.econovafx/econovafx.properties).
 */
public final class ConfigFileUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFileUtil.class);

    private ConfigFileUtil() {
    }

    public static Path getDefaultConfigDir() {
        String userHome = System.getProperty("user.home");
        return Path.of(userHome, ".econovafx");
    }

    public static Path getDefaultConfigFile() {
        return getDefaultConfigDir().resolve("econovafx.properties");
    }

    public static Path ensureConfigDir() throws IOException {
        Path dir = getDefaultConfigDir();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            logger.info("Created config directory {}", dir);
        }
        return dir;
    }

    public static Path saveProperties(Properties props) throws IOException {
        Path dir = ensureConfigDir();
        Path file = dir.resolve("econovafx.properties");
        try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(out, "EconoNovaFX external configuration (created by setup assistant)");
        }
        logger.info("Saved external properties to {}", file);
        return file;
    }
}
