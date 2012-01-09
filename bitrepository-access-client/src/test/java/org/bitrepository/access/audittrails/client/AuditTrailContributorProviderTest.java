package org.bitrepository.access.audittrails.client;

import java.io.File;
import java.math.BigInteger;

import org.bitrepository.access.audittrails.ComponentDestination;
import org.bitrepository.access.audittrails.TestGetAuditTrailsMessageFactory;
import org.bitrepository.access.audittrails.client.AuditTrailContributorProvider;
import org.bitrepository.access.getfile.TestGetFileMessageFactory;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AuditTrailContributorProviderTest extends DefaultFixtureClientTest {
    protected TestGetAuditTrailsMessageFactory testMessageFactory;

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        testMessageFactory = new TestGetAuditTrailsMessageFactory(settings.getCollectionID());
    }

    @Test(groups = {"testfirst"})
    public void getAvailableContributorsTest() throws Exception {
        addDescription("Tests that the AuditTrailContributorProvider can lookup the audit trail contributors when all " +
                "the defined contributor reply.");
        addStep("Define two pillar and one integrity service audit trail contributor and create a " +
                "AuditTrailContributorProvider based on this.", "");

        String contributor1 = "PILLAR1";
        String contributor2 = "PILLAR2";
        String contributor3 = "INTEGRITY-SERVICE";
        settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().clear();
        settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().add(contributor1);
        settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().add(contributor2);
        settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().add(contributor3);

        AuditTrailContributorProvider client = new AuditTrailContributorProvider(settings, messageBus);
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);

        addStep("Call the 'getAvailableContributors()' method", 
                "The operation should block and a 'IdentifyContributersForAuditTrailsRequest' should be sent.");

        addStep("Make the GetClient ask for fastest pillar.", 
                "It should send message to identify which pillars and a IdentifyPillarsRequestSent notification should be generated.");

        GetAvailableContributorsCaller caller = new GetAvailableContributorsCaller(settings, messageBus);
        Thread t = new Thread(caller);
        t.start();
        
        IdentifyContributorsForGetAuditTrailsRequest receivedIdentifyRequestMessage = null;

        receivedIdentifyRequestMessage = 
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);

        //        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);

        addStep("Three pillars send responses. First an average timeToDeliver, then a fast timeToDeliver and last a" +
                " slow timeToDeliver.", "The client should send a getFileRequest to the fast pillar. " +
                        "The event handler should receive the following events: " +
                "3 x PillarIdentified, a PillarSelected and a RequestSent");

        IdentifyContributorsForGetAuditTrailsResponse contributor1Response = 
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(
                        receivedIdentifyRequestMessage, contributor1, pillar1DestinationId);
        messageBus.sendMessage(contributor1Response);
        //
        //        IdentifyPillarsForGetFileResponse fastReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
        //                receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
        //        TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
        //        fastTime.setTimeMeasureUnit(TimeMeasureTYPE.TimeMeasureUnit.MILLISECONDS);
        //        fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
        //        fastReply.setTimeToDeliver(fastTime);
        //        messageBus.sendMessage(fastReply);
        //
        //        IdentifyPillarsForGetFileResponse slowReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
        //                receivedIdentifyRequestMessage, slowPillarID, pillar2DestinationId);
        //        TimeMeasureTYPE slowTime = new TimeMeasureTYPE();
        //        slowTime.setTimeMeasureValue(BigInteger.valueOf(1L));
        //        slowTime.setTimeMeasureUnit(TimeMeasureTYPE.TimeMeasureUnit.HOURS);
        //        slowReply.setTimeToDeliver(slowTime);
        //        messageBus.sendMessage(slowReply);

    }

    private class GetAvailableContributorsCaller implements Runnable {
        private ComponentDestination[] result;
        private AuditTrailContributorProvider provider;

        public GetAvailableContributorsCaller(Settings settings, MessageSender messageSender) {
            AuditTrailContributorProvider provider = new AuditTrailContributorProvider(settings, messageBus);
        }

        public void run() {
            result = provider.getAvailableContributors();
        }
    }
}
