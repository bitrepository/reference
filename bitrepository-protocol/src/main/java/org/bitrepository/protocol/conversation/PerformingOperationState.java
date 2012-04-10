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