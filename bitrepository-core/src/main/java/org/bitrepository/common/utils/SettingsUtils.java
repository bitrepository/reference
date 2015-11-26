/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
    private static Settings settings;

    /**
     *
     * @param injectedSettings The settings to use.
     */
    public static void initialize(Settings injectedSettings) {
        settings = injectedSettings;
    }

    /**
     * Finds the collections, which the given pillar is part of.
     * @param pillarID The id of the pillar.
     * @return The list of collection ids, which this pillar is part of.
     */
    public static List<String> getCollectionIDsForPillar(String pillarID) {
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
     * @return all collections in the repository
     */
    public static List<String> getAllCollectionsIDs() {
        List<String> res = new ArrayList<String>();
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            res.add(c.getID());
        }       
        return res;
    }
    
    /**
     * Get's the name of a given collection. If no name is given in the settings, 
     * the ID of the collection is returned
     * @param collectionID the collection id
     * @return The name of the collection 
     */
    public static String getCollectionName(String collectionID) {
        String name = null;
        
        for(Collection collection : settings.getRepositorySettings().getCollections().getCollection()) {
            if(collection.getID().equals(collectionID)) {
                if(collection.isSetName()) {
                    name = collection.getName();    
                } else {
                    name = collection.getID();
                }
                break;
            }
        }
        
        return name;
    }
    
    /**
     * Get the name of the repository
     * @return The name of the repository 
     */
    public static String getRepositoryName() {
        return settings.getRepositorySettings().getName();
    }
    
    /**
     * Retrieves all the different pillar ids defined across all collections (without duplicates).
     * @return The list of pillar ids. 
     */
    public static List<String> getAllPillarIDs() {
        List<String> res = new ArrayList<String>();
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            for(String pillarID : c.getPillarIDs().getPillarID()) {
                if(!res.contains(pillarID)) {
                    res.add(pillarID);
                }
            }
        }
        return res;
    }
    
    public static List<String> getPillarIDsForCollection(String collectionID) {
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
     * @param collectionID The id of the collection.
     * @return The list of ids for the contributors of audittrails for the collection
     */
    public static Set<String> getAuditContributorsForCollection(String collectionID) {
        Set<String> contributors = new HashSet<String>();
        contributors.addAll(
                settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs());
        contributors.addAll(SettingsUtils.getPillarIDsForCollection(collectionID));
        return contributors;
    }

    /**
     * Retrieves the contributors for status.
     * @return The list of ids for the status contributors.
     */
    public static Set<String> getStatusContributorsForCollection() {
        Set<String> contributors = new HashSet<String>();
        contributors.addAll(
                settings.getRepositorySettings().getGetStatusSettings().getNonPillarContributorIDs());
        contributors.addAll(SettingsUtils.getAllPillarIDs());
        return contributors;
    }
}
