package de.cavdar.view;

import de.cavdar.design.BaseViewPanel;
import de.cavdar.design.SampleViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Sample view demonstrating the BaseView framework.
 * Uses SampleViewPanel for GUI, this class contains only logic.
 *
 * Pattern:
 * - SampleViewPanel: GUI only (can be GUI designer generated)
 * - SampleView: Logic and event handlers only
 *
 * @author StandardMDIGUI
 * @version 2.0
 * @since 2024-12-25
 */
public class SampleView extends BaseView {
    private static final Logger LOG = LoggerFactory.getLogger(SampleView.class);

    private SampleViewPanel samplePanel;

    /**
     * Constructs a new SampleView.
     */
    public SampleView() {
        super("Kunden Analyse");
        LOG.debug("SampleView created");
    }

    @Override
    protected BaseViewPanel createPanel() {
        samplePanel = new SampleViewPanel();
        return samplePanel;
    }

    @Override
    protected void setupToolbarActions() {
        samplePanel.getStartButton().addActionListener(e -> startProcess());
    }

    // ===== ViewInfo Implementation =====

    @Override
    public KeyStroke getKeyboardShortcut() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK);
    }

    @Override
    public Icon getIcon() {
        return UIManager.getIcon("FileView.fileIcon");
    }

    @Override
    public String getMenuGroup() {
        return "Analyse";
    }

    // ===== Business Logic =====

    private void startProcess() {
        executeTask(() -> {
            LOG.info("Starting sample process");
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                    LOG.debug("Working... step {}/5", i + 1);
                } catch (InterruptedException ex) {
                    LOG.warn("Process interrupted", ex);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            LOG.info("Sample process completed");
        });
    }

    // ===== Getters =====

    public SampleViewPanel getSamplePanel() {
        return samplePanel;
    }
}
