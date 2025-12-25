package de.cavdar;

import org.junit.jupiter.api.*;

import java.sql.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for KC_TEST database.
 * Requires PostgreSQL with KC_TEST database to be running.
 *
 * Prerequisites:
 * 1. PostgreSQL running on localhost:5432
 * 2. Database 'kc_test' created
 * 3. init_kc_test.sql executed
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseIntegrationTest {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/kc_test";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres"; // Adjust as needed

    private Connection connection;

    // Test data IDs for cleanup
    private int testCustomerId;
    private int testAddressId;
    private int testOrderId;

    @BeforeAll
    void setUp() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            fail("PostgreSQL JDBC Driver not found");
        }

        connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        assertThat(connection).isNotNull();
        assertThat(connection.isClosed()).isFalse();

        // Clean up any previous test data
        cleanupTestData();
    }

    @AfterAll
    void tearDown() throws SQLException {
        cleanupTestData();

        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void cleanupTestData() throws SQLException {
        // Delete test data by customer_number pattern
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM orders WHERE customer_id IN (SELECT id FROM customer WHERE customer_number LIKE 'TEST-%')");
            stmt.executeUpdate("DELETE FROM address WHERE customer_id IN (SELECT id FROM customer WHERE customer_number LIKE 'TEST-%')");
            stmt.executeUpdate("DELETE FROM customer WHERE customer_number LIKE 'TEST-%'");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Insert test customer")
    void shouldInsertCustomer() throws SQLException {
        String sql = """
            INSERT INTO customer (customer_number, first_name, last_name, email, phone)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "TEST-001");
            pstmt.setString(2, "Test");
            pstmt.setString(3, "User");
            pstmt.setString(4, "test.user@junit.test");
            pstmt.setString(5, "+49 111 222333");

            ResultSet rs = pstmt.executeQuery();
            assertThat(rs.next()).isTrue();
            testCustomerId = rs.getInt("id");
            assertThat(testCustomerId).isGreaterThan(0);
        }

        // Verify insertion
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM customer WHERE customer_number = ?")) {
            pstmt.setString(1, "TEST-001");
            ResultSet rs = pstmt.executeQuery();

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("first_name")).isEqualTo("Test");
            assertThat(rs.getString("last_name")).isEqualTo("User");
            assertThat(rs.getString("email")).isEqualTo("test.user@junit.test");
            assertThat(rs.getBoolean("active")).isTrue();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Insert test address for customer")
    void shouldInsertAddress() throws SQLException {
        assertThat(testCustomerId).as("Customer must be created first").isGreaterThan(0);

        String sql = """
            INSERT INTO address (customer_id, address_type, street, house_number, postal_code, city, country, is_default)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, testCustomerId);
            pstmt.setString(2, "BOTH");
            pstmt.setString(3, "Teststraße");
            pstmt.setString(4, "42");
            pstmt.setString(5, "99999");
            pstmt.setString(6, "Teststadt");
            pstmt.setString(7, "Deutschland");
            pstmt.setBoolean(8, true);

            ResultSet rs = pstmt.executeQuery();
            assertThat(rs.next()).isTrue();
            testAddressId = rs.getInt("id");
            assertThat(testAddressId).isGreaterThan(0);
        }

        // Verify insertion
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM address WHERE id = ?")) {
            pstmt.setInt(1, testAddressId);
            ResultSet rs = pstmt.executeQuery();

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("street")).isEqualTo("Teststraße");
            assertThat(rs.getString("city")).isEqualTo("Teststadt");
            assertThat(rs.getString("address_type")).isEqualTo("BOTH");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Insert test order")
    void shouldInsertOrder() throws SQLException {
        assertThat(testCustomerId).as("Customer must be created first").isGreaterThan(0);
        assertThat(testAddressId).as("Address must be created first").isGreaterThan(0);

        String sql = """
            INSERT INTO orders (order_number, customer_id, shipping_address_id, billing_address_id, status, total_amount, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "TEST-ORD-001");
            pstmt.setInt(2, testCustomerId);
            pstmt.setInt(3, testAddressId);
            pstmt.setInt(4, testAddressId);
            pstmt.setString(5, "NEW");
            pstmt.setBigDecimal(6, new java.math.BigDecimal("199.99"));
            pstmt.setString(7, "JUnit Test Order");

            ResultSet rs = pstmt.executeQuery();
            assertThat(rs.next()).isTrue();
            testOrderId = rs.getInt("id");
            assertThat(testOrderId).isGreaterThan(0);
        }

        // Verify insertion
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM orders WHERE order_number = ?")) {
            pstmt.setString(1, "TEST-ORD-001");
            ResultSet rs = pstmt.executeQuery();

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("status")).isEqualTo("NEW");
            assertThat(rs.getBigDecimal("total_amount")).isEqualByComparingTo("199.99");
            assertThat(rs.getString("notes")).isEqualTo("JUnit Test Order");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Insert multiple customers in batch")
    void shouldInsertMultipleCustomers() throws SQLException {
        String sql = """
            INSERT INTO customer (customer_number, first_name, last_name, email)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Batch insert
            String[][] testData = {
                {"TEST-002", "Alice", "Wonderland", "alice@junit.test"},
                {"TEST-003", "Bob", "Builder", "bob@junit.test"},
                {"TEST-004", "Charlie", "Chocolate", "charlie@junit.test"}
            };

            for (String[] data : testData) {
                pstmt.setString(1, data[0]);
                pstmt.setString(2, data[1]);
                pstmt.setString(3, data[2]);
                pstmt.setString(4, data[3]);
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            assertThat(results).hasSize(3);
            assertThat(results).containsOnly(1); // Each insert affected 1 row
        }

        // Verify count
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM customer WHERE customer_number LIKE 'TEST-%'");
            rs.next();
            assertThat(rs.getInt(1)).isEqualTo(4); // TEST-001 + 3 batch inserts
        }
    }

    @Test
    @Order(5)
    @DisplayName("Update order status")
    void shouldUpdateOrderStatus() throws SQLException {
        assertThat(testOrderId).as("Order must be created first").isGreaterThan(0);

        String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "CONFIRMED");
            pstmt.setInt(2, testOrderId);

            int rowsAffected = pstmt.executeUpdate();
            assertThat(rowsAffected).isEqualTo(1);
        }

        // Verify update
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT status FROM orders WHERE id = ?")) {
            pstmt.setInt(1, testOrderId);
            ResultSet rs = pstmt.executeQuery();

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("status")).isEqualTo("CONFIRMED");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Query customer orders view")
    void shouldQueryCustomerOrdersView() throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM v_customer_orders WHERE customer_number = ?")) {
            pstmt.setString(1, "TEST-001");
            ResultSet rs = pstmt.executeQuery();

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("customer_name")).isEqualTo("Test User");
            assertThat(rs.getString("order_number")).isEqualTo("TEST-ORD-001");
            assertThat(rs.getString("status")).isEqualTo("CONFIRMED");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Query with JOIN")
    void shouldQueryWithJoin() throws SQLException {
        String sql = """
            SELECT c.customer_number, c.first_name, c.last_name,
                   a.street, a.city,
                   o.order_number, o.total_amount
            FROM customer c
            JOIN address a ON c.id = a.customer_id
            JOIN orders o ON c.id = o.customer_id
            WHERE c.customer_number = ?
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "TEST-001");
            ResultSet rs = pstmt.executeQuery();

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("first_name")).isEqualTo("Test");
            assertThat(rs.getString("street")).isEqualTo("Teststraße");
            assertThat(rs.getString("order_number")).isEqualTo("TEST-ORD-001");
        }
    }

    @Test
    @Order(8)
    @DisplayName("Transaction rollback test")
    void shouldRollbackTransaction() throws SQLException {
        connection.setAutoCommit(false);

        try {
            // Insert a customer
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO customer (customer_number, first_name, last_name, email) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, "TEST-ROLLBACK");
                pstmt.setString(2, "Rollback");
                pstmt.setString(3, "Test");
                pstmt.setString(4, "rollback@junit.test");
                pstmt.executeUpdate();
            }

            // Verify it exists within transaction
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM customer WHERE customer_number = 'TEST-ROLLBACK'");
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }

            // Rollback
            connection.rollback();

            // Verify it's gone after rollback
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM customer WHERE customer_number = 'TEST-ROLLBACK'");
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(0);
            }

        } finally {
            connection.setAutoCommit(true);
        }
    }
}
