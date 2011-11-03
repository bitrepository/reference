package org.bitrepository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.bitrepository.common.JaxbHelper;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceEnabledXmlFileSettingsReader extends XMLFileSettingsLoader {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public WebServiceEnabledXmlFileSettingsReader(String pathToSettingsFiles) {
        super(pathToSettingsFiles);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Loads the indicated settings for the specified collection.
     * @param <T> The type of settings to return
     * @param collectionID The collectionID to load the settings for.
     * @param settingsClass The settings class identifying the type of settings requested.
     * @return The loaded settings.
     */
    public <T> T loadSettings(String collectionID, Class<T> settingsClass) {
        String fileLocation = collectionID + "/" + settingsClass.getSimpleName() + ".xml";
        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileLocation);
        if (configStream == null) {
            try {
				configStream = new FileInputStream(fileLocation);
			} catch (FileNotFoundException e) {
				 throw new RuntimeException("Unable to load settings from " + fileLocation, e);
			}
        }

        log.debug("Loading the settings file '" + fileLocation + "'.");
        try {
			return (T) JaxbHelper.loadXml(settingsClass, configStream);
		} catch (JAXBException e) {
            throw new RuntimeException("Unable to load settings from " + fileLocation, e);
		}
    }

}
