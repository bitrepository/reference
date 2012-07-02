/*
 * #%L
 * Bitrepository Monitoring Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.monitoringservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.service.ServiceSettingsProvider;
import org.bitrepository.settings.referencesettings.ServiceType;

/**
 * The factory for the monitoring service.
 */
public class MonitoringServiceFactory {
    /** The configuration directory containing the settings, et.al.*/
    private static String confDir;
    /** The private key file containing the security information.*/
    private static String privateKeyFile;
    /** The properties file holding implementation specifics for the alarm service. */
    private static final String CONFIGFILE = "monitoring.properties";
    /** Property key to tell where to locate the path and filename to the private key file. */
    private static final String PRIVATE_KEY_FILE = "org.bitrepository.monitoring-service.privateKeyFile";
    /** The settings. */
    private static Settings settings;
    /** The security manager.*/
    private static BasicSecurityManager securityManager;
    /** The implementation of the integrity service.*/
    private static MonitoringService monitoringService;

    /**
     * Private constructor, use static getInstance method to get instance.
     */
    private MonitoringServiceFactory() { }
    
    /**
     * Set the configuration directory. 
     * Should only be run at initialization time. 
     */
    public synchronized static void init(String configurationDir) {
        confDir = configurationDir;
    }
    
    /**
     * Retrieves the settings from the defined location.
     * @return The settings.
     * @see {@link Settings}
     */
    public synchronized static Settings getSettings() {
        if(settings == null) {
            if(confDir == null) {
                throw new IllegalStateException("No configuration directory has been set!");
            }
            loadProperties();
            ServiceSettingsProvider settingsLoader =
                    new ServiceSettingsProvider(new XMLFileSettingsLoader(confDir), ServiceType.MONITORING_SERVICE);

            settings = settingsLoader.getSettings();
        }

        return settings;
    }
    
    /**
     * Instantiated the security manager for the integrity service.
     * @return The security manager.
     * @see #getSettings()
     * @see {@link BasicSecurityManager}
     */
    public synchronized static BasicSecurityManager getSecurityManager() {
        if(securityManager == null) {
            getSettings();
            PermissionStore permissionStore = new PermissionStore();
            MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
            MessageSigner signer = new BasicMessageSigner();
            OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
            securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                    authenticator, signer, authorizer, permissionStore, 
                    settings.getReferenceSettings().getMonitoringServiceSettings().getID());
        }
        
        return securityManager;
    }
    
    /**
     * Factory method to get a singleton instance of the SimpleIntegrityService. 
     * Uses the settings and the security manager.
     * @return The SimpleIntegrityService
     * @see #getSecurityManager()
     * @see #getSettings()
     * @see {@link MonitoringService}
     */
    public synchronized static MonitoringService getMonitoringService() {
        if(monitoringService == null) {
            getSettings();
            getSecurityManager();
            monitoringService = new MonitoringService(settings, securityManager);
        }
        
        return monitoringService;
    }
    
    
    /**
     * Loads the properties.
     */
    private static void loadProperties() {
        try {
            Properties properties = new Properties();
            String propertiesFile = confDir + "/" + CONFIGFILE;
            BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile));
            properties.load(propertiesReader);
            privateKeyFile = properties.getProperty(PRIVATE_KEY_FILE);
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate the properties.", e);
        }
    }
}
