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
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    private static final String LAST_LOAD_DIRECTORY_KEY = "LAST_LOAD_DIRECTORY";
    private static final String LOAD_DIRECTORIES_KEY = "LOAD_DIRECTORIES";
    private static final String FILE_HISTORY_KEY = "CUSTOMER_FILE_HISTORY";
    private static final String DIRECTORY_SEPARATOR = ";";
    private static final int MAX_DIRECTORY_HISTORY = 10;
    private static final int MAX_FILE_HISTORY = 15;

    private final AppConfig cfg = AppConfig.getInstance();
    private CustomerTreeViewPanel customerPanel;
    private List<TestCustomer> customers = new ArrayList<>();
    private File currentFile;
    private boolean isLoadingFromHistory = false;

    // Context menus
    private JPopupMenu customerContextMenu;
    private JPopupMenu scenarioContextMenu;
    private JPopupMenu testCrefoContextMenu;

    public CustomerTreeView() {
        super("Kunden Explorer");
        setupCheckboxTree();
        setupContextMenus();
        setupTreeMouseListener();
        loadFileHistory();
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
     * Sets up context menus for different node types.
     */
    private void setupContextMenus() {
        // Customer context menu
        customerContextMenu = new JPopupMenu();
        JMenuItem editCustomerItem = new JMenuItem("Kunde bearbeiten", IconLoader.load("folder_edit.png"));
        editCustomerItem.addActionListener(e -> editSelected());
        JMenuItem deleteCustomerItem = new JMenuItem("Kunde löschen", IconLoader.load("folder_delete.png"));
        deleteCustomerItem.addActionListener(e -> deleteSelected());
        JMenuItem newScenarioItem = new JMenuItem("Neues Szenario erstellen", IconLoader.load("folder_view.png"));
        newScenarioItem.addActionListener(e -> createNewScenario());
        customerContextMenu.add(editCustomerItem);
        customerContextMenu.add(deleteCustomerItem);
        customerContextMenu.addSeparator();
        customerContextMenu.add(newScenarioItem);

        // Scenario context menu
        scenarioContextMenu = new JPopupMenu();
        JMenuItem editScenarioItem = new JMenuItem("Szenario bearbeiten", IconLoader.load("folder_edit.png"));
        editScenarioItem.addActionListener(e -> editSelected());
        JMenuItem deleteScenarioItem = new JMenuItem("Szenario löschen", IconLoader.load("folder_delete.png"));
        deleteScenarioItem.addActionListener(e -> deleteSelected());
        JMenuItem newTestfallItem = new JMenuItem("Neuen Testfall erstellen", IconLoader.load("table_sql.png"));
        newTestfallItem.addActionListener(e -> createNewTestfall());
        scenarioContextMenu.add(editScenarioItem);
        scenarioContextMenu.add(deleteScenarioItem);
        scenarioContextMenu.addSeparator();
        scenarioContextMenu.add(newTestfallItem);

        // TestCrefo context menu
        testCrefoContextMenu = new JPopupMenu();
        JMenuItem editTestfallItem = new JMenuItem("Testfall bearbeiten", IconLoader.load("folder_edit.png"));
        editTestfallItem.addActionListener(e -> editSelected());
        JMenuItem deleteTestfallItem = new JMenuItem("Testfall löschen", IconLoader.load("folder_delete.png"));
        deleteTestfallItem.addActionListener(e -> deleteSelected());
        testCrefoContextMenu.add(editTestfallItem);
        testCrefoContextMenu.add(deleteTestfallItem);
    }

    /**
     * Sets up mouse listener for context menu on tree.
     */
    private void setupTreeMouseListener() {
        customerPanel.getTree().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleTreeMouseEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleTreeMouseEvent(e);
            }
        });
    }

    private void handleTreeMouseEvent(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JTree tree = customerPanel.getTree();
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());

            if (path != null) {
                tree.setSelectionPath(path);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();

                if (userObject instanceof TestCustomer) {
                    customerContextMenu.show(tree, e.getX(), e.getY());
                } else if (userObject instanceof TestScenario) {
                    scenarioContextMenu.show(tree, e.getX(), e.getY());
                } else if (userObject instanceof TestCrefo) {
                    testCrefoContextMenu.show(tree, e.getX(), e.getY());
                }
            }
        }
    }

    /**
     * Loads the file history from config into the ComboBox.
     */
    private void loadFileHistory() {
        JComboBox<String> cbHistory = customerPanel.getFileHistoryComboBox();
        cbHistory.removeAllItems();
        cbHistory.addItem("-- Zuletzt geladen --");

        String historyData = cfg.getProperty(FILE_HISTORY_KEY);
        if (!historyData.isEmpty()) {
            for (String filePath : historyData.split(DIRECTORY_SEPARATOR)) {
                if (!filePath.trim().isEmpty()) {
                    File file = new File(filePath.trim());
                    if (file.exists()) {
                        cbHistory.addItem(filePath.trim());
                    }
                }
            }
        }

        // Add listener for selection
        cbHistory.addActionListener(e -> {
            if (isLoadingFromHistory) return;
            int selectedIndex = cbHistory.getSelectedIndex();
            if (selectedIndex > 0) {
                String filePath = (String) cbHistory.getSelectedItem();
                loadFromHistoryFile(filePath);
            }
        });
    }

    /**
     * Adds a file to the history ComboBox and saves to config.
     */
    private void addToFileHistory(File file) {
        if (file == null || !file.exists()) return;

        String filePath = file.getAbsolutePath();

        // Get current history
        String historyData = cfg.getProperty(FILE_HISTORY_KEY);
        LinkedHashSet<String> history = new LinkedHashSet<>();
        history.add(filePath); // Add new one first

        if (!historyData.isEmpty()) {
            for (String path : historyData.split(DIRECTORY_SEPARATOR)) {
                if (!path.trim().isEmpty() && !path.trim().equals(filePath)) {
                    history.add(path.trim());
                }
            }
        }

        // Limit history size
        List<String> historyList = new ArrayList<>(history);
        if (historyList.size() > MAX_FILE_HISTORY) {
            historyList = historyList.subList(0, MAX_FILE_HISTORY);
        }

        // Save to config
        cfg.setProperty(FILE_HISTORY_KEY, String.join(DIRECTORY_SEPARATOR, historyList));
        cfg.save();

        // Update ComboBox
        isLoadingFromHistory = true;
        JComboBox<String> cbHistory = customerPanel.getFileHistoryComboBox();
        cbHistory.removeAllItems();
        cbHistory.addItem("-- Zuletzt geladen --");
        for (String path : historyList) {
            cbHistory.addItem(path);
        }
        cbHistory.setSelectedIndex(1); // Select the just-loaded file
        isLoadingFromHistory = false;

        LOG.debug("Added to file history: {}", filePath);
    }

    /**
     * Loads customers from a file in the history.
     */
    private void loadFromHistoryFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;

        File file = new File(filePath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Datei nicht gefunden: " + filePath,
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            currentFile = file;
            customers = TestDataLoader.loadFromJson(file);
            buildTree();
            expandAll();

            LOG.info("Loaded {} customers from history: {}", customers.size(), filePath);
        } catch (IOException e) {
            LOG.error("Failed to load from history", e);
            JOptionPane.showMessageDialog(this,
                    "Fehler beim Laden: " + e.getMessage(),
                    "Ladefehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Create New Items =====

    /**
     * Creates a new scenario for the selected customer.
     */
    private void createNewScenario() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                customerPanel.getTree().getLastSelectedPathComponent();

        if (node == null) return;

        Object userObject = node.getUserObject();
        if (!(userObject instanceof TestCustomer customer)) return;

        String name = JOptionPane.showInputDialog(this,
                "Name des neuen Szenarios:", "Neues Szenario");
        if (name == null || name.trim().isEmpty()) return;

        TestScenario scenario = new TestScenario(name.trim(), customer);
        customer.addTestScenario(scenario);

        // Add to tree
        DefaultMutableTreeNode scenarioNode = new DefaultMutableTreeNode(scenario);
        customerPanel.getTreeModel().insertNodeInto(scenarioNode, node, node.getChildCount());

        // Expand and select
        TreePath newPath = new TreePath(scenarioNode.getPath());
        customerPanel.getTree().expandPath(newPath);
        customerPanel.getTree().setSelectionPath(newPath);

        LOG.info("Created new scenario: {} for customer: {}", name, customer.getCustomerKey());
    }

    /**
     * Creates a new test case for the selected scenario.
     */
    private void createNewTestfall() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                customerPanel.getTree().getLastSelectedPathComponent();

        if (node == null) return;

        Object userObject = node.getUserObject();
        if (!(userObject instanceof TestScenario scenario)) return;

        String name = JOptionPane.showInputDialog(this,
                "Name des neuen Testfalls:", "Neuer Testfall");
        if (name == null || name.trim().isEmpty()) return;

        TestCrefo crefo = new TestCrefo(name.trim());
        scenario.addTestCrefo(crefo);

        // Add to tree
        DefaultMutableTreeNode crefoNode = new DefaultMutableTreeNode(crefo);
        customerPanel.getTreeModel().insertNodeInto(crefoNode, node, node.getChildCount());

        // Expand and select
        TreePath newPath = new TreePath(crefoNode.getPath());
        customerPanel.getTree().expandPath(newPath);
        customerPanel.getTree().setSelectionPath(newPath);

        LOG.info("Created new testfall: {} for scenario: {}", name, scenario.getScenarioName());
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

        // Set initial directory from config
        String lastDir = cfg.getProperty(LAST_LOAD_DIRECTORY_KEY);
        if (!lastDir.isEmpty()) {
            File dir = new File(lastDir);
            if (dir.exists() && dir.isDirectory()) {
                chooser.setCurrentDirectory(dir);
            }
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = chooser.getSelectedFile();
                customers = TestDataLoader.loadFromJson(currentFile);
                buildTree();
                expandAll();

                // Save directory to config and add to file history
                saveDirectoryToConfig(currentFile.getParentFile());
                addToFileHistory(currentFile);

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

        // Set initial directory from config or current file
        if (currentFile != null) {
            chooser.setSelectedFile(currentFile);
        } else {
            String lastDir = cfg.getProperty(LAST_LOAD_DIRECTORY_KEY);
            if (!lastDir.isEmpty()) {
                File dir = new File(lastDir);
                if (dir.exists() && dir.isDirectory()) {
                    chooser.setCurrentDirectory(dir);
                }
            }
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

                // Save directory to config and add to file history
                saveDirectoryToConfig(file.getParentFile());
                addToFileHistory(file);

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

    /**
     * Saves the directory to config as last used and adds to history.
     */
    private void saveDirectoryToConfig(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }

        String dirPath = directory.getAbsolutePath();

        // Save as last used directory
        cfg.setProperty(LAST_LOAD_DIRECTORY_KEY, dirPath);

        // Add to directory history
        String historyData = cfg.getProperty(LOAD_DIRECTORIES_KEY);
        LinkedHashSet<String> history = new LinkedHashSet<>();
        history.add(dirPath); // Add new one first

        if (!historyData.isEmpty()) {
            for (String dir : historyData.split(DIRECTORY_SEPARATOR)) {
                if (!dir.trim().isEmpty() && !dir.trim().equals(dirPath)) {
                    history.add(dir.trim());
                }
            }
        }

        // Limit history size
        List<String> historyList = new ArrayList<>(history);
        if (historyList.size() > MAX_DIRECTORY_HISTORY) {
            historyList = historyList.subList(0, MAX_DIRECTORY_HISTORY);
        }

        cfg.setProperty(LOAD_DIRECTORIES_KEY, String.join(DIRECTORY_SEPARATOR, historyList));
        cfg.save();

        LOG.debug("Saved directory to config: {}", dirPath);
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
