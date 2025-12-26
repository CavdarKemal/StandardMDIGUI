package de.cavdar.model;

/**
 * Represents a test case (Crefo) within a test scenario.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-26
 */
public class TestCrefo {
    private String testFallName;
    private String testFallInfo;
    private Long itsqTestCrefoNr;
    private Long pseudoCrefoNr;
    private boolean shouldBeExported;
    private boolean activated = true;
    private boolean exported = false;

    public TestCrefo() {
    }

    public TestCrefo(String testFallName) {
        this.testFallName = testFallName;
    }

    public TestCrefo(String testFallName, String testFallInfo) {
        this.testFallName = testFallName;
        this.testFallInfo = testFallInfo;
    }

    // ===== Getters and Setters =====

    public String getTestFallName() {
        return testFallName;
    }

    public void setTestFallName(String testFallName) {
        this.testFallName = testFallName;
    }

    public String getTestFallInfo() {
        return testFallInfo;
    }

    public void setTestFallInfo(String testFallInfo) {
        this.testFallInfo = testFallInfo;
    }

    public Long getItsqTestCrefoNr() {
        return itsqTestCrefoNr;
    }

    public void setItsqTestCrefoNr(Long itsqTestCrefoNr) {
        this.itsqTestCrefoNr = itsqTestCrefoNr;
    }

    public Long getPseudoCrefoNr() {
        return pseudoCrefoNr;
    }

    public void setPseudoCrefoNr(Long pseudoCrefoNr) {
        this.pseudoCrefoNr = pseudoCrefoNr;
    }

    public boolean isShouldBeExported() {
        return shouldBeExported;
    }

    public void setShouldBeExported(boolean shouldBeExported) {
        this.shouldBeExported = shouldBeExported;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    @Override
    public String toString() {
        return testFallName;
    }
}
