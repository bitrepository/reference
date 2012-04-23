package org.bitrepository.service.contributor;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.bitrepository.service.contributor.handler.RequestHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * General class for handling the general
 */
public abstract class AbstractContributor implements Contributor {
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
            handlerMap.put(handler.getRequestType(), handler);
        }
        messageBus.addListener(getContext().getReplyTo(), messageHandler);
    }

    public abstract RequestHandler[] createListOfHandlers();

    protected abstract ContributorContext getContext();

    private class GeneralMessageHandler implements MessageListener {
        @Override
        public void onMessage(Message message) {
            handlerMap.get(message.getClass().getSimpleName());
        }
    }
}
