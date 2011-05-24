/*
 * #%L
 * Bitmagasin integrationstest
 * *
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

import org.bitrepository.access.getfileids.BasicGetFileIDsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.access_client.configuration.AccessConfiguration;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.protocol.*;
import org.jaccept.structure.ExtendedTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.math.BigInteger;
import java.util.*;

/**
 * Test class for the 'GetFileIDsClient'.
 * @author kfc
 */
public class GetFileIDsClientTest extends ExtendedTestCase {

    private Logger log = LoggerFactory.getLogger(GetFileIDsClientTest.class);

    private static final String slaID = "TestSLA";
    private static String queue = "" + (new Date().getTime());//todo test queue

    private static final String HOURS = "HOURS";
    private static final String MILLISECONDS = "MILLISECONDS";

    private boolean mockUp = true;
    private TestMessageListener[] listeners;

    private GetFileIDsClient getFileIDsClient = new BasicGetFileIDsClient();
    private int numberOfPillars;
    private MessageMonitor monitor;


    /**
     * Set up the test scenario before running the tests in this class.
     * @throws javax.xml.bind.JAXBException
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws JAXBException {
        log.debug("setUp");

        // Defining the test queue to be the date for 'now'.
        AccessConfiguration config = AccessComponentFactory.getInstance().getConfig();
        // ToDo Convert to use SLACOnfiguration injection
        queue = config.getGetFileIDsClientQueue();

        // Add pillars that reply to given SLA. Mockup: TestMessageListeners.
        setUpPillars();

        // Monitor
        monitor = new MessageMonitor();
        ProtocolComponentFactory.getInstance().getMessageBus().addListener(queue, monitor);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        if (mockUp) {
            for (TestMessageListener listener: listeners) {
                ProtocolComponentFactory.getInstance().getMessageBus().removeListener(queue, listener);
            }
        }
        // Monitor
        ProtocolComponentFactory.getInstance().getMessageBus().removeListener(queue, monitor);
    }

    /**
     * Set up the pillars used in this test scenario.
     * @throws javax.xml.bind.JAXBException
     */
    private void setUpPillars() throws JAXBException {
        if (mockUp) {
            numberOfPillars = 2;
            listeners = new TestMessageListener[numberOfPillars];
            for (int i = 1; i <= numberOfPillars; i++) {
                String pillarID = "Pillar" + (i);
                log.debug("Setting up TestMessageListener with PillarID: " + pillarID);

                // Create stimuli-response-map
                Map<Object, List<Object>> stimuliResponseMap = new HashMap<Object, List<Object>>();

                IdentifyPillarsForGetFileIDsRequest identifyRequest =
                        TestMessageFactory.getIdentifyPillarsForGetFileIDsRequestTestMessage();
                IdentifyPillarsForGetFileIDsResponse identifyReply =
                        TestMessageFactory.getIdentifyPillarsForGetFileIDsResponseTestMessage(pillarID);
                List<Object> identifyReplyList = new ArrayList<Object>();
                identifyReplyList.add(identifyReply);
                stimuliResponseMap.put(identifyRequest, identifyReplyList);

                GetFileIDsRequest request = TestMessageFactory.getGetFileIDsRequestTestMessage(pillarID);
                GetFileIDsProgressResponse response = TestMessageFactory.getGetFileIDsResponseTestMessage(pillarID);
                List<Object> responseList = new ArrayList<Object>();
                responseList.add(response);
                GetFileIDsFinalResponse complete = TestMessageFactory.getGetFileIDsCompleteTestMessage(pillarID);
                responseList.add(complete);
                stimuliResponseMap.put(request, responseList);

                // Create test-message-listener
                listeners[i-1] = new TestMessageListener(pillarID, stimuliResponseMap, queue);
                ProtocolComponentFactory.getInstance().getMessageBus().addListener(queue, listeners[i-1]);

            }
        }
        // TODO set up Test Pillar and/or Reference Pillar? (remember numberOfPillars)
    }

    /**
     * Test the identify pillars functionality of the GetFileIDsClient.
     * Corresponds to the first part of the first user story described on
     * https://sbforge.org/display/BITMAG/Get+File+IDs+User+Stories
     * @return list of pillar replies
     * @throws Exception
     */
    @Test(groups = {"testfirst"})
    public void identifyPillarsForGetFileIDsTest() throws Exception {
        addDescription("Tests that the expected number of pillars reply to " +
                "request");

        addStep("Identifying pillars: Send message and receive replies.",
                "Logging of one request message and three reply messages " +
                        "(All pillars should reply (they may have no files, " +
                        "but can give the empty list of FileIDs)).");
        List<IdentifyPillarsForGetFileIDsResponse> identifyReplyList =
                getFileIDsClient.identifyPillarsForGetFileIDs(slaID);

        addStep("Ensure that the expected messages have been sent.",
                "Assertion test using internal monitor.");
        Map<Date, Class> messageMap = monitor.getMessageMap();
        Date[] keys = new Date[messageMap.size()];
        messageMap.keySet().toArray(keys);
        Arrays.sort(keys);
        Assert.assertEquals(messageMap.get(keys[0]), IdentifyPillarsForGetFileIDsRequest.class,
                "The first message is expected to be IdentifyPillarsForGetFileIDsRequest.");
        Assert.assertEquals(messageMap.get(keys[1]), IdentifyPillarsForGetFileIDsResponse.class,
                "The second message is expected to be IdentifyPillarsForGetFileIDsResponse.");
        Assert.assertEquals(messageMap.get(keys[2]), IdentifyPillarsForGetFileIDsResponse.class,
                "The third message is expected to be IdentifyPillarsForGetFileIDsResponse.");

        addStep("Ensure that the returned IdentifyPillarsForGetFileIDsReply list " +
                "contains the expected answers.",
                "Assertion test.");
        Assert.assertNotNull(identifyReplyList,
                "The list of replies should not be null.");
        List<String> pillarIDs = new ArrayList<String>();
        for (IdentifyPillarsForGetFileIDsResponse msg: identifyReplyList) {
            pillarIDs.add(msg.getPillarID());
        }
        log.debug(pillarIDs.toString());
        Assert.assertEquals(identifyReplyList.size(), numberOfPillars,
                "Expected number of replies is " + numberOfPillars);

        if (mockUp) {

            for (TestMessageListener listener: listeners) {
            Assert.assertTrue(pillarIDs.contains(listener.getPillarID()),
                    "pillarID " + listener.getPillarID() +
                            "should be in the result.");
            }
            // TODO test if the messages are the expected test messages
        } else {
            // TODO it should be possible to determine which pillars are part of a test set up
        }
    }

    /**
     * Test the Get functionality of the GetFileIDsClient.
     * Corresponds to the second part of the first user story described on
     * https://sbforge.org/display/BITMAG/Get+File+IDs+User+Stories
     * @throws Exception
     */
    @Test(groups = {"testfirst"})
    public void getFileIDsTest() throws Exception {
        addDescription("Tests that the client sends the request and receives a list of FileIDs.");

        addStep("Use identifyPillarsForGetFileIDsTest to identify pillars",
                "The returned list of pillar replies should not be empty.");
        List<IdentifyPillarsForGetFileIDsResponse> pillarReplies =
                getFileIDsClient.identifyPillarsForGetFileIDs(slaID);
        Assert.assertTrue(pillarReplies != null && pillarReplies.size()>0, "Fail: no reachable pillars");

        addStep("Send a message to the pillar with shortest TimeToDeliver to get FileIDs and receive " +
                "answer as part of message",
                "The returned file with the list of FileIDs should not be null");
        TimeMeasureTYPE time = pillarReplies.get(0).getTimeToDeliver();
        String unit = time.getTimeMeasureUnit();
        BigInteger value = time.getTimeMeasureValue();
        String pillarID = pillarReplies.get(0).getPillarID();

        for (IdentifyPillarsForGetFileIDsResponse reply: pillarReplies) {
            if (unit.equals(reply.getTimeToDeliver().getTimeMeasureUnit()) &&
                    reply.getTimeToDeliver().getTimeMeasureValue().compareTo(value) < 0) {
                value = reply.getTimeToDeliver().getTimeMeasureValue();
                pillarID = reply.getPillarID();
            } else if (unit.equals(HOURS) && reply.getTimeToDeliver().getTimeMeasureUnit().equals(MILLISECONDS)) {
                unit = MILLISECONDS;
                value = reply.getTimeToDeliver().getTimeMeasureValue();
                pillarID = reply.getPillarID();
            }
        }

        String correlationID = pillarReplies.get(0).getCorrelationID();
        File fileWithFileIds = getFileIDsClient.getFileIDs(correlationID, slaID, pillarID, queue);
        addStep("Ensure that the expected messages have been sent.",
                "Assertion test using internal monitor.");
        Map<Date, Class> messageMap = monitor.getMessageMap();
        log.debug(messageMap.toString());
        Date[] keys = new Date[messageMap.size()];
        messageMap.keySet().toArray(keys);
        Arrays.sort(keys);
        Assert.assertEquals(messageMap.get(keys[keys.length-1]), GetFileIDsFinalResponse.class,
                "The last message is expected to be a GetFileIDsFinalResponse.");
        Assert.assertEquals(messageMap.get(keys[keys.length-2]), GetFileIDsProgressResponse.class,
                "The second to last message is expected to be a GetFileIDsProgressResponse.");
        Assert.assertEquals(messageMap.get(keys[2]), GetFileIDsRequest.class,
                "The third last message is expected to be a GetFileIDsRequest.");

        addStep("Ensure the returned file is not null... unless it's supposed to be?",
                "The returned file with the list of FileIDs should not be null");
        Assert.assertNotNull(fileWithFileIds, "The returned file should not be null.");
    }


    /**
     * Test the results from the GetFileIDsClient.
     * This test also tests the responses of the pillars.
     * To test only the results of the Client, we need to know the
     * exact responses of the pillars.
     * @throws Exception
     */
    @Test(groups = {"specificationonly"})
    public void getFileIDsResultTest() throws Exception {
        addDescription("Tests the result list of FileIds from the client and thus the involved pillars.");
        //TODO test known fileIDs part of FileIDs list
        addStep("Put three files with known IDs into the Bit Repository " +
                "under given SLA. Mock-up: Put fake files into local " +
                "test pillars.", "Received PutFileFinalResponse messages with " +
                "positive CompleteCode. Mock-up: Logging of Puts.");

        addStep("Send a message to get FileIDs",
                "The returned list of FileIDs should be a GetFileIDsResults " +
                        "(see BitRepositoryData.xsd and contain the three " +
                        "known IDs.");
        //TODO test removed fileID NOT part of FileIDs list
        addStep("Remove one of the files with known IDs from the Bit " +
                "Repository under given SLA. Mock-up: Remove fake file from " +
                "local test pillars.", "Received DeleteFileComplete messages " +
                "with positive CompleteCode. Mock-up: Logging of Puts.");

        addStep("Send a message to get FileIDs",
                "The returned list of FileIDs should be a GetFileIDsResults " +
                        "(see BitRepositoryData.xsd and not contain the ID " +
                        "of the file just removed.");
    }

    /**
     * MessageMonitor
     */
    private class MessageMonitor extends AbstractMessageListener {

        private Map<Date, Class> messages;

        public MessageMonitor() {
            messages = new HashMap<Date, Class>();
        }

        public void onMessage(Object msg) {
            messages.put(new Date(), msg.getClass());
        }

        @Override
        public void onMessage(GetFileIDsRequest message) {
            onMessage((Object) message);
        }

        @Override
        public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
            onMessage((Object) message);
        }

        @Override
        public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
            onMessage((Object) message);
        }

        @Override
        public void onMessage(GetFileIDsProgressResponse message) {
            onMessage((Object) message);
        }

        @Override
        public void onMessage(GetFileIDsFinalResponse message) {
            onMessage((Object) message);
        }

        public Map<Date,Class> getMessageMap() {
            return messages;
        }
    }
}
