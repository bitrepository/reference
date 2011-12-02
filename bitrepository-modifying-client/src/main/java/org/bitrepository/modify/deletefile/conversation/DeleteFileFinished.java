package org.bitrepository.modify.deletefile.conversation;

import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFileFinished extends DeleteFileState {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructor.
     * @param conversation The conversation in this state.
     */
    protected DeleteFileFinished(SimpleDeleteFileConversation conversation) {
        super(conversation);
    }
    
    @Override
    public void onMessage(IdentifyPillarsForDeleteFileResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " 
                + "Received IdentifyPillarsForPutFileResponse from '" + response.getPillarID() 
                + "' after the PutFile has ended.");
    }

    @Override
    public void onMessage(DeleteFileProgressResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " 
                + "Received PutFileProgressResponse from '" + response.getPillarID() 
                + "' after the PutFile has ended.");
    }

    @Override
    public void onMessage(DeleteFileFinalResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " 
                + "Received PutFileFinalResponse from '" + response.getPillarID() 
                + "' after the PutFile has ended.");
    }

    @Override
    public void start() {
        // do nothing.
    }

    @Override
    public boolean hasEnded() {
        return true;
    }
    
}
