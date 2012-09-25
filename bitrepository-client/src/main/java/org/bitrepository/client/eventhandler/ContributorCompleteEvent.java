package org.bitrepository.client.eventhandler;

/**
 * The parent class for all the concrete operation specific contributors complete events
 */
public class ContributorCompleteEvent extends ContributorEvent {
    public ContributorCompleteEvent(String contributorID) {
        super(contributorID);
        setType(OperationEventType.COMPONENT_COMPLETE);
    }
}
