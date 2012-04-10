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
package org.bitrepository.protocol.conversation;

import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.protocol.pillarselector.ComponentSelector;

public abstract class IdentifyingState extends GeneralConversationState {
    @Override
    protected GeneralConversationState handleStateTimeout() {
        if (getContext().getState() == this) {
            if (getSelector().hasSelectedComponent()) {
                getContext().getMonitor().identifyPillarTimeout(
                        "Time has run out for looking up contributers. The following contributers " +
                                "didn't respond: " + getSelector().getOutstandingComponents() +
                                ". Using contributers based on uncomplete set of responses.");
                return getOperationState();
            } else {
                getContext().getMonitor().operationFailed(
                        "Unable to select any contributers, time has run out. " +
                                "The following contributers did't respond: " +
                                getSelector().getOutstandingComponents());
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
            getContext().getMonitor().pillarSelected("Identified contributors",
                    getSelector().getContributersAsString());
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

    public abstract ComponentSelector getSelector();

    public abstract GeneralConversationState getOperationState();
}
