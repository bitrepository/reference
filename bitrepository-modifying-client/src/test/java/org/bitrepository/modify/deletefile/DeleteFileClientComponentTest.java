/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.modify.deletefile;

import java.math.BigInteger;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.client.DefaultFixtureClientTest;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.modify.ModifyComponentFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DeleteFileClientComponentTest extends DefaultFixtureClientTest {
    private TestDeleteFileMessageFactory messageFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialise() throws Exception {
        if(useMockupPillar()) {
            messageFactory = new TestDeleteFileMessageFactory(settingsForCUT.getCollectionID());
        }
    }

    @Test(groups={"regressiontest"})
    public void verifyDeleteClientFromFactory() {
        addDescription("Testing the initialization through the ModifyComponentFactory.");
        addStep("Use the ModifyComponentFactory to instantiate a PutFileClient.",
                "It should be an instance of SimplePutFileClient");
        DeleteFileClient dfc = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(
                settingsForCUT, securityManager, settingsForTestClient.getComponentID());
        Assert.assertTrue(dfc instanceof ConversationBasedDeleteFileClient, "The DeleteFileClient '" + dfc
                + "' should be instance of '" + ConversationBasedDeleteFileClient.class.getName() + "'");
    }

    @Test(groups={"regressiontest"})
    public void deleteClientTester() throws Exception {
        addDescription("Tests the DeleteClient. Makes a whole conversation for the delete client for a 'good' scenario.");
        addStep("Initialise the number of pillars to one", "Should be OK.");

        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        String checksum = "123checksum321";
        ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
        checksumForPillar.setChecksumType(ChecksumType.MD5);
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumSpec(checksumForPillar);
        checksumData.setChecksumValue(checksum.getBytes());
        checksumData.setCalculationTimestamp(CalendarUtils.getEpoch());

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        addStep("Request a file to be deleted on all pillars (which means only the default pillar).",
                "A IdentifyPillarsForDeleteFileRequest should be sent to the pillar.");
        deleteClient.deleteFileAtAllPillars(DEFAULT_FILE_ID, checksumData, checksumRequest, testEventHandler, null);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = null;
            receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                    IdentifyPillarsForDeleteFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage,
                    messageFactory.createIdentifyPillarsForDeleteFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo(),
                            DEFAULT_FILE_ID,
                            settingsForTestClient.getComponentID()
                    ));
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");

        DeleteFileRequest receivedDeleteFileRequest = null;
            IdentifyPillarsForDeleteFileResponse identifyResponse
                    = messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                    PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
            messageBus.sendMessage(identifyResponse);
            receivedDeleteFileRequest = pillar1Destination.waitForMessage(DeleteFileRequest.class);
            Assert.assertEquals(receivedDeleteFileRequest,
                    messageFactory.createDeleteFileRequest(
                            PILLAR1_ID, pillar1DestinationId,
                            receivedDeleteFileRequest.getReplyTo(),
                            receivedDeleteFileRequest.getCorrelationID(),
                            DEFAULT_FILE_ID,
                            receivedDeleteFileRequest.getChecksumDataForExistingFile(),
                            receivedDeleteFileRequest.getChecksumRequestForExistingFile(),
                            settingsForTestClient.getComponentID()
                    ));

        addStep("Validate the steps of the DeleteClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("The pillar sends a progress response to the DeleteClient.", "Should be caught by the event handler.");
        if(useMockupPillar()) {
            DeleteFileProgressResponse deleteFileProgressResponse = messageFactory.createDeleteFileProgressResponse(
                    receivedDeleteFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(deleteFileProgressResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);

        addStep("Send a final response message to the DeleteClient.",
                "Should be caught by the event handler. First a PartiallyComplete, then a Complete.");
        DeleteFileFinalResponse deleteFileFinalResponse = messageFactory.createDeleteFileFinalResponse(
                receivedDeleteFileRequest, PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
        messageBus.sendMessage(deleteFileFinalResponse);
        for(int i = 1; i < 2* settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            OperationEventType eventType = testEventHandler.waitForEvent().getEventType();
            Assert.assertTrue( (eventType == OperationEventType.COMPONENT_COMPLETE)
                    || (eventType == OperationEventType.PROGRESS),
                    "Expected either PartiallyComplete or Progress, but was: " + eventType);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups={"regressiontest"})
    public void fileAlreadyDeletedFromPillar() throws Exception {
        addDescription("Test that a delete on a pillar completes successfully when the file is missing " +
                "(has already been deleted). This is a test of the Idempotent behaviour of the delete client");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        addStep("Request a file to be deleted on pillar1.",
                "A IdentifyPillarsForDeleteFileRequest should be sent " +
                        "and a IDENTIFY_REQUEST_SENT event should be generated.");
        deleteClient.deleteFile(DEFAULT_FILE_ID, PILLAR1_ID, TestFileHelper.getDefaultFileChecksum(), null, testEventHandler, null);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identify response from Pillar1 with a missing file response.",
                "The client should generate a COMPONENT_IDENTIFIED, a COMPONENT_COMPLETE and " +
                        "an IDENTIFICATION_COMPLETE event.");
        IdentifyPillarsForDeleteFileResponse identifyResponse = messageFactory.createIdentifyPillarsForDeleteFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
        identifyResponse.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
        messageBus.sendMessage(identifyResponse);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);

        addStep("The client should then continue to the performing phase and finish immediately as the pillar " +
                "has already had the file removed apparently .",
                "The client should generate a COMPLETE event.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);

        addStep("Send a identify response from Pillar2", "The response should be ignored");
        testEventHandler.verifyNoEventsAreReceived();
    }

    @Test(groups={"regressiontest"})
    public void deleteClientIdentificationTimeout() throws Exception {
        addDescription("Tests the handling of a failed identification for the DeleteClient");
        addStep("Initialise the number of pillars and the DeleteClient. Sets the identification timeout to 1 sec.",
                "Should be OK.");

        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        settingsForCUT.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(1000L));
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        String checksum = "123checksum321";
        ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
        checksumForPillar.setChecksumType(ChecksumType.MD5);
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumSpec(checksumForPillar);
        checksumData.setChecksumValue(checksum.getBytes());
        checksumData.setCalculationTimestamp(CalendarUtils.getEpoch());

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        addStep("Request a file to be deleted on the default pillar.",
                "A IdentifyPillarsForDeleteFileRequest should be sent to the pillar.");
        deleteClient.deleteFile(DEFAULT_FILE_ID, PILLAR1_ID, checksumData, checksumRequest, testEventHandler, null);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = null;
        if(useMockupPillar()) {
            receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                    IdentifyPillarsForDeleteFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage,
                    messageFactory.createIdentifyPillarsForDeleteFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo(),
                            DEFAULT_FILE_ID,
                            settingsForTestClient.getComponentID()
                    ));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Do not respond. Just await the timeout.",
                "Should make send a Failure event to the eventhandler.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups={"regressiontest"})
    public void deleteClientOperationTimeout() throws Exception {
        addDescription("Tests the handling of a failed operation for the DeleteClient");
        addStep("Initialise the number of pillars and the DeleteClient. Sets the operation timeout to 1 sec.",
                "Should be OK.");

        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        settingsForCUT.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(1000L));
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        String checksum = "123checksum321";
        ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
        checksumForPillar.setChecksumType(ChecksumType.MD5);
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumSpec(checksumForPillar);
        checksumData.setChecksumValue(checksum.getBytes());
        checksumData.setCalculationTimestamp(CalendarUtils.getEpoch());

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        addStep("Request a file to be deleted on the default pillar.",
                "A IdentifyPillarsForDeleteFileRequest should be sent to the pillar.");
        deleteClient.deleteFile(DEFAULT_FILE_ID, PILLAR1_ID, checksumData, checksumRequest, testEventHandler, null);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = null;
        if(useMockupPillar()) {
            receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                    IdentifyPillarsForDeleteFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage,
                    messageFactory.createIdentifyPillarsForDeleteFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo(),
                            DEFAULT_FILE_ID,
                            settingsForTestClient.getComponentID()
                    ));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");

        DeleteFileRequest receivedDeleteFileRequest = null;
        if(useMockupPillar()) {
            IdentifyPillarsForDeleteFileResponse identifyResponse
                    = messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                    PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
            messageBus.sendMessage(identifyResponse);
            receivedDeleteFileRequest = pillar1Destination.waitForMessage(DeleteFileRequest.class);
            Assert.assertEquals(receivedDeleteFileRequest,
                    messageFactory.createDeleteFileRequest(
                            PILLAR1_ID, pillar1DestinationId,
                            receivedDeleteFileRequest.getReplyTo(),
                            receivedDeleteFileRequest.getCorrelationID(),
                            DEFAULT_FILE_ID,
                            receivedDeleteFileRequest.getChecksumDataForExistingFile(),
                            receivedDeleteFileRequest.getChecksumRequestForExistingFile(),
                            settingsForTestClient.getComponentID()
                    ));
        }

        addStep("Validate the steps of the DeleteClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Do not respond. Just await the timeout.",
                "Should make send a Failure event to the eventhandler.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups={"regressiontest"})
    public void deleteClientPillarFailedDuringPerform() throws Exception {
        addDescription("Tests the handling of a operation failure for the DeleteClient. ");
        addStep("Initialise the number of pillars to one", "Should be OK.");

        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        String checksum = "123checksum321";
        ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
        checksumForPillar.setChecksumType(ChecksumType.MD5);
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumSpec(checksumForPillar);
        checksumData.setChecksumValue(checksum.getBytes());
        checksumData.setCalculationTimestamp(CalendarUtils.getEpoch());

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        addStep("Request a file to be deleted on the default pillar.",
                "A IdentifyPillarsForDeleteFileRequest should be sent to the pillar.");
        deleteClient.deleteFile(DEFAULT_FILE_ID, PILLAR1_ID, checksumData, checksumRequest, testEventHandler, null);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = null;
        if(useMockupPillar()) {
            receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                    IdentifyPillarsForDeleteFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage,
                    messageFactory.createIdentifyPillarsForDeleteFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo(),
                            DEFAULT_FILE_ID,
                            settingsForTestClient.getComponentID()
                    ));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");

        DeleteFileRequest receivedDeleteFileRequest = null;
        if(useMockupPillar()) {
            IdentifyPillarsForDeleteFileResponse identifyResponse
                    = messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                    PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
            messageBus.sendMessage(identifyResponse);
            receivedDeleteFileRequest = pillar1Destination.waitForMessage(DeleteFileRequest.class);
            Assert.assertEquals(receivedDeleteFileRequest,
                    messageFactory.createDeleteFileRequest(
                            PILLAR1_ID, pillar1DestinationId,
                            receivedDeleteFileRequest.getReplyTo(),
                            receivedDeleteFileRequest.getCorrelationID(),
                            DEFAULT_FILE_ID,
                            receivedDeleteFileRequest.getChecksumDataForExistingFile(),
                            receivedDeleteFileRequest.getChecksumRequestForExistingFile(),
                            settingsForTestClient.getComponentID()
                    ));
        }

        addStep("Validate the steps of the DeleteClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a failed response message to the DeleteClient.",
                "Should be caught by the event handler. First a COMPONENT_FAILED, then a COMPLETE.");
        if(useMockupPillar()) {
            DeleteFileFinalResponse deleteFileFinalResponse = messageFactory.createDeleteFileFinalResponse(
                    receivedDeleteFileRequest, PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.FAILURE);
            ri.setResponseText("Verifying that a failure can be understood!");
            deleteFileFinalResponse.setResponseInfo(ri);
            messageBus.sendMessage(deleteFileFinalResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups={"regressiontest"})
    public void deleteClientSpecifiedPillarFailedDuringIdentification() throws Exception {
        addDescription("Tests the handling of a identification failure for a pillar for the DeleteClient. ");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        addStep("Request a file to be deleted on the pillar1.",
                "A IdentifyPillarsForDeleteFileRequest should be sent.");
        deleteClient.deleteFile(
                DEFAULT_FILE_ID, PILLAR1_ID, TestFileHelper.getDefaultFileChecksum(), null, testEventHandler, null);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send a failed response from pillar1.",
                "The client should generate a COMPONENT_FAILED followed by a OperationEventType.FAILED.");

        IdentifyPillarsForDeleteFileResponse identifyResponse
                = messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
        identifyResponse.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        messageBus.sendMessage(identifyResponse);

        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups={"regressiontest"})
    public void deleteClientOtherPillarFailedDuringIdentification() throws Exception {
        addDescription("Tests the handling of a identification failure for a pillar for the DeleteClient. ");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        addStep("Request a file to be deleted on the pillar1.",
                "A IdentifyPillarsForDeleteFileRequest should be sent and a IDENTIFY_REQUEST_SENT event should be generated.");
        deleteClient.deleteFile(
                DEFAULT_FILE_ID, PILLAR1_ID, TestFileHelper.getDefaultFileChecksum(), null, testEventHandler, null);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send a failed response from pillar2.",
                "The response should be ignored as it isn't relevant for the operation.");
        IdentifyPillarsForDeleteFileResponse identifyResponse
                = messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                PILLAR2_ID, pillar2DestinationId, DEFAULT_FILE_ID);
        identifyResponse.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        messageBus.sendMessage(identifyResponse);
        testEventHandler.verifyNoEventsAreReceived();

        addStep("Send a ok response from pillar1.",
                "The client should generate the following events: COMPONENT_IDENTIFIED, IDENTIFICATION_COMPLETE and " +
                        "REQUEST_SENT. A delete request should be sent to pillar1");
        IdentifyPillarsForDeleteFileResponse identifyResponse2 =
                messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                        PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
        messageBus.sendMessage(identifyResponse2);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        DeleteFileRequest receivedDeleteFileRequest = pillar1Destination.waitForMessage(DeleteFileRequest.class);

        addStep("Send a final response message from pillar 1 to the DeleteClient.",
                "Should produce a COMPONENT_COMPLETE event followed by a COMPLETE event.");
        messageBus.sendMessage(messageFactory.createDeleteFileFinalResponse(
                receivedDeleteFileRequest, PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID));
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups={"regressiontest"})
    public void deleteOnChecksumPillar() throws Exception {
        addDescription("Verify that the DeleteClient works correctly when a checksum pillar is present. ");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        addStep("Request a file to be deleted on the pillar1.",
                "A IdentifyPillarsForDeleteFileRequest should be sent and a IDENTIFY_REQUEST_SENT event should be generated.");
        deleteClient.deleteFile(
                DEFAULT_FILE_ID, PILLAR1_ID, TestFileHelper.getDefaultFileChecksum(), null, testEventHandler, null);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send an identification response from pillar2.",
                "This should be ignored, eg. no events should be generated.");
        IdentifyPillarsForDeleteFileResponse identifyResponse2 =
                messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                        PILLAR2_ID, pillar2DestinationId, DEFAULT_FILE_ID);
        messageBus.sendMessage(identifyResponse2);
        testEventHandler.verifyNoEventsAreReceived();

        addStep("Send a response from pillar1 with PillarChecksumSpec element set, indicating that this is a " +
                "checksum pillar.",
                "Following events should be generated: COMPONENT_IDENTIFIED, IDENTIFICATION_COMPLETE and a REQUEST_SENT." +
                        "a request should be sent to pillar1");
        IdentifyPillarsForDeleteFileResponse identifyResponse
                = messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
        ChecksumSpecTYPE checksumSpecTYPEFromPillar = new ChecksumSpecTYPE();
        checksumSpecTYPEFromPillar.setChecksumType(ChecksumType.MD5);
        identifyResponse.setPillarChecksumSpec(checksumSpecTYPEFromPillar);
        messageBus.sendMessage(identifyResponse);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        Assert.assertNotNull(pillar1Destination.waitForMessage(DeleteFileRequest.class));
        pillar2Destination.checkNoMessageIsReceived(DeleteFileRequest.class);
    }

    @Test(groups={"regressiontest"})
    public void deleteOnChecksumPillarWithDefaultReturnChecksumType() throws Exception {
        addDescription("Verify that the DeleteClient works correctly when a return checksum of the default type" +
                "is requested. ");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        addStep("Request a file to be deleted on the pillar1. The call should include a request for a check sum of the " +
                "default type",
                "A IdentifyPillarsForDeleteFileRequest should be sent and a IDENTIFY_REQUEST_SENT event should be " +
                        "generated.");
        ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
        deleteClient.deleteFile( DEFAULT_FILE_ID, PILLAR1_ID, TestFileHelper.getDefaultFileChecksum(),
                checksumSpecTYPE, testEventHandler, null);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send a response from pillar1 with PillarChecksumSpec element set, indicating that this is a " +
                "checksum pillar.",
                "Following events should be generated: COMPONENT_IDENTIFIED, IDENTIFICATION_COMPLETE and a " +
                        "REQUEST_SENT. A request should be sent to pillar1 with a request for return of a checksum " +
                        "of the default type ");
        IdentifyPillarsForDeleteFileResponse identifyResponse
                = messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
        ChecksumSpecTYPE checksumSpecTYPEFromPillar = new ChecksumSpecTYPE();
        checksumSpecTYPEFromPillar.setChecksumType(ChecksumType.MD5);
        identifyResponse.setPillarChecksumSpec(checksumSpecTYPEFromPillar);
        messageBus.sendMessage(identifyResponse);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        DeleteFileRequest receivedPutFileRequest1 = pillar1Destination.waitForMessage(DeleteFileRequest.class);
        Assert.assertEquals(receivedPutFileRequest1.getChecksumRequestForExistingFile(), checksumSpecTYPE);
    }

    @Test(groups={"regressiontest"})
    public void deleteOnChecksumPillarWithSaltedReturnChecksumType() throws Exception {
        addDescription("Verify that the DeleteClient works correctly when a return checksum with a salt " +
                "is requested. ");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();

        addStep("Request a file to be deleted on the pillar1. The call should include a request for a salted check sum ",
                "A IdentifyPillarsForDeleteFileRequest should be sent and a IDENTIFY_REQUEST_SENT event should be " +
                        "generated.");
        ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
        checksumSpecTYPE.setChecksumSalt(Base16Utils.encodeBase16("aa"));
        deleteClient.deleteFile( DEFAULT_FILE_ID, PILLAR1_ID, TestFileHelper.getDefaultFileChecksum(),
                checksumSpecTYPE, testEventHandler, null);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send a response from pillar1 with PillarChecksumSpec element set, indicating that this is a " +
                "checksum pillar.",
                "Following events should be generated: COMPONENT_IDENTIFIED, IDENTIFICATION_COMPLETE and a " +
                        "REQUEST_SENT. A request should be sent to pillar1 without a request for a return checksum.");
        IdentifyPillarsForDeleteFileResponse identifyResponse
                = messageFactory.createIdentifyPillarsForDeleteFileResponse(receivedIdentifyRequestMessage,
                PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
        ChecksumSpecTYPE checksumSpecTYPEFromPillar = new ChecksumSpecTYPE();
        checksumSpecTYPEFromPillar.setChecksumType(ChecksumType.MD5);
        identifyResponse.setPillarChecksumSpec(checksumSpecTYPEFromPillar);
        messageBus.sendMessage(identifyResponse);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        DeleteFileRequest receivedPutFileRequest1 = pillar1Destination.waitForMessage(DeleteFileRequest.class);
        Assert.assertNull(receivedPutFileRequest1.getChecksumRequestForExistingFile());
    }

    /**
     * Creates a new test DeleteFileClient based on the supplied settingsForCUT.
     *
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new DeleteFileClient(Wrapper).
     */
    private DeleteFileClient createDeleteFileClient() {
        return new DeleteClientTestWrapper(new ConversationBasedDeleteFileClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()), testEventManager);
    }
}
