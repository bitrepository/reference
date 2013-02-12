/*
 * #%L
 * Bitrepository Access
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
/*
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
package org.bitrepository.access.getchecksums;

import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.DefaultClientTest;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetFileClient'.
 */
public class GetChecksumsClientComponentTest extends DefaultClientTest {
    private TestGetChecksumsMessageFactory testMessageFactory;

    private static final ChecksumSpecTYPE DEFAULT_CHECKSUM_SPECS;
    static {
        DEFAULT_CHECKSUM_SPECS = new ChecksumSpecTYPE();
        DEFAULT_CHECKSUM_SPECS.setChecksumSalt(null);
        DEFAULT_CHECKSUM_SPECS.setChecksumType(ChecksumType.MD5);
    }

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        testMessageFactory = new TestGetChecksumsMessageFactory(settingsForCUT.getCollectionID());
    }

    @Test(groups = {"regressiontest"})
    public void verifyGetChecksumsClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createGetChecksumsClient(settingsForCUT, securityManager,
                settingsForTestClient.getComponentID()) instanceof ConversationBasedGetChecksumsClient,
                "The default GetFileClient from the Access factory should be of the type '" +
                        ConversationBasedGetChecksumsClient.class.getName() + "'.");
    }

    @Test(groups = {"regressiontest"})
    public void getChecksumsFromSinglePillar() throws Exception {
        addDescription("Tests that the client can retrieve checksums from a single pillar.");

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = createGetChecksumsClient();

        addStep("Request the delivery of the checksum of a file from pillar1.",
                "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillars and a IDENTIFY_REQUEST_SENT event" +
                        "should be generated.");
        Collection<String> pillar1AsCollection = new LinkedList<String>();
        pillar1AsCollection.add(PILLAR1_ID);
        getChecksumsClient.getChecksums(new ContributorQuery[] {new ContributorQuery(PILLAR1_ID, null, null, null)},
                DEFAULT_FILE_ID, DEFAULT_CHECKSUM_SPECS, null, testEventHandler, "TEST-AUDIT");

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsRequest.class);
        Assert.assertEquals(receivedIdentifyRequestMessage.getFileIDs().getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(receivedIdentifyRequestMessage.getChecksumRequestForExistingFile(), DEFAULT_CHECKSUM_SPECS);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Sends a response from pillar2.",
                "This should be ignored.");
        IdentifyPillarsForGetChecksumsResponse identifyResponse2 = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(identifyResponse2);
        testEventHandler.verifyNoEventsAreReceived();

        addStep("Sends a response from pillar1.",
                "A getChecksumRequest should be sendt to pillar1 and the following events should be received: " +
                        "COMPONENT_IDENTIFIED, IDENTIFICATION_COMPLETE and REQUEST_SENT.");

        IdentifyPillarsForGetChecksumsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        GetChecksumsRequest receivedGetChecksumsRequest = pillar1Receiver.waitForMessage(GetChecksumsRequest.class);

        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a GetChecksumsFinalResponse to the client from pillar1",
                "A COMPONENT_COMPLETE event should be generated with the resulting checksum. Finally a COMPLETE event" +
                        "should be received.");
        GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);

        ResultingChecksums res = new ResultingChecksums();
        res.setResultAddress(receivedGetChecksumsRequest.getResultAddress());
        completeMsg.setResultingChecksums(res);

        messageBus.sendMessage(completeMsg);

        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void getChecksumsDeliveredAtUrl() throws Exception {
        addDescription("Tests the delivery of checksums from all pillars at a given URL.");

        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = createGetChecksumsClient();

        addStep("Ensure the delivery file isn't already present on the http server",
                "Should be remove if it already exists.");
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from all pillars.",
                "A IdentifyPillarsForGetChecksumsRequest should be sent and a IDENTIFY_REQUEST_SENT should be generated.");
        getChecksumsClient.getChecksums(null, DEFAULT_FILE_ID, null, deliveryUrl, testEventHandler, "TEST-AUDIT");

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                        + "message to the pillar");

        IdentifyPillarsForGetChecksumsResponse identifyResponse1 = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse1);
        IdentifyPillarsForGetChecksumsResponse identifyResponse2 = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(identifyResponse2);
        GetChecksumsRequest receivedGetChecksumsRequest1 = pillar1Receiver.waitForMessage(GetChecksumsRequest.class);

        for(int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Sends a final response from each pillar",
                "The GetChecksumsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
        GetChecksumsFinalResponse completeMsg1 = testMessageFactory.createGetChecksumsFinalResponse(
                receivedGetChecksumsRequest1, PILLAR1_ID, pillar1DestinationId);
        ResultingChecksums res = new ResultingChecksums();
        res.setResultAddress(receivedGetChecksumsRequest1.getResultAddress());
        completeMsg1.setResultingChecksums(res);
        messageBus.sendMessage(completeMsg1);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);

        GetChecksumsFinalResponse completeMsg2 = testMessageFactory.createGetChecksumsFinalResponse(
                receivedGetChecksumsRequest1, PILLAR2_ID, pillar2DestinationId);
        completeMsg2.setResultingChecksums(res);
        messageBus.sendMessage(completeMsg2);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void testNoSuchFile() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled.");
        addStep("Setting up variables and such.", "Should be OK.");
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = createGetChecksumsClient();

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.",
                "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(null, DEFAULT_FILE_ID, null, null, testEventHandler, "TEST-AUDIT");

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;

        receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(IdentifyPillarsForGetChecksumsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                        + "message to the pillar");

        GetChecksumsRequest receivedGetChecksumsRequest = null;
        IdentifyPillarsForGetChecksumsResponse identifyResponse =
                testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                        receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        receivedGetChecksumsRequest = pillar1Receiver.waitForMessage(GetChecksumsRequest.class);

        for(int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
        GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);

        ResponseInfo rfInfo = new ResponseInfo();
        rfInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
        rfInfo.setResponseText("No such file.");
        completeMsg.setResponseInfo(rfInfo);
        completeMsg.setResultingChecksums(null);

        messageBus.sendMessage(completeMsg);

        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }


    @Test(groups = {"regressiontest"})
    public void testPaging() throws Exception {
        addDescription("Tests the GetChecksums client correctly handles functionality for limiting results, either by " +
                "timestamp or result count.");

        GetChecksumsClient getFileIDsClient = createGetChecksumsClient();
        addStep("Request fileIDs from with MinTimestamp, MaxTimestamp, MaxNumberOfResults set for both pillars .",
                "A IdentifyPillarsForGetChecksumsRequest should be sent.");
        Date timestamp3 = new Date();
        Date timestamp2 =  new Date(timestamp3.getTime() - 100);
        Date timestamp1 =  new Date(timestamp3.getTime() - 1000);
        ContributorQuery query1 = new ContributorQuery(PILLAR1_ID, timestamp1, timestamp2, new Integer(1));
        ContributorQuery query2 = new ContributorQuery(PILLAR2_ID, timestamp2, timestamp3, new Integer(2));
        getFileIDsClient.getChecksums(new ContributorQuery[]{query1, query2}, null, null, null, testEventHandler, null);

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsRequest.class);

        addStep("Send a IdentifyPillarsForGetChecksumsResponse from both pillars.",
                "A GetChecksumsRequest should be sent to both pillars with the appropriate MinTimestamp, MaxTimestamp, " +
                        "MaxNumberOfResults values.");
        messageBus.sendMessage(testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId));
        messageBus.sendMessage(testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId));

        GetChecksumsRequest receivedGetChecksumsRequest1 = pillar1Receiver.waitForMessage(GetChecksumsRequest.class);
        Assert.assertEquals(receivedGetChecksumsRequest1.getMinTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query1.getMinTimestamp()),
                "Unexpected MinTimestamp in GetChecksumsRequest to pillar1.");
        Assert.assertEquals(receivedGetChecksumsRequest1.getMaxTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query1.getMaxTimestamp()),
                "Unexpected MaxTimestamp in GetChecksumsRequest to pillar1.");
        Assert.assertEquals(receivedGetChecksumsRequest1.getMaxNumberOfResults(),
                BigInteger.valueOf(query1.getMaxNumberOfResults()),
                "Unexpected MaxNumberOfResults in GetChecksumsRequest to pillar1.");

        GetChecksumsRequest receivedGetChecksumsRequest2 = pillar2Receiver.waitForMessage(GetChecksumsRequest.class);
        Assert.assertEquals(receivedGetChecksumsRequest2.getMinTimestamp(),
                CalendarUtils.getXmlGregorianCalendar((query2.getMinTimestamp())),
                "Unexpected MinTimestamp in GetChecksumsRequest to pillar2.");
        Assert.assertEquals(receivedGetChecksumsRequest2.getMaxTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query2.getMaxTimestamp()),
                "Unexpected MaxTimestamp in GetChecksumsRequest to pillar2.");
        Assert.assertEquals(receivedGetChecksumsRequest2.getMaxNumberOfResults(),
                BigInteger.valueOf(query2.getMaxNumberOfResults()),
                "Unexpected MaxNumberOfResults in GetChecksumsRequest to pillar2.");
    }


    /**
     * Creates a new test GetCheckSumsClient based on the supplied settings. 
     *
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new GetFileClient(Wrapper).
     */
    private GetChecksumsClient createGetChecksumsClient() {
        return new GetChecksumsClientTestWrapper(new ConversationBasedGetChecksumsClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()), testEventManager);
    }


    @Override
    protected MessageResponse createIdentifyResponse(MessageRequest identifyRequest, String from, String to) {
        MessageResponse response = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                (IdentifyPillarsForGetChecksumsRequest) identifyRequest, from, to);
        return response;
    }

    @Override
    protected MessageResponse createFinalResponse(MessageRequest request, String from, String to) {
        MessageResponse response =  testMessageFactory.createGetChecksumsFinalResponse(
                (GetChecksumsRequest) request, from, to);
        return response;
    }

    @Override
    protected MessageRequest waitForIdentifyRequest() {
        return collectionReceiver.waitForMessage(IdentifyPillarsForGetChecksumsRequest.class);
    }

    @Override
    protected MessageRequest waitForRequest(MessageReceiver receiver) {
        return receiver.waitForMessage(GetChecksumsRequest.class);
    }

    @Override
    protected void checkNoRequestIsReceived(MessageReceiver receiver) {
        receiver.checkNoMessageIsReceived(GetChecksumsRequest.class);
    }

    @Override
    protected void startOperation(TestEventHandler testEventHandler) {
        GetChecksumsClient getChecksumsClient = createGetChecksumsClient();
        getChecksumsClient.getChecksums(null, null, null, null, testEventHandler, null);
    }
}
