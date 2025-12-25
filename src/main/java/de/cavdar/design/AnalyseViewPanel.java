package de.cavdar.design;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * GUI panel for AnalyseView - contains only layout and components.
 * No listeners or business logic.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-25
 */
public class AnalyseViewPanel extends BaseViewPanel {

    // Toolbar components
    protected JButton btnAnalyse;
    protected JButton btnExport;
    protected JButton btnClear;

    // Content components
    protected JTable resultTable;
    protected DefaultTableModel tableModel;
    protected JScrollPane tableScrollPane;

    public AnalyseViewPanel() {
        super();
        initCustomComponents();
    }

    /**
     * Initializes analyse-specific components.
     */
    protected void initCustomComponents() {
        setupToolbarComponents();
        setupContentComponents();
    }

    private void setupToolbarComponents() {
        btnAnalyse = new JButton("Analyse starten");
        viewToolbar.add(btnAnalyse);

        btnExport = new JButton("Exportieren");
        viewToolbar.add(btnExport);

        btnClear = new JButton("Löschen");
        viewToolbar.add(btnClear);
    }

    private void setupContentComponents() {
        String[] columns = {"ID", "Name", "Status", "Ergebnis"};
        tableModel = new DefaultTableModel(columns, 0);

        resultTable = new JTable(tableModel);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        tableScrollPane = new JScrollPane(resultTable);
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);
    }

    // ===== Getters for View access =====

    public JButton getAnalyseButton() {
        return btnAnalyse;
    }

    public JButton getExportButton() {
        return btnExport;
    }

    public JButton getClearButton() {
        return btnClear;
    }

    public JTable getResultTable() {
        return resultTable;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }
}
