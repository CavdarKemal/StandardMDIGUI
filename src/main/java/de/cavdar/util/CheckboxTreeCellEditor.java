package de.cavdar.util;

import de.cavdar.model.TestCrefo;
import de.cavdar.model.TestCustomer;
import de.cavdar.model.TestScenario;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * Tree cell editor that handles checkbox clicks for activatable items.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-26
 */
public class CheckboxTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {
    private final JTree tree;
    private final JPanel editorPanel;
    private final JCheckBox checkBox;
    private final JLabel label;
    private DefaultMutableTreeNode currentNode;
    private Runnable onStateChanged;

    public CheckboxTreeCellEditor(JTree tree) {
        this.tree = tree;

        editorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        editorPanel.setOpaque(false);

        checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.addActionListener(e -> {
            if (currentNode != null) {
                toggleActivation(currentNode);
                stopCellEditing();
                if (onStateChanged != null) {
                    onStateChanged.run();
                }
            }
        });

        label = new JLabel();
        label.setOpaque(false);

        editorPanel.add(checkBox);
        editorPanel.add(label);
    }

    /**
     * Sets a callback to be invoked when activation state changes.
     *
     * @param onStateChanged the callback
     */
    public void setOnStateChanged(Runnable onStateChanged) {
        this.onStateChanged = onStateChanged;
    }

    private void toggleActivation(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();

        if (userObject instanceof TestCustomer customer) {
            customer.setActivated(!customer.isActivated());
        } else if (userObject instanceof TestScenario scenario) {
            scenario.setActivated(!scenario.isActivated());
        } else if (userObject instanceof TestCrefo crefo) {
            crefo.setActivated(!crefo.isActivated());
        }
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                boolean isSelected, boolean expanded,
                                                boolean leaf, int row) {
        if (value instanceof DefaultMutableTreeNode node) {
            currentNode = node;
            Object userObject = node.getUserObject();

            if (userObject instanceof TestCustomer customer) {
                checkBox.setSelected(customer.isActivated());
                label.setText(customer.toString());
                label.setIcon(IconLoader.load("folder_cubes.png"));

            } else if (userObject instanceof TestScenario scenario) {
                checkBox.setSelected(scenario.isActivated());
                label.setText(scenario.getScenarioName());
                label.setIcon(IconLoader.load("folder_view.png"));

            } else if (userObject instanceof TestCrefo crefo) {
                checkBox.setSelected(crefo.isActivated());
                label.setText(crefo.getTestFallName());
                label.setIcon(IconLoader.load("table_sql.png"));

            } else {
                return null;
            }

            return editorPanel;
        }
        return null;
    }

    @Override
    public Object getCellEditorValue() {
        return currentNode != null ? currentNode.getUserObject() : null;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent me) {
            // Only edit if clicked on a node with activatable content
            int row = tree.getRowForLocation(me.getX(), me.getY());
            if (row >= 0) {
                Object node = tree.getPathForRow(row).getLastPathComponent();
                if (node instanceof DefaultMutableTreeNode dmtn) {
                    Object userObject = dmtn.getUserObject();
                    return userObject instanceof TestCustomer
                            || userObject instanceof TestScenario
                            || userObject instanceof TestCrefo;
                }
            }
        }
        return false;
    }
}
