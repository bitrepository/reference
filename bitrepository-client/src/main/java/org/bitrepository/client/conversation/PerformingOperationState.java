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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.common.utils.TimeUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Handles the booking of performing the request phase messaging. Only the specialized workflow steps are required to
 * be implemented the subclass.
 */
public abstract class PerformingOperationState extends GeneralConversationState {
    protected Map<String, String> activeContributors;

    /**
     * @param expectedContributors The components to perform the actual operation for. The operation is considered
     *                             complete when all contributors have responded.
     */
    protected PerformingOperationState(Collection<SelectedComponentInfo> expectedContributors) {
        super(toComponentIDs(expectedContributors));
        this.activeContributors = new HashMap<>();
        for (SelectedComponentInfo contributorInfo : expectedContributors) {
            activeContributors.put(contributorInfo.getID(), contributorInfo.getDestination());
        }
    }

    protected PerformingOperationState(String componentID) {
        super(List.of(componentID));
    }

    @Override
    protected boolean processMessage(MessageResponse msg) throws UnableToFinishException {
        boolean isFinalResponse = true;
        if (msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_ACCEPTED_PROGRESS) ||
                msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_PROGRESS)) {
            getContext().getMonitor().progress(msg.getResponseInfo().getResponseText(), msg.getFrom());
            isFinalResponse = false;
        } else {
            try {
                if (msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_COMPLETED)) {
                    generateContributorCompleteEvent(msg);
                } else {
                    isFinalResponse = handleFailureResponse(msg);
                }
            } catch (UnexpectedResponseException ure) {
                getContext().getMonitor().warning(ure.getMessage());
            }
        }
        return isFinalResponse;
    }

    @Override
    protected void logStateTimeout() throws UnableToFinishException {
        throw new UnableToFinishException("Failed to receive responses from all contributors before timeout (" +
                        TimeUtils.durationToHuman(getTimeoutValue()) +
                        "). Missing contributors " +
                        getOutstandingComponents());
    }

    @Override
    protected Duration getTimeoutValue() {
        return getContext().getSettings().getOperationTimeout();
    }

    @Override
    protected GeneralConversationState completeState() throws UnableToFinishException {
        if (getOutstandingComponents().isEmpty()) {
            getContext().getMonitor().complete();
            return new FinishedState(getContext());
        } else {
            getContext().getMonitor().timeoutRemainingContributors(getOutstandingComponents());
            throw new UnableToFinishException("All contributors haven't responded. Missing contributors " + getOutstandingComponents());
        }
    }

    /**
     * Delegating the generation of the COMPLETE_EVENT to the concrete state.
     *
     * @param msg The final response to process into result event.
     * @throws UnexpectedResponseException Unable to generate a result event based on the supplied message.
     */
    protected abstract void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException;

    private static Collection<String> toComponentIDs(Collection<SelectedComponentInfo> contributors) {
        Collection<String> componentIDs = new HashSet<>();
        for (SelectedComponentInfo componentInfo : contributors) {
            componentIDs.add(componentInfo.getID());
        }
        return componentIDs;
    }

    /**
     * Implements the default handling of failure responses which is to do nothing
     * (besides being registered in the event monitor, which is handled by the parent class).
     *
     * @param msg The failure message to handle
     * @return true
     * @throws UnableToFinishException if the operation is unable to finish
     */
    protected boolean handleFailureResponse(MessageResponse msg) throws UnableToFinishException {
        getContext().getMonitor()
                .contributorFailed(msg.getResponseInfo().getResponseText(), msg.getFrom(), msg.getResponseInfo().getResponseCode());
        return true;
    }
}