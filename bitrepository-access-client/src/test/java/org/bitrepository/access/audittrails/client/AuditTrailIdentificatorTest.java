package org.bitrepository.access.audittrails.client;

import java.util.LinkedList;
import java.util.List;

import org.bitrepository.access.audittrails.ComponentDestination;
import org.bitrepository.access.audittrails.TestGetAuditTrailsMessageFactory;
import org.bitrepository.access.getfile.CollectionBasedGetFileClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfile.GetFileClientTestWrapper;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.jaccept.TestEventManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AuditTrailIdentificatorTest extends DefaultFixtureClientTest {
    protected TestGetAuditTrailsMessageFactory testMessageFactory;

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        testMessageFactory = new TestGetAuditTrailsMessageFactory(settings.getCollectionID());
    }

    @Test(groups = {"regressiontest"})
    public void getDefinedContributorsTest() throws Exception {
        addDescription("Tests that the AuditTrailContributorProvider returns the audit trail contributors defined in " +
        		"the settings.");
        addStep("Define two pillar and one integrity service audit trail contributor and create a " +
                "AuditTrailContributorProvider based on this.", "");

        String contributor1 = "PILLAR1";
        String contributor2 = "PILLAR2";
        String contributor3 = "INTEGRITY-SERVICE";
        settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().clear();
        settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().add(contributor1);
        settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().add(contributor2);
        settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().add(contributor3);
        
        AuditTrailIdentificator identificator = 
                new ConversationBasedAuditTrailIdentificator(settings, null, messageBus);
        
        List<String> expectedContributors = new LinkedList<String>();
        expectedContributors.add(contributor1);
        expectedContributors.add(contributor2);
        expectedContributors.add(contributor3);
        
        Assert.assertEquals(identificator.getDefinedContributors(), expectedContributors);
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

        addStep("Call the 'getAvailableContributors()' method", 
                "A 'IdentifyContributersForAuditTrailsRequest' should be sent.");

        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        AuditTrailIdentificator client = createAuditTrailIdentificator();
        client.getAvailableContributors(testEventHandler, null);
        
        IdentifyContributorsForGetAuditTrailsRequest receivedIdentifyRequestMessage = null;

        receivedIdentifyRequestMessage = 
                collectionDestination.waitForMessage(IdentifyContributorsForGetAuditTrailsRequest.class);

        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);

        addStep("Three components send responses.", "The event handler should receive the following events: " +
                "3 x COMPONENT_IDENTIFIED");

        IdentifyContributorsForGetAuditTrailsResponse contributor1Response = 
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(
                        receivedIdentifyRequestMessage, contributor1, pillar1DestinationId);
        messageBus.sendMessage(contributor1Response);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
        
        IdentifyContributorsForGetAuditTrailsResponse contributor2Response = 
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(
                        receivedIdentifyRequestMessage, contributor1, pillar1DestinationId);
        messageBus.sendMessage(contributor2Response);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
        
        IdentifyContributorsForGetAuditTrailsResponse contributor3Response = 
                testMessageFactory.createIdentifyContributorsForGetAuditTrailsResponse(
                        receivedIdentifyRequestMessage, contributor1, pillar1DestinationId);
        messageBus.sendMessage(contributor3Response);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
        
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
    }

//    Move this to future blocking test
//    private class GetAvailableContributorsCaller implements Runnable {
//        private ComponentDestination[] result;
//        private final AuditTrailContributorProvider provider;
//        private final TestEventManager testEventManager;
//
//        public GetAvailableContributorsCaller(Settings settings, MessageSender messageSender, TestEventManager testEventManager) {
//            this.testEventManager = testEventManager;
//            provider = new AuditTrailContributorProvider(settings, messageBus);
//        }
//
//        public void run() {
//            testEventManager.addStimuli("Calling " + provider + 
//                    " getAvailableContributors() getAvailableContributors()");
//            result = provider.getAvailableContributors();
//        }
//    }
    
    /**
     * Creates a new test GetFileClient based on the supplied settings. 
     * 
     * Note that the normal way of creating client through the module factory would reuse components with settings from
     * previous tests.
     * @return A new GetFileClient(Wrapper).
     */
    private AuditTrailIdentificator createAuditTrailIdentificator() {
        MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration());
        ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings);
        return new AuditTrailIdentificatorTestWrapper(new ConversationBasedAuditTrailIdentificator(
                settings, conversationMediator, messageBus)
        , testEventManager);
    }
}
