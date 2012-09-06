/*
 * #%L
 * Bitrepository Modifying Client
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.modify.deletefile.selector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;

public abstract class DeleteFileSelector implements ComponentSelector {
    
    /** Used for tracking who has answered. */
    protected ContributorResponseStatus responseStatus;
    protected final List<SelectedComponentInfo> selectedComponents = new LinkedList<SelectedComponentInfo>();

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
            if (checkPillarResponseForSelection(resp)
                && !resp.getResponseInfo().getResponseCode().equals(ResponseCode.FILE_NOT_FOUND_FAILURE)
               ) {
                selectedComponents.add(new SelectedComponentInfo(resp.getPillarID(), response.getReplyTo()));
            }
        } else {
            throw new UnexpectedResponseException("Are currently only expecting IdentifyPillarsForGetFileResponse's");
        } 
    }

    @Override
    public boolean isFinished() throws UnableToFinishException {
        return responseStatus.haveAllComponentsResponded();
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
        return Arrays.asList(responseStatus.getOutstandComponents());
    }

    /**
     * @return The selected pillars.
     */
    public List<SelectedComponentInfo> getSelectedComponents() {
        return selectedComponents;
    }

    @Override
    public String getContributersAsString() {
        return getSelectedComponents().toString();
    }
        
}
