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

import org.bitrepository.access_client.configuration.AccessConfiguration;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.protocol.AbstractMessageListener;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.jaccept.structure.ExtendedTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Test class for the 'GetFileIDsClient'.
 * @author kfc
 */
public class GetFileIDsClientTest extends ExtendedTestCase {

    private Logger log = LoggerFactory.getLogger(GetFileIDsClientTest.class);
    private static int WAITING_TIME_FOR_MESSAGE = 1000;
    private static String slaID = "THE-SLA";
    private static String queue = "" + (new Date().getTime());

    private boolean mockUp = true;
    private TestMessageListener[] listeners;
    GetFileIDsClient getFileIDsClient = new GetFileIDsClientImpl();


    @BeforeClass
    public void setUp() {
        addStep("Initialising variables for testing, e.g. defining the " +
                "queue to be the date for 'now'.", "");
        AccessConfiguration config = AccessComponentFactory.getInstance().getConfig();
        config.setQueue(queue);

        addStep("Add pillars that reply to given SLA. Mockup:" +
                "TestMessageListeners.",
                "Logging on INFO level that reports the pillars started");
        setUpPillars();
    }

    /**
     * Set up the pillars used in this test scenario
     */
    private void setUpPillars() {
        if (mockUp) {
            listeners = new TestMessageListener[3];
            for (int i = 1; i <= 3; i++) {
                listeners[i-1] = new TestMessageListener("Pillar" + (i));
                ProtocolComponentFactory.getInstance().getMessageBus().addListener(queue, listeners[i-1]);
            }
        }
        // TODO set up Test Pillar and/or Reference Pillar?
    }

    /**
     * Test the identify pillars functionality of the GetFileIDsClient.
     * Corresponds to the first part of the test described in the
     * https://sbforge.org/display/BITMAG/Use+of+GetFileIDs
     * user story WITHOUT the alarm/error part.
     * @return list of pillarIDs
     * @throws Exception
     */
    @Test(groups = {"test first"})
    public List<String> identifyPillarsForGetFileIDsTest() throws Exception {
        addDescription("Tests that the expected number of pillars reply to " +
                "request");

        addStep("Identifying pillars: Send message and receive replies.",
                "Logging of one request message and three reply messages " +
                        "(All pillars should reply (they may have no files, " +
                        "but can give the empty list of FileIDs)).");
        getFileIDsClient.sendIdentifyPillarsForGetFileIDsRequest(slaID);

        if (mockUp) {
            // wait for mock up pillars to receive request messages
            synchronized(this) {
                try {
                    wait(WAITING_TIME_FOR_MESSAGE);
                } catch (Exception e) {
                    // print, but ignore!
                    e.printStackTrace();
                }
            }

            addStep("Ensure that the IdentifyPillarsForGetFileIDsRequest " +
                    "message has been caught by all mock up pillars.",
                    "It should be valid.");
            for (TestMessageListener listener: listeners) {
                Assert.assertNotNull(listener.getMessage(),
                        "The message must not be null.");
                Assert.assertEquals(listener.getMessageClass().getName(),
                        IdentifyPillarsForGetFileIDsRequest.class.getName(),
                        "The message should be of the type " +
                                IdentifyPillarsForGetFileIDsRequest.
                                        class.getName());
            }

            // send replies
            addStep("Sending replies to the identify message.",
                    "Should be handled by the GetFileIDsClient.");
            mockUpSendIdentifyPillarsForGetFileIDsReplies();
        }

        // wait for getFileIDsClient to receive replies
        synchronized(this) {
            try {
                wait(WAITING_TIME_FOR_MESSAGE);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }

        List<String> pillarIDs =
                getFileIDsClient.identifyPillarsForGetFileIDs(slaID);

        Assert.assertNotNull(pillarIDs,
                "The list of pillar IDs should not be null.");
        if (mockUp) {
            for (TestMessageListener listener: listeners) {
            Assert.assertTrue(pillarIDs.contains(listener.getPillarID()),
                    "pillarID " + listener.getPillarID() +
                            "should be in the result.");
            }
        } else {
            // TODO it should be possible to determine which pillars are part
            // of a test set up
        }
        return pillarIDs;
    }

    /**
     * If mock up, we send the replies for the pillars.
     */
    private void mockUpSendIdentifyPillarsForGetFileIDsReplies() {
        for (TestMessageListener listener: listeners) {
            IdentifyPillarsForGetFileIDsRequest identifyMessage =
                    (IdentifyPillarsForGetFileIDsRequest) listener.getMessage();
            // TODO String queue = identifyMessage.getReplyTo();

            IdentifyPillarsForGetFileIDsReply reply =
                    new IdentifyPillarsForGetFileIDsReply();
            reply.setCorrelationID(identifyMessage.getCorrelationID());
            reply.setSlaID(identifyMessage.getSlaID());
            reply.setReplyTo(queue);
            reply.setPillarID(listener.getPillarID());

            TimeMeasureTYPE time = new TimeMeasureTYPE();
            time.setMiliSec(BigInteger.valueOf(1000));
            reply.setTimeToDeliver(time);

            reply.setVersion((short) 1);
            reply.setMinVersion((short) 1);

            ProtocolComponentFactory.getInstance().getMessageBus().
                    sendMessage(queue, reply);
        }
    }

    @Test(groups = {"specificationonly"})
    public void GetFileIDsTest() throws Exception {
        addDescription("Tests that a pillar returns the expected list of " +
                "FileIDs");

        addStep("Use identifyPillarsForGetFileIDsTest to identify pillars","");
        List<String> pillarIDs = identifyPillarsForGetFileIDsTest();
        assert pillarIDs != null && pillarIDs.size()>0: "Fail: no reachable " +
                "pillars";

        for (String pillarID: pillarIDs) {
            addStep("Send a message to a known pillar to get FileIDs",
                    "The returned file with the list of FileIDs should not be " +
                            "null");
            getFileIDsClient.sendGetFileIDsRequest(slaID, pillarID);

            if (mockUp) {
                // wait for mock up pillar to receive request messages
                synchronized(this) {
                    try {
                        wait(WAITING_TIME_FOR_MESSAGE);
                    } catch (Exception e) {
                        // print, but ignore!
                        e.printStackTrace();
                    }
                }


                // WORK IN PROGRESS


                addStep("Ensure that the GetFileIDsRequest " +
                        "message has been caught by all mock up pillars.",
                        "It should be valid.");
                for (TestMessageListener listener: listeners) {
                    Assert.assertNotNull(listener.getMessage(),
                            "The message must not be null.");
                    Assert.assertEquals(listener.getMessageClass().getName(),
                            GetFileIDsRequest.class.getName(),
                            "The message should be of the type " +
                                    GetFileIDsRequest.class.getName());
                }

                // send replies
                addStep("Sending replies to the request message.",
                        "Should be handled by the GetFileIDsClient.");
                mockUpSendGetFileIDsReplies();
            }

            // wait for getFileIDsClient to receive replies
            synchronized(this) {
                try {
                    wait(WAITING_TIME_FOR_MESSAGE);
                } catch (Exception e) {
                    // print, but ignore!
                    e.printStackTrace();
                }
            }





            File fileWithFileIds = getFileIDsClient.getFileIDs(slaID, pillarID);
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
            File fileWithFileIds = getFileIDsClient.getFileIDs(slaID, pillarID);
            BufferedReader in = new BufferedReader(new FileReader(fileWithFileIds));
            String fileIdsString = "";
            while (in.ready()) {
                fileIdsString += in.readLine();
            }
            //TODO extract GetFileIDsResults + asserts
        }

    }

    /**
     * If mock up, we send the replies for the pillars.
     * We assume NO result Address is given and the result is therefore
     * included in the complete message.
     */
    private void mockUpSendGetFileIDsReplies() {
        for (TestMessageListener listener: listeners) {
            GetFileIDsRequest requestMessage =
                    (GetFileIDsRequest) listener.getMessage();
            // TODO String queue = requestMessage.getReplyTo();
            String address = requestMessage.getResultAddress();
            Assert.assertTrue(address == null || address.equals(""), "We " +
                    "assume no result address is given");

            // send response
            GetFileIDsResponse response =
                    new GetFileIDsResponse();
            response.setCorrelationID(requestMessage.getCorrelationID());
            response.setSlaID(requestMessage.getSlaID());
            response.setReplyTo(queue);
            response.setPillarID(listener.getPillarID());
            response.setResponseCode("OK");

            response.setVersion((short) 1);
            response.setMinVersion((short) 1);

            ProtocolComponentFactory.getInstance().getMessageBus().
                    sendMessage(queue, response);

            // send complete
            GetFileIDsComplete completeMsg =
                    new GetFileIDsComplete();
            completeMsg.setCorrelationID(requestMessage.getCorrelationID());
            completeMsg.setSlaID(requestMessage.getSlaID());
            completeMsg.setReplyTo(queue);
            completeMsg.setPillarID(listener.getPillarID());
            completeMsg.setCompleteCode("OK");

            completeMsg.setNoOfItems(BigInteger.valueOf(0));



            completeMsg.setVersion((short) 1);
            completeMsg.setMinVersion((short) 1);

            ProtocolComponentFactory.getInstance().getMessageBus().
                    sendMessage(queue, completeMsg);
        }
    }

    /**
     * Test message listener
     */
    protected class TestMessageListener extends AbstractMessageListener
            implements ExceptionListener {
        private String pillarID;
        private Object lastMessage;

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
                lastMessage = msg;
                log.debug("TestMessageListener onMessage: " + msg.getClass());
            } catch (Exception e) {
                Assert.fail("Should not throw an exception: ", e);
            }
        }

        @Override
        public void onException(JMSException e) {
            e.printStackTrace();
        }
        public String getPillarID() {
            return pillarID;
        }
        public Object getMessage() {
            return lastMessage;
        }
        public Class getMessageClass() {
            return lastMessage.getClass();
        }
    }
}
