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

import java.math.BigInteger;
import java.net.URL;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.client.DefaultFixtureClientTest;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReplaceFileClientComponentTest extends DefaultFixtureClientTest {
    private ChecksumSpecTYPE DEFAULT_CHECKSUM_SPEC;
    private ChecksumDataForFileTYPE DEFAULT_OLD_CHECKSUM_DATA;
    private ChecksumDataForFileTYPE DEFAULT_NEW_CHECKSUM_DATA;
    private TestReplaceFileMessageFactory messageFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialise() throws Exception {
        messageFactory = new TestReplaceFileMessageFactory(settingsForCUT.getCollectionID());
        DEFAULT_CHECKSUM_SPEC = ChecksumUtils.getDefault(settingsForCUT);
        DEFAULT_OLD_CHECKSUM_DATA = createChecksumData("123checksum321");
        DEFAULT_NEW_CHECKSUM_DATA = createChecksumData("123checksum321");
    }

    @Test(groups={"regressiontest"})
    public void verifyReplaceFileClientFromFactory() {
        addDescription("Testing the initialization through the ModifyComponentFactory.");
        addStep("Use the ModifyComponentFactory to instantiate a ReplaceFileClient.",
                "It should be an instance of ConversationBasedReplaceFileClient");
        ReplaceFileClient rfc = ModifyComponentFactory.getInstance().retrieveReplaceFileClient(
                settingsForCUT, securityManager, settingsForTestClient.getComponentID());
        Assert.assertTrue(rfc instanceof ConversationBasedReplaceFileClient, "The ReplaceFileClient '" + rfc
                + "' should be instance of '" + ConversationBasedReplaceFileClient.class.getName() + "'");
    }

    @Test(groups={"regressiontest"})
    public void replaceClientTester() throws Exception {
        addDescription("Tests the ReplaceFileClient. Makes a whole conversation for the replace client for a "
                + "'good' scenario.");
        addStep("Initialise the number of pillars to one", "Should be OK.");

        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        ReplaceFileClient replaceClient = createReplaceFileClient();
        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        URL address = httpServer.getURL(DEFAULT_FILE_ID);

        addStep("Request a file to be replaced on all pillars (which means only the default pillar).",
                "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
        replaceClient.replaceFileAtAllPillars(DEFAULT_FILE_ID, DEFAULT_OLD_CHECKSUM_DATA, checksumRequest,
                address, 10, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = null;
        if(useMockupPillar()) {
            receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                    IdentifyPillarsForReplaceFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage,
                    messageFactory.createIdentifyPillarsForReplaceFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo(),
                            DEFAULT_FILE_ID, null, settingsForTestClient.getComponentID()));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");

        ReplaceFileRequest receivedReplaceFileRequest = null;
        if(useMockupPillar()) {
            IdentifyPillarsForReplaceFileResponse identifyResponse = messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage,
                    PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedReplaceFileRequest = pillar1Receiver.waitForMessage(ReplaceFileRequest.class);
            Assert.assertEquals(receivedReplaceFileRequest,
                    messageFactory.createReplaceFileRequest(PILLAR1_ID, pillar1DestinationId,
                            receivedReplaceFileRequest.getReplyTo(),
                            receivedReplaceFileRequest.getCorrelationID(),
                            address.toExternalForm(), BigInteger.valueOf(10), DEFAULT_FILE_ID, null,
                            settingsForTestClient.getComponentID(),
                            DEFAULT_OLD_CHECKSUM_DATA, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest));
        }

        addStep("Validate the steps of the ReplaceClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("The pillar sends a progress response to the ReplaceClient.", "Should be caught by the event handler.");
        if(useMockupPillar()) {
            ReplaceFileProgressResponse putFileProgressResponse = messageFactory.createReplaceFileProgressResponse(
                    receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(putFileProgressResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);

        addStep("Send a final response message to the ReplaceClient.",
                "Should be caught by the event handler. First a PillarComplete, then a Complete.");
        if(useMockupPillar()) {
            ReplaceFileFinalResponse replaceFileFinalResponse = messageFactory.createReplaceFileFinalResponse(
                    receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId, DEFAULT_NEW_CHECKSUM_DATA);
            messageBus.sendMessage(replaceFileFinalResponse);
        }
        for(int i = 1; i < 2* settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            OperationEventType eventType = testEventHandler.waitForEvent().getEventType();
            Assert.assertTrue( (eventType == OperationEventType.COMPONENT_COMPLETE)
                    || (eventType == OperationEventType.PROGRESS),
                    "Expected either PartiallyComplete or Progress, but was: " + eventType);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    @Test(groups={"regressiontest"})
    public void replaceClientIdentificationTimeout() throws Exception {
        addDescription("Tests the handling of a failed identification for the ReplaceClient");
        addStep("Initialise the number of pillars and the DeleteClient. Sets the identification timeout to 1 sec.",
                "Should be OK.");
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        settingsForCUT.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(1000L));
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        ReplaceFileClient replaceClient = createReplaceFileClient();
        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        URL address = httpServer.getURL(DEFAULT_FILE_ID);

        addStep("Request a file to be replaced on the default pillar.",
                "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
        replaceClient.replaceFile(DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA, checksumRequest,
                address, 10, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileRequest.class);
        Assert.assertEquals(receivedIdentifyRequestMessage,
                messageFactory.createIdentifyPillarsForReplaceFileRequest(
                        receivedIdentifyRequestMessage.getCorrelationID(),
                        receivedIdentifyRequestMessage.getReplyTo(),
                        receivedIdentifyRequestMessage.getTo(),
                        DEFAULT_FILE_ID, null, settingsForTestClient.getComponentID()));
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Do not respond. Just await the timeout.",
                "Should make send a Failure event to the eventhandler.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups={"regressiontest"})
    public void replaceClientOperationTimeout() throws Exception {
        addDescription("Tests the handling of a failed operation for the ReplaceClient");
        addStep("Initialise the number of pillars and the DeleteClient. Sets the operation timeout to 1 sec.",
                "Should be OK.");
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        settingsForCUT.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(1000L));
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        ReplaceFileClient replaceClient = createReplaceFileClient();

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        URL address = httpServer.getURL(DEFAULT_FILE_ID);

        addStep("Request a file to be replaced on the default pillar.",
                "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
        replaceClient.replaceFile(DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA, checksumRequest,
                address, 10, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileRequest.class);
        Assert.assertEquals(receivedIdentifyRequestMessage,
                messageFactory.createIdentifyPillarsForReplaceFileRequest(
                        receivedIdentifyRequestMessage.getCorrelationID(),
                        receivedIdentifyRequestMessage.getReplyTo(),
                        receivedIdentifyRequestMessage.getTo(),
                        DEFAULT_FILE_ID, null, settingsForTestClient.getComponentID()));
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");

        IdentifyPillarsForReplaceFileResponse identifyResponse = messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage,
                PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(identifyResponse);
        Assert.assertNotNull(pillar1Receiver.waitForMessage(ReplaceFileRequest.class));

        addStep("Validate the steps of the ReplaceClient by going through the events.", "Should be 'PillarIdentified', "
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
    public void replaceClientPillarFailed() throws Exception {
        addDescription("Tests the handling of a operation failure for the ReplaceClient. ");
        addStep("Initialise the number of pillars to one", "Should be OK.");

        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        ReplaceFileClient replaceClient = createReplaceFileClient();

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.SHA1);

        URL address = httpServer.getURL(DEFAULT_FILE_ID);

        addStep("Request a file to be replaced on the default pillar.",
                "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
        replaceClient.replaceFile(DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA, checksumRequest,
                address, 0, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileRequest.class);
        Assert.assertEquals(receivedIdentifyRequestMessage,
                messageFactory.createIdentifyPillarsForReplaceFileRequest(
                        receivedIdentifyRequestMessage.getCorrelationID(),
                        receivedIdentifyRequestMessage.getReplyTo(),
                        receivedIdentifyRequestMessage.getTo(),
                        DEFAULT_FILE_ID, null, settingsForTestClient.getComponentID()));
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");

        ReplaceFileRequest receivedReplaceFileRequest = null;
        if(useMockupPillar()) {
            IdentifyPillarsForReplaceFileResponse identifyResponse = messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage,
                    PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedReplaceFileRequest = pillar1Receiver.waitForMessage(ReplaceFileRequest.class);
            Assert.assertEquals(receivedReplaceFileRequest,
                    messageFactory.createReplaceFileRequest(PILLAR1_ID, pillar1DestinationId,
                            receivedReplaceFileRequest.getReplyTo(),
                            receivedReplaceFileRequest.getCorrelationID(),
                            address.toExternalForm(), BigInteger.valueOf(0), DEFAULT_FILE_ID, null,
                            settingsForTestClient.getComponentID(),
                            DEFAULT_OLD_CHECKSUM_DATA, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest));
        }

        addStep("Validate the steps of the ReplaceClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);

        addStep("Send a failed response message to the ReplaceClient.",
                "Should be caught by the event handler. First a PillarFailed, then a Complete.");
        if(useMockupPillar()) {
            ReplaceFileFinalResponse replaceFileFinalResponse = messageFactory.createReplaceFileFinalResponse(
                    receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId, DEFAULT_NEW_CHECKSUM_DATA);
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.FAILURE);
            ri.setResponseText("Verifying that a failure can be understood!");
            replaceFileFinalResponse.setResponseInfo(ri);
            messageBus.sendMessage(replaceFileFinalResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
    }

    @Test(groups={"regressiontest"})
    public void saltedReturnChecksumsForNewFileWithChecksumPillar() throws Exception {
        addDescription("Tests that the ReplaceClient handles the presence of a ChecksumPillar correctly, " +
                "when a salted return checksum (which a checksum pillar can't provide) is requested for the new file.");

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        ReplaceFileClient replaceClient = createReplaceFileClient();

        addStep("Call replaceFile while requesting a salted checksum to be returned.",
                "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                        "IDENTIFY_REQUEST_SENT should be generated.");
        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType(ChecksumType.MD5);
        checksumRequest.setChecksumSalt(Base16Utils.encodeBase16("aa"));
        replaceClient.replaceFile(DEFAULT_FILE_ID, PILLAR1_ID, DEFAULT_OLD_CHECKSUM_DATA, null,
                httpServer.getURL(DEFAULT_FILE_ID), 0, DEFAULT_NEW_CHECKSUM_DATA, checksumRequest, testEventHandler, null);

        IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send an identification response with a PillarChecksumSpec element set, indicating that this is a " +
                "checksum pillar.",
                "An COMPONENT_IDENTIFIED event should be generate followed by a COMPONENT_IDENTIFIED, " +
                        "a IDENTIFICATION_COMPLETE and a REQUEST_SENT event. A replace request should be set to the " +
                        "checksum pillar without a request for a salted return checksum for the new file");
        IdentifyPillarsForReplaceFileResponse identifyResponse = messageFactory.createIdentifyPillarsForReplaceFileResponse(
                receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
        markAsChecksumPillarResponse(identifyResponse);
        messageBus.sendMessage(identifyResponse);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        ReplaceFileRequest receivedReplaceFileRequest1 = pillar1Receiver.waitForMessage(ReplaceFileRequest.class);
        Assert.assertNull(receivedReplaceFileRequest1.getChecksumRequestForNewFile());
    }

    /**
     * Creates a new test PutFileClient based on the supplied settingsForCUT.
     *
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new PutFileClient(Wrapper).
     */
    private ReplaceFileClient createReplaceFileClient() {
        return new ReplaceClientTestWrapper(new ConversationBasedReplaceFileClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()), testEventManager);
    }

    private ChecksumDataForFileTYPE createChecksumData(String checksum) {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumSpec(DEFAULT_CHECKSUM_SPEC);
        checksumData.setChecksumValue(checksum.getBytes());
        checksumData.setCalculationTimestamp(CalendarUtils.getEpoch());
        return checksumData;
    }

    private void markAsChecksumPillarResponse(IdentifyPillarsForReplaceFileResponse identifyResponse) {
        identifyResponse.setPillarChecksumSpec(ChecksumUtils.getDefault(settingsForCUT));
    }
}
