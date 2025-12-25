package de.cavdar;

import de.cavdar.frame.MainFrame;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JInternalFrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
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
        MainFrame frame = GuiActionRunner.execute(() -> new MainFrame());
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            window.cleanUp();
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
        window.menuItem("Datei").requireVisible();
        window.menuItem("Fenster").requireVisible();
    }

    @Test
    public void shouldHaveToolbarWithButtons() {
        window.button("Neue View").requireVisible();
        window.button("DB-View").requireVisible();
    }

    @Test
    public void shouldOpenSampleViewFromMenu() {
        // Click menu item to open new view
        window.menuItem("Datei").click();
        window.menuItem("Neue View").click();

        // Verify internal frame was created
        JInternalFrameFixture internalFrame = window.internalFrame(
                new GenericTypeMatcher<JInternalFrame>(JInternalFrame.class) {
                    @Override
                    protected boolean isMatching(JInternalFrame frame) {
                        return "Kunden Analyse".equals(frame.getTitle());
                    }
                }
        );
        internalFrame.requireVisible();
    }

    @Test
    public void shouldOpenSampleViewFromToolbar() {
        window.button("Neue View").click();

        // Verify internal frame was created
        JInternalFrameFixture internalFrame = window.internalFrame(
                new GenericTypeMatcher<JInternalFrame>(JInternalFrame.class) {
                    @Override
                    protected boolean isMatching(JInternalFrame frame) {
                        return "Kunden Analyse".equals(frame.getTitle());
                    }
                }
        );
        internalFrame.requireVisible();
    }

    @Test
    public void shouldOpenDatabaseViewFromToolbar() {
        window.button("DB-View").click();

        // Verify internal frame was created
        JInternalFrameFixture internalFrame = window.internalFrame(
                new GenericTypeMatcher<JInternalFrame>(JInternalFrame.class) {
                    @Override
                    protected boolean isMatching(JInternalFrame frame) {
                        return "Datenbank".equals(frame.getTitle());
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
        window.menuItem("Fenster").click();
        window.menuItem("Kaskadiert anordnen").requireVisible();
        window.menuItem("Nebeneinander anordnen").requireVisible();
        window.menuItem("Untereinander anordnen").requireVisible();
    }

    @Test
    public void shouldCascadeWindows() {
        // Open multiple views
        window.button("Neue View").click();
        window.button("Neue View").click();

        // Cascade windows
        window.menuItem("Fenster").click();
        window.menuItem("Kaskadiert anordnen").click();

        // Both internal frames should still be visible
        // (We can't easily verify positions, but we can verify no errors occurred)
    }

    @Test
    public void shouldTileWindowsVertically() {
        // Open multiple views
        window.button("Neue View").click();
        window.button("Neue View").click();

        // Tile windows
        window.menuItem("Fenster").click();
        window.menuItem("Nebeneinander anordnen").click();
    }

    @Test
    public void shouldTileWindowsHorizontally() {
        // Open multiple views
        window.button("Neue View").click();
        window.button("Neue View").click();

        // Tile windows
        window.menuItem("Fenster").click();
        window.menuItem("Untereinander anordnen").click();
    }
}
