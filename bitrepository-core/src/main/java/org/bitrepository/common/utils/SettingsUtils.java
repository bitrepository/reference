package org.bitrepository.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.repositorysettings.Collection;

/**
 * Utility method for handling the settings.
 */
public class SettingsUtils {
    /**
     * Finds the collections, which the given pillar is part of. 
     * @param settings The settings with the collections.
     * @param pillarID The id of the pillar.
     * @return The list of collection ids, which this pillar is part of.
     */
    public static List<String> getCollectionIDsForPillar(Settings settings, String pillarID) {
        List<String> res = new ArrayList<String>();
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            if(c.getPillarIDs().getPillarID().contains(pillarID)) {
                res.add(c.getID());
            }
        }
        
        return res;
    }
    
    /**
     * Retrieves all the different pillar ids defined across all collections (without duplicates).
     * @param settings The settings.
     * @return The list of pillar ids. 
     */
    public static List<String> getAllPillarIDs(Settings settings) {
        List<String> res = new ArrayList<String>();
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            for(String pillarId : c.getPillarIDs().getPillarID()) {
                if(!res.contains(pillarId)) {
                    res.add(pillarId);
                }
            }
        }
        return res;
    }
}
