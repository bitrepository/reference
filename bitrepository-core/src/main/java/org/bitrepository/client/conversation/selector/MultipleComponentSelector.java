/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.client.conversation.selector;

import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * The general selector for choosing multiple contributors during the identification phase of a operation.
 */
public class MultipleComponentSelector implements ComponentSelector {
    /** Used for tracking who has answered. */
    private final ContributorResponseStatus responseStatus;
    private final List<SelectedPillarInfo> selectedComponents = new LinkedList<SelectedPillarInfo>();

    /**
     * @param pillarsWhichShouldRespond The IDs of the pillars to be selected.
     */
    public MultipleComponentSelector(Collection<String> pillarsWhichShouldRespond) {
        ArgumentValidator.checkNotNullOrEmpty(pillarsWhichShouldRespond, "pillarsWhichShouldRespond");
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
    }

    /**
     * Method for processing a IdentifyPillarsForDeleteFileResponse. Checks whether the response is from the
     * expected pillar.
     *
     * Consider overriding this in subclasses to perform type cheking og the message before delegating back to this
     * method (calling <code>super.processResponse(response)</code>).
     *
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
