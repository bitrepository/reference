package org.bitrepository.pillar.messagehandler;

import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.service.exception.RequestHandlerException;

public abstract class IdentifyRequestHandler<MessageRequest> extends PillarMessageHandler<MessageRequest> {

    /**
     * Constructor.
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected IdentifyRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public void processRequest(MessageRequest request, MessageContext requestContext)
            throws RequestHandlerException {
        validateRequest(request, requestContext);
        sendPositiveResponse(request, requestContext);
    }

    /**
     * Validate both that the given request it is possible to perform and that it is allowed.
     * @param request The request to validate.
     * @param requestContext The context for the request.
     * @throws RequestHandlerException If something in the request is inconsistent with the possibilities of the pillar.
     */
    protected abstract void validateRequest(MessageRequest request, MessageContext requestContext)
            throws RequestHandlerException ;
    
    /**
     * Sends a progress response.
     * @param request The request to respond to.
     * @param requestContext The context for the request.
     */
    protected abstract void sendPositiveResponse(MessageRequest request, MessageContext requestContext);
}
