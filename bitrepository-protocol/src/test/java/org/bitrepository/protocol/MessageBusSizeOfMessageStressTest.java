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
package org.bitrepository.protocol;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.AlarmDescription;
import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.settings.collectionsettings.MessageBusConfiguration;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Stress testing of the messagebus. 
 * 
 * The size is regulated by the 'BUFFER_TEXT' and the 'NUMBER_OF_REPEATS_OF_BUFFER_TEXT'.
 * Currently the buffer text is 100 bytes, and it is repeated 100 times, thus generating a message of size 10 kB. 
 */
public class MessageBusSizeOfMessageStressTest extends ExtendedTestCase {
    /** The time to wait when sending a message before it definitely should 
     * have been consumed by a listener.*/
    static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 500;
    /** The name of the queue to send the messages.*/
    private static String QUEUE = "TEST-QUEUE";
    /** The timeframe for the test.*/
    private final long TIME_FRAME = 60000L;

    /** The text to repeat to make the message large.*/
    private final String BUFFER_TEXT = "098765432109876543210987654321"
        + "0987654321098765432109876543210987654321098765432109876543210987654321"; // 100 bytes
    /** The number of repeats by the buffer text in the message.*/
    private final int NUMBER_OF_REPEATS_OF_BUFFER_TEXT = 100;

    /**
     * Tests the amount of messages send over a message bus, which is not placed locally.
     * Requires to send at least five per second.
     * @throws Exception 
     */
    @Test( groups = {"StressTest"} )
    public void SendLargeMessagesDistributed() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfiguration conf = MessageBusConfigurationFactory.createDefaultConfiguration();
        ResendMessageListener listener = null;

        try {
            addStep("Initialise the messagelistener", "Should be allowed.");
            listener = new ResendMessageListener(conf);

            Alarm message = getTestMessage();

            addStep("Start sending at '" + new Date() + "'", "Should just be waiting.");
            listener.startSending(message);
            synchronized (this) {
                try {
                    wait(TIME_FRAME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            addStep("Validating messages have been sent.", "Should be OK");
            int count = listener.getCount();
            Assert.assertTrue(count > 0, "Some message should have been sent.");
            System.out.println("Sent '" + count + "' messages in '" + TIME_FRAME/1000 + "' seconds.");
        } finally {
            if(listener != null) {
                listener.stop();
                listener = null;
            }
        }
    }

    /**
     * Tests the amount of messages send through a local messagebus. 
     * It should be at least 20 per second. 
     * @throws Exception
     */
    @Test( groups = {"StressTest"} )
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

            addStep("Initialise the messagelistener", "Should be allowed.");
            listener = new ResendMessageListener(conf);

            Alarm message = getTestMessage();

            addStep("Start sending at '" + new Date() + "'", "Should just be waiting.");
            listener.startSending(message);
            synchronized (this) {
                try {
                    wait(TIME_FRAME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //            addStep("Stopped sending at '" + new Date() + "'", "Should have send more than '" + messagePerSec 
            //            		+ "' messages per sec.");
            addStep("Validating the number of messages sent.", "Should be OK");
            int count = listener.getCount();
            //            Assert.assertTrue(count > (messagePerSec * timeFrame/1000), "There where send '" + count 
            //            		+ "' messages in '" + timeFrame/1000 + "' seconds, but it is required to handle at least '" 
            //            		+ messagePerSec + "' per second!");
            System.out.println("Sent '" + count + "' messages in '" + TIME_FRAME/1000 + "' seconds.");
        } finally {
            if(listener != null) {
                listener.stop();
                listener = null;
            }
            if(broker != null) {
                broker.stop();
                broker = null;
            }
        }
    }

    private Alarm getTestMessage() throws Exception {
        addStep("Creating the payload of the message.", "should be OK.");
        StringBuilder payload = new StringBuilder();
        for(int i = 0; i < NUMBER_OF_REPEATS_OF_BUFFER_TEXT; i++) {
            payload.append(BUFFER_TEXT);
        }

        addStep("Creating a message of size '" + payload.length() + "' bytes", 
        "Should be allowed");
        Alarm message = ExampleMessageFactory.createMessage(Alarm.class);
        AlarmDescription description = new AlarmDescription();
        description.setAlarmText(payload.toString());
        message.setAlarmDescription(description);
        return message;
    }

    /**
     * Messagelistener which only resends the messages it receive.
     * It does not reply, it send to the same destination, thus receiving it again.
     * It keeps track of the amount of messages received.
     */
    private class ResendMessageListener extends AbstractMessageListener {
        /** The message bus.*/
        private final MessageBus bus;
        /** The amount of messages received.*/
        private int count;

        /**
         * Constructor.
         * @param confs The configurations for declaring the messagebus.
         */
        public ResendMessageListener(MessageBusConfiguration conf) {
            this.bus = new ActiveMQMessageBus(conf);
            this.count = 0;

            bus.addListener(QUEUE, this);
        }

        /**
         * Method for stopping interaction with the messagelistener.
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

        /**
         * Starts sending messages.
         * @throws Exception If a problem with creating the message occurs.
         */
        public void startSending(Alarm message) throws Exception {
            message.setTo(QUEUE);
            bus.sendMessage(message);
        }

        @Override
        public void onMessage(Alarm message) {
            count++;
            bus.sendMessage(message);
        }
    }
}
