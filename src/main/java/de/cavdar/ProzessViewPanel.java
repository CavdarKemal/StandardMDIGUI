package de.cavdar;

import javax.swing.*;
import java.awt.*;

/**
 * GUI panel for ProzessView - contains only layout and components.
 * No listeners or business logic.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-25
 */
public class ProzessViewPanel extends BaseViewPanel {

    // Toolbar components
    protected JButton btnStart;
    protected JButton btnClear;

    // Content components
    protected JTextArea logArea;
    protected JScrollPane logScrollPane;

    public ProzessViewPanel() {
        super();
        initCustomComponents();
    }

    /**
     * Initializes process-specific components.
     */
    protected void initCustomComponents() {
        setupToolbarComponents();
        setupContentComponents();
    }

    private void setupToolbarComponents() {
        btnStart = new JButton("Start");
        viewToolbar.add(btnStart);

        btnClear = new JButton("Log löschen");
        viewToolbar.add(btnClear);
    }

    private void setupContentComponents() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        logScrollPane = new JScrollPane(logArea);
        contentPanel.add(logScrollPane, BorderLayout.CENTER);
    }

    // ===== Getters for View access =====

    public JButton getStartButton() {
        return btnStart;
    }

    public JButton getClearButton() {
        return btnClear;
    }

    public JTextArea getLogArea() {
        return logArea;
    }
}
