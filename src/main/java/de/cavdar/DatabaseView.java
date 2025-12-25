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
 * Allows connecting to various databases, saving connections, and executing SQL queries.
 *
 * @author StandardMDIGUI
 * @version 1.2
 * @since 2024-12-24
 */
public class DatabaseView extends BaseView implements ConnectionManager.ConnectionListener {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseView.class);
    private static final String NEW_CONNECTION = "<Neue Verbindung>";

    private JComboBox<String> cbConnections;
    private JTextField txtConnectionName;
    private JTextField txtUrl;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbDriver;
    private JButton btnConnect;
    private JButton btnSave;
    private JButton btnDelete;
    private JLabel lblStatus;

    private JTextArea txtQuery;
    private JButton btnExecute;
    private JButton btnClear;

    private JTable tblResults;
    private DefaultTableModel tableModel;
    private JLabel lblRowCount;

    private Connection connection;

    private static final String[] DRIVERS = {
            "org.postgresql.Driver",
            "com.mysql.cj.jdbc.Driver",
            "oracle.jdbc.OracleDriver",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "org.h2.Driver",
            "org.sqlite.JDBC"
    };

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

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createConnectionPanel(), BorderLayout.NORTH);
        mainPanel.add(createQueryPanel(), BorderLayout.CENTER);
        mainPanel.add(createResultPanel(), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        loadSavedConnections();

        // Preselect connection if specified
        if (connectionName != null && !connectionName.isEmpty()) {
            cbConnections.setSelectedItem(connectionName);
        }

        // Register for connection changes
        ConnectionManager.addListener(this);

        LOG.debug("DatabaseView created");
    }

    @Override
    public void onConnectionsChanged() {
        // Reload connections when they change externally
        SwingUtilities.invokeLater(this::loadSavedConnections);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Verbindung"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Saved Connections ComboBox
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Verbindung:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        cbConnections = new JComboBox<>();
        cbConnections.addActionListener(e -> onConnectionSelected());
        panel.add(cbConnections, gbc);

        // Connection Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtConnectionName = new JTextField(30);
        panel.add(txtConnectionName, gbc);

        // Driver
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Treiber:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        cbDriver = new JComboBox<>(DRIVERS);
        cbDriver.setEditable(true);
        panel.add(cbDriver, gbc);

        // URL
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("JDBC-URL:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtUrl = new JTextField("jdbc:postgresql://localhost:5432/postgres", 40);
        panel.add(txtUrl, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Benutzer:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtUsername = new JTextField("postgres", 20);
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Passwort:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtPassword = new JPasswordField(20);
        panel.add(txtPassword, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnConnect = new JButton("Verbinden");
        btnConnect.addActionListener(e -> toggleConnection());
        buttonPanel.add(btnConnect);

        btnSave = new JButton("Speichern");
        btnSave.addActionListener(e -> saveCurrentConnection());
        buttonPanel.add(btnSave);

        btnDelete = new JButton("Löschen");
        btnDelete.addActionListener(e -> deleteCurrentConnection());
        buttonPanel.add(btnDelete);

        lblStatus = new JLabel("Nicht verbunden");
        lblStatus.setForeground(Color.RED);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(lblStatus);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("SQL-Abfrage"));

        txtQuery = new JTextArea(5, 60);
        txtQuery.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtQuery.setText("SELECT * FROM ");
        panel.add(new JScrollPane(txtQuery), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnExecute = new JButton("Ausführen");
        btnExecute.addActionListener(e -> executeQuery());
        btnExecute.setEnabled(false);
        buttonPanel.add(btnExecute);

        btnClear = new JButton("Leeren");
        btnClear.addActionListener(e -> {
            txtQuery.setText("");
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            lblRowCount.setText("0 Zeilen");
        });
        buttonPanel.add(btnClear);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Ergebnisse"));

        tableModel = new DefaultTableModel();
        tblResults = new JTable(tableModel);
        tblResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(tblResults);
        scrollPane.setPreferredSize(new Dimension(850, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        lblRowCount = new JLabel("0 Zeilen");
        panel.add(lblRowCount, BorderLayout.SOUTH);

        return panel;
    }

    private void onConnectionSelected() {
        String selected = (String) cbConnections.getSelectedItem();
        if (selected == null || selected.equals(NEW_CONNECTION)) {
            // Clear fields for new connection
            txtConnectionName.setText("");
            txtConnectionName.setEditable(true);
            cbDriver.setSelectedIndex(0);
            txtUrl.setText("jdbc:postgresql://localhost:5432/postgres");
            txtUsername.setText("postgres");
            txtPassword.setText("");
            btnDelete.setEnabled(false);
        } else {
            // Load selected connection from ConnectionManager
            ConnectionInfo conn = ConnectionManager.getConnection(selected);
            if (conn != null) {
                txtConnectionName.setText(conn.getName());
                txtConnectionName.setEditable(false);
                cbDriver.setSelectedItem(conn.getDriver());
                txtUrl.setText(conn.getUrl());
                txtUsername.setText(conn.getUsername());
                txtPassword.setText(conn.getPassword());
                btnDelete.setEnabled(true);
            }
        }
    }

    private void loadSavedConnections() {
        String currentSelection = (String) cbConnections.getSelectedItem();

        cbConnections.removeAllItems();
        cbConnections.addItem(NEW_CONNECTION);

        List<ConnectionInfo> connections = ConnectionManager.getConnections();
        for (ConnectionInfo conn : connections) {
            cbConnections.addItem(conn.getName());
        }

        // Restore selection or select first connection
        if (currentSelection != null && !currentSelection.equals(NEW_CONNECTION)) {
            cbConnections.setSelectedItem(currentSelection);
        } else if (connections.size() > 0) {
            cbConnections.setSelectedIndex(1);
        }

        LOG.debug("Loaded {} saved connections", connections.size());
    }

    private void saveCurrentConnection() {
        String name = txtConnectionName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte geben Sie einen Namen für die Verbindung ein.",
                    "Name erforderlich", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String driver = (String) cbDriver.getSelectedItem();
        String url = txtUrl.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        ConnectionInfo conn = new ConnectionInfo(name, driver, url, username, password);
        ConnectionManager.saveConnection(conn);

        // Reload and select
        loadSavedConnections();
        cbConnections.setSelectedItem(name);
        txtConnectionName.setEditable(false);
        btnDelete.setEnabled(true);

        JOptionPane.showMessageDialog(this, "Verbindung '" + name + "' wurde gespeichert.",
                "Gespeichert", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteCurrentConnection() {
        String selected = (String) cbConnections.getSelectedItem();
        if (selected == null || selected.equals(NEW_CONNECTION)) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Verbindung '" + selected + "' wirklich löschen?",
                "Löschen bestätigen", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            ConnectionManager.deleteConnection(selected);
            loadSavedConnections();
            cbConnections.setSelectedItem(NEW_CONNECTION);
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
        String driver = (String) cbDriver.getSelectedItem();
        String url = txtUrl.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        executeTask(() -> {
            try {
                LOG.info("Connecting to database: {}", url);
                Class.forName(driver);
                connection = DriverManager.getConnection(url, username, password);

                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Verbunden: " + url);
                    lblStatus.setForeground(new Color(0, 128, 0));
                    btnConnect.setText("Trennen");
                    btnExecute.setEnabled(true);
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
            lblStatus.setText("Nicht verbunden");
            lblStatus.setForeground(Color.RED);
            btnConnect.setText("Verbinden");
            btnExecute.setEnabled(false);
        }
    }

    private void executeQuery() {
        String sql = txtQuery.getText().trim();
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
                            tableModel.setDataVector(data, columnNames);
                            lblRowCount.setText(rowCount + " Zeilen (" + duration + " ms)");
                            LOG.info("Query returned {} rows in {} ms", rowCount, duration);
                        });
                    }
                } else {
                    int updateCount = stmt.getUpdateCount();
                    long duration = System.currentTimeMillis() - startTime;

                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        tableModel.setColumnCount(0);
                        lblRowCount.setText(updateCount + " Zeilen betroffen (" + duration + " ms)");
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
                    lblRowCount.setText("Fehler: " + e.getMessage());
                });
            }
        });
    }

    @Override
    protected void setupViewToolbar(JToolBar tb) {
        // Toolbar is empty for DatabaseView, connection controls are in the panel
    }

    @Override
    public void dispose() {
        ConnectionManager.removeListener(this);
        disconnect();
        super.dispose();
    }
}
