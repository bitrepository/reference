package org.bitrepository.protocol;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;

public interface MessageBusConnection {
	/**
	 * The retrieval of a bus connection instance.
	 * @param property The properties for the connection.
	 * @return The connection for the given connection property.
	 */
//	MessageBusConnection getInstance(ConnectionProperty property);
	
	/**
	 * Adds the supplied listener to the indicated topic
	 * @param topicId The topic to listen to
	 * @param listener The listener with should handle the messages arriving on 
	 * the topic
	 * @throws JMSException Something has gone wrong in the JMS messaging
	 */
    void addListener(String topicId, MessageListener listener) 
	        throws JMSException;
	
	/**
	 * Adds the supplied listener to the indicated queue
	 * @param queueId The queue to listen to
	 * @param listener The listener with should handle the messages arriving on 
	 * the queue
	 * @throws JMSException Something has gone wrong in the JMS messaging
	 */
    void addQueueMessageProducer(String queueId, MessageProducer producer) 
	        throws JMSException;
	
}
