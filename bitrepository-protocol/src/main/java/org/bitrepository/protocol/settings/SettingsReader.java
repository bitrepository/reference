package org.bitrepository.protocol.settings;


/**
 * Used for reading collection settings, including specialized version of these (Client, pillar, getFile etc.).
 */
public interface SettingsReader {
    /**
     * Loads the settings for the indicated collection
     * @param collectionID The collection to load the settings for.
     * @param configurationClass Specifies the type of settings to load.
     * @return The loaded settings.
     * @throws Exception Failed to load the settings.
     */
    public <T> T loadSettings(String collectionID, Class<T> configurationClass) throws Exception ;
}
