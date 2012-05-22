package org.bitrepository.pillar.checksumpillar;

import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.MockAlarmDispatcher;
import org.bitrepository.pillar.MockAuditManager;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumStore;
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMediator;
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMessageHandler;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.exception.RequestHandlerException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChecksumPillarMessageHandlerTest extends DefaultFixturePillarTest {
    MemoryCache cache;
    ChecksumPillarMediator mediator;
    MockAlarmDispatcher alarmDispatcher;
    MockAuditManager audits;
    PillarContext context;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseDeleteFileTests() throws Exception {
        cache = new MemoryCache();
        audits = new MockAuditManager();
        ContributorContext contributorContext = new ContributorContext(messageBus, settings, 
                settings.getReferenceSettings().getPillarSettings().getPillarID(), 
                settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        alarmDispatcher = new MockAlarmDispatcher(contributorContext);
        context = new PillarContext(settings, messageBus, alarmDispatcher, audits);
    }    

    @Test( groups = {"regressiontest", "pillartest"})
    public void invalidChecksumCase() throws Exception {
        addDescription("Tests that the ChecksumPillar does not start with an invalid checksum.");
        addStep("Setup", "Should create a invalid checksum.");
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationSalt(
                "1234cccc4321");

        try {
            new MockChecksumMessageHandler(context, cache);
            Assert.fail("Should throw an IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private class MockChecksumMessageHandler extends ChecksumPillarMessageHandler<MessageResponse> {

        protected MockChecksumMessageHandler(PillarContext context, ChecksumStore refCache) {
            super(context, refCache);
        }

        @Override
        public Class<MessageResponse> getRequestClass() {
            return MessageResponse.class;
        }

        @Override
        public void processRequest(MessageResponse request) throws RequestHandlerException {}

        @Override
        public MessageResponse generateFailedResponse(MessageResponse request) {
            return null;
        }
    }
}
