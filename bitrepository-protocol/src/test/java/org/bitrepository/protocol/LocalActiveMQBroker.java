package org.bitrepository.protocol;

import org.apache.activemq.broker.BrokerService;
import org.bitrepository.protocol.configuration.MessageBusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalActiveMQBroker {
	private Logger log = LoggerFactory.getLogger(getClass());

	private BrokerService broker;
	
	public LocalActiveMQBroker(MessageBusConfiguration configuration) throws Exception {
        broker = new BrokerService();
		broker.setBrokerName(configuration.getId());
        broker.addConnector(configuration.getUrl());
        log.info("Created embedded broker " + LoggerFactory.getLogger(getClass()));
	}
	
	public void start() throws Exception {
        broker.start();
	}
	
	public void stop() throws Exception {
			broker.stop();
			Thread.sleep(1000);
	}
}
