/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityclient.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler that uses Timer to trigger events.
 */
public class TimerWorkflowScheduler implements IntegrityWorkflowScheduler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The timer that schedules events. */
    private final Timer timer;
    /** The period between testing whether triggers have triggered. */
    private final long interval;
    /** The map between the running timertasks and their names.*/
    private Map<String, TriggerTimerTask> workflowTimerTasks = new HashMap<String, TriggerTimerTask>();
    
    /** The name of the timer.*/
    private final String TIMER_NAME = "Integrity Information Scheduler";
    /** Whether the timer is a deamon.*/
    private final boolean TIMER_IS_DEAMON = true;
    /** A timer delay of 0 seconds.*/
    private final Long NO_DELAY = 0L;

    /** Setup a timer task for triggering all triggers at requested interval.
     *
     * @param configuration The configuration for the collection. Currently contains polling interval.
     */
    public TimerWorkflowScheduler(Settings settings) {
        this.interval = settings.getReferenceSettings().getIntegrityServiceSettings().getSchedulerInterval();
        timer = new Timer(TIMER_NAME, TIMER_IS_DEAMON);
    }

    @Override
    public void putTrigger(String name, Workflow workflow) {
        if(removeTrigger(name)) {
            log.info("Recreated workflow named '" + name + "': " + workflow);
        } else {
            log.debug("Created a workflow named '" + name + "': " + workflow);
        }
        TriggerTimerTask task = new TriggerTimerTask(workflow);
        timer.scheduleAtFixedRate(task, NO_DELAY, interval);
        
        workflowTimerTasks.put(name, task);
    }
    
    @Override
    public boolean removeTrigger(String name) {
        TimerTask task = workflowTimerTasks.remove(name);
        if(task == null) {
            return false;
        }
        
        task.cancel();
        return true;
    }
    
    @Override
    public List<Workflow> getWorkflows() {
        List<Workflow> workflows = new ArrayList<Workflow>();
        for(TriggerTimerTask task : workflowTimerTasks.values()) {
            workflows.add(task.getWorkflow());
        }
        
        return workflows;
    }
    
    /**
     * TimerTask for the triggers.
     */
    private static class TriggerTimerTask extends TimerTask {
        /** The trigger to test and run. */
        private Workflow workflow;

        /** Initialise a task that tests a trigger and runs it if it has triggered.
         *
         * @param trigger The trigger to test and run.
         */
        public TriggerTimerTask(Workflow trigger) {
            this.workflow = trigger;
        }

        @Override
        public void run() {
            if(workflow.getNextRun().getTime() < System.currentTimeMillis()) {
                workflow.trigger();
            }
        }
        
        public Workflow getWorkflow() {
            return workflow;
        }
    }
}
