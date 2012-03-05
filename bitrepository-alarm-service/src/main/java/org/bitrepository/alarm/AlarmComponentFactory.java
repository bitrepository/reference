/*
 * #%L
 * bitrepository-access-client
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
package org.bitrepository.alarm;

import org.bitrepository.alarm_service.alarmconfiguration.AlarmConfiguration;
import org.bitrepository.common.ConfigurationFactory;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Factory for the Alarm client module.
 * Instantiates the instances of the interfaces in the Alarm client module.
 */
public final class AlarmComponentFactory {
    /** The singleton component factory.*/
    private static AlarmComponentFactory instance;
    /** The characteristics for this module.*/
    private ModuleCharacteristics moduleCharacter;
    /** The configuration for this module.*/
    private AlarmConfiguration config;

    /** Constructor. Private to avoid instantiation of this utility class.*/
    private AlarmComponentFactory() { 
        moduleCharacter = new ModuleCharacteristics("alarm-service");
    }

    /**
     * Method for retrieving the singleton instance of this factory.
     * @return The singleton instance.
     */
    public static synchronized AlarmComponentFactory getInstance() {
        if(instance == null) {
            instance = new AlarmComponentFactory();
        }
        return instance;
    }

    /**
     * Method for retrieving the characteristics for this module.
     * @return The characteristics for this module.
     */
    public ModuleCharacteristics getModuleCharacteristics() {
        return moduleCharacter;
    }

    /**
     * Method for extracting the configuration for the access module.
     * @return The access module configuration.
     */
    public AlarmConfiguration getConfig() {
        if (config == null) {
            ConfigurationFactory configurationFactory = new ConfigurationFactory();
            config = configurationFactory.loadConfiguration(getModuleCharacteristics(), AlarmConfiguration.class);
        }
        return config;
    }

    /**
     * Retrieves a Alarm client based on the settings.
     * 
     * @param settings The settings for the AlarmClient.
     * @return The AlarmClient.
     */
    public AlarmService getAlarmService(Settings settings, SecurityManager securityManager) {
        try {
            MessageBus bus = MessageBusManager.getMessageBus(settings, securityManager);
            AlarmService service = new BasicAlarmService(bus);
            
            return service;
        } catch (Exception e) {
            throw new AlarmException("Cannot instantiate the AlarmClient.", e); 
        }
    }
}
