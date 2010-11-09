package messaging;

import javax.jms.JMSException;

import org.testng.annotations.Test;

import dk.bitmagasin.pillar.TestPillar;

public class MessagingTest {

	@Test(groups = { "regressiontest", "infrastructuretest" })
	public void pillarCreationTest() throws JMSException {
		TestPillar pillar1 = new TestPillar("SB1");
		TestPillar pillar2 = new TestPillar("SB2");
		TestPillar pillar3 = new TestPillar("KB1");
		TestPillar pillar4 = new TestPillar("SA1");
	}
}
