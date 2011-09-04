package org.bitrepository.alarm;

import org.bitrepository.protocol.settings.CollectionSettingsLoader;
import org.bitrepository.protocol.settings.SettingsReader;

public class AlarmSettingsLoader extends CollectionSettingsLoader {

    public AlarmSettingsLoader(SettingsReader settingsReader) {
        super(settingsReader);
    }
    
    public MutableAlarmSettings loadSettings(String collectionID) throws Exception {
        MutableAlarmSettings settings = new MutableAlarmSettings();

        addCollectionSettings(collectionID, settings);
        
        return settings;
    }
}
