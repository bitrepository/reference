/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

public class SimpleMessageBus implements MessageBus {
    Map<String,Set<MessageListener>> listeners = new HashMap<>();
    private final Set<String> componentFilter = new HashSet<String>();
    private final Set<String> collectionFilter = new HashSet<String>();

    @Override
    public void addListener(String destinationId, MessageListener listener) {
        getListeners(destinationId).add(listener);
    }

    @Override
    public void addListener(String destinationID, MessageListener listener, boolean durable) {
        addListener(destinationID, listener);
    }

    @Override
    public void removeListener(String destinationId, MessageListener listener) {
        getListeners(destinationId).remove(listener);
    }

    @Override
    public void close() throws JMSException {}

    @Override
    public void setComponentFilter(List<String> componentIDs) {
        componentFilter.clear();
        componentFilter.addAll(componentIDs);
    }

    @Override
    public void setCollectionFilter(List<String> collectionIDs) {
        collectionFilter.clear();
        collectionFilter.addAll(collectionIDs);
    }

    @Override
    public void sendMessage(Message content) {
        if (filterMessage(content)) {
            getListeners(content.getDestination()).forEach(listener -> listener.onMessage(content, new MessageContext(null)));
        }
    }

    private Set<MessageListener> getListeners(String destinationId) {
        if (!listeners.containsKey(destinationId)) {
            listeners.put(destinationId, new HashSet<>());
        }
        return listeners.get(destinationId);
    }

    private boolean filterMessage(Message message) {
        if(!componentFilter.isEmpty()) {
            if (message.getTo() != null && !componentFilter.contains(message.getTo())) {
                return false;
            }
        }
        if(!collectionFilter.isEmpty()) {
            if (message.getCollectionID() != null && !collectionFilter.contains(message.getCollectionID())) {
                return false;
            }
        }
        return true;
    }
}
