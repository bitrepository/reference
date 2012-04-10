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
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;

public abstract class PerformingOperationState extends GeneralConversationState {

    @Override
    protected GeneralConversationState getNextState() throws UnableToFinishException {
        if (getResponseStatus().haveAllPillarResponded()) {
            getContext().getMonitor().complete("Finished operation");
            return new FinishedState(getContext());
        } else {
            return this;
        }
    }

    @Override
    protected GeneralConversationState handleStateTimeout() {
        getContext().getMonitor().operationFailed(getName() + " operation timed out, " +
                "the following contributors didn't respond: " + getResponseStatus().getOutstandPillars());
        return new FinishedState(getContext());
    }

    @Override
    protected long getTimeout() {
        return getContext().getSettings().getCollectionSettings().getClientSettings().getOperationTimeout().longValue();
    }

    protected abstract PillarsResponseStatus getResponseStatus();
}