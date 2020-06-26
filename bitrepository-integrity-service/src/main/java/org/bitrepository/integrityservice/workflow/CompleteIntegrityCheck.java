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

import java.util.Date;

import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.integrityservice.workflow.step.FullUpdateChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.FullUpdateFileIDsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateFileIDsStep;

/**
 * Simple workflow for performing integrity checks of the system. 
 * Starts by updating the file ids in the integrity model, followed by updating the checksums in the integrity model.
 * Then the data is validated for integrity issues.
 * And finally it is verified whether any missing or obsolete checksums can be found.
 */
public class CompleteIntegrityCheck extends IntegrityCheckWorkflow {
    /**
     * Remember to call the initialise method needs to be called before the start method.
     */
    public CompleteIntegrityCheck() {}


    @Override
    public String getDescription() {
        return "Retrieves all fileIDs and checksums from all pillars and checks for all potential integrity " +
                "problems.";
    }

    @Override
    protected UpdateFileIDsStep getUpdateFileIDsStep() {
        return new FullUpdateFileIDsStep(context.getCollector(), context.getStore(),
                context.getAlerter(), context.getSettings(), collectionID, integrityContributors);
    }


    @Override
    protected UpdateChecksumsStep getUpdateChecksumsStep() {
        return new FullUpdateChecksumsStep(
                context.getCollector(), context.getStore(), context.getAlerter(),
                ChecksumUtils.getDefault(context.getSettings()), context.getSettings(), collectionID, integrityContributors);
    }


    @Override
    protected boolean cleanDeletedFiles() {
        return true;
    }


    @Override
    protected Date getChecksumUpdateCutoffDate() {
        return workflowStart;
    }
}
