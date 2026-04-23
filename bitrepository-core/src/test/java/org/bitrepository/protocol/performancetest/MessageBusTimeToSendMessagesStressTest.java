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

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

/**
 * Stress testing of the messagebus.
 */
class MessageBusTimeToSendMessagesStressTest {
    /** The time to wait when sending a message before it definitely should
     * have been consumed by a listener.*/
    static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 500;
    /**
     * The number of messages to send.
     */
    private static final int NUMBER_OF_MESSAGES = 1000;
    private Settings settings;
    private String testQueue;

    @BeforeEach
    void initializeSettings() {
        settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
        testQueue = "TEST-QUEUE-" + System.currentTimeMillis();
    }

    /**
     * Tests the amount of messages sent over a message bus, which is not placed locally.
     * Require sending at least five per second.
     */
    @Test
    @Tag("StressTest")
    void SendManyMessagesDistributed() {
        addDescription("Tests how fast a given number of messages can be handled.");
        addStep("Define constants", "This should not be possible to fail.");

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfiguration conf = MessageBusConfigurationFactory.createDefaultConfiguration();
        SecurityManager securityManager = new DummySecurityManager();
        CountMessagesListener listener = null;

        try {
            addStep("Initialise the message-listener", "Should be allowed.");
            listener = new CountMessagesListener(securityManager);

            Instant startSending = Instant.now();
            addStep("Start sending at '" + startSending.atZone(ZoneId.systemDefault()) + "'",
                    "Should just be waiting.");
            sendAllTheMessages(conf, securityManager);

            addStep("Sleep until the listeners have received all the messages.",
                    "Should be sleeping.");
            while (!listener.isFinished()) {
                try {
                    Thread.sleep(TIME_FOR_MESSAGE_TRANSFER_WAIT);
                } catch (InterruptedException e) {
                    Assertions.fail(e);
                }
            }

            Instant messageStopTime = listener.getStopSending();
            addStep("Validating the count. Started at '" + startSending.atZone(ZoneId.systemDefault()) + "' and ended at '"
                    + messageStopTime.atZone(ZoneId.systemDefault()) + "'", "Should not be wrong.");

            int count = listener.getCount();
            long timeFrame = ChronoUnit.SECONDS.between(startSending, messageStopTime);
            System.out.println("Sent '" + count + "' messages in '" + timeFrame + "' seconds.");
        } finally {
            if (listener != null) {
                listener.stop();
            }
        }
    }

    /**
     * Tests the amount of messages sent through a local messagebus.
     * It should be at least 20 per second.
     */
    @Test
    @Tag("StressTest")
    void SendManyMessagesLocally() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");

        addStep("Make configuration for the messagebus and define the local broker.",
                "Both should be created.");
        MessageBusConfiguration conf = new MessageBusConfiguration();
        int port = getFreePort();
        conf.setURL("tcp://localhost:" + port);
        settings.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(conf);
        LocalActiveMQBroker broker = new LocalActiveMQBroker(conf);
        Assertions.assertNotNull(broker);

        CountMessagesListener listener = null;
        SecurityManager securityManager = new DummySecurityManager();

        try {
            addStep("Starting the broker.", "Should be allowed");
            broker.start();

            addStep("Initialise the message-listener", "Should be allowed.");
            listener = new CountMessagesListener(securityManager);

            Instant startSending = Instant.now();
            addStep("Start sending at '" + startSending.atZone(ZoneId.systemDefault()) + "'",
                    "Should just be waiting.");
            sendAllTheMessages(conf, securityManager);

            addStep("Sleep until the listeners has received all the messages.",
                    "Should be sleeping.");
            long startTime = System.currentTimeMillis();
            long oneMinuteInMillis = 60000;
            while (!listener.isFinished() && (System.currentTimeMillis() - startTime) < oneMinuteInMillis) {
                try {
                    Thread.sleep(TIME_FOR_MESSAGE_TRANSFER_WAIT);
                } catch (InterruptedException e) {
                    Assertions.fail(e);
                }
            }

            addStep("Validating the count. Started at '" + startSending.atZone(ZoneId.systemDefault()) + "' and ended at '"
                    + listener.getStopSending().atZone(ZoneId.systemDefault()) + "'", "Should not be wrong.");
            int count = listener.getCount();
            long timeFrame = ChronoUnit.SECONDS.between(startSending, listener.getStopSending());
            System.out.println("Sent '" + count + "' messages in '" + timeFrame + "' seconds.");
        } finally {
            if (listener != null) {
                listener.stop();
            }
            broker.stop();
        }
    }

    /**
     * Finds a free port on the localhost.
     * @return A free port number.
     * @throws IOException If an I/O error occurs.
     */
    private int getFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /**
     * Sends the wanted amount of messages.
     *
     * @param conf The configuration for the messagebus, where the messages should be sent.
     */
    private void sendAllTheMessages(MessageBusConfiguration conf, SecurityManager securityManager) {
        /* The number of threads to send the messages. */
        int NUMBER_OF_SENDERS = 10;
        for (int i = 0; i < NUMBER_OF_SENDERS; i++) {
            Thread t = new MessageSenderThread(conf, securityManager, NUMBER_OF_MESSAGES / NUMBER_OF_SENDERS,
                    String.valueOf(i));
            t.start();
        }
    }

    private class MessageSenderThread extends Thread {
        private final MessageBus bus;
        private final int numberOfMessages;
        private final String id;

        public MessageSenderThread(MessageBusConfiguration conf, SecurityManager securityManager, int numberOfMessages, String id) {
            Settings senderSettings =
                    TestSettingsProvider.getSettings(MessageBusTimeToSendMessagesStressTest.class.getSimpleName() + "-" + id);
            senderSettings.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(conf);
            this.bus = new ActiveMQMessageBus(senderSettings, securityManager);
            this.numberOfMessages = numberOfMessages;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
                message.setDestination(testQueue);
                for (int i = 0; i < numberOfMessages; i++) {
                    message.setCorrelationID(id + ":" + i);
                    bus.sendMessage(message);

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    bus.close();
                } catch (javax.jms.JMSException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Message-listener which keeps track of the amount of messages received.
     */
    private class CountMessagesListener implements MessageListener {
        private final MessageBus bus;
        private int count;

        private boolean awaitingMore = true;

        private Instant stopSending;

        public CountMessagesListener(SecurityManager securityManager) {
            this.bus = new ActiveMQMessageBus(settings, securityManager);
            this.count = 0;

            bus.addListener(testQueue, this);
        }

        /**
         * Method for stopping interaction with the message-listener.
         */
        public void stop() {
            bus.removeListener(testQueue, this);
            try {
                bus.close();
            } catch (javax.jms.JMSException e) {
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
            if (count >= NUMBER_OF_MESSAGES) {
                stopSending = Instant.now();
                awaitingMore = false;
            }
        }

        public Instant getStopSending() {
            return stopSending;
        }

        public boolean isFinished() {
            return !awaitingMore;
        }
    }
}
