/*
 * #%L
 * bitrepository-access-client
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
package org.bitrepository.access.getfile;

import java.math.BigInteger;
import java.net.URL;

import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test class for the 'GetFileClient'.
 */
public class GetFileClientComponentTest extends AbstractGetFileClientTest {

    private static FilePart NO_FILE_PART = null;

    @Test(groups = {"regressiontest"})
    public void getFileFromSpecificPillar() throws Exception {
        addDescription("Tests that the GetClient client works correctly when requesting a file from a specific pillar");

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetFileClient client = createGetFileClient();

        addStep("Request the delivery of a file from pillar2.",
                "A IdentifyPillarsForGetFileRequest will be sent to the pillar with all the correct parameters.");
        String auditTrailInformation = "AuditTrailInfo for getFileFromSpecificPillarTest";
        client.getFileFromSpecificPillar(collectionID, DEFAULT_FILE_ID, null, httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                PILLAR2_ID, testEventHandler, auditTrailInformation);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(receivedIdentifyRequestMessage.getCollectionID(), collectionID);
        Assert.assertNotNull(receivedIdentifyRequestMessage.getCorrelationID());
        assertEquals(receivedIdentifyRequestMessage.getReplyTo(), settingsForCUT.getReceiverDestinationID());
        assertEquals(receivedIdentifyRequestMessage.getFileID(), DEFAULT_FILE_ID);
        assertEquals(receivedIdentifyRequestMessage.getFrom(), settingsForTestClient.getComponentID());
        assertEquals(receivedIdentifyRequestMessage.getDestination(), settingsForTestClient.getCollectionDestination());
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a response from pillar1", "Should be ignored, eg. nothing should happen" );
        IdentifyPillarsForGetFileResponse identifyResponse1 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse1);

        addStep("Send a response from pillar2", "A COMPONENT_IDENTIFIED event should be generated for pillar2 " +
                "followed by a IDENTIFICATION_COMPLETE event. " +
                "The client should then proceed to requesting the file from pillar2 by sending a GetFileRequest with " +
                "the correct and parameters" +
                "generating a REQUEST_SENT event.");
        IdentifyPillarsForGetFileResponse identifyResponse2 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(identifyResponse2);
        ContributorEvent componentIdentifiedEvent2 = (ContributorEvent)testEventHandler.waitForEvent();
        assertEquals(componentIdentifiedEvent2.getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(componentIdentifiedEvent2.getContributorID(), PILLAR2_ID);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        GetFileRequest receivedGetFileRequest = pillar2Receiver.waitForMessage(GetFileRequest.class);
        assertEquals(receivedGetFileRequest.getCollectionID(), collectionID);
        assertEquals(receivedGetFileRequest.getCorrelationID(), identifyResponse2.getCorrelationID());
        assertEquals(receivedGetFileRequest.getReplyTo(), settingsForCUT.getReceiverDestinationID());
        assertEquals(receivedGetFileRequest.getFileID(), DEFAULT_FILE_ID);
        assertEquals(receivedGetFileRequest.getAuditTrailInformation(), auditTrailInformation);
        assertEquals(receivedGetFileRequest.getFrom(), settingsForTestClient.getComponentID());
        assertEquals(receivedGetFileRequest.getDestination(), pillar2DestinationId);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a GetFileProgressResponse.",
                "The client should generating a PROGRESS event.");
        GetFileProgressResponse getFileProgressResponse = messageFactory.createGetFileProgressResponse(
                receivedGetFileRequest, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(getFileProgressResponse);
        ContributorEvent componentProgressEvent2 = (ContributorEvent)testEventHandler.waitForEvent();
        assertEquals(componentProgressEvent2.getEventType(), OperationEventType.PROGRESS);
        assertEquals(componentProgressEvent2.getContributorID(), PILLAR2_ID);

        addStep("Send a GetFileFinalResponse.",
                "The GetFileClient generates a COMPONENT_COMPLETE event followed by a COMPLETE event.");

        GetFileFinalResponse completeMsg = messageFactory.createGetFileFinalResponse(
                receivedGetFileRequest, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(completeMsg);
        ContributorEvent componentCompleteEvent2 = (ContributorEvent)testEventHandler.waitForEvent();
        assertEquals(componentCompleteEvent2.getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(componentCompleteEvent2.getContributorID(), PILLAR2_ID);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void getFileFromSpecificPillarWithFilePart() throws Exception {
        addDescription("Tests that the GetClient client works for a single pillar " +
                "participates. Also validate, that the 'FilePart' can be used.");
        addStep("Set the number of pillars to 1", "");

        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);

        FilePart filePart = new FilePart();
        filePart.setPartLength(BigInteger.TEN);
        filePart.setPartOffSet(BigInteger.ONE);

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetFileClient client = createGetFileClient();

        String chosenPillar = settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().get(0);

        addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.",
                "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
        client.getFileFromSpecificPillar(collectionID, DEFAULT_FILE_ID, filePart, httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                chosenPillar, testEventHandler, null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
        receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
                        "the pillar");

        IdentifyPillarsForGetFileResponse identifyResponse = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, chosenPillar, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        GetFileRequest receivedGetFileRequest = pillar1Receiver.waitForMessage(GetFileRequest.class);

        for(int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("The pillar sends a getFile response to the GetClient.",
                "The GetClient should notify about the response through the callback interface.");
        GetFileProgressResponse getFileProgressResponse = messageFactory.createGetFileProgressResponse(
                receivedGetFileRequest, chosenPillar, pillar1DestinationId);
        messageBus.sendMessage(getFileProgressResponse);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);

        addStep("The file is uploaded to the indicated url and the pillar sends a final response upload message",
                "The GetFileClient notifies that the file is ready through the callback listener and the uploaded " +
                        "file is present.");

        GetFileFinalResponse completeMsg = messageFactory.createGetFileFinalResponse(
                receivedGetFileRequest, chosenPillar, pillar1DestinationId);
        messageBus.sendMessage(completeMsg);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void chooseFastestPillarGetFileClient() throws Exception {
        addDescription("Set the GetClient to retrieve a file as fast as "
                + "possible, where it has to choose between to pillars with "
                + "different times. The messages should be delivered at the "
                + "same time.");
        addStep("Create a GetFileClient configured to use a fast and a slow pillar.", "");

        String averagePillarID = "THE-AVERAGE-PILLAR";
        String fastPillarID = "THE-FAST-PILLAR";
        String slowPillarID = "THE-SLOW-PILLAR";
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(averagePillarID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(fastPillarID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(slowPillarID);
        GetFileClient client = createGetFileClient();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);

        addStep("Defining the variables for the GetFileClient and defining them in the configuration",
                "It should be possible to change the values of the configurations.");

        addStep("Make the GetClient ask for fastest pillar.",
                "It should send message to identify which pillars and a IdentifyPillarsRequestSent notification should be generated.");
        client.getFileFromFastestPillar(collectionID, DEFAULT_FILE_ID, NO_FILE_PART,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                testEventHandler, null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Three pillars send responses. First an average timeToDeliver, then a fast timeToDeliver and last a" +
                " slow timeToDeliver.", "The client should send a getFileRequest to the fast pillar. " +
                "The event handler should receive the following events: " +
                "3 x PillarIdentified, a PillarSelected and a RequestSent");

        IdentifyPillarsForGetFileResponse averageReply = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, averagePillarID, pillar2DestinationId);
        TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
        averageTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        averageTime.setTimeMeasureValue(BigInteger.valueOf(100L));
        averageReply.setTimeToDeliver(averageTime);
        messageBus.sendMessage(averageReply);

        IdentifyPillarsForGetFileResponse fastReply = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
        TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
        fastTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
        fastReply.setTimeToDeliver(fastTime);
        messageBus.sendMessage(fastReply);

        IdentifyPillarsForGetFileResponse slowReply = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, slowPillarID, pillar2DestinationId);
        TimeMeasureTYPE slowTime = new TimeMeasureTYPE();
        slowTime.setTimeMeasureValue(BigInteger.valueOf(1L));
        slowTime.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        slowReply.setTimeToDeliver(slowTime);
        messageBus.sendMessage(slowReply);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        IdentificationCompleteEvent event = (IdentificationCompleteEvent) testEventHandler.waitForEvent();
        assertEquals(event.getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(event.getContributorIDs().get(0), fastPillarID);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        pillar1Receiver.waitForMessage(GetFileRequest.class);
    }

    @Test(groups = {"regressiontest"})
    public void getFileClientWithIdentifyTimeout() throws Exception {
        addDescription("Verify that the GetFile works correct without receiving responses from all pillars.");
        addFixture("Set the identification timeout to 500ms");
        settingsForCUT.getRepositorySettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(500));


        addStep("Call getFile form fastest pillar.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        GetFileClient client = createGetFileClient();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        client.getFileFromFastestPillar(collectionID, DEFAULT_FILE_ID, NO_FILE_PART,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                testEventHandler, null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from pillar1.",
                "A COMPONENT_IDENTIFIED event should be generated.");

        IdentifyPillarsForGetFileResponse identificationResponse1 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identificationResponse1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);

        addStep("Wait 1 second.",
                "A IDENTIFY_TIMEOUT event should be generated, followed by a IDENTIFICATION_COMPLETE.");
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a GetFileRequest should be sent to pillar1.");
        GetFileRequest getFileRequest = pillar1Receiver.waitForMessage(GetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response upload message",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        GetFileFinalResponse completeMsg = messageFactory.createGetFileFinalResponse(
                getFileRequest, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(completeMsg);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);

    }

    @Test(groups = {"regressiontest"})
    public void noIdentifyResponse() throws Exception {
        addDescription("Tests the the GetFileClient handles lack of IdentifyPillarResponses gracefully  ");
        addStep("Set a 500 ms timeout for identifying pillar.", "");

        settingsForCUT.getRepositorySettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(500));
        GetFileClient client = createGetFileClient();

        addStep("Make the GetClient ask for fastest pillar.",
                "It should send message to identify which pillar can respond fastest.");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        client.getFileFromFastestPillar(collectionID, DEFAULT_FILE_ID, NO_FILE_PART,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                testEventHandler, null);
        collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Wait for 1 seconds", "An IdentifyPillarTimeout event should be received followed by a FAILED event");
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void conversationTimeout() throws Exception {
        addDescription("Tests the the GetFileClient handles lack of IdentifyPillarResponses gracefully  ");
        addStep("Set the number of pillars to 100ms and a 300 ms timeout for the conversation.", "");

        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        settingsForCUT.getReferenceSettings().getClientSettings().setConversationTimeout(BigInteger.valueOf(100));
        GetFileClient client = createGetFileClient();

        addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.",
                "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        client.getFileFromSpecificPillar(collectionID, DEFAULT_FILE_ID, NO_FILE_PART,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                PILLAR1_ID, testEventHandler, null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
        receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The pillar sends a response to the identify message.",
                "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
                        "the pillar");

        IdentifyPillarsForGetFileResponse identifyResponse =
                messageFactory.createIdentifyPillarsForGetFileResponse(
                        receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        pillar1Receiver.waitForMessage(GetFileRequest.class);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Wait for 1 second", "An failed event should be generated followed by a FAILED event");
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void testNoSuchFileSpecificPillar() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled on a specific pillar request.");
        addStep("Define 1 pillar.", "");
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        String fileName = "ERROR-NO-SUCH-FILE-ERROR";
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        URL url = httpServerConfiguration.getURL(DEFAULT_FILE_ID);

        addStep("Call getFileFromSpecificPillar.",
                "An identify request should be sent and an IdentifyPillarsRequestSent event should be generate");
        GetFileClient client = createGetFileClient();

        client.getFileFromSpecificPillar(collectionID, fileName, NO_FILE_PART, url, PILLAR1_ID, testEventHandler,
                null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("The specified pillars sends a FILE_NOT_FOUND response",
                "The client should generate 1 PillarIdentified event followed by a operation failed event.");
        IdentifyPillarsForGetFileResponse pillar1Response = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        pillar1Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
        pillar1Response.getResponseInfo().setResponseText("File " +
                receivedIdentifyRequestMessage.getFileID() + " not present on this pillar " + PILLAR1_ID);
        messageBus.sendMessage(pillar1Response);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void testNoSuchFileMultiplePillars() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled when all pillars miss the file.");

        String fileName = "ERROR-NO-SUCH-FILE-ERROR";
        GetFileClient client = createGetFileClient();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        URL url = httpServerConfiguration.getURL(DEFAULT_FILE_ID);

        addStep("Use the default 2 pillars.", "");

        addStep("Call getFileFromFastestPillar.",
                "An identify request should be sent and a IdentifyPillarsRequestSent event should be generate");
        client.getFileFromFastestPillar(collectionID, fileName, NO_FILE_PART, url, testEventHandler, null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Both pillars sends a FILE_NOT_FOUND response",
                "The client should generate 2 PillarIdentified events followed by a Failed event.");

        IdentifyPillarsForGetFileResponse pillar1Response = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        pillar1Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
        pillar1Response.getResponseInfo().setResponseText("File " +
                receivedIdentifyRequestMessage.getFileID() + " not present on this pillar ");
        messageBus.sendMessage(pillar1Response);

        IdentifyPillarsForGetFileResponse pillar2Response = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        pillar2Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
        pillar2Response.getResponseInfo().setResponseText("File " +
                receivedIdentifyRequestMessage.getFileID() + "not present on this pillar " );
        messageBus.sendMessage(pillar2Response);

        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups = {"regressiontest"})
    public void getFileClientWithChecksumPillarInvolved() throws Exception {
        addDescription("Verify that the GetFile works correctly when a checksum pillar respond.");

        addStep("Call getFile form fastest pillar.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        GetFileClient client = createGetFileClient();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        client.getFileFromFastestPillar(collectionID, DEFAULT_FILE_ID, NO_FILE_PART,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                testEventHandler, null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from pillar1 with a REQUEST_NOT_SUPPORTED response code.",
                "No events should be generated.");

        IdentifyPillarsForGetFileResponse identificationResponse1 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        identificationResponse1.getResponseInfo().setResponseCode(ResponseCode.REQUEST_NOT_SUPPORTED);
        messageBus.sendMessage(identificationResponse1);
        testEventHandler.verifyNoEventsAreReceived();

        addStep("Send a identification response from pillar2 with a IDENTIFICATION_POSITIVE response code .",
                "A component COMPONENT_IDENTIFIED event shouæd be generated followed by a IDENTIFICATION_COMPLETE.");
        IdentifyPillarsForGetFileResponse identificationResponse2 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(identificationResponse2);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a GetFileRequest should be sent to pillar2.");
        GetFileRequest getFileRequest = pillar2Receiver.waitForMessage(GetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response upload message",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        GetFileFinalResponse completeMsg = messageFactory.createGetFileFinalResponse(
                getFileRequest, PILLAR2_ID, pillar1DestinationId);
        messageBus.sendMessage(completeMsg);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void singleComponentFailureDuringIdentify() throws Exception {
        addDescription("Verify that the GetFile reports a complete (not failed), in case of a component failing " +
                "during the identify phase.");

        addStep("Call getFile from the fastest pillar.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        GetFileClient client = createGetFileClient();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        client.getFileFromFastestPillar(collectionID, DEFAULT_FILE_ID, NO_FILE_PART,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                testEventHandler, null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identification response from pillar1 with a IDENTIFICATION_NEGATIVE response code .",
                "No events should be generated.");

        IdentifyPillarsForGetFileResponse identificationResponse1 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        identificationResponse1.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        messageBus.sendMessage(identificationResponse1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);

        addStep("Send a identification response from pillar2 with a IDENTIFICATION_POSITIVE response code .",
                "A component COMPONENT_IDENTIFIED event shouæd be generated followed by a IDENTIFICATION_COMPLETE.");
        IdentifyPillarsForGetFileResponse identificationResponse2 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(identificationResponse2);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("Verify that the client continues to the performing phase.",
                "A REQUEST_SENT event should be generated and a GetFileRequest should be sent to pillar2.");
        GetFileRequest getFileRequest = pillar2Receiver.waitForMessage(GetFileRequest.class);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a final response upload message",
                "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
        GetFileFinalResponse completeMsg = messageFactory.createGetFileFinalResponse(
                getFileRequest, PILLAR2_ID, pillar1DestinationId);
        messageBus.sendMessage(completeMsg);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void failureDuringPerform() throws Exception {
        addDescription("Verify that the GetFile reports a failed operation, in case of a component failing " +
                "during the performing phase.");

        addStep("Request a getFile from the fastest pillar.",
                "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
        GetFileClient client = createGetFileClient();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        client.getFileFromFastestPillar(collectionID, DEFAULT_FILE_ID, NO_FILE_PART,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                testEventHandler, null);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);

        addStep("Send a identification response from pillar1 and pillar2 with pillar1 the fastest.",
                "Pillar1 should be selected and a GetFileRequest should be sent.");
        IdentifyPillarsForGetFileResponse identificationResponse1 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identificationResponse1);
        IdentifyPillarsForGetFileResponse identificationResponse2 = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
        TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
        averageTime.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        averageTime.setTimeMeasureValue(BigInteger.valueOf(10L));
        identificationResponse2.setTimeToDeliver(averageTime);
        messageBus.sendMessage(identificationResponse2);
        GetFileRequest getFileRequest = pillar1Receiver.waitForMessage(GetFileRequest.class);

        addStep("Send a failure response from pillar1",
                "A COMPONENT_FAILED event should be generated followed by at FAILURE event.");
        testEventHandler.clearEvents();
        GetFileFinalResponse completeMsg = messageFactory.createGetFileFinalResponse(
                getFileRequest, PILLAR1_ID, pillar1DestinationId);
        completeMsg.getResponseInfo().setResponseCode(ResponseCode.FAILURE);
        messageBus.sendMessage(completeMsg);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }


    @Test(groups={"regressiontest"})
    public void getFileFromOtherCollection() throws Exception {
        addDescription("Tests the getFiles client will correctly try to get from a second collection if required");
        addFixture("Configure collection1 to contain both pillars and collection 2 to only contain pillar2");
        settingsForCUT.getReferenceSettings().getClientSettings().setOperationRetryCount(BigInteger.valueOf(2));
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR2_ID);
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getPillarIDs().getPillarID().add(PILLAR2_ID);
        String otherCollection =  settingsForCUT.getRepositorySettings().getCollections().getCollection().get(1).getID();
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetFileClient client = createGetFileClient();

        addStep("Request the getting of a file through the client for collection2",
                "A identification request should be dispatched.");

        client.getFileFromFastestPillar(otherCollection, DEFAULT_FILE_ID, NO_FILE_PART,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID),
                testEventHandler, null);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        assertEquals(receivedIdentifyRequestMessage.getCollectionID(), otherCollection);

        addStep("Send an identification response from pillar2.",
                "An COMPONENT_IDENTIFIED event should be generate folled by a IDENTIFICATION_COMPLETE and a " +
                        "REQUEST_SENT. A GetFileIdsFileRequest should be sent to pillar2");
        messageBus.sendMessage(messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId));
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        GetFileRequest receivedRequest = pillar2Receiver.waitForMessage(GetFileRequest.class);
        assertEquals(receivedRequest.getCollectionID(), otherCollection);

        addStep("Send a complete event from the pillar", "The client generates " +
                "a COMPONENT_COMPLETE, followed by a COMPLETE event.");
        GetFileFinalResponse putFileFinalResponse1 = messageFactory.createGetFileFinalResponse(
                receivedRequest, PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(putFileFinalResponse1);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    /**
     * Creates a new test GetFileClient based on the supplied settings.
     *
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new GetFileClient(Wrapper).
     */
    private GetFileClient createGetFileClient() {
        return new GetFileClientTestWrapper(new ConversationBasedGetFileClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()), testEventManager);
    }
}
