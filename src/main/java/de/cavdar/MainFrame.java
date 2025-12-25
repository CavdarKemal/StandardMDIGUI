package de.cavdar;

import de.cavdar.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Main MDI (Multiple Document Interface) application frame.
 * Provides the primary window with menu bar, toolbar, settings panel,
 * and a desktop pane for internal frames.
 *
 * @author StandardMDIGUI
 * @version 1.1
 * @since 2024-12-24
 */
public class MainFrame extends JFrame implements ConnectionManager.ConnectionListener {
    private static final Logger LOG = LoggerFactory.getLogger(MainFrame.class);

    private JDesktopPane desktopPane;
    private final AppConfig cfg = AppConfig.getInstance();
    private JComboBox<String> cbDbConnections;
    private DatabaseView databaseView;

    /**
     * Constructs the main MDI frame with all UI components.
     */
    public MainFrame() {
        setTitle("MDI Application - " + cfg.getProperty("TEST-BASE-PATH"));
        initWindow();

        desktopPane = new JDesktopPane();
        setJMenuBar(createMenuBar());
        add(createMainToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(createSettingsPanel()), BorderLayout.WEST);
        add(desktopPane, BorderLayout.CENTER);

        // Register for connection changes
        ConnectionManager.addListener(this);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                LOG.info("Application closing, saving window state");
                ConnectionManager.removeListener(MainFrame.this);
                cfg.setProperty("LAST_WINDOW_WIDTH", String.valueOf(getWidth()));
                cfg.setProperty("LAST_WINDOW_HEIGHT", String.valueOf(getHeight()));
                cfg.setProperty("LAST_WINDOW_X_POS", String.valueOf(getX()));
                cfg.setProperty("LAST_WINDOW_Y_POS", String.valueOf(getY()));
                cfg.save();
                System.exit(0);
            }
        });

        LOG.info("MainFrame initialized successfully");
    }

    @Override
    public void onConnectionsChanged() {
        // Reload DB connections ComboBox when connections change
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

        // Restore selection or last used
        if (currentSelection != null && cbDbConnections.getItemCount() > 0) {
            cbDbConnections.setSelectedItem(currentSelection);
        } else {
            String lastConn = ConnectionManager.getLastConnectionName();
            if (!lastConn.isEmpty()) {
                cbDbConnections.setSelectedItem(lastConn);
            }
        }
    }

    private void initWindow() {
        try {
            String lafClass = cfg.getProperty("LAST_LOOK_AND_FEEL_CLASS");
            if (!lafClass.isEmpty()) {
                UIManager.setLookAndFeel(lafClass);
                LOG.debug("Look and Feel set to: {}", lafClass);
            }
        } catch (Exception e) {
            LOG.warn("Could not set Look and Feel, using default", e);
        }

        int w = cfg.getInt("LAST_WINDOW_WIDTH", 1200);
        int h = cfg.getInt("LAST_WINDOW_HEIGHT", 800);
        int x = cfg.getInt("LAST_WINDOW_X_POS", 100);
        int y = cfg.getInt("LAST_WINDOW_Y_POS", 100);

        // Validate window bounds against screen
        Rectangle validBounds = validateWindowBounds(x, y, w, h);

        setBounds(validBounds);
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        LOG.debug("Window initialized with bounds: x={}, y={}, w={}, h={}",
                validBounds.x, validBounds.y, validBounds.width, validBounds.height);
    }

    /**
     * Validates and adjusts window bounds to ensure visibility on screen.
     * Checks all available monitors and adjusts position if window would be off-screen.
     *
     * @param x      desired x position
     * @param y      desired y position
     * @param width  desired width
     * @param height desired height
     * @return validated Rectangle with adjusted bounds
     */
    private Rectangle validateWindowBounds(int x, int y, int width, int height) {
        // Get virtual screen bounds (all monitors combined)
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle virtualBounds = new Rectangle();

        for (GraphicsDevice gd : ge.getScreenDevices()) {
            for (GraphicsConfiguration gc : gd.getConfigurations()) {
                virtualBounds = virtualBounds.union(gc.getBounds());
            }
        }

        // Ensure minimum size
        width = Math.max(width, 400);
        height = Math.max(height, 300);

        // Check if window is at least partially visible
        Rectangle windowBounds = new Rectangle(x, y, width, height);
        if (!virtualBounds.intersects(windowBounds)) {
            // Window completely off-screen, reset to default position
            LOG.warn("Window position ({}, {}) is off-screen, resetting to default", x, y);
            x = 100;
            y = 100;
        }

        // Ensure window doesn't exceed screen bounds
        if (x < virtualBounds.x) {
            x = virtualBounds.x;
        }
        if (y < virtualBounds.y) {
            y = virtualBounds.y;
        }
        if (x + width > virtualBounds.x + virtualBounds.width) {
            x = Math.max(virtualBounds.x, virtualBounds.x + virtualBounds.width - width);
        }
        if (y + height > virtualBounds.y + virtualBounds.height) {
            y = Math.max(virtualBounds.y, virtualBounds.y + virtualBounds.height - height);
        }

        return new Rectangle(x, y, width, height);
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu fileMenu = new JMenu("Datei");
        JMenuItem itemNew = new JMenuItem("Neue View");
        itemNew.addActionListener(e -> openView(new SampleView()));
        fileMenu.add(itemNew);

        mb.add(fileMenu);

        JMenu windowMenu = new JMenu("Fenster");

        JMenuItem itemCascade = new JMenuItem("Kaskadiert anordnen");
        itemCascade.addActionListener(e -> layoutCascaded());
        windowMenu.add(itemCascade);

        JMenuItem itemTileHor = new JMenuItem("Nebeneinander anordnen");
        itemTileHor.addActionListener(e -> layoutTileVertical());
        windowMenu.add(itemTileHor);

        JMenuItem itemTileVer = new JMenuItem("Untereinander anordnen");
        itemTileVer.addActionListener(e -> layoutTileHorizontal());
        windowMenu.add(itemTileVer);

        mb.add(windowMenu);
        return mb;
    }

    private JToolBar createMainToolbar() {
        JToolBar tb = new JToolBar();
        JButton btn = new JButton("Neue View");
        btn.addActionListener(e -> openView(new SampleView()));
        tb.add(btn);

        return tb;
    }

    private JPanel createSettingsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Database Connections
        p.add(new JLabel("DB-Verbindung:"));

        // Horizontal panel for ComboBox and DB button
        JPanel dbPanel = new JPanel();
        dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.X_AXIS));
        dbPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        // DB Icon Button
        JButton btnOpenDb = new JButton("\uD83D\uDDC4"); // Database icon (Unicode)
        btnOpenDb.setToolTipText("Datenbank-View öffnen");
        btnOpenDb.setMargin(new Insets(2, 6, 2, 6));
        btnOpenDb.addActionListener(e -> openOrShowDatabaseView());
        dbPanel.add(btnOpenDb);

        // Fix panel height
        Dimension prefSize = dbPanel.getPreferredSize();
        dbPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefSize.height));
        p.add(dbPanel);

        p.add(Box.createVerticalStrut(10));

        // Test Sources
        p.add(new JLabel("Test Sources:"));
        JComboBox<String> cbSources = new JComboBox<>(cfg.getArray("TEST-SOURCES"));
        cbSources.setSelectedItem(cfg.getProperty("LAST_TEST_SOURCE"));
        cbSources.addActionListener(e -> {
            cfg.setProperty("LAST_TEST_SOURCE", (String) cbSources.getSelectedItem());
            cfg.save();
        });
        fixComboBoxHeight(cbSources);
        p.add(cbSources);

        // Test Types
        p.add(new JLabel("Test Types:"));
        JComboBox<String> cbTypes = new JComboBox<>(cfg.getArray("TEST-TYPES"));
        fixComboBoxHeight(cbTypes);
        p.add(cbTypes);

        // ITSQ Revisions
        p.add(new JLabel("ITSQ Revisions:"));
        JComboBox<String> cbRev = new JComboBox<>(cfg.getArray("ITSQ_REVISIONS"));
        cbRev.setSelectedItem(cfg.getProperty("LAST_ITSQ_REVISION"));
        cbRev.addActionListener(e -> {
            cfg.setProperty("LAST_ITSQ_REVISION", (String) cbRev.getSelectedItem());
            cfg.save();
        });
        fixComboBoxHeight(cbRev);
        p.add(cbRev);

        p.add(Box.createVerticalStrut(10));

        p.add(createManagedCheckBox("Dump", "DUMP_IN_REST_CLIENT"));
        p.add(createManagedCheckBox("SFTP-Upload", "SFTP_UPLOAD_ACTIVE"));
        p.add(createManagedCheckBox("Export Protokoll", "CHECK-EXPORT-PROTOKOLL-ACTIVE"));
        p.add(createManagedCheckBox("Upload Synthetics", "LAST_UPLOAD_SYNTHETICS"));
        p.add(createManagedCheckBox("Only Test Clz", "LAST_USE_ONLY_TEST_CLZ"));

        p.add(new JLabel("Available Customers:"));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Customers");
        for (String cName : cfg.getProperty("AVAILABLE_CUSTOMERS").split(",")) {
            root.add(new DefaultMutableTreeNode(cName.trim()));
        }
        JTree tree = new JTree(root);
        p.add(new JScrollPane(tree));

        p.setPreferredSize(new Dimension(300, 0));
        return p;
    }

    /**
     * Fixes the height of a JComboBox to prevent vertical stretching in BoxLayout.
     */
    private void fixComboBoxHeight(JComboBox<?> comboBox) {
        Dimension pref = comboBox.getPreferredSize();
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
    }

    private JCheckBox createManagedCheckBox(String label, String propertyKey) {
        JCheckBox cb = new JCheckBox(label, cfg.getBool(propertyKey));
        cb.addActionListener(e -> {
            cfg.setProperty(propertyKey, String.valueOf(cb.isSelected()));
            cfg.save();
        });
        return cb;
    }

    private JInternalFrame[] getOpenFrames() {
        return java.util.Arrays.stream(desktopPane.getAllFrames())
                .filter(f -> !f.isIcon())
                .toArray(JInternalFrame[]::new);
    }

    private void layoutCascaded() {
        JInternalFrame[] frames = getOpenFrames();
        LOG.debug("Arranging {} frames in cascade layout", frames.length);

        int x = 0, y = 0;
        int offset = 30;

        for (JInternalFrame f : frames) {
            try {
                f.setMaximum(false);
            } catch (PropertyVetoException e) {
                LOG.warn("Could not restore frame from maximized state", e);
            }

            f.setBounds(x, y, 600, 400);
            f.toFront();
            x += offset;
            y += offset;

            if (x + 200 > desktopPane.getWidth()) x = 0;
            if (y + 200 > desktopPane.getHeight()) y = 0;
        }
    }

    private void layoutTileVertical() {
        JInternalFrame[] frames = getOpenFrames();
        if (frames.length == 0) return;

        LOG.debug("Arranging {} frames in vertical tile layout", frames.length);

        int width = desktopPane.getWidth() / frames.length;
        int height = desktopPane.getHeight();

        for (int i = 0; i < frames.length; i++) {
            try {
                frames[i].setMaximum(false);
            } catch (PropertyVetoException e) {
                LOG.warn("Could not restore frame at position {}", i, e);
            }
            frames[i].setBounds(i * width, 0, width, height);
        }
    }

    private void layoutTileHorizontal() {
        JInternalFrame[] frames = getOpenFrames();
        if (frames.length == 0) return;

        LOG.debug("Arranging {} frames in horizontal tile layout", frames.length);

        int width = desktopPane.getWidth();
        int height = desktopPane.getHeight() / frames.length;

        for (int i = 0; i < frames.length; i++) {
            try {
                frames[i].setMaximum(false);
            } catch (PropertyVetoException e) {
                LOG.warn("Could not restore frame at position {}", i, e);
            }
            frames[i].setBounds(0, i * height, width, height);
        }
    }

    /**
     * Opens a view in the desktop pane.
     *
     * @param view the view to open
     */
    public void openView(BaseView view) {
        LOG.info("Opening view: {}", view.getTitle());
        desktopPane.add(view);
        view.setVisible(true);
    }

    /**
     * Opens or shows the DatabaseView with the currently selected connection.
     * Reuses the same DatabaseView instance instead of creating a new one.
     */
    private void openOrShowDatabaseView() {
        String selected = (String) cbDbConnections.getSelectedItem();

        // Check if view exists and is still valid
        if (databaseView != null && !databaseView.isClosed()) {
            // Bring existing view to front
            try {
                if (databaseView.isIcon()) {
                    databaseView.setIcon(false);
                }
                databaseView.setSelected(true);
                databaseView.toFront();
            } catch (PropertyVetoException e) {
                LOG.warn("Could not bring DatabaseView to front", e);
            }
        } else {
            // Create new instance
            databaseView = new DatabaseView(selected);
            desktopPane.add(databaseView);
            databaseView.setVisible(true);
        }
    }

    /**
     * Application entry point.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        LOG.info("Starting MDI Application");
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
