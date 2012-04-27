package org.bitrepository.service.contributor.handler;


import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;


/**
 * Provides functionality for handling a single type of request.
 */
public interface RequestHandler {
    /** Return the request class which is handled by this handler. */
    public Class getRequestClass();

    /** Implements the concrete handling of a received request */
    public void processRequest(MessageRequest request);

    /** Used for creating responses signaling general failures to handle the request.  */
    public MessageResponse generateFailedResponse();
}
