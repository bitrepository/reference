package org.bitrepository.access.getstatus.conversation;

import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;

public class GetStatusFinished extends GetStatusState {

    /**
     * Constructor 
     */
    public GetStatusFinished(SimpleGetStatusConversation conversation) {
        super(conversation);
    }

    @Override
    public void onMessage(IdentifyContributorsForGetStatusResponse response) {
        monitor.outOfSequenceMessage("Received " + response.getClass().getName() + " from " + response.getContributor() 
                + " after the conversation has ended.");
    }

    @Override
    public void onMessage(GetStatusProgressResponse response) {
        monitor.outOfSequenceMessage("Received " + response.getClass().getName() + " from " + response.getContributor() 
                + " after the conversation has ended.");
    }

    @Override
    public void onMessage(GetStatusFinalResponse response) {
        monitor.outOfSequenceMessage("Received " + response.getClass().getName() + " from " + response.getContributor() 
                + " after the conversation has ended.");
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
