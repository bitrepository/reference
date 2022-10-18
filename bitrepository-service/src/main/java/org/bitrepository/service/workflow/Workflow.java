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

import org.bitrepository.service.exception.WorkflowAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface for running a SchedulableJob based on WorkflowSteps.
 *
 * @see SchedulableJob {@link SchedulableJob}
 * @see WorkflowStep {@link WorkflowStep}
 */
public abstract class Workflow implements SchedulableJob {
    protected JobID jobID;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private WorkflowStep currentStep = null;
    private WorkflowState currentState = WorkflowState.NOT_RUNNING;
    private WorkflowStatistic statistics;

    @Override
    public synchronized void start() {
        statistics = new WorkflowStatistic(getClass().getSimpleName());
        statistics.start();
    }

    /**
     * Initiates the given step and sets it to the current running step.
     *
     * @param step The step to start.
     */
    protected void performStep(WorkflowStep step) {
        if (currentState != WorkflowState.ABORTED) {
            this.currentState = WorkflowState.RUNNING;
            this.currentStep = step;
            log.info("Starting step: '{}'", step.getName());
            try {
                statistics.startSubStatistic(step.getName());
                step.performStep();
            } catch (WorkflowAbortedException e) {
                this.currentState = WorkflowState.ABORTED;
                log.warn("Failure occurred, aborting workflow", e);
            } catch (Exception e) {
                log.error("Failure in step: '{}'", step.getName(), e);
                throw new RuntimeException("Failed to run step " + step.getName(), e);
            } finally {
                statistics.finishSubStatistic(getFinishedWorkflowStatus());
                log.info(statistics.getCurrentSubStatistic().toString());
            }
        }
    }

    /**
     * For telling that the workflow has finished its task.
     */
    protected void finish() {
        statistics.finish(getFinishedWorkflowStatus());
        this.currentState = WorkflowState.NOT_RUNNING;
        this.currentStep = null;
        log.info(statistics.getFullStatistics());
    }

    /**
     * Get the final state of the workflow.
     *
     * @return ABORTED if current state is ABORTED, SUCCEEDED otherwise.
     */
    private WorkflowState getFinishedWorkflowStatus() {
        return (currentState == WorkflowState.ABORTED ? WorkflowState.ABORTED : WorkflowState.SUCCEEDED);
    }

    @Override
    public WorkflowState currentState() {
        return currentState;
    }

    @Override
    public void setCurrentState(WorkflowState newState) {
        this.currentState = newState;
    }

    @Override
    public String getHumanReadableState() {
        if (currentStep == null) {
            return currentState.name();
        } else {
            return currentStep.getName();
        }
    }

    /**
     * @return The statistics for this workflow.
     */
    public synchronized WorkflowStatistic getWorkflowStatistics() {
        if (statistics == null) {
            statistics = new WorkflowStatistic(getClass().getSimpleName());
        }
        return statistics;
    }

    @Override
    public JobID getJobID() {
        return jobID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        Workflow that = (Workflow) o;

        return jobID.equals(that.jobID);
    }

    @Override
    public int hashCode() {
        return jobID.hashCode();
    }
}
