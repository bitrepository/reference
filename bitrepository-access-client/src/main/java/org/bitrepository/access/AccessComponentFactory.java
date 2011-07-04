/*
 * #%L
 * bitrepository-access-client
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
package org.bitrepository.access;

import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfile.GetFileClientSettings;
import org.bitrepository.access.getfile.SimpleGetFileClient;
import org.bitrepository.access_client.configuration.AccessConfiguration;
import org.bitrepository.common.ConfigurationFactory;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.protocol.messagebus.MessageBusFactory;

/**
 * Factory class for the access module. 
 * Instantiates the instances of the interfaces within this module.
 */
public class AccessComponentFactory {
    /** The singleton instance. */
    private static AccessComponentFactory instance;

    /**
     * Instantiation of this singleton.
     * 
     * @return The singleton instance of this factory class.
     */
    public static AccessComponentFactory getInstance() {
        // ensure singleton.
        if(instance == null) {
            instance = new AccessComponentFactory();
        }
        return instance;
    }

    /** The characteristics for this module.*/
    private ModuleCharacteristics moduleCharacter;
    /** The configuration for this module.*/
    private AccessConfiguration config;

    /**
     * Private constructor for initialization of the singleton.
     */
    private AccessComponentFactory() { 
        moduleCharacter = new ModuleCharacteristics("access-client");
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
    public AccessConfiguration getConfig() {
        if (config == null) {
            ConfigurationFactory configurationFactory = new ConfigurationFactory();
            config =
                configurationFactory.loadConfiguration(getModuleCharacteristics(), AccessConfiguration.class);
        }
        return config;
    }

    /**
     * Method for getting a GetFileClient as defined in the access configuration.<p>
     * 
     * @return A GetFileClient.
     */
    public GetFileClient createGetFileClient(GetFileClientSettings settings) {
        return new SimpleGetFileClient(
                MessageBusFactory.createMessageBus(settings.getMessageBusConfiguration()),
                settings);
    }
}
