package org.bitrepository.service.contributor.handler;


import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;

public interface RequestHandler {
    /**
     * Return the simple name for the message with are handled by this class.
     */
    public String getRequestType();

    public void processRequest(MessageRequest request);

    public MessageResponse generateFailedResponse();
}
