package org.bitrepository.modify.deletefile.conversation;

import org.bitrepository.modify.putfile.conversation.PutFileFinished;
import org.bitrepository.modify.putfile.conversation.SimplePutFileConversation;
import org.bitrepository.protocol.conversation.ConversationEventMonitor;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * The interface for states of the DeleteFile communication.
 */
public abstract class DeleteFileState extends AbstractMessageListener implements ConversationState {
    /** The conversation in the given state.*/
    protected final SimpleDeleteFileConversation conversation;
    /** Handles the mediation of information regarding conversation updates */
    protected final ConversationEventMonitor monitor;
    /** Used for sending messages */
    protected final MessageSender messageSender;
    
    /**
     * Constructor.
     * @param conversation The conversation in the given state.
     */
    protected DeleteFileState(SimpleDeleteFileConversation conversation) {
        this.conversation = conversation;
        this.monitor = conversation.getMonitor();
        this.messageSender = conversation.messageSender;
    }
    
    /**
     * Mark this conversation as ended, and notifies whoever waits for it to end.
     */
    protected void endConversation() {
        conversation.conversationState = new DeleteFileFinished(conversation);
    }

}
