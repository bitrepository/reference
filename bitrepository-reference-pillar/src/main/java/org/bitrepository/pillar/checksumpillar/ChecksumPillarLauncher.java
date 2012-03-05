package org.bitrepository.pillar.checksumpillar;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.PillarComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Method for launching the ChecksumPillar. 
 * It just loads the settings from the given path, initiates the messagebus (with security) and then starts the 
 * ChecksumPillar.
 */
public class ChecksumPillarLauncher {
    /** The default path to the collection id during development.*/
    private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_SETTINGS = "settings/xml";
    
    /**
     * Private constructor. To prevent instantiation of this utility class.
     */
    private ChecksumPillarLauncher() { }
    
    /**
     * @param args <ol>
     * <li> The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.</li>
     * <li> The collection ID to load the settings for.</li>
     * </ol>
     */
    public static void main(String[] args) {
        String collectionId;
        String pathToSettings;
        String privateKeyFile = "foobar";
        if(args.length >= 2) {
            collectionId = args[0];
            pathToSettings = args[1];
        } else if(args.length == 1) {
            pathToSettings = args[0];
            collectionId = ".";
        } else {
            collectionId = DEFAULT_COLLECTION_ID;
            pathToSettings = DEFAULT_PATH_TO_SETTINGS;
        }
        
        // Instantiate the settings for the ChecksumPillar.
        Settings settings = null;
        try {
            SettingsProvider settingsLoader = new SettingsProvider(
                    new XMLFileSettingsLoader(pathToSettings));
            settings = settingsLoader.getSettings(collectionId);
        } catch (Exception e) {
            System.err.println("Could not load the settings from '" + pathToSettings + "'.");
            e.printStackTrace();
            System.exit(-1);
        }
        
        // Instantiate the security for the messagebus for the ChecksumPillar.
        SecurityManager securityManager = null;
        try {
            PermissionStore permissionStore = new PermissionStore();
            MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
            MessageSigner signer = new BasicMessageSigner();
            OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
            securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                    authenticator, signer, authorizer, permissionStore);
        } catch (Exception e) {
            System.err.println("Could not instantiate the security manager.");
            e.printStackTrace();
            System.exit(-1);
        }
        
        try {
            PillarComponentFactory.getInstance().getChecksumPillar(MessageBusManager.getMessageBus(settings, 
                    securityManager), settings);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }   
}
