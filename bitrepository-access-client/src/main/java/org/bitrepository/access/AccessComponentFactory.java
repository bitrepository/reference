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
package org.bitrepository.access;

import org.bitrepository.access.getchecksums.BasicGetChecksumsClient;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfile.SimpleGetFileClient;
import org.bitrepository.access.getfileids.BasicGetFileIDsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.collection.settings.standardsettings.Settings;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.protocol.messagebus.MessageBus;
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
     * Method for getting a GetFileClient as defined in the access configuration.<p>
     *
     * @param settings The settings for the GetFileClient.
     * @return A GetFileClient.
     */
    public GetFileClient createGetFileClient(Settings settings) {
        return new SimpleGetFileClient(
                MessageBusFactory.createMessageBus(
                        settings.getProtocol().getMessageBusConfiguration()),
                settings);
    }
    
    /**
     * Method for instantiating a GetChecksumsClient as defined in the access configurations.
     * @param settings The settings for the GetChecksumsClient.
     * @return The GetChecksumsClient
     */
    public GetChecksumsClient createGetChecksumsClient(Settings settings) {
        return new BasicGetChecksumsClient(
                MessageBusFactory.createMessageBus(settings.getProtocol().getMessageBusConfiguration()),
                settings);
    }

    /**
     * Method for getting a GetFileIDsClient as defined in the access configuration.<p>
     *
     * @return A GetFileIDsClient.
     */
    public GetFileIDsClient createGetFileIDsClient(MessageBus messageBus, Settings settings) {
        return new BasicGetFileIDsClient(messageBus, settings);
    }

}
