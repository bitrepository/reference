package org.bitrepository.service.contributor.handler;

import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Provides functionality for handling a single type of request.
 */
public interface RequestHandler<T> {
    /** Return the request class which is handled by this handler. */
    public Class<T> getRequestClass();

    /**
     * Implements the concrete handling of a received request.
     * @param request The request to handle.
     * @throws RequestHandlerException If something goes wrong while handling the 
     */
    public void processRequest(T request) throws RequestHandlerException;

    /**
     * Used for creating responses signaling general failures to handle the request.
     * The response is missing the response info field.
     * @return The failure response.
     */
    public MessageResponse generateFailedResponse(T request);
}
