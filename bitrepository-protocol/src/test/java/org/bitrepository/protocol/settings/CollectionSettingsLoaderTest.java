package org.bitrepository.protocol.settings;

import org.bitrepository.protocol.bitrepositorycollection.MutableCollectionSettings;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class CollectionSettingsLoaderTest extends ExtendedTestCase{
    private static final String PATH_TO_SETTINGS = "settings/xml";

    @Test
    public void testLocalCollectionSettingsLoading() throws Exception {
        CollectionSettingsLoader settingsLoader = 
            new CollectionSettingsLoader(new XMLFileSettingsLoader(PATH_TO_SETTINGS));
        
        MutableCollectionSettings collectionSettings = new MutableCollectionSettings();
        
        settingsLoader.addCollectionSettings("bitrepository-local", collectionSettings);
    }
    
    @Test
    public void testDevelCollectionSettingsLoading() throws Exception {
        CollectionSettingsLoader settingsLoader = 
            new CollectionSettingsLoader(new XMLFileSettingsLoader(PATH_TO_SETTINGS));
        
        MutableCollectionSettings collectionSettings = new MutableCollectionSettings();
        
        settingsLoader.addCollectionSettings("bitrepository-devel", collectionSettings);
    }
    
    @Test
    public void testIntegrationCollectionSettingsLoading() throws Exception {
        CollectionSettingsLoader settingsLoader = 
            new CollectionSettingsLoader(new XMLFileSettingsLoader(PATH_TO_SETTINGS));
        
        MutableCollectionSettings collectionSettings = new MutableCollectionSettings();
        
        settingsLoader.addCollectionSettings("bitrepository-integration", collectionSettings);
    }
}
