package org.bitrepository.access.getstatus.conversation;

import org.bitrepository.protocol.conversation.ConversationEventMonitor;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;

public abstract class GetStatusState extends AbstractMessageListener implements ConversationState {
    /** The conversation, where the state belong.*/
    protected final SimpleGetStatusConversation conversation;
    /** Handles the mediation of information regarding conversation updates */
    protected final ConversationEventMonitor monitor;
    
    /** 
     * The constructor for the indicated conversation.
     * @param conversation The related conversation containing context information.
     */
    public GetStatusState(SimpleGetStatusConversation conversation) {
        this.conversation = conversation;
        this.monitor = conversation.getMonitor();
    }

    /**
     * Mark this conversation as ended, and notifies whoever waits for it to end.
     */
    protected void endConversation() {
        conversation.conversationState = new GetStatusFinished(conversation);
    }
    
}
