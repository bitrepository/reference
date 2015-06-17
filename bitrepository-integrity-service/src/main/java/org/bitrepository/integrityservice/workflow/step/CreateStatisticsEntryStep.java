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
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class CreateStatisticsEntryStep extends AbstractWorkFlowStep {
    /** The Integrity Model. */
    private final IntegrityModel store;
    /** The collectionID */
    private final String collectionId;
    private final StatisticsCollector sc;
    
    public CreateStatisticsEntryStep(IntegrityModel store, String collectionId, StatisticsCollector statisticsCollector) {
        this.store = store;        
        this.collectionId = collectionId;
        this.sc = statisticsCollector;
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
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionId);
        for(String pillar : pillars) {
            sc.getPillarCollectionStat(pillar).setFileCount(store.getNumberOfFiles(pillar, collectionId));
            sc.getPillarCollectionStat(pillar).setDataSize(store.getCollectionFileSizeAtPillar(collectionId, pillar));
        }
        sc.getCollectionStat().setFileCount(store.getNumberOfFilesInCollection(collectionId));
        sc.getCollectionStat().setDataSize(store.getCollectionFileSize(collectionId));
        sc.getCollectionStat().setLatestFileTime(store.getDateForNewestFileEntryForCollection(collectionId));
        
        store.createStatistics(collectionId, sc);
    }

    public static String getDescription() {
        return "Creates a new statistics entry in the database.";
    }
}
