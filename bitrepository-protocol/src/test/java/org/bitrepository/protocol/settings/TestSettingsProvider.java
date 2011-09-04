package org.bitrepository.protocol.settings;

public class TestSettingsProvider implements SettingsReader {
    public final static String DEFAULT_SETTINGS = "Default-settings";

    /** Loads the default test settings 
     * @param <T>*/
    public <T> T loadDefaultSettings(Class<T> configurationClass) {
        return loadSettings(DEFAULT_SETTINGS, configurationClass);
    }

    @Override
    public <T> T loadSettings(String collectionID, Class<T> configurationClass) {
        // TODO Auto-generated method stub
        return null;
    }
}
