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
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.message.TestGetStatusMessageFactory;
import org.bitrepository.settings.repositorysettings.GetStatusSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import java.util.List;


public class GetStatusClientComponentTest extends DefaultFixtureClientTest {

    private TestGetStatusMessageFactory testMessageFactory;

    @BeforeEach
    public void beforeMethodSetup() {
        testMessageFactory = new TestGetStatusMessageFactory(settingsForTestClient.getComponentID());

        if (settingsForCUT.getRepositorySettings().getGetStatusSettings() == null) {
            settingsForCUT.getRepositorySettings().setGetStatusSettings(new GetStatusSettings());
        }
        List<String> contributors = settingsForCUT.getRepositorySettings().getGetStatusSettings().getNonPillarContributorIDs();
        contributors.clear();
        contributors.add(PILLAR1_ID);
        contributors.add(PILLAR2_ID);
    }

    @Test
    @Tag("regressiontest")
    public void verifyGetStatusClientFromFactory() {
        Assertions.assertInstanceOf(ConversationBasedGetStatusClient.class, AccessComponentFactory.getInstance().createGetStatusClient(
                settingsForCUT, securityManager, settingsForTestClient.getComponentID()), "The default GetStatusClient from the Access factory should be of the type '" +
                ConversationBasedGetStatusClient.class.getName() + "'.");
    }

    @Test
    @Tag("regressiontest")
    public void incompleteSetOfIdendifyResponses() throws Exception {
        addDescription("Verify that the GetStatus client works correct without receiving responses from all " +
                "contributors.");
        addStep("Configure 1 second timeout for identifying contributors. " +
                "The default 2 contributors collection is used", "");

        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        settingsForCUT.getRepositorySettings().getClientSettings()
                .setIdentificationTimeoutDuration(datatypeFactory.newDuration(1000));
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetStatusClient client = createGetStatusClient();

        client.getStatus(testEventHandler);
        IdentifyContributorsForGetStatusRequest identifyRequest =
                collectionReceiver.waitForMessage(IdentifyContributorsForGetStatusRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("Send a identifyResponse from pillar 1",
                "A COMPONENT_IDENTIFIED event should be received.");
        IdentifyContributorsForGetStatusResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);

        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());

        addStep("Wait for timeout event", "An IDENTIFY_TIMEOUT and IDENTIFICATION_COMPLETE event should be received" +
                "Right after this a GetStatusRequest should be sent to pillar1");
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_TIMEOUT, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_FAILED, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());
        pillar1Receiver.waitForMessage(GetStatusRequest.class);
    }

    @Test
    @Tag("regressiontest")
    public void getAllStatuses() throws InterruptedException {
        addDescription("Tests the simplest case of getting status for all contributors.");

        addStep("Create a GetStatusClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetStatusClient client = createGetStatusClient();

        addStep("Retrieve from all contributors in the collection",
                "This should be interpreted as a request for getting statuses from all contributors defined " +
                        "in the collection settings.");
        client.getStatus(testEventHandler);
        IdentifyContributorsForGetStatusRequest identifyRequest =
                collectionReceiver.waitForMessage(IdentifyContributorsForGetStatusRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT, testEventHandler.waitForEvent().getEventType());

        addStep("Send a identifyResponse from each pillar",
                "Two COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                        "Rights after this a REQUEST_SENT should be received and a GetStatusRequest should " +
                        "be sent to each pillar");
        IdentifyContributorsForGetStatusResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());

        IdentifyContributorsForGetStatusResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetStatusResponse(identifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED, testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE, testEventHandler.waitForEvent().getEventType());

        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT, testEventHandler.waitForEvent().getEventType());
        GetStatusRequest requestPillar1 = pillar1Receiver.waitForMessage(GetStatusRequest.class);
        Assertions.assertEquals(testMessageFactory.createGetStatusRequest(
                requestPillar1, PILLAR1_ID, pillar1DestinationId, settingsForTestClient.getComponentID()), requestPillar1);
        GetStatusRequest requestPillar2 = pillar2Receiver.waitForMessage(GetStatusRequest.class);
        Assertions.assertEquals(testMessageFactory.createGetStatusRequest(
                requestPillar2, PILLAR2_ID, pillar2DestinationId, settingsForTestClient.getComponentID()), requestPillar2);

        addStep("Send a final response from pillar 1",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results.");
        ResultingStatus status1 = createTestResultingStatus(PILLAR1_ID);
        GetStatusFinalResponse resultPillar1 =
                testMessageFactory.createGetStatusFinalResponse(requestPillar1,
                        PILLAR1_ID, pillar1DestinationId, status1);
        messageBus.sendMessage(resultPillar1);
        StatusCompleteContributorEvent result1Event = (StatusCompleteContributorEvent) testEventHandler.waitForEvent();
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, result1Event.getEventType());
        Assertions.assertEquals(status1, result1Event.getStatus());

        addStep("Send a final response from pillar 2",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results." +
                        "This should be followed by a COMPLETE event");
        ResultingStatus status2 = createTestResultingStatus(PILLAR2_ID);
        GetStatusFinalResponse resultPillar2 =
                testMessageFactory.createGetStatusFinalResponse(requestPillar1,
                        PILLAR2_ID, pillar2DestinationId, status2);
        messageBus.sendMessage(resultPillar2);

        StatusCompleteContributorEvent result2Event = (StatusCompleteContributorEvent) testEventHandler.waitForEvent();
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, result2Event.getEventType());
        Assertions.assertEquals(status2, result2Event.getStatus());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPLETE, testEventHandler.waitForEvent().getEventType());
    }

    /**
     * Creates a new test GetStatusClient based on the supplied settings.
     * <p>
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     *
     * @return A new GetStatusClient(Wrapper).
     */
    private GetStatusClient createGetStatusClient() {
        return new GetStatusClientTestWrapper(new ConversationBasedGetStatusClient(
                messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID()), testEventManager);
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
