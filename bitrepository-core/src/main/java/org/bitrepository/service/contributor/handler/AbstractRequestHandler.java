package org.bitrepository.service.contributor.handler;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.service.contributor.ContributorContext;

import java.math.BigInteger;

/**
 */
public abstract class AbstractRequestHandler implements RequestHandler {

    protected void dispatchResponse(MessageRequest originalRequest, MessageResponse response){
        response.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        response.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        response.setCollectionID(getContext().getCollectionID());
        response.setCorrelationID(originalRequest.getCorrelationID());
        response.setFrom(getContext().getComponentID());
        response.setReplyTo(getContext().getReplyTo());
        response.setTo(originalRequest.getReplyTo());

        getContext().getDispatcher().sendMessage(response);
    }

    /**
     * @return The handler context as defined in the concrete classes.
     */
    protected abstract ContributorContext getContext();
}
