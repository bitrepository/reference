/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.service.contributor.handler;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.*;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Provides functionality for handling a single type of request.
 * @param <T> the type request handled by this handler
 */
public interface RequestHandler<T extends MessageRequest> {
    /** Return the request class which is handled by this handler.
     * @return the class of requests handled by this RequestHandler
     * */
    Class<T> getRequestClass();

    /**
     * Implements the concrete handling of a received request.
     *
     * @param request The request to handle.
     * @param messageContext the message context
     * @throws RequestHandlerException If something goes wrong while handling the
     */
    void processRequest(T request, MessageContext messageContext) throws RequestHandlerException;

    /**
     * Used for creating responses signaling general failures to handle the request.
     * The response is missing the response info field.
     * @param request the request to create a response for
     * @return The failure response.
     */
    MessageResponse generateFailedResponse(T request);
}
