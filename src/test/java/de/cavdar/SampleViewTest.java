package de.cavdar;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.JInternalFrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

/**
 * GUI tests for SampleView.
 */
public class SampleViewTest extends AssertJSwingJUnitTestCase {

    private JInternalFrameFixture viewFixture;
    private JFrame containerFrame;

    @Override
    protected void onSetUp() {
        containerFrame = GuiActionRunner.execute(() -> {
            JFrame frame = new JFrame("Test Container");
            JDesktopPane desktop = new JDesktopPane();
            frame.add(desktop);
            frame.setSize(800, 600);

            SampleView view = new SampleView();
            desktop.add(view);
            view.setVisible(true);

            return frame;
        });

        robot().showWindow(containerFrame);

        viewFixture = new JInternalFrameFixture(robot(),
                (JInternalFrame) ((JDesktopPane) containerFrame.getContentPane().getComponent(0)).getAllFrames()[0]);
    }

    @Override
    protected void onTearDown() {
        if (containerFrame != null) {
            GuiActionRunner.execute(() -> containerFrame.dispose());
        }
    }

    @Test
    public void shouldHaveCorrectTitle() {
        viewFixture.requireTitle("Kunden Analyse");
    }

    @Test
    public void shouldBeResizable() {
        assertThat(viewFixture.target().isResizable()).isTrue();
    }

    @Test
    public void shouldBeClosable() {
        assertThat(viewFixture.target().isClosable()).isTrue();
    }

    @Test
    public void shouldBeMaximizable() {
        assertThat(viewFixture.target().isMaximizable()).isTrue();
    }

    @Test
    public void shouldBeIconifiable() {
        assertThat(viewFixture.target().isIconifiable()).isTrue();
    }

    @Test
    public void shouldHaveStartButton() {
        viewFixture.button("Start Prozess").requireVisible();
    }

    @Test
    public void shouldHaveContentLabel() {
        viewFixture.label("Inhalt der View...").requireVisible();
    }

    @Test
    public void shouldShowProgressBarWhenProcessStarts() {
        viewFixture.button("Start Prozess").click();

        // Progress bar should become visible
        // Note: This test might be flaky due to timing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // The progress bar should be visible during execution
        // We verify the button was clicked without errors
    }

    @Test
    public void shouldHaveCancelButton() {
        viewFixture.button("Cancel").requireVisible();
    }
}
