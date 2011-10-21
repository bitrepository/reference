/*
 * #%L
 * Bitrepository Integration
 * 
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
package org.bitrepository.integration;

import org.bitrepository.common.ConfigurationFactory;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integration.configuration.integrationconfiguration.IntegrationConfiguration;
import org.bitrepository.pillar.ReferencePillar;
import org.bitrepository.protocol.messagebus.MessageBusManager;

/**
 * Component factory for this module.
 */
public class IntegrationComponentFactory {
    /** The singleton instance. */
    private static IntegrationComponentFactory instance;

    /**
     * Instantiation of this singleton.
     *
     * @return The singleton instance of this factory class.
     */
    public static IntegrationComponentFactory getInstance() {
        // ensure singleton.
        if(instance == null) {
            instance = new IntegrationComponentFactory();
        }
        return instance;
    }

    /** The characteristics for this module.*/
    private ModuleCharacteristics moduleCharacter;
    /** The configuration for this module.*/
    private IntegrationConfiguration config;

    /**
     * Private constructor for initialization of the singleton.
     */
    private IntegrationComponentFactory() {
        moduleCharacter = new ModuleCharacteristics("integration");
    }

    /**
     * Method for retrieving the characteristics for this module.
     * @return The characteristics for this module.
     */
    public ModuleCharacteristics getModuleCharacteristics() {
        return moduleCharacter;
    }

    /**
     * Method for extracting the configuration for the integration module.
     * @return The integration module configuration.
     */
    public IntegrationConfiguration getConfig() {
        if (config == null) {
            ConfigurationFactory configurationFactory = new ConfigurationFactory();
            config = configurationFactory.loadConfiguration(getModuleCharacteristics(), 
                    IntegrationConfiguration.class);
        }
        return config;
    }

    /**
     * Method for retrieving a reference pillar.
     * @param settings The settings for the pillar.
     * @return The reference requested pillar.
     */
    public ReferencePillar getPillar(Settings settings) {
        return new ReferencePillar(MessageBusManager.getMessageBus(settings), settings);
    }
}
