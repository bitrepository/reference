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
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.MessageFactory;
import org.bitrepository.protocol.MessageListener;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.configuration.MessageBusConfiguration;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;
import org.bitrepository.protocol.exceptions.CoordinationLayerException;
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
public final class ActiveMQMessageBus implements MessageBus {
    /** The Log. */
    private Logger log = LoggerFactory.getLogger(ActiveMQMessageBus.class);

    /** The default acknowledge mode. */
    public static final int ACKNOWLEDGE_MODE = Session.AUTO_ACKNOWLEDGE;
    /** Default transacted. */
    public static final boolean TRANSACTED = true;

    /** The variable to separate the parts of the consumer key. */
    private static final String CONSUMER_KEY_SEPARATOR = "#";

    /** The session. */
    private final Session session;

    /** Map of the consumers, mapping from topicID"#"listener to consumer. */
    private final Map<String, MessageConsumer> consumers = Collections
            .synchronizedMap(new HashMap<String, MessageConsumer>());
    /** Map of topics, mapping from ID to topic. */
    private final Map<String, Topic> topics = new HashMap<String, Topic>();
    /** The configuration for the connection to the activeMQ. */
    private final MessageBusConfiguration configuration;

    /**
     * Use the {@link ProtocolComponentFactory} to get a handle on a instance of
     * MessageBusConnections. This constructor is for the
     * <code>ProtocolComponentFactory</code> eyes only.
     *
     * @param messageBusConfigurations The properties for the connection.
     */
    public ActiveMQMessageBus(MessageBusConfigurations messageBusConfigurations) {
        log.debug("Initializing ActiveMQConnection to '"
                          + messageBusConfigurations + "'.");
        this.configuration = messageBusConfigurations
                .getPrimaryMessageBusConfiguration();

        // Retrieve factory for connection
        ActiveMQConnectionFactory connectionFactory
                = new ActiveMQConnectionFactory(configuration.getUsername(),
                                                configuration.getPassword(),
                                                configuration.getUrl());

        try {
            // create and start the connection
            Connection connection = connectionFactory.createConnection();

            connection.setExceptionListener(new MessageBusExceptionListener());
            connection.start();

            session = connection.createSession(TRANSACTED, ACKNOWLEDGE_MODE);
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Unable to initialise connection to message bus", e);
        }
        log.debug("ActiveMQConnection initialized for '" + configuration + "'.");
    }

    @Override
    public synchronized void addListener(String destinationId,
                                         final MessageListener listener)
            throws JMSException {
        log.debug("Adding listener '" + listener + "' to topic: '" + destinationId
                        + "' on message-bus '" + configuration.getId() + "'.");
        MessageConsumer consumer = getMessageConsumer(destinationId, listener);
        consumer.setMessageListener(new ActiveMQMessageListener(listener));
    }

    @Override
    public synchronized void removeListener(String destinationId,
                                            MessageListener listener)
            throws JMSException {
        log.debug("Removing listener '" + listener + "' from topic: '"
                          + destinationId + "' on message-bus '"
                          + configuration.getId() + "'.");
        MessageConsumer consumer = getMessageConsumer(destinationId, listener);
        consumer.close();
        consumers.remove(getConsumerKey(destinationId, listener));
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsComplete content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsRequest content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsResponse content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileComplete content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsComplete content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsRequest content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsResponse content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileRequest content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileResponse content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId,
                            IdentifyPillarsForGetChecksumsReply content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId,
                            IdentifyPillarsForGetChecksumsRequest content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId,
                            IdentifyPillarsForGetFileIDsRequest content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId,
                            IdentifyPillarsForGetFileIDsReply content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId,
                            IdentifyPillarsForGetFileRequest content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId,
                            IdentifyPillarsForGetFileReply content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId,
                            IdentifyPillarsForPutFileReply content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId,
                            IdentifyPillarsForPutFileRequest content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileComplete content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileRequest content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileResponse content)
            throws Exception {
        sendMessage(destinationId, (Object) content);
    }

    /**
     * Send a message using ActiveMQ
     *
     * @param destinationId Name of topic to send message to
     * @param content       JAXB-serializable object to send.
     */
    private void sendMessage(String destinationId, Object content) {
        try {
            String xmlContent = MessageFactory.extractMessage(content);
            log.debug("The following message is sent to the topic '"
                              + destinationId + "'"
                              + " on message-bus '{}': \n{}",
                      configuration.getId(), xmlContent);
            MessageProducer producer = addTopicMessageProducer(
                    destinationId);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            Message msg = session.createTextMessage(xmlContent);
            // TODO use the StringProperty instead of this?
            msg.setJMSType(content.getClass().getSimpleName());
            msg.setJMSReplyTo(session.createQueue(destinationId));

            producer.send(msg);
            session.commit();
        } catch (Exception e) {
            throw new CoordinationLayerException("Could not send message", e);
        }
    }

    /**
     * Retrieves a consumer for the specific topic id and message listener.
     * If no such consumer already exists, then it is created.
     *
     * @param topicId  The id of the topic to consume messages from.
     * @param listener The listener to consume the messages.
     * @return The instance for consuming the messages.
     */
    private MessageConsumer getMessageConsumer(String topicId,
                                               MessageListener listener)
            throws JMSException {
        String key = getConsumerKey(topicId, listener);
        log.debug("Retrieving message consumer on topic '" + topicId
                          + "' for listener '" + listener + "'. Key: '" + key
                          + "'.");
        if (!consumers.containsKey(key)) {
            log.debug("No consumer known. Creating new for key '" + key + "'.");
            Topic topic = getTopic(topicId);
            MessageConsumer consumer = session.createConsumer(topic);
            consumers.put(key, consumer);
        }
        return consumers.get(key);
    }

    /**
     * Creates a unique key for the message listener and the topic id.
     *
     * @param topicId  The id for the topic.
     * @param listener The message listener.
     * @return The key for the message listener and the topic id.
     */
    private String getConsumerKey(String topicId, MessageListener listener) {
        return topicId + CONSUMER_KEY_SEPARATOR + listener.hashCode();
    }

    /**
     * Method for retrieving the message producer for a specific queue.
     *
     * @param topicId The id for the queue.
     * @return The message producer for this queue.
     *
     * @throws JMSException If the producer for the queue cannot be established.
     */
    private MessageProducer addTopicMessageProducer(String topicId)
            throws JMSException {
        Topic topic = getTopic(topicId);
        MessageProducer producer = session.createProducer(topic);
        return producer;
    }

    private Topic getTopic(String topicId) throws JMSException {
        Topic topic = topics.get(topicId);
        if (topic == null) {
            // TODO: According to javadoc, topics should be looked up in another fashion
            topic = session.createTopic(topicId);
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
    private static class ActiveMQMessageListener
            implements javax.jms.MessageListener {
        /** The Log. */
        private Logger log = LoggerFactory
                .getLogger(ActiveMQMessageBus.class);

        /** The protocol message listener that receives the messages. */
        private final MessageListener listener;

        /**
         * Iniitialise the adapter from ActiveMQ message listener to protocol
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
                type = message.getJMSType();
                text = ((TextMessage) message).getText();
                content = MessageFactory.createMessage(Class.forName(
                        "org.bitrepository.bitrepositorymessages." + type),
                                                       text);

                if (type.equals("GetChecksumsComplete")) {
                    listener.onMessage((GetChecksumsComplete) content);
                    return;
                }

                if (type.equals("GetChecksumsRequest")) {
                    listener.onMessage((GetChecksumsRequest) content);
                    return;
                }

                if (type.equals("GetChecksumsResponse")) {
                    listener.onMessage((GetChecksumsResponse) content);
                    return;
                }

                if (type.equals("GetFileComplete")) {
                    listener.onMessage((GetFileComplete) content);
                    return;
                }

                if (type.equals("GetFileIDsComplete")) {
                    listener.onMessage((GetFileIDsComplete) content);
                    return;
                }

                if (type.equals("GetFileIDsRequest")) {
                    listener.onMessage((GetFileIDsRequest) content);
                    return;
                }

                if (type.equals("GetFileIDsResponse")) {
                    listener.onMessage((GetFileIDsResponse) content);
                    return;
                }

                if (type.equals("GetFileRequest")) {
                    listener.onMessage((GetFileRequest) content);
                    return;
                }

                if (type.equals("GetFileResponse")) {
                    listener.onMessage((GetFileResponse) content);
                    return;
                }

                if (type.equals("IdentifyPillarsForGetChecksumsReply")) {
                    listener.onMessage(
                            (IdentifyPillarsForGetChecksumsReply) content);
                    return;
                }

                if (type.equals("IdentifyPillarsForGetChecksumsRequest")) {
                    listener.onMessage(
                            (IdentifyPillarsForGetChecksumsRequest) content);
                    return;
                }

                if (type.equals("IdentifyPillarsForGetFileIDsReply")) {
                    listener.onMessage(
                            (IdentifyPillarsForGetFileIDsReply) content);
                    return;
                }

                if (type.equals("IdentifyPillarsForGetFileIDsRequest")) {
                    listener.onMessage(
                            (IdentifyPillarsForGetFileIDsRequest) content);
                    return;
                }

                if (type.equals("IdentifyPillarsForGetFileReply")) {
                    listener.onMessage(
                            (IdentifyPillarsForGetFileReply) content);
                    return;
                }

                if (type.equals("IdentifyPillarsForGetFileRequest")) {
                    listener.onMessage(
                            (IdentifyPillarsForGetFileRequest) content);
                    return;
                }

                if (type.equals("IdentifyPillarsForPutFileReply")) {
                    listener.onMessage(
                            (IdentifyPillarsForPutFileReply) content);
                    return;
                }

                if (type.equals("IdentifyPillarsForPutFileRequest")) {
                    listener.onMessage(
                            (IdentifyPillarsForPutFileRequest) content);
                    return;
                }

                if (type.equals("PutFileComplete")) {
                    listener.onMessage((PutFileComplete) content);
                    return;
                }

                if (type.equals("PutFileRequest")) {
                    listener.onMessage((PutFileRequest) content);
                    return;
                }

                if (type.equals("PutFileResponse")) {
                    listener.onMessage((PutFileResponse) content);
                    return;
                }
                log.warn("Received message of unknown type '{}'\n{}", type,
                         text);
            } catch (Exception e) {
                log.warn("Error handling message. Received type was '{}'.\n{}",
                         type, text);
            }

        }
    }
}