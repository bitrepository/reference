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

import org.apache.activemq.ActiveMQConnection;

/**
 * Container for the properties needed for a connection to a messagebus.
 * Contains the collection of the properties for all the different connections.
 * 
 * TODO: currently the URL is used as the unique identifier for the 
 * connections, but it would be better to use another identifier, e.g. the SLA.
 * 
 * @author jolf
 *
 */
public class ConnectionProperty {
	/** Initialise all the different connection properties.*/
	private static Map<String, ConnectionProperty> properties = 
		Collections.synchronizedMap(new HashMap<String, ConnectionProperty>());
	
	/** Loads all the connection properties from settings.*/
	static {
		initialise();
	}
	
	/** 
	 * Load the settings and initialise all the properties.
	 */
	protected static void initialise() {
		// TODO load from settings.
        String user = ActiveMQConnection.DEFAULT_USER;
        String password = ActiveMQConnection.DEFAULT_PASSWORD;
        String url1 = "failover://tcp://sandkasse-01.kb.dk:61616";
        String url2 = "failover://tcp://localhost:61616";

		properties.put(url1, new ConnectionProperty(url1, user, password));
		properties.put(url2, new ConnectionProperty(url2, user, password));
	}
	
	/**
	 * Retrieve the connection for a specific url.
	 * 
	 * @param url The url for the specific connection.
	 * @return The properties for this connection.
	 */
	public static ConnectionProperty getProperty(String id) {
		if(properties.containsKey(id)) {
			return properties.get(id);
		}
		// TODO: handle this case!
		return null;
	}
	
	/**
	 * @return The collection of connections.
	 */
	public static Collection<ConnectionProperty> getAllConnections() {
		return properties.values();
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
	 */
	private ConnectionProperty(String url, String username, String password) {
		// check arguments
		if(url == null || url.isEmpty()) {
			throw new IllegalArgumentException("Invalid value for variable "
					+ "'url'");
		}
		// TODO: check username? As default it is null!
		// TODO: check password? As default it is null!
		this.url = url;
		this.username = username;
		this.password = password;
		this.id = url;
	}
	
	/** 
	 * @return The url for the connection to the messagebus.
	 */
	public String getUrl() {
		return url;
	}
	
	/** 
	 * @return The username for connecting to the messagebus.
	 */
	public String getUsername() {
		return username;
	}
	
	/** 
	 * @return The password for connecting to the messagebus.
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * @return the unique id for this connection.
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
