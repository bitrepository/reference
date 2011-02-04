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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains the basic functionality for connection and communicating with the 
 * coordination layer.
 * 
 * TODO add retries for whenever a JMS exception is thrown. Currently it is 
 * very unstable to connection issues.
 */
public class ActiveMQConnection implements MessageBusConnection {
	/** The connections on ActiveMQ buses.*/
	private static Map<String, ActiveMQConnection> instances = 
		Collections.synchronizedMap(new HashMap<String, ActiveMQConnection>());
	
	/** The default acknowledge mode.*/
    public static final int ACKNOWLEDGE_MODE = Session.AUTO_ACKNOWLEDGE;
    /** Default transacted.*/
    public static final boolean TRANSACTED = true;
	
	/**
	 * Returns a <code>MessageBusConnection</code> instance. 
	 * @param property The connection property for this connection.
	 * @throws JMSException Throw in case of problems with the creation of the 
	 * connection to the message bus.
	 */
	public synchronized static ActiveMQConnection getInstance(
			ConnectionProperty property) throws JMSException {
		if(!instances.containsKey(property.getId())) {
			instances.put(property.getId(), new ActiveMQConnection(property));
		}
		return instances.get(property.getId());
	}
	
	/** The variable to separate the parts of the consumer key.*/
	private static final String CONSUMER_KEY_SEPARATOR = "#";
	
	/** The Log.*/
	private final Log log = LogFactory.getLog(this.getClass());
	/** The connection.*/
    private Connection connection = null;
    /** The session.*/
    private Session session = null;
    /** Map of the consumers. */
    private Map<String, MessageConsumer> consumers 
            = Collections.synchronizedMap(new HashMap<String, 
            		MessageConsumer>());
    
    /**
     * Constructor. Creates a connection based on the given properties.
     * 
     * @param property The properties for the connection.
     * @throws JMSException If problems happen during the connection.
     */
    private ActiveMQConnection(ConnectionProperty property) throws JMSException {
    	log.debug("Initializing ActiveMQConnection to '" + property + "'.");
    	
    	// Retrieve factory for connection
    	ActiveMQConnectionFactory connectionFactory =
    		new ActiveMQConnectionFactory(property.getUsername(),
    				property.getPassword(), property.getUrl());	

    	// create and start the connection
    	connection = connectionFactory.createConnection();
    	
    	connection.setExceptionListener(new MessageBusExceptionListener());        
    	connection.start();

    	session = connection.createSession(TRANSACTED, ACKNOWLEDGE_MODE);
    }
	
	@Override
	public void addListener(String topicId, MessageListener listener) 
	        throws JMSException {
		MessageConsumer consumer = getMessageConsumer(topicId, listener);
		consumer.setMessageListener(listener);
	}
	
	@Override
	public void removeListener(String topicId, MessageListener listener)
	        throws JMSException {
		MessageConsumer consumer = getMessageConsumer(topicId, listener);
		consumer.close();
		consumers.remove(getConsumerKey(topicId, listener));
	}
	
	@Override
	public void sendMessage(String topicId, String content)
            throws JMSException {
		MessageProducer producer = addQueueMessageProducer(topicId);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		Message msg = session.createTextMessage(content);
		msg.setJMSType("MyMessage");
		msg.setJMSReplyTo(session.createQueue(topicId));
		producer.send(msg);
		session.commit();
	}
	
	/**
	 * Retrieves a consumer for the specific topic id and message listener.
	 * If no such consumer already exists, then it is created.
	 * @param topicId The id of the topic to consume messages from.
	 * @param listener The listener to consume the messages.
	 * @return The instance for consuming the messages.
	 */
	private MessageConsumer getMessageConsumer(String topicId, 
			MessageListener listener) throws JMSException {
		String key = getConsumerKey(topicId, listener);
		if(!consumers.containsKey(key)) {
			Topic topic = session.createTopic(topicId);
	        MessageConsumer consumer = session.createConsumer(topic);
	        consumers.put(key, consumer);
		}
		return consumers.get(key);
	}
	
	/**
	 * Creates a unique key for the message listener and the topic id.
	 * @param topicId The id for the topic.
	 * @param listener The message listener.
	 * @return The key for the message listener and the topic id.
	 */
	private String getConsumerKey(String topicId, MessageListener listener) {
		return topicId + CONSUMER_KEY_SEPARATOR + listener.hashCode();
	}
	
	/**
	 * Method for retrieving the message producer for a specific queue.
	 * @param queueId The id for the queue.
	 * @return The message producer for this queue.
	 * @throws JMSException If the producer for the queue cannot be established.
	 */
	private MessageProducer addQueueMessageProducer(String topicId) 
	        throws JMSException {
		Topic topic = session.createTopic(topicId);
		MessageProducer producer = session.createProducer(topic);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		return producer;
	}
	
	/**
	 * Class for handling the message bus exceptions.
	 */
    private class MessageBusExceptionListener implements ExceptionListener {
		@Override
		public void onException(JMSException arg0) {
			log.error(arg0);			
		}    	
    }
}
