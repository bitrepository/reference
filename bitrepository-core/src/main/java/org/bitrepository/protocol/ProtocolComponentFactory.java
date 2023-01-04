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

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.utils.FileExchangeResolver;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;

/**
 * Provides access to the different components in the org.bitrepository.protocol module (Spring/IOC wannabe)
 */
public final class ProtocolComponentFactory {
    private static ProtocolComponentFactory instance;

    public static synchronized ProtocolComponentFactory getInstance() {
        if (instance == null) {
            instance = new ProtocolComponentFactory();
        }
        return instance;
    }

    /**
     * The singleton constructor.
     */
    private ProtocolComponentFactory() {
    }

    /**
     * @see #getFileExchange(Settings)
     */
    private FileExchange fileExchange;

    /**
     * Gets you an <code>MessageBus</code> instance for accessing the BitRepository's message bus. If a message bus
     * already exists for the collection ID defined in the settings, the existing instance is returned.
     *
     * @param settings        The settings to get the MessageBus
     * @param securityManager The SecurityManager for the message bus
     * @return The message bus for this collection.
     */
    public MessageBus getMessageBus(Settings settings, SecurityManager securityManager) {
        return MessageBusManager.getMessageBus(settings, securityManager);
    }

    /**
     * Gets you a <code>FileExchange</code> instance for data communication. Is instantiated based on the
     * configurations.
     *
     * @param settings The settings for the file exchange.
     * @return The FileExchange according to the configuration.
     */
    public FileExchange getFileExchange(Settings settings) {
        if (fileExchange == null) {
            FileExchangeSettings fileExchangeSettings = settings.getReferenceSettings().getFileExchangeSettings();
            if (fileExchangeSettings != null) {
                fileExchange = FileExchangeResolver.getFileExchange(fileExchangeSettings);
            } else {
                throw new IllegalStateException("No FileExchangeSettings found in configuration");
            }
        }
        return fileExchange;
    }
}
