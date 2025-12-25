package de.cavdar.frame;

import de.cavdar.design.DesktopPanel;
import de.cavdar.design.SettingsPanel;
import de.cavdar.design.TreePanel;
import de.cavdar.model.AppConfig;
import de.cavdar.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Main MDI (Multiple Document Interface) application frame.
 * Uses a modular split pane layout with embedded panels:
 * - Left: SettingsPanel (top) and TreePanel (bottom)
 * - Right: DesktopPanel with MDI internal frames
 *
 * Supports dynamic view registration via registerView().
 *
 * @author StandardMDIGUI
 * @version 2.1
 * @since 2024-12-25
 */
public class MainFrame extends JFrame {
    private static final Logger LOG = LoggerFactory.getLogger(MainFrame.class);

    private final AppConfig cfg = AppConfig.getInstance();
    private SettingsPanel settingsPanel;
    private TreePanel treePanel;
    private DesktopPanel desktopPanel;

    private JMenu viewMenu;
    private JToolBar mainToolbar;
    private final List<ViewRegistration> registeredViews = new ArrayList<>();

    /**
     * Holds registration info for a view type.
     */
    private record ViewRegistration(
            Supplier<BaseView> supplier,
            String menuLabel,
            String toolbarLabel,
            Icon icon,
            KeyStroke shortcut,
            String tooltip
    ) {}

    /**
     * Constructs the main MDI frame with modular panel layout.
     */
    public MainFrame() {
        setTitle("MDI Application - " + cfg.getProperty("TEST-BASE-PATH"));
        initWindow();

        // Create embedded panels
        settingsPanel = new SettingsPanel();
        treePanel = new TreePanel();
        desktopPanel = new DesktopPanel();

        // Connect settings panel to desktop panel
        settingsPanel.setOnOpenDatabaseView(() ->
            desktopPanel.openOrShowDatabaseView(settingsPanel.getSelectedConnection())
        );

        // Create toolbar first (views will add buttons)
        mainToolbar = new JToolBar();

        // Build menu bar
        setJMenuBar(createMenuBar());
        add(mainToolbar, BorderLayout.NORTH);
        add(createMainSplitPane(), BorderLayout.CENTER);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                LOG.info("Application closing, saving window state");
                settingsPanel.dispose();
                cfg.setProperty("LAST_WINDOW_WIDTH", String.valueOf(getWidth()));
                cfg.setProperty("LAST_WINDOW_HEIGHT", String.valueOf(getHeight()));
                cfg.setProperty("LAST_WINDOW_X_POS", String.valueOf(getX()));
                cfg.setProperty("LAST_WINDOW_Y_POS", String.valueOf(getY()));
                cfg.save();
                System.exit(0);
            }
        });

        LOG.info("MainFrame initialized successfully with modular layout");
    }

    /**
     * Registers a view type for menu and optional toolbar access.
     * The view's ViewInfo interface provides menu label, toolbar label, icon, etc.
     *
     * @param viewSupplier supplier that creates new instances of the view
     */
    public void registerView(Supplier<BaseView> viewSupplier) {
        // Create temporary instance to read ViewInfo
        BaseView tempView = viewSupplier.get();

        ViewRegistration reg = new ViewRegistration(
                viewSupplier,
                tempView.getMenuLabel(),
                tempView.getToolbarLabel(),
                tempView.getIcon(),
                tempView.getKeyboardShortcut(),
                tempView.getToolbarTooltip()
        );

        // Dispose temporary instance
        tempView.dispose();

        registeredViews.add(reg);

        // Add to menu
        addViewToMenu(reg);

        // Add to toolbar if toolbarLabel is set
        if (reg.toolbarLabel() != null) {
            addViewToToolbar(reg);
        }

        LOG.info("Registered view: {}", reg.menuLabel());
    }

    private void addViewToMenu(ViewRegistration reg) {
        JMenuItem item = new JMenuItem(reg.menuLabel());
        if (reg.icon() != null) {
            item.setIcon(reg.icon());
        }
        if (reg.shortcut() != null) {
            item.setAccelerator(reg.shortcut());
        }
        item.addActionListener(e -> desktopPanel.openView(reg.supplier().get()));
        viewMenu.add(item);
    }

    private void addViewToToolbar(ViewRegistration reg) {
        JButton btn;
        if (reg.icon() != null) {
            btn = new JButton(reg.icon());
            btn.setText(reg.toolbarLabel());
        } else {
            btn = new JButton(reg.toolbarLabel());
        }
        if (reg.tooltip() != null) {
            btn.setToolTipText(reg.tooltip());
        }
        btn.addActionListener(e -> desktopPanel.openView(reg.supplier().get()));
        mainToolbar.add(btn);
    }

    private JSplitPane createMainSplitPane() {
        // Left side: Settings (top) + Tree (bottom)
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplit.setTopComponent(settingsPanel);
        leftSplit.setBottomComponent(treePanel);
        leftSplit.setDividerLocation(350);
        leftSplit.setResizeWeight(0.0);

        // Main split: Left panels + Desktop
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setLeftComponent(leftSplit);
        mainSplit.setRightComponent(desktopPanel);
        mainSplit.setDividerLocation(300);
        mainSplit.setResizeWeight(0.0);

        return mainSplit;
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

        Rectangle validBounds = validateWindowBounds(x, y, w, h);

        setBounds(validBounds);
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        LOG.debug("Window initialized with bounds: x={}, y={}, w={}, h={}",
                validBounds.x, validBounds.y, validBounds.width, validBounds.height);
    }

    private Rectangle validateWindowBounds(int x, int y, int width, int height) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle virtualBounds = new Rectangle();

        for (GraphicsDevice gd : ge.getScreenDevices()) {
            for (GraphicsConfiguration gc : gd.getConfigurations()) {
                virtualBounds = virtualBounds.union(gc.getBounds());
            }
        }

        width = Math.max(width, 400);
        height = Math.max(height, 300);

        Rectangle windowBounds = new Rectangle(x, y, width, height);
        if (!virtualBounds.intersects(windowBounds)) {
            LOG.warn("Window position ({}, {}) is off-screen, resetting to default", x, y);
            x = 100;
            y = 100;
        }

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

        // Datei menu with dynamically registered views
        JMenu fileMenu = new JMenu("Datei");
        viewMenu = fileMenu; // Reference for dynamic registration

        fileMenu.addSeparator();
        JMenuItem itemExit = new JMenuItem("Beenden");
        itemExit.addActionListener(e -> {
            dispatchEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING));
        });
        fileMenu.add(itemExit);

        mb.add(fileMenu);

        // Fenster menu
        JMenu windowMenu = new JMenu("Fenster");

        JMenuItem itemCascade = new JMenuItem("Kaskadiert anordnen");
        itemCascade.addActionListener(e -> desktopPanel.layoutCascaded());
        windowMenu.add(itemCascade);

        JMenuItem itemTileHor = new JMenuItem("Nebeneinander anordnen");
        itemTileHor.addActionListener(e -> desktopPanel.layoutTileVertical());
        windowMenu.add(itemTileHor);

        JMenuItem itemTileVer = new JMenuItem("Untereinander anordnen");
        itemTileVer.addActionListener(e -> desktopPanel.layoutTileHorizontal());
        windowMenu.add(itemTileVer);

        mb.add(windowMenu);
        return mb;
    }

    /**
     * Returns the desktop panel for external access.
     *
     * @return the desktop panel
     */
    public DesktopPanel getDesktopPanel() {
        return desktopPanel;
    }

    /**
     * Returns the settings panel for external access.
     *
     * @return the settings panel
     */
    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    /**
     * Returns the tree panel for external access.
     *
     * @return the tree panel
     */
    public TreePanel getTreePanel() {
        return treePanel;
    }

    /**
     * Application entry point.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        LOG.info("Starting MDI Application");
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();

            // Register views dynamically
            frame.registerView(SampleView::new);
            frame.registerView(ProzessView::new);
            frame.registerView(AnalyseView::new);
            frame.registerView(TreeView::new);
            frame.registerView(CustomerTreeView::new);

            frame.setVisible(true);
        });
    }
}
