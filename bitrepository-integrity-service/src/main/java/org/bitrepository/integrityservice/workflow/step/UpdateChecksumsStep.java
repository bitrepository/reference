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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityCollectorEventHandler;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The step for collecting the checksums of all files from all pillars.
 */
public class UpdateChecksumsStep implements WorkflowStep{
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The collector for retrieving the checksums.*/
    private final IntegrityInformationCollector collector;
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    /** The pillar ids.*/
    private final List<String> pillarIds;
    /** The checksum spec type.*/
    private final ChecksumSpecTYPE checksumType;
    /** The integrity alerter.*/
    private final IntegrityAlerter alerter;
    
    /**
     * Constructor.
     * @param collector The client for collecting the checksums.
     * @param store The storage for the integrity data.
     * @param alerter The alerter for sending failures.
     * @param pillarIds The ids of the pillars to collect the checksum from.
     * @param checksumType The type of checksum to collect.
     */
    public UpdateChecksumsStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter,
            List<String> pillarIds, ChecksumSpecTYPE checksumType) {
        this.collector = collector;
        this.store = store;
        this.pillarIds = pillarIds;
        this.checksumType = checksumType;
        this.alerter = alerter;
    }
    
    @Override
    public String getName() {
        return "Collecting checksums for all files.";
    }

    @Override
    public synchronized void performStep() {
        log.debug("Begin collecting the checksums.");
        
        IntegrityCollectorEventHandler eventHandler = new IntegrityCollectorEventHandler(store, alerter, 60000);
        collector.getChecksums(pillarIds, FileIDsUtils.getAllFileIDs(), checksumType, "IntegrityService: " + getName(),
                eventHandler);
        try {
            eventHandler.getFinish();
        } catch (InterruptedException e) {
            // TODO      
        }
        
        log.debug("Finished collecting the checksums.");
    }
}
