package org.bitrepository.protocol.messagebus;

import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;

/**
 * The place to create message buses.
 */
public class MessageBusFactory {
	/** Do not instantiate */
	private MessageBusFactory() {}
	
	/**
	 * Returns a new message bus instance based on the supplied configuration
	 * @param configuration
	 * @return
	 */
	public static MessageBus createMessageBus(MessageBusConfigurations configuration) {
		return new ActiveMQMessageBus(configuration);
	}
}
