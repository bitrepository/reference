package org.bitrepository.pillar;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMediator;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.common.PillarMediator;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.contributor.handler.RequestHandler;
import org.bitrepository.service.exception.RequestHandlerException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MediatorTest extends DefaultFixturePillarTest {
    ChecksumPillarMediator mediator;
    MockAlarmDispatcher alarmDispatcher;
    MockAuditManager audits;
    PillarContext context;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseTest() throws Exception {
        audits = new MockAuditManager();
        ContributorContext contributorContext = new ContributorContext(messageBus, settings, 
                settings.getReferenceSettings().getPillarSettings().getPillarID(), 
                settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        alarmDispatcher = new MockAlarmDispatcher(contributorContext);
        context = new PillarContext(settings, messageBus, alarmDispatcher, audits);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testMediatorRuntimeExceptionHandling() throws Exception {
        addDescription("Tests the handling of a runtime exception");
        addStep("Setup create and start the mediator.", "");
        
        TestMediator mediator = new TestMediator(context);
        try {
            mediator.start();
            
            Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0);
            
            addStep("Send a request to the mediator.", "Should be caught.");
            IdentifyContributorsForGetStatusRequest request = new IdentifyContributorsForGetStatusRequest();
            request.setAuditTrailInformation("audit");
            request.setCollectionID(settings.getCollectionID());
            request.setCorrelationID(UUID.randomUUID().toString());
            request.setFrom(FROM);
            request.setMinVersion(BigInteger.valueOf(1L));
            request.setReplyTo(clientDestinationId);
            request.setTo(settings.getCollectionDestination());
            request.setVersion(BigInteger.valueOf(1L));
            messageBus.sendMessage(request);
            
            MessageResponse response = clientTopic.waitForMessage(IdentifyContributorsForGetStatusResponse.class);
            Assert.assertEquals(response.getResponseInfo().getResponseCode(), ResponseCode.FAILURE);
            Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1);
        } finally {
            mediator.close();
        }
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testMediatorInvalidCollectionID() throws Exception {
        addDescription("Tests the handling of an invalid collection id");
        addStep("Setup create and start the mediator.", "");
        String wrongCollectionID = "wrongCollectionID";
        
        TestMediator mediator = new TestMediator(context);
        try {
            mediator.testCollectionID(wrongCollectionID);
            Assert.fail("Should throw an " + IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    private class TestMediator extends PillarMediator {

        public TestMediator(PillarContext context) {
            super(context);
        }
        
        public void testCollectionID(String collectionID) {
            validateBitrepositoryCollectionId(collectionID);
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected RequestHandler[] createListOfHandlers() {
            List<RequestHandler> handlers = new ArrayList<RequestHandler>();
            handlers.add(new ErroneousRequestHandler());
            return handlers.toArray(new RequestHandler[handlers.size()]);
        }
    }
    
    private class ErroneousRequestHandler implements RequestHandler<IdentifyContributorsForGetStatusRequest> {

        @Override
        public Class<IdentifyContributorsForGetStatusRequest> getRequestClass() {
            return IdentifyContributorsForGetStatusRequest.class;
        }

        @Override
        public void processRequest(IdentifyContributorsForGetStatusRequest request) throws RequestHandlerException {
            throw new RuntimeException("I am supposed to throw a RuntimeException");
        }

        @Override
        public IdentifyContributorsForGetStatusResponse generateFailedResponse(IdentifyContributorsForGetStatusRequest request) {
            IdentifyContributorsForGetStatusResponse res = new IdentifyContributorsForGetStatusResponse();
            res.setCollectionID(request.getCollectionID());
            res.setContributor(request.getTo());
            res.setCorrelationID(request.getCorrelationID());
            res.setFrom(request.getTo());
            res.setMinVersion(request.getMinVersion());
            res.setReplyTo(request.getTo());
            res.setTo(request.getReplyTo());
            res.setVersion(request.getVersion());
            return res;
        }
    }
}
