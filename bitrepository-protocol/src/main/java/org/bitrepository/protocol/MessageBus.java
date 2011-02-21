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
package org.bitrepository.protocol;

/**
 * The communication interface for the message bus in the bitrepositry protocol.
 *
 * TODO define a function for reconnecting to the message bus. Part of the
 * issue BITMAG-166
 *
 * @author jolf
 */
public interface MessageBus {
    /**
     * Adds the supplied listener to the indicated destination
     *
     * @param destinationId The destination to listen to
     * @param listener      The listener with should handle the messages
     *                      arriving on the destination
     * @throws Exception Something has gone wrong in the messaging
     */
    void addListener(String destinationId, MessageListener listener)
            throws Exception;

    /**
     * Removes the supplied listener from the indicated destination.
     *
     * @param destinationId The id for the destination, where the listener
     *                      should be removed.
     * @param listener      The listener to remove from the destination.
     * @throws Exception If something goes wrong with the connection.
     */
    void removeListener(String destinationId, MessageListener listener)
            throws Exception;

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @throws Exception If a problem with the connection to the Bus occurs
     *                   during the transportation of this message.
     */
    void sendMessage(String destinationId, String content) throws Exception;

}
