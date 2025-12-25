package de.cavdar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import javax.swing.*;

/**
 * Abstract base class for all internal view frames.
 * Provides common functionality like toolbar management, progress bar,
 * and background task execution with SwingWorker.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-24
 */
public abstract class BaseView extends JInternalFrame {
    private static final Logger LOG = LoggerFactory.getLogger(BaseView.class);

    protected JProgressBar progressBar;
    protected JButton btnCancel;
    protected JToolBar toolBar;
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

        toolBar = new JToolBar();
        setupViewToolbar(toolBar);
        add(toolBar, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        btnCancel = new JButton("Cancel");
        btnCancel.setVisible(false);

        btnCancel.addActionListener(e -> {
            if (currentWorker != null) {
                LOG.info("Cancelling background task in view: {}", getTitle());
                currentWorker.cancel(true);
            }
        });

        statusPanel.add(progressBar, BorderLayout.CENTER);
        statusPanel.add(btnCancel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        LOG.debug("BaseView created: {}", title);
    }

    /**
     * Template method for subclasses to setup their specific toolbar buttons.
     *
     * @param tb the toolbar to add buttons to
     */
    protected abstract void setupViewToolbar(JToolBar tb);

    /**
     * Executes a background task with progress indication.
     * Shows a progress bar and cancel button during execution.
     *
     * @param taskLogic the task logic to execute in background
     */
    protected void executeTask(Runnable taskLogic) {
        LOG.info("Starting background task in view: {}", getTitle());

        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        btnCancel.setVisible(true);

        currentWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                taskLogic.run();
                return null;
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                btnCancel.setVisible(false);
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
}
