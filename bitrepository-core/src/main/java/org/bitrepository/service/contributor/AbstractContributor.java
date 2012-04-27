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
 *
 */
public abstract class AbstractContributor implements Contributor {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final Map<String, RequestHandler> handlerMap = new HashMap<String, RequestHandler>();
    private final MessageBus messageBus;
    private final GeneralMessageHandler messageHandler;

    public AbstractContributor(MessageBus messageBus) {
        this.messageBus = messageBus;
        messageHandler = new GeneralMessageHandler();
    }

    /**
     * Starts listening for requests
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

    // ToDo Introduce fault barrier
    private class GeneralMessageHandler implements MessageListener {
        @Override
        public void onMessage(Message message) {
            if (message instanceof MessageRequest) {
                RequestHandler handler = handlerMap.get(message.getClass().getSimpleName());
                if (handler != null) {
                    handler.processRequest((MessageRequest)message);
                } else {
                    log.debug("Received unsupported request type");
                }
            } else {
                log.warn("Can only handle requests, but received " + message);
            }
        }
    }
}
