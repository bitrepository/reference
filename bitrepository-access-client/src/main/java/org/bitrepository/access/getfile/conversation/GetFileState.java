package org.bitrepository.access.getfile.conversation;

import java.util.Timer;

import org.bitrepository.protocol.messagebus.AbstractMessageListener;

public class GetFileState extends AbstractMessageListener {
    protected final SimpleGetFileConversation conversation;

    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The timer. Schedules conversation timeouts for this conversation. */
    final Timer timer = new Timer(TIMER_IS_DAEMON);


    public GetFileState(SimpleGetFileConversation conversation) {
        this.conversation = conversation;
    }

    /**
     * Mark this conversation as ended, and notifies whoever waits for it to end.
     */
    protected void endConversation() {
        conversation.conversationState = new GetFileFinished(conversation);
    }
}
