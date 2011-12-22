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

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the components of the PutFileClient.
 * TODO need more test-cases, e.g. the User-Stories...
 */
public class DeleteFileClientComponentTest extends DefaultFixtureClientTest {
    private TestDeleteFileMessageFactory messageFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialise() throws Exception {
        if(useMockupPillar()) {
            messageFactory = new TestDeleteFileMessageFactory(settings.getCollectionID());
        }
    }

//    @Test(groups={"regressiontest"})
    public void verifyDeleteClientFromFactory() {
        addDescription("Testing the initialization through the ModifyComponentFactory.");
        addStep("Use the ModifyComponentFactory to instantiate a PutFileClient.", 
                "It should be an instance of SimplePutFileClient");
        DeleteFileClient dfc = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(settings);
        Assert.assertTrue(dfc instanceof ConversationBasedDeleteFileClient, "The DeleteFileClient '" + dfc 
                + "' should be instance of '" + ConversationBasedDeleteFileClient.class.getName() + "'");
    }

    @Test(groups={"regressiontest"})
    public void deleteClientTester() throws Exception {
        addDescription("Tests the DeleteClient. Makes a whole conversation for the delete client for a 'good' scenario.");
        addStep("Initialise the number of pillars to one", "Should be OK.");

        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        DeleteFileClient deleteClient = createDeleteFileClient();
        
        String checksum = "123checksum321";
        ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
        checksumForPillar.setChecksumType("MD5");
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumSpec(checksumForPillar);
        checksumData.setChecksumValue(checksum.getBytes());
        checksumData.setCalculationTimestamp(CalendarUtils.getEpoch());

        ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
        checksumRequest.setChecksumType("SHA-1");

        addStep("Request a file to be deleted on the default pillar.", 
                "A IdentifyPillarsForDeleteFileRequest should be sent to the pillar.");
        deleteClient.deleteFile(DEFAULT_FILE_ID, PILLAR1_ID, checksumData, checksumRequest, testEventHandler, null);

        IdentifyPillarsForDeleteFileRequest receivedIdentifyRequestMessage = null;
        if(useMockupPillar()) {
            receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                    IdentifyPillarsForDeleteFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    messageFactory.createIdentifyPillarsForDeleteFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo(), 
                            DEFAULT_FILE_ID
                            ));
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);

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
                            receivedDeleteFileRequest.getChecksumDataForFile(),
                            receivedDeleteFileRequest.getFileChecksumSpec()
                            ));
        }

        addStep("Validate the steps of the DeleteClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);

        addStep("The pillar sends a progress response to the DeleteClient.", "Should be caught by the event handler.");
        if(useMockupPillar()) {
            DeleteFileProgressResponse putFileProgressResponse = messageFactory.createDeleteFileProgressResponse(
                    receivedDeleteFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(putFileProgressResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);

        addStep("Send a final response message to the DeleteClient.", 
                "Should be caught by the event handler. First a PartiallyComplete, then a Complete.");
        if(useMockupPillar()) {
            DeleteFileFinalResponse putFileFinalResponse = messageFactory.createDeleteFileFinalResponse(
                    receivedDeleteFileRequest, PILLAR1_ID, pillar1DestinationId, DEFAULT_FILE_ID);
            messageBus.sendMessage(putFileFinalResponse);
        }
        for(int i = 1; i < 2* settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
            OperationEventType eventType = testEventHandler.waitForEvent().getType();
            Assert.assertTrue( (eventType == OperationEventType.PillarComplete)
                    || (eventType == OperationEventType.Progress),
                    "Expected either PartiallyComplete or Progress, but was: " + eventType);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
    }
//
//    // Move to system test, not relevant as component test
//    //@Test(groups={"regressiontest"})
//    public void putClientBadURLTester() throws Exception {
//        addDescription("Tests the PutClient. Makes a whole conversation for the put client, for a 'bad' scenario. "
//                + "The URL is not valid, which should give errors in the FinalResponse.");
//        addStep("Initialise the number of pillars and the PutClient.", "Should be OK.");
//
//        URL invalidUrl = new URL("http://sandkasse-01.kb.dk/ERROR/ERROR/ERROR/ERROR/ERROR/ERROR/ERROR/test.xml");
//
//        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
//        PutFileClient putClient = createPutFileClient();
//
//        addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
//                "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
//        putClient.putFileWithId(invalidUrl, DEFAULT_FILE_ID, new Long(testFile.length()), testEventHandler);
//
//        IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
//        if(useMockupPillar()) {
//            receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
//                    IdentifyPillarsForPutFileRequest.class);
//            Assert.assertEquals(receivedIdentifyRequestMessage, 
//                    messageFactory.createIdentifyPillarsForPutFileRequest(
//                            receivedIdentifyRequestMessage.getCorrelationID(),
//                            receivedIdentifyRequestMessage.getReplyTo(),
//                            receivedIdentifyRequestMessage.getTo()
//                            ));
//        }
//
//        addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
//
//        PutFileRequest receivedPutFileRequest = null;
//        if(useMockupPillar()) {
//            IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
//                    .createIdentifyPillarsForPutFileResponse(
//                            receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
//            messageBus.sendMessage(identifyResponse);
//            receivedPutFileRequest = pillar1Destination.waitForMessage(PutFileRequest.class);
//            Assert.assertEquals(receivedPutFileRequest, 
//                    messageFactory.createPutFileRequest(
//                            PILLAR1_ID, pillar1DestinationId,
//                            receivedPutFileRequest.getReplyTo(),
//                            receivedPutFileRequest.getCorrelationID(),
//                            receivedPutFileRequest.getFileAddress(),
//                            receivedPutFileRequest.getFileSize(),
//                            DEFAULT_FILE_ID
//                            ));
//        }
//
//        addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
//                + "'PillarSelected' and 'RequestSent'");
//        for(String s : settings.getCollectionSettings().getClientSettings().getPillarIDs()) {
//            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
//        }
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
//
//        addStep("The pillar sends a progress response to the PutClient.", "Should be caught by the event handler.");
//        if(useMockupPillar()) {
//            PutFileProgressResponse putFileProgressResponse = messageFactory.createPutFileProgressResponse(
//                    receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
//            messageBus.sendMessage(putFileProgressResponse);
//        }
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
//
//        addStep("Send a 'bad' final response message to the PutClient.", 
//                "Should be caught by the event handler. First a Failed, then a Complete.");
//
//        if(useMockupPillar()) {
//            PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
//                    receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
//            ResponseInfo frInfo = new ResponseInfo();
//            frInfo.setResponseCode(ResponseCode.FAILURE);
//            // TODO has to contain 'Error' to be caught.
//            frInfo.setResponseText("Error: could not use URL!"); 
//            putFileFinalResponse.setResponseInfo(frInfo);
//            messageBus.sendMessage(putFileFinalResponse);
//        }
//
//        for(int i = 1; i < 3* settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
//            OperationEventType eventType = testEventHandler.waitForEvent().getType();
//            Assert.assertTrue( (eventType == OperationEventType.Failed)
//                    || (eventType == OperationEventType.Progress) 
//                    || (eventType == OperationEventType.PillarComplete),
//                    "Expected either Failed, Progress or PartiallyComplete, but was: " + eventType);
//        }
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
//    }
//
    /**
     * Creates a new test PutFileClient based on the supplied settings. 
     * 
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new PutFileClient(Wrapper).
     */
    private DeleteFileClient createDeleteFileClient() {
        MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration());
        ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings);
        return new DeleteClientTestWrapper(new ConversationBasedDeleteFileClient(
                messageBus, conversationMediator, settings)
        , testEventManager);
    }
}
