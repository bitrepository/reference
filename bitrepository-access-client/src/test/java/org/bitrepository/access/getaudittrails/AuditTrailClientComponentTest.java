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
package org.bitrepository.access.getaudittrails;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.access.getaudittrails.client.ConversationBasedAuditTrailClient;
import org.bitrepository.bitrepositoryelements.*;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.client.DefaultFixtureClientTest;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.settings.collectionsettings.GetAuditTrailSettings;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Test the default AuditTrailClient.
 */
public class AuditTrailClientComponentTest extends DefaultFixtureClientTest {
    private GetAuditTrailsMessageFactory testMessageFactory;
    private FileIDs ALL_FILE_IDS;

    @BeforeMethod(alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        testMessageFactory = new GetAuditTrailsMessageFactory(settings.getCollectionID(), TEST_CLIENT_ID);

        if (settings.getCollectionSettings().getGetAuditTrailSettings() == null) {
            settings.getCollectionSettings().setGetAuditTrailSettings(new GetAuditTrailSettings());
        }
        List<String> contributers = settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs();
        contributers.clear();
        contributers.add(PILLAR1_ID);
        contributers.add(PILLAR2_ID);

        ALL_FILE_IDS = new FileIDs();
        ALL_FILE_IDS.setAllFileIDs("TRUE");
    }
    /**
     * Test class for the 'AuditTrailClient'.
     */
    @Test(groups = {"regressiontest"})
    public void verifyAuditTrailClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createAuditTrailClient(
                settings, securityManager, TEST_CLIENT_ID)
                instanceof ConversationBasedAuditTrailClient,
                "The default AuditTrailClient from the Access factory should be of the type '" +
                        ConversationBasedAuditTrailClient.class.getName() + "'.");
    }

    @Test(groups = {"regressiontest"})
    public void getAllAuditTrails() throws InterruptedException {
        addDescription("Tests the simplest case of getting all audit trail event for all contributers.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailClient client = createAuditTrailClient();

        addStep("Retrieve all audit trails from the collection by calling with a null componentQueries array",
                "This should be interpreted as a request for all audit trails from all the collection settings " +
                        "defined contributers.");
        client.getAuditTrails(null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identifyResponse from each pillar",
                "Two COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                "Rights after this a REQUEST_SENT should be received and a GetAuditTrailsRequest should " +
                "be sent to each pillar");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);

        IdentifyContributorsForGetAuditTrailsResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.REQUEST_SENT);
        GetAuditTrailsRequest requestPillar1 = pillar1Destination.waitForMessage(GetAuditTrailsRequest.class);
        Assert.assertEquals(requestPillar1, testMessageFactory.createGetAuditTrailsRequest(
                identifyRequest, PILLAR1_ID, pillar1DestinationId));
        GetAuditTrailsRequest requestPillar2 = pillar2Destination.waitForMessage(GetAuditTrailsRequest.class);
        Assert.assertEquals(requestPillar2, testMessageFactory.createGetAuditTrailsRequest(
                identifyRequest, PILLAR2_ID, pillar2DestinationId));

        addStep("Send a final response from pillar 1",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results.");
        ResultingAuditTrails result1 = createTestResultingAuditTrails(PILLAR1_ID);
        GetAuditTrailsFinalResponse resultPillar1 =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR1_ID, pillar1DestinationId, result1);
        messageBus.sendMessage(resultPillar1);
        AuditTrailResult result1Event = (AuditTrailResult)testEventHandler.waitForEvent();
        Assert.assertEquals(result1Event.getType(), OperationEvent.OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(result1Event.getAuditTrailEvents(), result1);

        addStep("Send a final response from pillar 2",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results." +
                        "This should be followed by a COMPLETE event");
        ResultingAuditTrails result2 = createTestResultingAuditTrails(PILLAR2_ID);
        GetAuditTrailsFinalResponse resultPillar2 =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR2_ID, pillar2DestinationId, result2);
        messageBus.sendMessage(resultPillar2);

        AuditTrailResult result2Event = (AuditTrailResult)testEventHandler.waitForEvent();
        Assert.assertEquals(result2Event.getType(), OperationEvent.OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(result2Event.getAuditTrailEvents(), result2);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPLETE);
    }


    @Test(groups = {"regressiontest"})
    public void getSomeAuditTrails() throws InterruptedException {
        addDescription("Tests the client mappes a AuditTrail query correctly to a GetAuditTrail request.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailClient client = createAuditTrailClient();

        addStep("Request audit trails from pillar 1 with both min and max sequence number set.",
                "A identify request is sent.");
        AuditTrailQuery query1 = new AuditTrailQuery(PILLAR1_ID, 1, 3);
        client.getAuditTrails(new AuditTrailQuery[] { query1 }, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identifyResponse from pillar1",
                "A COMPONENT_IDENTIFIED event and a IDENTIFICATION_COMPLETE event should be received." +
                "Rights after this a REQUEST_SENT should be received and a GetAuditTrailsRequest should " +
                "be sent to pillar1");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);
        IdentifyContributorsForGetAuditTrailsResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.REQUEST_SENT);
        GetAuditTrailsRequest requestPillar1 = pillar1Destination.waitForMessage(GetAuditTrailsRequest.class);
        GetAuditTrailsRequest request = testMessageFactory.createGetAuditTrailsRequest(
                identifyRequest, PILLAR1_ID, pillar1DestinationId);
        request.setMinSequenceNumber(BigInteger.valueOf(1));
        request.setMaxSequenceNumber(BigInteger.valueOf(3));
        Assert.assertEquals(requestPillar1, request);

        addStep("Verify no request is sent to pillar2", "");
        pillar2Destination.checkNoMessageIsReceived(GetAuditTrailsRequest.class);

        addStep("Send a final response from pillar 1",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results." +
                "This should be followed by a COMPLETE event");
        ResultingAuditTrails result = createTestResultingAuditTrails(PILLAR1_ID);
        GetAuditTrailsFinalResponse resultResponse =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR1_ID, pillar1DestinationId, result);
        messageBus.sendMessage(resultResponse);
        AuditTrailResult resultEvent = (AuditTrailResult)testEventHandler.waitForEvent();
        Assert.assertEquals(resultEvent.getType(), OperationEvent.OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(resultEvent.getAuditTrailEvents(), result);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void negativIdentifyResponse() throws Exception {
        addDescription("Verify that the GetAuditTrail client works correct when receiving a negativ " +
                "identify response from one contributers.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailClient client = createAuditTrailClient();

        addStep("Retrieve all audit trails from the collection by calling with a null componentQueries array",
                "");
        client.getAuditTrails(null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a negative identifyResponse from pillar 1.",
                "A COMPONENT_FAILED event should be received");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        ResponseInfo info = new ResponseInfo();
        info.setResponseText("Identify failed");
        info.setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        responsePillar1.setResponseInfo(info);
        messageBus.sendMessage(responsePillar1);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_FAILED);

        addStep("Send a positive identifyResponse from pillar 2.",
                "An COMPONENT_IDENTIFIED and IDENTIFICATION_COMPLETE event should be received" +
                "Right after this a GetAuditTrailRequest should be sent to pillar2");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.REQUEST_SENT);
        pillar2Destination.waitForMessage(GetAuditTrailsRequest.class);
    }

    @Test(groups = {"regressiontest"})
    public void negativeGetAuditTrailsResponse() throws InterruptedException {
        addDescription("Verify that the GetAuditTrail client works correct when receiving a negativ " +
                       "GetAuditTrails response from one contributers.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailClient client = createAuditTrailClient();

        addStep("Retrieve all audit trails from the collection by calling with a null componentQueries array",
                "This should be interpreted as a request for all audit trails from all the collection settings " +
                "defined contributers.");
        client.getAuditTrails(null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identifyResponse from each of the two pillars",
                "COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                "Rights after this a REQUEST_SENT should be received and a GetAuditTrailsRequest should " +
                "be sent to each pillar");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);
        IdentifyContributorsForGetAuditTrailsResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.REQUEST_SENT);
        GetAuditTrailsRequest requestPillar1 = pillar1Destination.waitForMessage(GetAuditTrailsRequest.class);
        Assert.assertEquals(requestPillar1, testMessageFactory.createGetAuditTrailsRequest(
                identifyRequest, PILLAR1_ID, pillar1DestinationId));
        GetAuditTrailsRequest requestPillar2 = pillar2Destination.waitForMessage(GetAuditTrailsRequest.class);
        Assert.assertEquals(requestPillar2, testMessageFactory.createGetAuditTrailsRequest(
                identifyRequest, PILLAR2_ID, pillar2DestinationId));

        addStep("Send a failed response from pillar 1",
                "A COMPONENT_FAILED event should be generated.");
        ResultingAuditTrails result1 = createTestResultingAuditTrails(PILLAR1_ID);
        GetAuditTrailsFinalResponse failedResponsePillar1 =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR1_ID, pillar1DestinationId, result1);
        ResponseInfo failedInfo = new ResponseInfo();
        failedInfo.setResponseText("GetAuditTrails failed");
        failedInfo.setResponseCode(ResponseCode.FAILURE);
        failedResponsePillar1.setResponseInfo(failedInfo);
        messageBus.sendMessage(failedResponsePillar1);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_FAILED);

        addStep("Send a final response from pillar 2",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results." +
                "This should be followed by a COMPLETE event");
        ResultingAuditTrails result2 = createTestResultingAuditTrails(PILLAR2_ID);
        GetAuditTrailsFinalResponse resultPillar2 =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR2_ID, pillar2DestinationId, result2);
        messageBus.sendMessage(resultPillar2);

        AuditTrailResult result2Event = (AuditTrailResult)testEventHandler.waitForEvent();
        Assert.assertEquals(result2Event.getType(), OperationEvent.OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(result2Event.getAuditTrailEvents(), result2);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPLETE);
    }

    @Test(groups = {"regressiontest"})
    public void progressEvents() throws InterruptedException {
        addDescription("Tests that progress events are handled correctly.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailClient client = createAuditTrailClient();

        addStep("Retrieve all audit trails from the collection by calling with a null componentQueries array",
                "This should be interpreted as a request for all audit trails from all the collection settings " +
                "defined contributers.");
        client.getAuditTrails(null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identifyResponse from each of the two pillars",
                "COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                "Rights after this a REQUEST_SENT should be received and a GetAuditTrailsRequest should " +
                "be sent to each pillar");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);
        IdentifyContributorsForGetAuditTrailsResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.REQUEST_SENT);
        GetAuditTrailsRequest requestPillar1 = pillar1Destination.waitForMessage(GetAuditTrailsRequest.class);
        Assert.assertEquals(requestPillar1, testMessageFactory.createGetAuditTrailsRequest(
                identifyRequest, PILLAR1_ID, pillar1DestinationId));
        GetAuditTrailsRequest requestPillar2 = pillar2Destination.waitForMessage(GetAuditTrailsRequest.class);
        Assert.assertEquals(requestPillar2, testMessageFactory.createGetAuditTrailsRequest(
                identifyRequest, PILLAR2_ID, pillar2DestinationId));

        addStep("Send a progress accepted response from pillar 1",
                "A PROGRESS event should be generated.");
        GetAuditTrailsProgressResponse progressResponse1 =
                testMessageFactory.createGetAuditTrailsProgressResponse(requestPillar1,
                        PILLAR1_ID, pillar1DestinationId);
        ResponseInfo progressInfo1 = new ResponseInfo();
        progressInfo1.setResponseText("GetAuditTrails request accepted");
        progressInfo1.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        progressResponse1.setResponseInfo(progressInfo1);
        messageBus.sendMessage(progressResponse1);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.PROGRESS);

        addStep("Send a general progress response from pillar 2",
                "A PROGRESS event should be generated with the audit trail results.");
        GetAuditTrailsProgressResponse progressResponse2 =
                testMessageFactory.createGetAuditTrailsProgressResponse(requestPillar1,
                        PILLAR2_ID, pillar2DestinationId);
        ResponseInfo progressInfo2 = new ResponseInfo();
        progressInfo2.setResponseText("Still progressing");
        progressInfo2.setResponseCode(ResponseCode.OPERATION_PROGRESS);
        progressResponse2.setResponseInfo(progressInfo2);
        messageBus.sendMessage(progressResponse2);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.PROGRESS);
    }

   @Test(groups = {"regressiontest"})
   public void incompleteSetOfIdendifyResponses() throws Exception {
       addDescription("Verify that the GetAuditTrail client works correct without receiving responses from all " +
               "contributers.");
       addStep("Configure 3 second timeout for identifying contributers. " +
               "The default 2 contributers collection is used", "");

       settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       AuditTrailClient client = createAuditTrailClient();

       client.getAuditTrails(null, null, null, testEventHandler, null);
       IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
               collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(),
               OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

       addStep("Send a identifyResponse from pillar 1",
               "A COMPONENT_IDENTIFIED event should be received.");
       IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
               testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                       PILLAR1_ID, pillar1DestinationId);
       messageBus.sendMessage(responsePillar1);

       Assert.assertEquals(testEventHandler.waitForEvent().getType(),
               OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);

       addStep("Wait for 5 seconds", "An IDENTIFY_TIMEOUT and IDENTIFICATION_COMPLETE event should be received" +
               "Right after this a GetAuditTrailRequest should be sent to pillar1");
       Assert.assertEquals(testEventHandler.waitForEvent( 5, TimeUnit.SECONDS).getType(),
               OperationEvent.OperationEventType.IDENTIFY_TIMEOUT);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(),
               OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(),
               OperationEvent.OperationEventType.REQUEST_SENT);
       pillar1Destination.waitForMessage(GetAuditTrailsRequest.class);
   }

    @Test(groups = {"regressiontest"})
    public void incompleteSetOfFinalResponses() throws Exception {
        addDescription("Verify that the GetAuditTrail client works correct without receiving responses from all " +
                "contributers.");
        addStep("Configure 3 second timeout for the operation itself. " +
                "The default 2 contributers collection is used", "");

        settings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(3000));
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailClient client = createAuditTrailClient();

        client.getAuditTrails(null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identifyResponse from each of the two pillars",
                "COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                        "Rights after this a REQUEST_SENT should be received and a GetAuditTrails request should " +
                        "be sent to each pillar");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);
        IdentifyContributorsForGetAuditTrailsResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.REQUEST_SENT);
        GetAuditTrailsRequest requestPillar1 = pillar1Destination.waitForMessage(GetAuditTrailsRequest.class);
    }

   @Test(groups = {"regressiontest"})
   public void noIdentifyResponse() throws Exception {
       addDescription("Tests the AuditTrailClient handles lack of IdentifyResponses gracefully  ");
       addStep("Set a 3 second timeout for identifying contributers.", "");

       settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
       AuditTrailClient client = createAuditTrailClient();

       addStep("Make the client ask for all audit trails.",
               "It should send a identify message");
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       client.getAuditTrails(null, null, null, testEventHandler, null);
       IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
               collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(),
               OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

       addStep("Wait for 5 seconds", "An IDENTIFY_TIMEOUT event should be received follwed by a FAILED event");
       Assert.assertEquals(testEventHandler.waitForEvent( 5, TimeUnit.SECONDS).getType(),
               OperationEvent.OperationEventType.IDENTIFY_TIMEOUT);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.FAILED);
   }

    @Test(groups = {"regressiontest"})
    public void noFinalResponses() throws Exception {
        addDescription("Tests the the AuditTrailClient handles lack of Final Responses gracefully  ");
        addStep("Set a 3 second timeout for the operation.", "");

        settings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(3000));
        AuditTrailClient client = createAuditTrailClient();

        addStep("Make the client ask for all audit trails.",
                "It should send a identify message");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        client.getAuditTrails(null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Send a identifyResponse from each of the two pillars",
                "COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                        "Rights after this a REQUEST_SENT should be received.");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);
        IdentifyContributorsForGetAuditTrailsResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(identifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                OperationEvent.OperationEventType.REQUEST_SENT);

        addStep("Wait for 5 seconds", "An failed event should be received");
        Assert.assertEquals(testEventHandler.waitForEvent( 5, TimeUnit.SECONDS).getType(),
                OperationEvent.OperationEventType.FAILED);
    }

   @Test(groups = {"regressiontest"})
   public void conversationTimeout() throws Exception {
       addDescription("Tests a Audit Trail conversation times out gracefully.");
       addStep("Set a 3 second timeout for conversations.", "");

       //We need to use a different collection ID to avoid using a existing conversation mediator.
       settings.getCollectionSettings().setCollectionID("conversationTimeoutTest");
       settings.getReferenceSettings().getClientSettings().setConversationTimeout(BigInteger.valueOf(3000));
       AuditTrailClient client = createAuditTrailClient();

       addStep("Make the client ask for all audit trails.",
               "It should send a identify message");
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       client.getAuditTrails(null, null, null, testEventHandler, null);
       IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
               collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(),
               OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

       addStep("Wait for 5 seconds", "An failed event should be received");
       Assert.assertEquals(testEventHandler.waitForEvent( 5, TimeUnit.SECONDS).getType(),
               OperationEvent.OperationEventType.FAILED);
   }

    /**
     * Creates a new test AuditTrailClient based on the supplied settings.
     *
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new AuditTrailClient(Wrapper).
     */
    private AuditTrailClient createAuditTrailClient() {
        MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration(), securityManager);
        ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);
        return new AuditTrailClientTestWrapper(new ConversationBasedAuditTrailClient(
                settings, conversationMediator, messageBus, TEST_CLIENT_ID) , testEventManager);
    }

    private ResultingAuditTrails createTestResultingAuditTrails(String componentID) {
        ResultingAuditTrails auditTrails = new ResultingAuditTrails();
        AuditTrailEvents events = new AuditTrailEvents();

        AuditTrailEvent event1 = new AuditTrailEvent();
        event1.setActorOnFile(componentID);
        event1.setActionDateTime(CalendarUtils.getNow());
        event1.setSequenceNumber(BigInteger.valueOf(1));
        event1.setActionOnFile(FileAction.PUT_FILE);
        event1.setReportingComponent(componentID);
        event1.setAuditTrailInformation("Example audit trail information");
        event1.setFileID("File1");
        event1.setInfo("Test audit trail 1");

        AuditTrailEvent event2 = new AuditTrailEvent();
        event2.setActorOnFile(componentID);
        event2.setActionDateTime(CalendarUtils.getNow());
        event2.setSequenceNumber(BigInteger.valueOf(2));
        event2.setActionOnFile(FileAction.CHECKSUM_CALCULATED);
        event2.setReportingComponent(componentID);
        event2.setAuditTrailInformation("Example audit trail information");
        event2.setFileID("File1");
        event2.setInfo("Test audit trail 2");

        events.getAuditTrailEvent().add(event1);
        events.getAuditTrailEvent().add(event2);
        auditTrails.setAuditTrailEvents(events);
        return auditTrails;
    }
}
