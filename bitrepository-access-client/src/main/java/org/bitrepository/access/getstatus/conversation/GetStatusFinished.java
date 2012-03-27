package org.bitrepository.access.getstatus.conversation;

public class GetStatusFinished extends GetStatusState {

    /**
     * Constructor 
     */
    public GetStatusFinished(SimpleGetStatusConversation conversation) {
        super(conversation);
    }

    
    
    @Override
    public void start() {
        // Nothing to do, we are done..
        
    }

    @Override
    public boolean hasEnded() {
        return true;
    }

}
