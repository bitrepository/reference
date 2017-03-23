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

import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.ConversationBasedAuditTrailClient;
import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.preserver.AuditTrailPreserver;
import org.bitrepository.audittrails.preserver.LocalAuditTrailPreserver;
import org.bitrepository.audittrails.store.AuditTrailDatabaseManager;
import org.bitrepository.audittrails.store.AuditTrailServiceDAO;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.modify.putfile.ConversationBasedPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.SecurityManagerUtil;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.ServiceSettingsProvider;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.service.contributor.SimpleContributorMediator;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.AuditTrailServiceSettings;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.bitrepository.settings.referencesettings.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

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

    /** The properties file holding implementation specifics for the alarm service. */
    private static final String CONFIGFILE = "audittrails.properties";
    /** Property key to tell where to locate the path and filename to the private key file. */
    private static final String PRIVATE_KEY_FILE = "org.bitrepository.audit-trail-service.privateKeyFile";

    private static Settings settings;

    /**
     * Private constructor as the class is meant to be used in a static way.
     */
    private AuditTrailServiceFactory() { }
    
    /**
     * Initialize the factory with configuration. 
     * @param confDir String containing the path to the AuditTrailService's configuration directory
     */
    public static synchronized void init(String confDir) throws IOException {
        configurationDir = confDir;
        loadSettings();

    }
    
    /**
     * Factory method to retrieve AuditTrailService  
     * @return The AuditTrailService.
     */
    public static synchronized AuditTrailService getAuditTrailService() throws IOException {
        if(auditTrailService == null) {

            AuditTrailServiceSettings serviceSettings = settings.getReferenceSettings().getAuditTrailServiceSettings();
            String componentID = serviceSettings.getID();

            Path componentCertificate = Paths.get(getPrivateKeyFile());
            SecurityManager securityManager = SecurityManagerUtil.getSecurityManager(settings,
                                                                                     componentCertificate,
                                                                                     componentID);


            auditTrailService = new AuditTrailService(settings, securityManager);
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

        ServiceSettingsProvider settingsLoader =
                new ServiceSettingsProvider(new XMLFileSettingsLoader(configurationDir), ServiceType.AUDIT_TRAIL_SERVICE);
        settings = settingsLoader.getSettings();
        SettingsUtils.initialize(settings);        
    }

    /**
     * Loads the properties.
     * @throws IOException If any input/output issues occurs.
     */
    private static String getPrivateKeyFile() throws IOException {
        Properties properties = new Properties();
        File propertiesFile = new File(configurationDir, CONFIGFILE);
        try (BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile))) {
            properties.load(propertiesReader);
        }
        return properties.getProperty(PRIVATE_KEY_FILE);
    }
}
