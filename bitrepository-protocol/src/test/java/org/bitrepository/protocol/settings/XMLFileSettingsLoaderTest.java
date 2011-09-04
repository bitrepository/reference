package org.bitrepository.protocol.settings;

import org.bitrepository.collection.settings.standardsettings.StandardCollectionSettings;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class XMLFileSettingsLoaderTest extends ExtendedTestCase{
    private static final String COLLECTION_ID = "bitrepository-devel";
    private static final String PATH_TO_SETTINGS = "settings/xml";
    
    @Test
    public void testCollectionSettingsLoading() throws Exception {
        SettingsReader settingsLoader = new XMLFileSettingsLoader(PATH_TO_SETTINGS);
        
        StandardCollectionSettings collectionSettings = settingsLoader.loadSettings(
                COLLECTION_ID, StandardCollectionSettings.class);
    }
}
