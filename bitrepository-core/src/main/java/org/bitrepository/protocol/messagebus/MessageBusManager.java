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
package org.bitrepository.protocol.messagebus;

import java.util.HashMap;
import java.util.Map;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.collectionsettings.MessageBusConfiguration;

/**
 * The place to get message buses. Only one message bus is created for each collection ID.
 */
public final class MessageBusManager {
    /**
     * Map of the loaded mediators for the different collectionsIDs.
     * The keys are the collectionID and the values are the message buses
     */
    private static final Map<String,MessageBus> messageBusMap = new HashMap<String,MessageBus>();
    
    /** Do not instantiate */
    private MessageBusManager() {}

    /**
     * @return a new message bus instance based on the supplied configuration.
     */
    public synchronized static MessageBus getMessageBus(Settings settings, SecurityManager securityManager) {
        String collectionID = settings.getCollectionID();
        if (!messageBusMap.containsKey(collectionID)) {
            MessageBus messageBus = createMessageBus(settings.getMessageBusConfiguration(), securityManager);
            messageBusMap.put(collectionID, messageBus);
        }
        return messageBusMap.get(collectionID);
    }

    /**
     * Returns a messagebus for the given collection if it exists, else null.
     * @param collectionID
     * @return
     */
    public synchronized static MessageBus getMessageBus(String collectionID) {
        return messageBusMap.get(collectionID);
    }

    private static MessageBus createMessageBus(MessageBusConfiguration settings, SecurityManager securityManager) {
        ActiveMQMessageBus messageBus = new ActiveMQMessageBus(settings, securityManager);
        return messageBus;
    }
}
