package de.cavdar.view;

import de.cavdar.design.BaseViewPanel;
import de.cavdar.design.TreeViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cavdar.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Tree-based view with split pane layout.
 * Uses TreeViewPanel for GUI, this class adds logic and listeners.
 *
 * Pattern:
 * - TreeViewPanel: GUI only (can be GUI designer generated)
 * - TreeView: Logic and event handlers only
 *
 * Subclasses should:
 * 1. Create a custom Panel class extending TreeViewPanel
 * 2. Override createPanel() to return custom panel
 * 3. Override setupToolbarActions() for toolbar button listeners
 * 4. Override setupListeners() for additional event handlers
 *
 * @author StandardMDIGUI
 * @version 3.0
 * @since 2024-12-25
 */
public class TreeView extends BaseView {
    private static final Logger LOG = LoggerFactory.getLogger(TreeView.class);

    protected TreeViewPanel treePanel;

    /**
     * Constructs a TreeView with the specified title.
     *
     * @param title the window title
     */
    public TreeView(String title) {
        super(title);
        LOG.debug("TreeView created: {}", title);
    }

    /**
     * Constructs a TreeView with default title.
     */
    public TreeView() {
        this("Tree View");
    }

    @Override
    protected BaseViewPanel createPanel() {
        treePanel = new TreeViewPanel("Root");
        return treePanel;
    }

    @Override
    protected void setupToolbarActions() {
        // BaseView's main toolbar - can be used for view-level actions
        // Left and right toolbars are setup via setupLeftToolbarControls/setupRightToolbarControls
        setupLeftToolbarControls();
        setupRightToolbarControls();
        setupTabs();
    }

    /**
     * Adds controls to the left toolbar.
     * Override to customize.
     */
    protected void setupLeftToolbarControls() {
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTree());
        treePanel.getLeftToolbar().add(btnRefresh);
    }

    /**
     * Adds controls to the right toolbar.
     * Override to customize.
     */
    protected void setupRightToolbarControls() {
        // Default: empty - override in subclass
    }

    /**
     * Adds tabs to the tabbed pane.
     * Override to customize.
     */
    protected void setupTabs() {
        treePanel.getTabbedPane().addTab("Details", new JPanel());
    }

    // ===== Public API for Tree Operations =====

    /**
     * Adds a node to the tree root.
     *
     * @param nodeName the name of the new node
     * @return the created node
     */
    public DefaultMutableTreeNode addNode(String nodeName) {
        return addNode(treePanel.getRootNode(), nodeName);
    }

    /**
     * Adds a child node to a parent node.
     *
     * @param parent the parent node
     * @param nodeName the name of the new node
     * @return the created node
     */
    public DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, String nodeName) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeName);
        treePanel.getTreeModel().insertNodeInto(node, parent, parent.getChildCount());
        return node;
    }

    /**
     * Clears all nodes except the root.
     */
    public void clearTree() {
        treePanel.getRootNode().removeAllChildren();
        treePanel.getTreeModel().reload();
    }

    /**
     * Refreshes the tree display.
     */
    public void refreshTree() {
        treePanel.getTreeModel().reload();
        expandAll();
        LOG.debug("Tree refreshed");
    }

    /**
     * Expands all tree nodes.
     */
    public void expandAll() {
        JTree tree = treePanel.getTree();
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /**
     * Sets the root node name.
     *
     * @param name the new root name
     */
    public void setRootName(String name) {
        treePanel.getRootNode().setUserObject(name);
        treePanel.getTreeModel().nodeChanged(treePanel.getRootNode());
    }

    // ===== Public API for Tabs =====

    /**
     * Adds a tab to the tabbed pane.
     *
     * @param title the tab title
     * @param component the tab content
     */
    public void addTab(String title, Component component) {
        treePanel.getTabbedPane().addTab(title, component);
    }

    /**
     * Removes a tab by index.
     *
     * @param index the tab index
     */
    public void removeTab(int index) {
        JTabbedPane tabs = treePanel.getTabbedPane();
        if (index >= 0 && index < tabs.getTabCount()) {
            tabs.removeTabAt(index);
        }
    }

    // ===== Convenience Getters (delegate to panel) =====

    public TreeViewPanel getTreePanel() {
        return treePanel;
    }

    public JTree getTree() {
        return treePanel.getTree();
    }

    public DefaultTreeModel getTreeModel() {
        return treePanel.getTreeModel();
    }

    public DefaultMutableTreeNode getRootNode() {
        return treePanel.getRootNode();
    }

    public JTabbedPane getTabbedPane() {
        return treePanel.getTabbedPane();
    }

    public JToolBar getLeftToolbar() {
        return treePanel.getLeftToolbar();
    }

    public JToolBar getRightToolbar() {
        return treePanel.getRightToolbar();
    }

    // ===== ViewInfo Implementation =====

    @Override
    public String getToolbarLabel() {
        return "Tree";
    }

    @Override
    public KeyStroke getKeyboardShortcut() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK);
    }

    @Override
    public Icon getIcon() {
        return IconLoader.load("folder_view.png");
    }

    @Override
    public String getMenuGroup() {
        return "Navigation";
    }
}
