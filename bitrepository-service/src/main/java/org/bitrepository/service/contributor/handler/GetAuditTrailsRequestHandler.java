/*
 * #%L
 * Bitrepository Reference Pillar
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

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Class for handling the GetAuditTrails operation.
 */
public class GetAuditTrailsRequestHandler extends AbstractRequestHandler<GetAuditTrailsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     */
    public GetAuditTrailsRequestHandler(ContributorContext context, AuditTrailManager auditManager) {
        super(context);
        this.auditManager = auditManager;
    }

    @Override
    public Class<GetAuditTrailsRequest> getRequestClass() {
        return GetAuditTrailsRequest.class;
    }

    @Override
    public void processRequest(GetAuditTrailsRequest message) throws RequestHandlerException {
        validateMessage(message);
        sendProgressMessage(message);
        ResultingAuditTrails resAudits = collectAudits(message);
        sendFinalResponse(message, resAudits);
    }

    @Override
    public MessageResponse generateFailedResponse(GetAuditTrailsRequest request) {
        return createFinalResponse(request);
    }

    /**
     * Method for validating the content of the message.
     * @param message The message requesting the operation, which should be validated.
     * @return Whether it was valid.
     */
    protected void validateMessage(GetAuditTrailsRequest message) throws RequestHandlerException {
        if(!message.getContributor().equals(getContext().getSettings().getComponentID())) {
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            ri.setResponseText("Invalid contributor id.");
            throw new InvalidMessageException(ri);
        }
    }
    
    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     * @param message The request for the GetStatus operation.
     */
    protected void sendProgressMessage(GetAuditTrailsRequest message) {
        GetAuditTrailsProgressResponse response = createProgressResponse(message);
        
        // set missing variables in the message: ResponseInfo
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to extract the requested audit trails.");
        response.setResponseInfo(prInfo);
        
        // Send the ProgressResponse
        getContext().getDispatcher().sendMessage(response);
    }
    
    /**
     * Collect the requested audit trails.
     * @param message The message requesting the collecting of audit trails.
     * @return The requested audit trails.
     */
    protected ResultingAuditTrails collectAudits(GetAuditTrailsRequest message) {
        ResultingAuditTrails res = new ResultingAuditTrails();
        
        Long minSeq = null;
        if(message.getMinSequenceNumber() != null) {
            log.trace("Minimum sequence value: {}", message.getMinSequenceNumber().longValue());
            minSeq = message.getMinSequenceNumber().longValue();
        }
        Long maxSeq = null;
        if(message.getMaxSequenceNumber() != null) {
            log.trace("Maximum sequence value: {}", message.getMaxSequenceNumber().longValue());
            maxSeq = message.getMaxSequenceNumber().longValue();
        }
        Date minDate = null;
        if(message.getMinTimestamp() != null) {
            log.trace("Minimum date value: {}", message.getMinTimestamp());
            minDate = CalendarUtils.convertFromXMLGregorianCalendar(message.getMinTimestamp());
        }
        Date maxDate = null;
        if(message.getMaxTimestamp() != null) {
            log.trace("Maximum date value: {}", message.getMaxTimestamp());
            maxDate = CalendarUtils.convertFromXMLGregorianCalendar(message.getMaxTimestamp());
        }
        
        AuditTrailEvents events = new AuditTrailEvents();
        for(AuditTrailEvent event :  auditManager.getAudits(message.getFileID(), 
                minSeq, maxSeq, minDate, maxDate)) {
            log.trace("Adding audit trail event to results: {}", event);
            events.getAuditTrailEvent().add(event);
        }
        
        res.setAuditTrailEvents(events);
        res.setResultAddress(message.getResultAddress());
        
        return res;
    }

    /**
     * Method for sending a positive final response.
     * @param message The message to respond to.
     * @param resultingAudits The retrieved audit trails.
     */
    protected void sendFinalResponse(GetAuditTrailsRequest message, ResultingAuditTrails resultingAudits) {
        GetAuditTrailsFinalResponse response = createFinalResponse(message);
        response.setResultingAuditTrails(resultingAudits);
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        responseInfo.setResponseText("OperationSucessful performed.");
        response.setResponseInfo(responseInfo);

        getContext().getDispatcher().sendMessage(response);
    }
    
    /**
     * Creates a GetStatusProgressResponse based on a GetStatusRequest. Missing the 
     * following fields (besides the ones in dispatchResponse):
     * <br/> - ResponseInfo
     * 
     * @param message The message to base the response upon.
     * @return The GetStatusProgressResponse based on the request.
     */
    private GetAuditTrailsProgressResponse createProgressResponse(GetAuditTrailsRequest message) {
        GetAuditTrailsProgressResponse res = new GetAuditTrailsProgressResponse();
        populateResponse(message, res);
        res.setContributor(getContext().getSettings().getComponentID());
        res.setResultAddress(message.getResultAddress());
        
        return res;
    }
    
    /**
     * Creates a GetStatusFinalResponse based on a GetStatusRequest. Missing the 
     * following fields (besides the ones in dispatchResponse):
     * <br/> - ResponseInfo
     * <br/> - ResultingAuditTrails
     * 
     * @return The GetStatusFinalResponse based on the request.
     */
    protected GetAuditTrailsFinalResponse createFinalResponse(GetAuditTrailsRequest request) {
        GetAuditTrailsFinalResponse res = new GetAuditTrailsFinalResponse();
        populateResponse(request, res);
        res.setContributor(getContext().getSettings().getComponentID());
        
        return res;
    }
}
