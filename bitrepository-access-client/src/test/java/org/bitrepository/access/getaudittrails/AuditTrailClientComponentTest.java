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

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.access.getaudittrails.client.ConversationBasedAuditTrailClient;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.settings.collectionsettings.GetAuditTrailSettings;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AuditTrailClientComponentTest extends DefaultFixtureClientTest {
    private GetAuditTrailsMessageFactory testMessageFactory;
    private FileIDs ALL_FILE_IDS;
    private final String TEST_CLIENT_ID = "test-client";

    @BeforeMethod(alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        testMessageFactory = new GetAuditTrailsMessageFactory(settings.getCollectionID());

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
        Assert.assertTrue(AccessComponentFactory.getInstance().createAuditTrailClient(settings, securityManager)
                instanceof ConversationBasedAuditTrailClient,
                "The default AuditTrailClient from the Access factory should be of the type '" +
                        ConversationBasedAuditTrailClient.class.getName() + "'.");
    }

    @Test(groups = {"testfirst"})
    public void getAllAuditTrails() throws InterruptedException {
        addDescription("Tests the simplest case of getting all audit trail event for all contributers.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailClient client = createAuditTrailClient();

        addStep("Retrieve all audit trails from the collection by calling with a null componentQueries array",
                "This should be interpreted as a request for all audit trails from all the collection settings " +
                        "defined contributers.");
        client.getAuditTrails(null, null, null, TEST_CLIENT_ID, testEventHandler, null);
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
                identifyRequest, pillar1DestinationId, TEST_CLIENT_ID));
        GetAuditTrailsRequest requestPillar2 = pillar1Destination.waitForMessage(GetAuditTrailsRequest.class);
        Assert.assertEquals(requestPillar2, testMessageFactory.createGetAuditTrailsRequest(
                identifyRequest, pillar2DestinationId, TEST_CLIENT_ID));

        addStep("Send a final response from each pillar with 2 audit trail events in each",
                "Two COMPONENT_COMPLETE event should be generated with the audit trail results. " +
                        "These should be followed by ");
    }

   @Test(groups = {"regressiontest"})
   public void incompleteSetOfIdendifyResponses() throws Exception {
       addDescription("Verify that the GetAuditTrails works correct without receiving responses from all " +
               "contributers.");
       addStep("Configure 3 second timeout for identifying contributers. " +
               "The default 2 contributers collection is used", "");

       settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       AuditTrailClient client = createAuditTrailClient();

       client.getAuditTrails(null, null, null, TEST_CLIENT_ID, testEventHandler, null);
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
        addDescription("Verify that the GetAuditTrails works correct without receiving responses from all " +
                "contributers.");
        addStep("Configure 3 second timeout for the operation itself. " +
                "The default 2 contributers collection is used", "");

        settings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(3000));
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailClient client = createAuditTrailClient();

        client.getAuditTrails(null, null, null, TEST_CLIENT_ID, testEventHandler, null);
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
       addDescription("Tests the the AuditTrailClient handles lack of IdentifyResponses gracefully  ");
       addStep("Set a 3 second timeout for identifying contributers.", "");

       settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
       AuditTrailClient client = createAuditTrailClient();

       addStep("Make the client ask for all audit trails.",
               "It should send a identify message");
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       client.getAuditTrails(null, null, null, TEST_CLIENT_ID, testEventHandler, null);
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
        client.getAuditTrails(null, null, null, TEST_CLIENT_ID, testEventHandler, null);
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
       client.getAuditTrails(null, null, null, TEST_CLIENT_ID, testEventHandler, null);
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
                settings, conversationMediator, messageBus)
                , testEventManager);
    }
}
