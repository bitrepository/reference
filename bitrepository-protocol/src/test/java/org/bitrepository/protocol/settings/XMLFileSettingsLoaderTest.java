package org.bitrepository.protocol.settings;

import java.io.File;

import org.bitrepository.collection.settings.standardsettings.Settings;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class XMLFileSettingsLoaderTest extends ExtendedTestCase{
    private static final String COLLECTION_ID = "bitrepository-devel";
//    private static final String PATH_TO_SETTINGS = "settings/xml";
    private static final String PATH_TO_SETTINGS = "src/test/resources/settings/xml";
    
    @Test
    public void testCollectionSettingsLoading() throws Exception {
        System.out.println((new File(".")).getAbsolutePath());
        SettingsReader settingsLoader = new XMLFileSettingsLoader(PATH_TO_SETTINGS);
        
        Settings collectionSettings = settingsLoader.loadSettings(
                COLLECTION_ID, Settings.class);
    }
}
