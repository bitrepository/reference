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
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.protocol.utils.MessageUtils;

import java.util.Arrays;

/**
 * Handles the booking of performing the request phase messaging. Only the specialized workflow steps are required to
 * be implemented the subclass.
 */
public abstract class PerformingOperationState extends GeneralConversationState {

    @Override
    protected void processMessage(MessageResponse msg) {
        if (msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_ACCEPTED_PROGRESS) ||
                msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_PROGRESS)) {
            getContext().getMonitor().progress(msg.getResponseInfo().getResponseText(), msg.getFrom());
        } else {
            try {
                if (msg.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_COMPLETED)) {
                    getResponseStatus().responseReceived(msg.getFrom());
                    generateCompleteEvent(msg);
                } else if (MessageUtils.isIdentifyResponse(msg)) {
                    getContext().getMonitor().outOfSequenceMessage("Received identify response from " +
                            msg.getFrom() + " after identification was finished");
                } else {
                    getResponseStatus().responseReceived(msg.getFrom());
                    getContext().getMonitor().contributorFailed(
                            "Received negative response from component " + msg.getFrom() + ":  " +
                                    msg.getResponseInfo(), msg.getFrom(), msg.getResponseInfo().getResponseCode());
                }
            } catch(UnexpectedResponseException ure ) {
                getContext().getMonitor().warning(ure.getMessage());
            }
        }
    }

    @Override
    protected GeneralConversationState getNextState() throws UnableToFinishException {
        if (getResponseStatus().haveAllComponentsResponded()) {
            getContext().getMonitor().complete("Finished operation");
            return new FinishedState(getContext());
        } else {
            return this;
        }
    }

    @Override
    protected GeneralConversationState handleStateTimeout() {
        getContext().getMonitor().operationFailed(getName() + " operation timed out, " +
                "the following contributors didn't respond: " + Arrays.toString(getResponseStatus().getOutstandComponents()));
        return new FinishedState(getContext());
    }

    @Override
    protected long getTimeout() {
        return getContext().getSettings().getCollectionSettings().getClientSettings().getOperationTimeout().longValue();
    }

    /**
     * Delegating the generation of the COMPLETE_EVENT to the concrete state.
     * @param msg The final response to process into result event.
     * @throws UnexpectedResponseException Unable to generate a result event based on the supplied message.
     */
    protected abstract void generateCompleteEvent(MessageResponse msg) throws UnexpectedResponseException;

    /**
     * @return The concrete response status implemented by the subclass.
     */
    protected abstract ContributorResponseStatus getResponseStatus();
}