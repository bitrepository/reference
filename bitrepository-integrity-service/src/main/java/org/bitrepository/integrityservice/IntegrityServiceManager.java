/*
 * #%L
 * Bitrepository Protocol
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlarmDispatcher;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityDatabase;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.reports.IntegrityReportProvider;
import org.bitrepository.integrityservice.workflow.IntegrityWorkflowContext;
import org.bitrepository.integrityservice.workflow.IntegrityWorkflowManager;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.SecurityManagerUtil;
import org.bitrepository.service.LifeCycledService;
import org.bitrepository.service.ServiceSettingsProvider;
import org.bitrepository.service.audit.AuditTrailContributerDAOFactory;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.service.contributor.SimpleContributorMediator;
import org.bitrepository.service.scheduler.TimerbasedScheduler;
import org.bitrepository.service.workflow.WorkflowManager;
import org.bitrepository.settings.referencesettings.AlarmLevel;
import org.bitrepository.settings.referencesettings.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the different component in the integrity module.
 */
public final class IntegrityServiceManager {
    private static final Logger log = LoggerFactory.getLogger(IntegrityServiceManager.class);
    private static String privateKeyFile;
    private static File integrityReportStorageDir;

    /** The properties file holding implementation specifics for the integrity service. */
    private static final String CONFIGFILE = "integrity.properties";
    /** Property key to tell where to locate the path and filename to the private key file. */
    private static final String PRIVATE_KEY_FILE = "org.bitrepository.integrity-service.privateKeyFile";
    private static Settings settings;
    private static SecurityManager securityManager;
    private static IntegrityWorkflowManager workFlowManager;
    private static String confDir;
    private static IntegrityLifeCycleHandler lifeCycleHandler;
    private static IntegrityModel model;
    private static ContributorMediator contributor;
    private static MessageBus messageBus;
    private static IntegrityInformationCollector collector;
    private static AuditTrailManager auditManager;
    private static IntegrityAlerter alarmDispatcher;
    private static IntegrityReportProvider integrityReportProvider;

    /**
     * Returns the single instance of the intergity service.
     * @return A new integrity service instance.
     */
    public static LifeCycledService getIntegrityLifeCycleHandler() {
        if (lifeCycleHandler == null) {
            lifeCycleHandler = new IntegrityLifeCycleHandler();
        }
        return lifeCycleHandler;
    }

    /**
     * Initializes the integrity service
     * Should only be run at initialization time.
     * @param configurationDir the configuration dir
     */
    public static synchronized void initialize(String configurationDir) {
        confDir = configurationDir;
        loadSettings();
        String id = settings.getReferenceSettings().getIntegrityServiceSettings().getID();
        securityManager = SecurityManagerUtil.getSecurityManager(settings, Paths.get(privateKeyFile), id); 
                
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
        
        AuditTrailContributerDAOFactory daoFactory = new AuditTrailContributerDAOFactory();
        auditManager = daoFactory.getAuditTrailContributorDAO(
                settings.getReferenceSettings().getIntegrityServiceSettings().getAuditTrailContributerDatabase(),
                settings.getComponentID());
                
        alarmDispatcher = new IntegrityAlarmDispatcher(settings, messageBus, AlarmLevel.ERROR);
        model = new IntegrityDatabase(settings);

        AccessComponentFactory acf = AccessComponentFactory.getInstance();
        ModifyComponentFactory mcf = ModifyComponentFactory.getInstance();
        
        collector = new DelegatingIntegrityInformationCollector(
                acf.createGetFileIDsClient(settings, securityManager, id),
                acf.createGetChecksumsClient(settings, securityManager, id),
                acf.createGetFileClient(settings, securityManager, id),
                mcf.retrievePutClient(settings, securityManager, id));
        integrityReportProvider = new IntegrityReportProvider(integrityReportStorageDir);
        
        workFlowManager = new IntegrityWorkflowManager(
                new IntegrityWorkflowContext(settings, collector, model, alarmDispatcher, auditManager),
                new TimerbasedScheduler());
        contributor = new SimpleContributorMediator(messageBus, settings, auditManager, null);
        contributor.start();
    }

    /**
     * Retrieves the shared settings based on the directory specified in the {@link #initialize(String)} method.
     * @return The settings to used for the integrity service.
     */
    private static void loadSettings() {
        if(confDir == null) {
            throw new IllegalStateException("No configuration directory has been set!");
        }
        loadProperties();
        ServiceSettingsProvider settingsLoader =
                new ServiceSettingsProvider(new XMLFileSettingsLoader(confDir), ServiceType.INTEGRITY_SERVICE);
        settings = settingsLoader.getSettings();
        SettingsUtils.initialize(settings);
        integrityReportStorageDir = new File(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityReportsDir());
        if(!(integrityReportStorageDir.isDirectory() 
                && integrityReportStorageDir.canRead() 
                && integrityReportStorageDir.canWrite())) {
            throw new IllegalStateException(integrityReportStorageDir.getAbsolutePath() 
                    + " is either not a directory, can't be read or written to.");
        }        
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
                propertiesReader = new BufferedReader(new InputStreamReader(new FileInputStream(propertiesFile), StandardCharsets.UTF_8));
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

    /**
     * Gets you the <code>IntegrityModel</code> that contains the data needed to perform integrity operations.
     * @return the <code>IntegrityModel</code> that contains integrity information.
     */
    public static IntegrityModel getIntegrityModel() {
        return model;
    }
    
    /**
     *  @return Gets the directory for integrity report storage.
     */
    public static File getIntegrityReportStorageDir() {
        return integrityReportStorageDir;
    }

    public static IntegrityReportProvider getIntegrityReportProvider() {
        return integrityReportProvider;
    }
    
    /**
     * @return Gets you the <code>WorkflowManager</code> exposing the workflow model.
     */
    public static WorkflowManager getWorkflowManager() {
        return workFlowManager;
    }

    public static class IntegrityLifeCycleHandler implements LifeCycledService {
        @Override
        public void start() {}

        @Override
        public void shutdown() {
            if(messageBus != null) {
                try {
                    messageBus.close();
                } catch (Exception e) {
                    log.warn("Encountered issues when closing down the messagebus.", e);
                }
            }
            if(contributor != null) {
                contributor.close();
            }

            if(model != null) {
                model.close();
            }
        }
    }
}
