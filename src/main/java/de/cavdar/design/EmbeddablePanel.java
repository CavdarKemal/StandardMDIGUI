package de.cavdar.design;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Abstract base class for panels that can be embedded in the main frame.
 * Unlike BaseView (JInternalFrame), these panels are designed to be
 * embedded directly in JSplitPane or other containers.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-25
 */
public abstract class EmbeddablePanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(EmbeddablePanel.class);

    private final String panelName;

    /**
     * Constructs an EmbeddablePanel with the specified name.
     *
     * @param name the name of this panel (for logging)
     */
    protected EmbeddablePanel(String name) {
        this.panelName = name;
        setLayout(new BorderLayout());
        initializePanel();
        LOG.debug("EmbeddablePanel created: {}", name);
    }

    /**
     * Template method for subclasses to initialize their content.
     * Called during construction.
     */
    protected abstract void initializePanel();

    /**
     * Returns the name of this panel.
     *
     * @return panel name
     */
    public String getPanelName() {
        return panelName;
    }
}
