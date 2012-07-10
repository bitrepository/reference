package org.bitrepository.integrityservice.workflow;

import org.bitrepository.integrityservice.workflow.step.WorkflowStep;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StepBasedWorkflowTest extends ExtendedTestCase {
    String NAME_OF_STEP = "workflowstep";
    @Test(groups = {"regressiontest", "integritytest"})
    public void testStepBasedWorkflow() {
        addDescription("Testing the step-based workflow.");
        StepBasedWorkflow workflow = new StepBasedWorkflow() {
            @Override
            public void start() {
                WorkflowStep step = new WorkflowStep() {
                    @Override
                    public void performStep() {
                    }
                    
                    @Override
                    public String getName() {
                        return NAME_OF_STEP;
                    }
                };
                
                performStep(step);
                Assert.assertEquals(step.getName(), NAME_OF_STEP);
                Assert.assertTrue(currentState().contains(NAME_OF_STEP));
                
                finish();
                Assert.assertFalse(currentState().contains(NAME_OF_STEP));
            }
        };
        
        workflow.start();
    }

}
