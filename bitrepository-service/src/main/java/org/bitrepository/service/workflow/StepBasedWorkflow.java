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
package org.bitrepository.service.workflow;

import org.bitrepository.common.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface for running a Workflow based on WorkflowSteps.
 * @see Workflow {@link Workflow}
 * @see WorkflowStep {@link WorkflowStep} 
 */
public abstract class StepBasedWorkflow implements Workflow {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The default state when the workflow is not running.*/
    public static final String NOT_RUNNING = "The workflow is currently not running.";
    /** The prefix for telling which step is currently running. Should be postfixed with the step name.*/
    public static final String PREFIX_FOR_RUNNING_STEP = "Performing step: ";
    /** The current step running.*/
    private WorkflowStep currentStep = null;
    private long currentRunStart = -1;

    @Override
    public void start() {
        currentRunStart = System.currentTimeMillis();
    }

    /**
     * Initiates the given step and sets it to the current running step.
     * @param step The step to start.
     */
    protected void performStep(WorkflowStep step) {
        this.currentStep = step;
        log.info("Starting step: '" + step.getName() + "'");
        try {
            step.performStep();
            log.info("Finished step: '" + step.getName() + "' in " 
                    + TimeUtils.millisecondsToHuman(step.getRunningTime()));
        } catch (Exception e) {
            log.error("Failure in step: '" + step.getName() + "'.", e);
        }
    }
    
    /**
     * For telling that the workflow has finished its task.
     */
    protected void finish() {
        this.currentStep = null;
        currentRunStart = -1;
        log.info("Finished " + getClass().getSimpleName()+ " in '" + TimeUtils.millisecondsToHuman(getRunningTime()));
    }
    
    @Override
    public String currentState() {
        if(currentStep == null) {
            return NOT_RUNNING;
        } else {
            return PREFIX_FOR_RUNNING_STEP + currentStep.getName() +
                    "\nRunning for " + TimeUtils.millisecondsToHuman(currentStep.getRunningTime()) + "/" +
                            TimeUtils.millisecondsToHuman(getRunningTime()) + ")";
        }
    }

    private long getRunningTime() {
        return System.currentTimeMillis() - currentRunStart;
    }
}
