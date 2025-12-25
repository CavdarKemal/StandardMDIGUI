package de.cavdar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * View for data analysis and reporting.
 * Uses AnalyseViewPanel for GUI, this class contains only logic.
 *
 * Pattern:
 * - AnalyseViewPanel: GUI only (can be GUI designer generated)
 * - AnalyseView: Logic and event handlers only
 *
 * @author StandardMDIGUI
 * @version 2.0
 * @since 2024-12-25
 */
public class AnalyseView extends BaseView {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyseView.class);

    private AnalyseViewPanel analysePanel;

    /**
     * Constructs a new AnalyseView.
     */
    public AnalyseView() {
        super("Analyse");
        LOG.debug("AnalyseView created");
    }

    @Override
    protected BaseViewPanel createPanel() {
        analysePanel = new AnalyseViewPanel();
        return analysePanel;
    }

    @Override
    protected void setupToolbarActions() {
        analysePanel.getAnalyseButton().addActionListener(e -> startAnalysis());
        analysePanel.getExportButton().addActionListener(e -> exportData());
        analysePanel.getClearButton().addActionListener(e -> clearTable());
    }

    // ===== ViewInfo Implementation =====

    @Override
    public String getToolbarLabel() {
        return "Analyse";
    }

    // ===== Business Logic =====

    private void startAnalysis() {
        executeTask(() -> {
            LOG.info("Starting analysis");
            DefaultTableModel model = analysePanel.getTableModel();
            SwingUtilities.invokeLater(() -> model.setRowCount(0));

            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(500);
                    final int id = i;
                    SwingUtilities.invokeLater(() ->
                        model.addRow(new Object[]{
                            id,
                            "Element " + id,
                            "OK",
                            String.format("%.2f%%", Math.random() * 100)
                        }));
                } catch (InterruptedException ex) {
                    LOG.warn("Analysis interrupted", ex);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            LOG.info("Analysis completed");
        });
    }

    private void exportData() {
        int rowCount = analysePanel.getTableModel().getRowCount();
        JOptionPane.showMessageDialog(this,
            "Export von " + rowCount + " Zeilen...",
            "Export", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearTable() {
        analysePanel.getTableModel().setRowCount(0);
    }

    // ===== Getters =====

    public AnalyseViewPanel getAnalysePanel() {
        return analysePanel;
    }
}
