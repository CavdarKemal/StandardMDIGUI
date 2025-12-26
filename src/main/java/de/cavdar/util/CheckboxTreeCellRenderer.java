package de.cavdar.util;

import de.cavdar.model.TestCrefo;
import de.cavdar.model.TestCustomer;
import de.cavdar.model.TestScenario;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Tree cell renderer that displays checkboxes for activatable items.
 * Supports TestCustomer, TestScenario, and TestCrefo with activated status.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-26
 */
public class CheckboxTreeCellRenderer extends JPanel implements TreeCellRenderer {
    private final JCheckBox checkBox;
    private final JLabel label;
    private final DefaultTreeCellRenderer defaultRenderer;

    private static final Color INACTIVE_COLOR = Color.GRAY;
    private static final Color ACTIVE_COLOR = new Color(0, 100, 0); // Dark green

    // Selection colors - more prominent
    private static final Color SELECTION_BACKGROUND = new Color(51, 153, 255); // Bright blue
    private static final Color SELECTION_FOREGROUND = Color.WHITE;
    private static final Border SELECTION_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 1),
            BorderFactory.createEmptyBorder(1, 2, 1, 2)
    );
    private static final Border NO_SELECTION_BORDER = BorderFactory.createEmptyBorder(2, 3, 2, 3);

    public CheckboxTreeCellRenderer() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setOpaque(true);

        checkBox = new JCheckBox();
        checkBox.setOpaque(false);

        label = new JLabel();
        label.setOpaque(false);

        add(checkBox);
        add(label);

        defaultRenderer = new DefaultTreeCellRenderer();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {

        if (value instanceof DefaultMutableTreeNode node) {
            Object userObject = node.getUserObject();

            if (userObject instanceof TestCustomer customer) {
                return renderWithCheckbox(tree, customer.toString(), customer.isActivated(),
                        selected, IconLoader.load("folder_cubes.png"));

            } else if (userObject instanceof TestScenario scenario) {
                return renderWithCheckbox(tree, scenario.getScenarioName(), scenario.isActivated(),
                        selected, IconLoader.load("folder_view.png"));

            } else if (userObject instanceof TestCrefo crefo) {
                return renderWithCheckbox(tree, crefo.getTestFallName(), crefo.isActivated(),
                        selected, IconLoader.load("table_sql.png"));

            } else {
                // Default rendering for root and other nodes
                return defaultRenderer.getTreeCellRendererComponent(
                        tree, value, selected, expanded, leaf, row, hasFocus);
            }
        }

        return defaultRenderer.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);
    }

    private Component renderWithCheckbox(JTree tree, String text, boolean activated,
                                         boolean selected, Icon icon) {
        checkBox.setSelected(activated);
        label.setText(text);
        label.setIcon(icon);

        if (selected) {
            // Selected: white text on blue background
            setBackground(SELECTION_BACKGROUND);
            setBorder(SELECTION_BORDER);
            label.setForeground(SELECTION_FOREGROUND);
        } else {
            // Not selected: green/gray text on transparent background
            setBackground(tree.getBackground());
            setBorder(NO_SELECTION_BORDER);
            if (activated) {
                label.setForeground(ACTIVE_COLOR);
            } else {
                label.setForeground(INACTIVE_COLOR);
            }
        }

        return this;
    }
}
