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
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.client.conversation.selector.ComponentSelector;

/**
 * Handles the general identifying state functionality. For common usage this class handles all messages by using
 * the sub classe defined <code>selector</code>.
 *
 * This class also has a default implenentation for the <code>handleStateTimeout()</code>, <code>getNextState()</code>
 * and <code>getTimeout()</code> operations.
 * <code>getNextState()</code>
 * more specialized
 */
public abstract class IdentifyingState extends GeneralConversationState {
    @Override
    protected void processMessage(MessageResponse msg) throws UnexpectedResponseException, UnableToFinishException {
        if (!msg.getResponseInfo().getResponseCode().equals(ResponseCode.IDENTIFICATION_POSITIVE)    ) {
            getContext().getMonitor().contributorFailed(
                    "Received negative response from component " + msg.getFrom() +
                    ":  " + msg.getResponseInfo(), msg.getFrom(), msg.getResponseInfo().getResponseCode());
        } else {
            getContext().getMonitor().contributorIdentified(msg);
    }
        getSelector().processResponse(msg);
    }

    @Override
    protected GeneralConversationState handleStateTimeout() {
        if (getContext().getState() == this) {
            if (handleIdentificationTimeout()) {
                getContext().getMonitor().identifyContributorsTimeout(
                        "Time has run out for looking up contributers." +
                        " Using contributers based on uncomplete set of responses.",
                        getSelector().getOutstandingComponents());
                getContext().getMonitor().contributorSelected("Identified contributers",
                        getSelector().getContributersAsString());
                return getOperationState();
            } else {
                getContext().getMonitor().identifyContributorsTimeout(
                        "Time has run out for looking up contributers, unable to continue.",
                        getSelector().getOutstandingComponents());
                getContext().getMonitor().operationFailed(
                        "Unable to continue with operation, missing contributors");
                return new FinishedState(getContext());
            }
        } else {
            getContext().getMonitor().warning("Identification timeout, but " +
                    "the conversation state has already changed to " + getContext().getState());
            return new FinishedState(getContext());
        }
    }

    @Override
    protected GeneralConversationState getNextState() throws UnableToFinishException {
        if (getSelector().isFinished()) {
            getContext().getMonitor().contributorSelected(
                    "Identified contributors", getSelector().getContributersAsString());
            return getOperationState();
        } else {
            return this;
        }
    }

    @Override
    protected long getTimeout() {
        return getContext().getSettings().getCollectionSettings().getClientSettings().getIdentificationTimeout()
                .longValue();
    }

    /**
     * @return The concrete selector from the implementing subclass.
     */
    public abstract ComponentSelector getSelector();

    /**
     * @return The performing state object to be used after the identification is finished.
     */
    public abstract GeneralConversationState getOperationState();

    /**
     * Can be used by subclass the subclass to finish the identification. If the identification is incomplete,
     * a informative IDENTIFICATION_FAILED should be generate.
     *
     * The default implementation is just to return true if a components has been selected by the selector, else false.
     *
     * @return Whether the conversation should continue to the operation phase or be considered failed. Only
     */
    protected boolean handleIdentificationTimeout() {
        return getSelector().hasSelectedComponent();
    }
}
