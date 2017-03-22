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
package org.bitrepository.integrityservice.tools;

import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.database.IntegrityDBTools;
import org.bitrepository.service.ServiceSettingsProvider;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.settings.referencesettings.ServiceType;

/**
 * Class to handle removal and additions of colletions in the integrityDB 
 */
public class CollectionsAdmin {

    private final String collectionID;
    private final Settings settings;
    IntegrityDBTools tools;
    
    public CollectionsAdmin(String collectionID, String pathToSettings) {
        this.collectionID = collectionID;
        
        ServiceSettingsProvider settingsLoader =
                new ServiceSettingsProvider(new XMLFileSettingsLoader(pathToSettings), ServiceType.INTEGRITY_SERVICE);
        settings = settingsLoader.getSettings();
        SettingsUtils.initialize(settings);
        tools = new IntegrityDBTools(new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase()));
    }
    
    /**
     * Method to run the requested action on the database. Checks to see if the requested actions is supported.
     * And if we can operate on the requested collectionID
     * @param method The method to invoke, allowed values 'add' or 'remove'
     * @throws InvalidMethodException if the method/action is not supported
     * @throws UnknownCollectionException if the requested collectionID is not found in settings. 
     */
    public void invoke(String method) throws InvalidMethodException, UnknownCollectionException {
        hasCollection();
        if(method.equals("add")) {
            tools.addCollection(collectionID);
        } else if(method.equals("remove")) {
            tools.removeCollection(collectionID);
        } else {
            throw new InvalidMethodException("The method '" + method + "' is not supported, use: 'remove' or 'add'.");
        }
    }
    
    /**
     * Method to determine if a collection is present in the RepositorySettings. 
     * @throws UnknownCollectionException if the collection is not present 
     */
    private void hasCollection() throws UnknownCollectionException {
        List<String> collections = SettingsUtils.getAllCollectionsIDs(settings);
        if(!collections.contains(collectionID)) {
            throw new UnknownCollectionException("The collection '" + collectionID + "' is not present in RepositorySettings");
        }
    }
}
