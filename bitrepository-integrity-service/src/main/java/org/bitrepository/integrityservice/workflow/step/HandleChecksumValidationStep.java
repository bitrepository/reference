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

import java.util.List;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.IntegrityReporter;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleChecksumValidationStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The Integrity Model. */
    private final IntegrityModel store;
    /** The report model to populate */
    private final IntegrityReporter reporter;
    
    public HandleChecksumValidationStep(IntegrityModel store, IntegrityReporter reporter) {
        this.store = store;
        this.reporter = reporter;
    }
    
    @Override
    public String getName() {
        return "Handle files missing at some pillars.";
    }

    /**
     * Queries the IntegrityModel for missing files on each pillar. Reports them if any is returned.
     */
    @Override
    public synchronized void performStep() {
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());
        for(String pillar : pillars) {
            List<String> missingFiles = 
                    store.getMissingFilesAtPillar(pillar, 0, Integer.MAX_VALUE, reporter.getCollectionID());
            for(String missingFile : missingFiles) {
                reporter.reportMissingFile(missingFile, pillar);
            }
        }
    }

    public static String getDescription() {
        return "Detects and reports files that are missing from one or more pillars in the collection.";
    }
}
