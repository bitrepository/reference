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
package org.bitrepository.integrityservice.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.CollectionStat;
import org.bitrepository.integrityservice.cache.PillarCollectionStat;

public class StatisticsCollector {

    final CollectionStat collectionStat;
    final Map<String, PillarCollectionStat> pillarCollectionStats;
    
    public StatisticsCollector(String collectionID, Settings settings) {
        collectionStat = new CollectionStat(collectionID);
        pillarCollectionStats = new HashMap<>();
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID, settings);
        for(String pillar : pillars) {
            PillarCollectionStat ps = new PillarCollectionStat(pillar, collectionID);
            pillarCollectionStats.put(pillar, ps);
        }
    }
    
    public CollectionStat getCollectionStat() {
        return collectionStat;
    }
    
    public PillarCollectionStat getPillarCollectionStat(String pillarID) {
        return pillarCollectionStats.get(pillarID);
    }
    
}
