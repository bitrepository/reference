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
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Stress testing of the messagebus.
 */
public class MessageBusTimeToSendMessagesStressTest extends ExtendedTestCase {
    /** The time to wait when sending a message before it definitely should
     * have been consumed by a listener.*/
    static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 500;
    /** The name of the queue to send the messages.*/
    private static String QUEUE = "TEST-QUEUE";
    /** The number of messages to send.*/
    private static final int NUMBER_OF_MESSAGES = 1000;
    /** The date for start sending the messages.*/
    private static Date startSending;
    private Settings settings;

    @BeforeMethod
    public void initializeSettings() {
        settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
    }
    /**
     * Tests the amount of messages sent over a message bus, which is not placed locally.
     * Require sending at least five per second.
     */
    /* @Test( groups = {"StressTest"} ) */
    public void SendManyMessagesDistributed() {
        addDescription("Tests how fast a given number of messages can be handled.");
        addStep("Define constants", "This should not be possible to fail.");
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfiguration conf = MessageBusConfigurationFactory.createDefaultConfiguration();
        SecurityManager securityManager = new DummySecurityManager();
        CountMessagesListener listener = null;

        try {
            addStep("Initialise the message-listener", "Should be allowed.");
            listener = new CountMessagesListener(securityManager);

            startSending = new Date();
            addStep("Start sending at '" + startSending + "'", "Should just be waiting.");
            sendAllTheMessages(conf, securityManager);

            addStep("Sleep until the listeners have received all the messages.", "Should be sleeping.");
            while(!listener.isFinished()) {
                synchronized (this) {
                    try {
                        wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
                    } catch (InterruptedException e) {
                        /* e.printStackTrace(); */
                    }
                }
            }

            Date endDate = listener.getStopSending();
            addStep("Validating the count. Started at '" + startSending + "' and ended at '"
                    + endDate + "'", "Should not be wrong.");

            int count = listener.getCount();
            long timeFrame = (endDate.getTime() - startSending.getTime())/1000;
            System.out.println("Sent '" + count + "' messages in '" + timeFrame + "' seconds.");
        } finally {
            if(listener != null) {
                listener.stop();
            }
        }
    }

    /**
     * Tests the amount of messages sent through a local messagebus.
     * It should be at least 20 per second.
     */
    @Test( groups = {"StressTest"} )
    public void SendManyMessagesLocally() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus and define the local broker.", "Both should be created.");
        MessageBusConfiguration conf = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
        Assert.assertNotNull(conf);
        LocalActiveMQBroker broker = new LocalActiveMQBroker(conf);
        Assert.assertNotNull(broker);

        CountMessagesListener listener = null;
        SecurityManager securityManager = new DummySecurityManager();

        try {
            addStep("Starting the broker.", "Should be allowed");
            broker.start();

            addStep("Initialise the message-listener", "Should be allowed.");
            listener = new CountMessagesListener(securityManager);

            startSending = new Date();
            addStep("Start sending at '" + startSending + "'", "Should just be waiting.");
            sendAllTheMessages(conf, securityManager);

            addStep("Sleep until the listeners has received all the messages.", "Should be sleeping.");
            long startTime = new Date().getTime();
            long oneMinuteInMillis = 60000;
            while(!listener.isFinished() && (new Date().getTime() - startTime) < oneMinuteInMillis) {
                synchronized (this) {
                    try {
                        wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            addStep("Validating the count. Started at '" + startSending + "' and ended at '"
                    + listener.getStopSending() + "'", "Should not be wrong.");
            int count = listener.getCount();
            long timeFrame = (listener.getStopSending().getTime() - startSending.getTime())/1000;
            System.out.println("Sent '" + count + "' messages in '" + timeFrame + "' seconds.");
        } finally {
            if(listener != null) {
                listener.stop();
            }
            broker.stop();
        }
    }

    /**
     * Sends the wanted amount of messages.
     * @param conf The configuration for the messagebus, where the messages should be sent.
     */
    private void sendAllTheMessages(MessageBusConfiguration conf, SecurityManager securityManager) {
        /* The number of threads to send the messages. */
        int NUMBER_OF_SENDERS = 10;
        for(int i = 0; i < NUMBER_OF_SENDERS; i++) {
            Thread t = new MessageSenderThread(conf, securityManager, NUMBER_OF_MESSAGES / NUMBER_OF_SENDERS, "#" + i);
            t.start();
        }
    }

    private class MessageSenderThread extends Thread {
        private final MessageBus bus;
        private final int numberOfMessages;
        private final String id;

        public MessageSenderThread(MessageBusConfiguration conf, SecurityManager securityManager, int numberOfMessages, String id) {
            this.bus = new ActiveMQMessageBus(settings, securityManager);
            this.numberOfMessages = numberOfMessages;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
                message.setDestination(QUEUE);
                for(int i = 0; i < numberOfMessages; i++) {
                    message.setCorrelationID(id + ":" + i);
                    bus.sendMessage(message);

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Message-listener which only resends the messages it receives.
     * It does not reply, it sent to the same destination, thus receiving it again.
     * It keeps track of the amount of messages received.
     */
    private class CountMessagesListener implements MessageListener {
        private final MessageBus bus;
        private int count;

        private boolean awaitingMore = true;

        private Date stopSending;

        public CountMessagesListener(SecurityManager securityManager) {
            this.bus = new ActiveMQMessageBus(settings, securityManager);
            this.count = 0;

            bus.addListener(QUEUE, this);
        }

        /**
         * Method for stopping interaction with the message-listener.
         */
        public void stop() {
            bus.removeListener(QUEUE, this);
        }

        /**
         * Retrieval of the amount of messages caught by the listener.
         * @return The number of message received by this.
         */
        public int getCount() {
            return count;
        }

        @Override
        public void onMessage(Message message, MessageContext messageContext) {
            count++;
            if(count >= NUMBER_OF_MESSAGES) {
                stopSending = new Date();
                awaitingMore = false;
            }
        }

        public Date getStopSending() {
            return stopSending;
        }

        public boolean isFinished() {
            return !awaitingMore;
        }
    }
}
