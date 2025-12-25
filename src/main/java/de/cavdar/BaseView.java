package de.cavdar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import javax.swing.*;

/**
 * Abstract base class for all internal view frames.
 * Uses BaseViewPanel for GUI, this class handles logic and events.
 *
 * Pattern:
 * - BaseViewPanel: GUI only (can be GUI designer generated)
 * - BaseView: Logic and event handlers only
 *
 * Subclasses should:
 * 1. Create a custom Panel class extending BaseViewPanel
 * 2. Override createPanel() to return custom panel
 * 3. Override setupToolbarActions() to add toolbar button listeners
 * 4. Override setupListeners() for additional event handlers
 *
 * @author StandardMDIGUI
 * @version 2.0
 * @since 2024-12-25
 */
public abstract class BaseView extends JInternalFrame implements ViewInfo {
    private static final Logger LOG = LoggerFactory.getLogger(BaseView.class);

    protected BaseViewPanel panel;
    protected SwingWorker<Void, Void> currentWorker;

    /**
     * Constructs a new BaseView with the specified title.
     *
     * @param title the title of the internal frame
     */
    public BaseView(String title) {
        super(title, true, true, true, true);
        setSize(400, 300);
        setLayout(new BorderLayout());

        // Create and add panel
        panel = createPanel();
        add(panel, BorderLayout.CENTER);

        // Setup logic
        setupCancelAction();
        setupToolbarActions();
        setupListeners();

        LOG.debug("BaseView created: {}", title);
    }

    /**
     * Creates the panel instance.
     * Override to return a custom panel (e.g., GUI designer generated).
     *
     * @return the panel instance
     */
    protected BaseViewPanel createPanel() {
        return new BaseViewPanel();
    }

    /**
     * Sets up the cancel button action.
     */
    private void setupCancelAction() {
        panel.getCancelButton().addActionListener(e -> {
            if (currentWorker != null) {
                LOG.info("Cancelling background task in view: {}", getTitle());
                currentWorker.cancel(true);
            }
        });
    }

    /**
     * Override to add action listeners to toolbar buttons.
     * Called after panel creation.
     */
    protected abstract void setupToolbarActions();

    /**
     * Override to add additional event listeners.
     * Called after setupToolbarActions().
     */
    protected void setupListeners() {
        // Default: empty - override in subclass
    }

    /**
     * Executes a background task with progress indication.
     * Shows a progress bar and cancel button during execution.
     *
     * @param taskLogic the task logic to execute in background
     */
    protected void executeTask(Runnable taskLogic) {
        LOG.info("Starting background task in view: {}", getTitle());

        panel.setProgressVisible(true, true);

        currentWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                taskLogic.run();
                return null;
            }

            @Override
            protected void done() {
                panel.setProgressVisible(false, false);
                if (isCancelled()) {
                    LOG.info("Background task cancelled in view: {}", getTitle());
                    JOptionPane.showMessageDialog(BaseView.this, "Aktion abgebrochen.");
                } else {
                    LOG.info("Background task completed in view: {}", getTitle());
                }
            }
        };
        currentWorker.execute();
    }

    // ===== Convenience methods =====

    /**
     * Returns the panel instance.
     *
     * @return the panel
     */
    public BaseViewPanel getPanel() {
        return panel;
    }

    /**
     * Returns the view toolbar for adding buttons.
     *
     * @return the toolbar
     */
    public JToolBar getViewToolbar() {
        return panel.getViewToolbar();
    }

    /**
     * Returns the content panel for adding content.
     *
     * @return the content panel
     */
    public JPanel getContentPanel() {
        return panel.getContentPanel();
    }

    // ===== ViewInfo Default Implementation =====

    @Override
    public String getMenuLabel() {
        return getTitle();
    }

    @Override
    public String getToolbarLabel() {
        return null;
    }
}
