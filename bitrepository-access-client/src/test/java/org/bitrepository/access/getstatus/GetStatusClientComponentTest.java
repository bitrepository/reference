package org.bitrepository.access.getstatus;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.access.getstatus.conversation.StatusCompleteContributorEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.settings.collectionsettings.GetStatusSettings;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetStatusClientComponentTest extends DefaultFixtureClientTest{

        private TestGetStatusMessageFactory testMessageFactory;

        @BeforeMethod(alwaysRun=true)
        public void beforeMethodSetup() throws Exception {
            testMessageFactory = new TestGetStatusMessageFactory(settings.getCollectionID(), TEST_CLIENT_ID);

            if (settings.getCollectionSettings().getGetStatusSettings() == null) {
                settings.getCollectionSettings().setGetStatusSettings(new GetStatusSettings());
            }
            List<String> contributers = settings.getCollectionSettings().getGetStatusSettings().getContributorIDs();
            contributers.clear();
            contributers.add(PILLAR1_ID);
            contributers.add(PILLAR2_ID);
        }

        @Test(groups = {"regressiontest"})
        public void verifyGetStatusClientFromFactory() throws Exception {
            Assert.assertTrue(AccessComponentFactory.getInstance().createGetStatusClient(
                    settings, securityManager, TEST_CLIENT_ID)
                    instanceof CollectionBasedGetStatusClient,
                    "The default GetStatusClient from the Access factory should be of the type '" +
                            CollectionBasedGetStatusClient.class.getName() + "'.");
        }
    
        
        @Test(groups = {"regressiontest"})
        public void incompleteSetOfIdendifyResponses() throws Exception {
            addDescription("Verify that the GetStatus client works correct without receiving responses from all " +
                    "contributers.");
            addStep("Configure 3 second timeout for identifying contributers. " +
                    "The default 2 contributers collection is used", "");

            settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
            TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
            GetStatusClient client = createGetStatusClient();

            client.getStatus(testEventHandler);
            IdentifyContributorsForGetStatusRequest identifyRequest =
                    collectionDestination.waitForMessage(IdentifyContributorsForGetStatusRequest.class);
            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

            addStep("Send a identifyResponse from pillar 1",
                    "A COMPONENT_IDENTIFIED event should be received.");
            IdentifyContributorsForGetStatusResponse responsePillar1 =
                    testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                            PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(responsePillar1);

            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);

            addStep("Wait for 5 seconds", "An IDENTIFY_TIMEOUT and IDENTIFICATION_COMPLETE event should be received" +
                    "Right after this a GetStatusRequest should be sent to pillar1");
            Assert.assertEquals(testEventHandler.waitForEvent( 5, TimeUnit.SECONDS).getType(),
                    OperationEvent.OperationEventType.IDENTIFY_TIMEOUT);
            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);
            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.REQUEST_SENT);
            pillar1Destination.waitForMessage(GetStatusRequest.class);
        }
        
        @Test(groups = {"regressiontest"})
        public void getAllAuditTrails() throws InterruptedException {
            addDescription("Tests the simplest case of getting status for all contributers.");

            addStep("Create a GetStatusClient.", "");
            TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
            GetStatusClient client = createGetStatusClient();

            addStep("Retrieve from all contributors in the collection",
                    "This should be interpreted as a request for getting statuses from all contributors defined " +
                    "in the collection settings.");
            client.getStatus(testEventHandler);
            IdentifyContributorsForGetStatusRequest identifyRequest =
                    collectionDestination.waitForMessage(IdentifyContributorsForGetStatusRequest.class);
            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

            addStep("Send a identifyResponse from each pillar",
                    "Two COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                    "Rights after this a REQUEST_SENT should be received and a GetStatusRequest should " +
                    "be sent to each pillar");
            IdentifyContributorsForGetStatusResponse responsePillar1 =
                    testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                            PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(responsePillar1);
            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);

            IdentifyContributorsForGetStatusResponse responsePillar2 =
                    testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                            PILLAR2_ID, pillar2DestinationId);
            messageBus.sendMessage(responsePillar2);
            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);

            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.REQUEST_SENT);
            GetStatusRequest requestPillar1 = pillar1Destination.waitForMessage(GetStatusRequest.class);
            Assert.assertEquals(requestPillar1, testMessageFactory.createGetStatusRequest(
                    requestPillar1, PILLAR1_ID, pillar1DestinationId));
            GetStatusRequest requestPillar2 = pillar2Destination.waitForMessage(GetStatusRequest.class);
            Assert.assertEquals(requestPillar2, testMessageFactory.createGetStatusRequest(
                    requestPillar2, PILLAR2_ID, pillar2DestinationId));

            addStep("Send a final response from pillar 1",
                    "A COMPONENT_COMPLETE event should be generated with the audit trail results.");
            ResultingStatus status1 = createTestResultingStatus(PILLAR1_ID);
            GetStatusFinalResponse resultPillar1 =
                    testMessageFactory.createGetStatusFinalResponse(requestPillar1,
                            PILLAR1_ID, pillar1DestinationId, status1);
            messageBus.sendMessage(resultPillar1);
            StatusCompleteContributorEvent result1Event = (StatusCompleteContributorEvent) testEventHandler.waitForEvent();
            Assert.assertEquals(result1Event.getType(), OperationEvent.OperationEventType.COMPONENT_COMPLETE);
            Assert.assertEquals(result1Event.getStatus(), status1);

            addStep("Send a final response from pillar 2",
                    "A COMPONENT_COMPLETE event should be generated with the audit trail results." +
                            "This should be followed by a COMPLETE event");
            ResultingStatus status2 = createTestResultingStatus(PILLAR2_ID);
            GetStatusFinalResponse resultPillar2 =
                    testMessageFactory.createGetStatusFinalResponse(requestPillar1,
                            PILLAR2_ID, pillar2DestinationId, status2);
            messageBus.sendMessage(resultPillar2);

            StatusCompleteContributorEvent result2Event = (StatusCompleteContributorEvent) testEventHandler.waitForEvent();
            Assert.assertEquals(result2Event.getType(), OperationEvent.OperationEventType.COMPONENT_COMPLETE);
            Assert.assertEquals(result2Event.getStatus(), status2);
            Assert.assertEquals(testEventHandler.waitForEvent().getType(),
                    OperationEvent.OperationEventType.COMPLETE);
        }

        
        
        
        /**
         * Creates a new test GetStatusClient based on the supplied settings.
         *
         * Note that the normal way of creating client through the module factory would reuse components with settings from
         * previous tests.
         * @return A new GetStatusClient(Wrapper).
         */
        private GetStatusClient createGetStatusClient() {
            MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration(), securityManager);
            ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);
            return new GetStatusClientTestWrapper(new CollectionBasedGetStatusClient(
                    messageBus, conversationMediator, settings, TEST_CLIENT_ID) , testEventManager);
        }
        
        private ResultingStatus createTestResultingStatus(String componentID) {
            ResultingStatus resultingStatus = new ResultingStatus();
            StatusInfo info = new StatusInfo();
            info.setStatusCode(StatusCode.OK);
            info.setStatusText("Everythings fine..");
            resultingStatus.setStatusInfo(info);
            resultingStatus.setStatusTimestamp(CalendarUtils.getNow());
            return resultingStatus;
        }
}
