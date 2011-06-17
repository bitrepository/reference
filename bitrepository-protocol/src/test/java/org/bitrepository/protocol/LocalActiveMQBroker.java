package org.bitrepository.protocol;

import org.apache.activemq.broker.BrokerService;

public class LocalActiveMQBroker {

	private BrokerService broker;
	
	public LocalActiveMQBroker() {
        broker = new BrokerService();
	}
	
	public void init() throws Exception {
        broker.addConnector("tcp://localhost:61616");
        broker.start();
	}
	
	public void close() throws Exception {
		if(broker != null) {
			broker.stop();
			broker = null;
		}
	}
}
