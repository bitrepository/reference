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

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
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
 * <p>
 * The size is regulated by the 'BUFFER_TEXT' and the 'NUMBER_OF_REPEATS_OF_BUFFER_TEXT'.
 * Currently, the buffer text is 100 bytes, and it is repeated 100 times, thus generating a message of size 10 kB.
 */
public class MessageBusSizeOfMessageStressTest extends ExtendedTestCase {
    private static String QUEUE = "TEST-QUEUE";
    private final long TIME_FRAME = 60000L;
    private Settings settings;

    @BeforeMethod
    public void initializeSettings() {
        settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
    }

    /**
     * Tests the amount of messages sent over a message bus, which is not placed locally.
     * Requires sending at least five per second.
     */
    /* @Test( groups = {"StressTest"} ) */
    public void SendLargeMessagesDistributed() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfiguration conf = MessageBusConfigurationFactory.createDefaultConfiguration();
        ResendMessageListener listener = null;

        try {
            addStep("Initialise the message-listener", "Should be allowed.");
            listener = new ResendMessageListener();

            AlarmMessage message = getTestMessage();

            addStep("Start sending at '" + new Date() + "'", "Should just be waiting.");
            listener.startSending(message);
            synchronized (this) {
                try {
                    wait(TIME_FRAME);
                } catch (InterruptedException e) {
                    /* e.printStackTrace(); */
                }
            }

            addStep("Validating messages have been sent.", "Should be OK");
            int count = listener.getCount();
            Assert.assertTrue(count > 0, "Some message should have been sent.");
            System.out.println("Sent '" + count + "' messages in '" + TIME_FRAME / 1000 + "' seconds.");
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
    @Test(groups = {"StressTest"})
    public void SendLargeMessagesLocally() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus and define the local broker.", "Both should be created.");
        MessageBusConfiguration conf = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
        Assert.assertNotNull(conf);
        LocalActiveMQBroker broker = new LocalActiveMQBroker(conf);
        Assert.assertNotNull(broker);

        ResendMessageListener listener = null;

        try {
            addStep("Starting the broker.", "Should be allowed");
            broker.start();

            addStep("Initialise the message-listener", "Should be allowed.");
            listener = new ResendMessageListener();

            AlarmMessage message = getTestMessage();

            addStep("Start sending at '" + new Date() + "'", "Should just be waiting.");
            listener.startSending(message);
            synchronized (this) {
                try {
                    wait(TIME_FRAME);
                } catch (InterruptedException e) {
                    /* e.printStackTrace(); */
                }
            }

            addStep("Validating the number of messages sent.", "Should be OK");
            int count = listener.getCount();
            System.out.println("Sent '" + count + "' messages in '" + TIME_FRAME / 1000 + "' seconds.");
        } finally {
            if (listener != null) {
                listener.stop();
            }
            broker.stop();
        }
    }

    private AlarmMessage getTestMessage() throws Exception {
        addStep("Creating the payload of the message.", "should be OK.");
        StringBuilder payload = new StringBuilder();
        /* The number of repeats by the buffer text in the message.*/
        int NUMBER_OF_REPEATS_OF_BUFFER_TEXT = 100;
        for (int i = 0; i < NUMBER_OF_REPEATS_OF_BUFFER_TEXT; i++) {
            /* The text to repeat to make the message large (100 bytes).*/
            String BUFFER_TEXT = "098765432109876543210987654321"
                    + "0987654321098765432109876543210987654321098765432109876543210987654321";
            payload.append(BUFFER_TEXT);
        }

        addStep("Creating a message of size '" + payload.length() + "' bytes", "Should be allowed");
        AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
        Alarm alarm = new Alarm();
        alarm.setAlarmText(payload.toString());
        alarm.setAlarmRaiser("test");
        alarm.setAlarmCode(AlarmCode.INVALID_MESSAGE);
        alarm.setOrigDateTime(CalendarUtils.getEpoch());
        message.setAlarm(alarm);
        return message;
    }

    /**
     * Message-listener which only resends the messages it receives.
     * It does not reply, it sends to the same destination, thus receiving it again.
     * It keeps track of the amount of messages received.
     */
    private class ResendMessageListener implements MessageListener {
        private final MessageBus bus;
        private int count;

        public ResendMessageListener() {
            /* Mocked SecurityManager */
            SecurityManager securityManager = new DummySecurityManager();
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
         *
         * @return The number of message received by this.
         */
        public int getCount() {
            return count;
        }

        /**
         * Starts sending messages.
         */
        public void startSending(AlarmMessage message) {
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
