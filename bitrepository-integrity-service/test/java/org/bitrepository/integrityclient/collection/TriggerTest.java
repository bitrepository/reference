package org.bitrepository.integrityclient.collection;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

/**
 * Test that triggering works.
 */
public class TriggerTest extends ExtendedTestCase {
    @Test(groups = "specificationonly")
    void testTriggerOnOldData() throws Exception {
        addDescription("Test that triggering on old data works");
        addStep("Set up the system", "No errors");
        addStep("Test that the trigger triggers when data is old", "Trigger responds with true");
        addStep("Test that event is to collect information", "Trigger calls information collection methods");
    }

    @Test(groups = "specificationonly")
    void testTriggerAtIntervals() throws Exception {
        addDescription("Test that a trigger that should respond with true at certain intervals does so.");
        addStep("Set up a trigger that triggers once every two seconds", "No errors");
        addStep("Check if triggered every half second", "Trigger reports true every two seconds");
    }

    @Test(groups = "specificationonly")
    void testTriggerOfRandomSet() throws Exception {
        addDescription("Test that a trigger that should generate collection on random files does so.");
        addStep("Set up a trigger that generates collection of checksums on three random files", "No errors");
        addStep("Pull the trigger 10 times", "Trigger generates collection event for three random files");
    }
}
