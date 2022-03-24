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
package org.bitrepository.alarm.handling;

import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Receives and distributes the messages to the handler.
 */
public class AlarmMediator implements MessageListener {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final List<AlarmHandler> handlers;
    private final MessageBus messageBus;
    private final String destination;

    /**
     * Sets the parameters of this mediator, and adds itself as a listener to the given destination.
     *
     * @param messageBus          The messageBus for communication.
     * @param listenerDestination The destination where the handler should be listening for alarms.
     */
    public AlarmMediator(MessageBus messageBus, String listenerDestination) {
        this.messageBus = messageBus;
        this.destination = listenerDestination;

        messageBus.addListener(listenerDestination, this, true);
        handlers = new ArrayList<>();
    }

    /**
     * Adds a given AlarmHandler to the handlers array.
     *
     * @param handler the handler
     */
    public void addHandler(AlarmHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void onMessage(Message msg, MessageContext messageContext) {
        if (msg instanceof AlarmMessage) {
            for (AlarmHandler handler : handlers) {
                handler.handleAlarm((AlarmMessage) msg);
            }
        } else {
            log.warn("Received unexpected message: '{}'", msg);
        }
    }

    /**
     * Closes the handlers respectively and remove this mediator from the destination.
     */
    public void close() {
        if (handlers != null) {
            for (AlarmHandler handler : handlers) {
                handler.close();
            }
            handlers.clear();
        }
        messageBus.removeListener(destination, this);
    }
}
