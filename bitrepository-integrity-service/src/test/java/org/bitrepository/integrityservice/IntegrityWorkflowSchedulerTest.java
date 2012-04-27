/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.integrityservice;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.integrityservice.mocks.MockWorkflow;
import org.bitrepository.integrityservice.workflow.TimerWorkflowScheduler;
import org.bitrepository.integrityservice.workflow.Workflow;
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

    //@Test(groups = {"regressiontest"})
    public void testSchedulerCalls() throws Exception {
        addDescription("Test that schedulers call all workflow at the given intervals.");
        addStep("Set a scheduler that should run every second", "No errors");
        settings.getReferenceSettings().getIntegrityServiceSettings().setSchedulerInterval(1000L);
        TimerWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
        
        addStep("Add three workflows that record methods calls. All with an interval of 2 seconds.", "No errors");
        MockWorkflow workflow1 = new MockWorkflow(2000L, "mock-1");
        MockWorkflow workflow2 = new MockWorkflow(2000L, "mock-2");
        MockWorkflow workflow3 = new MockWorkflow(2000L, "mock-3");
        scheduler.putWorkflow(workflow1);
        scheduler.putWorkflow(workflow2);
        scheduler.putWorkflow(workflow3);
        addStep("Wait four seconds the interval (plus extra for instantiation). Then remove the workflows.",
                "All workflows should be queried at least thrice, and called at least once at most twice");
        try {
            synchronized(this) {
                wait(4050L);
            }
        } catch (Exception e) {
            // unexpected, 
        }
        scheduler.removeWorkflow(workflow1.getName());
        scheduler.removeWorkflow(workflow1.getName());
        scheduler.removeWorkflow(workflow1.getName());
        
        Assert.assertEquals(workflow1.getNextRunCount(), 5, "Should call getNextRunCount at least thrice, was " 
                + workflow1.getNextRunCount());
        Assert.assertEquals(workflow2.getNextRunCount(), 5, "Should call getNextRunCount at least thrice, was " 
                + workflow2.getNextRunCount());
        Assert.assertEquals(workflow3.getNextRunCount(), 5, "Should call getNextRunCount at least thrice, was " 
                + workflow3.getNextRunCount());
        
        Assert.assertTrue(workflow1.getWorkflowCalled() >= 1, "Should call getWorkflowCalled at least once");
        Assert.assertTrue(workflow2.getWorkflowCalled() >= 1, "Should call getWorkflowCalled at least once");
        Assert.assertTrue(workflow3.getWorkflowCalled() >= 1, "Should call getWorkflowCalled at least once");
        Assert.assertTrue(workflow1.getWorkflowCalled() <= 2, "Should call getWorkflowCalled at most twice, was "
                + workflow1.getWorkflowCalled());
        Assert.assertTrue(workflow2.getWorkflowCalled() <= 2, "Should call getWorkflowCalled at most twice, was "
                + workflow2.getWorkflowCalled());
        Assert.assertTrue(workflow3.getWorkflowCalled() <= 2, "Should call getWorkflowCalled at most twice, was "
                + workflow3.getWorkflowCalled());
    }
    
    @Test(groups = {"regressiontest"})
    public void testSchedulerContainingWorkflows() {
        addDescription("Test that schedulers call all workflow at the given intervals.");
        addStep("Setup a scheduler and validate initial state", "No errors and no workflows");
        TimerWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
        Assert.assertEquals(scheduler.getWorkflows().size(), 0, "Should not be any workflows in the scheduler.");
        
        addStep("Make a new workflow, add it to the scheduler and extract it afterwards.", 
                "Should extract the same workflow");
        Workflow testWorkflow = new MockWorkflow(3600000L, "testWorkflow");
        scheduler.putWorkflow(testWorkflow);
        Assert.assertEquals(scheduler.getWorkflows().size(), 1, "Should only be one workflow in the scheduler.");
        Assert.assertEquals(scheduler.getWorkflows().get(0), testWorkflow, "Should be the same workflow.");
        
        addStep("Add the workflow again to the scheduler", "Should still be only the one and same workflow in the scheduler");
        scheduler.putWorkflow(testWorkflow);
        Assert.assertEquals(scheduler.getWorkflows().size(), 1, "Should only be one workflow in the scheduler.");
        Assert.assertEquals(scheduler.getWorkflows().get(0), testWorkflow, "Should be the same workflow.");        
        
        addStep("Remove the workflow from the scheduler two times", 
                "Should not be any workflows in the scheduler, and only successfully remove workflow once.");
        Assert.assertTrue(scheduler.removeWorkflow(testWorkflow.getName()));
        Assert.assertEquals(scheduler.getWorkflows().size(), 0, "Should not be any workflows in the scheduler.");
        Assert.assertFalse(scheduler.removeWorkflow(testWorkflow.getName()));
    }
}
