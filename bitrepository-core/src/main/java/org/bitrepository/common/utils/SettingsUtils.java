package org.bitrepository.common.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * Finds the complete list of collections in the repository.
     * @param settings The settings for the repository. 
     */
    public static List<String> getAllCollectionsIDs(Settings settings) {
        List<String> res = new ArrayList<String>();
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            res.add(c.getID());
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
    
    public static List<String> getPillarIDsForCollection(Settings settings, String collectionID) {
        List<String> res = new ArrayList<String>();
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            if(c.getID().equals(collectionID)) {
                res.addAll(c.getPillarIDs().getPillarID());
            }
        }
        return res;
    }
    
    /**
     * Retrieves the contributors for audittrail for a specific collection.
     * @param settings The settings.
     * @param collectionID The id of the collection.
     * @return The list of ids for the contributors of audittrails for the collection
     */
    public static Set<String> getAuditContributorsForCollection(Settings settings, String collectionID) {
        Set<String> contributors = new HashSet<String>();
        contributors.addAll(
                settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs());
        contributors.addAll(SettingsUtils.getPillarIDsForCollection(settings, collectionID));
        return contributors;
    }
}
