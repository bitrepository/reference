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

import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class CreateStatisticsEntryStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The Integrity Model. */
    private final IntegrityModel store;
    /** The collectionID */
    private final String collectionId;
    
    public CreateStatisticsEntryStep(IntegrityModel store, String collectionId) {
        this.store = store;        
        this.collectionId = collectionId;
    }
    
    @Override
    public String getName() {
        return "Create statistics";
    }

    /**
     * Uses IntegrityChecker to validate whether any checksums are missing.
     * Dispatches an alarm if any checksums were missing.
     */
    @Override
    public synchronized void performStep() {
        store.makeStatisticsForCollection(collectionId);
    }

    public static String getDescription() {
        return "Creates a new statistics entry in the database.";
    }
}
