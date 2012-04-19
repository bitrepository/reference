package org.bitrepository.pillar;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;

/**
 * Common interface for the pillar launchers. Contains the methods for extracting the resources to instantiate the 
 * pillars.
 */
public abstract class PillarLauncher {
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_SETTINGS = "conf";
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_KEY_FILE = "conf/client.pem";
    
    /**
     * Method for retrieving the settings for the launcher.
     * @param pathToSettings The path to the settings. If it is null or empty, then the default path is used.
     * @return The settings.
     */
    protected static Settings loadSettings(String pathToSettings) {
        SettingsProvider settingsLoader;
        if(pathToSettings == null || pathToSettings.isEmpty()) {
            settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(DEFAULT_PATH_TO_SETTINGS));
        } else {
            settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(pathToSettings));
        }
        
        return settingsLoader.getSettings();        
    }
    
    /**
     * Instantiates the security manager based on the settings and the path to the key file.
     * @param pathToPrivateKeyFile The path to the key file.
     * @param settings The settings.
     * @return The security manager.
     */
    protected static BasicSecurityManager loadSecurityManager(String pathToPrivateKeyFile, Settings settings) {
        String privateKeyFile;
        if(pathToPrivateKeyFile == null || pathToPrivateKeyFile.isEmpty()) {
            privateKeyFile = DEFAULT_PATH_TO_KEY_FILE;
        } else {
            privateKeyFile = pathToPrivateKeyFile;
        }
        
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                authenticator, signer, authorizer, permissionStore,
                settings.getReferenceSettings().getPillarSettings().getPillarID());
    }
}
