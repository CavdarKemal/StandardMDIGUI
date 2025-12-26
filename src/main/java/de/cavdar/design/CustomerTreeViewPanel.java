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

    // Toolbar container with GridBagLayout
    protected JPanel toolbarContainer;

    // Toolbar components - Row 1
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

    // Tab components - Details
    protected JTextArea detailsArea;
    protected JScrollPane detailsScrollPane;

    // Tab components - Testfall Editor
    protected JPanel testfallEditorPanel;
    protected JTextField txtTestfallName;
    protected JTextField txtTestfallInfo;
    protected JTextField txtItsqNr;
    protected JTextField txtPseudoNr;
    protected JCheckBox chkTestfallActivated;
    protected JCheckBox chkExported;
    protected JCheckBox chkShouldBeExported;
    protected JButton btnSaveTestfall;

    // Tab components - Notes and History
    protected JTextArea notesArea;
    protected JScrollPane notesScrollPane;

    protected JList<String> historyList;
    protected JScrollPane historyScrollPane;

    public CustomerTreeViewPanel() {
        super("Kunden"); // Root node name
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
        // Create container with GridBagLayout for toolbar controls
        toolbarContainer = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // === Row 0: File history ComboBox ===
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 3;
        cbFileHistory = new JComboBox<>();
        cbFileHistory.setToolTipText("Zuletzt geladene Dateien");
        toolbarContainer.add(cbFileHistory, gbc);

        // === Row 1: Load, Save buttons ===
        gbc.gridwidth = 1;
        gbc.gridy = 1;

        gbc.gridx = 0;
        btnLoad = new JButton("Laden", IconLoader.load("folder_view.png"));
        btnLoad.setToolTipText("Testdaten aus JSON-Datei laden");
        toolbarContainer.add(btnLoad, gbc);

        gbc.gridx = 1;
        btnSave = new JButton("Speichern", IconLoader.load("save.png"));
        btnSave.setToolTipText("Testdaten in JSON-Datei speichern");
        toolbarContainer.add(btnSave, gbc);

        gbc.gridx = 2;
        btnRefresh = new JButton("", IconLoader.load("refresh.png"));
        btnRefresh.setToolTipText("Aktualisieren");
        toolbarContainer.add(btnRefresh, gbc);

        // === Row 2: Filter controls ===
        gbc.gridy = 2;

        gbc.gridx = 0;
        lblFilter = new JLabel("Filter:");
        toolbarContainer.add(lblFilter, gbc);

        gbc.gridx = 1;
        cbFilter = new JComboBox<>(new String[]{"Alle", "Aktiv", "Inaktiv"});
        toolbarContainer.add(cbFilter, gbc);

        gbc.gridx = 2;
        chkActiveOnly = new JCheckBox("Nur aktive");
        toolbarContainer.add(chkActiveOnly, gbc);

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
        // Note: Edit/Delete buttons removed - functionality available via context menu
    }

    private void setupTabComponents() {
        // Details tab
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsScrollPane = new JScrollPane(detailsArea);
        tabbedPane.addTab("Details", detailsScrollPane);

        // Testfall Editor tab
        testfallEditorPanel = createTestfallEditorPanel();
        tabbedPane.addTab("Testfall Editor", testfallEditorPanel);

        // Notes tab
        notesArea = new JTextArea();
        notesArea.setText("Notizen zum ausgewählten Element...");
        notesScrollPane = new JScrollPane(notesArea);
        tabbedPane.addTab("Notizen", notesScrollPane);

        // History tab
        historyList = new JList<>(new String[]{
            "2024-12-26: Erstellt"
        });
        historyScrollPane = new JScrollPane(historyList);
        tabbedPane.addTab("Historie", historyScrollPane);
    }

    /**
     * Creates the Testfall editor panel with editable fields.
     */
    private JPanel createTestfallEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Testfall Name
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Testfall Name:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtTestfallName = new JTextField(20);
        panel.add(txtTestfallName, gbc);

        // Testfall Info
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Testfall Info:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtTestfallInfo = new JTextField(20);
        panel.add(txtTestfallInfo, gbc);

        // ITSQ Nr
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("ITSQ Nr:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtItsqNr = new JTextField(15);
        panel.add(txtItsqNr, gbc);

        // Pseudo Nr
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Pseudo Nr:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtPseudoNr = new JTextField(15);
        panel.add(txtPseudoNr, gbc);

        // Checkboxes
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        chkTestfallActivated = new JCheckBox("Aktiviert");
        panel.add(chkTestfallActivated, gbc);

        row++;
        gbc.gridy = row;
        chkExported = new JCheckBox("Exportiert");
        panel.add(chkExported, gbc);

        row++;
        gbc.gridy = row;
        chkShouldBeExported = new JCheckBox("Soll exportiert werden");
        panel.add(chkShouldBeExported, gbc);

        // Save button
        row++;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5);
        btnSaveTestfall = new JButton("Speichern", IconLoader.load("save.png"));
        btnSaveTestfall.setEnabled(false); // Disabled until a testfall is selected
        panel.add(btnSaveTestfall, gbc);

        // Spacer to push everything to the top
        row++;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), gbc);

        return panel;
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

    public JTextArea getDetailsArea() {
        return detailsArea;
    }

    public JTextArea getNotesArea() {
        return notesArea;
    }

    public JList<String> getHistoryList() {
        return historyList;
    }

    // ===== Testfall Editor Getters =====

    public JPanel getTestfallEditorPanel() {
        return testfallEditorPanel;
    }

    public JTextField getTestfallNameField() {
        return txtTestfallName;
    }

    public JTextField getTestfallInfoField() {
        return txtTestfallInfo;
    }

    public JTextField getItsqNrField() {
        return txtItsqNr;
    }

    public JTextField getPseudoNrField() {
        return txtPseudoNr;
    }

    public JCheckBox getTestfallActivatedCheckBox() {
        return chkTestfallActivated;
    }

    public JCheckBox getExportedCheckBox() {
        return chkExported;
    }

    public JCheckBox getShouldBeExportedCheckBox() {
        return chkShouldBeExported;
    }

    public JButton getSaveTestfallButton() {
        return btnSaveTestfall;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
}
