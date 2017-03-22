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

import java.math.BigInteger;
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
    /** Default maximum pagesize / number of results used in clients. Can be overridden by configuration */
    public final static Integer DEFAULT_MAX_CLIENT_PAGE_SIZE = 10000;

    /**
     *
     * @param injectedSettings The settings to use.
     */
    public static void initialize(Settings injectedSettings) {
    }

    /**
     * Finds the collections, which the given pillar is part of.
     * @param pillarID The id of the pillar.
     * @param settings the settings
     * @return The list of collection ids, which this pillar is part of.
     */
    public static List<String> getCollectionIDsForPillar(String pillarID, Settings settings) {
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
     * @param settings the settings
     */
    public static List<String> getAllCollectionsIDs(Settings settings) {
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
     * @param settings the settings
     * @return The name of the collection
     */
    public static String getCollectionName(String collectionID, Settings settings) {
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
     * @param settings the settings
     */
    public static String getRepositoryName(Settings settings) {
        return settings.getRepositorySettings().getName();
    }
    
    /**
     * Retrieves all the different pillar ids defined across all collections (without duplicates).
     * @return The list of pillar ids.
     * @param settings the settings
     */
    public static List<String> getAllPillarIDs(Settings settings) {
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
    
    public static List<String> getPillarIDsForCollection(String collectionID, Settings settings) {
        List<String> res = new ArrayList<String>();
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            if(c.getID().equals(collectionID)) {
                res.addAll(c.getPillarIDs().getPillarID());
            }
        }
        return res;
    }
    
    /**
     * Get the maximum page size for clients. 
     * If none have been set in the settings, use the default of 10000.
     * @return {@link Integer} the maximum number of results per page.
     * @param settings the settings
     */
    public static Integer getMaxClientPageSize(Settings settings) {
        BigInteger maxClientSizeSettings = settings.getReferenceSettings().getClientSettings().getMaxPageSize();
        return maxClientSizeSettings != null ? maxClientSizeSettings.intValue() : DEFAULT_MAX_CLIENT_PAGE_SIZE;
    }
    
    /**
     * Retrieves the contributors for audittrail for a specific collection.
     *
     * @param settings the settings
     * @param collectionID The id of the collection.
     * @return The list of ids for the contributors of audittrails for the collection
     */
    public static Set<String> getAuditContributorsForCollection(Settings settings, String collectionID) {
        Set<String> contributors = new HashSet<String>();
        contributors.addAll(settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs());
        contributors.addAll(SettingsUtils.getPillarIDsForCollection(collectionID, settings));
        return contributors;
    }

    /**
     * Retrieves the contributors for status.
     * @return The list of ids for the status contributors.
     * @param settings the settings
     */
    public static Set<String> getStatusContributorsForCollection(Settings settings) {
        Set<String> contributors = new HashSet<String>();
        contributors.addAll(settings.getRepositorySettings().getGetStatusSettings().getNonPillarContributorIDs());
        contributors.addAll(SettingsUtils.getAllPillarIDs(settings));
        return contributors;
    }
}
