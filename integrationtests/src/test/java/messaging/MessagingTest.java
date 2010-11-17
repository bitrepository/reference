package messaging;

import javax.jms.JMSException;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

import dk.bitmagasin.client.ClientStub;
import dk.bitmagasin.pillar.TestPillar;

public class MessagingTest {

	@Test(groups = { "regressiontest", "infrastructuretest" })
	public void pillarCreationTest() throws JMSException {
		TestPillar pillar1 = new TestPillar("SB1");
		TestPillar pillar2 = new TestPillar("SB2");
		TestPillar pillar3 = new TestPillar("KB1");
		TestPillar pillar4 = new TestPillar("SA1");
	}
	
	@Test(groups = { "regressiontest", "infrastructuretest" })
	public void clientCreationTest() throws JMSException {
		ClientStub client = new ClientStub();
	}
	
	/**
	 * This is current just a example of how to describe a messaging sequence 
	 * through a test sequence. 
	 * 
	 * This method doesn't really do anything, and  hasn't been annotated as a test.
	 */
	public void clientMessageTestExample() throws Exception {
		TestPillar pillar1 = new TestPillar("SB1");
		ClientStub client = new ClientStub();
		
		client.sendGetData( null, null, "SB1" );
		
		assert("ClientMessage" == pillar1.readMessageBlocking());
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
