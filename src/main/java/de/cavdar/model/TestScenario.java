package de.cavdar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a test scenario containing multiple test cases (Crefos).
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-26
 */
public class TestScenario {
    private boolean activated = true;
    @JsonIgnore
    private TestCustomer testCustomer;
    private String scenarioName;
    private Map<String, TestCrefo> testFallNameToTestCrefoMap = new TreeMap<>();

    public TestScenario() {
    }

    public TestScenario(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public TestScenario(String scenarioName, TestCustomer testCustomer) {
        this.scenarioName = scenarioName;
        this.testCustomer = testCustomer;
    }

    // ===== TestCrefo Management =====

    public void addTestCrefo(TestCrefo testCrefo) {
        testFallNameToTestCrefoMap.put(testCrefo.getTestFallName(), testCrefo);
    }

    public TestCrefo getTestCrefo(String testFallName) {
        return testFallNameToTestCrefoMap.get(testFallName);
    }

    public void removeTestCrefo(String testFallName) {
        testFallNameToTestCrefoMap.remove(testFallName);
    }

    // ===== Getters and Setters =====

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public TestCustomer getTestCustomer() {
        return testCustomer;
    }

    public void setTestCustomer(TestCustomer testCustomer) {
        this.testCustomer = testCustomer;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public Map<String, TestCrefo> getTestFallNameToTestCrefoMap() {
        return testFallNameToTestCrefoMap;
    }

    public void setTestFallNameToTestCrefoMap(Map<String, TestCrefo> testFallNameToTestCrefoMap) {
        this.testFallNameToTestCrefoMap = testFallNameToTestCrefoMap;
    }

    @Override
    public String toString() {
        return scenarioName;
    }
}
