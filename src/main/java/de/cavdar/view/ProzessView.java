package de.cavdar.view;

import de.cavdar.design.BaseViewPanel;
import de.cavdar.design.ProzessViewPanel;
import de.cavdar.util.IconLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * View for process management and execution.
 * Uses ProzessViewPanel for GUI, this class contains only logic.
 *
 * Pattern:
 * - ProzessViewPanel: GUI only (can be GUI designer generated)
 * - ProzessView: Logic and event handlers only
 *
 * @author StandardMDIGUI
 * @version 2.0
 * @since 2024-12-25
 */
public class ProzessView extends BaseView {
    private static final Logger LOG = LoggerFactory.getLogger(ProzessView.class);

    private ProzessViewPanel prozessPanel;

    /**
     * Constructs a new ProzessView.
     */
    public ProzessView() {
        super("Prozess");
        LOG.debug("ProzessView created");
    }

    @Override
    protected BaseViewPanel createPanel() {
        prozessPanel = new ProzessViewPanel();
        return prozessPanel;
    }

    @Override
    protected void setupToolbarActions() {
        prozessPanel.getStartButton().addActionListener(e -> startProcess());
        prozessPanel.getClearButton().addActionListener(e -> clearLog());
    }

    // ===== ViewInfo Implementation =====

    @Override
    public String getToolbarLabel() {
        return "Prozess";
    }

    @Override
    public KeyStroke getKeyboardShortcut() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK);
    }

    @Override
    public Icon getIcon() {
        return IconLoader.load("gear_run.png");
    }

    @Override
    public String getMenuGroup() {
        return "Verwaltung";
    }

    // ===== Business Logic =====

    private void startProcess() {
        executeTask(() -> {
            LOG.info("Starting process");
            appendLog("Prozess gestartet...");
            for (int i = 1; i <= 5; i++) {
                try {
                    Thread.sleep(1000);
                    final int step = i;
                    SwingUtilities.invokeLater(() ->
                        appendLog("Schritt " + step + "/5 abgeschlossen"));
                } catch (InterruptedException ex) {
                    LOG.warn("Process interrupted", ex);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            SwingUtilities.invokeLater(() -> appendLog("Prozess beendet."));
            LOG.info("Process completed");
        });
    }

    private void clearLog() {
        prozessPanel.getLogArea().setText("");
    }

    private void appendLog(String message) {
        JTextArea logArea = prozessPanel.getLogArea();
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // ===== Getters =====

    public ProzessViewPanel getProzessPanel() {
        return prozessPanel;
    }
}
