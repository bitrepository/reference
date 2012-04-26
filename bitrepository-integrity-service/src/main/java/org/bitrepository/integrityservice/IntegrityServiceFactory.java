/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.integrityservice;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.integrityservice.web.IntegrityServiceWebInterface;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Factory class for instantiating the integrity service
 */
public final class IntegrityServiceFactory {
    /** The configuration directory containing the settings, et.al.*/
    private static String confDir;
    /** The private key file containing the security information.*/
    private static String privateKeyFile;
    
    /** The properties file holding implementation specifics for the alarm service. */
    private static final String CONFIGFILE = "integrity.properties";
    /** Property key to tell where to locate the path and filename to the private key file. */
    private static final String PRIVATE_KEY_FILE = "org.bitrepository.integrity-service.privateKeyFile";
    /** The time of one week.*/
    private static final long DEFAULT_MAX_TIME_SINCE_UPDATE = 604800000;
    
    
    /** The settings. */
    private static Settings settings;
    /** The security manager.*/
    private static BasicSecurityManager securityManager;
    /** The implementation of the integrity service.*/
    private static SimpleIntegrityService simpleIntegrityService;
    /** The integrity service wrapped into the web interface.*/
    private static IntegrityServiceWebInterface integrityServiceWebInterface;

    /**
     * Private constructor, use static getInstance method to get instance.
     */
    private IntegrityServiceFactory() {
        //Empty constructor 
    }
    
    /**
     * Set the configuration directory. 
     * Should only be run at initialization time. 
     */
    public static synchronized void init(String configurationDir) {
        confDir = configurationDir;
    }
    
    /**
     * Retrieves the settings from the defined location.
     * @return The settings.
     * @see {@link Settings}
     */
    public static synchronized Settings getSettings() {
        if(settings == null) {
            if(confDir == null) {
                throw new IllegalStateException("No configuration directory has been set!");
            }
            loadProperties();
            SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(confDir));
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
    public static synchronized BasicSecurityManager getSecurityManager() {
        if(securityManager == null) {
            getSettings();
            PermissionStore permissionStore = new PermissionStore();
            MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
            MessageSigner signer = new BasicMessageSigner();
            OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
            securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                    authenticator, signer, authorizer, permissionStore,
                    settings.getReferenceSettings().getIntegrityServiceSettings().getID());
        }
        
        return securityManager;
    }
    
    /**
     * Factory method to get a singleton instance of the SimpleIntegrityService. 
     * Uses the settings and the security manager.
     * @return The SimpleIntegrityService
     * @see #getSecurityManager()
     * @see #getSettings()
     * @see {@link SimpleIntegrityService} 
     */
    public static synchronized SimpleIntegrityService getIntegrityService() {
        if(simpleIntegrityService == null) {
            getSettings();
            getSecurityManager();
            simpleIntegrityService = new SimpleIntegrityService(settings, securityManager);
        }
        
        return simpleIntegrityService;
    }
    
    /**
     * Factory method to get a singleton instance of the web interface for the integrity service.
     * Uses the Settings and the SimpleIntegrityService.
     * @return The IntegrityServiceWebInterface instance.
     * @see #getSettings()
     * @see #getIntegrityService()
     * @see {@link IntegrityServiceWebInterface}
     */
    public static synchronized IntegrityServiceWebInterface getIntegrityServiceWebInterface() {
        if(integrityServiceWebInterface == null) {
            long timeSinceLastChecksumUpdate = DEFAULT_MAX_TIME_SINCE_UPDATE;
            long timeSinceLastFileIDsUpdate = DEFAULT_MAX_TIME_SINCE_UPDATE;
            getSettings();
            getIntegrityService();
            integrityServiceWebInterface = new IntegrityServiceWebInterface(simpleIntegrityService, settings);
            simpleIntegrityService.startChecksumIntegrityCheck(timeSinceLastChecksumUpdate, 
                    settings.getReferenceSettings().getIntegrityServiceSettings().getSchedulerInterval());
            simpleIntegrityService.startAllFileIDsIntegrityCheck(timeSinceLastFileIDsUpdate);
        }
        return integrityServiceWebInterface;
    }
    
    /**
     * Loads the properties.
     */
    private static void loadProperties() {
        try {
            Properties properties = new Properties();
            String propertiesFile = confDir + "/" + CONFIGFILE;
            BufferedReader propertiesReader = null; 
            try {
                propertiesReader = new BufferedReader(new FileReader(propertiesFile));
                properties.load(propertiesReader);
                privateKeyFile = properties.getProperty(PRIVATE_KEY_FILE);
            } finally {
                if(propertiesReader != null) {
                    propertiesReader.close();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate the properties.", e);
        }
    }
}
