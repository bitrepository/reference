package org.bitrepository.integrityservice.workflow.step;

/**
 * The interface for a step for a workflow.
 */
public interface WorkflowStep {
    /**
     * @return The name of this given step in the workflow.
     */
    String getName();
    
    /**
     * Perform the task wrapped in this step.
     */
    void performStep();
}
