package de.cavdar;

import de.cavdar.util.ConnectionInfo;
import de.cavdar.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.Vector;

/**
 * Database view with generic JDBC support and connection management.
 * Uses DatabaseViewPanel for GUI, this class contains only logic.
 *
 * Pattern:
 * - DatabaseViewPanel: GUI only (can be GUI designer generated)
 * - DatabaseView: Logic and event handlers only
 *
 * @author StandardMDIGUI
 * @version 2.0
 * @since 2024-12-25
 */
public class DatabaseView extends BaseView implements ConnectionManager.ConnectionListener {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseView.class);
    private static final String NEW_CONNECTION = "<Neue Verbindung>";

    private DatabaseViewPanel dbPanel;
    private Connection connection;
    private String preselectedConnection;

    /**
     * Constructs a new DatabaseView.
     */
    public DatabaseView() {
        this(null);
    }

    /**
     * Constructs a new DatabaseView with a preselected connection.
     *
     * @param connectionName the connection name to preselect, or null for default
     */
    public DatabaseView(String connectionName) {
        super("Datenbank");
        setSize(900, 700);
        this.preselectedConnection = connectionName;

        // Load saved connections after panel is created
        loadSavedConnections();

        // Preselect connection if specified
        if (connectionName != null && !connectionName.isEmpty()) {
            dbPanel.getConnectionsComboBox().setSelectedItem(connectionName);
        }

        // Register for connection changes
        ConnectionManager.addListener(this);

        LOG.debug("DatabaseView created");
    }

    @Override
    protected BaseViewPanel createPanel() {
        dbPanel = new DatabaseViewPanel();
        return dbPanel;
    }

    @Override
    protected void setupToolbarActions() {
        // Toolbar is empty for DatabaseView, all controls are in the panel
    }

    @Override
    protected void setupListeners() {
        // Connection selection
        dbPanel.getConnectionsComboBox().addActionListener(e -> onConnectionSelected());

        // Connection buttons
        dbPanel.getConnectButton().addActionListener(e -> toggleConnection());
        dbPanel.getSaveButton().addActionListener(e -> saveCurrentConnection());
        dbPanel.getDeleteButton().addActionListener(e -> deleteCurrentConnection());

        // Query buttons
        dbPanel.getExecuteButton().addActionListener(e -> executeQuery());
        dbPanel.getClearButton().addActionListener(e -> clearQuery());
    }

    @Override
    public void onConnectionsChanged() {
        // Reload connections when they change externally
        SwingUtilities.invokeLater(this::loadSavedConnections);
    }

    // ===== Connection Logic =====

    private void onConnectionSelected() {
        String selected = (String) dbPanel.getConnectionsComboBox().getSelectedItem();
        if (selected == null || selected.equals(NEW_CONNECTION)) {
            // Clear fields for new connection
            dbPanel.getConnectionNameField().setText("");
            dbPanel.getConnectionNameField().setEditable(true);
            dbPanel.getDriverComboBox().setSelectedIndex(0);
            dbPanel.getUrlField().setText("jdbc:postgresql://localhost:5432/postgres");
            dbPanel.getUsernameField().setText("postgres");
            dbPanel.getPasswordField().setText("");
            dbPanel.getDeleteButton().setEnabled(false);
        } else {
            // Load selected connection from ConnectionManager
            ConnectionInfo conn = ConnectionManager.getConnection(selected);
            if (conn != null) {
                dbPanel.getConnectionNameField().setText(conn.getName());
                dbPanel.getConnectionNameField().setEditable(false);
                dbPanel.getDriverComboBox().setSelectedItem(conn.getDriver());
                dbPanel.getUrlField().setText(conn.getUrl());
                dbPanel.getUsernameField().setText(conn.getUsername());
                dbPanel.getPasswordField().setText(conn.getPassword());
                dbPanel.getDeleteButton().setEnabled(true);
            }
        }
    }

    private void loadSavedConnections() {
        JComboBox<String> cb = dbPanel.getConnectionsComboBox();
        String currentSelection = (String) cb.getSelectedItem();

        cb.removeAllItems();
        cb.addItem(NEW_CONNECTION);

        List<ConnectionInfo> connections = ConnectionManager.getConnections();
        for (ConnectionInfo conn : connections) {
            cb.addItem(conn.getName());
        }

        // Restore selection or select first connection
        if (currentSelection != null && !currentSelection.equals(NEW_CONNECTION)) {
            cb.setSelectedItem(currentSelection);
        } else if (connections.size() > 0) {
            cb.setSelectedIndex(1);
        }

        LOG.debug("Loaded {} saved connections", connections.size());
    }

    private void saveCurrentConnection() {
        String name = dbPanel.getConnectionNameField().getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte geben Sie einen Namen für die Verbindung ein.",
                    "Name erforderlich", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String driver = (String) dbPanel.getDriverComboBox().getSelectedItem();
        String url = dbPanel.getUrlField().getText().trim();
        String username = dbPanel.getUsernameField().getText().trim();
        String password = new String(dbPanel.getPasswordField().getPassword());

        ConnectionInfo conn = new ConnectionInfo(name, driver, url, username, password);
        ConnectionManager.saveConnection(conn);

        // Reload and select
        loadSavedConnections();
        dbPanel.getConnectionsComboBox().setSelectedItem(name);
        dbPanel.getConnectionNameField().setEditable(false);
        dbPanel.getDeleteButton().setEnabled(true);

        JOptionPane.showMessageDialog(this, "Verbindung '" + name + "' wurde gespeichert.",
                "Gespeichert", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteCurrentConnection() {
        String selected = (String) dbPanel.getConnectionsComboBox().getSelectedItem();
        if (selected == null || selected.equals(NEW_CONNECTION)) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Verbindung '" + selected + "' wirklich löschen?",
                "Löschen bestätigen", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            ConnectionManager.deleteConnection(selected);
            loadSavedConnections();
            dbPanel.getConnectionsComboBox().setSelectedItem(NEW_CONNECTION);
        }
    }

    private void toggleConnection() {
        if (connection == null) {
            connect();
        } else {
            disconnect();
        }
    }

    private void connect() {
        String driver = (String) dbPanel.getDriverComboBox().getSelectedItem();
        String url = dbPanel.getUrlField().getText().trim();
        String username = dbPanel.getUsernameField().getText().trim();
        String password = new String(dbPanel.getPasswordField().getPassword());

        executeTask(() -> {
            try {
                LOG.info("Connecting to database: {}", url);
                Class.forName(driver);
                connection = DriverManager.getConnection(url, username, password);

                SwingUtilities.invokeLater(() -> {
                    dbPanel.getStatusLabel().setText("Verbunden: " + url);
                    dbPanel.getStatusLabel().setForeground(new Color(0, 128, 0));
                    dbPanel.getConnectButton().setText("Trennen");
                    dbPanel.getExecuteButton().setEnabled(true);
                    LOG.info("Database connection established");
                });
            } catch (ClassNotFoundException e) {
                LOG.error("JDBC driver not found: {}", driver, e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(DatabaseView.this,
                            "JDBC-Treiber nicht gefunden: " + driver + "\n\n" +
                                    "Bitte stellen Sie sicher, dass die Treiber-JAR im Classpath ist.",
                            "Treiber-Fehler",
                            JOptionPane.ERROR_MESSAGE);
                });
            } catch (SQLException e) {
                LOG.error("Database connection failed", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(DatabaseView.this,
                            "Verbindungsfehler: " + e.getMessage(),
                            "Verbindungsfehler",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOG.info("Database connection closed");
            }
        } catch (SQLException e) {
            LOG.error("Error closing database connection", e);
        } finally {
            connection = null;
            dbPanel.getStatusLabel().setText("Nicht verbunden");
            dbPanel.getStatusLabel().setForeground(Color.RED);
            dbPanel.getConnectButton().setText("Verbinden");
            dbPanel.getExecuteButton().setEnabled(false);
        }
    }

    // ===== Query Logic =====

    private void executeQuery() {
        String sql = dbPanel.getQueryArea().getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte geben Sie eine SQL-Abfrage ein.");
            return;
        }

        executeTask(() -> {
            long startTime = System.currentTimeMillis();
            LOG.info("Executing SQL: {}", sql);

            try (Statement stmt = connection.createStatement()) {
                boolean isResultSet = stmt.execute(sql);

                if (isResultSet) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        ResultSetMetaData meta = rs.getMetaData();
                        int columnCount = meta.getColumnCount();

                        Vector<String> columnNames = new Vector<>();
                        for (int i = 1; i <= columnCount; i++) {
                            columnNames.add(meta.getColumnLabel(i));
                        }

                        Vector<Vector<Object>> data = new Vector<>();
                        while (rs.next()) {
                            Vector<Object> row = new Vector<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.add(rs.getObject(i));
                            }
                            data.add(row);
                        }

                        long duration = System.currentTimeMillis() - startTime;
                        int rowCount = data.size();

                        SwingUtilities.invokeLater(() -> {
                            dbPanel.getTableModel().setDataVector(data, columnNames);
                            dbPanel.getRowCountLabel().setText(rowCount + " Zeilen (" + duration + " ms)");
                            LOG.info("Query returned {} rows in {} ms", rowCount, duration);
                        });
                    }
                } else {
                    int updateCount = stmt.getUpdateCount();
                    long duration = System.currentTimeMillis() - startTime;

                    SwingUtilities.invokeLater(() -> {
                        dbPanel.getTableModel().setRowCount(0);
                        dbPanel.getTableModel().setColumnCount(0);
                        dbPanel.getRowCountLabel().setText(updateCount + " Zeilen betroffen (" + duration + " ms)");
                        JOptionPane.showMessageDialog(DatabaseView.this,
                                updateCount + " Zeilen wurden aktualisiert.",
                                "Ausführung erfolgreich",
                                JOptionPane.INFORMATION_MESSAGE);
                        LOG.info("Update affected {} rows in {} ms", updateCount, duration);
                    });
                }
            } catch (SQLException e) {
                LOG.error("SQL execution failed", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(DatabaseView.this,
                            "SQL-Fehler: " + e.getMessage(),
                            "Fehler",
                            JOptionPane.ERROR_MESSAGE);
                    dbPanel.getRowCountLabel().setText("Fehler: " + e.getMessage());
                });
            }
        });
    }

    private void clearQuery() {
        dbPanel.getQueryArea().setText("");
        dbPanel.getTableModel().setRowCount(0);
        dbPanel.getTableModel().setColumnCount(0);
        dbPanel.getRowCountLabel().setText("0 Zeilen");
    }

    // ===== Getters =====

    public DatabaseViewPanel getDatabasePanel() {
        return dbPanel;
    }

    @Override
    public void dispose() {
        ConnectionManager.removeListener(this);
        disconnect();
        super.dispose();
    }
}
