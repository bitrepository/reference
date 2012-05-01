package org.bitrepository.access.getfile.selectors;

import java.util.Arrays;
import java.util.List;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;

public abstract class GetFileSelector implements ComponentSelector {
    /** Used for tracking who has answered. */
    ContributorResponseStatus responseStatus;
    SelectedPillarForGetFileInfo selectedPillar = null; 
 
    
    @Override
    public void processResponse(MessageResponse response) throws UnexpectedResponseException {
        if (response instanceof IdentifyPillarsForGetFileResponse) {
            IdentifyPillarsForGetFileResponse resp = (IdentifyPillarsForGetFileResponse) response;
            responseStatus.responseReceived(resp.getFrom());
            if (checkPillarResponseForSelection(resp)) {
                selectedPillar = new SelectedPillarForGetFileInfo(
                        resp.getPillarID(), response.getReplyTo(), resp.getTimeToDeliver());
            }
        } else {
            throw new UnexpectedResponseException("Are currently only expecting IdentifyPillarsForGetFileResponse's");
        } 
    }
    
    abstract protected boolean checkPillarResponseForSelection(IdentifyPillarsForGetFileResponse response) 
            throws UnexpectedResponseException;
    
    @Override
    public List<String> getOutstandingComponents() {
        return Arrays.asList(responseStatus.getOutstandPillars());
    }
    
    public SelectedPillarForGetFileInfo getSelectedComponent() {
        return selectedPillar;
    }
    
    @Override
    public String getContributersAsString() {
        return selectedPillar.getID();
    }
    
    @Override
    public boolean hasSelectedComponent() {
        return selectedPillar != null;
    }
    
    @Override
    public boolean isFinished() throws UnableToFinishException {
        if (responseStatus.haveAllPillarResponded()) {
            if (selectedPillar != null) {
                return true;
            } else {
                throw new UnableToFinishException("All expected components have answered, but none where suitable");
            }
        } else {
            return false;
        }
    }
    
}
