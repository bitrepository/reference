package org.bitrepository.protocol.bitrepositorycollection;

import org.bitrepository.collection.settings.standardsettings.StandardCollectionSettings;
import org.bitrepository.protocol.settings.specificsettings.SpecificCollectionSettings;

/**
 * Modifiable implementation of the <code>CollectionSettings</code> interface.
 */
public class MutableCollectionSettings implements CollectionSettings {

    /** @see #getStandardSettings()  */
    private StandardCollectionSettings standardSettings;
    @Override
    public StandardCollectionSettings getStandardSettings() {
        return standardSettings;
    }
    /** @see #getStandardSettings()  */
    public void setStandardSettings(StandardCollectionSettings standardSettings) {
        this.standardSettings = standardSettings;
    }
    
    /** @see #getStandardSettings()  */
    private SpecificCollectionSettings specificSettings;
    @Override
    public SpecificCollectionSettings getSpecificSettings() {
        return specificSettings;
    }
    /** @see #getSpecificSettings()  */
    public void setSpecificSettings(SpecificCollectionSettings specificSettings) {
        this.specificSettings = specificSettings;
    }
}
