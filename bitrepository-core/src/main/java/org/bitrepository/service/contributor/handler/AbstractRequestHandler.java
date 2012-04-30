package org.bitrepository.service.contributor.handler;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.service.contributor.ContributorContext;

import java.math.BigInteger;

/**
 * The interface for the request handlers.
 * @param <T> The request class for the specific type of requests to be handled by this request handler.
 */
public abstract class AbstractRequestHandler<T> implements RequestHandler<T> {
    /** The context for the contributor.*/
    private final ContributorContext context;
    
    /**
     * Constructor.
     * @param context The context for this contributor.
     */
    protected AbstractRequestHandler(ContributorContext context) {
        this.context = context;
    }
    
    /**
     * Completes and sends a given response.
     * All the values of the specific response elements has to be set, including the ResponseInfo.
     * <br/> Sets the fields:
     * <br/> CollectionID
     * <br/> CorrelationID
     * <br/> From
     * <br/> MinVersion
     * <br/> ReplyTo
     * <br/> To
     * <br/> Version
     * 
     * @param originalRequest The original request to respond to.
     * @param response The response which only needs the basic information to be send.
     */
    protected void populateResponse(MessageRequest originalRequest, MessageResponse response){
        response.setCollectionID(getContext().getSettings().getCollectionID());
        response.setCorrelationID(originalRequest.getCorrelationID());
        response.setFrom(getContext().getComponentID());
        response.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        response.setReplyTo(getContext().getReplyTo());
        response.setTo(originalRequest.getReplyTo());
        response.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
    }

    /**
     * @return The handler context as defined in the concrete classes.
     */
    protected ContributorContext getContext() {
        return context;
    }
}
