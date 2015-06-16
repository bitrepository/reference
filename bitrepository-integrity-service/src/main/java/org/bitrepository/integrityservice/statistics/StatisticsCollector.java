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
    
    public StatisticsCollector(Settings settings, String collectionID) {
        collectionStat = new CollectionStat(collectionID);
        pillarCollectionStats = new HashMap<>();
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
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
