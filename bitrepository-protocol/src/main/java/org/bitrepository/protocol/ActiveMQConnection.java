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
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains the basic functionality for connection and communicating with the 
 * coordination layer
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
	
	/** The Log.*/
	private final Log log = LogFactory.getLog(this.getClass());
	/** The connection.*/
    private Connection connection = null;
    /** The session.*/
    private Session session = null;
    
    /**
     * Constructor. Creates a connection based on the given properties.
     * 
     * @param property The properties for the connection.
     * @throws JMSException If problems happen during the connection.
     */
    private ActiveMQConnection(ConnectionProperty property) throws JMSException {
    	ActiveMQConnectionFactory connectionFactory =
    		new ActiveMQConnectionFactory(property.getUsername(),
    				property.getPassword(), property.getUrl());	

    	connection = connectionFactory.createConnection();
    	connection.setExceptionListener(new MessageBusExceptionListener());        
    	connection.start();

    	session = connection.createSession(TRANSACTED, 
    			ACKNOWLEDGE_MODE);
    }
	
	@Override
	public void addListener(String topicId, MessageListener listener) 
	        throws JMSException {
		Topic topic = session.createTopic(topicId);
        session.createConsumer(topic).setMessageListener(listener);
	}
	
	@Override
	public void addQueueMessageProducer(String queueId, MessageProducer producer) 
	        throws JMSException {
		Queue queue = session.createQueue(queueId);
		producer = session.createProducer(queue);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
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
