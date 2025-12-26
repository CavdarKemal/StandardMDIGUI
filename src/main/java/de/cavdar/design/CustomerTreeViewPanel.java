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

    // Left toolbar components
    protected JButton btnLoad;
    protected JButton btnSave;
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
        // Load/Save buttons
        btnLoad = new JButton("Laden", IconLoader.load("folder_view.png"));
        btnLoad.setToolTipText("Testdaten aus JSON-Datei laden");
        leftToolbar.add(btnLoad);

        btnSave = new JButton("Speichern", IconLoader.load("save.png"));
        btnSave.setToolTipText("Testdaten in JSON-Datei speichern");
        leftToolbar.add(btnSave);

        leftToolbar.addSeparator();

        // Filter label and combo
        lblFilter = new JLabel("Filter:");
        leftToolbar.add(lblFilter);

        cbFilter = new JComboBox<>(new String[]{"Alle", "Aktiv", "Inaktiv"});
        leftToolbar.add(cbFilter);

        leftToolbar.addSeparator();

        // Active only checkbox
        chkActiveOnly = new JCheckBox("Nur aktive");
        leftToolbar.add(chkActiveOnly);

        leftToolbar.addSeparator();

        // Refresh button
        btnRefresh = new JButton("Aktualisieren", IconLoader.load("refresh.png"));
        leftToolbar.add(btnRefresh);
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
