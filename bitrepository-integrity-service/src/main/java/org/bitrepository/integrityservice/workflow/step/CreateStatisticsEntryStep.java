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

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.PillarCollectionMetric;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

import java.util.List;
import java.util.Map;

/**
 * A workflow step for creating pillar statistics.
 */
public class CreateStatisticsEntryStep extends AbstractWorkFlowStep {
    private final IntegrityModel store;
    private final String collectionID;
    private final StatisticsCollector sc;

    public CreateStatisticsEntryStep(IntegrityModel store, String collectionID, StatisticsCollector statisticsCollector) {
        this.store = store;
        this.collectionID = collectionID;
        this.sc = statisticsCollector;
    }

    @Override
    public String getName() {
        return "Create statistics";
    }

    /**
     * Collects pillar metrics for the given collection and creates from them a statistics entry in the store.
     */
    @Override
    public synchronized void performStep() {
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        Map<String, PillarCollectionMetric> pillarMetrics = store.getPillarCollectionMetrics(collectionID);
        for (String pillar : pillars) {
            PillarCollectionMetric metric = pillarMetrics.get(pillar);
            if (metric == null) {
                sc.getPillarCollectionStat(pillar).setFileCount(0L);
                sc.getPillarCollectionStat(pillar).setDataSize(0L);
                sc.getPillarCollectionStat(pillar).setOldestChecksumTimestamp(null);
            } else {
                sc.getPillarCollectionStat(pillar).setFileCount(metric.getPillarFileCount());
                sc.getPillarCollectionStat(pillar).setDataSize(metric.getPillarCollectionSize());
                sc.getPillarCollectionStat(pillar).setOldestChecksumTimestamp(metric.getOldestChecksumTimestamp());
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
