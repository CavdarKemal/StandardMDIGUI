package de.cavdar.design;

import de.cavdar.model.AppConfig;
import de.cavdar.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Panel containing application settings controls.
 * Can be embedded in the main frame's split pane.
 *
 * @author StandardMDIGUI
 * @version 1.1
 * @since 2024-12-25
 */
public class SettingsPanel extends EmbeddablePanel implements ConnectionManager.ConnectionListener {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsPanel.class);

    private AppConfig cfg;
    private JComboBox<String> cbDbConnections;
    private Runnable onOpenDatabaseView;

    /**
     * Constructs the SettingsPanel.
     */
    public SettingsPanel() {
        super("Settings");
        ConnectionManager.addListener(this);
    }

    /**
     * Sets the callback for opening the database view.
     *
     * @param callback the callback to invoke when DB button is clicked
     */
    public void setOnOpenDatabaseView(Runnable callback) {
        this.onOpenDatabaseView = callback;
    }

    /**
     * Returns the currently selected database connection name.
     *
     * @return selected connection name or null
     */
    public String getSelectedConnection() {
        return (String) cbDbConnections.getSelectedItem();
    }

    @Override
    public void onConnectionsChanged() {
        SwingUtilities.invokeLater(this::refreshDbConnections);
    }

    private void refreshDbConnections() {
        if (cbDbConnections == null) return;

        String currentSelection = (String) cbDbConnections.getSelectedItem();
        cbDbConnections.removeAllItems();

        String[] connectionNames = ConnectionManager.getConnectionNames();
        for (String name : connectionNames) {
            cbDbConnections.addItem(name);
        }

        if (currentSelection != null && cbDbConnections.getItemCount() > 0) {
            cbDbConnections.setSelectedItem(currentSelection);
        } else {
            String lastConn = ConnectionManager.getLastConnectionName();
            if (!lastConn.isEmpty()) {
                cbDbConnections.setSelectedItem(lastConn);
            }
        }
    }

    @Override
    protected void initializePanel() {
        cfg = AppConfig.getInstance();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Database Connections - Label links, ComboBox + Button rechts
        JPanel dbPanel = new JPanel();
        dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.X_AXIS));
        dbPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDb = new JLabel("DB-Verbindung:");
        lblDb.setPreferredSize(new Dimension(100, lblDb.getPreferredSize().height));
        dbPanel.add(lblDb);
        dbPanel.add(Box.createHorizontalStrut(5));

        cbDbConnections = new JComboBox<>(ConnectionManager.getConnectionNames());
        String lastConn = ConnectionManager.getLastConnectionName();
        if (!lastConn.isEmpty()) {
            cbDbConnections.setSelectedItem(lastConn);
        }
        cbDbConnections.addActionListener(e -> {
            String selected = (String) cbDbConnections.getSelectedItem();
            if (selected != null) {
                ConnectionManager.setLastConnectionName(selected);
            }
        });
        fixComboBoxHeight(cbDbConnections);
        dbPanel.add(cbDbConnections);

        dbPanel.add(Box.createHorizontalStrut(5));

        JButton btnOpenDb = new JButton("\uD83D\uDDC4");
        btnOpenDb.setToolTipText("Datenbank-View öffnen");
        btnOpenDb.setMargin(new Insets(2, 6, 2, 6));
        btnOpenDb.addActionListener(e -> {
            if (onOpenDatabaseView != null) {
                onOpenDatabaseView.run();
            }
        });
        dbPanel.add(btnOpenDb);

        fixRowHeight(dbPanel);
        content.add(dbPanel);
        content.add(Box.createVerticalStrut(5));

        // Test Sources - Label links
        JComboBox<String> cbSources = new JComboBox<>(cfg.getArray("TEST-SOURCES"));
        cbSources.setSelectedItem(cfg.getProperty("LAST_TEST_SOURCE"));
        cbSources.addActionListener(e -> {
            cfg.setProperty("LAST_TEST_SOURCE", (String) cbSources.getSelectedItem());
            cfg.save();
        });
        content.add(createLabeledRow("Test Sources:", cbSources));
        content.add(Box.createVerticalStrut(5));

        // Test Types - Label links
        JComboBox<String> cbTypes = new JComboBox<>(cfg.getArray("TEST-TYPES"));
        cbTypes.setSelectedItem(cfg.getProperty("LAST_TEST_TYPE"));
        cbTypes.addActionListener(e -> {
            cfg.setProperty("LAST_TEST_TYPE", (String) cbTypes.getSelectedItem());
            cfg.save();
        });
        content.add(createLabeledRow("Test Types:", cbTypes));
        content.add(Box.createVerticalStrut(5));

        // ITSQ Revisions - Label links
        JComboBox<String> cbRev = new JComboBox<>(cfg.getArray("ITSQ_REVISIONS"));
        cbRev.setSelectedItem(cfg.getProperty("LAST_ITSQ_REVISION"));
        cbRev.addActionListener(e -> {
            cfg.setProperty("LAST_ITSQ_REVISION", (String) cbRev.getSelectedItem());
            cfg.save();
        });
        content.add(createLabeledRow("ITSQ Revisions:", cbRev));

        content.add(Box.createVerticalStrut(10));

        // Checkboxen in zwei Spalten
        JPanel checkboxPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        checkboxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkboxPanel.add(createManagedCheckBox("Dump", "DUMP_IN_REST_CLIENT"));
        checkboxPanel.add(createManagedCheckBox("SFTP-Upload", "SFTP_UPLOAD_ACTIVE"));
        checkboxPanel.add(createManagedCheckBox("Export Protokoll", "CHECK-EXPORT-PROTOKOLL-ACTIVE"));
        checkboxPanel.add(createManagedCheckBox("Upload Synthetics", "LAST_UPLOAD_SYNTHETICS"));
        checkboxPanel.add(createManagedCheckBox("Only Test Clz", "LAST_USE_ONLY_TEST_CLZ"));

        // Wrapper damit GridLayout nicht die volle Breite einnimmt
        JPanel checkboxWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkboxWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkboxWrapper.add(checkboxPanel);
        content.add(checkboxWrapper);

        add(new JScrollPane(content), BorderLayout.CENTER);

        LOG.debug("SettingsPanel initialized");
    }

    /**
     * Creates a row with label on the left and component on the right.
     */
    private JPanel createLabeledRow(String labelText, JComponent component) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(100, label.getPreferredSize().height));
        row.add(label);
        row.add(Box.createHorizontalStrut(5));

        if (component instanceof JComboBox) {
            fixComboBoxHeight((JComboBox<?>) component);
        }
        row.add(component);

        fixRowHeight(row);
        return row;
    }

    private void fixComboBoxHeight(JComboBox<?> comboBox) {
        Dimension pref = comboBox.getPreferredSize();
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
    }

    private void fixRowHeight(JPanel row) {
        Dimension pref = row.getPreferredSize();
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
    }

    private JCheckBox createManagedCheckBox(String label, String propertyKey) {
        JCheckBox cb = new JCheckBox(label, cfg.getBool(propertyKey));
        cb.addActionListener(e -> {
            cfg.setProperty(propertyKey, String.valueOf(cb.isSelected()));
            cfg.save();
        });
        return cb;
    }

    /**
     * Cleans up resources when panel is no longer needed.
     */
    public void dispose() {
        ConnectionManager.removeListener(this);
        LOG.debug("SettingsPanel disposed");
    }
}
