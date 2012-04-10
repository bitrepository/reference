package org.bitrepository.protocol.conversation;

import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.protocol.pillarselector.ComponentSelector;

public abstract class IdentifyingState extends GeneralConversationState {
    @Override
    protected GeneralConversationState handleStateTimeout() {
        if (getContext().getState() == this) {
            if (getSelector().getOutstandingComponents().size() != 0) {
                getContext().getMonitor().identifyPillarTimeout(
                        "Time has run out for looking up contributers. The following contributers " +
                                "didn't respond: " + getSelector().getOutstandingComponents() +
                                ". Using contributers based on uncomplete set of responses.");
                return getOperationState();
            } else {
                getContext().getMonitor().operationFailed(
                        "Unable to select a pillar, time has run out. " +
                                "The following pillars did't respond: " + getSelector().getOutstandingComponents());
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
        return getContext().getSettings().getCollectionSettings().getClientSettings().getIdentificationTimeout().longValue();
    }

    public abstract ComponentSelector getSelector();

    public abstract GeneralConversationState getOperationState();
}
