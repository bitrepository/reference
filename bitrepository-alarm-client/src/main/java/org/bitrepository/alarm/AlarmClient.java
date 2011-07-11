/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AccessComponentFactory.java 212 2011-07-05 10:04:10Z bam $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/AccessComponentFactory.java $
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

import org.bitrepository.alarm_client.alarmclientconfiguration.AlarmConfiguration;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * The alarm client.
 */
public class AlarmClient {
    /** The messagebus for the communication.*/
    private final MessageBus messagebus;
    /** The instance for handling the alarms.*/
    private final AlarmHandler alarmHandler;
    /** The configuration for the Alarm.*/
    private final AlarmConfiguration configuration;
    /** The conversation mediator to keep track of the conversations.*/
    private final ConversationMediator<AlarmConversation> mediator;

    /**
     * Constructor.
     */
    public AlarmClient(MessageBus bus, AlarmHandler handler, AlarmConfiguration config) {
        messagebus = bus;
        alarmHandler = handler;
        configuration = config;
        mediator = new ConversationReceiverMediator<AlarmConversation>(bus, config.getQueue(), handler);
    }
}
