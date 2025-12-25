package de.cavdar.model;

import de.cavdar.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Property groups for organized config file output.
     */
    private static final Map<String, List<String>> PROPERTY_GROUPS;

    static {
        PROPERTY_GROUPS = new LinkedHashMap<>();

        PROPERTY_GROUPS.put("WINDOW - Window position and size", List.of(
                "LAST_WINDOW_HEIGHT",
                "LAST_WINDOW_WIDTH",
                "LAST_WINDOW_X_POS",
                "LAST_WINDOW_Y_POS"
        ));

        PROPERTY_GROUPS.put("LATEST - Last used values and selections", List.of(
                "LAST_CFG_FILENAME",
                "LAST_CFG_FILENAMES_LIST",
                "LAST_DB_CONNECTION",
                "LAST_ITSQ_REVISION",
                "LAST_LOAD_PATH",
                "LAST_LOOK_AND_FEEL_CLASS",
                "LAST_TEST_SOURCE",
                "LAST_TEST_TYPE"
                ));

        PROPERTY_GROUPS.put("FLAGS - Boolean settings", List.of(
                "ADMIN_FUNCS_ENABLED",
                "CHECK-EXPORT-PROTOKOLL-ACTIVE",
                "DUMP_IN_REST_CLIENT",
                "SFTP_UPLOAD_ACTIVE",
                "LAST_UPLOAD_SYNTHETICS",
                "LAST_USE_ONLY_TEST_CLZ"
        ));

        PROPERTY_GROUPS.put("CUSTOMERS - Customer configuration", List.of(
                "AVAILABLE_CUSTOMERS",
                "CUSTOMER_CUST01",
                "CUSTOMER_CUST02",
                "CUSTOMER_CUST03",
                "CUSTOMER_CUST04"
        ));

        PROPERTY_GROUPS.put("URL - Service URLs", List.of(
                "ACTIVITI_URLS",
                "MASTERKONSOLE_URLS"
        ));

        PROPERTY_GROUPS.put("DATABASE - Database connections", List.of(
                "DB_CONNECTIONS"
        ));

        PROPERTY_GROUPS.put("TESTS - Test configuration", List.of(
                "ITSQ_REVISIONS",
                "ITSQ_TAG_NAME_FORMAT",
                "MAX_CUSTOMERS_PER_TEST",
                "TEST-BASE-PATH",
                "TEST-SOURCES",
                "TEST-TYPES"
        ));

        PROPERTY_GROUPS.put("TIMINGS - Time delays and intervals", List.of(
                "TIME_BEFORE_BTLG_IMPORT",
                "TIME_BEFORE_CT_IMPORT",
                "TIME_BEFORE_EXPORT",
                "TIME_BEFORE_INSO_EXPORTS",
                "TIME_BEFORE_SFTP_COLLECT"
        ));
    }

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
     * Saves the current configuration to file with grouped sections.
     *
     * @throws ConfigurationException if saving fails
     */
    public void save() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.ISO_8859_1))) {

            // Write header
            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write("# StandardMDIGUI Configuration");
            writer.newLine();
            writer.write("# Updated: " + timestamp);
            writer.newLine();
            writer.newLine();

            // Write each group
            for (Map.Entry<String, List<String>> group : PROPERTY_GROUPS.entrySet()) {
                writer.write("# ==============================================================================");
                writer.newLine();
                writer.write("# " + group.getKey());
                writer.newLine();
                writer.write("# ==============================================================================");
                writer.newLine();

                for (String key : group.getValue()) {
                    String value = props.getProperty(key);
                    if (value != null) {
                        writer.write(escapeKey(key) + "=" + escapeValue(value));
                        writer.newLine();
                    }
                }
                writer.newLine();
            }

            // Write any remaining properties not in groups
            boolean hasUnknown = false;
            for (String key : props.stringPropertyNames()) {
                boolean found = PROPERTY_GROUPS.values().stream()
                        .anyMatch(list -> list.contains(key));
                if (!found) {
                    if (!hasUnknown) {
                        writer.write("# ==============================================================================");
                        writer.newLine();
                        writer.write("# OTHER - Uncategorized properties");
                        writer.newLine();
                        writer.write("# ==============================================================================");
                        writer.newLine();
                        hasUnknown = true;
                    }
                    writer.write(escapeKey(key) + "=" + escapeValue(props.getProperty(key)));
                    writer.newLine();
                }
            }

            LOG.info("Configuration saved to {}", FILE_PATH);
        } catch (IOException e) {
            LOG.error("Failed to save configuration to {}", FILE_PATH, e);
            throw new ConfigurationException("Failed to save configuration", e);
        }
    }

    /**
     * Escapes special characters in property keys.
     */
    private String escapeKey(String key) {
        return key.replace(":", "\\:")
                .replace("=", "\\=")
                .replace(" ", "\\ ");
    }

    /**
     * Escapes special characters in property values.
     */
    private String escapeValue(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\t' -> sb.append("\\t");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case ':' -> sb.append("\\:");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
