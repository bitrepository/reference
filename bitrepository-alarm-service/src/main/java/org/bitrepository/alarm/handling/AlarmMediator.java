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

import java.util.ArrayList;
import java.util.List;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives and distributes the messages to the handler.
 */
public class AlarmMediator implements MessageListener {
    /** The logger.*/
    private Logger log = LoggerFactory.getLogger(this.getClass());
    /** The handler. Where the received messages should be handled.*/
    private List<AlarmHandler> handlers;
    /** The messagebus for this mediator.*/
    private final MessageBus messageBus;
    /** The destination to listen to.*/ 
    private final String destination;

    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the given destination.
     * @param messageBus The messagebus for communication.
     * @param listenerDestination The destination where the handler should be listening for alarms.
     */
    public AlarmMediator(MessageBus messageBus, String listenerDestination) {
        this.messageBus = messageBus;
        this.destination = listenerDestination;
        
        messageBus.addListener(listenerDestination, this, true);
        handlers = new ArrayList<AlarmHandler>();
    }
    
    /**
     * Adds a given alarm handler to handle the .
     * @param handler
     */
    public void addHandler(AlarmHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void onMessage(Message msg) {
        if(msg instanceof AlarmMessage) {
            for(AlarmHandler handler : handlers) {
                handler.handleAlarm((AlarmMessage) msg);
            }
        } else {
            log.warn("Recieved unexpected message: '{}'", msg);
        }
    }
    
    /**
     * Close the handlers respectively and remove this mediator from the destination.
     */
    public void close() {
        if(handlers != null) {
            for(AlarmHandler handler : handlers) {
                handler.close();
            }
            handlers.clear();
        }
        messageBus.removeListener(destination, this);
    }
}
