/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id: MessageBusTest.java 49 2011-01-03 08:48:13Z mikis $
 * $HeadURL: https://gforge.statsbiblioteket.dk/svn/bitmagasin/trunk/bitrepository-integration/src/test/java/org/bitrepository/bus/MessageBusTest.java $
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

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.broker.BrokerService;
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
    static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 100;

    @Test(groups = { "testfirst" })
    public void messageBusConnectionTest() throws JMSException {
        addDescription("Verifies that we are able to connect to the message bus");
        addStep("Get a connection to the message bus from the "
                + "<i>MessageBusConnection</i> connection class",
        "No exceptions should be thrown");
        Assert.assertNotNull(ConnectionFactory.getInstance());
    }

    @Test(groups = { "functest" })
    public void busActivityTest() throws Exception {
        addDescription("Tests whether it is possible to create a message listener," +
                "and then set it to listen to the topic. Then puts a message" +
                "on the topic for the message listener to find, and" +
        "tests whether it finds the correct message.");

        String content = "Content of message for busActivityTest";
        TestMessageListener listener = new TestMessageListener();
        MessageBusConnection con = ConnectionFactory.getInstance();
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
        Assert.assertEquals(listener.getMessage(), content);
    }

    //	@Test(groups = { "specificationonly" })
    public void twoMessageBusConnectionTest() {
        addDescription("Verifies that we are switch to a second message bus");

        ConnectionFactory.getInstance();
        Assert.assertTrue(ConnectionFactory.hasNextConnection(), 
        "Should have the properties for another connection.");
        // TODO make the other connection.
    }

    //	@Test(groups = { "specificationonly" })
    public void messageBusFailoverTest() {
        addDescription("Verifies that we can switch to at second message bus " +
                "in the middle of a conversation, if the connection is lost. " +
                "We should also be able to resume the conversation on the new " +
        "message bus");
    }

    //	@Test(groups = { "specificationonly" })
    public void messageBusReconnectTest() {
        addDescription("Test whether we are able to reconnect to the message " +
        "bus if the connection is lost");
    }

    @Test(groups = {"functest", "connection"})
    public void localBrokerTest() throws Exception {
        addDescription("Tests the possibility for starting the broker locally,"
                + " and using it for communication by sending a simple message"
                + " over it and verifying that the corresponding message is "
                + "received.");
        String content = "Content of localBrokerTest message";
        
        addStep("Starting the local broker.", "A lot of info-level logs should"
                + " be seen here.");
        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://localhost:61616");
        broker.start();

        try {
            addStep("Connecting to the bus, and then connect to the local bus.", 
                    "Info-level logs should be seen here for both connections. "
                    + "Only the last is used.");
            MessageBusConnection con = ConnectionFactory.getInstance();
            con = ConnectionFactory.getNext();

            addStep("Make a listener for the messagebus and make it listen. "
                    + "Then send a message for the message listener to catch.",
                    "several DEBUG-level logs");
            TestMessageListener listener = new TestMessageListener();
            con.addListener("EmbeddedBrokerTopic", listener);
            con.sendMessage("EmbeddedBrokerTopic", content);

            synchronized(this) {
                try {
                    this.wait(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Assert.assertNotNull(listener.getMessage(), "A message should be "
                    + "received.");
            Assert.assertEquals(content, listener.getMessage());

            con.removeListener("EmbeddedBrokerTopic", listener);
        } finally {
            broker.stop();
        }
    }

    protected class TestMessageListener implements MessageListener, 
    ExceptionListener {
        private String message = null;
        @Override
        public void onMessage(Message msg) {
            Assert.assertTrue(msg instanceof TextMessage);
            try {
                message = ((TextMessage) msg).getText();
            } catch (Exception e) {
                Assert.fail("Should not throw an exception: ", e);
            }
        }
        @Override
        public void onException(JMSException e) {
            e.printStackTrace();
        }
        public String getMessage() {
            return message;
        }
    }
}
