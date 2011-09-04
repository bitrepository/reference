package org.bitrepository.protocol.settings;

import java.io.InputStream;

import org.bitrepository.common.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads settings as xml from the classpath.
 *
 */
public class XMLFileSettingsLoader implements SettingsReader {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String pathToSettingsFiles;

    /**
     * Creates a new loader
     * @param pathToSettingsFiles The location of the files to load. The settings xml files are assume to be placed as
     * ${COLLECTION_ID}/${CONFIGURATION_CLASS}.xml under this directory.
     * 
     * Note the the file is loaded from the class path so the path should only be different from the default "" if a 
     * need to 
     */
    public XMLFileSettingsLoader(String pathToSettingsFiles) {
        this.pathToSettingsFiles = pathToSettingsFiles;
    }

    /**
     * Loads the indicated settings for the specified collection.
     * @param <T> The type of settings to return
     * @param collectionID The collectionID to load the settings for.
     * @param settingsClass The settings class identifying the type of settings requested.
     * @return The loaded settings.
     */
    public <T> T loadSettings(String collectionID, Class<T> settingsClass) throws Exception {
        String fileLocation = pathToSettingsFiles + "/" + collectionID + "/" + settingsClass.getSimpleName() + ".xml";
        InputStream configStream = ClassLoader.getSystemResourceAsStream(fileLocation);
        if (configStream == null) {
            throw new IllegalArgumentException("Unable to find settings from " + fileLocation + " in classpath");
        }
        log.debug("Loading the settings file '" + fileLocation + "'.");
        return (T) JaxbHelper.loadXml(settingsClass, configStream);
    }
}
