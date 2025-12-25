package de.cavdar;

import org.junit.jupiter.api.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AppConfig singleton.
 */
class AppConfigTest {

    private static Path tempConfigFile;
    private static Path originalConfigFile;

    @BeforeAll
    static void setupClass() throws IOException {
        // Backup original config if exists
        originalConfigFile = Path.of("config.properties");
        if (Files.exists(originalConfigFile)) {
            tempConfigFile = Path.of("config.properties.backup");
            Files.copy(originalConfigFile, tempConfigFile);
        }
    }

    @AfterAll
    static void teardownClass() throws IOException {
        // Restore original config
        if (tempConfigFile != null && Files.exists(tempConfigFile)) {
            Files.move(tempConfigFile, originalConfigFile);
        }
    }

    @Nested
    @DisplayName("Singleton Tests")
    class SingletonTests {

        @Test
        @DisplayName("getInstance should always return same instance")
        void getInstanceShouldReturnSameInstance() {
            AppConfig instance1 = AppConfig.getInstance();
            AppConfig instance2 = AppConfig.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("getInstance should not return null")
        void getInstanceShouldNotReturnNull() {
            AppConfig instance = AppConfig.getInstance();

            assertThat(instance).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property Operations")
    class PropertyOperations {

        @Test
        @DisplayName("setProperty and getProperty should work correctly")
        void setAndGetPropertyShouldWork() {
            AppConfig config = AppConfig.getInstance();

            config.setProperty("TEST_KEY", "TEST_VALUE");

            assertThat(config.getProperty("TEST_KEY")).isEqualTo("TEST_VALUE");
        }

        @Test
        @DisplayName("getProperty should return empty string for missing key")
        void getPropertyShouldReturnEmptyForMissingKey() {
            AppConfig config = AppConfig.getInstance();

            assertThat(config.getProperty("NON_EXISTENT_KEY_12345")).isEmpty();
        }

        @Test
        @DisplayName("setProperty should handle null gracefully")
        void setPropertyShouldHandleNull() {
            AppConfig config = AppConfig.getInstance();

            // Should not throw exception
            config.setProperty(null, "value");
            config.setProperty("key", null);
        }
    }

    @Nested
    @DisplayName("getArray Tests")
    class GetArrayTests {

        @Test
        @DisplayName("should split semicolon-separated values")
        void shouldSplitSemicolonSeparatedValues() {
            AppConfig config = AppConfig.getInstance();
            config.setProperty("ARRAY_TEST", "one;two;three");

            String[] result = config.getArray("ARRAY_TEST");

            assertThat(result).containsExactly("one", "two", "three");
        }

        @Test
        @DisplayName("should return empty array for missing key")
        void shouldReturnEmptyArrayForMissingKey() {
            AppConfig config = AppConfig.getInstance();

            String[] result = config.getArray("NON_EXISTENT_ARRAY");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should handle comments in values")
        void shouldHandleCommentsInValues() {
            AppConfig config = AppConfig.getInstance();
            config.setProperty("ARRAY_WITH_COMMENT", "a;b;c # comment");

            String[] result = config.getArray("ARRAY_WITH_COMMENT");

            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("getBool Tests")
    class GetBoolTests {

        @Test
        @DisplayName("should return true for 'true' value")
        void shouldReturnTrueForTrueValue() {
            AppConfig config = AppConfig.getInstance();
            config.setProperty("BOOL_TRUE", "true");

            assertThat(config.getBool("BOOL_TRUE")).isTrue();
        }

        @Test
        @DisplayName("should return false for 'false' value")
        void shouldReturnFalseForFalseValue() {
            AppConfig config = AppConfig.getInstance();
            config.setProperty("BOOL_FALSE", "false");

            assertThat(config.getBool("BOOL_FALSE")).isFalse();
        }

        @Test
        @DisplayName("should return false for missing key")
        void shouldReturnFalseForMissingKey() {
            AppConfig config = AppConfig.getInstance();

            assertThat(config.getBool("NON_EXISTENT_BOOL")).isFalse();
        }
    }

    @Nested
    @DisplayName("getInt Tests")
    class GetIntTests {

        @Test
        @DisplayName("should parse valid integer")
        void shouldParseValidInteger() {
            AppConfig config = AppConfig.getInstance();
            config.setProperty("INT_VALUE", "42");

            assertThat(config.getInt("INT_VALUE", 0)).isEqualTo(42);
        }

        @Test
        @DisplayName("should return default for invalid integer")
        void shouldReturnDefaultForInvalidInteger() {
            AppConfig config = AppConfig.getInstance();
            config.setProperty("INVALID_INT", "notANumber");

            assertThat(config.getInt("INVALID_INT", 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("should return default for missing key")
        void shouldReturnDefaultForMissingKey() {
            AppConfig config = AppConfig.getInstance();

            assertThat(config.getInt("NON_EXISTENT_INT", 123)).isEqualTo(123);
        }

        @Test
        @DisplayName("should handle comments in integer values")
        void shouldHandleCommentsInIntegerValues() {
            AppConfig config = AppConfig.getInstance();
            config.setProperty("INT_WITH_COMMENT", "500 # max size");

            assertThat(config.getInt("INT_WITH_COMMENT", 0)).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("save should persist properties to file")
        void saveShouldPersistProperties() throws IOException {
            AppConfig config = AppConfig.getInstance();
            String testValue = "test_" + System.currentTimeMillis();
            config.setProperty("SAVE_TEST", testValue);

            config.save();

            // Verify file was updated
            Properties props = new Properties();
            props.load(Files.newInputStream(Path.of("config.properties")));
            assertThat(props.getProperty("SAVE_TEST")).isEqualTo(testValue);
        }
    }
}
