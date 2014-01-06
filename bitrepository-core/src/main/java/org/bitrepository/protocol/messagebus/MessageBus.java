/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.protocol.messagebus;

import javax.jms.JMSException;
import java.util.List;


/**
 * The communication interface for the message bus.
 */
public interface MessageBus extends MessageSender {
    /**
     * Adds the supplied listener to the indicated destination (non-durable).
     *
     * @param destinationId The destination to listen to
     * @param listener      The listener with should handle the messages
     *                      arriving on the destination
     */
    void addListener(String destinationId, MessageListener listener);

    /**
     * Adds the supplied listener to the indicated destination
     *
     * @param destinationID The destination to listen to
     * @param listener      The listener with should handle the messages
     *                      arriving on the destination
     * @param durable       Indicates whether the lister should use a durable subscriber. Only allowed for topics and
     *                      only relevant if the consumer needs to be created.
     */
    void addListener(String destinationID, MessageListener listener, boolean durable);

    /**
     * Removes the supplied listener from the indicated destination.
     *
     * @param destinationId The id for the destination, where the listener
     *                      should be removed.
     * @param listener      The listener to remove from the destination.
     */
    void removeListener(String destinationId, MessageListener listener);

    /**
     * Closes the messagebus connection so that everything can be shutdown nicely.  
     */
    void close() throws JMSException;

    /**
     * @param componentIDs Defines the list of componentIDs with receiver component ID relevant for this messagebus instance. If
     * the list contains any elements, the receiverID for incoming messages are read before being parsed. This enables
     * the message bus to discard messages prior to parsing, if the message is meant for other components.
     * <p>
     * Messages will only be discarded if the componentFilter contains at least one componentID and the received
     * message has a defined receiver.
     * </p>
     *
     */
    void setComponentFilter(List<String> componentIDs);

    /**
     * @param collectionIDs If defined specifies the list of collectionIDs with should be handled. If
     * the list contains any elements, the collectionID for incoming messages are read before being parsed. This
     * enables the message bus to discard messages prior to parsing, if the message is meant for other collections.
     * <p>
     * Messages will only be discarded if the collection contains at least one collectionsID and the received
     * message has a defined collectionID.
     * </p>
     *
     */
    void setCollectionFilter(List<String> collectionIDs);
}
