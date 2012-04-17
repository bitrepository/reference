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

import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.http.HTTPFileExchange;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Provides access to the different component in the org.bitrepository.org.bitrepository.protocol module (Spring/IOC wannabe)
 */
public final class ProtocolComponentFactory {

    /** The singleton instance */
    private static ProtocolComponentFactory instance;

    /**
     * The singletonic access to the instance of this class
     * @return The one and only instance
     */
    public static synchronized ProtocolComponentFactory getInstance() {
        if (instance == null) {
            instance = new ProtocolComponentFactory();
        }
        return instance;
    }

    /**
     * The singleton constructor.
     */
    protected ProtocolComponentFactory() { }

    /** @see #getModuleCharacteristics() */
    private static final ModuleCharacteristics MODULE_CHARACTERISTICS = new ModuleCharacteristics("org/bitrepository/protocol/org.bitrepository.protocol");
    /** @see #getFileExchange() */
    protected FileExchange fileExchange;

    /**
     * Gets you a <code>ModuleCharacteristics</code> object defining the generic characteristics of this module
     * @return A <code>ModuleCharacteristics</code> object defining the generic characteristics of this module 
     */
    public ModuleCharacteristics getModuleCharacteristics() {
        return MODULE_CHARACTERISTICS;
    }

    /**
     * Gets you an <code>MessageBus</code> instance for accessing the Bitrepositorys message bus. If a messagebus 
     * already exists for the collection ID defined in the settings, the existing instance is returned.
     * @return The messagebus for this collection.
     */
    public MessageBus getMessageBus(Settings settings, SecurityManager securityManager) {
        return MessageBusManager.getMessageBus(settings, securityManager);
    }

    /**
     * Gets you an <code>FileExchange</code> instance for data communication. Is instantiated based on the 
     * configurations.
     * @return The FileExchange according to the configuration.
     * @deprecated The file exchange class will be replaced by the HttpServerConnector.
     */
    public FileExchange getFileExchange() {
        if (fileExchange == null) {
            // TODO handle different?
            fileExchange = new HTTPFileExchange();
        }
        return fileExchange;
    }
}
