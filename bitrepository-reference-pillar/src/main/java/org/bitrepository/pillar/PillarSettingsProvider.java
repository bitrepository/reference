package org.bitrepository.pillar;

import org.bitrepository.common.settings.SettingsLoader;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.settings.referencesettings.ReferenceSettings;

public class PillarSettingsProvider extends SettingsProvider {
    private String pillarID;

    /**
     * Creates a <code>SettingsProvider</code> which will use the provided <code>SettingsLoader</code> for loading the
     * settings.
     *
     * @param settingsReader Use for loading the settings.
     * @param pillarID Optional pillarID if more than one reference pillar in the collection. If null the
     *                  pillarID from the first pillar settings section in the reference settings wil be used.
     */
    public PillarSettingsProvider(SettingsLoader settingsReader, String pillarID) {
        super(settingsReader, null);
        this.pillarID = pillarID;
    }

    protected String getComponentID(ReferenceSettings referenceSettings) {
        if(pillarID == null) {
            return referenceSettings.getPillarSettings().getPillarID();
        } else {
            return pillarID;
        }
    }
}
