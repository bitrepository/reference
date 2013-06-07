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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.service.scheduler.TimerbasedScheduler;
import org.bitrepository.service.scheduler.WorkflowScheduler;
import org.bitrepository.settings.referencesettings.Schedule;
import org.bitrepository.settings.referencesettings.WorkflowConfiguration;
import org.bitrepository.settings.referencesettings.WorkflowSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final WorkflowScheduler scheduler;
    private final WorkflowContext context;
    private final Map<WorkflowID, SchedulableJob> workflows = new HashMap<WorkflowID, SchedulableJob>();

    public WorkflowManager(
            WorkflowContext context,
            WorkflowSettings configuration,
            long schedulerInterval) {
        this.context = context;
        scheduler = new TimerbasedScheduler(schedulerInterval);
        loadWorkFlows(configuration);
    }

    public String startWorkflow(WorkflowID workflowID) {
        SchedulableJob workflowToStart = workflows.get(workflowID);
        return scheduler.startWorkflow(workflowToStart);
    }

    public Collection<WorkflowTimerTask> getWorkflows(String collectionID) {
        return scheduler.getWorkflows(collectionID);
    }

    private void loadWorkFlows(WorkflowSettings configuration) {
        for (WorkflowConfiguration workflowConf:configuration.getWorkflow()) {
            List<String> unscheduledWorkFlows = new LinkedList<String>(SettingsUtils.getAllCollectionsIDs());
            try {
                for (Schedule schedule:workflowConf.getSchedules().getSchedule()) {
                    List<String> collectionsToScheduleWorkflowFor;
                    if (schedule.isSetCollections()) {
                        collectionsToScheduleWorkflowFor = schedule.getCollections().getCollectionID();
                    } else {
                        collectionsToScheduleWorkflowFor = SettingsUtils.getAllCollectionsIDs();
                    }
                    for (String collectionID:collectionsToScheduleWorkflowFor) {
                        SchedulableJob workflow =
                                (SchedulableJob)Class.forName(workflowConf.getWorkflowClass()).newInstance();
                        workflow.initialise(context, collectionID);
                        scheduler.scheduleWorkflow(workflow, schedule.getWorkflowInterval());
                        unscheduledWorkFlows.remove(collectionID);
                    }
                }
                // Create a instance of all workflows not explicitly scheduled.
                for (String collection:unscheduledWorkFlows) {
                    SchedulableJob workflow =
                            (SchedulableJob)Class.forName(workflowConf.getWorkflowClass()).newInstance();
                    workflow.initialise(context, collection);
                    scheduler.scheduleWorkflow(workflow, null);
                }
            } catch (Exception e) {
                log.error("Unable to schedule workflow " + workflowConf.getWorkflowClass(), e);
            }
        }
    }
}
