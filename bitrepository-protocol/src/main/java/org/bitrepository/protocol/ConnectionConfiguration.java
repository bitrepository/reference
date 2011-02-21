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
 * Container for the configurations needed for a connection to a messagebus.
 *
 * TODO Change the current hardcoded variables used for the connection into
 * a map of configurations for the connection. This map should be generic, so
 * any type of configuration data needed could be placed within it.
 *
 * @author jolf
 */
public class ConnectionConfiguration {
    /** The URL for the connection of the messagebus. */
    private String url;
    /** The username for connecting to the messagebus. */
    private String username;
    /** The password for connection to the messagebus. */
    private String password;
    /** The unique identifier for this connection. */
    private String id;

    /**
     * Creates an instance of ConnectionProperty.
     *
     * @param url      The url for this connection.
     * @param username The username for this connection.
     * @param password The password for this connection.
     */
    public ConnectionConfiguration(String url, String username,
                                    String password) {
        // check arguments
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid value for variable " + "url: '" + url + "'.");
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
     *
     * @return The url for the connection to the messagebus.
     */
    public final String getUrl() {
        return url;
    }

    /**
     * The username for connecting to the messagebus.
     *
     * @return The username for connecting to the messagebus.
     */
    public final String getUsername() {
        return username;
    }

    /**
     * The password for connecting to the messagebus.
     *
     * @return The password for connecting to the messagebus.
     */
    public final String getPassword() {
        return password;
    }

    /**
     * The unique id for this connection.
     *
     * @return The unique id for this connection.
     */
    public final String getId() {
        return id;
    }

    @Override
    public final String toString() {
        return super.toString() + ", with values: URL = " + getUrl()
                + ", username = " + getUsername() + ", (secret password)";
    }
}
