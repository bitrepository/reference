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
 * Contains the general data needed by an integrity workflow, avoiding methods with many arguments.
 */
public record IntegrityWorkflowContext(
    Settings settings,
    IntegrityInformationCollector collector,
    IntegrityModel store,
    IntegrityAlerter alerter,
    AuditTrailManager auditManager
) implements WorkflowContext {

    /**
     * @deprecated Use {@link #settings()} instead
     */
    @Deprecated(forRemoval = true)
    public Settings getSettings() {
        return settings;
    }

    /**
     * @deprecated Use {@link #collector()} instead
     */
    @Deprecated(forRemoval = true)
    public IntegrityInformationCollector getCollector() {
        return collector;
    }

    /**
     * @deprecated Use {@link #store()} instead
     */
    @Deprecated(forRemoval = true)
    public IntegrityModel getStore() {
        return store;
    }

    /**
     * @deprecated Use {@link #alerter()} instead
     */
    @Deprecated(forRemoval = true)
    public IntegrityAlerter getAlerter() {
        return alerter;
    }

    /**
     * @deprecated Use {@link #auditManager()} instead
     */
    @Deprecated(forRemoval = true)
    public AuditTrailManager getAuditManager() {
        return auditManager;
    }
}
