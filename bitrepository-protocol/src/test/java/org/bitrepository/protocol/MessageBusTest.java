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

import org.apache.activemq.broker.BrokerService;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.custommonkey.xmlunit.XMLAssert;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class for testing the interface with the message bus.
 * @author jolf
 */
public class MessageBusTest extends ExtendedTestCase {
    /** The time to wait when sending a message before it definitely should 
     * have been consumed by a listener.*/
    static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 500;

    @Test(groups = { "regressiontest" })
    public final void messageBusConnectionTest() {
        addDescription("Verifies that we are able to connect to the message bus");
        addStep("Get a connection to the message bus from the "
                + "<i>MessageBusConnection</i> connection class",
        "No exceptions should be thrown");
        Assert.assertNotNull(ProtocolComponentFactory.getInstance().getMessageBus());
    }

    @Test(groups = { "regressiontest" })
    public final void busActivityTest() throws Exception {
        addDescription("Tests whether it is possible to create a message listener," +
                "and then set it to listen to the topic. Then puts a message" +
                "on the topic for the message listener to find, and" +
        "tests whether it finds the correct message.");

        IdentifyPillarsForGetFileRequest content = TestMessageFactory.getTestMessage();
        TestMessageListener listener = new TestMessageListener();
        MessageBus con = ProtocolComponentFactory.getInstance().getMessageBus();
        Assert.assertNotNull(con);
        con.addListener("BusActivityTest", listener);
        con.sendMessage("BusActivityTest", content);

        synchronized(this) {
            try {
                wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Assert.assertNotNull(listener.getMessage());
        XMLAssert.assertXMLEqual(MessageFactory.extractMessage(content),
                                 MessageFactory
                                         .extractMessage(listener.getMessage()));
    }

    @Test(groups = { "test-first" })
    public final void twoListenersForTopic() throws Exception {
        addDescription("Verifies that two listeners on the same topic both receive the message");

        //Test data
        String TopicName = "BusActivityTest";
        IdentifyPillarsForGetFileRequest content = TestMessageFactory.getTestMessage();

        addStep("Make a connection to the message bus and add two listeners",
                "No exceptions should be thrown");
        MessageBus con = ProtocolComponentFactory.getInstance().getMessageBus();
        Assert.assertNotNull(con);

        TestMessageListener listener1 = new TestMessageListener();
        TestMessageListener listener2 = new TestMessageListener();
        con.addListener(TopicName, listener1);
        con.addListener(TopicName, listener2);

        addStep("Send a message to the topic",
                "No exceptions should be thrown");
        con.sendMessage(TopicName, content);
        synchronized(this) {
            try {
                wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        addStep("Make sure both listeners received the message",
                "Both listeners received the message, and it is identical");

        Assert.assertNotNull(listener1.getMessage());
        XMLAssert.assertXMLEqual(MessageFactory.extractMessage(content),
                                 MessageFactory
                                         .extractMessage(listener1.getMessage()));
        Assert.assertNotNull(listener2.getMessage());
        XMLAssert.assertXMLEqual(MessageFactory.extractMessage(content),
                                 MessageFactory
                                         .extractMessage(listener2.getMessage()));

    }

    @Test(groups = { "specificationonly" })
    public final void twoMessageBusConnectionTest() {
        addDescription("Verifies that we are switch to a second message bus. Awaiting introduction of robustness issue");
    }

    @Test(groups = { "specificationonly" })
    public final void messageBusFailoverTest() {
        addDescription("Verifies that we can switch to at second message bus " +
                "in the middle of a conversation, if the connection is lost. " +
                "We should also be able to resume the conversation on the new " +
        "message bus");
    }

    @Test(groups = { "specificationonly" })
    public final void messageBusReconnectTest() {
        addDescription("Test whether we are able to reconnect to the message " +
        "bus if the connection is lost");
    }

    /**
     * Temporary reverting to test-first, because of difficulty in changing messagebus at run time. This perhaps reflects that  
     * @throws Exception
     */
    @Test(groups = {"test-first", "connectiontest"})
    public final void localBrokerTest() throws Exception {
        addDescription("Tests the possibility for starting the broker locally,"
                + " and using it for communication by sending a simple message"
                + " over it and verifying that the corresponding message is "
                + "received.");
        IdentifyPillarsForGetFileRequest content = TestMessageFactory.getTestMessage();

        addStep("Starting the local broker.", "A lot of info-level logs should"
                + " be seen here.");
        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://localhost:61616");
        broker.start();

        try {
            addStep("Connecting to the bus, and then connect to the local bus.", 
                    "Info-level logs should be seen here for both connections. "
                    + "Only the last is used.");
            MessageBus con = ProtocolComponentFactory.getInstance().getMessageBus();
            //con = ConnectionFactory.getNextConnection();

            addStep("Make a listener for the messagebus and make it listen. "
                    + "Then send a message for the message listener to catch.",
                    "several DEBUG-level logs");
            TestMessageListener listener = new TestMessageListener();
            con.addListener("EmbeddedBrokerTopic", listener);
            con.sendMessage("EmbeddedBrokerTopic", content);

            synchronized(this) {
                try {
                    this.wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Assert.assertNotNull(listener.getMessage(), "A message should be "
                    + "received.");
            XMLAssert.assertEquals(MessageFactory.extractMessage(content),
                                   MessageFactory.extractMessage(listener.getMessage()));

            con.removeListener("EmbeddedBrokerTopic", listener);
        } finally {
            broker.stop();
        }
    }

    protected class TestMessageListener extends AbstractMessageListener {
        /** Container for a message, when it is received.*/
        private Object message = null;
        @Override
        public final void onMessage(IdentifyPillarsForGetFileRequest message) {
            try {
                this.message = message;
            } catch (Exception e) {
                Assert.fail("Should not throw an exception: ", e);
            }
        }

        /**
         * Retrieving the last message caught by this listener.
         * @return The last received message.
         */
        public final Object getMessage() {
            return message;
        }
    }
}
