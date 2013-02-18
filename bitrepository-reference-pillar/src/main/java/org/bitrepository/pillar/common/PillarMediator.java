/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.common;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.contributor.AbstractContributorMediator;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.contributor.handler.RequestHandler;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the abstract instance for delegating the conversations for the pillar.
 * It only responds to requests. It does not it self start conversations, though it might send Alarms when something 
 * is not right.
 * All other messages than requests are considered garbage.
 */
public abstract class PillarMediator extends AbstractContributorMediator {
    private Logger log = LoggerFactory.getLogger(getClass());
    protected final MessageHandlerContext context;

    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
     */
    public PillarMediator(MessageBus messageBus, MessageHandlerContext context) {
        super(messageBus);
        this.context = context;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void handleRequest(MessageRequest request, RequestHandler handler) {
        try {
            log.debug("Receiving request: " + request.getClass().getSimpleName());
            handler.processRequest(request);
        } catch (IllegalArgumentException e) {
            getAlarmDispatcher().handleIllegalArgumentException(e);
        } catch (RequestHandlerException e) {
            log.warn("Cannot perform operation. Sending failed response. Cause: \n"
                    + e.getResponseInfo().getResponseText());
            MessageResponse response = handler.generateFailedResponse(request);
            response.setResponseInfo(e.getResponseInfo());
            context.getResponseDispatcher().dispatchResponse(response, request);
            
            log.trace("Stack trace for request handler exception.", e);
            getAlarmDispatcher().handleRequestException(e);
        } catch (RuntimeException e) {
            log.warn("Unexpected exception caught.", e);
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.FAILURE);
            responseInfo.setResponseText(e.toString());
            
            MessageResponse response = handler.generateFailedResponse(request);
            response.setResponseInfo(responseInfo);
            context.getResponseDispatcher().dispatchResponse(response, request);
            getAlarmDispatcher().handleRuntimeExceptions(e);
        }
    }

    @Override
    protected ContributorContext getContext() {
        return context;
    }

    protected PillarAlarmDispatcher getAlarmDispatcher() {
        return (PillarAlarmDispatcher) context.getAlarmDispatcher();
    }
}
