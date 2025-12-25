package de.cavdar.util;

import de.cavdar.model.ConfigEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ConfigEntry record.
 */
class ConfigEntryTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create entry with key and value")
        void shouldCreateEntryWithKeyAndValue() {
            ConfigEntry entry = new ConfigEntry("testKey", "testValue");

            assertThat(entry.key()).isEqualTo("testKey");
            assertThat(entry.value()).isEqualTo("testValue");
        }

        @Test
        @DisplayName("should throw exception when key is null")
        void shouldThrowExceptionWhenKeyIsNull() {
            assertThatThrownBy(() -> new ConfigEntry(null, "value"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Key cannot be null");
        }

        @Test
        @DisplayName("should handle null value as empty string")
        void shouldHandleNullValueAsEmptyString() {
            ConfigEntry entry = new ConfigEntry("key", null);

            assertThat(entry.value()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("cleanValue Tests")
    class CleanValueTests {

        @Test
        @DisplayName("should return value without comments")
        void shouldReturnValueWithoutComments() {
            ConfigEntry entry = new ConfigEntry("key", "value # this is a comment");

            assertThat(entry.cleanValue()).isEqualTo("value");
        }

        @Test
        @DisplayName("should trim whitespace")
        void shouldTrimWhitespace() {
            ConfigEntry entry = new ConfigEntry("key", "  value  ");

            assertThat(entry.cleanValue()).isEqualTo("value");
        }

        @Test
        @DisplayName("should handle value without comments")
        void shouldHandleValueWithoutComments() {
            ConfigEntry entry = new ConfigEntry("key", "simpleValue");

            assertThat(entry.cleanValue()).isEqualTo("simpleValue");
        }
    }

    @Nested
    @DisplayName("asArray Tests")
    class AsArrayTests {

        @Test
        @DisplayName("should split by semicolon")
        void shouldSplitBySemicolon() {
            ConfigEntry entry = new ConfigEntry("key", "a;b;c");

            assertThat(entry.asArray()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("should return empty array for empty value")
        void shouldReturnEmptyArrayForEmptyValue() {
            ConfigEntry entry = new ConfigEntry("key", "");

            assertThat(entry.asArray()).isEmpty();
        }

        @Test
        @DisplayName("should handle value with comments")
        void shouldHandleValueWithComments() {
            ConfigEntry entry = new ConfigEntry("key", "a;b;c # comment");

            assertThat(entry.asArray()).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("asBoolean Tests")
    class AsBooleanTests {

        @Test
        @DisplayName("should return true for 'true'")
        void shouldReturnTrueForTrue() {
            ConfigEntry entry = new ConfigEntry("key", "true");

            assertThat(entry.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("should return false for 'false'")
        void shouldReturnFalseForFalse() {
            ConfigEntry entry = new ConfigEntry("key", "false");

            assertThat(entry.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("should return false for invalid value")
        void shouldReturnFalseForInvalidValue() {
            ConfigEntry entry = new ConfigEntry("key", "notABoolean");

            assertThat(entry.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("should handle value with comments")
        void shouldHandleValueWithComments() {
            ConfigEntry entry = new ConfigEntry("key", "true # enabled");

            assertThat(entry.asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("asInt Tests")
    class AsIntTests {

        @Test
        @DisplayName("should parse valid integer")
        void shouldParseValidInteger() {
            ConfigEntry entry = new ConfigEntry("key", "42");

            assertThat(entry.asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("should parse negative integer")
        void shouldParseNegativeInteger() {
            ConfigEntry entry = new ConfigEntry("key", "-100");

            assertThat(entry.asInt()).isEqualTo(-100);
        }

        @Test
        @DisplayName("should throw exception for invalid integer")
        void shouldThrowExceptionForInvalidInteger() {
            ConfigEntry entry = new ConfigEntry("key", "notANumber");

            assertThatThrownBy(entry::asInt)
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("should return default value for invalid integer")
        void shouldReturnDefaultValueForInvalidInteger() {
            ConfigEntry entry = new ConfigEntry("key", "notANumber");

            assertThat(entry.asInt(99)).isEqualTo(99);
        }

        @Test
        @DisplayName("should handle value with comments")
        void shouldHandleValueWithComments() {
            ConfigEntry entry = new ConfigEntry("key", "123 # max value");

            assertThat(entry.asInt()).isEqualTo(123);
        }
    }

    @Nested
    @DisplayName("asDouble Tests")
    class AsDoubleTests {

        @Test
        @DisplayName("should parse valid double")
        void shouldParseValidDouble() {
            ConfigEntry entry = new ConfigEntry("key", "3.14");

            assertThat(entry.asDouble()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("should return default value for invalid double")
        void shouldReturnDefaultValueForInvalidDouble() {
            ConfigEntry entry = new ConfigEntry("key", "notANumber");

            assertThat(entry.asDouble(1.5)).isEqualTo(1.5);
        }
    }

    @Nested
    @DisplayName("isEmpty Tests")
    class IsEmptyTests {

        @Test
        @DisplayName("should return true for empty value")
        void shouldReturnTrueForEmptyValue() {
            ConfigEntry entry = new ConfigEntry("key", "");

            assertThat(entry.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should return true for whitespace-only value")
        void shouldReturnTrueForWhitespaceOnlyValue() {
            ConfigEntry entry = new ConfigEntry("key", "   ");

            assertThat(entry.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should return false for non-empty value")
        void shouldReturnFalseForNonEmptyValue() {
            ConfigEntry entry = new ConfigEntry("key", "value");

            assertThat(entry.isEmpty()).isFalse();
        }
    }

    @Test
    @DisplayName("toString should return key=value format")
    void toStringShouldReturnKeyValueFormat() {
        ConfigEntry entry = new ConfigEntry("myKey", "myValue");

        assertThat(entry.toString()).isEqualTo("myKey=myValue");
    }
}
