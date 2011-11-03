package org.bitrepository;

import java.io.IOException;
import java.util.Properties;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;

public class BasicClientFactory {
    private static BasicClient client;

    public synchronized static BasicClient getInstance() {
        if(client == null) {
            SettingsProvider settingsLoader = new SettingsProvider(new WebServiceEnabledXmlFileSettingsReader("."));
            Settings settings = settingsLoader.getSettings("bitrepository-devel");	 
            loadProperties();
            try {
                client = new BasicClient(settings);
            } catch (Exception e) {
            }
        }
        return client;
         
    } 
    
    private static void loadProperties() {
    	Properties properties = new Properties();
    	try {
            properties.load(Thread.currentThread().getContextClassLoader()
                                    .getResourceAsStream("webclient.properties"));
            
            System.setProperty("javax.net.ssl.keyStore", 
            		properties.getProperty("org.bitrepository.webclient.keystorefile"));
            System.setProperty("javax.net.ssl.keyStorePassword", 
            		properties.getProperty("org.bitrepository.webclient.keystorepassword"));
            System.setProperty("javax.net.ssl.trustStore", 
            		properties.getProperty("org.bitrepository.webclient.truststorefile"));
            System.setProperty("javax.net.ssl.trustStorePassword", 
            		properties.getProperty("org.bitrepository.webclient.truststorepassword"));
        } catch (IOException e) {
            //will just fail setting keystore stuff and we won't be able to connect over ssl
        	// not a big deal..
        }
    }
    
}
