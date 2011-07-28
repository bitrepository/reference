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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
 * 
 * TODO Important note for testers:
 * The number of listeners should be regulated through the 'NUMBER_OF_LISTENERS' constant.
 * When using many listeners, the DEFAULT_WAIT_TIME should be increased, e.g. 5000 for 25 listeners 
 * and 15000 for 100 listeners. 
 * Otherwise it is not ensured, that all the messagelisteners will receive all the messages before the validation.
 * 
 * Also, the shutdown of the messagelisteners can generate some noise, which will make it impossible to retrieve the
 * output data from the console. Therefore the results can be written to a file after the test. 
 * This is controlled through the variables 'WRITE_RESULTS_TO_FILE', which deternimes whether to write to the file, and
 * 'OUTPUT_FILE_NAME' which is the name of the file to write the output results.
 * 
 */
public class MessageBusNumberOfListenersStressTest extends ExtendedTestCase {
    /** The queue name.*/
    private static String QUEUE = "TEST-LISTENERS";
    /** The default time to wait for a simple communication.*/
    private static long DEFAULT_WAIT_TIME = 500;

    /** The time for the whole test.*/
    private static long TIME_FRAME = 60000L;
    /** The number of message listeners in the test.*/
    private static int NUMBER_OF_LISTENERS = 10;

    /** Whether the results will be written to a file.*/
    private static final boolean WRITE_RESULTS_TO_FILE = false;
    /** The name of the output file for the results of the tests.*/
    private static final String OUTPUT_FILE_NAME = "NumberOfListeners-results.test";

    /** The reached correlation ID for the message.*/
    private static int idReached = -1;

    /** The message to send back and forth over the message bus.*/
    private static Alarm alarmMessage;

    /** The message bus instance for sending the messages.*/
    private static MessageBus bus;

    /** The amount of messages received.*/
    private static int messageReceived = 0;

    /** Whether more messages should be send.*/
    private static boolean sendMoreMessages = true;

    /**
     * Tests the amount of messages send over a message bus, which is not placed locally.
     * Requires to send at least five per second.
     * @throws Exception 
     */
    @Test( groups = {"StressTest"} )
    public void testManyListenersOnLocalMessageBus() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe when a given number of "
                + "listeners are receiving them.");
        addStep("Define constants", "This should not be possible to fail.");
        QUEUE += "-" + (new Date()).getTime();
        messageReceived = 0;
        idReached = -1;
        sendMoreMessages = true;

        addStep("Define the message to send.", "Should retrieve the Alarm message from examples and set the To.");
        alarmMessage = ExampleMessageFactory.createMessage(Alarm.class);
        alarmMessage.setTo(QUEUE);

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfigurations confs = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
        LocalActiveMQBroker broker = new LocalActiveMQBroker(confs.getPrimaryMessageBusConfiguration());

        try {
            addStep("Start the broker and initialise the listeners.", 
            "Connections should be established.");
            broker.start();
            bus = new ActiveMQMessageBus(confs);

            testListeners(confs);
        } finally {
            if(broker != null) {
                broker.stop();
                broker = null;
            }
        }
    }

    @Test( groups = {"StressTest"} )
    public void testManyListenersOnDistributedMessageBus() throws Exception {
        addDescription("Tests how many messages can be handled within a given timeframe when a given number of "
                + "listeners are receiving them.");
        addStep("Define constants", "This should not be possible to fail.");
        QUEUE += "-" + (new Date()).getTime();
        messageReceived = 0;
        idReached = -1;
        sendMoreMessages = true;

        addStep("Define the message to send.", "Should retrieve the Alarm message from examples and set the To.");
        alarmMessage = ExampleMessageFactory.createMessage(Alarm.class);
        alarmMessage.setTo(QUEUE);

        addStep("Make configuration for the messagebus.", "Both should be created.");
        MessageBusConfigurations confs = MessageBusConfigurationFactory.createDefaultConfiguration();

        addStep("Start the broker and initialise the listeners.", 
        "Connections should be established.");
        bus = new ActiveMQMessageBus(confs);

        testListeners(confs);
    }


    public void testListeners(MessageBusConfigurations confs) throws Exception {
        List<NotificationMessageListener> listeners = new ArrayList<NotificationMessageListener>(NUMBER_OF_LISTENERS);

        try {
            addStep("Initialise the message listeners.", "Should be created and connected to the message bus.");
            for(int i = 0; i < NUMBER_OF_LISTENERS; i++) {
                listeners.add(new NotificationMessageListener(confs));
            }

            addStep("Wait for setup", "We wait!");
            synchronized (this) {
                try {
                    wait(DEFAULT_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            addStep("Send the first message", "Message should be send.");
            sendMessageWithId(1);

            addStep("Wait for the timeframe on '" + TIME_FRAME + "' milliseconds.", 
            "We wait!");
            synchronized (this) {
                try {
                    wait(TIME_FRAME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            addStep("Stop sending more messages and await all the messages to be received by all the listeners", 
            "Should be Ok");
            sendMoreMessages = false;
            synchronized (this) {
                try {
                    wait(DEFAULT_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            addStep("Verifying the amount of message sent '" + idReached + "' has been received by all '" 
                    + NUMBER_OF_LISTENERS + "' listeners", "Should be the same amount for each listener, and the same "
                    + "amount as the correlation ID of the message");
            Assert.assertTrue(idReached * NUMBER_OF_LISTENERS == messageReceived, 
                    "Reached message Id " + idReached + " thus each message of the " + NUMBER_OF_LISTENERS + " listener "
                    + "should have received " + idReached + " message, though they have received " 
                    + messageReceived + " message all together.");
            for(NotificationMessageListener listener : listeners) {
                Assert.assertTrue((listener.getCount() == idReached), 
                        "Should have received " + idReached + " messages, but has received " 
                        + listener.getCount());
            }

            // If too many messagelisteners, then they will create so much noise, that the results cannot be read from
            // the console output (due to shutdown 'warnings'). Thus write the results in a file.
            if(WRITE_RESULTS_TO_FILE) {
                FileOutputStream out = new FileOutputStream(new File(OUTPUT_FILE_NAME), true);
                out.write(new String("idReached: " + idReached + ", NumberOfListeners: " + NUMBER_OF_LISTENERS 
                        + ", messagesReceived: " + messageReceived + " on bus " 
                        + confs.getPrimaryMessageBusConfiguration().getUrl() + "\n").getBytes());
                out.flush();
                out.close();
            } 
        } finally {
            if(listeners != null) {
                for(NotificationMessageListener listener : listeners) {
                    listener.stop();
                }
                listeners.clear();
                listeners = null;
            }
            synchronized (this) {
                try {
                    wait(DEFAULT_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Method for sending the Alarm message with a specific ID.
     * 
     * @param id The correlation id for the message to send.
     */
    private static void sendMessageWithId(int id) {
        if(sendMoreMessages) {
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
        if(receivedId > idReached) {
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
    private class NotificationMessageListener extends AbstractMessageListener {
        /** The message bus.*/
        private final MessageBus bus;
        /** The amount of messages received.*/
        private int count;

        /**
         * Constructor.
         * @param confs The configurations for declaring the messagebus.
         */
        public NotificationMessageListener(MessageBusConfigurations confs) {
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
            int receivedId = Integer.parseInt(message.getCorrelationID());
            handleMessageDistribution(receivedId);
        }
    }
}
