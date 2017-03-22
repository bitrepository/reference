/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.preserver.AuditTrailPreserver;
import org.bitrepository.audittrails.preserver.LocalAuditTrailPreserver;
import org.bitrepository.audittrails.store.AuditTrailDatabaseManager;
import org.bitrepository.audittrails.store.AuditTrailServiceDAO;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.SecurityManagerUtil;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.ServiceSettingsProvider;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.service.contributor.SimpleContributorMediator;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.AuditTrailServiceSettings;
import org.bitrepository.settings.referencesettings.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for accessing the AuditTrailService 
 */
public final class AuditTrailServiceFactory {

    private static Logger log = LoggerFactory.getLogger(AuditTrailServiceFactory.class);
    /** The audit trail service. 
     * @see #getAuditTrailService().*/
    private static AuditTrailService auditTrailService;
    /** The path to the directory containing the configuration files.*/
    private static String configurationDir;
    /** The path to the private key file.*/
    private static String privateKeyFile;
    
    /** The properties file holding implementation specifics for the alarm service. */
    private static final String CONFIGFILE = "audittrails.properties";
    /** Property key to tell where to locate the path and filename to the private key file. */
    private static final String PRIVATE_KEY_FILE = "org.bitrepository.audit-trail-service.privateKeyFile";
       
    private static AlarmDispatcher alarmDispatcher;
    private static Settings settings;
    private static SecurityManager securityManager;
    private static MessageBus messageBus;
    
    /**
     * Private constructor as the class is meant to be used in a static way.
     */
    private AuditTrailServiceFactory() { }
    
    /**
     * Initialize the factory with configuration. 
     * @param confDir String containing the path to the AuditTrailService's configuration directory
     */
    public static synchronized void init(String confDir) {
        configurationDir = confDir;
        loadSettings();
        securityManager = SecurityManagerUtil.getSecurityManager(settings, Paths.get(privateKeyFile),
                settings.getReferenceSettings().getAuditTrailServiceSettings().getID());
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
        alarmDispatcher = new AlarmDispatcher(settings, messageBus);
    }
    
    /**
     * Factory method to retrieve AuditTrailService  
     * @return The AuditTrailService.
     */
    public static synchronized AuditTrailService getAuditTrailService() {
        if(auditTrailService == null) {
            ContributorMediator mediator = new SimpleContributorMediator(
                    ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager),
                    settings, null, ProtocolComponentFactory.getInstance().getFileExchange(settings));
            
            PutFileClient putClient = ModifyComponentFactory.getInstance().retrievePutClient(settings, 
                    securityManager, "audit-trail-preserver");
            
            AuditTrailServiceSettings serviceSettings = settings.getReferenceSettings().getAuditTrailServiceSettings();
            
            DatabaseManager auditTrailServiceDatabaseManager = new AuditTrailDatabaseManager(
                    serviceSettings.getAuditTrailServiceDatabase());
            AuditTrailStore store = new AuditTrailServiceDAO(auditTrailServiceDatabaseManager);
            AuditTrailClient client = AccessComponentFactory.createAuditTrailClient(settings,
                    securityManager, serviceSettings.getID());
            
            AuditTrailCollector collector = new AuditTrailCollector(settings, client, store, alarmDispatcher);
            AuditTrailPreserver preserver;
            if (serviceSettings.isSetAuditTrailPreservation()) {
                preserver = new LocalAuditTrailPreserver(
                        settings, store, putClient, ProtocolComponentFactory.getInstance().getFileExchange(settings));
                preserver.start();
            } else {
                log.info("Audit trail preservation disabled, no configuration defined.");
            }
            
            auditTrailService = new AuditTrailService(store, collector, mediator, settings);
        }
        
        return auditTrailService;
    }
    
    /**
     * Retrieves the shared settings based on the directory specified in the {@link #initialize(String)} method.
     */
    private static void loadSettings() {
        if(configurationDir == null) {
            throw new IllegalStateException("No configuration directory has been set!");
        }
        loadProperties();
        ServiceSettingsProvider settingsLoader =
                new ServiceSettingsProvider(new XMLFileSettingsLoader(configurationDir), ServiceType.AUDIT_TRAIL_SERVICE);
        settings = settingsLoader.getSettings();
        SettingsUtils.initialize(settings);        
    }
    
    /**
     * Loads the properties.
     * @throws IOException If any input/output issues occurs.
     */
    private static void loadProperties() {
        try {
        Properties properties = new Properties();
        File propertiesFile = new File(configurationDir, CONFIGFILE);
        BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile));
        properties.load(propertiesReader);
        privateKeyFile = properties.getProperty(PRIVATE_KEY_FILE);
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate the properties.", e);
        }
    }
}
