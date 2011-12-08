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

/**
 * The place to get message buses. Only one message bus is created for each collection ID.
 */
public final class MessageBusManager {
    /** Map of the loaded mediators */
    private static final Map<String,MessageBus> messageBusMap = new HashMap<String,MessageBus>();
    
    /** Do not instantiate */
    private MessageBusManager() {}

    /**
     * Returns a new message bus instance based on the supplied configuration
     * @param configuration
     * @return
     */
    public static MessageBus getMessageBus(Settings settings) {
        String collectionID = settings.getCollectionID();
        if (!messageBusMap.containsKey(collectionID)) {
            MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration());
            messageBusMap.put(collectionID, messageBus);
        }
        return messageBusMap.get(collectionID);
    }
}
