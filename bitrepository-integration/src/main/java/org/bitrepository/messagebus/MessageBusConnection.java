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
package dk.bitmagasin.messagebus;

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
public class MessageBusConnection {
	private final Log log = LogFactory.getLog(this.getClass());
	private static MessageBusConnection singleInstance;
    private Connection connection = null;
    private Session session = null;    

    public static final int ACKNOWLEDGE_MODE = Session.AUTO_ACKNOWLEDGE;
    public static final boolean TRANSACTED = true;
	
	private MessageBusConnection() throws JMSException {
		ActiveMQConnectionFactory connectionFactory =
	        new ActiveMQConnectionFactory(MessageBusSettings.getUser(),
	        		MessageBusSettings.getPassword(), MessageBusSettings.getUrl());	

	    connection = connectionFactory.createConnection();
        connection.setExceptionListener(new MessageBusExceptionListener());        
        connection.start();        

        session = connection.createSession(TRANSACTED, 
        		ACKNOWLEDGE_MODE);
	}
	
	/**
	 * Returns a <code>MessageBusConnection</code> instance. 
	 * @throws JMSException Throw in case of problems with the creation of the 
	 * connection to the message bus.
	 */
	public synchronized static MessageBusConnection getMessageBusConnection() 
	throws JMSException {
		if (singleInstance == null) {
			singleInstance = new MessageBusConnection();
		}
		return singleInstance;
	}
	
	/**
	 * Adds the supplied lister to the indicated tobic
	 * @param topicId The topic to listen to
	 * @param listener The listener with should handle the messages arriving on 
	 * the topic
	 * @throws JMSException Something has gone wrong in the JMS messaging
	 */
	public void addListener(String topicId, MessageListener listener) 
	throws JMSException {
		Topic topic = session.createTopic(topicId);
        session.createConsumer(topic).setMessageListener(listener);
	}
	
	public void addQueueMessageProducer(String queueId, MessageProducer producer) 
	throws JMSException {
		Queue queue = session.createQueue(queueId);
		producer = session.createProducer(queue);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
	}
    
    private class MessageBusExceptionListener implements ExceptionListener {
		@Override
		public void onException(JMSException arg0) {
			log.error(arg0);			
		}    	
    }
}
