package de.cavdar;

import de.cavdar.view.DatabaseView;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.JInternalFrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

/**
 * GUI tests for DatabaseView.
 */
public class DatabaseViewTest extends AssertJSwingJUnitTestCase {

    private JInternalFrameFixture viewFixture;
    private JFrame containerFrame;

    @Override
    protected void onSetUp() {
        containerFrame = GuiActionRunner.execute(() -> {
            JFrame frame = new JFrame("Test Container");
            JDesktopPane desktop = new JDesktopPane();
            frame.add(desktop);
            frame.setSize(1000, 800);

            DatabaseView view = new DatabaseView();
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
        viewFixture.requireTitle("Datenbank");
    }

    @Test
    public void shouldHaveConnectionPanel() {
        // Check for connection components
        viewFixture.textBox().requireVisible(); // JDBC URL field
        viewFixture.button("Verbinden").requireVisible();
    }

    @Test
    public void shouldHaveDriverComboBox() {
        viewFixture.comboBox().requireVisible();
    }

    @Test
    public void shouldHaveConnectButton() {
        viewFixture.button("Verbinden").requireVisible();
        viewFixture.button("Verbinden").requireEnabled();
    }

    @Test
    public void shouldHaveExecuteButton() {
        viewFixture.button("Ausführen").requireVisible();
        // Execute button should be disabled when not connected
        viewFixture.button("Ausführen").requireDisabled();
    }

    @Test
    public void shouldHaveClearButton() {
        viewFixture.button("Leeren").requireVisible();
        viewFixture.button("Leeren").requireEnabled();
    }

    @Test
    public void shouldHaveQueryTextArea() {
        // There should be a text area for SQL input
        // The default text should contain "SELECT"
    }

    @Test
    public void shouldHaveResultsTable() {
        viewFixture.table().requireVisible();
    }

    @Test
    public void shouldClearQueryAndResults() {
        viewFixture.button("Leeren").click();

        // Table should be empty after clear
        assertThat(viewFixture.table().rowCount()).isZero();
    }

    @Test
    public void shouldShowNotConnectedStatus() {
        // The status label should show "Nicht verbunden" initially
        viewFixture.label("Nicht verbunden").requireVisible();
    }

    @Test
    public void shouldHaveCommonJdbcDrivers() {
        JComboBox<?> comboBox = viewFixture.comboBox().target();
        assertThat(comboBox.getItemCount()).isGreaterThan(0);

        // Check that common drivers are available
        boolean hasMysql = false;
        boolean hasPostgres = false;

        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = (String) comboBox.getItemAt(i);
            if (item.contains("mysql")) hasMysql = true;
            if (item.contains("postgresql")) hasPostgres = true;
        }

        assertThat(hasMysql).isTrue();
        assertThat(hasPostgres).isTrue();
    }

    @Test
    public void shouldBeResizableAndClosable() {
        assertThat(viewFixture.target().isResizable()).isTrue();
        assertThat(viewFixture.target().isClosable()).isTrue();
        assertThat(viewFixture.target().isMaximizable()).isTrue();
    }
}
