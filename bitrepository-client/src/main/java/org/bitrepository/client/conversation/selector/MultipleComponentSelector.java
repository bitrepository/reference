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

import org.bitrepository.bitrepositoryelements.ResponseCode;
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
public class MultipleComponentSelector extends ComponentSelector {
    protected final List<SelectedComponentInfo> selectedComponents = new LinkedList<SelectedComponentInfo>();

    /**
     * @param componentsWhichShouldRespond The IDs of the components to be selected.
     */
    public MultipleComponentSelector(Collection<String> componentsWhichShouldRespond) {
        ArgumentValidator.checkNotNullOrEmpty(componentsWhichShouldRespond, "componentsWhichShouldRespond");
        responseStatus = new ContributorResponseStatus(componentsWhichShouldRespond);
    }

    /**
     * Method for processing a IdentifyPillarsForDeleteFileResponse. Checks whether the response is from the
     * expected component.
     *
     * Consider overriding this in subclasses to perform type cheking og the message before delegating back to this
     * method (calling <code>super.processResponse(response)</code>).
     *
     * @param response The response identifying a pillar for the DeleteFile operation.
     */
    public void processResponse(MessageResponse response) throws UnexpectedResponseException {
        String respondingComponentID = response.getFrom();
        responseStatus.responseReceived(respondingComponentID);
        if(response.getResponseInfo().getResponseCode().equals(ResponseCode.IDENTIFICATION_POSITIVE) &&
                responseStatus.getComponentsWhichShouldRespond().contains(respondingComponentID)) {
            selectedComponents.add(new SelectedComponentInfo(respondingComponentID, response.getReplyTo()));
        }
    }

    @Override
    public boolean isFinished() throws UnableToFinishException {
        if (responseStatus.haveAllComponentsResponded()) {
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

    /*
     * @return <code>true</code> if all the expected contributers have been selected.
     */
    public boolean haveSelectedAllComponents() {
        return selectedComponents.size() == responseStatus.getComponentsWhichShouldRespond().size();
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
