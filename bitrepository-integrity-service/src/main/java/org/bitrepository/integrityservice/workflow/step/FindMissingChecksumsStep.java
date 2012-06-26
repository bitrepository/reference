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
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * 
 * It goes through every file id available, extracts all the fileinfos and validates whether any pillars
 * are missing the given checksum (e.g. whether the ChecksumState is Unknown and the FileState is not Missing).
 * This is a very simple and definitely not optimized way of finding missing checksums.
 */
public class FindMissingChecksumsStep implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    /** The mapping between */
    private List<String> missingChecksums = new ArrayList<String>();
    /** The dispatcher of alarms.*/
    private final IntegrityAlerter dispatcher;

    /**
     * Constructor.
     * @param store The storage for the integrity data.
     */
    public FindMissingChecksumsStep(IntegrityModel store, IntegrityAlerter alarmDispatcher) {
        this.store = store;
        this.dispatcher = alarmDispatcher;
    }
    
    @Override
    public String getName() {
        return "Finding missing checksums";
    }

    /**
     * Goes through all the file ids in the database and extract their respective fileinfos.
     * Then it goes through all the file infos to validate that the file at no pillar exists but has an unknown state 
     * for the checksum.
     */
    @Override
    public synchronized void performStep() {
        List<String> missingChecksums = store.findMissingChecksums();
        
        if(missingChecksums.isEmpty()) {
            log.debug("No files are missing their checksum.");
        } else {
            // TODO
//            dispatcher.
        }
    }

    /**
     * Creates and returns a list of the file ids where the checksums should be recollected.
     * @return The list of file ids which is missing the checksum for some pillar.
     */
    public List<String> getResults() {
        return new ArrayList<String>(missingChecksums);
    }
}