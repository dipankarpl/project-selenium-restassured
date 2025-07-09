package com.yourorg.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LogManager.getLogger(ConfigLoader.class);
    private static Properties properties;
    private static final String CONFIG_PATH = "config/";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        properties = new Properties();
        String environment = EnvReader.get("ENVIRONMENT", "qa");
        String configFile = CONFIG_PATH + environment + "-config.properties";
        
        try {
            // Load environment-specific config
            properties.load(new FileInputStream(configFile));
            logger.info("Loaded configuration from: {}", configFile);
        } catch (IOException e) {
            logger.warn("Failed to load environment-specific config: {}. Loading default config.", e.getMessage());
            try {
                // Fallback to default config
                properties.load(new FileInputStream(CONFIG_PATH + "config.properties"));
                logger.info("Loaded default configuration");
            } catch (IOException ex) {
                logger.error("Failed to load default configuration: {}", ex.getMessage());
                throw new RuntimeException("Unable to load configuration files", ex);
            }
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for key {}: {}", key, value);
            }
        }
        return defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    public static void reloadConfig() {
        loadConfig();
    }
}