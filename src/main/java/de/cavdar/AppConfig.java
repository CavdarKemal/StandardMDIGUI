package de.cavdar;

import de.cavdar.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Singleton configuration manager for the MDI application.
 * Manages properties from the config.properties file with support for
 * various data types (String, Array, Boolean, int).
 *
 * <p>This implementation uses eager initialization for thread-safety.</p>
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-24
 */
public class AppConfig {
    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);
    private static final String FILE_PATH = "config.properties";
    private static final AppConfig INSTANCE = new AppConfig();

    private final Properties props = new Properties();

    private AppConfig() {
        load();
    }

    /**
     * Returns the singleton instance of AppConfig.
     *
     * @return the singleton AppConfig instance
     */
    public static AppConfig getInstance() {
        return INSTANCE;
    }

    private void load() {
        try (InputStream is = new FileInputStream(FILE_PATH)) {
            props.load(is);
            LOG.info("Configuration loaded successfully from {}", FILE_PATH);
        } catch (FileNotFoundException e) {
            LOG.warn("Configuration file not found: {}. Using default values.", FILE_PATH);
            initializeDefaults();
        } catch (IOException e) {
            LOG.error("Failed to load configuration from {}", FILE_PATH, e);
            initializeDefaults();
        }
    }

    private void initializeDefaults() {
        LOG.info("Initializing default configuration values");
        props.setProperty("TEST-BASE-PATH", "/X-TESTS/ENE");
        props.setProperty("TEST-SOURCES", "ITSQ;LOCAL;REMOTE");
        props.setProperty("TEST-TYPES", "UNIT;INTEGRATION;E2E");
        props.setProperty("ITSQ_REVISIONS", "1.0;2.0;3.0");
        props.setProperty("AVAILABLE_CUSTOMERS", "CUST01,CUST02,CUST03,CUST04");
        props.setProperty("LAST_WINDOW_WIDTH", "1200");
        props.setProperty("LAST_WINDOW_HEIGHT", "800");
        props.setProperty("LAST_WINDOW_X_POS", "100");
        props.setProperty("LAST_WINDOW_Y_POS", "100");
    }

    /**
     * Sets a property value.
     *
     * @param key   the property key
     * @param value the property value
     */
    public void setProperty(String key, String value) {
        if (key == null || value == null) {
            LOG.warn("Attempted to set property with null key or value");
            return;
        }
        props.setProperty(key, value);
        LOG.debug("Property set: {} = {}", key, value);
    }

    /**
     * Gets a property value.
     *
     * @param key the property key
     * @return the property value, or empty string if not found
     */
    public String getProperty(String key) {
        return props.getProperty(key, "");
    }

    /**
     * Gets a property value as a semicolon-separated array.
     * Comments (text after #) are removed from values.
     *
     * @param key the property key
     * @return the property value as string array
     */
    public String[] getArray(String key) {
        String val = getPropertyX(key, "");
        return val.isEmpty() ? new String[0] : val.split(";");
    }

    /**
     * Gets a property value as boolean.
     *
     * @param key the property key
     * @return the boolean value, false if not found or invalid
     */
    public boolean getBool(String key) {
        return Boolean.parseBoolean(getPropertyX(key, "false"));
    }

    /**
     * Gets a property value as integer.
     *
     * @param key the property key
     * @param def the default value if property is not found or invalid
     * @return the integer value
     */
    public int getInt(String key, int def) {
        try {
            String propertyX = getPropertyX(key, String.valueOf(def));
            return Integer.parseInt(propertyX);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid integer value for key '{}', using default: {}", key, def);
            return def;
        }
    }

    private String getPropertyX(String key, String def) {
        String propValue = props.getProperty(key, def);
        String[] split = propValue.split("#");
        return split[0].trim();
    }

    /**
     * Saves the current configuration to file.
     *
     * @throws ConfigurationException if saving fails
     */
    public void save() {
        try (OutputStream os = new FileOutputStream(FILE_PATH)) {
            props.store(os, "Updated by MDI Application");
            LOG.info("Configuration saved to {}", FILE_PATH);
        } catch (IOException e) {
            LOG.error("Failed to save configuration to {}", FILE_PATH, e);
            throw new ConfigurationException("Failed to save configuration", e);
        }
    }
}
