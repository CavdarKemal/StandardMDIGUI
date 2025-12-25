package de.cavdar.design;

import de.cavdar.model.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * Panel containing the customer tree view.
 * Can be embedded in the main frame's split pane.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-25
 */
public class TreePanel extends EmbeddablePanel {
    private static final Logger LOG = LoggerFactory.getLogger(TreePanel.class);

    private AppConfig cfg;
    private JTree customerTree;

    /**
     * Constructs the TreePanel.
     */
    public TreePanel() {
        super("CustomerTree");
    }

    @Override
    protected void initializePanel() {
        cfg = AppConfig.getInstance();

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(new JLabel("Available Customers:"));
        add(headerPanel, BorderLayout.NORTH);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Customers");
        String customers = cfg.getProperty("AVAILABLE_CUSTOMERS");
        if (customers != null && !customers.isEmpty()) {
            for (String cName : customers.split(",")) {
                root.add(new DefaultMutableTreeNode(cName.trim()));
            }
        }

        customerTree = new JTree(root);
        add(new JScrollPane(customerTree), BorderLayout.CENTER);

        LOG.debug("TreePanel initialized with {} customers", root.getChildCount());
    }

    /**
     * Returns the customer tree component.
     *
     * @return the JTree component
     */
    public JTree getTree() {
        return customerTree;
    }

    /**
     * Reloads the tree with current customer data from config.
     */
    public void reloadTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Customers");
        String customers = cfg.getProperty("AVAILABLE_CUSTOMERS");
        if (customers != null && !customers.isEmpty()) {
            for (String cName : customers.split(",")) {
                root.add(new DefaultMutableTreeNode(cName.trim()));
            }
        }
        customerTree.setModel(new javax.swing.tree.DefaultTreeModel(root));
        LOG.debug("TreePanel reloaded with {} customers", root.getChildCount());
    }
}
