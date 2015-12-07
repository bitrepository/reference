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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides details about the statistics of an workflow. The statistics will not be fully populated until the
 * workflow has finished.
 */
public class WorkflowStatistic {
    private final String name;
    private WorkflowState finishState;
    private Date start;
    private Date finish;
    private final List<WorkflowStatistic> subStatistics = new LinkedList<WorkflowStatistic>();

    private static final String LINEFEED = "\n";

    /**
     * Creates a fresh <code>WorkflowStatistic</code> instance.
     * @param name The name of this instance to be used as title for the statistics.
     */
    public WorkflowStatistic(String name) {
        this.name = name;
    }

    /**
     * Marks the start time of the statistics.
     */
    public void start() {
        start = new Date();
    }

    /**
     * Marks the start time of a sub step of the statistics statistics.
     * @param name The name of the substep to be used as identifier in the statistics.
     */
    public void startSubStatistic(String name) {
        subStatistics.add(new WorkflowStatistic(name));
        getCurrentSubStatistic().start();
    }

    /**
     * Marks the end time of a sub step of the statistics statistics.
     * @param state what state the sub step ended in.
     */
    public void finishSubStatistic(WorkflowState state) {
        getCurrentSubStatistic().finish(state);
    }

    /**
     * @return A string representation of statistics, including all substatistics.
     */
    public String getFullStatistics() {
        if (start == null) {
            return "Haven't finished a run yet";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getName() + " duration: " + TimeUtils.millisecondsToHuman(getDuration()));
        for (WorkflowStatistic stepStat: subStatistics) {
            sb.append(LINEFEED +stepStat.getFullStatistics());
        }
        return sb.toString();
    }

    /**
     * Humanly readable string indicating how long the current step has run and the total time the workflow has flown.
     * @return a string of the form 'step duration'/'workflow duration'. If the workflow isn't running
     * a "Not running" string will be returned.
     */
    public String getPartStatistics() {
        if (start == null) {
            return "Not started yet";
        }
        else if (finish != null ) {
            return "Idle";
        } else {
            WorkflowStatistic currentSubStatistic = getCurrentSubStatistic();
            return currentSubStatistic.getName() + LINEFEED +
                    "Running for " +
                    TimeUtils.millisecondsToHuman(currentSubStatistic.getDuration()) + "/" +
                    TimeUtils.millisecondsToHuman(getDuration()) + ")";
        }
    }

    /**
     * Helper to get the status of the latest step.
     * @return statistics for the current step if the workflow statistics have an active step.
     */
    public WorkflowStatistic getCurrentSubStatistic() {
        if (subStatistics.isEmpty() || finish != null) {
            return null;
        } else return subStatistics.get(subStatistics.size()-1);
    }

    /**
     * Get the start date of the statistics.
     * @return starting time as a Date.
     */
    public Date getStart() {
        return start;
    }

    /**
     * Get the finish date of the statistics.
     * @return finish time as a Date.
     */
    public Date getFinish() {
        return finish;
    }
    
    /**
     * Get the final state of the workflow.
     * @return the final state of the workflow as a WorkflowState.
     */
    public WorkflowState getFinishState() {
        return finishState;
    }
    
    /**
     * Mark this part of the workflow as finished.
     * @param finishState The state at which the workflow (or step) finished at
     */
    public void finish(WorkflowState finishState) {
        this.finish = new Date();
        this.finishState = finishState;
    }

    public String getName() {
        return name;
    }

    /**
     * @return The duration of the workflow if it has been started, else 0.
     */
    private long getDuration() {
        if (start == null) {
            return 0;
        }
        if (getFinish() == null) {
            return System.currentTimeMillis() - start.getTime();
        } else {
            return finish.getTime() - start.getTime();
        }
    }

    @Override
    public String toString() {
        return "WorkflowStatistic{" +
                "name='" + name + '\'' +
                ", start=" + start +
                ", finish=" + finish +
                ", subStatistics=" + subStatistics +
                '}';
    }
}
