package org.bitrepository.access.getaudittrails.client;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.conversation.GeneralConversationState;
import org.bitrepository.protocol.conversation.ConversationContext;
import org.bitrepository.protocol.conversation.IdentifyingState;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.ComponentSelector;
import org.bitrepository.protocol.pillarselector.MultipleComponentSelector;

public class IdentifyingAuditTrailContributers extends IdentifyingState {
    private final AuditTrailConversationContext context;
    private MultipleComponentSelector selector;

    public IdentifyingAuditTrailContributers (AuditTrailConversationContext context) {
        super();
        this.context = context;
        List<String> expectedContributers = new ArrayList<String>(context.getComponentQueries().length);
        for (AuditTrailQuery entry:context.getComponentQueries()) {
            expectedContributers.add(entry.getComponentID());
        }
        selector = new MultipleComponentSelector(expectedContributers);
    }

    @Override
    protected void sendRequest() {
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest = new IdentifyContributorsForGetAuditTrailsRequest();
        identifyRequest.setCorrelationID(context.getConversationID());
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setCollectionID(context.getSettings().getCollectionID());
        identifyRequest.setReplyTo(context.getSettings().getReferenceSettings().getClientSettings().getReceiverDestination());
        identifyRequest.setTo(context.getSettings().getCollectionDestination());
        identifyRequest.setAuditTrailInformation(context.getAuditTrailInformation());

        context.getMessageSender().sendMessage(identifyRequest);
        context.getMonitor().identifyPillarsRequestSent("Identifying contributers for audit trails");
    }

    @Override
    protected void processMessage(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof IdentifyContributorsForGetAuditTrailsResponse) {
            IdentifyContributorsForGetAuditTrailsResponse response = (IdentifyContributorsForGetAuditTrailsResponse)msg;
            selector.processResponse(response);
            context.getMonitor().pillarIdentified(response);
        } else {
            throw new UnexpectedResponseException("Are currently only expecting IdentifyContributorsForGetAuditTrailsResponse's");
        }
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Identify contributers for Audit Trails";
    }

    @Override
    public ComponentSelector getSelector() {
        return selector;
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new GettingAuditTrails(context, selector.getSelectedComponents());
    }
}