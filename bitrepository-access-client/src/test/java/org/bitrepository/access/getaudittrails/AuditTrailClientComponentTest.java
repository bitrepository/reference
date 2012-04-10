package org.bitrepository.access.getaudittrails;

import java.util.List;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.access.getaudittrails.client.ConversationBasedAuditTrailClient;
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
    private TestGetAuditTrailsMessageFactory testMessageFactory;

    @BeforeMethod(alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        testMessageFactory = new TestGetAuditTrailsMessageFactory(settings.getCollectionID());

        if (settings.getCollectionSettings().getGetAuditTrailSettings() == null) {
            settings.getCollectionSettings().setGetAuditTrailSettings(new GetAuditTrailSettings());
        }
        List<String> contributers = settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs();
        contributers.clear();
        contributers.add(PILLAR1_ID);
        contributers.add(PILLAR2_ID);
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
        client.getAuditTrails(null, null, testEventHandler, null);
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

    }

    /*
   @Test(groups = {"regressiontest"})
   public void chooseFastestPillarAuditTrailClientWithIdentifyTimeout() throws Exception {
       addDescription("Verify that the FastestPillarGetFile works correct without receiving responses from all " +
               "pillars.");
       addStep("Create a AuditTrailClient configured to use 3 pillars and a 3 second timeout for identifying pillar.", "");

       String averagePillarID = "THE-AVERAGE-PILLAR";
       String fastPillarID = "THE-FAST-PILLAR";
       String slowPillarID = "THE-SLOW-PILLAR";
       settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
       settings.getCollectionSettings().getClientSettings().getPillarIDs().add(averagePillarID);
       settings.getCollectionSettings().getClientSettings().getPillarIDs().add(fastPillarID);
       settings.getCollectionSettings().getClientSettings().getPillarIDs().add(slowPillarID);
       settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
       AuditTrailClient client = createAuditTrailClient();

       addStep("Make the GetClient ask for fastest pillar.",
               "It should send message to identify which pillar can respond fastest.");
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       client.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
       IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
       if (useMockupPillar()) {
           receivedIdentifyRequestMessage =
                   collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
       }
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

       addStep("Two pillars send responses. First an average timeToDeliver, then a fast timeToDeliver.",
               "The client should send a getFileRequest to the fast pillar after 3 seconds. " +
                       "The event handler should receive the following events: " +
                       "2 x PillarIdentified, a identify timeout, a PillarSelected and a RequestSent event.");

       if (useMockupPillar()) {
           IdentifyPillarsForGetFileResponse averageReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                   receivedIdentifyRequestMessage, averagePillarID, pillar2DestinationId);
           TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
           averageTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
           averageTime.setTimeMeasureValue(BigInteger.valueOf(100L));
           averageReply.setTimeToDeliver(averageTime);
           messageBus.sendMessage(averageReply);

           IdentifyPillarsForGetFileResponse fastReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                   receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
           TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
           fastTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
           fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
           fastReply.setTimeToDeliver(fastTime);
           messageBus.sendMessage(fastReply);

           GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(
                   GetFileRequest.class, 5, TimeUnit.SECONDS );
           Assert.assertEquals(receivedGetFileRequest,
                   testMessageFactory.createGetFileRequest(receivedGetFileRequest, fastPillarID, pillar1DestinationId));
       }

       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.IDENTIFY_TIMEOUT);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.REQUEST_SENT);
   }

   @Test(groups = {"regressiontest"})
   public void noIdentifyResponse() throws Exception {
       addDescription("Tests the the AuditTrailClient handles lack of IdentifyPillarResponses gracefully  ");
       addStep("Set the number of pillars to 1 and a 3 second timeout for identifying pillar.", "");

       settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
       settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
       settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
       AuditTrailClient client = createAuditTrailClient();

       addStep("Make the GetClient ask for fastest pillar.",
               "It should send message to identify which pillar can respond fastest.");
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       client.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
       if (useMockupPillar()) {
           collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
       }
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

       addStep("Wait for 5 seconds", "An IdentifyPillarTimeout event should be received");

       Assert.assertEquals(testEventHandler.waitForEvent( 5, TimeUnit.SECONDS).getType(), OperationEvent.OperationEventType.FAILED);
   }

   @Test(groups = {"regressiontest"})
   public void conversationTimeout() throws Exception {
       addDescription("Tests the the AuditTrailClient handles lack of IdentifyPillarResponses gracefully  ");
       addStep("Set the number of pillars to 1 and a 3 second timeout for the conversation.", "");

       //We need to use a different collection ID to avoid using a existing conversation mediator.
       String newCollectionID = "conversationTimeoutTest";
       settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
       settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
       settings.getReferenceSettings().getClientSettings().setConversationTimeout(BigInteger.valueOf(3000));
       AuditTrailClient client = createAuditTrailClient();

       addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.",
               "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       client.getFileFromSpecificPillar( DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), PILLAR1_ID,
               testEventHandler);
       IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
       if (useMockupPillar()) {
           receivedIdentifyRequestMessage =
                   collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
           IdentifyPillarsForGetFileRequest expectedMessage =
                   testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage,
                           collectionDestinationID);
           Assert.assertEquals(receivedIdentifyRequestMessage, expectedMessage);
       }
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

       addStep("The pillar sends a response to the identify message.",
               "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
                       "the pillar");

       if (useMockupPillar()) {
           IdentifyPillarsForGetFileResponse identifyResponse =
                   testMessageFactory.createIdentifyPillarsForGetFileResponse(
                           receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
           messageBus.sendMessage(identifyResponse);
           pillar1Destination.waitForMessage(GetFileRequest.class);
       }

       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.REQUEST_SENT);

       addStep("Wait for 5 seconds", "An failed event should be received");
       Assert.assertEquals(testEventHandler.waitForEvent(5, TimeUnit.SECONDS).getType(), OperationEvent.OperationEventType.FAILED);
   }

   @Test(groups = {"regressiontest"})
   public void testNoSuchFileSpecificPillar() throws Exception {
       addDescription("Testing how a request for a non-existing file is handled on a specific pillar request.");
       addStep("Define 2 pillars.", "");
       String fileName = "ERROR-NO-SUCH-FILE-ERROR";
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       URL url = httpServer.getURL(DEFAULT_FILE_ID);

       addStep("Call getFileFromSpecificPillar.",
               "An identify request should be sent and an IdentifyPillarsRequestSent event should be generate");
       AuditTrailClient client = createAuditTrailClient();

       client.getFileFromSpecificPillar(fileName, url, PILLAR1_ID, testEventHandler);
       IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
               collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

       addStep("The specified pillars sends a FILE_NOT_FOUND response",
               "The client should generate 1 PillarIdentified event followed by a operation failed event.");
       IdentifyPillarsForGetFileResponse pillar1Response = testMessageFactory.createIdentifyPillarsForGetFileResponse(
               receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
       pillar1Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
       pillar1Response.getResponseInfo().setResponseText("File " +
               receivedIdentifyRequestMessage.getFileID() + " not present on this pillar " + PILLAR1_ID);
       messageBus.sendMessage(pillar1Response);

       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.FAILED);
   }

   @Test(groups = {"regressiontest"})
   public void testNoSuchFileMultiplePillars() throws Exception {
       addDescription("Testing how a request for a non-existing file is handled when all pillars miss the file.");

       String fileName = "ERROR-NO-SUCH-FILE-ERROR";
       AuditTrailClient client = createAuditTrailClient();
       TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
       URL url = httpServer.getURL(DEFAULT_FILE_ID);

       addStep("Use the default 2 pillars.", "");

       addStep("Call getFileFromFastestPillar.",
               "An identify request should be sent and a IdentifyPillarsRequestSent event should be generate");
       client.getFileFromFastestPillar(fileName, url, testEventHandler);
       IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
               collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

       addStep("Both pillars sends a FILE_NOT_FOUND response",
               "The client should generate 2 PillarIdentified events followed by a Failed event.");

       IdentifyPillarsForGetFileResponse pillar1Response = testMessageFactory.createIdentifyPillarsForGetFileResponse(
               receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
       pillar1Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
       pillar1Response.getResponseInfo().setResponseText("File " +
               receivedIdentifyRequestMessage.getFileID() + "not present on this pillar " );
       messageBus.sendMessage(pillar1Response);

       IdentifyPillarsForGetFileResponse pillar2Response = testMessageFactory.createIdentifyPillarsForGetFileResponse(
               receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
       pillar2Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
       pillar2Response.getResponseInfo().setResponseText("File " +
               receivedIdentifyRequestMessage.getFileID() + "not present on this pillar " );
       messageBus.sendMessage(pillar2Response);

       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
       Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEvent.OperationEventType.FAILED);
   } */

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
