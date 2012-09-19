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
package org.bitrepository.client.conversation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;

/**
 * Handles the booking of performing the request phase messaging. Only the specialized workflow steps are required to
 * be implemented the subclass.
 */
public abstract class PerformingOperationState extends GeneralConversationState {
    protected Map<String,String> activeContributors;

    /**
     * @param expectedContributors The components to perform the actual operation for. The operation is considered
     *                             complete when all contributors have responded.
     */
    protected PerformingOperationState(Collection<SelectedComponentInfo> expectedContributors) {
        super(toComponentIDs(expectedContributors));
        this.activeContributors = new HashMap<String,String>();
        for (SelectedComponentInfo contributorInfo : expectedContributors) {
            activeContributors.put(contributorInfo.getID(), contributorInfo.getDestination());
        }
    }

    protected PerformingOperationState(String componentID) {
        super(Arrays.asList(componentID));
    }

    @Override
    protected void processMessage(MessageResponse msg) {
        if (msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_ACCEPTED_PROGRESS) ||
                msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_PROGRESS)) {
            getContext().getMonitor().progress(msg.getResponseInfo().getResponseText(), msg.getFrom());
        } else {
            try {
                if (msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_COMPLETED)) {
                    generateContributorCompleteEvent(msg);
                } else {
                    getContext().getMonitor().contributorFailed(
                            msg.getResponseInfo().getResponseText(), msg.getFrom(), msg.getResponseInfo().getResponseCode());
                }
            } catch(UnexpectedResponseException ure ) {
                getContext().getMonitor().warning(ure.getMessage());
            }
        }
    }

    @Override
    protected void logStateTimeout() throws UnableToFinishException {
        throw new UnableToFinishException("Failed to receive responses from all contributors before timeout(" +
                getTimeoutValue() + "ms)");
    }

    @Override
    protected long getTimeoutValue() {
        return getContext().getSettings().getCollectionSettings().getClientSettings().getOperationTimeout().longValue();
    }

    @Override
    protected GeneralConversationState completeState() throws UnableToFinishException {
        if (getOutstandingComponents().isEmpty()) {
            getContext().getMonitor().complete();
            return new FinishedState(getContext());
        } else {
            throw new UnableToFinishException("All contributors haven't responded");
        }
    }

    /**
     * Delegating the generation of the COMPLETE_EVENT to the concrete state.
     * @param msg The final response to process into result event.
     * @throws UnexpectedResponseException Unable to generate a result event based on the supplied message.
     */
    protected abstract void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException;

    private static Collection<String> toComponentIDs(Collection<SelectedComponentInfo> contributors) {
        Collection componentIDs = new HashSet();
        for (SelectedComponentInfo componentInfo: contributors) {
            componentIDs.add(componentInfo.getID());
        }
        return componentIDs;
    }

    protected boolean isChecksumPillar(String pillarID) {
        return getContext().getChecksumPillars().contains(pillarID);
    }
}