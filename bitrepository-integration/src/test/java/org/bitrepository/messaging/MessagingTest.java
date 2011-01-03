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
package org.bitrepository.messaging;

import javax.jms.JMSException;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

import org.bitrepository.client.ClientStub;
import org.bitrepository.pillar.TestPillar;

public class MessagingTest {	

	@Test(groups = { "regressiontest", "infrastructuretest" })
	public void pillarCreationTest() throws JMSException {
		new TestPillar("SB1");
		new TestPillar("SB2");
		new TestPillar("KB1");
		new TestPillar("SA1");
	}
	
	@Test(groups = { "regressiontest", "infrastructuretest" })
	public void clientCreationTest() throws JMSException {
		new ClientStub();
	}
	
	/**
	 * Test that the getData operation on a pillar returns the correct result.
	 */
	@Test(groups = { "regressiontest", "infrastructuretest" })
	public void getDataTestMessageTest() throws Exception {
		TestPillar pillar1 = new TestPillar("SB1");
		ClientStub client = new ClientStub();
		
		client.sendGetData("SB1");
		
		assertEquals("ClientMessage", pillar1.readMessageBlocking());
	}
	
	/**
	 * This is current just a example of how to create message data through 
	 * factories.
	 * 
	 * This method doesn't really do anything, and hasn't been annotated as a test.
	 */
	public void clientMessageFactoryExample() throws Exception {
		TestPillar pillar1 = new TestPillar("SB1");
		ClientStub client = new ClientStub();
		
		String singlePillarGetTimeMessage = MessageFactory.createMessage("SinglePillarGetTime");
		client.send(singlePillarGetTimeMessage);
		assertEquals(singlePillarGetTimeMessage, pillar1.readMessageBlocking());
		
		GetTimeResponseMessage singlePillarGetTimeResponseMessage =
			GetMessageResponseFactory.createMessage("SinglePillarGetTimeResponse");
		pillar1.sendMessage(singlePillarGetTimeResponseMessage.getXml());
	}
	
	public void jacceptTestExample() {
		//See http://sbforge.statsbiblioteket.dk/pages/viewpage.action?pageId=4161821
	}
}
