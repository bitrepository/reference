package org.bitrepository.access.getstatus;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.bitrepository.access.MessageSenderStub;
import org.bitrepository.access.getstatus.conversation.GetStatusFinished;
import org.bitrepository.access.getstatus.conversation.GettingStatus;
import org.bitrepository.access.getstatus.conversation.IdentifyingContributorsForGetStatus;
import org.bitrepository.access.getstatus.conversation.SimpleGetStatusConversation;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleGetStatusConversationTest extends IntegrationTest {

    private static final String CONTRIBUTOR_ID = "pillar1";
    
    private SimpleGetStatusConversation conversation;
    private MessageSenderStub messageSender = new MessageSenderStub(testEventManager);
    private List<String> contributors = new ArrayList<String>();
    private TestEventHandler eventHandler = new TestEventHandler(testEventManager);
    private TestGetStatusMessageFactory messageFactory;
    
    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        messageFactory = new TestGetStatusMessageFactory(settings.getCollectionID());
        contributors.clear();
        contributors.add(CONTRIBUTOR_ID);
        settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(new BigInteger("2000"));
        settings.getCollectionSettings().getClientSettings().setOperationTimeout(new BigInteger("2000"));
        conversation = new SimpleGetStatusConversation(messageSender, settings, contributors, eventHandler,
                new FlowController(settings));
    }
    
    @AfterMethod(alwaysRun = true)
    public void afterTest() {
        conversation = null;
        messageSender.clearMessages();
    }
    
    @Test(groups = {"regressiontest"})
    public void verifySunshine() {
        addDescription("Test that getStatusClient works as expected when everything goes good.");
        addStep("Start the conversation", "The conversation state is IdentifyingContributorsForGetStatus, " +
        		"and an IdentifyContributorsForGetStatusRequest is sent");
        conversation.startConversation();
        
        try {
            OperationEvent event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.IDENTIFY_REQUEST_SENT);
            Assert.assertEquals(conversation.getConversationState().getClass(), IdentifyingContributorsForGetStatus.class);
            Message identifyRequest = messageSender.waitForMessage();
            if(identifyRequest != null) {
                Assert.assertEquals(identifyRequest.getClass(), IdentifyContributorsForGetStatusRequest.class);
            } else {
                Assert.fail("No identify request was sent");
            }
            addStep("Send an IdentifyContributorsForGetStatusResponse", "The conversation changes state to GettingStatus," +
            		"and sends the actual GetStuatusRequest.");
            IdentifyContributorsForGetStatusResponse identifyResponse = 
                    messageFactory.createIdentifyContributorsForGetStatusResponse(
                        (IdentifyContributorsForGetStatusRequest) identifyRequest);
            identifyResponse.setContributor(CONTRIBUTOR_ID);
            conversation.onMessage(identifyResponse);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.COMPONENT_IDENTIFIED);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.IDENTIFICATION_COMPLETE);
            Message getRequest = messageSender.waitForMessage();
            if(getRequest != null) {
                Assert.assertEquals(getRequest.getClass(), GetStatusRequest.class);
            } else {
                Assert.fail("No get status request was sent");
            }
            Assert.assertEquals(conversation.getConversationState().getClass(), GettingStatus.class);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.REQUEST_SENT);
            
            addStep("Send an GetStatusFinalResponse", "The conversation changes state to GetStatusFinished");
            GetStatusFinalResponse response = messageFactory.createGetStatusFinalResponse((GetStatusRequest) getRequest);
            conversation.onMessage(response);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.COMPONENT_COMPLETE);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.COMPLETE);
            Assert.assertEquals(conversation.getConversationState().getClass(), GetStatusFinished.class);
            
        } catch (InterruptedException e) {
            Assert.fail("Got interrupted (InterruptedException)");
        }

    }
    
    @Test(groups = {"regressiontest"})
    public void verifyIdentifyTimeout() {
        addDescription("Test that the identification timeout mekanism of the getStatusClient works");
        addStep("Start conversation and wait for timeout", "Timeout event is emmitted");
        conversation.startConversation();
        
        try {
            OperationEvent event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.IDENTIFY_REQUEST_SENT);
            Assert.assertEquals(IdentifyingContributorsForGetStatus.class, conversation.getConversationState().getClass());
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.FAILED);
        } catch (InterruptedException e) {
            Assert.fail("Got interrupted (InterruptedException)");
        }
        
    }
    
    @Test(groups = {"regressiontest"})
    public void verifyGettingStatusTimeout() {
        addDescription("Test that the getting status timeout mekanism of the getStatusClient works");
        conversation.startConversation();
        
        try {
            OperationEvent event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.IDENTIFY_REQUEST_SENT);
            Assert.assertEquals(IdentifyingContributorsForGetStatus.class, conversation.getConversationState().getClass());
            
            Message identifyRequest = messageSender.waitForMessage();
            if(identifyRequest != null) {
                Assert.assertEquals(identifyRequest.getClass(), IdentifyContributorsForGetStatusRequest.class);
            } else {
                Assert.fail("No identify request was sent");
            }
            addStep("Send an IdentifyContributorsForGetStatusResponse", "The conversation changes state to GettingStatus," +
                    "and sends the actual GetStuatusRequest.");
            IdentifyContributorsForGetStatusResponse identifyResponse = 
                    messageFactory.createIdentifyContributorsForGetStatusResponse(
                        (IdentifyContributorsForGetStatusRequest) identifyRequest);
            identifyResponse.setContributor(CONTRIBUTOR_ID);
            conversation.onMessage(identifyResponse);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.COMPONENT_IDENTIFIED);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.IDENTIFICATION_COMPLETE);
            Message getRequest = messageSender.waitForMessage();
            if(getRequest != null) {
                Assert.assertEquals(getRequest.getClass(), GetStatusRequest.class);
            } else {
                Assert.fail("No get status request was sent");
            }
            Assert.assertEquals(conversation.getConversationState().getClass(), GettingStatus.class);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.REQUEST_SENT);
            event = eventHandler.waitForEvent();
            checkEvent(event, OperationEventType.FAILED);
            
        } catch (InterruptedException e) {
            Assert.fail("Got interrupted (InterruptedException)");
        }
        
        
    }
    
    private void checkEvent(OperationEvent event, OperationEvent.OperationEventType type) {
        if(event != null) {
            Assert.assertEquals(type, event.getType()); 
        } else {
            Assert.fail("Event null, expected" + type.toString());
        }
    }
}
