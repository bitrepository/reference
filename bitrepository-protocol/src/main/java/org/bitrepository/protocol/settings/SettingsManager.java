package org.bitrepository.protocol.settings;

import org.bitrepository.protocol.bitrepositorycollection.CollectionSettings;

/**
 * Used for reading settings and storing settings.
 */
public interface SettingsManager extends SettingsReader {

    /**
     * Saves the provided settings.
     * @param settings The settings to save
     */
    public CollectionSettings saveCollectionSettings(CollectionSettings settings);
}
