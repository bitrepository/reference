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

import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.bitrepositoryelements.*;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.client.DefaultClientTest;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.settings.repositorysettings.Collection;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;

/**
 * Test the default AuditTrailClient.
 */
@ExtendWith(SuiteInfoParameterResolver.class)
public class AuditTrailClientComponentTest extends DefaultClientTest {
    private GetAuditTrailsMessageFactory testMessageFactory;
    private DatatypeFactory datatypeFactory;

    @BeforeEach
    public void beforeMethodSetup() throws DatatypeConfigurationException {
        testMessageFactory = new GetAuditTrailsMessageFactory(settingsForTestClient.getComponentID());

        Collection c = settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0);
        c.setID(collectionID);
        c.getPillarIDs().getPillarID().clear();
        c.getPillarIDs().getPillarID().add(PILLAR1_ID);
        c.getPillarIDs().getPillarID().add(PILLAR2_ID);

        settingsForCUT.getRepositorySettings().getCollections().getCollection().clear();
        settingsForCUT.getRepositorySettings().getCollections().getCollection().add(c);

        settingsForCUT.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().clear();

        datatypeFactory = DatatypeFactory.newInstance();
    }

    @Test
    @Tag("regressiontest")
    @DisplayName("Tests that the AuditTrailClient can be created from the AccessComponentFactory.")
    public void verifyAuditTrailClientFromFactory() {
        Assertions.assertInstanceOf(ConversationBasedAuditTrailClient.class,
                AccessComponentFactory.getInstance().createAuditTrailClient(
                settingsForCUT, securityManager, settingsForTestClient.getComponentID()), "The default " +
                        "AuditTrailClient from the Access factory should be of the type '" +
                        ConversationBasedAuditTrailClient.class.getName() + "'.");
    }

    @Test
    @Tag("regressiontest")
    @DisplayName("Tests that the AuditTrailClient can be created from the AccessComponentFactory.")
    public void getAllAuditTrailsTest() throws InterruptedException {
        addDescription("Tests the simplest case of getting all audit trail event for all contributors.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler();
        AuditTrailClient client = createAuditTrailClient();

        addStep("Retrieve all audit trails from the collection by calling with a null componentQueries array",
                "This should be interpreted as a request for all audit trails from all the collection settings " +
                        "defined contributors.");
        client.getAuditTrails(collectionID, null, DEFAULT_FILE_ID, null, testEventHandler, null);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());
        IdentifyContributorsForGetAuditTrailsRequest receivedIdentifyRequest =
                collectionReceiver.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assertions.assertEquals(collectionID, receivedIdentifyRequest.getCollectionID());
        Assertions.assertNotNull(receivedIdentifyRequest.getCorrelationID());
        Assertions.assertEquals(settingsForCUT.getReceiverDestinationID(), receivedIdentifyRequest.getReplyTo());
        Assertions.assertEquals(settingsForTestClient.getComponentID(), receivedIdentifyRequest.getFrom());
        Assertions.assertEquals(settingsForTestClient.getCollectionDestination(),
                receivedIdentifyRequest.getDestination());

        addStep("Send a identifyResponse from each pillar",
                "Two COMPONENT_IDENTIFIED events and a IDENTIFICATION_COMPLETE event should be received." +
                        "Rights after this a REQUEST_SENT should be received and a GetAuditTrailsRequest should " +
                        "be sent to each pillar");
        IdentifyContributorsForGetAuditTrailsResponse responsePillar1 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(receivedIdentifyRequest,
                        PILLAR1_ID, pillar1DestinationId);
        messageBus.sendMessage(responsePillar1);
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());

        IdentifyContributorsForGetAuditTrailsResponse responsePillar2 =
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(receivedIdentifyRequest,
                        PILLAR2_ID, pillar2DestinationId);
        messageBus.sendMessage(responsePillar2);
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE,
                testEventHandler.waitForEvent().getEventType());

        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());
        GetAuditTrailsRequest requestPillar1 = pillar1Receiver.waitForMessage(GetAuditTrailsRequest.class);
        Assertions.assertEquals(collectionID, requestPillar1.getCollectionID());
        Assertions.assertEquals(receivedIdentifyRequest.getCorrelationID(), requestPillar1.getCorrelationID());
        Assertions.assertEquals(settingsForCUT.getReceiverDestinationID(), requestPillar1.getReplyTo());
        Assertions.assertEquals(DEFAULT_FILE_ID, requestPillar1.getFileID());
        Assertions.assertEquals(settingsForTestClient.getComponentID(), requestPillar1.getFrom());
        Assertions.assertEquals(pillar1DestinationId, requestPillar1.getDestination());

        GetAuditTrailsRequest requestPillar2 = pillar2Receiver.waitForMessage(GetAuditTrailsRequest.class);
        Assertions.assertEquals(collectionID, requestPillar2.getCollectionID());
        Assertions.assertEquals(receivedIdentifyRequest.getCorrelationID(), requestPillar2.getCorrelationID());
        Assertions.assertEquals(settingsForCUT.getReceiverDestinationID(), requestPillar2.getReplyTo());
        Assertions.assertEquals(DEFAULT_FILE_ID, requestPillar2.getFileID());
        Assertions.assertEquals(settingsForTestClient.getComponentID(), requestPillar2.getFrom());
        Assertions.assertEquals(pillar2DestinationId, requestPillar2.getDestination());

        addStep("Send a final response from pillar 1",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results.");
        ResultingAuditTrails result1 = createTestResultingAuditTrails(PILLAR1_ID);
        GetAuditTrailsFinalResponse resultPillar1 =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR1_ID, pillar1DestinationId, result1);
        messageBus.sendMessage(resultPillar1);
        AuditTrailResult result1Event = (AuditTrailResult) testEventHandler.waitForEvent();
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, result1Event.getEventType());
        Assertions.assertEquals(result1, result1Event.getAuditTrailEvents());

        addStep("Send a final response from pillar 2",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results." +
                        "This should be followed by a COMPLETE event");
        ResultingAuditTrails result2 = createTestResultingAuditTrails(PILLAR2_ID);
        GetAuditTrailsFinalResponse resultPillar2 =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR2_ID, pillar2DestinationId, result2);
        messageBus.sendMessage(resultPillar2);

        AuditTrailResult result2Event = (AuditTrailResult) testEventHandler.waitForEvent();
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, result2Event.getEventType());
        Assertions.assertEquals(result2, result2Event.getAuditTrailEvents());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPLETE,
                testEventHandler.waitForEvent().getEventType());
    }


    @Test
    @Tag("regressiontest")
    public void getSomeAuditTrailsTest() throws InterruptedException {
        addDescription("Tests the client maps a AuditTrail query correctly to a GetAuditTrail request.");

        TestEventHandler testEventHandler = new TestEventHandler();
        AuditTrailClient client = createAuditTrailClient();

        addStep("Request audit trails from pillar 1 with both min and max sequence number set.",
                "A identify request is sent.");
        AuditTrailQuery query1 = new AuditTrailQuery(PILLAR1_ID, 1L, 3L, 10000);
        client.getAuditTrails(collectionID, new AuditTrailQuery[]{query1}, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionReceiver.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());

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

        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE,
                testEventHandler.waitForEvent().getEventType());

        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());
        GetAuditTrailsRequest requestPillar1 = pillar1Receiver.waitForMessage(GetAuditTrailsRequest.class);
        Assertions.assertEquals(10000, requestPillar1.getMaxNumberOfResults().intValue());
        Assertions.assertEquals(1, requestPillar1.getMinSequenceNumber().intValue());
        Assertions.assertEquals(3, requestPillar1.getMaxSequenceNumber().intValue());

        addStep("Verify no request is sent to pillar2", "");
        pillar2Receiver.checkNoMessageIsReceived(GetAuditTrailsRequest.class);

        addStep("Send a final response from pillar 1",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results." +
                        "This should be followed by a COMPLETE event");
        ResultingAuditTrails result = createTestResultingAuditTrails(PILLAR1_ID);
        GetAuditTrailsFinalResponse resultResponse =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR1_ID, pillar1DestinationId, result);
        messageBus.sendMessage(resultResponse);
        AuditTrailResult resultEvent = (AuditTrailResult) testEventHandler.waitForEvent();
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, resultEvent.getEventType());
        Assertions.assertEquals(result, resultEvent.getAuditTrailEvents());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPLETE,
                testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag("regressiontest")
    public void negativeGetAuditTrailsResponseTest() throws InterruptedException {
        addDescription("Verify that the GetAuditTrail client works correct when receiving a negative " +
                "GetAuditTrails response from one contributors.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler();
        AuditTrailClient client = createAuditTrailClient();

        addStep("Retrieve all audit trails from the collection by calling with a null componentQueries array",
                "This should be interpreted as a request for all audit trails from all the collection settings " +
                        "defined contributors.");
        client.getAuditTrails(collectionID, null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionReceiver.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());

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

        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE,
                testEventHandler.waitForEvent().getEventType());

        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());
        GetAuditTrailsRequest requestPillar1 = pillar1Receiver.waitForMessage(GetAuditTrailsRequest.class);
        Assertions.assertNotNull(requestPillar1);
        GetAuditTrailsRequest requestPillar2 = pillar2Receiver.waitForMessage(GetAuditTrailsRequest.class);
        Assertions.assertNotNull(requestPillar2);

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
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_FAILED,
                testEventHandler.waitForEvent().getEventType());

        addStep("Send a final response from pillar 2",
                "A COMPONENT_COMPLETE event should be generated with the audit trail results." +
                        "This should be followed by a COMPLETE event");
        ResultingAuditTrails result2 = createTestResultingAuditTrails(PILLAR2_ID);
        GetAuditTrailsFinalResponse resultPillar2 =
                testMessageFactory.createGetAuditTrailsFinalResponse(requestPillar1,
                        PILLAR2_ID, pillar2DestinationId, result2);
        messageBus.sendMessage(resultPillar2);

        AuditTrailResult result2Event = (AuditTrailResult) testEventHandler.waitForEvent();
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_COMPLETE, result2Event.getEventType());
        Assertions.assertEquals(result2, result2Event.getAuditTrailEvents());
        Assertions.assertEquals(OperationEvent.OperationEventType.FAILED,
                testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag("regressiontest")
    public void progressEventsTest() throws InterruptedException {
        addDescription("Tests that progress events are handled correctly.");

        addStep("Create a AuditTrailClient.", "");
        TestEventHandler testEventHandler = new TestEventHandler();
        AuditTrailClient client = createAuditTrailClient();

        addStep("Retrieve all audit trails from the collection by calling with a null componentQueries array",
                "This should be interpreted as a request for all audit trails from all the collection settings " +
                        "defined contributors.");
        client.getAuditTrails(collectionID, null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionReceiver.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());

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

        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE,
                testEventHandler.waitForEvent().getEventType());

        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());
        GetAuditTrailsRequest requestPillar1 = pillar1Receiver.waitForMessage(GetAuditTrailsRequest.class);
        GetAuditTrailsRequest requestPillar2 = pillar2Receiver.waitForMessage(GetAuditTrailsRequest.class);

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
        Assertions.assertEquals(OperationEvent.OperationEventType.PROGRESS,
                testEventHandler.waitForEvent().getEventType());

        addStep("Send a general progress response from pillar 2",
                "A PROGRESS event should be generated with the audit trail results.");
        GetAuditTrailsProgressResponse progressResponse2 =
                testMessageFactory.createGetAuditTrailsProgressResponse(requestPillar2,
                        PILLAR2_ID, pillar2DestinationId);
        ResponseInfo progressInfo2 = new ResponseInfo();
        progressInfo2.setResponseText("Still progressing");
        progressInfo2.setResponseCode(ResponseCode.OPERATION_PROGRESS);
        progressResponse2.setResponseInfo(progressInfo2);
        messageBus.sendMessage(progressResponse2);
        Assertions.assertEquals(OperationEvent.OperationEventType.PROGRESS,
                testEventHandler.waitForEvent().getEventType());
    }

    @Test
    @Tag("regressiontest")
    public void incompleteSetOfFinalResponsesTest() throws Exception {
        addDescription("Verify that the GetAuditTrail client works correct without receiving responses from all " +
                "contributors.");
        addStep("Configure 500 ms second timeout for the operation itself. " +
                "The default 2 contributors collection is used", "");

        settingsForCUT.getRepositorySettings().getClientSettings().setOperationTimeoutDuration(datatypeFactory.newDuration(500));
        TestEventHandler testEventHandler = new TestEventHandler();
        AuditTrailClient client = createAuditTrailClient();

        client.getAuditTrails(collectionID, null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionReceiver.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());

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

        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE,
                testEventHandler.waitForEvent().getEventType());

        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());
        GetAuditTrailsRequest requestPillar1 = pillar1Receiver.waitForMessage(GetAuditTrailsRequest.class);
        Assertions.assertNotNull(requestPillar1);
    }

    @Test
    @Tag("regressiontest")
    public void noFinalResponsesTest() throws Exception {
        addDescription("Tests the the AuditTrailClient handles lack of Final Responses gracefully  ");
        addStep("Set a 100 ms timeout for the operation.", "");

        settingsForCUT.getRepositorySettings().getClientSettings()
                .setOperationTimeoutDuration(datatypeFactory.newDuration(100));
        AuditTrailClient client = createAuditTrailClient();

        addStep("Make the client ask for all audit trails.",
                "It should send a identify message");
        TestEventHandler testEventHandler = new TestEventHandler();
        client.getAuditTrails(collectionID, null, null, null, testEventHandler, null);
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest =
                collectionReceiver.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());

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

        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.COMPONENT_IDENTIFIED,
                testEventHandler.waitForEvent().getEventType());
        Assertions.assertEquals(OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE,
                testEventHandler.waitForEvent().getEventType());

        Assertions.assertEquals(OperationEvent.OperationEventType.REQUEST_SENT,
                testEventHandler.waitForEvent().getEventType());

        addStep("Wait for 1 second", "An failed event should be received");
        Assertions.assertEquals(OperationEvent.OperationEventType.FAILED,
                testEventHandler.waitForEvent().getEventType());
    }

    /**
     * Creates a new test AuditTrailClient based on the supplied settings.
     * <p>
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     *
     * @return A new AuditTrailClient(Wrapper).
     */
    private AuditTrailClient createAuditTrailClient() {
        return new AuditTrailClientTestWrapper(new ConversationBasedAuditTrailClient(
                settingsForCUT, conversationMediator, messageBus, settingsForTestClient.getComponentID()));
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

    @Override
    protected String getComponentID() {
        return "AuditTrailClientUnderTest";
    }

    @Override
    protected MessageResponse createIdentifyResponse(
            MessageRequest identifyRequest, String from, String to) {
        return testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(
                (IdentifyContributorsForGetAuditTrailsRequest) identifyRequest, from, to);
    }

    @Override
    protected MessageResponse createFinalResponse(MessageRequest request, String from, String to) {
        return testMessageFactory.createGetAuditTrailsFinalResponse(
                (GetAuditTrailsRequest) request, from, to, null);
    }

    @Override
    protected MessageRequest waitForIdentifyRequest() {
        return collectionReceiver.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);
    }

    @Override
    protected MessageRequest waitForRequest(MessageReceiver receiver) {
        return receiver.waitForMessage(GetAuditTrailsRequest.class);
    }

    @Override
    protected void checkNoRequestIsReceived(MessageReceiver receiver) {
        receiver.checkNoMessageIsReceived(GetAuditTrailsRequest.class);
    }

    @Override
    protected void startOperation(TestEventHandler testEventHandler) {
        AuditTrailClient getAuditTrailClient = createAuditTrailClient();
        getAuditTrailClient.getAuditTrails(collectionID, null, null, null, testEventHandler, null);
    }
}
