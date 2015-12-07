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

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;

public class IncrementalUpdateFileIDsStep extends UpdateFileIDsStep {

    public IncrementalUpdateFileIDsStep(IntegrityInformationCollector collector, IntegrityModel store, 
            IntegrityAlerter alerter, Settings settings, String collectionID, IntegrityContributors integrityContributors) {
        super(collector, store, alerter, settings, collectionID, integrityContributors);
    }

    @Override
    public String getName() {
        return "Collect new fileIDs from pillars";
    }

    /**
     * @return Description of this step.
     */
    public static String getDescription() {
        return "Contacts all pillar to retrieve the list of new files from the pillars";
    }
    
}
