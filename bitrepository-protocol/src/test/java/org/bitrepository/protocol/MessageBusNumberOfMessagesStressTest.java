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

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Stress testing of the messagebus. 
 */
public class MessageBusNumberOfMessagesStressTest extends ExtendedTestCase {
    /** The time to wait when sending a message before it definitely should 
     * have been consumed by a listener.*/
    static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 500;
    /** The name of the queue to send the messages.*/
    private static String QUEUE = "TEST-QUEUE";

    /**
     * Tests the amount of messages send over a message bus, which is not placed locally.
     * Requires to send at least five per second.
     * @throws Exception 
     */
    @Test( groups = {"StressTest"} )
    public void SendManyMessagesDistributed() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");
        long timeFrame = 60000L; // one minute in millis
        long messagePerSec = 5;
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfigurations confs = MessageBusConfigurationFactory.createDefaultConfiguration();
        ResendMessageListener listener = null;

        try {
            addStep("Initialise the messagelistener", "Should be allowed.");
            listener = new ResendMessageListener(confs);

            addStep("Start sending at '" + new Date() + "'", "Should just be waiting.");
            listener.startSending();
            synchronized (this) {
                try {
                    wait(timeFrame);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            addStep("Stopped sending at '" + new Date() + "'", "Should have send more than '" + messagePerSec 
                    + "' messages per sec.");
            int count = listener.getCount();
            Assert.assertTrue(count > (messagePerSec * timeFrame/1000), "There where send '" + count 
                    + "' messages in '" + timeFrame/1000 + "' seconds, but it is required to handle at least '" 
                    + messagePerSec + "' per second!");
            System.out.println("Sent '" + count + "' messages in '" + timeFrame/1000 + "' seconds.");
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
    public void SendManyMessagesLocally() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe.");
        addStep("Define constants", "This should not be possible to fail.");
        long timeFrame = 60000L; // one minute in millis
        long messagePerSec = 20;
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus and define the local broker.", "Both should be created.");
        MessageBusConfigurations confs = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
        Assert.assertNotNull(confs);
        LocalActiveMQBroker broker = new LocalActiveMQBroker(confs.getPrimaryMessageBusConfiguration());
        Assert.assertNotNull(broker);

        ResendMessageListener listener = null;

        try {
            addStep("Starting the broker.", "Should be allowed");
            broker.start();

            addStep("Initialise the messagelistener", "Should be allowed.");
            listener = new ResendMessageListener(confs);

            addStep("Start sending at '" + new Date() + "'", "Should just be waiting.");
            listener.startSending();
            synchronized (this) {
                try {
                    wait(timeFrame);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            addStep("Stopped sending at '" + new Date() + "'", "Should have send more than '" + messagePerSec 
                    + "' messages per sec.");
            int count = listener.getCount();
            Assert.assertTrue(count > (messagePerSec * timeFrame/1000), "There where send '" + count 
                    + "' messages in '" + timeFrame/1000 + "' seconds, but it is required to handle at least '" 
                    + messagePerSec + "' per second!");
            System.out.println("Sent '" + count + "' messages in '" + timeFrame/1000 + "' seconds.");
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
        public ResendMessageListener(MessageBusConfigurations confs) {
            this.bus = new ActiveMQMessageBus(confs);
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
        public void startSending() throws Exception {
            Alarm message = ExampleMessageFactory.createMessage(Alarm.class);
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
