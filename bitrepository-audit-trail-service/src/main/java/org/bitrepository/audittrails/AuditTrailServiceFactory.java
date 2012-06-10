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

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.preserver.AuditTrailPreserver;
import org.bitrepository.audittrails.preserver.LocalAuditTrailPreservation;
import org.bitrepository.audittrails.store.AuditTrailServiceDAO;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.service.contributor.SimpleContributorMediator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Factory class for accessing the AuditTrailService 
 */
public final class AuditTrailServiceFactory {
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
    }
    
    /**
     * Factory method to retrieve AuditTrailService  
     * @return The AuditTrailService.
     */
    public static synchronized AuditTrailService getAuditTrailService() {
        if(auditTrailService == null) {
            MessageAuthenticator authenticator;
            MessageSigner signer;
            OperationAuthorizor authorizer;
            PermissionStore permissionStore;
            SecurityManager securityManager;
            SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(configurationDir));
            String auditTrailComponentID =
                    settingsLoader.loadReferenceSettings().getAuditTrailServiceSettings().getID();
            Settings settings = settingsLoader.getSettings(auditTrailComponentID);
            try {
                loadProperties();
                permissionStore = new PermissionStore();
                authenticator = new BasicMessageAuthenticator(permissionStore);
                signer = new BasicMessageSigner();
                authorizer = new BasicOperationAuthorizor(permissionStore);
                securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                        authenticator, signer, authorizer, permissionStore, 
                        settings.getReferenceSettings().getAuditTrailServiceSettings().getID());
                
                ContributorMediator mediator = new SimpleContributorMediator(
                        ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager), 
                        settings, null);
                
                PutFileClient putClient = ModifyComponentFactory.getInstance().retrievePutClient(settings, 
                        securityManager, "audit-trail-preserver");
                
                AuditTrailStore store = new AuditTrailServiceDAO(settings);
                AuditTrailClient client = AccessComponentFactory.getInstance().createAuditTrailClient(settings, 
                        securityManager, settings.getReferenceSettings().getAuditTrailServiceSettings().getID());
                AuditTrailCollector collector = new AuditTrailCollector(settings, client, store);
                AuditTrailPreserver preserver = new LocalAuditTrailPreservation(settings, store, putClient);
                
                auditTrailService = new AuditTrailService(store, collector, mediator, preserver);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        return auditTrailService;
    }
    
    /**
     * Loads the properties.
     * @throws IOException If any input/output issues occurs.
     */
    private static void loadProperties() throws IOException {
        Properties properties = new Properties();
        File propertiesFile = new File(configurationDir, CONFIGFILE);
        BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile));
        properties.load(propertiesReader);
        privateKeyFile = properties.getProperty(PRIVATE_KEY_FILE);
    }
}
