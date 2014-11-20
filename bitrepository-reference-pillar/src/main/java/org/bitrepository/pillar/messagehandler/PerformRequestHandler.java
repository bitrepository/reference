package org.bitrepository.pillar.messagehandler;

import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.service.exception.RequestHandlerException;

public abstract class PerformRequestHandler<MessageRequest> extends PillarMessageHandler<MessageRequest> {

    /**
     * Constructor.
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected PerformRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public void processRequest(MessageRequest request, MessageContext requestContext)
            throws RequestHandlerException {
        validateRequest(request, requestContext);
        sendProgressResponse(request, requestContext);
        performOperation(request, requestContext);
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
     * @throws RequestHandlerException If something data necessary for the progress response could not be extracted. 
     */
    protected abstract void sendProgressResponse(MessageRequest request, MessageContext requestContext)
            throws RequestHandlerException ;
    
    /**
     * Perform the operation behind the request.
     * @param request The request to perform.
     * @param requestContext The context for the request.
     * @throws RequestHandlerException If the request is unable to be performed.
     */
    protected abstract void performOperation(MessageRequest request, MessageContext requestContext)
            throws RequestHandlerException ;
}
