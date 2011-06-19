package org.bitrepository.protocol.bus;

import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.configuration.MessageBusConfiguration;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;

/**
 * 
 * 
 * Consider moving definitions to disk
 */
public class MessageBusConfigurationFactory {

	private MessageBusConfigurationFactory() {}

	public static MessageBusConfigurations createDefaultConfiguration() {
		return ProtocolComponentFactory.getInstance().getProtocolConfiguration().getMessageBusConfigurations();
	}

	public static MessageBusConfigurations createEmbeddedMessageBusConfiguration() {
		MessageBusConfigurations configs2 = new MessageBusConfigurations();
		MessageBusConfiguration config2 = new MessageBusConfiguration();
		config2.setUrl("tcp://localhost:61616");
		config2.setId("Embedded-messagebus1");
		config2.setUsername("");
		config2.setPassword("");
		configs2.setPrimaryMessageBusConfiguration(config2);
		return configs2;
	}
}