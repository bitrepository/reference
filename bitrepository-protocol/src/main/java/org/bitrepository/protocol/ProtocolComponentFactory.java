/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.protocol;

import org.bitrepository.common.ConfigurationFactory;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.configuration.ProtocolConfiguration;

/**
 * Provides access to the different component in the protocol module (Spring wannabe)
 */
public final class ProtocolComponentFactory {

    //---------------------Singleton-------------------------
    private static ProtocolComponentFactory instance;
    public synchronized static ProtocolComponentFactory getInstance() {
        if (instance == null) {
            instance = new ProtocolComponentFactory();
        }
        return instance;
    }

    private ProtocolComponentFactory() {
        moduleCharacteristics = new ModuleCharacteristics("protocol");
    }

    // --------------------- Components-----------------------
    private final ModuleCharacteristics moduleCharacteristics;
    private ProtocolConfiguration protocolConfiguration;
    private MessageBus messagebus;

    public ModuleCharacteristics getModuleCharacteristics() {
        return moduleCharacteristics;
    }

    /**
     * Gets you a object for accessing the Bitrepositories message bus
     */
    public MessageBus getMessageBus() {
        if (messagebus == null) {
            messagebus = new ActiveMQMessageBus(getProtocolConfiguration().getMessageBusConfigurations());
        }
        return messagebus;
    }

    /**
     * Gets you the configuration for this module
     */
    private ProtocolConfiguration getProtocolConfiguration() {
        if (protocolConfiguration == null) {
            protocolConfiguration =
                    ConfigurationFactory.loadConfiguration(getModuleCharacteristics(), ProtocolConfiguration.class);
        }
        return protocolConfiguration;
    }
}
