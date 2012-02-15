package org.bitrepository.integrityclient;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.integrityclient.IntegrityService;
import org.bitrepository.integrityclient.SimpleIntegrityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IntegrityServiceFactory {

    private Logger log = LoggerFactory.getLogger(IntegrityServiceFactory.class);
    private static String confDir;
    private static IntegrityService integrityService;
    
    /** Default collection settings identifier (used to build the path the collection and referencesettings */
    private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
    /** The properties file holding implementation specifics for the alarm service. */
    private static final String CONFIGFILE = "integrity.properties"; 
    /** Property key for keystore file path setting */
    private static final String KEYSTOREFILE = "org.bitrepository.webclient.keystorefile";
    /** Property key for keystore file password setting */
    private static final String KEYSTOREPASSWD = "org.bitrepository.webclient.keystorepassword";
    /** Property key for truststore file path setting */
    private static final String TRUSTSTOREFILE = "org.bitrepository.webclient.truststorefile";
    /** Property key for truststore file password setting */
    private static final String TRUSTSTOREPASSWD = "org.bitrepository.webclient.truststorepassword";
    /** Java environment property for setting keystore file */
    private static final String JAVA_KEYSTORE_PROP = "javax.net.ssl.keyStore";
    /** Java environment property for setting keystore password */
    private static final String JAVA_KEYSTOREPASS_PROP = "javax.net.ssl.keyStorePassword";
    /** Java environment property for setting truststore file */
    private static final String JAVA_TRUSTSTORE_PROP = "javax.net.ssl.trustStore";
    /** Java environment property for setting truststore password */
    private static final String JAVA_TRUSTSTOREPASS_PROP = "javax.net.ssl.trustStorePassword";
    
    
    private IntegrityServiceFactory() {
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
    public synchronized static IntegrityService getIntegrityService() {
        if(integrityService == null) {
        	if(confDir == null) {
        		throw new RuntimeException("No configuration dir has been set!");
        	}
        	SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(confDir));
            Settings settings = settingsLoader.getSettings(DEFAULT_COLLECTION_ID);	 
            try {
            	loadProperties();
            	SimpleIntegrityService simpleIntegrityService = new SimpleIntegrityService(settings);
                integrityService = new IntegrityService(simpleIntegrityService, settings);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return integrityService;
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
    }
}
