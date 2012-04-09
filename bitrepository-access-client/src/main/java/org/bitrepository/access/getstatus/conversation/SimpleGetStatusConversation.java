package org.bitrepository.access.getstatus.conversation;

import java.util.Collection;
import java.util.UUID;

import org.bitrepository.access.getstatus.selector.ContributorSelectorForGetStatus;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

public class SimpleGetStatusConversation extends AbstractConversation {

    /** The sender to use for dispatching messages */
    final MessageSender messageSender; 
    /** The configuration specific to the BitRepositoryCollection related to this conversion. */
    final Settings settings;
    
    /** Selects a pillar based on responses. */
    final ContributorSelectorForGetStatus selector;
    /** The conversation state (State pattern) */
    GetStatusState conversationState;
    
    public SimpleGetStatusConversation(MessageSender messageSender, Settings settings, Collection<String> contributors, 
            EventHandler eventHandler, FlowController flowController) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);
        this.messageSender = messageSender;
        this.settings = settings;     
        selector = new ContributorSelectorForGetStatus(contributors);
        conversationState = new IdentifyingContributorsForGetStatus(this);
    }

    @Override
    public synchronized void onMessage(GetStatusFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(GetStatusProgressResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(IdentifyContributorsForGetStatusResponse message) {
        conversationState.onMessage(message);
    }
    
    @Override
    public void endConversation() {
        conversationState.endConversation();       
    }

    @Override
    public boolean hasEnded() {
        return conversationState.hasEnded();
    }

    @Override
    public ConversationState getConversationState() {
        return conversationState;
    }

}
