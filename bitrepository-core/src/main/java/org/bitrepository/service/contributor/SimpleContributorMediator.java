package org.bitrepository.service.contributor;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.contributor.handler.GetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.RequestHandler;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of a contributor mediator.
 * It only supports the handling of the 'GetStatus' identification and operation.
 */
public class SimpleContributorMediator extends AbstractContributorMediator {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The context for this simple contributor mediator.*/
    private final ContributorContext context;
    
    /**
     * Constructor.
     * @param messageBus The messagebus for the mediator.
     * @param settings the settings for the mediator.
     * @param componentID The id of this component.
     * @param replyTo The destination for this component.
     */
    public SimpleContributorMediator(MessageBus messageBus, Settings settings, String componentID, String replyTo) {
        super(messageBus);
        context = new ContributorContext(messageBus, settings, componentID, replyTo);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected RequestHandler[] createListOfHandlers() {
        List<RequestHandler> handlers = new ArrayList<RequestHandler>();
        
        handlers.add(new IdentifyContributorsForGetStatusRequestHandler(getContext()));
        handlers.add(new GetStatusRequestHandler(getContext()));
        
        return handlers.toArray(new RequestHandler[handlers.size()]);
    }
    
    @Override
    protected ContributorContext getContext() {
        return context;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void handleRequest(MessageRequest request, RequestHandler handler) {
        try {
            handler.processRequest(request);
        } catch (RequestHandlerException e) {
            log.info("Invalid Message exception caught. Sending failed response.", e);
            MessageResponse response = handler.generateFailedResponse(request);
            response.setResponseInfo(e.getResponseInfo());
            context.getDispatcher().sendMessage(response);            
        } catch (Exception e) {
            log.warn("Unexpected exception caught.", e);
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.FAILURE);
            responseInfo.setResponseText(e.getMessage());
            
            MessageResponse response = handler.generateFailedResponse(request);
            response.setResponseInfo(responseInfo);
            context.getDispatcher().sendMessage(response);
        }
    }
}
