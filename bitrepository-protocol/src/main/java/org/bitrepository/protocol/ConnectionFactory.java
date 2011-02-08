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

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory contains a collection of connections. It is possible to iterate
 * through these connections and use which ever is wanted. Thus easily changing
 * the connection if the current connection dies.
 * It iterates through the collection in a loop, so if the last connection is 
 * reached and another is wanted, then the first connection is retrieved.
 * Also the issue of handling the different connections and knowing when to 
 * retrieve a new one is left with the clients of this factory.
 * 
 * TODO this function should be made generic by using a setting for 
 * choosing which implementor of the MessageBusConnection interface to
 * instantiate as connection. 
 * Currently only instantiating ActiveMQ connections.
 * 
 * @author jolf
 */
public class ConnectionFactory {
    private static Logger log = LoggerFactory.getLogger(
            ConnectionFactory.class);
    /** The collection of connection properties.*/
    private static Collection<ConnectionConfiguration> properties 
    = ConnectionConfiguration.getAllConfigurations();
    /** The next connection property to use.*/
    private static Iterator<ConnectionConfiguration> currentProperty
    = properties.iterator();

    /**
     * Method for retrieving the first connection for a messagebus.
     * @return The first connection in the settings.
     */
    public static MessageBusConnection getInstance() {
        // reinitialise the current connection.
        currentProperty = properties.iterator();
        log.debug("Loading default connection '" + currentProperty + "'.");
        synchronized(currentProperty) {
            if(!currentProperty.hasNext()) {
                throw new IllegalStateException("No connections found!");
            }
            return retrieveConnection(currentProperty.next());
        }
    }

    /**
     * Whether more connections are known.
     * @return Whether more connections are known.
     */
    public static boolean hasNextConnection() {
        return currentProperty.hasNext();
    }

    /**
     * Retrieve the next connection. If no more connections are known, then 
     * the first connection is retrieved again.
     * @return The next connection.
     */
    public static MessageBusConnection getNext() {
        synchronized(currentProperty) {
            if(!currentProperty.hasNext()) {
                // reinitialise the iterator, and log about it.
                currentProperty = properties.iterator();
                log.warn("No more connections found. Reinitializing '" 
                        + currentProperty + "'.");
            }
            return retrieveConnection(currentProperty.next());
        }
    }

    /**
     * Retrieves the connection for this instance.
     * 
     * TODO this function should be made generic by using a setting for 
     * choosing which implementor of the MessageBusConnection interface to
     * instantiate as connection. 
     * Currently only instantiating ActiveMQ connections.
     * 
     * @param p The property for the connection.
     * @return The given connection.
     * @throws IllegalStateException If the connection cannot be instantiated.
     */
    private static MessageBusConnection retrieveConnection(
            ConnectionConfiguration p) throws IllegalStateException {
        log.debug("Creating connection for instance '" + p + "'.");
        try {
            return ActiveMQConnection.getInstance(p);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot instantiate connection for"
                    + " property: " + p, e);
        }
    }
}
