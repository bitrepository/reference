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
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.workflow.WorkflowContext;

/**
 * Contains the general data needed by a integrity workflow. The class wraps a number of objects normally
 * needed by integrity workflows. This avoids complicated methods with lots a arguments.
 */
public class IntegrityWorkflowContext implements WorkflowContext {
    private final Settings settings;
    private final IntegrityInformationCollector collector;
    private final IntegrityModel store;
    private final IntegrityAlerter alerter;
    private final AuditTrailManager auditManager;

    /**
     * @param settings The <code>Settings</code> to use in the workflow.
     * @param collector The <code>IntegrityInformationCollector</code> to use in the workflow.
     * @param store The <code>IntegrityModel</code> to use in the workflow.
     * @param alerter The <code>IntegrityAlerter</code> to use in the workflow.
     * @param auditManager The <code>AuditTrailManager</code> to use in the workflow.
     */
    public IntegrityWorkflowContext(Settings settings,
            IntegrityInformationCollector collector,
            IntegrityModel store,
            IntegrityAlerter alerter,
            AuditTrailManager auditManager) {
        this.settings = settings;
        this.collector = collector;
        this.store = store;
        this.alerter = alerter;
        this.auditManager = auditManager;

    }

    public Settings getSettings() {
        return settings;
    }

    public IntegrityInformationCollector getCollector() {
        return collector;
    }

    public IntegrityModel getStore() {
        return store;
    }

    public IntegrityAlerter getAlerter() {
        return alerter;
    }

    public AuditTrailManager getAuditManager() {
        return auditManager;
    }

    @Override
    public String toString() {
        return "IntegrityWorkflowContext{" +
                "settings=" + settings +
                ", collector=" + collector +
                ", store=" + store +
                ", alerter=" + alerter +
                ", auditManager=" + auditManager + '\'' +
                '}';
    }
}
