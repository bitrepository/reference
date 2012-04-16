package org.bitrepository.access.getaudittrails.client;

import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.MultipleComponentSelector;

import java.util.Collection;

public class AuditTrailContributorSelector extends MultipleComponentSelector {
    /**
     * @param pillarsWhichShouldRespond The IDs of the pillars to be selected.
     */
    public AuditTrailContributorSelector(Collection<String> pillarsWhichShouldRespond) {
        super(pillarsWhichShouldRespond);
    }

    @Override
    public void processResponse(MessageResponse response) throws UnexpectedResponseException {
        if (response instanceof IdentifyContributorsForGetAuditTrailsResponse) {
            super.processResponse(response);
        } else {
            throw new UnexpectedResponseException("Are currently only expecting IdentifyContributorsForGetAuditTrailsResponse's");
        }
    }
}
