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
package org.bitrepository.protocol.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorymessages.GetChecksumsComplete;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.protocol.CoordinationLayerException;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.MessageListener;
import org.bitrepository.protocol.configuration.MessageBusConfiguration;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the basic functionality for connection and communicating with the
 * coordination layer over JMS through active MQ.
 *
 * TODO add retries for whenever a JMS exception is thrown. Currently it is
 * very unstable to connection issues.
 *
 * TODO currently creates only topics.
 */
public class ActiveMQMessageBus implements MessageBus {
    /** The Log. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The key for storing the message type in a string property in the message headers. */
    public static final String MESSAGE_TYPE_KEY = "org.bitrepository.messages.type";
    /** The default acknowledge mode. */
    public static final int ACKNOWLEDGE_MODE = Session.AUTO_ACKNOWLEDGE;
    /** Default transacted. */
    public static final boolean TRANSACTED = true;

    /** The variable to separate the parts of the consumer key. */
    private static final String CONSUMER_KEY_SEPARATOR = "#";

    /** The session. */
    private final Session session;

    /**
     * Map of the consumers, mapping from a hash of "destinations and listener" to consumer.
     * Used to identify if a listener is already registered.
     */
    private final Map<String, MessageConsumer> consumers = Collections
            .synchronizedMap(new HashMap<String, MessageConsumer>());
    /** Map of topics, mapping from ID to topic. */
    private final Map<String, Topic> topics = new HashMap<String, Topic>();
    /** The configuration for the connection to the activeMQ. */
    private final MessageBusConfiguration configuration;

    /**
     * Use the {@link org.bitrepository.protocol.ProtocolComponentFactory} to get a handle on a instance of
     * MessageBusConnections. This constructor is for the
     * <code>ProtocolComponentFactory</code> eyes only.
     *
     * @param messageBusConfigurations The properties for the connection.
     */
    public ActiveMQMessageBus(MessageBusConfigurations messageBusConfigurations) {
        log.debug("Initializing ActiveMQConnection to '" + messageBusConfigurations + "'.");
        this.configuration = messageBusConfigurations.getPrimaryMessageBusConfiguration();

        // Retrieve factory for connection
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(configuration.getUsername(),
                                                                                    configuration.getPassword(),
                                                                                    configuration.getUrl());

        try {
            // create and start the connection
            Connection connection = connectionFactory.createConnection();

            connection.setExceptionListener(new MessageBusExceptionListener());
            connection.start();

            session = connection.createSession(TRANSACTED, ACKNOWLEDGE_MODE);
        } catch (JMSException e) {
            throw new CoordinationLayerException("Unable to initialise connection to message bus", e);
        }
        log.debug("ActiveMQConnection initialized for '" + configuration + "'.");
    }

    @Override
    public synchronized void addListener(String destinationId, final MessageListener listener) {
        log.debug("Adding listener '" + listener + "' to destination: '" + destinationId + "' on message-bus '"
                          + configuration.getId() + "'.");
        MessageConsumer consumer = getMessageConsumer(destinationId, listener);
        try {
            consumer.setMessageListener(new ActiveMQMessageListener(listener));
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Unable to add listener '" + listener + "' to destinationId '" + destinationId + "'", e);
        }
    }

    @Override
    public synchronized void removeListener(String destinationId, MessageListener listener) {
        log.debug("Removing listener '" + listener + "' from destination: '" + destinationId + "' on message-bus '"
                          + configuration.getId() + "'.");
        MessageConsumer consumer = getMessageConsumer(destinationId, listener);
        try {
            consumer.close();
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Unable to remove listener '" + listener + "' from destinationId '" + destinationId + "'", e);
        }
        consumers.remove(getConsumerHash(destinationId, listener));
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsComplete content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsRequest content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsResponse content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileComplete content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsComplete content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsRequest content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsResponse content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileRequest content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileResponse content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetChecksumsReply content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetChecksumsRequest content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetFileIDsRequest content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetFileIDsReply content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetFileRequest content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForGetFileReply content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForPutFileReply content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, IdentifyPillarsForPutFileRequest content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileComplete content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileRequest content) {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileResponse content) {
        sendMessage(destinationId, (Object) content);
    }

    /**
     * Send a message using ActiveMQ.
     *
     * @param destinationId Name of destination to send message to
     * @param content       JAXB-serializable object to send.
     */
    private void sendMessage(String destinationId, Object content) {
        try {
            String xmlContent = JaxbHelper.serializeToXml(content);
            log.debug("The following message is sent to the destination '" + destinationId + "'" + " on message-bus '"
                              + configuration.getId() + "': \n{}", xmlContent);
            MessageProducer producer = addTopicMessageProducer(destinationId);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            Message msg = session.createTextMessage(xmlContent);
            msg.setStringProperty(MESSAGE_TYPE_KEY, content.getClass().getSimpleName());
            msg.setJMSReplyTo(session.createQueue(destinationId));

            producer.send(msg);
            session.commit();
        } catch (JMSException e) {
            throw new CoordinationLayerException("Could not send message", e);
        } catch (JAXBException e) {
            throw new CoordinationLayerException("Could not send message", e);
        }
    }

    /**
     * Retrieves a consumer for the specific destination id and message listener.
     * If no such consumer already exists, then it is created.
     *
     * @param destinationId The id of the destination to consume messages from.
     * @param listener      The listener to consume the messages.
     * @return The instance for consuming the messages.
     */
    private MessageConsumer getMessageConsumer(String destinationId, MessageListener listener) {
        String key = getConsumerHash(destinationId, listener);
        log.debug("Retrieving message consumer on destination '" + destinationId + "' for listener '" + listener
                          + "'. Key: '" + key + "'.");
        if (!consumers.containsKey(key)) {
            log.debug("No consumer known. Creating new for key '" + key + "'.");
            Topic topic = getTopic(destinationId);
            MessageConsumer consumer;
            try {
                consumer = session.createConsumer(topic);
            } catch (JMSException e) {
                throw new CoordinationLayerException("Could not create message consumer for topic '" + topic + '"', e);
            }
            consumers.put(key, consumer);
        }
        return consumers.get(key);
    }

    /**
     * Creates a unique hash of the message listener and the destination id.
     *
     * @param destinationId The id for the destination.
     * @param listener      The message listener.
     * @return The key for the message listener and the destination id.
     */
    private String getConsumerHash(String destinationId, MessageListener listener) {
        return destinationId + CONSUMER_KEY_SEPARATOR + listener.hashCode();
    }

    /**
     * Method for retrieving the message producer for a specific queue.
     *
     * @param destination The id for the destination.
     * @return The message producer for this destination.
     */
    private MessageProducer addTopicMessageProducer(String destination) {
        Topic topic = getTopic(destination);
        MessageProducer producer;
        try {
            producer = session.createProducer(topic);
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Could not create message producer for destination '" + destination + "'", e);
        }
        return producer;
    }

    /**
     * Given a topic ID, retrieve the topic object.
     *
     * @param topicId ID of the topic.
     * @return The object representing that topic. Will always return the same topic object for the same topic ID.
     */
    private Topic getTopic(String topicId) {
        Topic topic = topics.get(topicId);
        if (topic == null) {
            try {
                // TODO: According to javadoc, topics should be looked up in another fashion.
                // See http://download.oracle.com/javaee/6/api/javax/jms/Session.html#createTopic(java.lang.String)
                topic = session.createTopic(topicId);
            } catch (JMSException e) {
                throw new CoordinationLayerException("Could not create topic '" + topicId + "'", e);
            }
            topics.put(topicId, topic);
        }
        return topic;
    }

    /** Class for handling the message bus exceptions. */
    private class MessageBusExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException arg0) {
            log.error("JMSException caught: ", arg0);
        }
    }

    /**
     * Adapter from Active MQ message listener to protocol message listener.
     *
     * This adapts from general Active MQ messages to the protocol types.
     */
    private static class ActiveMQMessageListener implements javax.jms.MessageListener {
        /** The Log. */
        private Logger log = LoggerFactory.getLogger(getClass());

        /** The protocol message listener that receives the messages. */
        private final MessageListener listener;

        /**
         * Initialise the adapter from ActiveMQ message listener to protocol
         * message listener.
         *
         * @param listener The protocol message listener that should receive the
         *                 messages.
         */
        public ActiveMQMessageListener(MessageListener listener) {
            this.listener = listener;
        }

        /**
         * When receiving the message, call the appropriate method on the
         * protocol message listener.
         *
         * This method acts as a fault barrier for all exceptions from message
         * reception. They are all logged as warnings, but otherwise ignored.
         *
         * @param message The message received.
         */
        @Override
        public void onMessage(final Message message) {
            String type = null;
            String text = null;
            Object content;
            try {
                type = message.getStringProperty(MESSAGE_TYPE_KEY);
                text = ((TextMessage) message).getText();
                content = JaxbHelper.loadXml(Class.forName("org.bitrepository.bitrepositorymessages." + type),
                                             new ByteArrayInputStream(text.getBytes()));

                if (content.getClass().equals(GetChecksumsComplete.class)) {
                    listener.onMessage((GetChecksumsComplete) content);
                    return;
                }

                if (content.getClass().equals(GetChecksumsRequest.class)) {
                    listener.onMessage((GetChecksumsRequest) content);
                    return;
                }

                if (content.getClass().equals(GetChecksumsResponse.class)) {
                    listener.onMessage((GetChecksumsResponse) content);
                    return;
                }

                if (content.getClass().equals(GetFileComplete.class)) {
                    listener.onMessage((GetFileComplete) content);
                    return;
                }

                if (content.getClass().equals(GetFileIDsComplete.class)) {
                    listener.onMessage((GetFileIDsComplete) content);
                    return;
                }

                if (content.getClass().equals(GetFileIDsRequest.class)) {
                    listener.onMessage((GetFileIDsRequest) content);
                    return;
                }

                if (content.getClass().equals(GetFileIDsResponse.class)) {
                    listener.onMessage((GetFileIDsResponse) content);
                    return;
                }

                if (content.getClass().equals(GetFileRequest.class)) {
                    listener.onMessage((GetFileRequest) content);
                    return;
                }

                if (content.getClass().equals(GetFileResponse.class)) {
                    listener.onMessage((GetFileResponse) content);
                    return;
                }

                if (content.getClass().equals(IdentifyPillarsForGetChecksumsReply.class)) {
                    listener.onMessage((IdentifyPillarsForGetChecksumsReply) content);
                    return;
                }

                if (content.getClass().equals(IdentifyPillarsForGetChecksumsRequest.class)) {
                    listener.onMessage((IdentifyPillarsForGetChecksumsRequest) content);
                    return;
                }

                if (content.getClass().equals(IdentifyPillarsForGetFileIDsReply.class)) {
                    listener.onMessage((IdentifyPillarsForGetFileIDsReply) content);
                    return;
                }

                if (content.getClass().equals(IdentifyPillarsForGetFileIDsRequest.class)) {
                    listener.onMessage((IdentifyPillarsForGetFileIDsRequest) content);
                    return;
                }

                if (content.getClass().equals(IdentifyPillarsForGetFileReply.class)) {
                    listener.onMessage((IdentifyPillarsForGetFileReply) content);
                    return;
                }

                if (content.getClass().equals(IdentifyPillarsForGetFileRequest.class)) {
                    listener.onMessage((IdentifyPillarsForGetFileRequest) content);
                    return;
                }

                if (content.getClass().equals(IdentifyPillarsForPutFileReply.class)) {
                    listener.onMessage((IdentifyPillarsForPutFileReply) content);
                    return;
                }

                if (content.getClass().equals(IdentifyPillarsForPutFileRequest.class)) {
                    listener.onMessage((IdentifyPillarsForPutFileRequest) content);
                    return;
                }

                if (content.getClass().equals(PutFileComplete.class)) {
                    listener.onMessage((PutFileComplete) content);
                    return;
                }

                if (content.getClass().equals(PutFileRequest.class)) {
                    listener.onMessage((PutFileRequest) content);
                    return;
                }

                if (content.getClass().equals(PutFileResponse.class)) {
                    listener.onMessage((PutFileResponse) content);
                    return;
                }
                log.error("Received message of unknown type '" + type + "'\n{}", text);
            } catch (Exception e) {
                log.error("Error handling message. Received type was '" + type + "'.\n{}", text);
            }

        }
    }
}