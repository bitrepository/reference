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

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The place to get message buses. Only one message bus is created for each collection ID.
 */
public final class MessageBusManager {
    public static final String DEFAULT_MESSAGE_BUS = "Default message bus";
    private static final Logger log = LoggerFactory.getLogger(MessageBusManager.class);

    /**
     * Map of the loaded mediators for the different collectionsIDs.
     * The keys are the collectionID and the values are the message buses
     */
    private static final Map<String, MessageBus> messageBusMap = new HashMap<>();

    /**
     * Do not instantiate
     */
    private MessageBusManager() {
    }

    /**
     * @param settings        the message bus settings
     * @param securityManager the security manager
     * @return A the default message bus instance based on the supplied configuration. If the default message bus
     * doesn't already exist, it is created.
     */
    public synchronized static MessageBus getMessageBus(Settings settings, SecurityManager securityManager) {
        if (!messageBusMap.containsKey(DEFAULT_MESSAGE_BUS)) {
            MessageBus messageBus = createMessageBus(settings, securityManager);
            messageBusMap.put(DEFAULT_MESSAGE_BUS, messageBus);
            messageBus.setComponentFilter(List.of(settings.getComponentID()));
        }
        return messageBusMap.get(DEFAULT_MESSAGE_BUS);
    }

    /**
     * @return a message bus for the given collection if it exists, else null.
     */
    public synchronized static MessageBus getMessageBus() {
        return messageBusMap.get(DEFAULT_MESSAGE_BUS);
    }

    private static MessageBus createMessageBus(Settings settings, SecurityManager securityManager) {
        return new ActiveMQMessageBus(settings, securityManager);
    }

    /**
     * Can be used to inject a custom messageBus for a specific name.
     *
     * @param name       a specific name
     * @param messageBus The custom instance of the message bus.
     */
    public static void injectCustomMessageBus(String name, MessageBus messageBus) {
        messageBusMap.put(name, messageBus);
    }

    /**
     * Can be used to clear the current message buses, e.g. new message buses will be created on access.
     * <p/>
     * All message busses will be closed.
     */
    public static void clear() {
        for (MessageBus bus : messageBusMap.values()) {
            try {
                bus.close();
            } catch (JMSException e) {
                log.warn("Failed to close message bus {} during clear()", bus);
            }
        }
        messageBusMap.clear();
    }
}
