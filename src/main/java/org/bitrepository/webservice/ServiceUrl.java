package org.bitrepository.webservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceUrl {
    private static final String CONFIGFILE = "services.properties";
    private static final String ALARMURL = "org.bitrepository.webclient.alarmserviceurl"; 
    private static final String AUDITTRAILURL = "org.bitrepository.webclient.audittrailserviceurl"; 
    private static final String INTEGRITYURL = "org.bitrepository.webclient.integrityserviceurl"; 
    private static final String MONITORINGURL = "org.bitrepository.webclient.monitoringserviceurl";
    private static final String DEFAULTHTTPURL = "org.bitrepository.webclient.defaulthttpserverurl"; 
        
	private static String alarmUrl = "";
	private static String auditTrailUrl = "";
	private static String integrityUrl = "";
    private static String monitoringUrl = "";
	private static String defaultHttpUrl = "";
	private static String configDir;
    private final Logger log = LoggerFactory.getLogger(getClass());

	
	ServiceUrl() {
		if(configDir == null) {
			log.debug("ServiceUrl constructor called before init!!");
    		throw new RuntimeException("No configuration dir has been set!");
    	}
		log.debug("Called ServiceUrl constructor, confdir: " + configDir);
        loadProperties();
	}
	
    private void loadProperties() {
    	log.debug("Loading service properties..");
    	Properties properties = new Properties();
    	try {
    		String propertiesFile = configDir + "/" + CONFIGFILE;
    		BufferedReader reader
    		   = new BufferedReader(new FileReader(propertiesFile));
    		properties.load(reader);
    		
    		alarmUrl = properties.getProperty(ALARMURL);
    		auditTrailUrl = properties.getProperty(AUDITTRAILURL);
    		integrityUrl = properties.getProperty(INTEGRITYURL);
    		monitoringUrl = properties.getProperty(MONITORINGURL);
    		defaultHttpUrl = properties.getProperty(DEFAULTHTTPURL);
    		log.debug("Properties has been loaded, alarm: " + alarmUrl + ", auditTrail: " + auditTrailUrl + 
    				", integrity: " + integrityUrl + ", http: " + defaultHttpUrl);
    		
        } catch (IOException e) {
            //will just fail setting keystore stuff and we won't be able to connect over ssl
        	// not a big deal..
        	log.debug("Caught I/O exception while loading properties", e);
        }
    }
	
	public synchronized static void init(String confDir) {
		configDir = confDir;
	}
	
	public String getAlarmServiceUrl() {
		return alarmUrl;
	}
	
	public String getAuditTrailServiceUrl() {
		return auditTrailUrl;
	}
	
	public String getIntegrityServiceUrl() {
		return integrityUrl;
	}
	
	public String getMonitoringServiceUrl() {
	    return monitoringUrl;
	}
	
	public String getDefaultHttpServerUrl() {
		return defaultHttpUrl;
	}
}
