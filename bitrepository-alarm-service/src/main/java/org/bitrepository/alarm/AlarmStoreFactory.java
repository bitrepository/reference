package org.bitrepository.alarm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;

public class AlarmStoreFactory {
    private static AlarmStore alarmStore;
    private static String confDir; 
    private static String alarmStoreFile;
    private static final String CONFIGFILE = "alarmservice.properties"; 
    private static final String KEYSTOREFILE = "org.bitrepository.webclient.keystorefile";
    private static final String KEYSTOREPASSWD = "org.bitrepository.webclient.keystorepassword";
    private static final String TRUSTSTOREFILE = "org.bitrepository.webclient.truststorefile";
    private static final String TRUSTSTOREPASSWD = "org.bitrepository.webclient.truststorepassword";
    private static final String ALARMSTOREFILE = "org.bitrepository.webclient.alarmstorefile";
    private static final String JAVA_KEYSTORE_PROP = "javax.net.ssl.keyStore";
    private static final String JAVA_KEYSTOREPASS_PROP = "javax.net.ssl.keyStorePassword";
    private static final String JAVA_TRUSTSTORE_PROP = "javax.net.ssl.trustStore";
    private static final String JAVA_TRUSTSTOREPASS_PROP = "javax.net.ssl.trustStorePassword";
    private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";

    
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
    public synchronized static AlarmStore getInstance() {
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
