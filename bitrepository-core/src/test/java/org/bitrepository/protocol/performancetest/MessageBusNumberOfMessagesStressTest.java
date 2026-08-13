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

import jakarta.jms.JMSException;
import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.pillar.integration.ArtemisFixedPortContainer;
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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

/**
 * Stress testing of the messagebus.
 */
@EnabledIfSystemProperty(named = "runStressTests", matches = "true")
public class MessageBusNumberOfMessagesStressTest {
    /**
     * The name of the queue to send the messages.
     */
    private static String QUEUE = "TEST-QUEUE";
    private Settings settings;

    @BeforeEach
    public void initializeSettings() {
        settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
    }

    /**
     * Tests the amount of messages sent over a message bus, which is not placed locally.
     * Require sending at least five messages per second.
     */
    @Test
    @Tag(TestGroups.STRESS_TEST)
    public void SendManyMessagesDistributed() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");
        long timeFrame = 60000L; // one minute in millis
        long messagePerSec = 5;
        QUEUE += "-" + Instant.now().toEpochMilli();

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfiguration conf = new MessageBusConfiguration();
        int port = getFreePort();
        conf.setURL("tcp://localhost:" + port);
        settings.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(conf);
        LocalActiveMQBroker broker = new LocalActiveMQBroker(conf);
        ResendMessageListener listener = null;

        try {
            broker.start();
            addStep("Initialise the message-listener", "Should be allowed.");
            listener = new ResendMessageListener(settings);

            addStep("Start sending at '" + Instant.now() + "'", "Should just be waiting.");
            listener.startSending();
            synchronized (this) {
                try {
                    wait(timeFrame);
                } catch (InterruptedException e) {
                    Assertions.fail(e);
                }
            }

            addStep("Stopped sending at '" + OffsetDateTime.now(ZoneId.systemDefault()) + "'",
                    "Should have sent more than '" + messagePerSec + "' messages per sec.");
            int count = listener.getCount();
            Assertions.assertTrue(count > (messagePerSec * timeFrame / 1000), "There where send '" + count
                    + "' messages in '" + timeFrame / 1000 + "' seconds, but it is required to handle at least '"
                    + messagePerSec + "' per second!");
            System.out.println("Sent '" + count + "' messages in '" + timeFrame / 1000 + "' seconds.");
        } finally {
            if (listener != null) {
                listener.stop();
                listener = null;
            }
            broker.stop();
        }
    }

    /**
     * Tests the amount of messages send through a local messagebus.
     * It should be at least 20 per second.
     */
    @Test
    @Tag(TestGroups.STRESS_TEST)
    public void SendManyMessagesLocally() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");
        long timeFrame = 60000L; // one minute in millis
        long messagePerSec = 10;
        QUEUE += "-" + Instant.now().toEpochMilli();

        addStep("Make configuration for the messagebus and define the local broker.",
                "Both should be created.");
        settings.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(
                MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration()
        );
        LocalActiveMQBroker broker = new LocalActiveMQBroker(settings.getMessageBusConfiguration());
        Assertions.assertNotNull(broker);

        ResendMessageListener listener = null;

        try {
            addStep("Starting the broker.", "Should be allowed");
            broker.start();

            addStep("Initialise the message-listener", "Should be allowed.");
            listener = new ResendMessageListener(settings);

            addStep("Start sending at '" + Instant.now() + "'",
                    "Should just be waiting.");
            listener.startSending();
            synchronized (this) {
                try {
                    wait(timeFrame);
                } catch (InterruptedException e) {
                    Assertions.fail(e);
                }
            }

            addStep("Stopped sending at '" + OffsetDateTime.now(ZoneId.systemDefault()) + "'",
                    "Should have send more than '" + messagePerSec + "' messages per sec.");
            int count = listener.getCount();
            Assertions.assertTrue(count > (messagePerSec * timeFrame / 1000), "There where send '" + count
                    + "' messages in '" + timeFrame / 1000 + "' seconds, but it is required to handle at least '"
                    + messagePerSec + "' per second!");
            System.out.println("Sent '" + count + "' messages in '" + timeFrame / 1000 + "' seconds.");
        } finally {
            if (listener != null) {
                listener.stop();
            }
            broker.stop();
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
     * Messagelistener which only resends the messages it receive.
     * It does not reply, it send to the same destination, thus receiving it again.
     * It keeps track of the amount of messages received.
     */
    private static class ResendMessageListener implements MessageListener {
        /**
         * The message bus.
         */
        private final MessageBus bus;
        /**
         * The amount of messages received.
         */
        private int count;

        /**
         * Constructor.
         *
         * @param conf The configurations for declaring the message bus.
         */
        public ResendMessageListener(Settings conf) {
            /* The mocked SecurityManager */
            SecurityManager securityManager = new DummySecurityManager();
            this.bus = new ActiveMQMessageBus(conf, securityManager);
            this.count = 0;

            bus.addListener(QUEUE, this);
        }

        /**
         * Method for stopping interaction with the message-listener.
         */
        public void stop() {
            bus.removeListener(QUEUE, this);
            try {
                bus.close();
            } catch (JMSException e) {
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

        /**
         * Starts sending messages.
         *
         * @throws Exception If a problem with creating the message occurs.
         */
        public void startSending() throws Exception {
            AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
            message.setDestination(QUEUE);
            bus.sendMessage(message);
        }

        @Override
        public void onMessage(Message message, MessageContext messageContext) {
            count++;
            bus.sendMessage(message);
        }
    }
}
