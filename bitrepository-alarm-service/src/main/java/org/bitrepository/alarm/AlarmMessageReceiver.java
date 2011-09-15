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
package org.bitrepository.alarm;

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Receives and distributes the messages to the handler.
 */
public class AlarmMessageReceiver extends AbstractMessageListener {

    /** The handler. Where the received messages should be handled.*/
    private final AlarmHandler handler;

    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the given destination.
     * @param messagebus The messagebus for communication.
     * @param listenerDestination The destination where the handler should be listening for alarms.
     * @param handler The method for handling alarms.
     */
    public AlarmMessageReceiver(MessageBus messagebus, String listenerDestination, AlarmHandler handler) {
        this.handler = handler;

        messagebus.addListener(listenerDestination, this);        
    }

    @Override
    public void onMessage(Alarm msg) {
        handler.handleAlarm(msg);
    }
    
    @Override
    protected void reportUnsupported(Object msg) {
        handler.handleOther(msg);
    }
}
