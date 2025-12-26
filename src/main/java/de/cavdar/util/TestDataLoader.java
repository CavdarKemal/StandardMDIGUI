package de.cavdar.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cavdar.model.TestCrefo;
import de.cavdar.model.TestCustomer;
import de.cavdar.model.TestScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for loading and saving test data from/to JSON files.
 *
 * @author StandardMDIGUI
 * @version 1.0
 * @since 2024-12-26
 */
public class TestDataLoader {
    private static final Logger LOG = LoggerFactory.getLogger(TestDataLoader.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestDataLoader() {
        // Utility class
    }

    /**
     * Loads test customers from a JSON file.
     *
     * @param file the JSON file to load
     * @return list of test customers
     * @throws IOException if loading fails
     */
    public static List<TestCustomer> loadFromJson(File file) throws IOException {
        LOG.info("Loading test data from: {}", file.getAbsolutePath());
        List<TestCustomer> customers = MAPPER.readValue(file, new TypeReference<List<TestCustomer>>() {});

        // Re-establish parent references
        for (TestCustomer customer : customers) {
            for (TestScenario scenario : customer.getTestScenariosMap().values()) {
                scenario.setTestCustomer(customer);
            }
        }

        LOG.info("Loaded {} customers", customers.size());
        return customers;
    }

    /**
     * Saves test customers to a JSON file.
     *
     * @param customers the customers to save
     * @param file the file to save to
     * @throws IOException if saving fails
     */
    public static void saveToJson(List<TestCustomer> customers, File file) throws IOException {
        LOG.info("Saving test data to: {}", file.getAbsolutePath());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, customers);
        LOG.info("Saved {} customers", customers.size());
    }

    /**
     * Creates sample test data for demonstration.
     *
     * @return list of sample test customers
     */
    public static List<TestCustomer> createSampleData() {
        List<TestCustomer> customers = new ArrayList<>();

        // Customer 1: CUST01
        TestCustomer cust1 = new TestCustomer("CUST01", "Mustermann GmbH");
        cust1.setJvmName("JVM_ENE_01");
        cust1.setTestPhase("PHASE1");
        cust1.setActivated(true);

        TestScenario scenario1 = new TestScenario("Buchung_Standard");
        scenario1.setActivated(true);
        TestCrefo crefo1 = new TestCrefo("BU_001", "Standard Buchung");
        crefo1.setItsqTestCrefoNr(1001L);
        crefo1.setActivated(true);
        scenario1.addTestCrefo(crefo1);

        TestCrefo crefo2 = new TestCrefo("BU_002", "Buchung mit Rabatt");
        crefo2.setItsqTestCrefoNr(1002L);
        crefo2.setActivated(true);
        scenario1.addTestCrefo(crefo2);

        TestCrefo crefo3 = new TestCrefo("BU_003", "Storno Buchung");
        crefo3.setItsqTestCrefoNr(1003L);
        crefo3.setActivated(false);
        scenario1.addTestCrefo(crefo3);

        cust1.addTestScenario(scenario1);

        TestScenario scenario2 = new TestScenario("Zahlung_Eingang");
        scenario2.setActivated(true);
        TestCrefo crefo4 = new TestCrefo("ZE_001", "Zahlung Lastschrift");
        crefo4.setItsqTestCrefoNr(2001L);
        crefo4.setActivated(true);
        scenario2.addTestCrefo(crefo4);

        TestCrefo crefo5 = new TestCrefo("ZE_002", "Zahlung Überweisung");
        crefo5.setItsqTestCrefoNr(2002L);
        crefo5.setActivated(true);
        scenario2.addTestCrefo(crefo5);

        cust1.addTestScenario(scenario2);
        customers.add(cust1);

        // Customer 2: CUST02
        TestCustomer cust2 = new TestCustomer("CUST02", "Beispiel AG");
        cust2.setJvmName("JVM_ENE_02");
        cust2.setTestPhase("PHASE2");
        cust2.setActivated(true);

        TestScenario scenario3 = new TestScenario("Export_Daten");
        scenario3.setActivated(false);
        TestCrefo crefo6 = new TestCrefo("EX_001", "CSV Export");
        crefo6.setItsqTestCrefoNr(3001L);
        crefo6.setActivated(true);
        scenario3.addTestCrefo(crefo6);

        cust2.addTestScenario(scenario3);
        customers.add(cust2);

        // Customer 3: CUST03 (deactivated)
        TestCustomer cust3 = new TestCustomer("CUST03", "Inaktiv KG");
        cust3.setJvmName("JVM_ENE_03");
        cust3.setTestPhase("PHASE1");
        cust3.setActivated(false);

        TestScenario scenario4 = new TestScenario("Alte_Tests");
        scenario4.setActivated(false);
        TestCrefo crefo7 = new TestCrefo("OLD_001", "Alter Test");
        crefo7.setItsqTestCrefoNr(9001L);
        crefo7.setActivated(false);
        scenario4.addTestCrefo(crefo7);

        cust3.addTestScenario(scenario4);
        customers.add(cust3);

        LOG.info("Created {} sample customers", customers.size());
        return customers;
    }
}
