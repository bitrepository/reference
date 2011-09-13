package org.bitrepository.protocol.bitrepositorycollection;

import org.bitrepository.collection.settings.specificsettings.SpecificCollectionSettings;
import org.bitrepository.collection.settings.standardsettings.Settings;

/**
 * Modifiable implementation of the <code>CollectionSettings</code> interface.
 */
public class MutableCollectionSettings implements CollectionSettings {

    /** @see #getStandardSettings()  */
    private Settings settings;
    @Override
    public Settings getSettings() {
        return settings;
    }
    /** @see #getStandardSettings()  */
    public void setSettings(Settings settings) {
        this.settings = settings;
    }
    
//    /** @see #getStandardSettings()  */
//    private SpecificCollectionSettings specificSettings;
//    @Override
//    public SpecificCollectionSettings getSpecificSettings() {
//        return specificSettings;
//    }
//    /** @see #getSpecificSettings()  */
//    public void setSpecificSettings(SpecificCollectionSettings specificSettings) {
//        this.specificSettings = specificSettings;
//    }
}
