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
package org.bitrepository.protocol.performancetest;

import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.bus.LocalActiveMQBroker;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

/**
 * Stress testing of the messagebus.
 * <p>
 * TODO Important note for testers:
 * The number of listeners should be regulated through the 'NUMBER_OF_LISTENERS' constant.
 * When using many listeners, the DEFAULT_WAIT_TIME should be increased, e.g. 5000 for 25 listeners
 * and 15000 for 100 listeners.
 * Otherwise it is not ensured, that all the messagelisteners will receive all the messages before the validation.
 * <p>
 * Also, the shutdown of the messagelisteners can generate some noise, which will make it impossible to retrieve the
 * output data from the console. Therefore the results can be written to a file after the test.
 * This is controlled through the variables 'WRITE_RESULTS_TO_FILE', which deternimes whether to write to the file, and
 * 'OUTPUT_FILE_NAME' which is the name of the file to write the output results.
 */
public class MessageBusNumberOfListenersStressTest {
    /**
     * The default time to wait for a simple communication.
     */
    private static final long DEFAULT_WAIT_TIME = 500;
    /**
     * The time for the whole test.
     */
    private static final long TIME_FRAME = 60000L;
    /**
     * The number of message listeners in the test.
     */
    private static final int NUMBER_OF_LISTENERS = 10;
    /**
     * Whether the results will be written to a file.
     */
    private static final boolean WRITE_RESULTS_TO_FILE = false;
    /**
     * The name of the output file for the results of the tests.
     */
    private static final String OUTPUT_FILE_NAME = "NumberOfListeners-results.test";
    /**
     * The reached correlation ID for the message.
     */
    private static int idReached = -1;
    /**
     * The message to send back and forth over the message bus.
     */
    private static AlarmMessage alarmMessage;
    /**
     * The message bus instance for sending the messages.
     */
    private static MessageBus bus;
    /**
     * The amount of messages received.
     */
    private static int messageReceived = 0;
    /**
     * Whether more messages should be sent.
     */
    private static boolean sendMoreMessages = true;
    private Settings settings;
    private String testQueue;

    @BeforeEach
    public void initializeSettings() {
        settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
        testQueue = "TEST-LISTENERS-" + System.currentTimeMillis();
    }

    /**
     * Tests the amount of messages sent over a message bus, which is not placed locally.
     * Require sending at least five messages per second.
     *
     * @throws Exception Can possibly throw an exception.
     */
    @Test
    @Tag("StressTest")
    public void testManyListenersOnLocalMessageBus() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe when a given number of "
                + "listeners are receiving them.");
        addStep("Define constants", "This should not be possible to fail.");
        messageReceived = 0;
        idReached = -1;
        sendMoreMessages = true;

        addStep("Define the message to send.",
                "Should retrieve the Alarm message from examples and set the To.");
        alarmMessage = ExampleMessageFactory.createMessage(AlarmMessage.class);
        alarmMessage.setDestination(testQueue);

        addStep("Make configuration for the messagebus.", "Both should be created.");
        settings.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(
                MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration()
        );
        /* The mocked SecurityManager */
        SecurityManager securityManager = new DummySecurityManager();
        LocalActiveMQBroker broker = new LocalActiveMQBroker(settings.getMessageBusConfiguration());

        try {
            addStep("Start the broker and initialise the listeners.",
                    "Connections should be established.");
            broker.start();
            bus = new ActiveMQMessageBus(settings, securityManager);

            testListeners(settings.getMessageBusConfiguration(), securityManager);
        } finally {
            if (bus != null) {
                try {
                    bus.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            broker.stop();
        }
    }

    @Test
    @Tag("StressTest")
    public void testManyListenersOnDistributedMessageBus() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe when a given number of "
                + "listeners are receiving them.");
        addStep("Define constants", "This should not be possible to fail.");
        messageReceived = 0;
        idReached = -1;
        sendMoreMessages = true;

        addStep("Define the message to send.",
                "Should retrieve the Alarm message from examples and set the To.");
        alarmMessage = ExampleMessageFactory.createMessage(AlarmMessage.class);
        alarmMessage.setDestination(testQueue);

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfiguration conf = new MessageBusConfiguration();
        int port = getFreePort();
        conf.setURL("tcp://localhost:" + port);
        settings.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(conf);
        /* The mocked SecurityManager */
        SecurityManager securityManager = new DummySecurityManager();
        LocalActiveMQBroker broker = new LocalActiveMQBroker(conf);

        try {
            broker.start();
            addStep("Start the broker and initialise the listeners.",
                    "Connections should be established.");
            bus = new ActiveMQMessageBus(settings, securityManager);

            testListeners(conf, securityManager);
        } finally {
            if (bus != null) {
                try {
                    bus.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            broker.stop();
        }
    }

    public void testListeners(MessageBusConfiguration conf, SecurityManager securityManager) throws Exception {
        List<NotificationMessageListener> listeners = new ArrayList<>(NUMBER_OF_LISTENERS);

        try {
            addStep("Initialise the message listeners.",
                    "Should be created and connected to the message bus.");
            for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
                Settings listenerSettings = TestSettingsProvider.getSettings(getClass().getSimpleName() + i);
                listenerSettings.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(conf);
                listeners.add(new NotificationMessageListener(listenerSettings, securityManager, testQueue));
            }

            addStep("Wait for setup", "We wait!");
            try {
                Thread.sleep(DEFAULT_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            addStep("Send the first message", "Message should be send.");
            sendMessageWithId(1);

            addStep("Wait for the timeframe on '" + TIME_FRAME + "' milliseconds.",
                    "We wait!");
            try {
                Thread.sleep(TIME_FRAME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            addStep("Stop sending more messages and await all the messages to be received by all the listeners",
                    "Should be Ok");
            sendMoreMessages = false;
            try {
                Thread.sleep(DEFAULT_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            addStep("Verifying the amount of message sent '" + idReached + "' has been received by all '"
                    + NUMBER_OF_LISTENERS + "' listeners", "Should be the same amount for each listener, and the same "
                    + "amount as the correlation ID of the message");
            Assertions.assertEquals(idReached * NUMBER_OF_LISTENERS, messageReceived, "Reached message Id " + idReached + " thus" +
                    " each message of the " + NUMBER_OF_LISTENERS + " listener "
                    + "should have received " + idReached + " message, though they have received "
                    + messageReceived + " message all together.");
            for (NotificationMessageListener listener : listeners) {
                Assertions.assertTrue((listener.getCount() == idReached),
                        "Should have received " + idReached + " messages, but has received "
                                + listener.getCount());
            }

            // If too many message-listeners, then they will create so much noise, that the results cannot be read from
            // the console output (due to shutdown 'warnings'). Thus write the results in a file.
            if (WRITE_RESULTS_TO_FILE) {
                FileOutputStream out = new FileOutputStream(new File(OUTPUT_FILE_NAME), true);
                out.write(("idReached: " + idReached + ", NumberOfListeners: " + NUMBER_OF_LISTENERS
                        + ", messagesReceived: " + messageReceived + " on bus "
                        + conf.getURL() + "\n").getBytes());
                out.flush();
                out.close();
            }
        } finally {
            for (NotificationMessageListener listener : listeners) {
                listener.stop();
            }
            listeners.clear();

            try {
                Thread.sleep(DEFAULT_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Finds a free port on the localhost.
     *
     * @return A free port number.
     * @throws IOException If an I/O error occurs.
     */
    private int getFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /**
     * Method for sending the Alarm message with a specific ID.
     *
     * @param id The correlation id for the message to send.
     */
    private static void sendMessageWithId(int id) {
        if (sendMoreMessages) {
            alarmMessage.setCorrelationID("" + id);
            bus.sendMessage(alarmMessage);
        }
    }

    /**
     * Function for handling the Correlation id of the received messages of the listeners.
     * If it is the first time a correlation id is received, then a new message with the subsequent correlation
     * id is sent. This ensures that the message is only sent once per Correlation id.
     *
     * @param receivedId The received correlation id.
     */
    public static synchronized void handleMessageDistribution(int receivedId) {
        if (receivedId > idReached) {
            idReached = receivedId;
            sendMessageWithId(idReached + 1);
        }
        messageReceived++;
    }

    /**
     * Messagelistener which notifies the 'handleMessageDistribution' method with the correlation id whenever
     * a message it received.
     * Otherwise counts the amount of received messages.
     */
    private static class NotificationMessageListener implements MessageListener {
        /**
         * The message bus.
         */
        private final MessageBus bus;
        /**
         * The amount of messages received.
         */
        private int count;
        private final String queueName;

        /**
         * Constructor.
         *
         * @param settings The configuration for defining the message bus.
         */
        public NotificationMessageListener(Settings settings, SecurityManager securityManager, String queueName) {
            this.bus = new ActiveMQMessageBus(settings, securityManager);
            this.count = 0;
            this.queueName = queueName;

            bus.addListener(queueName, this);
        }

        /**
         * Method for stopping interaction with the message listener.
         */
        public void stop() {
            bus.removeListener(queueName, this);
            try {
                bus.close();
            } catch (jakarta.jms.JMSException e) {
                // ignore
            }
        }

        /**
         * Retrieval of the amount of messages caught by the listener.
         *
         * @return The number of message received by this.
         */
        public int getCount() {
            return count;
        }

        @Override
        public void onMessage(Message message, MessageContext messageContext) {
            count++;
            int receivedId = Integer.parseInt(message.getCorrelationID());
            handleMessageDistribution(receivedId);
        }
    }
}
