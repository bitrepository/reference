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
package org.bitrepository.access.getfileinfos;

import static org.testng.Assert.assertEquals;

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
import org.bitrepository.bitrepositoryelements.ResultingFileInfos;
import org.bitrepository.bitrepositorymessages.GetFileInfosFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileInfosRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileInfosRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileInfosResponse;
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
 * Test class for the 'GetFileInfoClient'.
 */
public class GetFileInfosClientComponentTest extends DefaultClientTest {
    private TestGetFileInfosMessageFactory messageFactory;

    private static final ChecksumSpecTYPE DEFAULT_CHECKSUM_SPECS;
    static {
        DEFAULT_CHECKSUM_SPECS = new ChecksumSpecTYPE();
        DEFAULT_CHECKSUM_SPECS.setChecksumSalt(null);
        DEFAULT_CHECKSUM_SPECS.setChecksumType(ChecksumType.MD5);
    }

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        messageFactory = new TestGetFileInfosMessageFactory(settingsForTestClient.getComponentID());
    }

    @Test(groups = {"regressiontest"})
    public void verifyGetFileInfosClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileInfosClient(settingsForCUT, securityManager,
                settingsForTestClient.getComponentID()) instanceof ConversationBasedGetFileInfosClient,
                "The default GetFileInfos from the Access factory should be of the type '" +
                        ConversationBasedGetFileInfosClient.class.getName() + "'.");
    }

    @Test(groups = {"regressiontest"})
    public void getFileInfosFromSinglePillar() throws Exception {
        addDescription("Tests that the client can retrieve fileinfos from a single pillar.");

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetFileInfosClient getFileInfosClient = createGetFileInfosClient();

        addStep("Request the delivery of the fileinfo of a file from pillar1.",
                "A IdentifyPillarsForGetFileInfosRequest will be sent to the pillars and a IDENTIFY_REQUEST_SENT event" +
                        "should be generated.");
        Collection<String> pillar1AsCollection = new LinkedList<String>();
        pillar1AsCollection.add(PILLAR1_ID);
        getFileInfosClient.getFileInfos(collectionID, new ContributorQuery[] {new ContributorQuery(PILLAR1_ID, null,
                null,
                null)},
                DEFAULT_FILE_ID, DEFAULT_CHECKSUM_SPECS, null, testEventHandler, "TEST-AUDIT");

        IdentifyPillarsForGetFileInfosRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileInfosRequest.class);
        assertEquals(receivedIdentifyRequestMessage.getFileIDs().getFileID(), DEFAULT_FILE_ID);
        assertEquals(receivedIdentifyRequestMessage.getChecksumRequestForExistingFile(), DEFAULT_CHECKSUM_SPECS);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Sends a response from pillar2.",
                "This should be ignored.");
        IdentifyPillarsForGetFileInfosResponse identifyResponse2 = messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(identifyResponse2);
        testEventHandler.verifyNoEventsAreReceived();

        addStep("Sends a response from pillar1.",
                "A getFileInfosRequest should be sendt to pillar1 and the following events should be received: " +
                        "COMPONENT_IDENTIFIED, IDENTIFICATION_COMPLETE and REQUEST_SENT.");

        IdentifyPillarsForGetFileInfosResponse identifyResponse = messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        GetFileInfosRequest receivedGetFileInfosRequest = pillar1Receiver.waitForMessage(GetFileInfosRequest.class);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a GetFileInfosFinalResponse to the client from pillar1",
                "A COMPONENT_COMPLETE event should be generated with the resulting fileinfo. Finally a COMPLETE event" +
                        "should be received.");
        GetFileInfosFinalResponse completeMsg = messageFactory.createGetFileInfosFinalResponse(
                receivedGetFileInfosRequest, PILLAR1_ID, pillar1DestinationId);

        ResultingFileInfos res = new ResultingFileInfos();
        res.setResultAddress(receivedGetFileInfosRequest.getResultAddress());
        completeMsg.setResultingFileInfos(res);

        messageBus.sendMessage(completeMsg);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void getFileInfosDeliveredAtUrl() throws Exception {
        addDescription("Tests the delivery of fileinfos from all pillars at a given URL.");

        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetFileInfosClient getFileInfosClient = createGetFileInfosClient();

        addStep("Ensure the delivery file isn't already present on the http server",
                "Should be remove if it already exists.");
        URL deliveryUrl = httpServerConfiguration.getURL(deliveryFilename);

        addStep("Request the delivery of the fileinfo of a file from all pillars.",
                "A IdentifyPillarsForGetFileInfosRequest should be sent and a IDENTIFY_REQUEST_SENT should be generated.");
        getFileInfosClient.getFileInfos(collectionID, null, DEFAULT_FILE_ID, null, deliveryUrl, testEventHandler,
                "TEST-AUDIT");

        IdentifyPillarsForGetFileInfosRequest receivedIdentifyRequestMessage = null;
        receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileInfosRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileInfosRequest "
                        + "message to the pillar");

        IdentifyPillarsForGetFileInfosResponse identifyResponse1 = messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse1);
        IdentifyPillarsForGetFileInfosResponse identifyResponse2 = messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(identifyResponse2);
        GetFileInfosRequest receivedGetFileInfosRequest1 = pillar1Receiver.waitForMessage(GetFileInfosRequest.class);

        for(int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Sends a final response from each pillar",
                "The GetFileInfosClient notifies that the file is ready through the callback listener and the uploaded file is present.");
        GetFileInfosFinalResponse completeMsg1 = messageFactory.createGetFileInfosFinalResponse(
                receivedGetFileInfosRequest1, PILLAR1_ID, pillar1DestinationId);
        ResultingFileInfos res = new ResultingFileInfos();
        res.setResultAddress(receivedGetFileInfosRequest1.getResultAddress());
        completeMsg1.setResultingFileInfos(res);
        messageBus.sendMessage(completeMsg1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);

        GetFileInfosFinalResponse completeMsg2 = messageFactory.createGetFileInfosFinalResponse(
                receivedGetFileInfosRequest1, PILLAR2_ID, pillar2DestinationId);
        completeMsg2.setResultingFileInfos(res);
        messageBus.sendMessage(completeMsg2);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void testNoSuchFile() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled.");
        addStep("Setting up variables and such.", "Should be OK.");
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetFileInfosClient getFileInfosClient = createGetFileInfosClient();

        addStep("Request the delivery of the fileinfo of a file from the pillar(s). A callback listener should be supplied.",
                "A IdentifyPillarsForGetFileInfosRequest will be sent to the pillar(s).");
        getFileInfosClient.getFileInfos(collectionID, null, DEFAULT_FILE_ID, null, null, testEventHandler,
                "TEST-AUDIT");

        IdentifyPillarsForGetFileInfosRequest receivedIdentifyRequestMessage = null;

        receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(IdentifyPillarsForGetFileInfosRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileInfosRequest "
                        + "message to the pillar");

        GetFileInfosRequest receivedGetFileInfosRequest = null;
        IdentifyPillarsForGetFileInfosResponse identifyResponse =
                messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                        receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        receivedGetFileInfosRequest = pillar1Receiver.waitForMessage(GetFileInfosRequest.class);

        for(int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
        GetFileInfosFinalResponse completeMsg = messageFactory.createGetFileInfosFinalResponse(
                receivedGetFileInfosRequest, PILLAR1_ID, pillar1DestinationId);

        ResponseInfo rfInfo = new ResponseInfo();
        rfInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
        rfInfo.setResponseText("No such file.");
        completeMsg.setResponseInfo(rfInfo);
        completeMsg.setResultingFileInfos(null);

        messageBus.sendMessage(completeMsg);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }


    @Test(groups = {"regressiontest"})
    public void testPaging() throws Exception {
        addDescription("Tests the GetFileInfos client correctly handles functionality for limiting results, either by " +
                "timestamp or result count.");

        GetFileInfosClient getFileInfosClient = createGetFileInfosClient();
        addStep("Request fileinfos from with MinTimestamp, MaxTimestamp, MaxNumberOfResults set for both pillars .",
                "A IdentifyPillarsForGetFileInfosRequest should be sent.");
        Date timestamp3 = new Date();
        Date timestamp2 =  new Date(timestamp3.getTime() - 100);
        Date timestamp1 =  new Date(timestamp3.getTime() - 1000);
        ContributorQuery query1 = new ContributorQuery(PILLAR1_ID, timestamp1, timestamp2, new Integer(1));
        ContributorQuery query2 = new ContributorQuery(PILLAR2_ID, timestamp2, timestamp3, new Integer(2));
        getFileInfosClient.getFileInfos(collectionID, new ContributorQuery[]{query1, query2}, null, null, null,
                testEventHandler, null);

        IdentifyPillarsForGetFileInfosRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileInfosRequest.class);

        addStep("Send a IdentifyPillarsForGetFileInfosResponse from both pillars.",
                "A GetFileInfosRequest should be sent to both pillars with the appropriate MinChecksumTimestamp, MaxChecksumTimestamp, " +
                        "MaxNumberOfResults values.");
        messageBus.sendMessage(messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId));
        messageBus.sendMessage(messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId));

        GetFileInfosRequest receivedGetFileInfosRequest1 = pillar1Receiver.waitForMessage(GetFileInfosRequest.class);
        assertEquals(receivedGetFileInfosRequest1.getMinChecksumTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query1.getMinTimestamp()),
                "Unexpected MinTimestamp in GetFileInfosRequest to pillar1.");
        assertEquals(receivedGetFileInfosRequest1.getMaxChecksumTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query1.getMaxTimestamp()),
                "Unexpected MaxTimestamp in GetFileInfosRequest to pillar1.");
        assertEquals(receivedGetFileInfosRequest1.getMaxNumberOfResults(),
                BigInteger.valueOf(query1.getMaxNumberOfResults()),
                "Unexpected MaxNumberOfResults in GetFileInfosRequest to pillar1.");

        GetFileInfosRequest receivedGetFileInfosRequest2 = pillar2Receiver.waitForMessage(GetFileInfosRequest.class);
        assertEquals(receivedGetFileInfosRequest2.getMinChecksumTimestamp(),
                CalendarUtils.getXmlGregorianCalendar((query2.getMinTimestamp())),
                "Unexpected MinTimestamp in GetFileInfosRequest to pillar2.");
        assertEquals(receivedGetFileInfosRequest2.getMaxChecksumTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query2.getMaxTimestamp()),
                "Unexpected MaxTimestamp in GetFileInfosRequest to pillar2.");
        assertEquals(receivedGetFileInfosRequest2.getMaxNumberOfResults(),
                BigInteger.valueOf(query2.getMaxNumberOfResults()),
                "Unexpected MaxNumberOfResults in GetFileInfosRequest to pillar2.");
    }

    @Test(groups={"regressiontest"})
    public void getFileInfosFromOtherCollection() throws Exception {
        addDescription("Tests the getFileInfos client will correctly try to get from a second collection if required");
        addFixture("Configure collection1 to contain both pillars and collection 2 to only contain pillar2");
        settingsForCUT.getReferenceSettings().getClientSettings().setOperationRetryCount(BigInteger.valueOf(2));
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR2_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getPillarIDs().getPillarID().add(PILLAR2_ID);
        String otherCollection =  settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getID();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetFileInfosClient client = createGetFileInfosClient();

        addStep("Request the fileinfo of a file through the GetFileInfosClient for collection2",
                "A identification request should be dispatched.");
        client.getFileInfos(otherCollection, null, null, null, null, testEventHandler, null);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        IdentifyPillarsForGetFileInfosRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileInfosRequest.class);
        assertEquals(receivedIdentifyRequestMessage.getCollectionID(), otherCollection);

        addStep("Send an identification response from pillar2.",
                "An COMPONENT_IDENTIFIED event should be generate folled by a IDENTIFICATION_COMPLETE and a " +
                        "REQUEST_SENT. A GetFileInfosFileRequest should be sent to pillar2");
        messageBus.sendMessage(messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId));
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        GetFileInfosRequest receivedRequest = pillar2Receiver.waitForMessage(GetFileInfosRequest.class);
        assertEquals(receivedRequest.getCollectionID(), otherCollection);

        addStep("Send a complete event from the pillar", "The client generates " +
                "a COMPONENT_COMPLETE, followed by a COMPLETE event.");
        GetFileInfosFinalResponse getFileInfosFinalResponse1 = messageFactory.createGetFileInfosFinalResponse(
                receivedRequest, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(getFileInfosFinalResponse1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }


    /**
     * Creates a new test GetCheckSumsClient based on the supplied settings. 
     *
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new GetFileClient(Wrapper).
     */
    private GetFileInfosClient createGetFileInfosClient() {
        return new GetFileInfosClientTestWrapper(new ConversationBasedGetFileInfosClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()), testEventManager);
    }


    @Override
    protected MessageResponse createIdentifyResponse(MessageRequest identifyRequest, String from, String to) {
        MessageResponse response = messageFactory.createIdentifyPillarsForGetFileInfosResponse(
                (IdentifyPillarsForGetFileInfosRequest) identifyRequest, from, to);
        return response;
    }

    @Override
    protected MessageResponse createFinalResponse(MessageRequest request, String from, String to) {
        MessageResponse response =  messageFactory.createGetFileInfosFinalResponse(
                (GetFileInfosRequest) request, from, to);
        return response;
    }

    @Override
    protected MessageRequest waitForIdentifyRequest() {
        return collectionReceiver.waitForMessage(IdentifyPillarsForGetFileInfosRequest.class);
    }

    @Override
    protected MessageRequest waitForRequest(MessageReceiver receiver) {
        return receiver.waitForMessage(GetFileInfosRequest.class);
    }

    @Override
    protected void checkNoRequestIsReceived(MessageReceiver receiver) {
        receiver.checkNoMessageIsReceived(GetFileInfosRequest.class);
    }

    @Override
    protected void startOperation(TestEventHandler testEventHandler) {
        GetFileInfosClient getFileInfosClient = createGetFileInfosClient();
        getFileInfosClient.getFileInfos(collectionID, null, null, null, null, testEventHandler, null);
    }
}
