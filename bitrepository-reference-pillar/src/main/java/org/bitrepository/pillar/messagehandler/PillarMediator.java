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
package org.bitrepository.pillar.messagehandler;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.contributor.AbstractContributorMediator;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.contributor.handler.GetAuditTrailsRequestHandler;
import org.bitrepository.service.contributor.handler.GetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetAuditTrailsRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetStatusRequestHandler;
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
public class PillarMediator extends AbstractContributorMediator {
    private Logger log = LoggerFactory.getLogger(getClass());
    protected final MessageHandlerContext context;

    protected final StorageModel pillarModel;
    
    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
     */
    public PillarMediator(MessageBus messageBus, MessageHandlerContext context, StorageModel pillarModel) {
        super(messageBus);
        this.context = context;
        this.pillarModel = pillarModel;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void handleRequest(MessageRequest request, MessageContext messageContext, RequestHandler handler) {
        try {
            handler.processRequest(request, messageContext);
        } catch (IllegalArgumentException e) {
            getAlarmDispatcher().handleIllegalArgumentException(e);
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            responseInfo.setResponseText(e.getMessage());
            log.trace("Stack trace for illegal argument", e);
            dispatchNegativeResponse(request, handler, responseInfo);
        } catch (RequestHandlerException e) {
            dispatchNegativeResponse(request, handler, e.getResponseInfo());
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
    
    @SuppressWarnings("rawtypes")
    @Override
    protected RequestHandler[] createListOfHandlers() {
        List<RequestHandler> handlers = new ArrayList<RequestHandler>();
        
        handlers.add(new IdentifyPillarsForGetFileRequestHandler(context, pillarModel));
        handlers.add(new GetFileRequestHandler(context, pillarModel));
        handlers.add(new IdentifyPillarsForGetFileIDsRequestHandler(context, pillarModel));
        handlers.add(new GetFileIDsRequestHandler(context, pillarModel));
        handlers.add(new IdentifyPillarsForGetChecksumsRequestHandler(context, pillarModel));
        handlers.add(new GetChecksumsRequestHandler(context, pillarModel));
        
        handlers.add(new IdentifyContributorsForGetStatusRequestHandler(getContext()));
        handlers.add(new GetStatusRequestHandler(getContext()));
        handlers.add(new IdentifyContributorsForGetAuditTrailsRequestHandler(getContext()));
        handlers.add(new GetAuditTrailsRequestHandler(getContext(), context.getAuditTrailManager()));
        
        handlers.add(new IdentifyPillarsForPutFileRequestHandler(context, pillarModel));
        handlers.add(new PutFileRequestHandler(context, pillarModel));
        handlers.add(new IdentifyPillarsForDeleteFileRequestHandler(context, pillarModel));
        handlers.add(new DeleteFileRequestHandler(context, pillarModel));
        handlers.add(new IdentifyPillarsForReplaceFileRequestHandler(context, pillarModel));
        handlers.add(new ReplaceFileRequestHandler(context, pillarModel));
        
        return handlers.toArray(new RequestHandler[handlers.size()]);
    }

    @Override
    protected ContributorContext getContext() {
        return context;
    }

    protected PillarAlarmDispatcher getAlarmDispatcher() {
        return (PillarAlarmDispatcher) context.getAlarmDispatcher();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void dispatchNegativeResponse(MessageRequest request, RequestHandler handler, ResponseInfo info) {
        log.warn("Cannot perform operation. Sending failed response. Cause: " + info.getResponseText());
        MessageResponse response = handler.generateFailedResponse(request);
        response.setResponseInfo(info);
        context.getResponseDispatcher().dispatchResponse(response, request);
    }
}
