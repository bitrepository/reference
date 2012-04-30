package org.bitrepository.service.contributor;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.bitrepository.service.contributor.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the general functionality for handling a set of requests. Does this by delegating the
 * handling of the specific request to appropriate <code>RequestHandler</code>s.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractContributorMediator implements ContributorMediator {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The map of request handlers. Mapping between request name and message handler for the given request.*/
    private final Map<String, RequestHandler> handlerMap = new HashMap<String, RequestHandler>();
    /** The message bus.*/
    private final MessageBus messageBus;
    /**  */
    private final GeneralMessageHandler messageHandler;

    /**
     * 
     * @param messageBus
     */
    public AbstractContributorMediator(MessageBus messageBus) {
        this.messageBus = messageBus;
        messageHandler = new GeneralMessageHandler();
    }

    /**
     * Starts listening for requests.
     * Listens both to the general collection destination and to the local destination for the contributor.
     */
    public final void start() {
        for (RequestHandler handler : createListOfHandlers()) {
            handlerMap.put(handler.getRequestClass().getSimpleName(), handler);
        }
        messageBus.addListener(getContext().getReplyTo(), messageHandler);
        messageBus.addListener(getContext().getSettings().getCollectionDestination(), messageHandler);
    }

    /**
     * @return The set of <code>RequestHandler</code>s used for this contributor.
     */
    protected abstract RequestHandler[] createListOfHandlers();

    /**
     * @return The concrete context used for this contributor.
     */
    protected abstract ContributorContext getContext();
    
    /**
     * Make the inheriting class create the environment for safely handling the request. 
     * E.g. creating the specific fault barrier.
     * @param request The request to handle.
     * @param handler The handler for the request.
     */
    protected abstract void handleRequest(MessageRequest request, RequestHandler handler);

    /**
     * 
     */
    private class GeneralMessageHandler implements MessageListener {
        @Override
        public void onMessage(Message message) {
            if (message instanceof MessageRequest) {
                RequestHandler handler = handlerMap.get(message.getClass().getSimpleName());
                if (handler != null) {
                    handleRequest((MessageRequest) message, handler);
                } else {
                    log.debug("Received unsupported request type");
                }
            } else {
                log.warn("Can only handle message requests, but received: \n{}", message);
            }
        }
    }
    
    /**
    * Closes the mediator by removing all the message handler.
    */
    public void close() {
        handlerMap.clear();
        // removes to both the general topic and the local queue.
        messageBus.removeListener(getContext().getSettings().getCollectionDestination(), messageHandler);
        messageBus.removeListener(getContext().getReplyTo(), messageHandler);
    }
}
