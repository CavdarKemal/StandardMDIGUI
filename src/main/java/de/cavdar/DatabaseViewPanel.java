package de.cavdar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * GUI panel for DatabaseView - contains only layout and components.
 * No listeners or business logic.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-25
 */
public class DatabaseViewPanel extends BaseViewPanel {

    private static final String[] DRIVERS = {
            "org.postgresql.Driver",
            "com.mysql.cj.jdbc.Driver",
            "oracle.jdbc.OracleDriver",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "org.h2.Driver",
            "org.sqlite.JDBC"
    };

    // Connection panel components
    protected JPanel connectionPanel;
    protected JComboBox<String> cbConnections;
    protected JTextField txtConnectionName;
    protected JTextField txtUrl;
    protected JTextField txtUsername;
    protected JPasswordField txtPassword;
    protected JComboBox<String> cbDriver;
    protected JButton btnConnect;
    protected JButton btnSave;
    protected JButton btnDelete;
    protected JLabel lblStatus;

    // Query panel components
    protected JPanel queryPanel;
    protected JTextArea txtQuery;
    protected JButton btnExecute;
    protected JButton btnClear;

    // Result panel components
    protected JPanel resultPanel;
    protected JTable tblResults;
    protected DefaultTableModel tableModel;
    protected JLabel lblRowCount;

    public DatabaseViewPanel() {
        super();
        initCustomComponents();
    }

    /**
     * Initializes database-specific components.
     */
    protected void initCustomComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createConnectionPanel(), BorderLayout.NORTH);
        mainPanel.add(createQueryPanel(), BorderLayout.CENTER);
        mainPanel.add(createResultPanel(), BorderLayout.SOUTH);

        contentPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createConnectionPanel() {
        connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Verbindung"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Saved Connections ComboBox
        gbc.gridx = 0; gbc.gridy = 0;
        connectionPanel.add(new JLabel("Verbindung:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        cbConnections = new JComboBox<>();
        connectionPanel.add(cbConnections, gbc);

        // Connection Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        connectionPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtConnectionName = new JTextField(30);
        connectionPanel.add(txtConnectionName, gbc);

        // Driver
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.gridwidth = 1;
        connectionPanel.add(new JLabel("Treiber:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        cbDriver = new JComboBox<>(DRIVERS);
        cbDriver.setEditable(true);
        connectionPanel.add(cbDriver, gbc);

        // URL
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.gridwidth = 1;
        connectionPanel.add(new JLabel("JDBC-URL:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtUrl = new JTextField("jdbc:postgresql://localhost:5432/postgres", 40);
        connectionPanel.add(txtUrl, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.gridwidth = 1;
        connectionPanel.add(new JLabel("Benutzer:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtUsername = new JTextField("postgres", 20);
        connectionPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; gbc.gridwidth = 1;
        connectionPanel.add(new JLabel("Passwort:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtPassword = new JPasswordField(20);
        connectionPanel.add(txtPassword, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnConnect = new JButton("Verbinden");
        buttonPanel.add(btnConnect);

        btnSave = new JButton("Speichern");
        buttonPanel.add(btnSave);

        btnDelete = new JButton("Löschen");
        buttonPanel.add(btnDelete);

        lblStatus = new JLabel("Nicht verbunden");
        lblStatus.setForeground(Color.RED);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(lblStatus);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        connectionPanel.add(buttonPanel, gbc);

        return connectionPanel;
    }

    private JPanel createQueryPanel() {
        queryPanel = new JPanel(new BorderLayout(5, 5));
        queryPanel.setBorder(BorderFactory.createTitledBorder("SQL-Abfrage"));

        txtQuery = new JTextArea(5, 60);
        txtQuery.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtQuery.setText("SELECT * FROM ");
        queryPanel.add(new JScrollPane(txtQuery), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnExecute = new JButton("Ausführen");
        btnExecute.setEnabled(false);
        buttonPanel.add(btnExecute);

        btnClear = new JButton("Leeren");
        buttonPanel.add(btnClear);

        queryPanel.add(buttonPanel, BorderLayout.SOUTH);

        return queryPanel;
    }

    private JPanel createResultPanel() {
        resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Ergebnisse"));

        tableModel = new DefaultTableModel();
        tblResults = new JTable(tableModel);
        tblResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(tblResults);
        scrollPane.setPreferredSize(new Dimension(850, 200));
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        lblRowCount = new JLabel("0 Zeilen");
        resultPanel.add(lblRowCount, BorderLayout.SOUTH);

        return resultPanel;
    }

    // ===== Getters for View access =====

    public JComboBox<String> getConnectionsComboBox() {
        return cbConnections;
    }

    public JTextField getConnectionNameField() {
        return txtConnectionName;
    }

    public JTextField getUrlField() {
        return txtUrl;
    }

    public JTextField getUsernameField() {
        return txtUsername;
    }

    public JPasswordField getPasswordField() {
        return txtPassword;
    }

    public JComboBox<String> getDriverComboBox() {
        return cbDriver;
    }

    public JButton getConnectButton() {
        return btnConnect;
    }

    public JButton getSaveButton() {
        return btnSave;
    }

    public JButton getDeleteButton() {
        return btnDelete;
    }

    public JLabel getStatusLabel() {
        return lblStatus;
    }

    public JTextArea getQueryArea() {
        return txtQuery;
    }

    public JButton getExecuteButton() {
        return btnExecute;
    }

    public JButton getClearButton() {
        return btnClear;
    }

    public JTable getResultsTable() {
        return tblResults;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JLabel getRowCountLabel() {
        return lblRowCount;
    }
}
