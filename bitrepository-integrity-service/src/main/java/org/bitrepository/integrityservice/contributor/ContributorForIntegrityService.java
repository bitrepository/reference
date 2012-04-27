package org.bitrepository.integrityservice.contributor;

import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.contributor.AbstractContributor;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetStatusRequestRequestHandler;
import org.bitrepository.service.contributor.handler.GetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.RequestHandler;

/**
 * Provides GetStatus and GetAuditTrails functionality.
 */
public class ContributorForIntegrityService extends AbstractContributor {
    private final ContributorContext context;
    public ContributorForIntegrityService(MessageBus messageBus, ContributorContext context) {
        super(messageBus);
        this.context = context;
    }

    @Override
    public RequestHandler[] createListOfHandlers() {
        return new RequestHandler[] {
            new IdentifyContributorsForGetStatusRequestRequestHandler(context),
            new GetStatusRequestHandler(context)
        };
    }

    @Override
    public ContributorContext getContext() {
        return context;
    }
}
