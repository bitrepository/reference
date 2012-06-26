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

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * 
 * It goes through every file id available, extracts all the fileinfos and validates the timestamp for the given 
 * checksums. If it is too old, then it will mark the given file id for retrieval of checksums.
 * 
 * TODO make a method in the database for extracting all the ids of all the files which have the checksum state 
 * Unknown for any pillar. 
 */
public class FindObsoleteChecksumsStep implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    /** The interval for a checksum timestamp to timeout and become obsolete.*/
    private final Long obsoleteTimeout;
    /** The mapping between */
    private List<String> obsoleteChecksums = new ArrayList<String>();
    /** The dispatcher of alarms.*/
    private final IntegrityAlerter dispatcher;

    /**
     * Constructor.
     * @param store The storage for the integrity data.
     * @param obsoleteTimeout The interval for a checksum timestamp to timeout and become obsolete.
     */
    public FindObsoleteChecksumsStep(IntegrityModel store, IntegrityAlerter alarmDispatcher, long obsoleteTimeout) {
        this.store = store;
        this.obsoleteTimeout = obsoleteTimeout;
        this.dispatcher = alarmDispatcher;
    }
    
    @Override
    public String getName() {
        return "Finding obsolete checksums";
    }

    /**
     * Goes through all the file ids in the database and extract their respective fileinfos.
     * Then it goes through all the file infos to validate that the timestamp for the checksum calculation.
     * TODO needs optimization for the extraction of file ids.
     */
    @Override
    public synchronized void performStep() {
        List<String> missingFiles = store.findMissingFiles();
        
        if(missingFiles.isEmpty()) {
            log.debug("No files are missing at any pillar.");
        } else {
//            dispatcher.
        }
    }

    /**
     * Creates and returns a list of the file ids where the checksums should be recollected.
     * @return The list of file ids which have obsolete checksums for some pillar(s).
     */
    public List<String> getResults() {
        return new ArrayList<String>(obsoleteChecksums);
    }
}
