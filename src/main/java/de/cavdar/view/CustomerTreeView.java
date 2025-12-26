package de.cavdar.view;

import de.cavdar.design.BaseViewPanel;
import de.cavdar.design.CustomerTreeViewPanel;
import de.cavdar.model.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cavdar.util.IconLoader;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * View for customer data with tree navigation.
 * Uses CustomerTreeViewPanel for GUI, this class contains only logic.
 *
 * Pattern:
 * - CustomerTreeViewPanel: GUI only (can be GUI designer generated)
 * - CustomerTreeView: Logic and event handlers only
 *
 * @author StandardMDIGUI
 * @version 3.0
 * @since 2024-12-25
 */
public class CustomerTreeView extends TreeView {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerTreeView.class);

    private final AppConfig cfg = AppConfig.getInstance();
    private CustomerTreeViewPanel customerPanel;

    public CustomerTreeView() {
        super("Kunden Explorer");
        loadCustomers();
        LOG.debug("CustomerTreeView initialized");
    }

    @Override
    protected BaseViewPanel createPanel() {
        customerPanel = new CustomerTreeViewPanel();
        treePanel = customerPanel;  // Also set parent's reference
        return customerPanel;
    }

    @Override
    protected void setupToolbarActions() {
        // Don't call super - we handle all toolbars ourselves
        setupCustomListeners();
    }

    /**
     * Sets up all event listeners.
     */
    private void setupCustomListeners() {
        // Tree selection listener
        customerPanel.getTree().addTreeSelectionListener(this::onTreeSelection);

        // Left toolbar listeners
        customerPanel.getFilterComboBox().addActionListener(e -> applyFilter());
        customerPanel.getActiveOnlyCheckBox().addActionListener(e -> applyFilter());
        customerPanel.getRefreshButton().addActionListener(e -> {
            loadCustomers();
            refreshTree();
        });

        // Right toolbar listeners
        customerPanel.getSearchButton().addActionListener(e -> performSearch());
        customerPanel.getSearchField().addActionListener(e -> performSearch()); // Enter key
        customerPanel.getEditButton().addActionListener(e -> editSelected());
        customerPanel.getDeleteButton().addActionListener(e -> deleteSelected());
    }

    // ===== Business Logic =====

    private void loadCustomers() {
        clearTree();
        String customers = cfg.getProperty("AVAILABLE_CUSTOMERS");
        if (customers != null && !customers.isEmpty()) {
            for (String cName : customers.split(",")) {
                String trimmed = cName.trim();
                DefaultMutableTreeNode customerNode = addNode(trimmed);

                // Add sub-nodes for each customer
                addNode(customerNode, "Stammdaten");
                addNode(customerNode, "Verträge");
                addNode(customerNode, "Dokumente");
            }
        }
        expandAll();
    }

    private void onTreeSelection(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                customerPanel.getTree().getLastSelectedPathComponent();

        if (node == null) return;

        String nodeName = node.toString();
        LOG.debug("Selected: {}", nodeName);

        // Update details
        StringBuilder details = new StringBuilder();
        details.append("Ausgewählt: ").append(nodeName).append("\n\n");
        details.append("Pfad: ");

        Object[] path = node.getUserObjectPath();
        for (int i = 0; i < path.length; i++) {
            if (i > 0) details.append(" > ");
            details.append(path[i]);
        }
        details.append("\n\n");
        details.append("Kinder: ").append(node.getChildCount()).append("\n");
        details.append("Blatt: ").append(node.isLeaf()).append("\n");
        details.append("Ebene: ").append(node.getLevel());

        customerPanel.getDetailsArea().setText(details.toString());
    }

    private void applyFilter() {
        String filter = (String) customerPanel.getFilterComboBox().getSelectedItem();
        boolean activeOnly = customerPanel.getActiveOnlyCheckBox().isSelected();
        LOG.info("Applying filter: {}, activeOnly: {}", filter, activeOnly);
        // TODO: Implement filter logic
    }

    private void performSearch() {
        String term = customerPanel.getSearchField().getText();
        LOG.info("Searching for: {}", term);
        // TODO: Implement search logic
    }

    private void editSelected() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                customerPanel.getTree().getLastSelectedPathComponent();
        if (node != null) {
            String newName = JOptionPane.showInputDialog(this,
                    "Neuer Name:", node.toString());
            if (newName != null && !newName.isEmpty()) {
                node.setUserObject(newName);
                customerPanel.getTreeModel().nodeChanged(node);
            }
        }
    }

    private void deleteSelected() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                customerPanel.getTree().getLastSelectedPathComponent();
        if (node != null && node != customerPanel.getRootNode()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "'" + node + "' wirklich löschen?",
                    "Löschen bestätigen",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                customerPanel.getTreeModel().removeNodeFromParent(node);
            }
        }
    }

    // ===== Getters =====

    public CustomerTreeViewPanel getCustomerPanel() {
        return customerPanel;
    }

    // ===== ViewInfo Implementation =====

    @Override
    public String getMenuLabel() {
        return "Kunden Explorer";
    }

    @Override
    public String getToolbarLabel() {
        return "Kunden";
    }

    @Override
    public KeyStroke getKeyboardShortcut() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK);
    }

    @Override
    public Icon getIcon() {
        return IconLoader.load("folder_cubes.png");
    }

    @Override
    public String getMenuGroup() {
        return "Verwaltung";
    }
}
