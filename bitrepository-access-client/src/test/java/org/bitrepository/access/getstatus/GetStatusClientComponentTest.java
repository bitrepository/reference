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
package org.bitrepository.access.getstatus;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getstatus.conversation.StatusCompleteContributorEvent;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.client.DefaultFixtureClientTest;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.message.TestGetStatusMessageFactory;
import org.bitrepository.settings.repositorysettings.GetStatusSettings;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.List;

public class GetStatusClientComponentTest extends DefaultFixtureClientTest {

        private TestGetStatusMessageFactory testMessageFactory;

        @BeforeMethod(alwaysRun=true)
        public void beforeMethodSetup() throws Exception {
            testMessageFactory = new TestGetStatusMessageFactory(settingsForTestClient.getComponentID());

            if (settingsForCUT.getRepositorySettings().getGetStatusSettings() == null) {
                settingsForCUT.getRepositorySettings().setGetStatusSettings(new GetStatusSettings());
            }
            List<String> contributers = settingsForCUT.getRepositorySettings().getGetStatusSettings().getNonPillarContributorIDs();
            contributers.clear();
            contributers.add(PILLAR1_ID);
            contributers.add(PILLAR2_ID);
        }

        @Test(groups = {"regressiontest"})
        public void verifyGetStatusClientFromFactory() throws Exception {
            Assert.assertTrue(AccessComponentFactory.getInstance().createGetStatusClient(
                    settingsForCUT, securityManager, settingsForTestClient.getComponentID())
                    instanceof ConversationBasedGetStatusClient,
                    "The default GetStatusClient from the Access factory should be of the type '" +
                            ConversationBasedGetStatusClient.class.getName() + "'.");
        }
        
        @Test(groups = {"regressiontest"})
        public void incompleteSetOfIdendifyResponses() throws Exception {
            addDescription("Verify that the GetStatus client works correct without receiving responses from all " +
                    "contributers.");
            addStep("Configure 5 second timeout for identifying contributers. " +
                    "The default 2 contributers collection is used", "");

            settingsForCUT.getRepositorySettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(5000));
            TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
            GetStatusClient client = createGetStatusClient();

            client.getStatus(testEventHandler);
            IdentifyContributorsForGetStatusRequest identifyRequest =
                    collectionReceiver.waitForMessage(IdentifyContributorsForGetStatusRequest.class);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.IDENTIFY_REQUEST_SENT);

            addStep("Send a identifyResponse from pillar 1",
                    "A COMPONENT_IDENTIFIED event should be received.");
            IdentifyContributorsForGetStatusResponse responsePillar1 =
                    testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                            PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(responsePillar1);

            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.COMPONENT_IDENTIFIED);

            addStep("Wait for timeout event", "An IDENTIFY_TIMEOUT and IDENTIFICATION_COMPLETE event should be received" +
                    "Right after this a GetStatusRequest should be sent to pillar1");
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.IDENTIFY_TIMEOUT);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.COMPONENT_FAILED);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.IDENTIFICATION_COMPLETE);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.REQUEST_SENT);
            pillar1Receiver.waitForMessage(GetStatusRequest.class);
        }
        
        @Test(groups = {"regressiontest"})
        public void getAllStatuses() throws InterruptedException {
            addDescription("Tests the simplest case of getting status for all contributers.");

            addStep("Create a GetStatusClient.", "");
            TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
            GetStatusClient client = createGetStatusClient();

            addStep("Retrieve from all contributors in the collection",
                    "This should be interpreted as a request for getting statuses from all contributors defined " +
                    "in the collection settings.");
            client.getStatus(testEventHandler);
            IdentifyContributorsForGetStatusRequest identifyRequest =
                    collectionReceiver.waitForMessage(IdentifyContributorsForGetStatusRequest.class);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.IDENTIFY_REQUEST_SENT);

            addStep("Send a identifyResponse from each pillar",
                    "Two COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                    "Rights after this a REQUEST_SENT should be received and a GetStatusRequest should " +
                    "be sent to each pillar");
            IdentifyContributorsForGetStatusResponse responsePillar1 =
                    testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                            PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(responsePillar1);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.COMPONENT_IDENTIFIED);

            IdentifyContributorsForGetStatusResponse responsePillar2 =
                    testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                            PILLAR2_ID, pillar2DestinationId);
            messageBus.sendMessage(responsePillar2);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.COMPONENT_IDENTIFIED);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.IDENTIFICATION_COMPLETE);

            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.REQUEST_SENT);
            GetStatusRequest requestPillar1 = pillar1Receiver.waitForMessage(GetStatusRequest.class);
            Assert.assertEquals(requestPillar1, testMessageFactory.createGetStatusRequest(
                    requestPillar1, PILLAR1_ID, pillar1DestinationId, settingsForTestClient.getComponentID()));
            GetStatusRequest requestPillar2 = pillar2Receiver.waitForMessage(GetStatusRequest.class);
            Assert.assertEquals(requestPillar2, testMessageFactory.createGetStatusRequest(
                    requestPillar2, PILLAR2_ID, pillar2DestinationId, settingsForTestClient.getComponentID()));

            addStep("Send a final response from pillar 1",
                    "A COMPONENT_COMPLETE event should be generated with the audit trail results.");
            ResultingStatus status1 = createTestResultingStatus(PILLAR1_ID);
            GetStatusFinalResponse resultPillar1 =
                    testMessageFactory.createGetStatusFinalResponse(requestPillar1,
                            PILLAR1_ID, pillar1DestinationId, status1);
            messageBus.sendMessage(resultPillar1);
            StatusCompleteContributorEvent result1Event = (StatusCompleteContributorEvent) testEventHandler.waitForEvent();
            Assert.assertEquals(result1Event.getEventType(), OperationEventType.COMPONENT_COMPLETE);
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
            Assert.assertEquals(result2Event.getEventType(), OperationEventType.COMPONENT_COMPLETE);
            Assert.assertEquals(result2Event.getStatus(), status2);
            Assert.assertEquals(testEventHandler.waitForEvent().getEventType(),
                    OperationEventType.COMPLETE);
        }

        /**
         * Creates a new test GetStatusClient based on the supplied settings.
         *
         * Note that the normal way of creating client through the module factory would reuse components with settings from
         * previous tests.
         * @return A new GetStatusClient(Wrapper).
         */
        private GetStatusClient createGetStatusClient() {
            return new GetStatusClientTestWrapper(new ConversationBasedGetStatusClient(
                    messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()) , testEventManager);
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
