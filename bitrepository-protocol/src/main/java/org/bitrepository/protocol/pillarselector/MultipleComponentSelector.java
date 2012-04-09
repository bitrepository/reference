package org.bitrepository.protocol.pillarselector;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;

public class MultipleComponentSelector implements ComponentSelector {
    /** Used for tracking who has answered. */
    private final PillarsResponseStatus responseStatus;
    private final List<SelectedPillarInfo> selectedComponents = new LinkedList<SelectedPillarInfo>();

    /**
     * @param pillarsWhichShouldRespond The IDs of the pillars to be selected.
     */
    public MultipleComponentSelector(Collection<String> pillarsWhichShouldRespond) {
        ArgumentValidator.checkNotNullOrEmpty(pillarsWhichShouldRespond, "pillarsWhichShouldRespond");
        responseStatus = new PillarsResponseStatus(pillarsWhichShouldRespond);
    }

    /**
     * Method for processing a IdentifyPillarsForDeleteFileResponse. Checks whether the response is from the wanted
     * expected pillar.
     * @param response The response identifying a pillar for the DeleteFile operation.
     */
    public void processResponse(MessageResponse response) throws UnexpectedResponseException {
        responseStatus.responseReceived(response.getFrom());
        selectedComponents.add(new SelectedPillarInfo(response.getFrom(), response.getReplyTo()));
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
