package org.bitrepository.integrityservice;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.integrityservice.mocks.MockWorkflow;
import org.bitrepository.integrityservice.workflow.IntegrityWorkflowScheduler;
import org.bitrepository.integrityservice.workflow.TimerWorkflowScheduler;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test that scheduler calls triggers.
 */
public class IntegrityWorkflowSchedulerTest extends ExtendedTestCase {
    Settings settings;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
    }

    @Test(groups = {"regressiontest"})
    public void testScheduler() throws Exception {
        addDescription("Test that schedulers call all workflow at the given intervals.");
        addStep("Set a scheduler that should run every second", "No errors");
        settings.getReferenceSettings().getIntegrityServiceSettings().setSchedulerInterval(1000L);
        IntegrityWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
        
        addStep("Add three workflows that record methods calls. All with an interval of 2 seconds.", "No errors");
        MockWorkflow trigger1 = new MockWorkflow(2000L, "mock-1");
        MockWorkflow trigger2 = new MockWorkflow(2000L, "mock-2");
        MockWorkflow trigger3 = new MockWorkflow(2000L, "mock-3");
        scheduler.putWorkflow(trigger1);
        scheduler.putWorkflow(trigger2);
        scheduler.putWorkflow(trigger3);
        addStep("Wait four seconds the interval (plus extra for instantiation). Then remove the workflows.",
                "All workflows should be queried at least thrice, and called at least once at most twice");
        try {
            synchronized(this) {
                wait(4050L);
            }
        } catch (Exception e) {
            // unexpected, 
        }
        scheduler.removeWorkflow(trigger1.getName());
        scheduler.removeWorkflow(trigger1.getName());
        scheduler.removeWorkflow(trigger1.getName());
        
        Assert.assertEquals(trigger1.getNextRunCount(), 5, "Should call getNextRunCount at least thrice, was " 
                + trigger1.getNextRunCount());
        Assert.assertEquals(trigger2.getNextRunCount(), 5, "Should call getNextRunCount at least thrice, was " 
                + trigger2.getNextRunCount());
        Assert.assertEquals(trigger3.getNextRunCount(), 5, "Should call getNextRunCount at least thrice, was " 
                + trigger3.getNextRunCount());
        
        Assert.assertTrue(trigger1.getWorkflowCalled() >= 1, "Should call getWorkflowCalled at least once");
        Assert.assertTrue(trigger2.getWorkflowCalled() >= 1, "Should call getWorkflowCalled at least once");
        Assert.assertTrue(trigger3.getWorkflowCalled() >= 1, "Should call getWorkflowCalled at least once");
        Assert.assertTrue(trigger1.getWorkflowCalled() <= 2, "Should call getWorkflowCalled at most twice, was "
                + trigger1.getWorkflowCalled());
        Assert.assertTrue(trigger2.getWorkflowCalled() <= 2, "Should call getWorkflowCalled at most twice, was "
                + trigger2.getWorkflowCalled());
        Assert.assertTrue(trigger3.getWorkflowCalled() <= 2, "Should call getWorkflowCalled at most twice, was "
                + trigger3.getWorkflowCalled());
    }
}
