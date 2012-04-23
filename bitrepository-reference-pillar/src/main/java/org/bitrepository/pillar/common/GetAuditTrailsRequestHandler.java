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
package org.bitrepository.pillar.common;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.exceptions.InvalidMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the GetAuditTrails operation.
 */
public class GetAuditTrailsRequestHandler extends PillarMessageHandler<GetAuditTrailsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     */
    public GetAuditTrailsRequestHandler(PillarContext context) {
        super(context);
    }

    @Override
    public void handleMessage(GetAuditTrailsRequest message) {
        ArgumentValidator.checkNotNull(message, "DeleteFileRequest message");
        
        try {
            validateMessage(message);
            sendProgressMessage(message);
            ResultingAuditTrails resAudits = collectAudits(message);
            sendFinalResponse(message, resAudits);
        } catch (InvalidMessageException e) {
            sendFailedResponse(message, e.getResponseInfo());
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException. Message ", e);
            getAlarmDispatcher().handleIllegalArgumentException(e);
        } catch (RuntimeException e) {
            log.warn("Internal RunTimeException caught. Sending response for 'error at my end'.", e);
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FAILURE);
            fri.setResponseText("Error: " + e.getMessage());
            sendFailedResponse(message, fri);
        }
    }

    /**
     * Method for validating the content of the message.
     * @param message The message requesting the operation, which should be validated.
     * @return Whether it was valid.
     */
    protected void validateMessage(GetAuditTrailsRequest message) {
        validateBitrepositoryCollectionId(message.getCollectionID());
        validatePillarId(message.getContributor());
    }
    
    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     * @param message The request for the GetStatus operation.
     */
    protected void sendProgressMessage(GetAuditTrailsRequest message) {
        GetAuditTrailsProgressResponse pResponse = createProgressResponse(message);
        
        // set missing variables in the message: ResponseInfo
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to check the status.");
        pResponse.setResponseInfo(prInfo);
        
        // Send the ProgressResponse
        getMessageBus().sendMessage(pResponse);
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
            minSeq = message.getMinSequenceNumber().longValue();
        }
        Long maxSeq = null;
        if(message.getMaxSequenceNumber() != null) {
            maxSeq = message.getMaxSequenceNumber().longValue();
        }
        Date minDate = null;
        if(message.getMinTimestamp() != null) {
            minDate = CalendarUtils.convertFromXMLGregorianCalendar(message.getMinTimestamp());
        }
        Date maxDate = null;
        if(message.getMaxTimestamp() != null) {
            maxDate = CalendarUtils.convertFromXMLGregorianCalendar(message.getMaxTimestamp());
        }
        
        AuditTrailEvents events = new AuditTrailEvents();
        for(AuditTrailEvent event :  getAuditManager().getAudits(message.getFileID(), 
                minSeq, maxSeq, minDate, maxDate)) {
            events.getAuditTrailEvent().add(event);
        }
        
        res.setAuditTrailEvents(events);
        res.setResultAddress(message.getResultAddress());
        
        return res;
    }
    
    /**
     * Method for sending a response telling that the operation has failed.
     * @param message The message requesting the operation.
     * @param ri The information about what went wrong.
     */
    protected void sendFailedResponse(GetAuditTrailsRequest message, ResponseInfo ri) {
        GetAuditTrailsFinalResponse response = createFinalResponse(message);
        response.setResponseInfo(ri);
        getMessageBus().sendMessage(response);
    }

    /**
     * Method for sending a positive final response.
     * @param message The message to respond to.
     * @param status The result status for the operation.
     */
    protected void sendFinalResponse(GetAuditTrailsRequest message, ResultingAuditTrails resultingAudits) {
        GetAuditTrailsFinalResponse response = createFinalResponse(message);
        response.setResultingAuditTrails(resultingAudits);
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        responseInfo.setResponseText("OperationSucessful performed.");
        response.setResponseInfo(responseInfo);

        getMessageBus().sendMessage(response);
    }
    
    /**
     * Creates a GetStatusProgressResponse based on a GetStatusRequest. Missing the 
     * following fields:
     * <br/> - ResponseInfo
     * 
     * @param msg The GetStatusRequest to base the progress response on.
     * @return The GetStatusProgressResponse based on the request.
     */
    private GetAuditTrailsProgressResponse createProgressResponse(GetAuditTrailsRequest message) {
        GetAuditTrailsProgressResponse res = new GetAuditTrailsProgressResponse();
        res.setCollectionID(getSettings().getCollectionID());
        res.setContributor(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setCorrelationID(message.getCorrelationID());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setMinVersion(MIN_VERSION);
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        res.setResultAddress(message.getResultAddress());
        res.setTo(message.getReplyTo());
        res.setVersion(VERSION);
        
        return res;
    }
    
    /**
     * Creates a GetStatusFinalResponse based on a GetStatusRequest. Missing the 
     * following fields:
     * <br/> - ResponseInfo
     * <br/> - ResultingAuditTrails
     * 
     * @param msg The GetStatusRequest to base the final response on.
     * @return The GetStatusFinalResponse based on the request.
     */
    protected GetAuditTrailsFinalResponse createFinalResponse(GetAuditTrailsRequest msg) {
        GetAuditTrailsFinalResponse res = new GetAuditTrailsFinalResponse();
        
        res.setCollectionID(getSettings().getCollectionID());
        res.setContributor(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setCorrelationID(msg.getCorrelationID());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setMinVersion(MIN_VERSION);
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        res.setTo(msg.getReplyTo());
        res.setVersion(VERSION);
        
        return res;
    }
}
