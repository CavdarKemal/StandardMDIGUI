package de.cavdar.view;

import de.cavdar.design.BaseViewPanel;
import de.cavdar.design.CustomerTreeViewPanel;
import de.cavdar.model.AppConfig;
import de.cavdar.model.TestCrefo;
import de.cavdar.model.TestCustomer;
import de.cavdar.model.TestScenario;
import de.cavdar.util.CheckboxTreeCellEditor;
import de.cavdar.util.CheckboxTreeCellRenderer;
import de.cavdar.util.IconLoader;
import de.cavdar.util.TestDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * View for test customer data with hierarchical tree navigation.
 * Displays TestCustomer -> TestScenario -> TestCrefo hierarchy
 * with checkbox support for activation status.
 *
 * @author StandardMDIGUI
 * @version 4.0
 * @since 2024-12-26
 */
public class CustomerTreeView extends TreeView {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerTreeView.class);

    private final AppConfig cfg = AppConfig.getInstance();
    private CustomerTreeViewPanel customerPanel;
    private List<TestCustomer> customers = new ArrayList<>();
    private File currentFile;

    public CustomerTreeView() {
        super("Kunden Explorer");
        setupCheckboxTree();
        loadSampleData();
        LOG.debug("CustomerTreeView initialized");
    }

    @Override
    protected BaseViewPanel createPanel() {
        customerPanel = new CustomerTreeViewPanel();
        treePanel = customerPanel;
        return customerPanel;
    }

    @Override
    protected void setupToolbarActions() {
        setupCustomListeners();
    }

    /**
     * Sets up the tree with checkbox renderer and editor.
     */
    private void setupCheckboxTree() {
        JTree tree = customerPanel.getTree();
        tree.setCellRenderer(new CheckboxTreeCellRenderer());

        CheckboxTreeCellEditor editor = new CheckboxTreeCellEditor(tree);
        editor.setOnStateChanged(() -> {
            tree.repaint();
            updateDetails();
        });
        tree.setCellEditor(editor);
        tree.setEditable(true);
    }

    /**
     * Sets up all event listeners.
     */
    private void setupCustomListeners() {
        // Tree selection listener
        customerPanel.getTree().addTreeSelectionListener(this::onTreeSelection);

        // Load/Save buttons
        customerPanel.getLoadButton().addActionListener(e -> loadFromFile());
        customerPanel.getSaveButton().addActionListener(e -> saveToFile());

        // Filter controls
        customerPanel.getFilterComboBox().addActionListener(e -> applyFilter());
        customerPanel.getActiveOnlyCheckBox().addActionListener(e -> applyFilter());
        customerPanel.getRefreshButton().addActionListener(e -> refreshTree());

        // Search
        customerPanel.getSearchButton().addActionListener(e -> performSearch());
        customerPanel.getSearchField().addActionListener(e -> performSearch());

        // Edit/Delete
        customerPanel.getEditButton().addActionListener(e -> editSelected());
        customerPanel.getDeleteButton().addActionListener(e -> deleteSelected());
    }

    // ===== Data Loading =====

    private void loadSampleData() {
        customers = TestDataLoader.createSampleData();
        buildTree();
        expandAll();
        LOG.info("Loaded sample data: {} customers", customers.size());
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Testdaten laden");
        chooser.setFileFilter(new FileNameExtensionFilter("JSON-Dateien (*.json)", "json"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = chooser.getSelectedFile();
                customers = TestDataLoader.loadFromJson(currentFile);
                buildTree();
                expandAll();
                JOptionPane.showMessageDialog(this,
                        customers.size() + " Kunden geladen.",
                        "Laden erfolgreich", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                LOG.error("Failed to load test data", e);
                JOptionPane.showMessageDialog(this,
                        "Fehler beim Laden: " + e.getMessage(),
                        "Ladefehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveToFile() {
        if (customers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Keine Daten zum Speichern vorhanden.",
                    "Keine Daten", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Testdaten speichern");
        chooser.setFileFilter(new FileNameExtensionFilter("JSON-Dateien (*.json)", "json"));

        if (currentFile != null) {
            chooser.setSelectedFile(currentFile);
        } else {
            chooser.setSelectedFile(new File("testdata.json"));
        }

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getAbsolutePath() + ".json");
            }

            try {
                TestDataLoader.saveToJson(customers, file);
                currentFile = file;
                JOptionPane.showMessageDialog(this,
                        customers.size() + " Kunden gespeichert.",
                        "Speichern erfolgreich", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                LOG.error("Failed to save test data", e);
                JOptionPane.showMessageDialog(this,
                        "Fehler beim Speichern: " + e.getMessage(),
                        "Speicherfehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== Tree Building =====

    private void buildTree() {
        clearTree();

        String filterValue = (String) customerPanel.getFilterComboBox().getSelectedItem();
        boolean activeOnly = customerPanel.getActiveOnlyCheckBox().isSelected();

        for (TestCustomer customer : customers) {
            // Apply filter
            if (activeOnly && !customer.isActivated()) {
                continue;
            }
            if ("Aktiv".equals(filterValue) && !customer.isActivated()) {
                continue;
            }
            if ("Inaktiv".equals(filterValue) && customer.isActivated()) {
                continue;
            }

            DefaultMutableTreeNode customerNode = new DefaultMutableTreeNode(customer);

            for (TestScenario scenario : customer.getTestScenariosMap().values()) {
                if (activeOnly && !scenario.isActivated()) {
                    continue;
                }

                DefaultMutableTreeNode scenarioNode = new DefaultMutableTreeNode(scenario);

                for (TestCrefo crefo : scenario.getTestFallNameToTestCrefoMap().values()) {
                    if (activeOnly && !crefo.isActivated()) {
                        continue;
                    }

                    DefaultMutableTreeNode crefoNode = new DefaultMutableTreeNode(crefo);
                    scenarioNode.add(crefoNode);
                }

                customerNode.add(scenarioNode);
            }

            treePanel.getRootNode().add(customerNode);
        }

        treePanel.getTreeModel().reload();
    }

    @Override
    public void refreshTree() {
        buildTree();
        expandAll();
    }

    // ===== Selection and Details =====

    private void onTreeSelection(TreeSelectionEvent e) {
        updateDetails();
    }

    private void updateDetails() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                customerPanel.getTree().getLastSelectedPathComponent();

        if (node == null) {
            customerPanel.getDetailsArea().setText("");
            return;
        }

        Object userObject = node.getUserObject();
        StringBuilder details = new StringBuilder();

        if (userObject instanceof TestCustomer customer) {
            details.append("=== Kunde ===\n\n");
            details.append("Key: ").append(customer.getCustomerKey()).append("\n");
            details.append("Name: ").append(customer.getCustomerName()).append("\n");
            details.append("JVM: ").append(customer.getJvmName()).append("\n");
            details.append("Phase: ").append(customer.getTestPhase()).append("\n");
            details.append("Aktiviert: ").append(customer.isActivated() ? "Ja" : "Nein").append("\n");
            details.append("\nSzenarien: ").append(customer.getTestScenariosMap().size());

        } else if (userObject instanceof TestScenario scenario) {
            details.append("=== Szenario ===\n\n");
            details.append("Name: ").append(scenario.getScenarioName()).append("\n");
            details.append("Kunde: ").append(scenario.getTestCustomer().getCustomerKey()).append("\n");
            details.append("Aktiviert: ").append(scenario.isActivated() ? "Ja" : "Nein").append("\n");
            details.append("\nTestfälle: ").append(scenario.getTestFallNameToTestCrefoMap().size());

        } else if (userObject instanceof TestCrefo crefo) {
            details.append("=== Testfall ===\n\n");
            details.append("Name: ").append(crefo.getTestFallName()).append("\n");
            details.append("Info: ").append(crefo.getTestFallInfo()).append("\n");
            details.append("ITSQ Nr: ").append(crefo.getItsqTestCrefoNr()).append("\n");
            details.append("Pseudo Nr: ").append(crefo.getPseudoCrefoNr()).append("\n");
            details.append("Aktiviert: ").append(crefo.isActivated() ? "Ja" : "Nein").append("\n");
            details.append("Exportiert: ").append(crefo.isExported() ? "Ja" : "Nein").append("\n");
            details.append("Soll exportiert werden: ").append(crefo.isShouldBeExported() ? "Ja" : "Nein");

        } else {
            details.append("Ausgewählt: ").append(node.toString()).append("\n");
            details.append("Kinder: ").append(node.getChildCount());
        }

        customerPanel.getDetailsArea().setText(details.toString());
    }

    // ===== Filter and Search =====

    private void applyFilter() {
        LOG.info("Applying filter");
        buildTree();
        expandAll();
    }

    private void performSearch() {
        String term = customerPanel.getSearchField().getText().toLowerCase().trim();
        if (term.isEmpty()) {
            return;
        }

        LOG.info("Searching for: {}", term);

        // Search through tree and select first match
        DefaultMutableTreeNode root = treePanel.getRootNode();
        DefaultMutableTreeNode match = findNode(root, term);

        if (match != null) {
            customerPanel.getTree().setSelectionPath(
                    new javax.swing.tree.TreePath(match.getPath()));
            customerPanel.getTree().scrollPathToVisible(
                    new javax.swing.tree.TreePath(match.getPath()));
        } else {
            JOptionPane.showMessageDialog(this,
                    "Keine Treffer für '" + term + "' gefunden.",
                    "Suche", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode parent, String term) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            String nodeText = child.toString().toLowerCase();

            if (nodeText.contains(term)) {
                return child;
            }

            DefaultMutableTreeNode found = findNode(child, term);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // ===== Edit and Delete =====

    private void editSelected() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                customerPanel.getTree().getLastSelectedPathComponent();

        if (node == null) {
            return;
        }

        Object userObject = node.getUserObject();

        if (userObject instanceof TestCustomer customer) {
            String newName = JOptionPane.showInputDialog(this,
                    "Kundenname:", customer.getCustomerName());
            if (newName != null && !newName.isEmpty()) {
                customer.setCustomerName(newName);
                customerPanel.getTreeModel().nodeChanged(node);
                updateDetails();
            }

        } else if (userObject instanceof TestScenario scenario) {
            String newName = JOptionPane.showInputDialog(this,
                    "Szenarioname:", scenario.getScenarioName());
            if (newName != null && !newName.isEmpty()) {
                scenario.setScenarioName(newName);
                customerPanel.getTreeModel().nodeChanged(node);
                updateDetails();
            }

        } else if (userObject instanceof TestCrefo crefo) {
            String newInfo = JOptionPane.showInputDialog(this,
                    "Testfall-Info:", crefo.getTestFallInfo());
            if (newInfo != null) {
                crefo.setTestFallInfo(newInfo);
                updateDetails();
            }
        }
    }

    private void deleteSelected() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                customerPanel.getTree().getLastSelectedPathComponent();

        if (node == null || node == treePanel.getRootNode()) {
            return;
        }

        Object userObject = node.getUserObject();
        String itemName = node.toString();

        int result = JOptionPane.showConfirmDialog(this,
                "'" + itemName + "' wirklich löschen?",
                "Löschen bestätigen",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // Remove from data model
            if (userObject instanceof TestCustomer customer) {
                customers.remove(customer);
            } else if (userObject instanceof TestScenario scenario) {
                scenario.getTestCustomer().getTestScenariosMap()
                        .remove(scenario.getScenarioName());
            } else if (userObject instanceof TestCrefo crefo) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                if (parent.getUserObject() instanceof TestScenario scenario) {
                    scenario.getTestFallNameToTestCrefoMap()
                            .remove(crefo.getTestFallName());
                }
            }

            // Remove from tree
            customerPanel.getTreeModel().removeNodeFromParent(node);
        }
    }

    // ===== Getters =====

    public CustomerTreeViewPanel getCustomerPanel() {
        return customerPanel;
    }

    public List<TestCustomer> getCustomers() {
        return customers;
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
