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
        List<String> collections = SettingsUtils.getAllCollectionsIDs();
        if(!collections.contains(collectionID)) {
            throw new UnknownCollectionException("The collection '" + collectionID + "' is not present in RepositorySettings");
        }
    }
}
