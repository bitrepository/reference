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
import java.util.Map;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.PillarCollectionMetric;
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
    private final String collectionID;
    private final StatisticsCollector sc;
    private final Settings settings;

    public CreateStatisticsEntryStep(IntegrityModel store, String collectionID, StatisticsCollector statisticsCollector, Settings settings) {
        this.store = store;        
        this.collectionID = collectionID;
        this.sc = statisticsCollector;
        this.settings = settings;
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
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID, settings);
        Map<String, PillarCollectionMetric> pillarMetrics = store.getPillarCollectionMetrics(collectionID);
        for(String pillar : pillars) {
            PillarCollectionMetric metric = pillarMetrics.get(pillar);
            if(metric == null) {
                sc.getPillarCollectionStat(pillar).setFileCount(0L);
                sc.getPillarCollectionStat(pillar).setDataSize(0L);
            } else {
                sc.getPillarCollectionStat(pillar).setFileCount(metric.getPillarFileCount());
                sc.getPillarCollectionStat(pillar).setDataSize(metric.getPillarCollectionSize());    
            }
        }
        sc.getCollectionStat().setFileCount(store.getNumberOfFilesInCollection(collectionID));
        sc.getCollectionStat().setDataSize(store.getCollectionFileSize(collectionID));
        sc.getCollectionStat().setLatestFileTime(store.getDateForNewestFileEntryForCollection(collectionID));
        
        store.createStatistics(collectionID, sc);
    }

    public static String getDescription() {
        return "Creates a new statistics entry in the database.";
    }
}
