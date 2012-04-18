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
package org.bitrepository.pillar.checksumpillar.messagehandler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.AlarmDispatcher;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumCache;
import org.bitrepository.pillar.exceptions.InvalidMessageException;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the GetStatus operation.
 */
public class GetStatusRequestHandler extends ChecksumPillarMessageHandler<GetStatusRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    protected GetStatusRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ChecksumCache refCache) {
        super(settings, messageBus, alarmDispatcher, refCache);
    }

    @Override
    void handleMessage(GetStatusRequest message) {
        ArgumentValidator.checkNotNull(message, "DeleteFileRequest message");

        try {
            validateMessage(message);
            sendProgressMessage(message);
            ResultingStatus status = checkStatus();
            sendFinalResponse(message, status);
        } catch (InvalidMessageException e) {
            sendFailedResponse(message, e.getResponseInfo());
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException. Message ", e);
            getAlarmDispatcher().handleIllegalArgumentException(e);
            
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            ri.setResponseText(e.getMessage());
            sendFailedResponse(message, ri);
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
    protected void validateMessage(GetStatusRequest message) {
        validateBitrepositoryCollectionId(message.getCollectionID());
        validatePillarId(message.getContributor());
    }
    
    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     * @param message The request for the GetStatus operation.
     */
    protected void sendProgressMessage(GetStatusRequest message) {
        GetStatusProgressResponse pResponse = createProgressResponse(message);
        
        // set missing variables in the message: ResponseInfo
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to check the status.");
        pResponse.setResponseInfo(prInfo);
        
        // Send the ProgressResponse
        getMessageBus().sendMessage(pResponse);
    }
    
    /**
     * Check the status of the ReferencePillar.
     * @return The resulting status of the check.
     */
    protected ResultingStatus checkStatus() {
        
        // TODO find stuff to validate.
        ResultingStatus res = new ResultingStatus();
        res.setStatusTimestamp(CalendarUtils.getNow());
        
        StatusInfo info = new StatusInfo();
        info.setStatusCode(StatusCode.OK);
        info.setStatusText("No issues here.");
        res.setStatusInfo(info);
        return res;
    }
    
    /**
     * Method for sending a positive final response.
     * @param message The message to respond to.
     * @param status The result status for the operation.
     */
    protected void sendFinalResponse(GetStatusRequest message, ResultingStatus status) {
        GetStatusFinalResponse response = createFinalResponse(message);
        
        response.setResultingStatus(status);
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        responseInfo.setResponseText("OperationSucessful performed.");
        response.setResponseInfo(responseInfo);
        
        getMessageBus().sendMessage(response);
    }
    
    /**
     * Method for sending a response telling that the operation has failed.
     * @param message The message requesting the operation.
     * @param ri The information about what went wrong.
     */
    protected void sendFailedResponse(GetStatusRequest message, ResponseInfo ri) {
        log.warn("Sending unsuccessful response for the GetStatusRequest.");
        GetStatusFinalResponse response = createFinalResponse(message);
        response.setResponseInfo(ri);
        
        ResultingStatus status = new ResultingStatus();
        StatusInfo info = new StatusInfo();
        info.setStatusCode(StatusCode.ERROR);
        info.setStatusText(ri.getResponseText());
        status.setStatusInfo(info);
        status.setStatusTimestamp(CalendarUtils.getNow());
        
        response.setResultingStatus(status);
        getMessageBus().sendMessage(response);
    }
    
    /**
     * Creates a DeleteFileProgressResponse based on a DeleteFileRequest. Missing the 
     * following fields:
     * <br/> - ResponseInfo
     * 
     * @param msg The DeleteFileRequest to base the progress response on.
     * @return The DeleteFileProgressResponse based on the request.
     */
    private GetStatusProgressResponse createProgressResponse(GetStatusRequest message) {
        GetStatusProgressResponse res = new GetStatusProgressResponse();
        res.setCollectionID(getSettings().getCollectionID());
        res.setContributor(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setCorrelationID(message.getCorrelationID());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setMinVersion(MIN_VERSION);
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        res.setTo(message.getReplyTo());
        res.setVersion(VERSION);
        
        return res;
    }
    
    /**
     * Creates a GetStatusFinalResponse based on a GetStatusRequest. Missing the 
     * following fields:
     * <br/> - ResponseInfo
     * <br/> - ResultingStatus
     * 
     * @param msg The GetStatusRequest to base the final response on.
     * @return The GetStatusFinalResponse based on the request.
     */
    protected GetStatusFinalResponse createFinalResponse(GetStatusRequest msg) {
        GetStatusFinalResponse res = new GetStatusFinalResponse();
        
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
