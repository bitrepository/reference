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
package org.bitrepository.service.contributor;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.handler.GetAuditTrailsRequestHandler;
import org.bitrepository.service.contributor.handler.GetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetAuditTrailsRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.RequestHandler;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of a contributor mediator.
 * It supports the handling of the 'GetStatus' identification and operation.
 * 
 * If the optional AuditTrailManager is given as argument, then this mediator will also be able to handle the 
 * GetAuditTrails identification and operation. 
 */
public class SimpleContributorMediator extends AbstractContributorMediator {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The context for this simple contributor mediator.*/
    private final ContributorContext context;
    /** The audit trail manager. */
    private AuditTrailManager auditManager;
    
    /**
     * Constructor.
     * @param messageBus The messagebus for the mediator.
     * @param settings the settings for the mediator.
     * @param componentID The id of this component.
     * @param replyTo The destination for this component.
     * @param auditManager [OPTIONAL] The manager of audit trails. Only if the contributor has audit trails.
     */
    public SimpleContributorMediator(MessageBus messageBus, Settings settings, String componentID, String replyTo,
            AuditTrailManager auditManager) {
        super(messageBus);
        context = new ContributorContext(messageBus, settings, componentID, replyTo);
        this.auditManager = auditManager;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected RequestHandler[] createListOfHandlers() {
        List<RequestHandler> handlers = new ArrayList<RequestHandler>();
        
        handlers.add(new IdentifyContributorsForGetStatusRequestHandler(getContext()));
        handlers.add(new GetStatusRequestHandler(getContext()));
        
        if(auditManager != null) {
            handlers.add(new IdentifyContributorsForGetAuditTrailsRequestHandler(getContext()));
            handlers.add(new GetAuditTrailsRequestHandler(getContext(), auditManager));
        }
        
        return handlers.toArray(new RequestHandler[handlers.size()]);
    }
    
    @Override
    protected ContributorContext getContext() {
        return context;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void handleRequest(MessageRequest request, RequestHandler handler) {
        try {
            handler.processRequest(request);
        } catch (RequestHandlerException e) {
            log.info("Invalid Message exception caught. Sending failed response.", e);
            MessageResponse response = handler.generateFailedResponse(request);
            response.setResponseInfo(e.getResponseInfo());
            context.getDispatcher().sendMessage(response);            
        } catch (Exception e) {
            log.warn("Unexpected exception caught.", e);
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.FAILURE);
            responseInfo.setResponseText(e.toString());
            
            MessageResponse response = handler.generateFailedResponse(request);
            response.setResponseInfo(responseInfo);
            context.getDispatcher().sendMessage(response);
        }
    }
}
