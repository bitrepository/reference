package org.bitrepository.pillar;

import org.bitrepository.collection.settings.standardsettings.StandardCollectionSettings;
import org.bitrepository.protocol.settings.SettingsReader;

public class PillarSettingsLoader {
    private final SettingsReader settingsReader;

    public PillarSettingsLoader(SettingsReader settingsReader) {
        this.settingsReader = settingsReader;
    }
    
    public MutablePillarSettings loadPillarSettings(String collectionID) throws Exception {
        MutablePillarSettings settings = new MutablePillarSettings();
        
        settings.setStandardSettings(settingsReader.loadSettings(collectionID, StandardCollectionSettings.class));
        settings.setStandardSettings(settingsReader.loadSettings(collectionID, StandardCollectionSettings.class));
        
        return settings;
    }
}
