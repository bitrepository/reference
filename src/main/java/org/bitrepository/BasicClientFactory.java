package org.bitrepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.webservice.ServiceUrl;

public class BasicClientFactory {
    private static BasicClient client;
    private static String confDir; 
    private static String logFile;
    private static final String CONFIGFILE = "webclient.properties"; 
    private static final String KEYSTOREFILE = "org.bitrepository.webclient.keystorefile";
    private static final String KEYSTOREPASSWD = "org.bitrepository.webclient.keystorepassword";
    private static final String TRUSTSTOREFILE = "org.bitrepository.webclient.truststorefile";
    private static final String TRUSTSTOREPASSWD = "org.bitrepository.webclient.truststorepassword";
    private static final String LOGFILE = "org.bitrepository.webclient.logfile";
    
    /**
     * Set the configuration directory. 
     * Should only be run at initialization time. 
     */
    public synchronized static void init(String configurationDir) {
    	confDir = configurationDir;
    	ServiceUrl.init(configurationDir);
    }
    
    /**
     *	Factory method to get a singleton instance of BasicClient
     *	@return The BasicClient instance or a null in case of trouble.  
     */
    public synchronized static BasicClient getInstance() {
        if(client == null) {
        	if(confDir == null) {
        		throw new RuntimeException("No configuration dir has been set!");
        	}
        	SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(confDir));
            Settings settings = settingsLoader.getSettings("bitrepository-devel");	 
            loadProperties();
            try {
                client = new BasicClient(settings, logFile);
            } catch (Exception e) {
            	//won't handle..
                throw new RuntimeException(e);
            }
        }
        return client;
         
    } 
    
    private static void loadProperties() {
    	Properties properties = new Properties();
    	try {
    		String propertiesFile = confDir + "/" + CONFIGFILE;
    		BufferedReader reader
    		   = new BufferedReader(new FileReader(propertiesFile));
    		properties.load(reader);
    		
            System.setProperty("javax.net.ssl.keyStore", 
            		properties.getProperty(KEYSTOREFILE));
            System.setProperty("javax.net.ssl.keyStorePassword", 
            		properties.getProperty(KEYSTOREPASSWD));
            System.setProperty("javax.net.ssl.trustStore", 
            		properties.getProperty(TRUSTSTOREFILE));
            System.setProperty("javax.net.ssl.trustStorePassword", 
            		properties.getProperty(TRUSTSTOREPASSWD));
            logFile = properties.getProperty(LOGFILE);
        } catch (IOException e) {
            //will just fail setting keystore stuff and we won't be able to connect over ssl
        	// not a big deal..
        }
    }
    
}
