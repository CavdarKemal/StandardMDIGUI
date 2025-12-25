package de.cavdar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.BorderLayout;

/**
 * Sample view demonstrating the BaseView framework.
 * Shows a simple customer analysis view with a start process button.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-24
 */
public class SampleView extends BaseView {
    private static final Logger LOG = LoggerFactory.getLogger(SampleView.class);

    /**
     * Constructs a new SampleView.
     */
    public SampleView() {
        super("Kunden Analyse");
        add(new JLabel("Inhalt der View..."), BorderLayout.CENTER);
        LOG.debug("SampleView created");
    }

    @Override
    protected void setupViewToolbar(JToolBar tb) {
        JButton btnStart = new JButton("Start Prozess");
        btnStart.addActionListener(e -> executeTask(() -> {
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
        }));
        tb.add(btnStart);
    }
}
