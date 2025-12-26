package de.cavdar.design;

import de.cavdar.util.IconLoader;

import javax.swing.*;
import java.awt.*;

/**
 * GUI panel for CustomerTreeView - contains only layout and components.
 * No listeners or business logic.
 *
 * Extends TreeViewPanel to inherit the split pane layout.
 * This class can be replaced by a GUI designer generated class.
 * Fields are protected for access from the View class.
 *
 * @author StandardMDIGUI
 * @version 2.0
 * @since 2024-12-25
 */
public class CustomerTreeViewPanel extends TreeViewPanel {

    // Toolbar containers
    protected JPanel toolbarContainer;
    protected JToolBar toolbar1;  // Row 1: File history, Load, Save
    protected JToolBar toolbar2;  // Row 2: Filter, Active only, Refresh

    // Row 1 components
    protected JComboBox<String> cbFileHistory;
    protected JButton btnLoad;
    protected JButton btnSave;

    // Row 2 components
    protected JLabel lblFilter;
    protected JComboBox<String> cbFilter;
    protected JCheckBox chkActiveOnly;
    protected JButton btnRefresh;

    // Right toolbar components
    protected JLabel lblSearch;
    protected JTextField txtSearch;
    protected JButton btnSearch;
    protected JButton btnEdit;
    protected JButton btnDelete;

    // Tab components
    protected JTextArea detailsArea;
    protected JScrollPane detailsScrollPane;

    protected JTable dataTable;
    protected JScrollPane dataScrollPane;

    protected JTextArea notesArea;
    protected JScrollPane notesScrollPane;

    protected JList<String> historyList;
    protected JScrollPane historyScrollPane;

    public CustomerTreeViewPanel() {
        super("Kunden");
    }

    @Override
    protected void initTreeComponents() {
        super.initTreeComponents();
        initCustomComponents();
    }

    /**
     * Initializes customer-specific components.
     * Called after parent initTreeComponents().
     */
    protected void initCustomComponents() {
        setupLeftToolbarComponents();
        setupRightToolbarComponents();
        setupTabComponents();
    }

    private void setupLeftToolbarComponents() {
        // Create container for two toolbar rows
        toolbarContainer = new JPanel();
        toolbarContainer.setLayout(new BoxLayout(toolbarContainer, BoxLayout.Y_AXIS));

        // === Row 1: File history, Load, Save ===
        toolbar1 = new JToolBar();
        toolbar1.setFloatable(false);

        // File history ComboBox - shows only filename
        cbFileHistory = new JComboBox<>();
        cbFileHistory.setToolTipText("Zuletzt geladene Dateien");
        Dimension cbSize = new Dimension(180, 25);
        cbFileHistory.setPreferredSize(cbSize);
        cbFileHistory.setMinimumSize(cbSize);
        cbFileHistory.setMaximumSize(cbSize);
        toolbar1.add(cbFileHistory);

        toolbar1.addSeparator(new Dimension(5, 0));

        // Load/Save buttons
        btnLoad = new JButton("Laden", IconLoader.load("folder_view.png"));
        btnLoad.setToolTipText("Testdaten aus JSON-Datei laden");
        toolbar1.add(btnLoad);

        btnSave = new JButton("Speichern", IconLoader.load("save.png"));
        btnSave.setToolTipText("Testdaten in JSON-Datei speichern");
        toolbar1.add(btnSave);

        toolbarContainer.add(toolbar1);

        // === Row 2: Filter, Active only, Refresh ===
        toolbar2 = new JToolBar();
        toolbar2.setFloatable(false);

        // Filter label and combo - fixed size
        lblFilter = new JLabel("Filter:");
        toolbar2.add(lblFilter);

        cbFilter = new JComboBox<>(new String[]{"Alle", "Aktiv", "Inaktiv"});
        Dimension filterSize = new Dimension(80, 25);
        cbFilter.setPreferredSize(filterSize);
        cbFilter.setMinimumSize(filterSize);
        cbFilter.setMaximumSize(filterSize);
        toolbar2.add(cbFilter);

        toolbar2.addSeparator(new Dimension(10, 0));

        // Active only checkbox
        chkActiveOnly = new JCheckBox("Nur aktive");
        toolbar2.add(chkActiveOnly);

        toolbar2.addSeparator(new Dimension(10, 0));

        // Refresh button
        btnRefresh = new JButton("Aktualisieren", IconLoader.load("refresh.png"));
        toolbar2.add(btnRefresh);

        toolbarContainer.add(toolbar2);

        // Replace the single leftToolbar with our container
        leftPanel.remove(leftToolbar);
        leftPanel.add(toolbarContainer, BorderLayout.NORTH);
    }

    private void setupRightToolbarComponents() {
        // Search field
        lblSearch = new JLabel("Suche:");
        rightToolbar.add(lblSearch);

        txtSearch = new JTextField(15);
        txtSearch.setMaximumSize(new Dimension(150, 25));
        rightToolbar.add(txtSearch);

        btnSearch = new JButton("Suchen", IconLoader.load("folder_view.png"));
        rightToolbar.add(btnSearch);

        rightToolbar.addSeparator();

        // Action buttons
        btnEdit = new JButton("Bearbeiten", IconLoader.load("folder_edit.png"));
        rightToolbar.add(btnEdit);

        btnDelete = new JButton("Löschen", IconLoader.load("folder_delete.png"));
        rightToolbar.add(btnDelete);
    }

    private void setupTabComponents() {
        // Details tab
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsScrollPane = new JScrollPane(detailsArea);
        tabbedPane.addTab("Details", detailsScrollPane);

        // Data tab with table
        String[] columns = {"Eigenschaft", "Wert"};
        dataTable = new JTable(new Object[][]{}, columns);
        dataScrollPane = new JScrollPane(dataTable);
        tabbedPane.addTab("Daten", dataScrollPane);

        // Notes tab
        notesArea = new JTextArea();
        notesArea.setText("Notizen zum ausgewählten Kunden...");
        notesScrollPane = new JScrollPane(notesArea);
        tabbedPane.addTab("Notizen", notesScrollPane);

        // History tab
        historyList = new JList<>(new String[]{
            "2024-12-25: Erstellt",
            "2024-12-24: Aktualisiert"
        });
        historyScrollPane = new JScrollPane(historyList);
        tabbedPane.addTab("Historie", historyScrollPane);
    }

    // ===== Getters for View access =====

    public JComboBox<String> getFileHistoryComboBox() {
        return cbFileHistory;
    }

    public JButton getLoadButton() {
        return btnLoad;
    }

    public JButton getSaveButton() {
        return btnSave;
    }

    public JComboBox<String> getFilterComboBox() {
        return cbFilter;
    }

    public JCheckBox getActiveOnlyCheckBox() {
        return chkActiveOnly;
    }

    public JButton getRefreshButton() {
        return btnRefresh;
    }

    public JTextField getSearchField() {
        return txtSearch;
    }

    public JButton getSearchButton() {
        return btnSearch;
    }

    public JButton getEditButton() {
        return btnEdit;
    }

    public JButton getDeleteButton() {
        return btnDelete;
    }

    public JTextArea getDetailsArea() {
        return detailsArea;
    }

    public JTable getDataTable() {
        return dataTable;
    }

    public JTextArea getNotesArea() {
        return notesArea;
    }

    public JList<String> getHistoryList() {
        return historyList;
    }
}
