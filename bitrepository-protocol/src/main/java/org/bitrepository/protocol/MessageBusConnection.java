/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id: MessageBusConnection.java 49 2011-01-03 08:48:13Z mikis $
 * $HeadURL: https://gforge.statsbiblioteket.dk/svn/bitmagasin/trunk/bitrepository-integration/src/main/java/org/bitrepository/messagebus/MessageBusConnection.java $
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol;

import javax.jms.MessageListener;

/**
 * The communication interface for the message bus.
 *  
 *  TODO define a function for reconnecting to the message bus. Part of the
 *  issue BITMAG-166
 *  TODO handle the jms message listener issue. We should not be depending on
 *  the JMS technology.
 *  
 * @author jolf
 */
public interface MessageBusConnection {
    /**
     * Adds the supplied listener to the indicated topic
     * @param topicId The topic to listen to
     * @param listener The listener with should handle the messages arriving on 
     * the topic
     * @throws JMSException Something has gone wrong in the JMS messaging
     */
    void addListener(String topicId, MessageListener listener) 
    throws Exception;

    /**
     * Removes the supplied listener from the indicated topic.
     * @param topidId The id for the topic, where the listener should be 
     * removed.
     * @param listener The listener to remove from the topic.
     * @throws JMSException If something goes wrong with the JMS-connection.
     */
    void removeListener(String topidId, MessageListener listener) 
    throws Exception;

    /**
     * Method for sending a message on a specific queue/topic.
     * 
     * @param queueId The id for the queue/topic to send message.
     * @param content The content of the message.
     * @throws JMSException If a problem with the connection to the Bus occurs 
     * during the transportation of this message.
     */
    void sendMessage(String queueId, String content)
    throws Exception;

}
