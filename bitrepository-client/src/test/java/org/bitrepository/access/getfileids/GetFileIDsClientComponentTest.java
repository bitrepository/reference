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

import io.qameta.allure.junit5.AllureJunit5;
import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
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
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addFixture;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

/**
 * Test class for the 'GetFileIDsClient'.
 */
@ExtendWith(AllureJunit5.class)
@ExtendWith(SuiteInfoParameterResolver.class)
class GetFileIDsClientComponentTest extends DefaultClientTest {

    private TestGetFileIDsMessageFactory messageFactory;

    /**
     * Set up the test scenario before running the tests in this class.
     *
     */
    @BeforeEach
    void setUp() {
        // TODO getFileIDsFromFastestPillar settings
        messageFactory = new TestGetFileIDsMessageFactory(settingsForTestClient.getComponentID());
    }

    @Test
    @Tag("regressiontest")
    void verifyGetFileIDsClientFromFactory() {
        Assertions.assertInstanceOf(ConversationBasedGetFileIDsClient.class,
                AccessComponentFactory.getInstance().createGetFileIDsClient(settingsForCUT, securityManager,
                        settingsForTestClient.getComponentID()),
                "The default GetFileClient from the Access factory should be of the type '" +
                ConversationBasedGetFileIDsClient.class.getName() + "'.");
    }

    @Test
    @Tag("regressiontest")
    @DisplayName("Test that the GetFileIDsClient can be created from the AccessComponentFactory.")
    void getFileIDsDeliveredAtUrl() throws Exception {
        addDescription("Tests the delivery of fileIDs from a pillar at a given URL.");
        addStep("Initialise the variables for this test.",
                "EventManager and GetFileIDsClient should be instantiated.");

        String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(DEFAULT_FILE_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        GetFileIDsClient getFileIDsClient = createGetFileIDsClient();
        URL deliveryUrl = httpServerConfiguration.getURL(deliveryFilename);

        addStep("Request the delivery of the file ids of a file from the pillar(s). A callback listener should be " +
                        "supplied.",
                "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
        getFileIDsClient.getFileIDs(collectionID, null, DEFAULT_FILE_ID, deliveryUrl, testEventHandler);

        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsRequest.class);
        Assertions.assertEquals(collectionID, receivedIdentifyRequestMessage.getCollectionID());
        Assertions.assertNotNull(receivedIdentifyRequestMessage.getCorrelationID());
        Assertions.assertEquals(settingsForCUT.getReceiverDestinationID(), receivedIdentifyRequestMessage.getReplyTo());
        Assertions.assertEquals(PILLAR1_ID, receivedIdentifyRequestMessage.getTo());
        Assertions.assertEquals(settingsForTestClient.getComponentID(), receivedIdentifyRequestMessage.getFrom());
        Assertions.assertEquals(settingsForTestClient.getCollectionDestination(), receivedIdentifyRequestMessage.getDestination());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                        + "message to the pillar");
        IdentifyPillarsForGetFileIDsResponse identifyResponse =
                messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                        receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());

        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());
        GetFileIDsRequest receivedGetFileIDsRequest = pillar1Receiver.waitForMessage(GetFileIDsRequest.class);
        Assertions.assertEquals(collectionID, receivedGetFileIDsRequest.getCollectionID());
        Assertions.assertEquals(receivedIdentifyRequestMessage.getCorrelationID(), receivedGetFileIDsRequest.getCorrelationID());
        Assertions.assertEquals(settingsForCUT.getReceiverDestinationID(), receivedGetFileIDsRequest.getReplyTo());
        Assertions.assertEquals(settingsForTestClient.getComponentID(), receivedGetFileIDsRequest.getFrom());
        Assertions.assertEquals(pillar1DestinationId, receivedGetFileIDsRequest.getDestination());

        addStep("The pillar sends a getFileIDsProgressResponse to the GetFileIDsClient.",
                "The GetFileIDsClient should notify about the response through the callback interface.");
        GetFileIDsProgressResponse getFileIDsProgressResponse = messageFactory.createGetFileIDsProgressResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(getFileIDsProgressResponse);
        Assertions.assertEquals(OperationEvent.OperationEventType.PROGRESS, testEventHandler.waitForEvent().getEventType());

        addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload " +
                        "message",
                "The GetFileIDsClient notifies that the file is ready through the callback listener " +
                        "and the uploaded file is present.");
        GetFileIDsFinalResponse completeMsg = messageFactory.createGetFileIDsFinalResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);

        ResultingFileIDs res = new ResultingFileIDs();
        res.setResultAddress(receivedGetFileIDsRequest.getResultAddress());
        completeMsg.setResultingFileIDs(res);

        messageBus.sendMessage(completeMsg);

        addStep("Receive and validate event results for the pillar.",
                "Should be a FileIDsCompletePillarEvent with the ResultingFileIDs containing only the URL.");
        FileIDsCompletePillarEvent event = (FileIDsCompletePillarEvent) testEventHandler.waitForEvent();
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, event.getEventType());
        ResultingFileIDs resFileIDs = event.getFileIDs();
        Assertions.assertNotNull(resFileIDs, "The ResultingFileIDs may not be null.");
        Assertions.assertTrue(resFileIDs.getResultAddress().contains(deliveryUrl.toExternalForm()),
                "The resulting address'" + resFileIDs.getResultAddress() + "' should contain the argument address: '"
                        + deliveryUrl.toExternalForm() + "'");
        Assertions.assertNull(resFileIDs.getFileIDsData(), "No FileIDsData should be returned.");
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPLETE, testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag("regressiontest")
    void getFileIDsDeliveredThroughMessage() throws Exception {
        addDescription("Tests the delivery of fileIDs from a pillar at a given URL.");
        addStep("Initialise the variables for this test.",
                "EventManager and GetFileIDsClient should be instantiated.");

        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        GetFileIDsClient getFileIDsClient = createGetFileIDsClient();

        addStep("Ensure the delivery file isn't already present on the http server",
                "Should be remove if it already exists.");

        addStep("Request the delivery of the file ids of a file from the pillar(s). A callback listener should be " +
                        "supplied.",
                "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
        getFileIDsClient.getFileIDs(collectionID, null, DEFAULT_FILE_ID,
                null, testEventHandler);

        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                        + "message to the pillar");

        IdentifyPillarsForGetFileIDsResponse identifyResponse =
                messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                        receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        GetFileIDsRequest receivedGetFileIDsRequest = pillar1Receiver.waitForMessage(GetFileIDsRequest.class);

        for (int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());
        }
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("The pillar sends a getFileIDsProgressResponse to the GetFileIDsClient.",
                "The GetFileIDsClient should notify about the response through the callback interface.");
        GetFileIDsProgressResponse getFileIDsProgressResponse = messageFactory.createGetFileIDsProgressResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(getFileIDsProgressResponse);
        Assertions.assertEquals(OperationEvent.OperationEventType.PROGRESS, testEventHandler.waitForEvent().getEventType());

        addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload " +
                        "message",
                "The GetFileIDsClient notifies that the file is ready through the callback listener and the uploaded " +
                        "file is present.");
        GetFileIDsFinalResponse completeMsg = messageFactory.createGetFileIDsFinalResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);

        ResultingFileIDs res = new ResultingFileIDs();
        FileIDsData fileIDsData = new FileIDsData();
        FileIDsDataItems fiddItems = new FileIDsDataItems();
        String fileID = receivedGetFileIDsRequest.getFileIDs().getFileID();
        FileIDsDataItem fidItem = new FileIDsDataItem();
        fidItem.setLastModificationTime(CalendarUtils.getXmlGregorianCalendar(Instant.now()));
        fidItem.setFileID(fileID);
        fiddItems.getFileIDsDataItem().add(fidItem);

        fileIDsData.setFileIDsDataItems(fiddItems);
        res.setFileIDsData(fileIDsData);
        completeMsg.setResultingFileIDs(res);

        messageBus.sendMessage(completeMsg);

        addStep("Receive and validate event results for the pillar.",
                "Should be a FileIDsCompletePillarEvent with the ResultingFileIDs containing the list of fileids.");
        for (int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            FileIDsCompletePillarEvent event = (FileIDsCompletePillarEvent) testEventHandler.waitForEvent();
            Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, event.getEventType());
            ResultingFileIDs resFileIDs = event.getFileIDs();
            Assertions.assertNotNull(resFileIDs, "The ResultingFileIDs may not be null.");
            Assertions.assertNull(resFileIDs.getResultAddress(), "The results should be sent back through the message, "
                    + "and therefore no resulting address should be returned.");
            Assertions.assertNotNull(resFileIDs.getFileIDsData(), "No FileIDsData should be returned.");
            Assertions.assertEquals(1, resFileIDs.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(),
                    "Response should contain same amount of fileids as requested.");
        }

        Assertions.assertEquals(OperationEvent.OperationEventType.COMPLETE, testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag("regressiontest")
    void testNoSuchFile() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled.");
        addStep("Setting up variables and such.", "Should be OK.");

        String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";

        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        GetFileIDsClient client = createGetFileIDsClient();
        URL deliveryUrl = httpServerConfiguration.getURL(deliveryFilename);

        addStep("Request the delivery of the file id of a file from the pillar(s). A callback listener should be " +
                        "supplied.",
                "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
        client.getFileIDs(collectionID, null, DEFAULT_FILE_ID,
                deliveryUrl, testEventHandler);

        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                        + "message to the pillar");

        GetFileIDsRequest receivedGetFileIDsRequest;
        IdentifyPillarsForGetFileIDsResponse identifyResponse =
                messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                        receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        receivedGetFileIDsRequest = pillar1Receiver.waitForMessage(GetFileIDsRequest.class);

        for (int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());
        }
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
        GetFileIDsFinalResponse completeMsg = messageFactory.createGetFileIDsFinalResponse(
                receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);

        ResponseInfo rfInfo = new ResponseInfo();
        rfInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
        rfInfo.setResponseText("No such file.");
        completeMsg.setResponseInfo(rfInfo);
        completeMsg.setResultingFileIDs(null);

        messageBus.sendMessage(completeMsg);

        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_FAILED, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.FAILED, testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag("regressiontest")
    void testPaging() {
        addDescription("Tests the GetFileIDs client correctly handles functionality for limiting results, either by " +
                "timestamp or result count.");

        GetFileIDsClient client = createGetFileIDsClient();
        addStep("Request fileIDs from with MinTimestamp, MaxTimestamp, MaxNumberOfResults set for both pillars .",
                "A IdentifyPillarsForGetFileIDsRequest should be sent.");
        Instant timestamp3 = Instant.now();
        Instant timestamp2 = timestamp3.minusMillis(100);
        Instant timestamp1 = timestamp3.minusMillis(1000);
        ContributorQuery query1 = new ContributorQuery(PILLAR1_ID, timestamp1, timestamp2, 1);
        ContributorQuery query2 = new ContributorQuery(PILLAR2_ID, timestamp2, timestamp3, 2);
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
        Assertions.assertEquals(CalendarUtils.getXmlGregorianCalendar(query1.getMinTimestampInstant()), receivedGetFileIDsRequest1.getMinTimestamp(),
                "Unexpected MinTimestamp in GetFileIDsRequest to pillar1.");
        Assertions.assertEquals(CalendarUtils.getXmlGregorianCalendar(query1.getMaxTimestampInstant()), receivedGetFileIDsRequest1.getMaxTimestamp(),
                "Unexpected MaxTimestamp in GetFileIDsRequest to pillar1.");
        Assertions.assertEquals(BigInteger.valueOf(query1.getMaxNumberOfResults()), receivedGetFileIDsRequest1.getMaxNumberOfResults(),
                "Unexpected MaxNumberOfResults in GetFileIDsRequest to pillar1.");

        GetFileIDsRequest receivedGetFileIDsRequest2 = pillar2Receiver.waitForMessage(GetFileIDsRequest.class);
        Assertions.assertEquals(CalendarUtils.getXmlGregorianCalendar((query2.getMinTimestampInstant())),
                receivedGetFileIDsRequest2.getMinTimestamp(), "Unexpected MinTimestamp in GetFileIDsRequest to " +
                        "pillar2.");
        Assertions.assertEquals(CalendarUtils.getXmlGregorianCalendar(query2.getMaxTimestampInstant()), receivedGetFileIDsRequest2.getMaxTimestamp(),
                "Unexpected MaxTimestamp in GetFileIDsRequest to pillar2.");
        Assertions.assertEquals(BigInteger.valueOf(query2.getMaxNumberOfResults()), receivedGetFileIDsRequest2.getMaxNumberOfResults(),
                "Unexpected MaxNumberOfResults in GetFileIDsRequest to pillar2.");
    }

    @Test
    @Tag("regressiontest")
    void getFileIDsFromOtherCollection() throws Exception {
        addDescription("Tests the getFileIDs client will correctly try to get from a second collection if required");
        addFixture("Configure collection1 to contain both pillars and collection 2 to only contain pillar2");
        settingsForCUT.getReferenceSettings().getClientSettings().setOperationRetryCount(BigInteger.valueOf(2));
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR2_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getPillarIDs().getPillarID().add(PILLAR2_ID);
        String otherCollection = settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getID();
        TestEventHandler testEventHandler = new TestEventHandler();
        GetFileIDsClient client = createGetFileIDsClient();

        addStep("Request the putting of a file through the PutClient for collection2",
                "A identification request should be dispatched.");
        client.getFileIDs(otherCollection, null, DEFAULT_FILE_ID, null, testEventHandler);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());
        IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileIDsRequest.class);
        Assertions.assertEquals(otherCollection, receivedIdentifyRequestMessage.getCollectionID());

        addStep("Send an identification response from pillar2.",
                "An COMPONENT_IDENTIFIED event should be generate folled by a IDENTIFICATION_COMPLETE and a " +
                        "REQUEST_SENT. A GetFileIdsFileRequest should be sent to pillar2");
        messageBus.sendMessage(messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId));
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());
        GetFileIDsRequest receivedRequest = pillar2Receiver.waitForMessage(GetFileIDsRequest.class);
        Assertions.assertEquals(otherCollection, receivedRequest.getCollectionID());

        addStep("Send a complete event from the pillar", "The client generates " +
                "a COMPONENT_COMPLETE, followed by a COMPLETE event.");
        GetFileIDsFinalResponse putFileFinalResponse1 = messageFactory.createGetFileIDsFinalResponse(
                receivedRequest, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(putFileFinalResponse1);
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPLETE, testEventHandler.waitForEvent().getEventType());
    }

    /**
     * Creates a new test GetFileIDsClient based on the supplied settings.
     * <p>
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     *
     * @return A new GetFileIDsClient(Wrapper).
     */
    private GetFileIDsClient createGetFileIDsClient() {
        return new GetFileIDsClientTestWrapper(new ConversationBasedGetFileIDsClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()));
    }

    @Override
    protected MessageResponse createIdentifyResponse(MessageRequest identifyRequest, String from, String to) {
        return messageFactory.createIdentifyPillarsForGetFileIDsResponse(
                (IdentifyPillarsForGetFileIDsRequest) identifyRequest, from, to);
    }

    @Override
    protected MessageResponse createFinalResponse(MessageRequest request, String from, String to) {
        return messageFactory.createGetFileIDsFinalResponse(
                (GetFileIDsRequest) request, from, to);
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
