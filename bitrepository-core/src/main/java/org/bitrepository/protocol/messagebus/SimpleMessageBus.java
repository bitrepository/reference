package org.bitrepository.protocol.messagebus;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.JMSException;
import java.util.*;

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
