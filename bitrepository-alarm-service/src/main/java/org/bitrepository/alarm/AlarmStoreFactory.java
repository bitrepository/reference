package org.bitrepository.alarm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;

public class AlarmStoreFactory {
	/** The instance of the alarmStore */
    private static AlarmStore alarmStore;
    /** Member to hold the directory path of where the configuration files are located. */
    private static String confDir; 
    /** Member to hold the name of the file which persists alarms, once read from propperties */
    private static String alarmStoreFile;
    /** The propperties file holding implementation specifics for the alarm service. */
    private static final String CONFIGFILE = "alarmservice.properties"; 
    /** Property key for keystore file path setting */
    private static final String KEYSTOREFILE = "org.bitrepository.webclient.keystorefile";
    /** Property key for keystore file password setting */
    private static final String KEYSTOREPASSWD = "org.bitrepository.webclient.keystorepassword";
    /** Property key for truststore file path setting */
    private static final String TRUSTSTOREFILE = "org.bitrepository.webclient.truststorefile";
    /** Property key for truststore file password setting */
    private static final String TRUSTSTOREPASSWD = "org.bitrepository.webclient.truststorepassword";
    /** Property key for the propperty holding the path the file persisting alarms */
    private static final String ALARMSTOREFILE = "org.bitrepository.webclient.alarmstorefile";
    /** Java environment propperty for setting keystore file */
    private static final String JAVA_KEYSTORE_PROP = "javax.net.ssl.keyStore";
    /** Java environment propperty for setting keystore password */
    private static final String JAVA_KEYSTOREPASS_PROP = "javax.net.ssl.keyStorePassword";
    /** Java environment propperty for setting truststore file */
    private static final String JAVA_TRUSTSTORE_PROP = "javax.net.ssl.trustStore";
    /** Java environment propperty for setting truststore password */
    private static final String JAVA_TRUSTSTOREPASS_PROP = "javax.net.ssl.trustStorePassword";
    /** Default collection settings identifier (used to build the path the collection and referencesettings */
    private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";

    private AlarmStoreFactory() {
    	//Empty constructor
    }
    
    /**
     * Set the configuration directory. 
     * Should only be run at initialization time. 
     */
    public synchronized static void init(String configurationDir) {
    	confDir = configurationDir;
    }
    
    /**
     *	Factory method to get a singleton instance of BasicClient
     *	@return The BasicClient instance or a null in case of trouble.  
     */
    public synchronized static AlarmStore getAlarmStore() {
        if(alarmStore == null) {
        	if(confDir == null) {
        		throw new RuntimeException("No configuration dir has been set!");
        	}
        	SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(confDir));
            Settings settings = settingsLoader.getSettings(DEFAULT_COLLECTION_ID);	 
            try {
            	loadProperties();
                alarmStore = new AlarmStore(settings, alarmStoreFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return alarmStore;
    } 
    
    private static void loadProperties() throws IOException {
    	Properties properties = new Properties();
    	String propertiesFile = confDir + "/" + CONFIGFILE;
    	BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile));
    	properties.load(propertiesReader);

    	System.setProperty(JAVA_KEYSTORE_PROP, properties.getProperty(KEYSTOREFILE));
    	System.setProperty(JAVA_KEYSTOREPASS_PROP, properties.getProperty(KEYSTOREPASSWD));
    	System.setProperty(JAVA_TRUSTSTORE_PROP, properties.getProperty(TRUSTSTOREFILE));
    	System.setProperty(JAVA_TRUSTSTOREPASS_PROP, properties.getProperty(TRUSTSTOREPASSWD));
    	alarmStoreFile = properties.getProperty(ALARMSTOREFILE);
    }
    
}
