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

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageBusTest extends ExtendedTestCase {
	/** The time to wait when sending a message before it definitely should 
	 * have been consumed by a listener.*/
	static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 100;
	
	@Test(groups = { "testfirst" })
	public void messageBusConnectionTest() throws JMSException {
		addDescription("Verifies that we are able to connect to the message bus");
		addStep("Get a connection to the message bus from the <i>MessageBusConnection</i> connection class",
				"No exceptions should be thrown");
		Assert.assertNotNull(ConnectionFactory.getInstance());
	}
	
	@Test(groups = { "functest" })
	public void standaloneListenerTest() {
		addDescription("Tests whether it is possible to start a messagelistener," +
				"who puts up a message on the bus, and then takes it down again.");
		try {
			new TestSingleMessage();
		} catch (Exception e) {
			Assert.fail("Should not throw an exception: ", e);
		}
	}
	
	@Test(groups = { "functest" })
	public void busActivityTest() {
		addDescription("Tests whether it is possible to create a message listener," +
				"and then set it to listen to the topic. Then puts a message" +
				"on the topic for the message listener to find, and" +
				"tests whether it finds the correct message.");
		
		String content = "Content of message for busActivityTest";
		try {
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
			
		} catch (Exception e) {
			Assert.fail("Should not throw an exception: ", e);
		}
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
	
	protected class TestSingleMessage implements MessageListener, 
	        ExceptionListener {
		private String message = null;
		public TestSingleMessage() throws JMSException {
			String messageContent = "Content of test message!";
			System.out.println("Connecting to bus!");
			MessageBusConnection con = ConnectionFactory.getInstance();
			
			con.addListener("SLA-TEST", this);
			
			con.sendMessage("SLA-TEST", messageContent);

			synchronized(this) {
				try {
					wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// Tests whether the message is successfully received.
			Assert.assertEquals(message, messageContent);
		}
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
	};

}
