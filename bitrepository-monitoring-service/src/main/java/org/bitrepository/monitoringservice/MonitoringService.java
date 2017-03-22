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

import java.util.Map;
import javax.jms.JMSException;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getstatus.GetStatusClient;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.monitoringservice.alarm.BasicMonitoringServiceAlerter;
import org.bitrepository.monitoringservice.alarm.MonitorAlerter;
import org.bitrepository.monitoringservice.collector.StatusCollector;
import org.bitrepository.monitoringservice.status.ComponentStatus;
import org.bitrepository.monitoringservice.status.ComponentStatusStore;
import org.bitrepository.monitoringservice.status.StatusStore;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.LifeCycledService;
import org.bitrepository.settings.referencesettings.AlarmLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The monitoring service.
 */
public class MonitoringService implements LifeCycledService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The settings. */
    private final Settings settings;
    /** The store of collected statuses */
    private final StatusStore statusStore;
    /** The client for getting statuses. */
    private final GetStatusClient getStatusClient;
    /** The alerter for sending alarms */
    private final MonitorAlerter alerter;
    /** The status collector */
    private final StatusCollector collector;
    
    /**
     * Constructor.
     * @param settings The settings.
     * @param securityManager The security manager.
     */
    public MonitoringService(Settings settings, SecurityManager securityManager) {
        this.settings = settings;
        SettingsUtils.initialize(settings);
        MessageBus messageBus = MessageBusManager.getMessageBus(settings, securityManager);
        statusStore = new ComponentStatusStore(SettingsUtils.getStatusContributorsForCollection(settings));
        alerter = new BasicMonitoringServiceAlerter(settings, messageBus, AlarmLevel.ERROR, statusStore);
        getStatusClient = AccessComponentFactory.getInstance().createGetStatusClient(settings, securityManager,
                settings.getReferenceSettings().getMonitoringServiceSettings().getID());
        collector = new StatusCollector(getStatusClient, settings, statusStore, alerter);
        collector.start();
    }
    
    /**
     * @return The map of the status for the components.
     */
    public Map<String, ComponentStatus> getStatus() {
        return statusStore.getStatusMap();
    }
    
    @Override
    public void start() {}
    
    /**
     * @return The maximum number of attempts to retrieve a status from a component before dispatching an alarm.
     */
    public int getMaxRetries() {
        return settings.getReferenceSettings().getMonitoringServiceSettings().getMaxRetries().intValue();
    } 
    
    /**
     * @return The interval between collecting status from the components.
     */
    public long getCollectionInterval() {
        return settings.getReferenceSettings().getMonitoringServiceSettings().getCollectionInterval();
    }
    
    @Override
    public void shutdown() {
        collector.stop();
        MessageBus messageBus = MessageBusManager.getMessageBus();
        if ( messageBus != null) {
            try {
                messageBus.close();
            } catch (JMSException e) {
                log.warn("Failed to close message bus cleanly, " + e.getMessage());
            }
        }
    }
}
