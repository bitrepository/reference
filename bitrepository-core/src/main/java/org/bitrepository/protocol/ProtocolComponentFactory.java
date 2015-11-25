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
import org.bitrepository.protocol.http.HttpFileExchange;
import org.bitrepository.protocol.http.HttpsFileExchange;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.referencesettings.ProtocolType;

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

    /** @see #getFileExchange(Settings) */
    protected FileExchange fileExchange;

    /**
     * Gets you an <code>MessageBus</code> instance for accessing the Bitrepositorys message bus. If a messagebus 
     * already exists for the collection ID defined in the settings, the existing instance is returned.
     * @param settings The settings to get the MessageBus 
     * @param securityManager The SecurityManager for the messagebus
     * @return The messagebus for this collection.
     */
    public MessageBus getMessageBus(Settings settings, SecurityManager securityManager) {
        return MessageBusManager.getMessageBus(settings, securityManager);
    }

    /**
     * Gets you an <code>FileExchange</code> instance for data communication. Is instantiated based on the 
     * configurations.
     * @param settings The settings for the file exchange.
     * @return The FileExchange according to the configuration.
     */
    public FileExchange getFileExchange(Settings settings) {
        if (fileExchange == null) {
            if((settings.getReferenceSettings().getFileExchangeSettings() != null )) {
                ProtocolType protocolType = settings.getReferenceSettings().getFileExchangeSettings().getProtocolType();
                if(protocolType == ProtocolType.HTTP) {
                    fileExchange = new HttpFileExchange(settings);
                } else if (protocolType == ProtocolType.HTTPS) {
                    fileExchange = new HttpsFileExchange(settings);
                } else if (protocolType == ProtocolType.FILE) {
                    fileExchange = new LocalFileExchange(
                            settings.getReferenceSettings().getFileExchangeSettings().getPath());
                }
            } else {
                fileExchange = new HttpFileExchange(settings);
            }
        }
        return fileExchange;
    }
}
