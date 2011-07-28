/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AlarmClient.java 239 2011-07-22 13:51:09Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-alarm-client/src/main/java/org/bitrepository/alarm/AlarmClient.java $
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic alarm service
 */
public class BasicAlarmService implements AlarmService {
    /** The logger for the AlarmService.*/
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /** The messagebus for the communication.*/
    private final MessageBus messagebus;
    /** The conversation mediator to keep track of the conversations.*/
    private List<ConversationReceiverMediator<AbstractConversation<URL>>> mediator;

    /**
     * Constructor.
     */
    public BasicAlarmService(MessageBus bus) {
        messagebus = bus;
        mediator = new ArrayList<ConversationReceiverMediator<AbstractConversation<URL>>>();
    }

    @Override
    public void addHandler(AlarmHandler handler, String queue) {
        log.info("Adding handler '" + handler.getClass().getName() + "' for alarms on the queue '"
                + queue + "'.");
        mediator.add(new ConversationReceiverMediator<AbstractConversation<URL>>(messagebus, queue, handler));
    }
}
