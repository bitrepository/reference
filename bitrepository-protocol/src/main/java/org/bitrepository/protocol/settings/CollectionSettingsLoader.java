package org.bitrepository.protocol.settings;

import org.bitrepository.collection.settings.standardsettings.Settings;
import org.bitrepository.protocol.bitrepositorycollection.MutableCollectionSettings;

public class CollectionSettingsLoader {
    protected final SettingsReader settingsReader;

    public CollectionSettingsLoader(SettingsReader settingsReader) {
        this.settingsReader = settingsReader;
    }
    
    public MutableCollectionSettings loadSettings(String collectionID) throws Exception {
        MutableCollectionSettings settings = new MutableCollectionSettings();
        addCollectionSettings(collectionID, settings);
        return settings;
    }
    
    protected final void addCollectionSettings(String collectionID, MutableCollectionSettings settings) throws Exception {
        settings.setSettings(settingsReader.loadSettings(collectionID, Settings.class));
    }
}
