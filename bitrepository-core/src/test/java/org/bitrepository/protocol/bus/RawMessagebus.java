/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

package org.bitrepository.protocol.bus;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.protocol.CoordinationLayerException;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;

public class RawMessagebus {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<String, Destination> destinations = new HashMap<>();
    private final Map<String, MessageConsumer> consumers = new HashMap<>();
    private final Session producerSession;
    private final Session consumerSession;
    private final Connection connection;
    public static final boolean TRANSACTED = false;
    private final SecurityManager securityManager;

    public RawMessagebus(MessageBusConfiguration messageBusConfiguration, SecurityManager securityManager) {
        this.securityManager = securityManager;

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(messageBusConfiguration.getURL());
        try {
            connection = connectionFactory.createConnection();
            connection.setExceptionListener(new MessageBusExceptionListener());

            producerSession = connection.createSession(TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            consumerSession = connection.createSession(TRANSACTED, Session.AUTO_ACKNOWLEDGE);

            connection.start();
        } catch (JMSException e) {
            throw new CoordinationLayerException("Unable to initialise connection to message bus", e);
        }
    }

    public void addHeader(Message msg,
                          String messageClass,
                          String replyTo,
                          String collectionID,
                          String correlationID) throws JMSException {

        msg.setStringProperty(ActiveMQMessageBus.MESSAGE_TYPE_KEY, messageClass);
        msg.setStringProperty(ActiveMQMessageBus.COLLECTION_ID_KEY, collectionID);
        msg.setJMSCorrelationID(correlationID);
        msg.setJMSReplyTo(getDestination(replyTo, producerSession));
    }

    public Message createMessage(org.bitrepository.bitrepositorymessages.Message message) throws JMSException {
        JaxbHelper jaxbHelper = new JaxbHelper("xsd/", "BitRepositoryMessages.xsd");
        String xmlContent;
        try {
            xmlContent = jaxbHelper.serializeToXml(message);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return producerSession.createTextMessage(xmlContent);
    }

    public void sendMessage(String destinationID, Message msg) throws JMSException {
        getProducer(destinationID).send(msg);
    }

    /**
     * Method for retrieving the message producer for a specific queue.
     *
     * @param destinationID The id for the destination.
     * @return The message producer for this destination.
     */
    public MessageProducer getProducer(String destinationID) throws JMSException {
        Destination destination = getDestination(destinationID, producerSession);
        return producerSession.createProducer(destination);
    }

    private Destination getDestination(String destinationID, Session session) {
        Destination destination = destinations.get(destinationID);
        if (destination == null) {
            try {

                String[] parts = destinationID.split("://");
                if (parts.length == 1) {
                    destination = session.createTopic(destinationID);
                } else if (parts.length == 2) {
                    if (parts[0].equals("topic")) {
                        destination = session.createTopic(parts[1]);
                    } else if (parts[0].equals("queue")) {
                        destination = session.createQueue(parts[1]);
                    } else if (parts[0].equals("temporary-queue")) {
                        destination = session.createTemporaryQueue();
                    } else if (parts[0].equals("temporary-topic")) {
                        destination = session.createTemporaryTopic();
                    } else {
                        throw new CoordinationLayerException("Unable to create destination '" +
                                destination + "'. Unknown type.");
                    }
                }
            } catch (JMSException e) {
                throw new CoordinationLayerException("Could not create destination '" + destinationID + "'", e);
            }
            destinations.put(destinationID, destination);
        }
        return destination;
    }

    public synchronized void addListener(String destinationID, javax.jms.MessageListener listener) throws JMSException {
        MessageConsumer consumer = getMessageConsumer(destinationID, listener);
        try {
            consumer.setMessageListener(listener);
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Unable to add listener '" + listener + "' to destinationID '" + destinationID + "'", e);
        }
    }

    private MessageConsumer getMessageConsumer(String destinationID, javax.jms.MessageListener listener) throws JMSException {
        String key = destinationID + "#" + listener.hashCode();
        if (!consumers.containsKey(key)) {
            Destination destination = getDestination(destinationID, consumerSession);
            MessageConsumer consumer = consumerSession.createConsumer(destination);
            consumers.put(key, consumer);
        }
        return consumers.get(key);
    }

    private class MessageBusExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException arg0) {
            log.error("JMSException caught: ", arg0);
        }
    }

}
