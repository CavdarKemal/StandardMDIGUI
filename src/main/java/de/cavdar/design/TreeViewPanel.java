package de.cavdar.design;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * GUI panel for TreeView - contains only layout and components.
 * No listeners or business logic.
 *
 * Extends BaseViewPanel to inherit the view toolbar and status panel.
 * The split pane is added to the content panel.
 *
 * This class can be replaced by a GUI designer generated class.
 * Fields are protected for access from the View class.
 *
 * @author StandardMDIGUI
 * @version 2.0
 * @since 2024-12-25
 */
public class TreeViewPanel extends BaseViewPanel {

    // Main layout
    protected JSplitPane mainSplitPane;

    // Left panel components
    protected JPanel leftPanel;
    protected JToolBar leftToolbar;
    protected JTree tree;
    protected DefaultTreeModel treeModel;
    protected DefaultMutableTreeNode rootNode;
    protected JScrollPane treeScrollPane;

    // Right panel components
    protected JPanel rightPanel;
    protected JToolBar rightToolbar;
    protected JTabbedPane tabbedPane;

    // Root name for the tree
    private String rootName;

    /**
     * Constructs the panel with default root name "Root".
     */
    public TreeViewPanel() {
        this("Root");
    }

    /**
     * Constructs the panel with specified root name.
     *
     * @param rootName the name for the tree root node
     */
    public TreeViewPanel(String rootName) {
        this.rootName = rootName;
        // BaseViewPanel constructor calls initComponents()
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        initTreeComponents();
    }

    /**
     * Initializes tree-specific components.
     */
    protected void initTreeComponents() {
        // Create main split pane
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(250);
        mainSplitPane.setResizeWeight(0.3);

        // Build panels
        leftPanel = createLeftPanel();
        rightPanel = createRightPanel();

        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(rightPanel);

        // Add to BaseViewPanel's content panel
        contentPanel.add(mainSplitPane, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Toolbar (empty - to be filled by View or subclass)
        leftToolbar = new JToolBar();
        leftToolbar.setFloatable(false);
        panel.add(leftToolbar, BorderLayout.NORTH);

        // Tree - use rootName if set, otherwise default to "Root"
        String name = (rootName != null) ? rootName : "Root";
        rootNode = new DefaultMutableTreeNode(name);
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        treeScrollPane = new JScrollPane(tree);
        panel.add(treeScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Toolbar (empty - to be filled by View or subclass)
        rightToolbar = new JToolBar();
        rightToolbar.setFloatable(false);
        panel.add(rightToolbar, BorderLayout.NORTH);

        // Tabbed pane (empty - to be filled by View or subclass)
        tabbedPane = new JTabbedPane();
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    // ===== Getters for View access =====

    public JSplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    public JTree getTree() {
        return tree;
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public DefaultMutableTreeNode getRootNode() {
        return rootNode;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public JToolBar getLeftToolbar() {
        return leftToolbar;
    }

    public JToolBar getRightToolbar() {
        return rightToolbar;
    }
}
