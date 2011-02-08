/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id: MessageBusTest.java 49 2011-01-03 08:48:13Z mikis $
 * $HeadURL: https://gforge.statsbiblioteket.dk/svn/bitmagasin/trunk/bitrepository-integration/src/test/java/org/bitrepository/bus/MessageBusTest.java $
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for the configurations needed for a connection to a messagebus.
 * Contains the collection of the configurations for all the different 
 * messagebus connections.
 * 
 * TODO currently the URL is used as the unique identifier for the 
 * connections, but it would be better to use another identifier. 
 * To be defined in the settings.
 * 
 * TODO Change the current hardcoded variables used for the connection into
 * a map of configurations for the connection. This map should be generic, so
 * any type of configuration data needed could be placed within it.
 * 
 * @author jolf
 */
public class ConnectionConfiguration {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(
            ConnectionConfiguration.class);
    /** Initialise all the different connection configurations.*/
    private static Map<String, ConnectionConfiguration> 
            connectionConfigurationMap = Collections.synchronizedMap(
                    new HashMap<String, ConnectionConfiguration>());

    /** Loads all the connection configurations from settings.*/
    static {
        initialise();
    }

    /** 
     * Load the settings and initialise all the configuration.
     */
    protected static void initialise() {
        log.debug("Initialising the connection configurations.");
        // TODO load from settings.
        log.warn("Load from settings instead of using these hardcoded values!");

        String user = null;
        String password = null;
        String url1 = "failover://tcp://sandkasse-01.kb.dk:61616";
        String url2 = "failover://tcp://localhost:61616";

        connectionConfigurationMap.put(url1, new ConnectionConfiguration(url1, user, password));
        connectionConfigurationMap.put(url2, new ConnectionConfiguration(url2, user, password));
    }

    /**
     * Retrieve the connection for a specific url.
     * 
     * @param connectionId The identifier for the specific connection.
     * @return The properties for this connection.
     * @throws IllegalStateException If no configuration corresponds to the 
     * given connectionId.
     */
    public static ConnectionConfiguration getConfiguration(String connectionId) 
            throws IllegalStateException {
        log.debug("Retrieving configuration for id '" + connectionId + "'.");
        if(connectionConfigurationMap.containsKey(connectionId)) {
            return connectionConfigurationMap.get(connectionId);
        }
        throw new IllegalStateException("No configuration found for the id '"
                + connectionId + "'.");
    }

    /**
     * The collection of connection configurations.
     * @return The collection of connection configurations.
     */
    public static Collection<ConnectionConfiguration> getAllConfigurations() {
        return connectionConfigurationMap.values();
    }

    /** The URL for the connection of the messagebus.*/
    private String url;
    /** The username for connecting to the messagebus.*/
    private String username;
    /** The password for connection to the messagebus.*/
    private String password;
    /** The unique identifier for this connection.*/
    private String id;

    /**
     * Creates an instance of ConnectionProperty.
     * @param url The url for this connection.
     * @param username The username for this connection.
     * @param password The password for this connection.
     * @throws IllegalArgumentException If the url is not valid (either null 
     * or the empty string).
     */
    private ConnectionConfiguration(String url, String username, String 
            password) throws IllegalArgumentException {
        // check arguments
        if(url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Invalid value for variable "
                    + "url: '" + url + "'.");
        }
        // TODO: check username? As default it is null!
        // TODO: check password? As default it is null!
        this.url = url;
        this.username = username;
        this.password = password;
        this.id = url;
    }

    /** 
     * The url for the connection to the messagebus.
     * @return The url for the connection to the messagebus.
     */
    public String getUrl() {
        return url;
    }

    /** 
     * The username for connecting to the messagebus.
     * @return The username for connecting to the messagebus.
     */
    public String getUsername() {
        return username;
    }

    /** 
     * The password for connecting to the messagebus.
     * @return The password for connecting to the messagebus.
     */
    public String getPassword() {
        return password;
    }

    /**
     * The unique id for this connection.
     * @return The unique id for this connection.
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return super.toString() + ", with values: URL = " + getUrl() 
        + ", username = " + getUsername() + ", (secret password)";
    }
}
