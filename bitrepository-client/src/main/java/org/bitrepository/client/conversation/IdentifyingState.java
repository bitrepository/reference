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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;

/**
 * Handles the general identifying state functionality. For common usage this class handles all messages by using
 * the sub classe defined <code>selector</code>.
 *
 * This class also has a default implementation for the <code>handleStateTimeout()</code>, <code>getNextState()</code>
 * and <code>getTimeoutValue()</code> operations.
 * <code>getNextState()</code>
 * more specialized
 */
public abstract class IdentifyingState extends GeneralConversationState {
    private ComponentSelector selector = new ComponentSelector();

    protected IdentifyingState(Collection<String> expectedContributors) {
        super(expectedContributors);
    }

    @Override
    protected final void processMessage(MessageResponse msg) throws UnexpectedResponseException, UnableToFinishException {
        if (msg.getResponseInfo().getResponseCode().equals(ResponseCode.IDENTIFICATION_POSITIVE)) {
            getContext().getMonitor().contributorIdentified(msg);
            getSelector().selectComponent(msg);
            checkForChecksumPillar(msg);
        } else if (msg.getResponseInfo().getResponseCode().equals(ResponseCode.REQUEST_NOT_SUPPORTED)) {
            getContext().getMonitor().debug(
                    "Response received indicating that the operation is not supported for pillar " + msg.getFrom());
        } else {
            handleFailureResponse(msg);
        }
    }

    @Override
    protected void logStateTimeout() {
        getContext().getMonitor().identifyContributorsTimeout(getOutstandingComponents());
    }

    @Override
    protected final GeneralConversationState completeState() throws UnableToFinishException {
        if (canFinish()) {
            generateContributorsSelectedEvent(getSelector().getSelectedComponents());
            return getOperationState();
        } else {
            throw new UnableToFinishException("Unable to continue operation, contributors unavailable.");
        }
    }

    @Override
    protected long getTimeoutValue() {
        return getContext().getSettings().getCollectionSettings().getClientSettings().getIdentificationTimeout()
                .longValue();
    }

    /**
     * @return The concrete selector from the implementing subclass.
     */
    protected final ComponentSelector getSelector() {
        return selector;
    }

    /**
     * @return The concrete selector from the implementing subclass.
     */
    protected void setSelector(ComponentSelector customSelector) {
        selector = customSelector;
    }

    /**
     * @return The performing state object to be used after the identification is finished.
     */
    protected abstract GeneralConversationState getOperationState() throws UnableToFinishException;

    /**
     * Implements the default handling of failure responses. May be overridden by operation specific behaviour,
     * idempotent behaviour f.ex,.
     */
    protected void handleFailureResponse(MessageResponse msg) throws UnableToFinishException {
        getContext().getMonitor().contributorFailed(
                msg.getResponseInfo().getResponseText(), msg.getFrom(), msg.getResponseInfo().getResponseCode());
    }

    private void generateContributorsSelectedEvent(Collection<SelectedComponentInfo> selectedComponentInfo) {
        List<String> selectedComponentIDs = new LinkedList<String>();
        for (SelectedComponentInfo componentInfo:selectedComponentInfo) {
            selectedComponentIDs.add(componentInfo.getID());
        }
        getContext().getMonitor().contributorsSelected(selectedComponentIDs);
    }

    /**
     * Indicates whether the identification state can finish, eg. can continue to the operation phase.
     * The default implementation is to return true if at least one a contributor has identified.
     * has been selected. May be overridden by concrete classes.
     */
    protected boolean canFinish() {
        return !getSelector().getSelectedComponents().isEmpty();
    }

    /**
     * Can be used by some operation to mark the responding pillar as checksum pillar if this is relevant for the
     * operation. The default implementation is to do nothing
     */
    protected void checkForChecksumPillar(MessageResponse msg) {

    }
}
