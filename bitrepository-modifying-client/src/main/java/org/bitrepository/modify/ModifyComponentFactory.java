/*
 * #%L
 * Bitmagasin modify client
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

package org.bitrepository.modify;

import org.bitrepository.common.ConfigurationFactory;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.modify.put.PutClient;
import org.bitrepository.modify.put.PutFileClientSettings;
import org.bitrepository.modify.put.SimplePutClient;
import org.bitrepository.modify_client.configuration.ModifyConfiguration;
import org.bitrepository.protocol.messagebus.MessageBusFactory;

/**
 * Factory class for the access module. 
 * Instantiates the instances of the interfaces within this module.
 */
public class ModifyComponentFactory {
    /** The singleton instance. */
    private static ModifyComponentFactory instance;

    /**
     * Instantiation of this singleton.
     * 
     * @return The singleton instance of this factory class.
     */
    public static ModifyComponentFactory getInstance() {
        // ensure singleton.
        if(instance == null) {
            instance = new ModifyComponentFactory();
        }
        return instance;
    }

    /** The characteristics for this module.*/
    private ModuleCharacteristics moduleCharacter;
    /** The configuration for this module.*/
    private ModifyConfiguration config;

    /**
     * Private constructor for initialisation of the singleton.
     */
    private ModifyComponentFactory() { 
        moduleCharacter = new ModuleCharacteristics("modify-client");
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
    public ModifyConfiguration getConfig() {
        if (config == null) {
            ConfigurationFactory configurationFactory = new ConfigurationFactory();
            config =
                configurationFactory.loadConfiguration(getModuleCharacteristics(), ModifyConfiguration.class);
        }
        return config;
    }
    
    /**
     * Method for initialising the PutClient in the configuration.
     * TODO use the configuration instead of this default. 
     * @return The configured PutClient.
     */
    public PutClient retrievePutClient(PutFileClientSettings settings) {
        return new SimplePutClient(
                MessageBusFactory.createMessageBus(settings.getMessageBusConfiguration()),
                settings);
    }
}
