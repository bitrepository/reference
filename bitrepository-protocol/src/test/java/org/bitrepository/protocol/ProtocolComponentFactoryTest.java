package org.bitrepository.protocol;

import org.bitrepository.protocol.bitrepositorycollection.MutableCollectionSettings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.settings.CollectionSettingsLoader;
import org.bitrepository.protocol.settings.XMLFileSettingsLoader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProtocolComponentFactoryTest {
    private MutableCollectionSettings settings;
    
    @BeforeMethod(alwaysRun = true)
    public void beforeMethodSetup() throws Exception {
        setupSettings();
    }
    
    protected void setupSettings() throws Exception {
        CollectionSettingsLoader settingsLoader = new CollectionSettingsLoader(new XMLFileSettingsLoader("src/test/resources/settings/xml"));
        settings = settingsLoader.loadSettings("bitrepository-devel");
    }
    
    @Test(groups = { "regressiontest" })    
    /**
     * Validates that only one message bus instance is created for each collection ID.
     */
    public void getMessageTest() throws Exception {
        MessageBus bus =
            ProtocolComponentFactory.getInstance().getMessageBus(settings.getSettings());
    }
}
