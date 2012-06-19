package org.bitrepository.integrityservice.scheduler.workflow;

import org.bitrepository.integrityservice.scheduler.workflow.step.WorkflowStep;

/**
 * Abstract interface for running a Workflow based on WorkflowSteps.
 * @see Workflow {@link Workflow}
 * @see WorkflowStep {@link WorkflowStep} 
 */
public abstract class StepBasedWorkflow implements Workflow {
    /** The default state when the workflow is not running.*/
    private static final String NOT_RUNNING = "The workflow is currently not running.";
    /** The prefix for telling which step is currently running. Should be postfixed with the step name.*/
    private static final String PREFIX_FOR_RUNNING_STEP = "Performing step: ";
    /** The current step running.*/
    private WorkflowStep currentStep = null;
    
    /**
     * Initiates the given step and sets it to the current running step.
     * @param step The step to start.
     */
    protected void performStep(WorkflowStep step) {
        this.currentStep = step;
        step.performStep();
    }
    
    /**
     * For telling that the workflow has finished its task.
     */
    protected void finish() {
        this.currentStep = null;
    }
    
    @Override
    public String currentState() {
        if(currentStep == null) {
            return NOT_RUNNING;
        }
        return PREFIX_FOR_RUNNING_STEP + currentStep.getName();
    }
}
