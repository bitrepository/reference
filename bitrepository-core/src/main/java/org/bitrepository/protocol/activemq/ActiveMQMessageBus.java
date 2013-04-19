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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.protocol.CoordinationLayerException;
import org.bitrepository.protocol.MessageVersionValidator;
import org.bitrepository.protocol.OperationType;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.bitrepository.protocol.messagebus.logger.AlarmMessageLogger;
import org.bitrepository.protocol.messagebus.logger.DeleteFileMessageLogger;
import org.bitrepository.protocol.messagebus.logger.GetAuditTrailsMessageLogger;
import org.bitrepository.protocol.messagebus.logger.GetChecksumsMessageLogger;
import org.bitrepository.protocol.messagebus.logger.GetFileIDsMessageLogger;
import org.bitrepository.protocol.messagebus.logger.GetFileMessageLogger;
import org.bitrepository.protocol.messagebus.logger.GetStatusMessageLogger;
import org.bitrepository.protocol.messagebus.logger.MessageLoggerProvider;
import org.bitrepository.protocol.messagebus.logger.PutFileMessageLogger;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Contains the basic functionality for connection and communicating with the
 * coordination layer over JMS through active MQ.
 */
public class ActiveMQMessageBus implements MessageBus {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The key for storing the message type in a string property in the message headers. */
    public static final String MESSAGE_TYPE_KEY = "org.bitrepository.messages.type";
    /** The key for storing the BitRepositoryCollectionID in a string property in the message headers. */
    public static final String COLLECTION_ID_KEY = "org.bitrepository.messages.collectionid";
    /** The key for storing the message type in a string property in the message headers. */
    public static final String MESSAGE_SIGNATURE_KEY = "org.bitrepository.messages.signature";
    public static final String MESSAGE_RECIPIENT_KEY = "org.bitrepository.messages.recipient";
    /** Default transacted. */
    public static final boolean TRANSACTED = false;

    /** The variable to separate the parts of the consumer key. */
    private static final String CONSUMER_KEY_SEPARATOR = "#";
    
    /** The initial number of threads running in the thread pool.*/
    private static final int INIT_SIZE_OF_THREAD_POOL = 20;
    /** The maximum number of threads in the pool.*/
    private static final int MAX_SIZE_OF_THREAD_POOL = 20;
    /** The number of seconds to keep the threads alive, when idle.*/
    private static final long SECONDS_TO_KEEP_THREADS_ALIVE = 60;

    /** The session for sending messages. Should not be the same as the consumer session, 
     * as sessions are not thread safe. This also means the session should be used in a synchronized manor.
     * TODO Switch to use a session pool/producer poll to allow multithreaded message sending, see 
     * https://sbforge.org/jira/browse/BITMAG-357.
     */
    private final Session producerSession;

    /** The session for receiving messages. */
    private final Session consumerSession;

    /**
     * Map of the consumers, mapping from a hash of "destinations and listener" to consumer.
     * Used to identify if a listener is already registered.
     */
    private final Map<String, MessageConsumer> consumers = Collections
            .synchronizedMap(new HashMap<String, MessageConsumer>());
    /** Map of destinations, mapping from ID to destination. */
    private final Map<String, Destination> destinations = new HashMap<String, Destination>();
    /** The configuration for the connection to the activeMQ. */
    private final MessageBusConfiguration configuration;
    private String schemaLocation = "BitRepositoryMessages.xsd";
    private final JaxbHelper jaxbHelper;
    private final Connection connection;
    private final SecurityManager securityManager;

    private final Set<String> componentFilter = new HashSet<String>();
    private final Set<String> collectionFilter = new HashSet<String>();

    /** The queue with the runnable threads for handling the received messages. */
    private final BlockingQueue<Runnable> threadQueue;
    /** The executor for threading of the message handling.*/
    private final ExecutorService executor;
    /** The single producer used to send all messages. The destination need to be sent on the messages. */
    private final MessageProducer producer;
    
    /**
     * Use the {@link org.bitrepository.protocol.ProtocolComponentFactory} to get a handle on a instance of
     * MessageBusConnections. This constructor is for the
     * <code>ProtocolComponentFactory</code> eyes only.
     *
     * @param messageBusConfiguration The properties for the connection.
     * @param securityManager The security manager to use for message authentication.
     * messages.
     */
    public ActiveMQMessageBus(
            MessageBusConfiguration messageBusConfiguration,
            SecurityManager securityManager) {
        log.info("Initializing ActiveMQMessageBus:'" + messageBusConfiguration + "'.");
        this.configuration = messageBusConfiguration;
        this.securityManager = securityManager;
        jaxbHelper = new JaxbHelper("xsd/", schemaLocation);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(configuration.getURL());
        registerCustomMessageLoggers();
        try {
            connection = connectionFactory.createConnection();
            connection.setExceptionListener(new MessageBusExceptionListener());

            producerSession = connection.createSession(TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            consumerSession = connection.createSession(TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            producer = producerSession.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            startListeningForMessages();
        } catch (JMSException e) {
            throw new CoordinationLayerException("Unable to initialise connection to message bus", e);
        }
        log.debug("ActiveMQConnection initialized for '" + configuration + "'.");
        
        threadQueue = new LinkedBlockingQueue<Runnable>();
        executor = new ThreadPoolExecutor(INIT_SIZE_OF_THREAD_POOL, MAX_SIZE_OF_THREAD_POOL, 
                SECONDS_TO_KEEP_THREADS_ALIVE, TimeUnit.SECONDS, threadQueue);
    }

    /**
     * Start to listen for message on the message bus. This is done in a separate thread to avoid blocking, 
     * so the main thread can continue without having to wait for the messagebus listening to start.
     */
    private void startListeningForMessages() {
        Thread connectionStarter = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection.start();
                } catch (Exception e) {
                    log.error("Unable to start listening on the message bus", e);
                }
            }
        });
        connectionStarter.start();

    }

    @Override
    public synchronized void addListener(String destinationID, final MessageListener listener) {
        log.info("Adding listener '{}' to destination: '{}' on message-bus '{}'.",
                new Object[] {listener, destinationID, configuration.getURL()});
        MessageConsumer consumer = getMessageConsumer(destinationID, listener);
        try {
            consumer.setMessageListener(new ActiveMQMessageListener(listener));
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Unable to add listener '" + listener + "' to destinationID '" + destinationID + "'", e);
        }
    }

    /*
    public synchronized void addDurableListener(String destinationID, final MessageListener listener) {
        log.debug("Adding durable listener '{}' to destination: '{}' on message-bus '{}'.",
                new Object[] {listener, destinationID, configuration.getName()});
        MessageConsumer consumer = getDurableMessageConsumer(destinationID, listener);
        try {
            consumer.setMessageListener(new ActiveMQMessageListener(listener));
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Unable to add durable listener '" + listener + "' to destinationID '" + destinationID + "'", e);
        }
    }       */

    @Override
    public synchronized void removeListener(String destinationID, MessageListener listener) {
        log.debug("Removing listener '" + listener + "' from destination: '" + destinationID + "' " +
                "on message-bus '" + configuration + "'.");
        MessageConsumer consumer = getMessageConsumer(destinationID, listener);
        try {
            // We need to set the listener to null to have the removeListerer take effect at once. 
            // If this isn't done the listener will continue to receive messages. Do we have a memory leak here? 
            consumer.setMessageListener(null);
            consumer.close();
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Unable to remove listener '" + listener + "' from destinationID '" + destinationID + "'", e);
        }
        consumers.remove(getConsumerHash(destinationID, listener));
    }

    @Override
    public void close() throws JMSException {
        log.info("Closing message bus: " + configuration);
        producerSession.close();
        log.debug("Producer session closed.");
        consumerSession.close();
        log.debug("Consumer session closed.");
        connection.close();
        log.debug("Connection closed.");
    }

    @Override
    public void sendMessage(Message content) {
        sendMessage(content.getDestination(), content.getReplyTo(), content.getTo(), content.getCollectionID(),
                content.getCorrelationID(), content);
        MessageLoggerProvider.getInstance().logMessageSent(content);
    }

    /**
     * Send a message using ActiveMQ.
     *
     * Note that the method is synchronized to avoid multithreaded usage of the providerSession.
     *
     * @param destinationID Name of destination to send message to.
     * @param replyTo       The queue to reply to.
     * @param collectionID  The collection ID of the message.
     * @param correlationID The correlation ID of the message.
     * @param content       JAXB-serializable object to send.
     */
    private synchronized void sendMessage(String destinationID,
                                          String replyTo,
                                          String recipient,
                                          String collectionID,
                                          String correlationID,
                                          Message content) {
        String xmlContent = null;
        try {
            xmlContent = jaxbHelper.serializeToXml(content);
            jaxbHelper.validate(new ByteArrayInputStream(xmlContent.getBytes()));
            log.trace("The following message is sent to the destination '" + destinationID + "'" + " on message-bus '"
                    + configuration.getName() + "': \n{}", xmlContent);

            javax.jms.Message msg = producerSession.createTextMessage(xmlContent);
            String stringData = ((TextMessage) msg).getText();
            String messageSignature = securityManager.signMessage(stringData);
            msg.setStringProperty(MESSAGE_SIGNATURE_KEY, messageSignature);
            msg.setStringProperty(MESSAGE_TYPE_KEY, content.getClass().getSimpleName());
            if (recipient != null) {
                msg.setStringProperty(MESSAGE_RECIPIENT_KEY, recipient);
            }
            msg.setStringProperty(COLLECTION_ID_KEY, collectionID);
            msg.setJMSCorrelationID(correlationID);
            msg.setJMSReplyTo(getDestination(replyTo, producerSession));

            producer.send(getDestination(destinationID, producerSession), msg);
        } catch (SAXException e) {
            throw new CoordinationLayerException("Rejecting to send invalid message: " + xmlContent, e);
        } catch (Exception e) {
            throw new CoordinationLayerException("Could not send message", e);
        }
    }

    /**
     * Retrieves a consumer for the specific destination id and message listener.
     * If no such consumer already exists, then it is created.
     *
     * @param destinationID The id of the destination to consume messages from.
     * @param listener      The listener to consume the messages.
     * @return The instance for consuming the messages.
     */
    private MessageConsumer getMessageConsumer(String destinationID, MessageListener listener) {
        String key = getConsumerHash(destinationID, listener);
        log.debug("Retrieving message consumer on destination '" + destinationID + "' for listener '" + listener
                + "'. Key: '" + key + "'.");
        if (!consumers.containsKey(key)) {
            log.debug("No consumer known. Creating new for key '" + key + "'.");
            Destination destination = getDestination(destinationID, consumerSession);
            MessageConsumer consumer;
            try {
                consumer = consumerSession.createConsumer(destination);
            } catch (JMSException e) {
                throw new CoordinationLayerException("Could not create message consumer for destination '" + destination + '"', e);
            }
            consumers.put(key, consumer);
        }
        return consumers.get(key);
    }

    /**
     * Creates a unique hash of the message listener and the destination id.
     *
     * @param destinationID The id for the destination.
     * @param listener      The message listener.
     * @return The key for the message listener and the destination id.
     */
    private String getConsumerHash(String destinationID, MessageListener listener) {
        return destinationID + CONSUMER_KEY_SEPARATOR + listener.hashCode();
    }

    /**
     * Method for retrieving the message producer for a specific queue.
     *
     * @param destinationID The id for the destination.
     * @return The message producer for this destination.
     */
    private MessageProducer addDestinationMessageProducer(String destinationID) {
        Destination destination = getDestination(destinationID, producerSession);
        MessageProducer producer;
        try {
            producer = producerSession.createProducer(destination);
        } catch (JMSException e) {
            throw new CoordinationLayerException(
                    "Could not create message producer for destination '" + destinationID + "'", e);
        }
        return producer;
    }

    /**
     * Given a destination ID, retrieve the destination object.
     *
     * @param destinationID ID of the destination.
     * @return The object representing that destination. Will always return the same destination object for the same destination ID.
     */
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

    /** Class for handling the message bus exceptions. */
    private class MessageBusExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException arg0) {
            log.error("JMSException caught: ", arg0);
        }
    }

    /**
     * Adapter from Active MQ message listener to message listener.
     *
     * This adapts from general Active MQ messages to the types.
     */
    private class ActiveMQMessageListener implements javax.jms.MessageListener {
        /** The Log. */
        private Logger log = LoggerFactory.getLogger(getClass());

        /** The message listener that receives the messages. */
        private final MessageListener messageListener;
        
        /**
         * Initialise the adapter from ActiveMQ message listener .
         *
         * @param listener The message listener that should receive the messages.
         */
        public ActiveMQMessageListener(MessageListener listener) {
            this.messageListener = listener;
        }

        /**
         * When receiving the message, call the appropriate method on the message listener.
         *
         * This method acts as a fault barrier for all exceptions from message
         * reception. They are all logged as warnings, but otherwise ignored.
         *
         * @param jmsMessage The message received.
         */
        @Override
        public void onMessage(final javax.jms.Message jmsMessage) {
            String type = null;
            String text = null;
            try {
                String recipientID = jmsMessage.getStringProperty(MESSAGE_RECIPIENT_KEY);
                type = jmsMessage.getStringProperty(MESSAGE_TYPE_KEY);
                if(!componentFilter.isEmpty()) {
                    if (recipientID != null && !componentFilter.contains(recipientID)) {
                        log.trace("Ignoring " + type + " message to other component " + recipientID);
                        return;
                    }
                }
                String collectionID = jmsMessage.getStringProperty(COLLECTION_ID_KEY);
                if(!collectionFilter.isEmpty()) {
                    if (collectionID != null && !collectionFilter.contains(collectionID)) {
                        log.trace("Ignoring message to unknown collection " + collectionID);
                        return;
                    }
                }
                String signature = jmsMessage.getStringProperty(MESSAGE_SIGNATURE_KEY);
                text = ((TextMessage) jmsMessage).getText();
                log.trace("Received xml message: " + text);
                jaxbHelper.validate(new ByteArrayInputStream(text.getBytes()));
                Message content = (Message) jaxbHelper.loadXml(Class.forName("org.bitrepository.bitrepositorymessages."
                        + type),
                        new ByteArrayInputStream(text.getBytes()));
                log.trace("Checking signature " + signature);
                securityManager.authenticateMessage(text, signature);
                securityManager.authorizeCertificateUse((content).getFrom(), text, signature);
                if (content instanceof MessageRequest) {
                    securityManager.authorizeOperation(content.getClass().getSimpleName(), text, signature);
                }
                MessageVersionValidator.validateMessageVersion(content);
                MessageLoggerProvider.getInstance().logMessageReceived(content);
                threadMessageHandling(content);
            } catch (SAXException e) {
                log.error("Error validating message " + jmsMessage, e);
            } catch (Exception e) {
                log.error("Error handling message. Received type was '" + type + "'.\n{}", text, e);
            }
        }
        
        /**
         * Making the handling of the message be performed in parallel.
         * @param message The message to be handled by the MessageListener.
         */
        private void threadMessageHandling(Message message) {
            MessageListenerThread mlt = new MessageListenerThread(messageListener, message);
            executor.execute(mlt);
            log.trace("Adding a new message handling thread. Currently number of running threads: " + threadQueue.size());
        }
    }
    
    /**
     * Simple class to thread the handling of messages by the message listener.
     */
    private class MessageListenerThread extends Thread {
        /** The message listener.*/
        private final MessageListener listener;
        /** The message for the listener to handle.*/
        private Message message;
        
        /**
         * @param listener The MessageListener to handle the message.
         * @param message The message to be handled by the MessageListener.
         */
        MessageListenerThread(MessageListener listener, Message message) {
            this.listener = listener;
            this.message = message;
        }
        
        @Override
        public void run() {
            listener.onMessage(message);
        }
    }

    // This should be done on a per module basis, but how?
    private void registerCustomMessageLoggers() {
        MessageLoggerProvider loggerProvider = MessageLoggerProvider.getInstance();
        loggerProvider.registerLogger(OperationType.GET_FILE, new GetFileMessageLogger());
        loggerProvider.registerLogger(OperationType.PUT_FILE, new PutFileMessageLogger());
        loggerProvider.registerLogger(OperationType.DELETE_FILE, new DeleteFileMessageLogger());
        loggerProvider.registerLogger(OperationType.REPLACE_FILE, new GetStatusMessageLogger());
        loggerProvider.registerLogger(OperationType.GET_FILE_IDS, new GetFileIDsMessageLogger());
        loggerProvider.registerLogger(OperationType.GET_CHECKSUMS, new GetChecksumsMessageLogger());
        loggerProvider.registerLogger(OperationType.GET_AUDIT_TRAILS, new GetAuditTrailsMessageLogger());
        loggerProvider.registerLogger(OperationType.GET_STATUS, new GetStatusMessageLogger());
        loggerProvider.registerLogger(Arrays.asList("AlarmMessage"), new AlarmMessageLogger());
    }

    @Override
    public Set<String> getComponentFilter() {
        return componentFilter;
    }

    @Override
    public Set<String> getCollectionFilter() {
        return collectionFilter;
    }
}