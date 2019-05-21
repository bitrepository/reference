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
package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;

/**
 * Workflow step class to handle the full collection of checksums.
 */
public class FullUpdateFileInfosStep extends UpdateFileInfosStep {

    public FullUpdateFileInfosStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter,
            ChecksumSpecTYPE checksumType, Settings settings, String collectionID, IntegrityContributors integrityContributors) {
        super(collector, store, alerter, checksumType, settings, collectionID, integrityContributors);
    }

    @Override
    public String getName() {
        return "Collect all fileinfos from pillars";
    }

    @Override
    protected void initialStepAction() {
        store.resetFileInfoCollectionProgress(collectionID);
    }

    /**
     * @return Description of this step.
     */
    public static String getDescription() {
        return "Contacts all pillars to retrieve the list of fileinfos for the pillar";
    }

}
