/*
 * #%L
 * Bitrepository Access Client
 *
 * $Id: PutFileClientComponentTest.java 626 2011-12-09 13:23:52Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/test/java/org/bitrepository/modify/putfile/PutFileClientComponentTest.java $
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
package org.bitrepository.modify.replacefile;

import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.*;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.client.DefaultFixtureClientIT;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

@ExtendWith(SuiteInfoParameterResolver.class)
public class ReplaceFileClientComponentIT extends DefaultFixtureClientIT {
    private ChecksumSpecTYPE DEFAULT_CHECKSUM_SPEC;
    private ChecksumDataForFileTYPE DEFAULT_OLD_CHECKSUM_DATA;
    private ChecksumDataForFileTYPE DEFAULT_NEW_CHECKSUM_DATA;
    private TestReplaceFileMessageFactory messageFactory;
    private DatatypeFactory datatypeFactory;

    @BeforeEach
    public void initialise() throws DatatypeConfigurationException {
        messageFactory = new TestReplaceFileMessageFactory(settingsForTestClient.getComponentID());
        DEFAULT_CHECKSUM_SPEC = ChecksumUtils.getDefault(settingsForCUT);
        DEFAULT_OLD_CHECKSUM_DATA = createChecksumData("123checksum321");
        DEFAULT_NEW_CHECKSUM_DATA = createChecksumData("123checksum321");
        datatypeFactory = DatatypeFactory.newInstance();
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void verifyReplaceFileClientFromFactory() {
        addDescription("Testing the initialization through the ModifyComponentFactory.");
        addStep("Use the ModifyComponentFactory to instantiate a ReplaceFileClient.",
                "It should be an instance of ConversationBasedReplaceFileClient");
        ReplaceFileClient rfc = ModifyComponentFactory.getInstance().retrieveReplaceFileClient(
                settingsForCUT, securityManager, settingsForTestClient.getComponentID());
        Assertions.assertInstanceOf(ConversationBasedReplaceFileClient.class, rfc, "The ReplaceFileClient '" + rfc
                + "' should be instance of '" + ConversationBasedReplaceFileClient.class.getName() + "'");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void replaceClientTester() throws Exception {
        addDescription("Tests the ReplaceFileClient. Makes a whole conversation for the replace client for a "
                + "'good' scenario.");
        addStep("Initialise the number of pillars to one", "Should be OK.");

        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        TestEventHandler testEventHandler = new TestEventHandler();
        ReplaceFileClient replaceClient = createReplaceFileClient();
        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        URL address = httpServerConfiguration.getURL(DEFAULT_FILE_ID);

        addStep("Request a file to be replaced on all pillars (which means only the default pillar).",
                "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
        replaceClient.replaceFile(collectionID, DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA,
                checksumRequest,
                address, 10, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileRequest.class);
        Assertions.assertEquals(collectionID, receivedIdentifyRequestMessage.getCollectionID());
        Assertions.assertNotNull(receivedIdentifyRequestMessage.getCorrelationID());
        Assertions.assertEquals(settingsForCUT.getReceiverDestinationID(), receivedIdentifyRequestMessage.getReplyTo());
        Assertions.assertEquals(DEFAULT_FILE_ID, receivedIdentifyRequestMessage.getFileID());
        Assertions.assertEquals(settingsForTestClient.getComponentID(), receivedIdentifyRequestMessage.getFrom());
        Assertions.assertEquals(settingsForTestClient.getCollectionDestination(), receivedIdentifyRequestMessage.getDestination());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the " +
                "request.");

        ReplaceFileRequest receivedReplaceFileRequest;
        IdentifyPillarsForReplaceFileResponse identifyResponse =
                messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        receivedReplaceFileRequest = pillar1Receiver.waitForMessage(ReplaceFileRequest.class);
        Assertions.assertEquals(collectionID, receivedReplaceFileRequest.getCollectionID());
        Assertions.assertEquals(receivedIdentifyRequestMessage.getCorrelationID(), receivedReplaceFileRequest.getCorrelationID());
        Assertions.assertEquals(settingsForCUT.getReceiverDestinationID(), receivedReplaceFileRequest.getReplyTo());
        Assertions.assertEquals(DEFAULT_FILE_ID, receivedReplaceFileRequest.getFileID());
        Assertions.assertEquals(settingsForTestClient.getComponentID(), receivedReplaceFileRequest.getFrom());
        Assertions.assertEquals(pillar1DestinationId, receivedReplaceFileRequest.getDestination());

        addStep("Validate the steps of the ReplaceClient by going through the events.",
                "Should be 'PillarIdentified', 'PillarSelected' and 'RequestSent'");
        for (int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());
        }
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("The pillar sends a progress response to the ReplaceClient.",
                "Should be caught by the event handler.");
        ReplaceFileProgressResponse putFileProgressResponse = messageFactory.createReplaceFileProgressResponse(
                receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(putFileProgressResponse);
        Assertions.assertEquals(OperationEvent.OperationEventType.PROGRESS, testEventHandler.waitForEvent().getEventType());

        addStep("Send a final response message to the ReplaceClient.",
                "Should be caught by the event handler. First a PillarComplete, then a Complete.");
        ReplaceFileFinalResponse replaceFileFinalResponse = messageFactory.createReplaceFileFinalResponse(
                receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId, DEFAULT_NEW_CHECKSUM_DATA);
        messageBus.sendMessage(replaceFileFinalResponse);
        for (int i = 1; i < 2 * settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            OperationEventType eventType = testEventHandler.waitForEvent().getEventType();
            Assertions.assertTrue((eventType == OperationEvent.OperationEventType.COMPONENT_COMPLETE)
                            || (eventType == OperationEvent.OperationEventType.PROGRESS),
                    "Expected either PartiallyComplete or Progress, but was: " + eventType);
        }
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPLETE, testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void replaceClientIdentificationTimeout() throws Exception {
        addDescription("Tests the handling of a failed identification for the ReplaceClient");
        addStep("Initialise the number of pillars and the DeleteClient. Sets the identification timeout to 100 ms.",
                "Should be OK.");
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        settingsForCUT.getRepositorySettings().getClientSettings()
                .setIdentificationTimeoutDuration(datatypeFactory.newDuration(100));
        TestEventHandler testEventHandler = new TestEventHandler();
        ReplaceFileClient replaceClient = createReplaceFileClient();
        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        URL address = httpServerConfiguration.getURL(DEFAULT_FILE_ID);

        addStep("Request a file to be replaced on the default pillar.",
                "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
        replaceClient.replaceFile(collectionID, DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA,
                checksumRequest,
                address, 10, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        collectionReceiver.waitForMessage(IdentifyPillarsForReplaceFileRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());

        addStep("Do not respond. Just await the timeout.",
                "Should make send a Failure event to the eventhandler.");
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_TIMEOUT,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_FAILED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.FAILED, testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void replaceClientOperationTimeout() throws Exception {
        addDescription("Tests the handling of a failed operation for the ReplaceClient");
        addStep("Initialise the number of pillars and the DeleteClient. Sets the operation timeout to 100 ms.",
                "Should be OK.");
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        settingsForCUT.getRepositorySettings().getClientSettings()
                .setOperationTimeoutDuration(datatypeFactory.newDuration(100));
        TestEventHandler testEventHandler = new TestEventHandler();
        ReplaceFileClient replaceClient = createReplaceFileClient();

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        URL address = httpServerConfiguration.getURL(DEFAULT_FILE_ID);

        addStep("Request a file to be replaced on the default pillar.",
                "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
        replaceClient.replaceFile(collectionID, DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA,
                checksumRequest,
                address, 10, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());

        addStep("Make response for the pillar.",
                "The client receive the response, identify the pillar and send the request.");

        IdentifyPillarsForReplaceFileResponse identifyResponse =
                messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        Assertions.assertNotNull(pillar1Receiver.waitForMessage(ReplaceFileRequest.class));

        addStep("Validate the steps of the ReplaceClient by going through the events.",
                "Should be 'PillarIdentified', 'PillarSelected' and 'RequestSent'");
        for (int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());
        }
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("Do not respond. Just await the timeout.",
                "Should make send a Failure event to the eventhandler.");
        Assertions.assertEquals(OperationEvent.OperationEventType.FAILED, testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void replaceClientPillarFailed() throws Exception {
        addDescription("Tests the handling of a operation failure for the ReplaceClient. ");
        addStep("Initialise the number of pillars to one", "Should be OK.");

        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR1_ID);
        TestEventHandler testEventHandler = new TestEventHandler();
        ReplaceFileClient replaceClient = createReplaceFileClient();

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        URL address = httpServerConfiguration.getURL(DEFAULT_FILE_ID);

        addStep("Request a file to be replaced on the default pillar.",
                "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
        replaceClient.replaceFile(collectionID, DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA,
                checksumRequest,
                address, 0, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("Make response for the pillar.",
                "The client receive the response, identify the pillar and send the request.");

        ReplaceFileRequest receivedReplaceFileRequest;
        IdentifyPillarsForReplaceFileResponse identifyResponse =
                messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        receivedReplaceFileRequest = pillar1Receiver.waitForMessage(ReplaceFileRequest.class);

        addStep("Validate the steps of the ReplaceClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for (int i = 0; i < settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size(); i++) {
            Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());
        }
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("Send a failed response message to the ReplaceClient.",
                "Should be caught by the event handler. First a PillarFailed, then a Complete.");
        ReplaceFileFinalResponse replaceFileFinalResponse = messageFactory.createReplaceFileFinalResponse(
                receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId, DEFAULT_NEW_CHECKSUM_DATA);
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.FAILURE);
        ri.setResponseText("Verifying that a failure can be understood!");
        replaceFileFinalResponse.setResponseInfo(ri);
        messageBus.sendMessage(replaceFileFinalResponse);
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_FAILED, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.FAILED, testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void saltedReturnChecksumsForNewFileWithChecksumPillar() throws Exception {
        addDescription("Tests that the ReplaceClient handles the presence of a ChecksumPillar correctly, " +
                "when a salted return checksum (which a checksum pillar can't provide) is requested for the new file.");

        TestEventHandler testEventHandler = new TestEventHandler();
        ReplaceFileClient replaceClient = createReplaceFileClient();

        addStep("Call replaceFile while requesting a salted checksum to be returned.",
                "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                        "IDENTIFY_REQUEST_SENT should be generated.");
        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.MD5);
        checksumRequest.setChecksumSalt(Base16Utils.encodeBase16("aa"));
        replaceClient.replaceFile(collectionID, DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA, null,
                httpServerConfiguration.getURL(DEFAULT_FILE_ID), 0, DEFAULT_NEW_CHECKSUM_DATA,
                checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("Send an identification response with a PillarChecksumSpec element set, indicating that this is a " +
                        "checksum pillar.",
                "An COMPONENT_IDENTIFIED event should be generate followed by a COMPONENT_IDENTIFIED, " +
                        "a IDENTIFICATION_COMPLETE and a REQUEST_SENT event. A replace request should be set to the " +
                        "checksum pillar without a request for a salted return checksum for the new file");
        IdentifyPillarsForReplaceFileResponse identifyResponse =
                messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage,
                        PILLAR1_ID, pillar1DestinationId);
        markAsChecksumPillarResponse(identifyResponse);
        messageBus.sendMessage(identifyResponse);
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());
        ReplaceFileRequest receivedReplaceFileRequest1 = pillar1Receiver.waitForMessage(ReplaceFileRequest.class);
        Assertions.assertNull(receivedReplaceFileRequest1.getChecksumRequestForNewFile());
    }

    /**
     * Creates a new test PutFileClient based on the supplied settings.
     * <p/>
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     *
     * @return A new PutFileClient(Wrapper).
     */
    private ReplaceFileClient createReplaceFileClient() {
        return new ReplaceFileClientTestWrapper(new ConversationBasedReplaceFileClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()));
    }

    private ChecksumDataForFileTYPE createChecksumData(String checksum) {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumSpec(DEFAULT_CHECKSUM_SPEC);
        checksumData.setChecksumValue(checksum.getBytes(StandardCharsets.UTF_8));
        checksumData.setCalculationTimestamp(CalendarUtils.getEpoch());
        return checksumData;
    }

    private void markAsChecksumPillarResponse(IdentifyPillarsForReplaceFileResponse identifyResponse) {
        identifyResponse.setPillarChecksumSpec(ChecksumUtils.getDefault(settingsForCUT));
    }
}
