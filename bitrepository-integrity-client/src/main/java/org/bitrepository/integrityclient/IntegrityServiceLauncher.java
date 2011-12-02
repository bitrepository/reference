package org.bitrepository.integrityclient;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;

/**
 * A simple launcher for the IntegrityService.
 * Takes the arguments:
 * <br/> - Collection id,
 * <br/> - path to settings (the step before the 'collection id' folder)
 * <br/> - The time between updates of the checksums. A third of this will be used for the 'interval'.
 */
public class IntegrityServiceLauncher {
    /** The default collection id in the development.*/
    private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_SETTINGS = "settings/xml";
    
    /** One week. Unless a third argument is given.*/
    private static final long DEFAULT_MAX_TIME_SINCE_UPDATE = 604800000;

    /**
     * @param args <ol>
     * <li> The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.</li>
     * <li> The collection ID to load the settings for.</li>
     * </ol>
     */
    public static void main(String[] args) throws Exception {
        String collectionId;
        String pathToSettings;
        long timeSinceLastChecksumUpdate;
        if(args.length >= 2) {
            collectionId = args[0];
            pathToSettings = args[1];
            
            if(args.length >= 3) {
                timeSinceLastChecksumUpdate = Long.parseLong(args[2]);
            } else {
                timeSinceLastChecksumUpdate = DEFAULT_MAX_TIME_SINCE_UPDATE;
            }
        } else {
            collectionId = DEFAULT_COLLECTION_ID;
            pathToSettings = DEFAULT_PATH_TO_SETTINGS;
            timeSinceLastChecksumUpdate = DEFAULT_MAX_TIME_SINCE_UPDATE;
        }
        
        
        SettingsProvider settingsLoader = new SettingsProvider(
                new XMLFileSettingsLoader(pathToSettings));
        try {
            Settings settings = settingsLoader.getSettings(collectionId);
            SimpleIntegrityService integrityService = new SimpleIntegrityService(settings);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
