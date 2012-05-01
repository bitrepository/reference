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
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.service.contributor.ContributorContext;

import java.math.BigInteger;

/**
 * The interface for the request handlers.
 * @param <T> The request class for the specific type of requests to be handled by this request handler.
 */
public abstract class AbstractRequestHandler<T> implements RequestHandler<T> {
    /** The context for the contributor.*/
    private final ContributorContext context;
    
    /**
     * Constructor.
     * @param context The context for this contributor.
     */
    protected AbstractRequestHandler(ContributorContext context) {
        this.context = context;
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
     * @param originalRequest The original request to respond to.
     * @param response The response which only needs the basic information to be send.
     */
    protected void populateResponse(MessageRequest originalRequest, MessageResponse response){
        response.setCollectionID(getContext().getSettings().getCollectionID());
        response.setCorrelationID(originalRequest.getCorrelationID());
        response.setFrom(getContext().getComponentID());
        response.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        response.setReplyTo(getContext().getReplyTo());
        response.setTo(originalRequest.getReplyTo());
        response.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
    }

    /**
     * @return The handler context as defined in the concrete classes.
     */
    protected ContributorContext getContext() {
        return context;
    }
}
