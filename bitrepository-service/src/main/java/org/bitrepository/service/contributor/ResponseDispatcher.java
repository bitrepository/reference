package org.bitrepository.service.contributor;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * Provides the general functionality for sending reponses from a contributor.
 */
public class ResponseDispatcher extends MessageDispatcher {

    public ResponseDispatcher(Settings settings, MessageSender sender) {
        super(settings, sender);
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
     * @param response The response which only needs the basic information to be send.
     * @param request The original request to respond to.
     */
    public void dispatchResponse(MessageResponse response, MessageRequest request) {
        response.setCorrelationID(request.getCorrelationID());
        response.setReplyTo(settings.getContributorDestinationID());
        response.setTo(request.getReplyTo());
        dispatchMessage(response);
    }
}
