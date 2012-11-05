package org.bitrepository.service.contributor;

/*
 * #%L
 * Bitrepository Service
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
