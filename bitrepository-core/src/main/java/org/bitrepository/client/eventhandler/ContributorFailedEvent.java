package org.bitrepository.client.eventhandler;

import org.bitrepository.bitrepositoryelements.ResponseCode;

import static org.bitrepository.client.eventhandler.OperationEvent.OperationEventType.COMPONENT_FAILED;

public class ContributorFailedEvent extends ContributorEvent {
    private final ResponseCode responseCode;

    /**
     * @param responseCode The response code from any response indicating the failure. Might be null, if no relevant
     *                     response exists.
     */
    public ContributorFailedEvent(
            String info,
            String contributorID,
            ResponseCode responseCode,
            String conversationID) {
        super(COMPONENT_FAILED, info, contributorID, conversationID);
        this.responseCode = responseCode;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    @Override
    public String additionalInfo() {
        return super.additionalInfo() + "responseCode: " + getContributorID();
    }
}
