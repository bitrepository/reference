package org.bitrepository.modify.deletefile.selector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.conversation.selector.SelectedPillarInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;

public abstract class DeleteFileSelector implements ComponentSelector {
    
    /** Used for tracking who has answered. */
    protected ContributorResponseStatus responseStatus;
    protected final List<SelectedPillarInfo> selectedComponents = new LinkedList<SelectedPillarInfo>();

    /**
     * Method to determine if a pillar should be chosen for deleting the file. 
     */
    abstract protected boolean checkPillarResponseForSelection(IdentifyPillarsForDeleteFileResponse response) 
            throws UnexpectedResponseException;

    
    /**
     * Method for processing a IdentifyPillarsForDeleteFileResponse. Checks whether the response is from the
     * expected pillar.
     *
     * Consider overriding this in subclasses to perform type cheking og the message before delegating back to this
     * method (calling <code>super.processResponse(response)</code>).
     *
     * @param response The response identifying a pillar for the DeleteFile operation.
     */
    @Override
    public void processResponse(MessageResponse response) throws UnexpectedResponseException {
        if (response instanceof IdentifyPillarsForDeleteFileResponse) {
            IdentifyPillarsForDeleteFileResponse resp = (IdentifyPillarsForDeleteFileResponse) response;
            responseStatus.responseReceived(resp.getFrom());
            if (checkPillarResponseForSelection(resp)) {
                selectedComponents.add(new SelectedPillarInfo(resp.getPillarID(), response.getReplyTo()));
            }
        } else {
            throw new UnexpectedResponseException("Are currently only expecting IdentifyPillarsForGetFileResponse's");
        } 
    }

    @Override
    public boolean isFinished() throws UnableToFinishException {
        if (responseStatus.haveAllPillarResponded()) {
            if (!selectedComponents.isEmpty()) {
                return true;
            } else {
                throw new UnableToFinishException(
                        "All expected components have answered, but none where suitable");
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean hasSelectedComponent() {
        return !selectedComponents.isEmpty();
    }

    /**
     * Method for identifying the components, which needs to be identified for this operation to be finished.
     * @return An array of the IDs of the components which have not yet responded.
     */
    @Override
    public List<String> getOutstandingComponents() {
        return Arrays.asList(responseStatus.getOutstandPillars());
    }

    /**
     * @return The selected pillars.
     */
    public List<SelectedPillarInfo> getSelectedComponents() {
        return selectedComponents;
    }

    @Override
    public String getContributersAsString() {
        return getSelectedComponents().toString();
    }
        
}
