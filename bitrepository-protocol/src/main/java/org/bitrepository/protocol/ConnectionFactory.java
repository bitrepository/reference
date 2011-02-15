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

import org.bitrepository.protocol.activemq.ActiveMQConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 * TODO currently the URL is used as the unique identifier for the
 * connections, but it would be better to use another identifier.
 * To be defined in the settings.
 *
 * @author jolf
 */
public final class ConnectionFactory {
    /** Log for this class. */
    private static final Logger LOG = LoggerFactory
            .getLogger(ConnectionFactory.class);

    /** A mapping from connection IDs to configurations. */
    private static final Map<String, ConnectionConfiguration>
            CONNECTION_CONFIGURATION_MAP = Collections
            .synchronizedMap(new HashMap<String, ConnectionConfiguration>());

    /* Load all the connection configurations from settings.*/
    static {
        LOG.debug("Initialising the connection configurations.");
        // TODO load from settings.
        LOG.warn("Load from settings instead of using these hardcoded values!");

        String user = null;
        String password = null;
        String url1 = "failover://tcp://sandkasse-01.kb.dk:61616";
        String url2 = "failover://tcp://localhost:61616";

        CONNECTION_CONFIGURATION_MAP
                .put(url1, new ConnectionConfiguration(url1, user, password));
        CONNECTION_CONFIGURATION_MAP
                .put(url2, new ConnectionConfiguration(url2, user, password));
    }

    /** The next connection property to use. */
    private static Iterator<ConnectionConfiguration> currentConfiguration
            = getAllConfigurations().iterator();

    /** Utility class. No public constructor. */
    private ConnectionFactory() {}

    /**
     * Method for retrieving the first connection for a message bus. This will
     * also reset the values for the {@link #hasNextConnection()} and
     * {@link #getNextConnection()} methods.
     *
     * @return The first connection in the settings.
     */
    public static MessageBusConnection getInstance() {
        // reinitialise the current connection.
        currentConfiguration = getAllConfigurations().iterator();
        LOG.debug("Loading default connection '" + currentConfiguration + "'.");
        synchronized (CONNECTION_CONFIGURATION_MAP) {
            if (!currentConfiguration.hasNext()) {
                throw new IllegalStateException("No connections found!");
            }
            return retrieveConnection(currentConfiguration.next());
        }
    }

    /**
     * Whether more connections are known. Note, you can still use
     * {@link #getNextConnection()} even if this reports false. In that case the
     * first connection is returned again.
     *
     * @return Whether more connections are known.
     */
    public static boolean hasNextConnection() {
        return currentConfiguration.hasNext();
    }

    /**
     * Retrieve the next connection. If no more connections are known, then
     * the first connection is retrieved again.
     *
     * @return The next connection.
     */
    public static MessageBusConnection getNextConnection() {
        synchronized (currentConfiguration) {
            if (!currentConfiguration.hasNext()) {
                // reinitialise the iterator, and log about it.
                currentConfiguration = getAllConfigurations().iterator();
                LOG.warn("No more connections found. Reinitializing '"
                                 + currentConfiguration + "'.");
            }
            return retrieveConnection(currentConfiguration.next());
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
     *
     * @throws IllegalStateException If the connection cannot be instantiated.
     */
    private static MessageBusConnection retrieveConnection(
            ConnectionConfiguration p) {
        LOG.debug("Creating connection for instance '" + p + "'.");
        try {
            return ActiveMQConnection.getInstance(p);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot instantiate connection for" + " property: " + p, e);
        }
    }

    /**
     * Retrieve the connection for a specific id.
     *
     * @param connectionId The identifier for the specific connection.
     * @return The properties for this connection.
     *
     * @throws IllegalStateException If no configuration corresponds to the
     *                               given connectionId.
     */
    public static ConnectionConfiguration getConfiguration(String connectionId) {
        LOG.debug("Retrieving configuration for id '" + connectionId + "'.");
        if (CONNECTION_CONFIGURATION_MAP.containsKey(connectionId)) {
            return CONNECTION_CONFIGURATION_MAP.get(connectionId);
        }
        throw new IllegalStateException(
                "No configuration found for the id '" + connectionId + "'.");
    }

    /**
     * The collection of connection configurations.
     *
     * @return The collection of connection configurations.
     */
    public static Collection<ConnectionConfiguration> getAllConfigurations() {
        return CONNECTION_CONFIGURATION_MAP.values();
    }

}
