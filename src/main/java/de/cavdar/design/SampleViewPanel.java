package de.cavdar.design;

import javax.swing.*;

/**
 * GUI panel for SampleView - contains only layout and components.
 * No listeners or business logic.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-25
 */
public class SampleViewPanel extends BaseViewPanel {

    // Content components
    protected JLabel lblContent;
    protected JButton btnStart;

    public SampleViewPanel() {
        super();
        initCustomComponents();
    }

    /**
     * Initializes sample-specific components.
     */
    protected void initCustomComponents() {
        // Toolbar button
        btnStart = new JButton("Start Prozess");
        btnStart.setName("Start Prozess");
        viewToolbar.add(btnStart);

        // Content label
        lblContent = new JLabel("Inhalt der View...");
        lblContent.setName("Inhalt der View...");
        contentPanel.add(lblContent, java.awt.BorderLayout.CENTER);
    }

    // ===== Getters for View access =====

    public JButton getStartButton() {
        return btnStart;
    }

    public JLabel getContentLabel() {
        return lblContent;
    }
}
