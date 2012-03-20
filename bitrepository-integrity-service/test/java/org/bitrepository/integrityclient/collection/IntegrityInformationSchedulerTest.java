package org.bitrepository.integrityclient.collection;

import org.bitrepository.integrityclient.collection.triggers.MockTrigger;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that scheduler calls triggers.
 */
public class IntegrityInformationSchedulerTest extends ExtendedTestCase {
    @Test(groups = "integrationtest")
    public void testScheduler() throws Exception {
        addDescription("Test that schedulers call all triggers");
        addStep("Set a scheduler that should run every second", "No errors");
        IntegrityInformationScheduler scheduler = new TimerIntegrityInformationScheduler(1000L);
        addStep("Add three triggers that record methods calls, and trigger every other second", "No errors");
        MockTrigger trigger1 = new MockTrigger();
        MockTrigger trigger2 = new MockTrigger();
        MockTrigger trigger3 = new MockTrigger();
        scheduler.addTrigger(trigger1);
        scheduler.addTrigger(trigger2);
        scheduler.addTrigger(trigger3);
        addStep("Wait three seconds",
                "All triggers should be queried at least thrice, and called at least once at most twice");
        Thread.currentThread().sleep(3000L);
        Assert.assertTrue(trigger1.getIsTriggeredCalled() >= 3, "Should call isTriggered at least thrice");
        Assert.assertTrue(trigger2.getIsTriggeredCalled() >= 3, "Should call isTriggered at least thrice");
        Assert.assertTrue(trigger3.getIsTriggeredCalled() >= 3, "Should call isTriggered at least thrice");
        Assert.assertTrue(trigger1.getTriggerCalled() >= 1, "Should call trigger at least once");
        Assert.assertTrue(trigger2.getTriggerCalled() >= 1, "Should call trigger at least once");
        Assert.assertTrue(trigger3.getTriggerCalled() >= 1, "Should call trigger at least once");
        Assert.assertTrue(trigger1.getTriggerCalled() <= 2, "Should call trigger at most twice, was "
                + trigger1.getTriggerCalled());
        Assert.assertTrue(trigger2.getTriggerCalled() <= 2, "Should call trigger at most twice, was "
                + trigger2.getTriggerCalled());
        Assert.assertTrue(trigger3.getTriggerCalled() <= 2, "Should call trigger at most twice, was "
                + trigger3.getTriggerCalled());
    }
}
