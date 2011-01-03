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
package org.bitrepository.bus;

import javax.jms.JMSException;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

import org.bitrepository.messagebus.MessageBusConnection;

public class MessageBusTest extends ExtendedTestCase {
	
	@Test(groups = { "testfirst" })
	public void messageBusConnectionTest() throws JMSException {
		addDescription("Verifies that we are able to connect to the message bus");
		addStep("Get a connection to the message bus from the <i>MessageBusConnection</i> connection class",
				"No exceptions should be thrown");
		MessageBusConnection.getMessageBusConnection();
	}
	
	
	@Test(groups = { "specificationonly" })
	public void twoMessageBusConnectionTest() {
		addDescription("Verifies that we are switch to a second message bus");
	}
	
	@Test(groups = { "specificationonly" })
	public void messageBusFailoverTest() {
		addDescription("Verifies that we can switch to at second message bus " +
				"in the middle of a conversation, if the connection is lost. " +
				"We should also be able to resume the conversation on the new " +
				"message bus");
	}
	
	@Test(groups = { "specificationonly" })
	public void messageBusReconnectTest() {
		addDescription("Test whether we are able to reconnect to the message " +
				"bus if the connection is lost");
	}
}
