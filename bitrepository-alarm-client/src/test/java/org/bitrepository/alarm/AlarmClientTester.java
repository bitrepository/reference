/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AccessComponentFactory.java 212 2011-07-05 10:04:10Z bam $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/AccessComponentFactory.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.alarm;

import java.io.PrintStream;
import java.math.BigInteger;

import org.apache.kahadb.util.ByteArrayOutputStream;
import org.bitrepository.alarm.handler.AlarmLoggingHandler;
import org.bitrepository.alarm.handler.MailingAlarmHandler;
import org.bitrepository.alarm_client.alarmclientconfiguration.AlarmConfiguration;
import org.bitrepository.alarm_client.alarmclientconfiguration.AlarmConfiguration.MailingConfiguration;
import org.bitrepository.bitrepositoryelements.AlarmDescription;
import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.protocol.ExampleMessageFactory;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the AlarmClient.
 */
public class AlarmClientTester extends ExtendedTestCase {

	@Test( groups={"test-first"})
	public void defaultTest() {
		
	}
	
	@Test( groups={"regressiontest"})
	public void TestLoggingHandler() throws Exception {
		addDescription("Tests the AlarmLoggingHandler handling of alarms and other objects.");
		addStep("Initalise the output stream receiver.", "Should be OK.");
		PrintStream defaultOut = System.out;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			System.setOut(new PrintStream(out));

			addStep("Initalise handler, AlarmMessage and constants", "Should not be problematic.");
			AlarmHandler handler = new AlarmLoggingHandler();
			Alarm msg = ExampleMessageFactory.createMessage(Alarm.class);

			String ALARM_MESSAGE = "REGRESSION-TEST";
			String ALARM_CODE = "123456";
			String EXCEPTION_MESSAGE = "Can you handle this??";


			addStep("Insert description of ALARM_CODE and ALARM_MESSAGE in message send to handler", 
			"Should sent to log.");
			AlarmDescription desc = new AlarmDescription();
			desc.setAlarmCode(new BigInteger(ALARM_CODE));
			desc.setAlarmText(ALARM_MESSAGE);
			msg.setAlarmDescription(desc);
			handler.notify(msg);

			addStep("Tests whether it has been written to the LOG", "Should be found in the outputstream");
			String logwrittenOutput = new String(out.toByteArray());
			Assert.assertTrue(logwrittenOutput.contains(ALARM_MESSAGE), 
					"The message should contain '" + ALARM_MESSAGE + "' but was: '" + logwrittenOutput);
			Assert.assertTrue(logwrittenOutput.contains(ALARM_CODE), 
					"The message should contain '" + ALARM_CODE + "' but was: '" + logwrittenOutput);
			out.flush();

			addStep("Tests the handling of other objects, in this case an exception", "Should be written to the log.");
			handler.notify(new NullPointerException(EXCEPTION_MESSAGE));
			logwrittenOutput = new String(out.toByteArray());
			Assert.assertTrue(logwrittenOutput.contains(EXCEPTION_MESSAGE), 
					"The message should contain '" + EXCEPTION_MESSAGE + "' but was: '" + logwrittenOutput);
		} finally {
			System.setOut(defaultOut);
		}
	}
	
	@Test( groups={"pre-test"})
	public void mailingAlarmHandler() throws Exception {
		addDescription("Testing the MailingAlarmHandler");
		addStep("Initialising the variables for the test.", "Should be OK");
		AlarmConfiguration aconf = new AlarmConfiguration();
		MailingConfiguration conf = new MailingConfiguration();
		conf.setMailReceiver("jolf@kb.dk");
		conf.setMailSender("error@kb-br-dev-01.kb.dk");
		conf.setMailServer("kb-br-dev-01.kb.dk");
		aconf.setMailingConfiguration(conf);
		AlarmHandler handler = new MailingAlarmHandler(aconf);
		Alarm msg = ExampleMessageFactory.createMessage(Alarm.class);

		String ALARM_MESSAGE = "REGRESSION-TEST";
		String ALARM_CODE = "123456";
		String EXCEPTION_MESSAGE = "Can you handle this??";

		addStep("Insert description of ALARM_CODE and ALARM_MESSAGE in message send to handler", 
		"Should sent to log.");
		AlarmDescription desc = new AlarmDescription();
		desc.setAlarmCode(new BigInteger(ALARM_CODE));
		desc.setAlarmText(ALARM_MESSAGE);
		msg.setAlarmDescription(desc);
		handler.notify(msg);

		addStep("Tests the handling of other objects, in this case an exception", "Should be written to the log.");
		handler.notify(new NullPointerException(EXCEPTION_MESSAGE));
	}
}
