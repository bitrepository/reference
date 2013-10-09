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
package org.bitrepository.access.getfileids;

import access.AccessComponentFactory;
import access.ContributorQuery;
import access.getfileids.ConversationBasedGetFileIDsClient;
import access.getfileids.GetFileIDsClient;
import access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
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

import javax.xml.bind.JAXBException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;

import static org.testng.Assert.assertEquals;

/**
 * Test class for the 'GetFileIDsClient'.
 */
public class GetFileIDsClientComponentTest extends DefaultClientTest {

    private TestGetFileIDsMessageFactory messageFactory;

    /**
     * Set up the test scenario before running the tests in this class.
     * @throws javax.xml.bind.JAXBException
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws JAXBException {
        // TODO getFileIDsFromFastestPillar settings
        messageFactory = new TestGetFileIDsMessageFactory(settingsForTestClient.getComponentID());
    }

    @Test(groups = {"regressiontest"})
    public void verifyGetFileIDsClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileIDsClient(settingsForCUT, securityManager,
                settingsForTestClient.getComponentID()) instanceof ConversationBasedGetFileIDsClient,
                "The default GetFileClient from the Access factory should be of the type '" +
                        ConversationBasedGetFileIDsClient.class.getName() + "'.");
    }

    @Test(groups = {"regressiontest"})
    public void getFileIDsDeliveredAtUrl() throws Exception {
        addDescription("Tests the delivery of fileIDs from a pillar at a given URL.");
        addStep("Initialise the variables for this test.",
                "EventManager and GetFileIDsClient should be instantiated.");

        String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(DEFAULT_FILE_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        GetFileIDsClient getFileIDsClient = createGetFileIDsClient();
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the file ids of a file from the pillar(s). A callback listener should be supplied.",
                "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
        getFileIDsClient.getFileIDs(collectionID, null, DEFAULT_FILE_ID,deliveryUrl, testEventHandler);

        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage  = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsRequest.class);
        assertEquals(receivedIdentifyRequestMessage.getCollectionID(), collectionID);
        Assert.assertNotNull(receivedIdentifyRequestMessage.getCorrelationID());
        assertEquals(receivedIdentifyRequestMessage.getReplyTo(), settingsForCUT.getReceiverDestinationID());
        assertEquals(receivedIdentifyRequestMessage.getTo(), PILLAR1_ID);
        assertEquals(receivedIdentifyRequestMessage.getFrom(), settingsForTestClient.getComponentID());
        assertEquals(receivedIdentifyRequestMessage.getDestination(), settingsForTestClient.getCollectionDestination());
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                        + "message to the pillar");
        IdentifyPillarsForGetFileIDsResponse identifyResponse = messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        GetFileIDsRequest receivedGetFileIDsRequest = pillar1Receiver.waitForMessage(GetFileIDsRequest.class);
        assertEquals(receivedGetFileIDsRequest.getCollectionID(), collectionID);
        assertEquals(receivedGetFileIDsRequest.getCorrelationID(), receivedIdentifyRequestMessage.getCorrelationID());
        assertEquals(receivedGetFileIDsRequest.getReplyTo(), settingsForCUT.getReceiverDestinationID());
        assertEquals(receivedGetFileIDsRequest.getFrom(), settingsForTestClient.getComponentID());
        assertEquals(receivedGetFileIDsRequest.getDestination(), pillar1DestinationId);

        addStep("The pillar sends a getFileIDsProgressResponse to the GetFileIDsClient.",
                "The GetFileIDsClient should notify about the response through the callback interface.");
        GetFileIDsProgressResponse getFileIDsProgressResponse = messageFactory.createGetFileIDsProgressResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(getFileIDsProgressResponse);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);

        addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message",
                "The GetFileIDsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
        GetFileIDsFinalResponse completeMsg = messageFactory.createGetFileIDsFinalResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);

        ResultingFileIDs res = new ResultingFileIDs();
        res.setResultAddress(receivedGetFileIDsRequest.getResultAddress());
        completeMsg.setResultingFileIDs(res);

        messageBus.sendMessage(completeMsg);

        addStep("Receive and validate event results for the pillar.",
                "Should be a FileIDsCompletePillarEvent with the ResultingFileIDs containing only the URL.");
        FileIDsCompletePillarEvent event = (FileIDsCompletePillarEvent) testEventHandler.waitForEvent();
        assertEquals(event.getEventType(), OperationEventType.COMPONENT_COMPLETE);
        ResultingFileIDs resFileIDs = event.getFileIDs();
        Assert.assertNotNull(resFileIDs, "The ResultingFileIDs may not be null.");
        Assert.assertTrue(resFileIDs.getResultAddress().contains(deliveryUrl.toExternalForm()),
                "The resulting address'" + resFileIDs.getResultAddress() + "' should contain the argument address: '"
                        + deliveryUrl.toExternalForm() + "'");
        Assert.assertNull(resFileIDs.getFileIDsData(), "No FileIDsData should be returned.");
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void getFileIDsDeliveredThroughMessage() throws Exception {
        addDescription("Tests the delivery of fileIDs from a pillar at a given URL.");
        addStep("Initialise the variables for this test.",
                "EventManager and GetFileIDsClient should be instantiated.");

        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        GetFileIDsClient getFileIDsClient = createGetFileIDsClient();

        addStep("Ensure the delivery file isn't already present on the http server",
                "Should be remove if it already exists.");

        addStep("Request the delivery of the file ids of a file from the pillar(s). A callback listener should be supplied.",
                "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
        getFileIDsClient.getFileIDs(collectionID, null, DEFAULT_FILE_ID,
                null, testEventHandler);

        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                        + "message to the pillar");

        IdentifyPillarsForGetFileIDsResponse identifyResponse = messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        GetFileIDsRequest receivedGetFileIDsRequest = pillar1Receiver.waitForMessage(GetFileIDsRequest.class);

        for(int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("The pillar sends a getFileIDsProgressResponse to the GetFileIDsClient.",
                "The GetFileIDsClient should notify about the response through the callback interface.");
        GetFileIDsProgressResponse getFileIDsProgressResponse = messageFactory.createGetFileIDsProgressResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(getFileIDsProgressResponse);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);

        addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message",
                "The GetFileIDsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
        GetFileIDsFinalResponse completeMsg = messageFactory.createGetFileIDsFinalResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);

        ResultingFileIDs res = new ResultingFileIDs();
        FileIDsData fileIDsData = new FileIDsData();
        FileIDsDataItems fiddItems = new FileIDsDataItems();
        String fileID = receivedGetFileIDsRequest.getFileIDs().getFileID();
        FileIDsDataItem fidItem = new FileIDsDataItem();
        fidItem.setLastModificationTime(CalendarUtils.getNow());
        fidItem.setFileID(fileID);
        fiddItems.getFileIDsDataItem().add(fidItem);

        fileIDsData.setFileIDsDataItems(fiddItems);
        res.setFileIDsData(fileIDsData);
        completeMsg.setResultingFileIDs(res);

        messageBus.sendMessage(completeMsg);

        addStep("Receive and validate event results for the pillar.",
                "Should be a FileIDsCompletePillarEvent with the ResultingFileIDs containing the list of fileids.");
        for(int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            FileIDsCompletePillarEvent event = (FileIDsCompletePillarEvent) testEventHandler.waitForEvent();
            assertEquals(event.getEventType(), OperationEventType.COMPONENT_COMPLETE);
            ResultingFileIDs resFileIDs = event.getFileIDs();
            Assert.assertNotNull(resFileIDs, "The ResultingFileIDs may not be null.");
            Assert.assertNull(resFileIDs.getResultAddress(), "The results should be sent back through the message, "
                    + "and therefore no resulting address should be returned.");
            Assert.assertNotNull(resFileIDs.getFileIDsData(), "No FileIDsData should be returned.");
            assertEquals(resFileIDs.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(),
                    1, "Response should contain same amount of fileids as requested.");
        }

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void testNoSuchFile() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled.");
        addStep("Setting up variables and such.", "Should be OK.");

        String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";

        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        GetFileIDsClient client = createGetFileIDsClient();
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the file id of a file from the pillar(s). A callback listener should be supplied.",
                "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
        client.getFileIDs(collectionID, null, DEFAULT_FILE_ID,
                deliveryUrl, testEventHandler);

        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                        + "message to the pillar");

        GetFileIDsRequest receivedGetFileIDsRequest = null;
        IdentifyPillarsForGetFileIDsResponse identifyResponse = messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        receivedGetFileIDsRequest = pillar1Receiver.waitForMessage(GetFileIDsRequest.class);

        for(int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
        GetFileIDsFinalResponse completeMsg = messageFactory.createGetFileIDsFinalResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);

        ResponseInfo rfInfo = new ResponseInfo();
        rfInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
        rfInfo.setResponseText("No such file.");
        completeMsg.setResponseInfo(rfInfo);
        completeMsg.setResultingFileIDs(null);

        messageBus.sendMessage(completeMsg);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void testPaging() throws Exception {
        addDescription("Tests the GetFileIDs client correctly handles functionality for limiting results, either by " +
            "timestamp or result count.");

        GetFileIDsClient client = createGetFileIDsClient();
        addStep("Request fileIDs from with MinTimestamp, MaxTimestamp, MaxNumberOfResults set for both pillars .",
            "A IdentifyPillarsForGetFileIDsRequest should be sent.");
        Date timestamp3 = new Date();
        Date timestamp2 =  new Date(timestamp3.getTime() - 100);
        Date timestamp1 =  new Date(timestamp3.getTime() - 1000);
        ContributorQuery query1 = new ContributorQuery(PILLAR1_ID, timestamp1, timestamp2, new Integer(1));
        ContributorQuery query2 = new ContributorQuery(PILLAR2_ID, timestamp2, timestamp3, new Integer(2));
        client.getFileIDs(collectionID, new ContributorQuery[]{query1, query2}, null, null, testEventHandler);

        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
            IdentifyPillarsForGetFileIDsRequest.class);

        addStep("Send a IdentifyPillarsForGetFileIDsResponse from both pillars.",
            "A GetFileIDsRequest should be sent to both pillars with the appropriate MinTimestamp, MaxTimestamp, " +
                "MaxNumberOfResults values.");
        messageBus.sendMessage(messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId));
        messageBus.sendMessage(messageFactory.createIdentifyPillarsForGetFileIDsResponse(
            receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId));

        GetFileIDsRequest receivedGetFileIDsRequest1 = pillar1Receiver.waitForMessage(GetFileIDsRequest.class);
        assertEquals(receivedGetFileIDsRequest1.getMinTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query1.getMinTimestamp()),
                "Unexpected MinTimestamp in GetFileIDsRequest to pillar1.");
        assertEquals(receivedGetFileIDsRequest1.getMaxTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query1.getMaxTimestamp()),
                "Unexpected MaxTimestamp in GetFileIDsRequest to pillar1.");
        assertEquals(receivedGetFileIDsRequest1.getMaxNumberOfResults(),
                BigInteger.valueOf(query1.getMaxNumberOfResults()),
                "Unexpected MaxNumberOfResults in GetFileIDsRequest to pillar1.");

        GetFileIDsRequest receivedGetFileIDsRequest2 = pillar2Receiver.waitForMessage(GetFileIDsRequest.class);
        assertEquals(receivedGetFileIDsRequest2.getMinTimestamp(),
                CalendarUtils.getXmlGregorianCalendar((query2.getMinTimestamp())),
                "Unexpected MinTimestamp in GetFileIDsRequest to pillar2.");
        assertEquals(receivedGetFileIDsRequest2.getMaxTimestamp(),
                CalendarUtils.getXmlGregorianCalendar(query2.getMaxTimestamp()),
                "Unexpected MaxTimestamp in GetFileIDsRequest to pillar2.");
        assertEquals(receivedGetFileIDsRequest2.getMaxNumberOfResults(),
                BigInteger.valueOf(query2.getMaxNumberOfResults()),
                "Unexpected MaxNumberOfResults in GetFileIDsRequest to pillar2.");
    }

    @Test(groups={"regressiontest"})
    public void getFileIDsFromOtherCollection() throws Exception {
        addDescription("Tests the getFileIDs client will correctly try to get from a second collection if required");
        addFixture("Configure collection1 to contain both pillars and collection 2 to only contain pillar2");
        settingsForCUT.getReferenceSettings().getClientSettings().setOperationRetryCount(BigInteger.valueOf(2));
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR2_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getPillarIDs().getPillarID().add(PILLAR2_ID);
        String otherCollection =  settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getID();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetFileIDsClient client = createGetFileIDsClient();

        addStep("Request the putting of a file through the PutClient for collection2",
                "A identification request should be dispatched.");
        client.getFileIDs(otherCollection, null, DEFAULT_FILE_ID, null, testEventHandler);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileIDsRequest.class);
        assertEquals(receivedIdentifyRequestMessage.getCollectionID(), otherCollection);

        addStep("Send an identification response from pillar2.",
                "An COMPONENT_IDENTIFIED event should be generate folled by a IDENTIFICATION_COMPLETE and a " +
                        "REQUEST_SENT. A GetFileIdsFileRequest should be sent to pillar2");
        messageBus.sendMessage(messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId));
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        GetFileIDsRequest receivedRequest = pillar2Receiver.waitForMessage(GetFileIDsRequest.class);
        assertEquals(receivedRequest.getCollectionID(), otherCollection);

        addStep("Send a complete event from the pillar", "The client generates " +
                "a COMPONENT_COMPLETE, followed by a COMPLETE event.");
        GetFileIDsFinalResponse putFileFinalResponse1 = messageFactory.createGetFileIDsFinalResponse(
                receivedRequest, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(putFileFinalResponse1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    /**
     * Creates a new test GetFileIDsClient based on the supplied settings.
     *
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new GetFileIDsClient(Wrapper).
     */
    private GetFileIDsClient createGetFileIDsClient() {
        return new GetFileIDsClientTestWrapper(new ConversationBasedGetFileIDsClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()), testEventManager);
    }

    @Override
    protected MessageResponse createIdentifyResponse(MessageRequest identifyRequest, String from, String to) {
        MessageResponse response = messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                (IdentifyPillarsForGetFileIDsRequest)identifyRequest, from, to);
        return response;
    }

    @Override
    protected MessageResponse createFinalResponse(MessageRequest request, String from, String to) {
        MessageResponse response =  messageFactory.createGetFileIDsFinalResponse(
                (GetFileIDsRequest)request, from, to);
        return response;
    }

    @Override
    protected MessageRequest waitForIdentifyRequest() {
        return collectionReceiver.waitForMessage(IdentifyPillarsForGetFileIDsRequest.class);
    }

    @Override
    protected MessageRequest waitForRequest(MessageReceiver receiver) {
        return receiver.waitForMessage(GetFileIDsRequest.class);
    }

    @Override
    protected void checkNoRequestIsReceived(MessageReceiver receiver) {
        receiver.checkNoMessageIsReceived(GetFileIDsRequest.class);
    }

    @Override
    protected void startOperation(TestEventHandler testEventHandler) {
        GetFileIDsClient getFileIDsClient = createGetFileIDsClient();
        getFileIDsClient.getFileIDs(collectionID, null, null, null, testEventHandler);
    }
}
