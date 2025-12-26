package de.cavdar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a test customer containing multiple test scenarios.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-26
 */
public class TestCustomer {
    private String customerKey;
    private String jvmName;
    private String customerName;
    private String testPhase;
    @JsonIgnore
    private File testResultsFile;
    private boolean activated = true;
    private Map<String, TestScenario> testScenariosMap = new TreeMap<>();

    public TestCustomer() {
    }

    public TestCustomer(String customerKey, String customerName) {
        this.customerKey = customerKey;
        this.customerName = customerName;
    }

    // ===== TestScenario Management =====

    public void addTestScenario(TestScenario scenario) {
        scenario.setTestCustomer(this);
        testScenariosMap.put(scenario.getScenarioName(), scenario);
    }

    public TestScenario getTestScenario(String scenarioName) {
        return testScenariosMap.get(scenarioName);
    }

    public void removeTestScenario(String scenarioName) {
        testScenariosMap.remove(scenarioName);
    }

    // ===== Getters and Setters =====

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTestPhase() {
        return testPhase;
    }

    public void setTestPhase(String testPhase) {
        this.testPhase = testPhase;
    }

    public File getTestResultsFile() {
        return testResultsFile;
    }

    public void setTestResultsFile(File testResultsFile) {
        this.testResultsFile = testResultsFile;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Map<String, TestScenario> getTestScenariosMap() {
        return testScenariosMap;
    }

    public void setTestScenariosMap(Map<String, TestScenario> testScenariosMap) {
        this.testScenariosMap = testScenariosMap;
    }

    @Override
    public String toString() {
        return customerKey + " - " + customerName;
    }
}
