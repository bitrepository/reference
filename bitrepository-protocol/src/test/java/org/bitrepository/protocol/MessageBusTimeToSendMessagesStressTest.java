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
public class MessageBusTimeToSendMessagesStressTest extends ExtendedTestCase {
    /** The time to wait when sending a message before it definitely should 
     * have been consumed by a listener.*/
    static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 500;
    /** The name of the queue to send the messages.*/
    private static String QUEUE = "TEST-QUEUE";
    /** The number of messages to send.*/
    private static int NUMBER_OF_MESSAGES = 10000;
    /** The number of threads to send the messages. */
    private static int NUMBER_OF_SENDERS = 10;

    /** The date for start sending the messages.*/
    private static Date startSending;

    /**
     * Tests the amount of messages send over a message bus, which is not placed locally.
     * Requires to send at least five per second.
     * @throws Exception 
     */
    //	@Test( groups = {"StressTest"} )
    public void SendManyMessagesDistributed() throws Exception {
        addDescription("Tests how fast a given number of messages can be handled.");
        addStep("Define constants", "This should not be possible to fail.");
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfigurations confs = MessageBusConfigurationFactory.createDefaultConfiguration();
        CountMessagesListener listener = null;

        try {
            addStep("Initialise the messagelistener", "Should be allowed.");
            listener = new CountMessagesListener(confs);

            startSending = new Date();
            addStep("Start sending at '" + startSending + "'", "Should just be waiting.");
            sendAllTheMessages(confs);

            addStep("Sleept untill the listerner has received all the messages.", "Should be sleeping.");
            while(!listener.isFinished()) {
                synchronized (this) {
                    try {
                        wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
        QUEUE += "-" + (new Date()).getTime();

        addStep("Make configuration for the messagebus and define the local broker.", "Both should be created.");
        MessageBusConfigurations confs = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
        Assert.assertNotNull(confs);
        LocalActiveMQBroker broker = new LocalActiveMQBroker(confs.getPrimaryMessageBusConfiguration());
        Assert.assertNotNull(broker);

        CountMessagesListener listener = null;

        try {
            addStep("Starting the broker.", "Should be allowed");
            broker.start();

            addStep("Initialise the messagelistener", "Should be allowed.");
            listener = new CountMessagesListener(confs);

            startSending = new Date();
            addStep("Start sending at '" + startSending + "'", "Should just be waiting.");
            sendAllTheMessages(confs);

            addStep("Sleept untill the listerner has received all the messages.", "Should be sleeping.");
            while(!listener.isFinished()) {
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
                listener = null;
            }
            if(broker != null) {
                broker.stop();
                broker = null;
            }
        }
    }

    /**
     * Sends the wanted amount of messages.
     * @param confs The configuration for the messagebus, where the messages should be sent.
     * @throws Exception
     */
    private void sendAllTheMessages(MessageBusConfigurations confs) throws Exception {
        for(int i = 0; i < NUMBER_OF_SENDERS; i++) {
            Thread t = new MessageSenderThread(confs, NUMBER_OF_MESSAGES / NUMBER_OF_SENDERS, "#" + i);
            t.start();
        }
    }

    private class MessageSenderThread extends Thread {
        private final MessageBus bus;
        private final int numberOfMessages;
        private final String id;

        public MessageSenderThread(MessageBusConfigurations confs, int numberOfMessages, String id) {
            this.bus = new ActiveMQMessageBus(confs);
            this.numberOfMessages = numberOfMessages;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                Alarm message = ExampleMessageFactory.createMessage(Alarm.class);
                message.setTo(QUEUE);
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
     * Messagelistener which only resends the messages it receive.
     * It does not reply, it send to the same destination, thus receiving it again.
     * It keeps track of the amount of messages received.
     */
    private class CountMessagesListener extends AbstractMessageListener {
        /** The message bus.*/
        private final MessageBus bus;
        /** The amount of messages received.*/
        private int count;

        private boolean awaitingMore = true;

        private Date stopSending;

        /**
         * Constructor.
         * @param confs The configurations for declaring the messagebus.
         */
        public CountMessagesListener(MessageBusConfigurations confs) {
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

        @Override
        public void onMessage(Alarm message) {
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
