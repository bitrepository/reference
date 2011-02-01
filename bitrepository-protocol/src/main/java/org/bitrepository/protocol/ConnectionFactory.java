package org.bitrepository.protocol;

import java.util.Collection;
import java.util.Iterator;

import javax.jms.JMSException;

/**
 * Class for retrieving a connection to the bus.
 * @author jolf
 *
 */
public class ConnectionFactory {
	/** The collection of connection properties.*/
	private static Collection<ConnectionProperty> properties 
            = ConnectionProperty.getAllConnections();
	/** The next connection property to use.*/
	private static Iterator<ConnectionProperty> currentProperty
	        = properties.iterator();
	
	/**
	 * Method for retrieving the first connection for a messagebus.
	 * @return The first connection in the settings.
	 */
	public static MessageBusConnection getInstance() {
		// reinitialise the current connection.
		currentProperty = properties.iterator();
		if(!currentProperty.hasNext()) {
			throw new IllegalStateException("No connections found!");
		}
		return retrieveConnection(currentProperty.next());
	}
	
	/**
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
		if(!currentProperty.hasNext()) {
			//TODO: log a warning about reinitializing the iterator.
			currentProperty = properties.iterator();
		}
		return retrieveConnection(currentProperty.next());
	}

	/**
	 * Retrieves the connection for this instance.
	 * 
	 * TODO this function should be made generic by using a setting for 
	 * choosing which implementor of the MessageBusConnection interface to
	 * instantiate as connection.
	 * 
	 * @param p The property for the connection.
	 * @return The given connection.
	 */
	private static MessageBusConnection retrieveConnection(
			ConnectionProperty p) {
		try {
			return ActiveMQConnection.getInstance(p);
		} catch (JMSException e) {
			throw new IllegalStateException("Cannot instantiate connection for"
					+ " property: " + p, e);
		}
	}
}
