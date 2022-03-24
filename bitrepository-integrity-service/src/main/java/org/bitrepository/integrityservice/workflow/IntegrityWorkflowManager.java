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

package org.bitrepository.integrityservice.workflow;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.scheduler.JobScheduler;
import org.bitrepository.service.workflow.WorkflowManager;
import org.bitrepository.settings.referencesettings.Schedule;
import org.bitrepository.settings.referencesettings.Schedules;
import org.bitrepository.settings.referencesettings.WorkflowConfiguration;
import org.bitrepository.settings.referencesettings.WorkflowSettings;

/**
 * Manages the workflows for the integrity service. Delegates most functionality to the <code>Workflow</code>
 * class, but handles som configuration specific to the integrity workflows.
 */
public class IntegrityWorkflowManager extends WorkflowManager {
    public static final long DAILY = 86400000;
    public static final long HOURLY = 360000;

    public IntegrityWorkflowManager(IntegrityWorkflowContext context, JobScheduler scheduler) {
        super(context, getWorkflowSettings(context.getSettings()), scheduler);
    }

    private static WorkflowSettings getWorkflowSettings(Settings settings) {
        WorkflowSettings workflowSettings;
        if (settings.getReferenceSettings().getIntegrityServiceSettings().isSetWorkflows()) {
            workflowSettings = settings.getReferenceSettings().getIntegrityServiceSettings().getWorkflows();
        } else {
            workflowSettings = createDefaultWorkflowSettings();
        }
        return workflowSettings;
    }

    /**
     * Will create a default set of workflows. This is currently just the {@link CompleteIntegrityCheck}
     * workflow running once a day on all collections.
     *
     * @return a default set of workflows
     */
    protected static WorkflowSettings createDefaultWorkflowSettings() {
        WorkflowSettings defaultWorkflowSettings = new WorkflowSettings();
        WorkflowConfiguration completeIntegrityWorkflowConf = new WorkflowConfiguration();
        completeIntegrityWorkflowConf.setWorkflowClass(CompleteIntegrityCheck.class.getCanonicalName());
        Schedule schedule = new Schedule();
        schedule.setWorkflowInterval(DAILY);
        completeIntegrityWorkflowConf.setSchedules(new Schedules());
        completeIntegrityWorkflowConf.getSchedules().getSchedule().add(schedule);
        defaultWorkflowSettings.getWorkflow().add(completeIntegrityWorkflowConf);
        return defaultWorkflowSettings;
    }

    @Override
    protected String getDefaultWorkflowPackage() {
        return "org.bitrepository.integrityservice.workflow";
    }
}
