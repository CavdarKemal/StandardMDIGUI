package de.cavdar;

import de.cavdar.frame.MainFrame;
import de.cavdar.view.*;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JInternalFrameFixture;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Pause;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

/**
 * GUI tests for MainFrame.
 * Uses AssertJ-Swing for automated GUI testing.
 */
public class MainFrameTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        MainFrame frame = GuiActionRunner.execute(() -> {
            MainFrame f = new MainFrame();
            // Register views like in main()
            f.registerView(SampleView::new);
            f.registerView(ProzessView::new);
            f.registerView(AnalyseView::new);
            f.registerView(TreeView::new);
            f.registerView(CustomerTreeView::new);
            return f;
        });
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            window.cleanUp();
        }
    }

    /**
     * Helper method for reliable menu clicks.
     * Ensures the UI is ready before interacting with menus.
     * Uses longer delays and increased robot delay between events.
     */
    private JMenuItemFixture clickMenu(String... path) {
        // Ensure UI is idle and give extra time for menu system to be ready
        robot().waitForIdle();
        Pause.pause(300);

        // Focus the main window's menu bar to ensure menu is accessible
        window.focus();
        robot().waitForIdle();
        Pause.pause(100);

        // Temporarily increase delay between events for menu interaction
        int originalDelay = robot().settings().delayBetweenEvents();
        robot().settings().delayBetweenEvents(150);

        try {
            JMenuItemFixture menuItem = window.menuItemWithPath(path);
            menuItem.click();
            robot().waitForIdle();
            return menuItem;
        } finally {
            // Restore original delay
            robot().settings().delayBetweenEvents(originalDelay);
        }
    }

    @Test
    public void shouldShowMainFrame() {
        window.requireVisible();
        window.requireTitle("MDI Application - /X-TESTS/ENE");
    }

    @Test
    public void shouldBeResizable() {
        assertThat(window.target().isResizable()).isTrue();
    }

    @Test
    public void shouldHaveMenuBar() {
        // Check that menus exist in the menu bar
        MainFrame frame = (MainFrame) window.target();
        assertThat(frame.getJMenuBar()).isNotNull();
        assertThat(frame.getJMenuBar().getMenuCount()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldHaveToolbarWithButtons() {
        // Views with toolbar labels: Analyse, Kunden, Tree, Prozess
        window.button("Analyse").requireVisible();
        window.button("Prozess").requireVisible();
    }

    @Test
    public void shouldOpenAnalyseViewFromMenu() {
        // Click menu item to open Analyse view (now in Analyse submenu)
        clickMenu("Datei", "Analyse", "Analyse");

        // Verify internal frame was created
        JInternalFrameFixture internalFrame = window.internalFrame(
                new GenericTypeMatcher<JInternalFrame>(JInternalFrame.class) {
                    @Override
                    protected boolean isMatching(JInternalFrame frame) {
                        return "Analyse".equals(frame.getTitle());
                    }
                }
        );
        internalFrame.requireVisible();
    }

    @Test
    public void shouldOpenAnalyseViewFromToolbar() {
        window.button("Analyse").click();

        // Verify internal frame was created
        JInternalFrameFixture internalFrame = window.internalFrame(
                new GenericTypeMatcher<JInternalFrame>(JInternalFrame.class) {
                    @Override
                    protected boolean isMatching(JInternalFrame frame) {
                        return "Analyse".equals(frame.getTitle());
                    }
                }
        );
        internalFrame.requireVisible();
    }

    @Test
    public void shouldOpenProzessViewFromToolbar() {
        window.button("Prozess").click();

        // Verify internal frame was created
        JInternalFrameFixture internalFrame = window.internalFrame(
                new GenericTypeMatcher<JInternalFrame>(JInternalFrame.class) {
                    @Override
                    protected boolean isMatching(JInternalFrame frame) {
                        return "Prozess".equals(frame.getTitle());
                    }
                }
        );
        internalFrame.requireVisible();
    }

    @Test
    public void shouldHaveSettingsPanel() {
        // Check for settings components
        window.label("Test Sources:").requireVisible();
        window.label("Test Types:").requireVisible();
        window.label("ITSQ Revisions:").requireVisible();
    }

    @Test
    public void shouldHaveCheckboxes() {
        window.checkBox("Dump").requireVisible();
        window.checkBox("SFTP-Upload").requireVisible();
        window.checkBox("Export Protokoll").requireVisible();
    }

    @Test
    public void shouldHaveWindowMenu() {
        // Use helper to ensure UI is ready before checking menu items
        robot().waitForIdle();
        Pause.pause(100);
        window.menuItemWithPath("Fenster", "Kaskadiert anordnen").requireVisible();
        window.menuItemWithPath("Fenster", "Nebeneinander anordnen").requireVisible();
        window.menuItemWithPath("Fenster", "Untereinander anordnen").requireVisible();
    }

    @Test
    public void shouldCascadeWindows() {
        // Open multiple views with pause between to ensure proper initialization
        window.button("Analyse").click();
        robot().waitForIdle();
        Pause.pause(100);
        window.button("Prozess").click();
        robot().waitForIdle();
        Pause.pause(200);

        // Cascade windows using helper for reliability
        clickMenu("Fenster", "Kaskadiert anordnen");

        // Both internal frames should still be visible
        // (We can't easily verify positions, but we can verify no errors occurred)
    }

    @Test
    public void shouldTileWindowsVertically() {
        // Open multiple views with pause between to ensure proper initialization
        window.button("Analyse").click();
        robot().waitForIdle();
        Pause.pause(100);
        window.button("Prozess").click();
        robot().waitForIdle();
        Pause.pause(200);

        // Tile windows using helper for reliability
        clickMenu("Fenster", "Nebeneinander anordnen");
    }

    @Test
    public void shouldTileWindowsHorizontally() {
        // Open multiple views with pause between to ensure proper initialization
        window.button("Analyse").click();
        robot().waitForIdle();
        Pause.pause(100);
        window.button("Prozess").click();
        robot().waitForIdle();
        Pause.pause(200);

        // Tile windows using helper for reliability
        clickMenu("Fenster", "Untereinander anordnen");
    }
}
