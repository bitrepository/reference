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
package org.bitrepository.access;

import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.protocol.AbstractMessageListener;
import org.jaccept.structure.ExtendedTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * Test class for the 'GetFileIDsClient'.
 * @author kfc
 */
public class GetFileIDsClientTest extends ExtendedTestCase {

    private Logger log = LoggerFactory.getLogger(GetFileIDsClientTest.class);

    private static int WAITING_TIME_FOR_MESSAGE = 1000;

    private static String slaId = "THE-SLA";

    @Test(groups = {"specificationonly"})
    public void identifyPillarsForGetFileIDsTestSpec() throws Exception {
        addDescription("Tests that the expected number of pillars reply to " +
                "request");
        GetFileIDsClient getFileIDsClient = new GetFileIDsClientImpl();
        addStep("Add three reference pillars that reply to given SLA. First" +
                "version: 3 TestMessageListeners.",
                "Logging on INFO level that reports the pillars started");
        setUpPillars(3);

        addStep("Identifying pillars: Send message and receive replies.",
                "Logging of one request message and three reply messages " +
                        "(All pillars should reply (they have no files, but " +
                        "will be able to give the empty list of FileIDs).");
        List<String> pillarIDs = getFileIDsClient.identifyPillarsForGetFileIDs(slaId);
        Assert.assertNotNull(pillarIDs, "The list of pillar IDs should not be null.");

    }

    private void setUpPillars(int numberOfPillars) {
        for (int i = 1; i <= numberOfPillars; i++) {
        TestMessageListener listener = new TestMessageListener("Pillar" + (i));
        //TODO Test Pillar and/or Reference Pillar?
        }
    }

    @Test(groups = {"specificationonly"})
    public void GetFileIDsTest() throws Exception {
        addDescription("Tests that a pillar returns the expected list of " +
                "FileIDs");
        GetFileIDsClient getFileIDsClient = new GetFileIDsClientImpl();

        addStep("Add a reference pillar that replies to given SLA",
                "Logging on INFO level that reports the pillar started");
        setUpPillars(3);

        addStep("Identifying pillars: Send message and receive replies.",
                "Logging of request message + reply message with a ReplyTo queue" +
                        "where the getFileIDsRequest can be send");
        List<String> pillarIDs = getFileIDsClient.identifyPillarsForGetFileIDs(slaId);
        Assert.assertNotNull(pillarIDs, "The list of pillar IDs should not be null.");

        addStep("Send a message to get FileIDs",
                "The returned list of FileIDs should be empty");
        for (String pillarID: pillarIDs) {
            File fileWithFileIds = getFileIDsClient.getFileIDs(slaId, pillarID);
            FileReader reader = new FileReader(fileWithFileIds);
            Assert.assertEquals(reader.read(), -1);
        }

        addStep("Put three files with known IDs into the Bit Repository " +
                "under given SLA. First version: Put fake files into local " +
                "test pillars.", "Received PutFileComplete messages with " +
                "positive CompleteCode. First version: Logging of Puts.");

        addStep("Send a message to get FileIDs",
                "The returned list of FileIDs should be a GetFileIDsResults " +
                        "(see BitRepositoryData.xsd and contain the three " +
                        "known IDs");
        for (String pillarID: pillarIDs) {
            File fileWithFileIds = getFileIDsClient.getFileIDs(slaId, pillarID);
            BufferedReader in = new BufferedReader(new FileReader(fileWithFileIds));
            String fileIdsString = "";
            while (in.ready()) {
                fileIdsString += in.readLine();
            }
            //TODO extract GetFileIDsResults + asserts
        }

    }
    protected class TestMessageListener extends AbstractMessageListener
            implements ExceptionListener {
        private String pillarID;

        private String message = null;
        private Class messageClass = null;

        public TestMessageListener(String pillarID) {
            this.pillarID = pillarID;
        }

        @Override
        public void onMessage(GetFileIDsRequest message) {
            onMessage((Object) message);
        }

        @Override
        public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
            onMessage((Object) message);
        }

        public void onMessage(Object msg) {
            try {
                message = JaxbHelper.serializeToXml(msg);
                messageClass = msg.getClass();
            } catch (Exception e) {
                Assert.fail("Should not throw an exception: ", e);
            }
            // awaken the tester
            Thread.currentThread().notifyAll();
        }

        @Override
        public void onException(JMSException e) {
            e.printStackTrace();
        }
        public String getMessage() {
            return message;
        }
        public Class getMessageClass() {
            return messageClass;
        }
    }
}
