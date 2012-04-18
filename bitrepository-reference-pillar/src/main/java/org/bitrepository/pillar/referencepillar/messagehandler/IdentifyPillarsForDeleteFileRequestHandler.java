/*
 * #%L
 * Bitrepository Reference Pillar
 * 
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
package org.bitrepository.pillar.referencepillar.messagehandler;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.exceptions.IdentifyPillarsException;
import org.bitrepository.pillar.exceptions.InvalidMessageException;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.utils.TimeMeasurementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the DeleteFile operation.
 */
public class IdentifyPillarsForDeleteFileRequestHandler 
        extends ReferencePillarMessageHandler<IdentifyPillarsForDeleteFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForDeleteFileRequestHandler(PillarContext context, ReferenceArchive referenceArchive) {
        super(context, referenceArchive);
    }

    @Override
    public void handleMessage(IdentifyPillarsForDeleteFileRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyPillarsForDeleteFileRequest message");

        try {
            validateBitrepositoryCollectionId(message.getCollectionID());
            checkThatRequestedFileIsAvailable(message);
            respondSuccessfulIdentification(message);
        } catch (IllegalArgumentException e) {
            getAlarmDispatcher().handleIllegalArgumentException(e);
        } catch (IdentifyPillarsException e) {
            log.warn("Unsuccessful identification for the DeleteFile operation.", e);
            respondUnsuccessfulIdentification(message, e.getResponseInfo());
        } catch (InvalidMessageException e) {
            log.warn("Unsuccessful identification for the DeleteFile operation.", e);
            respondUnsuccessfulIdentification(message, e.getResponseInfo());
        } catch (RuntimeException e) {
            getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Failed identifying pillar.", 
                    message.getAuditTrailInformation(), FileAction.FAILURE);
            getAlarmDispatcher().handleRuntimeExceptions(e);
        }
    }
    
    /**
     * Validates that the requested files are present in the archive. 
     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
     * @param message The message containing the id of the file. If no file id is given, then a warning is logged, 
     * but the operation is accepted.
     */
    private void checkThatRequestedFileIsAvailable(IdentifyPillarsForDeleteFileRequest message) {
        if(!getArchive().hasFile(message.getFileID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            irInfo.setResponseText("Could not find the requested file to delete.");
            throw new IdentifyPillarsException(irInfo);
        }
    }

    /**
     * Method for making a successful response to the identification.
     * @param message The request message to respond to.
     */
    private void respondSuccessfulIdentification(IdentifyPillarsForDeleteFileRequest message) {
        // Create the response.
        IdentifyPillarsForDeleteFileResponse reply = createIdentifyPillarsForDeleteFileResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, IdentifyResponseInfo (ignore PillarChecksumSpec)
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        reply.setResponseInfo(irInfo);
        
        getMessageBus().sendMessage(reply);
    }
    
    /**
     * Sends a bad response with the given cause.
     * @param message The identification request to respond to.
     * @param responseInfo The cause of the bad identification (e.g. which file is missing).
     */
    private void respondUnsuccessfulIdentification(IdentifyPillarsForDeleteFileRequest message,
            ResponseInfo responseInfo) {
        IdentifyPillarsForDeleteFileResponse reply = createIdentifyPillarsForDeleteFileResponse(message);
        
        reply.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
        reply.setResponseInfo(responseInfo);
        
        getMessageBus().sendMessage(reply);        
    }
    
    /**
     * Creates a IdentifyPillarsForDeleteFileResponse based on a 
     * IdentifyPillarsForDeleteFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - ResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param msg The IdentifyPillarsForDeleteFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForDeleteFileResponse createIdentifyPillarsForDeleteFileResponse(
            IdentifyPillarsForDeleteFileRequest msg) {
        IdentifyPillarsForDeleteFileResponse res = new IdentifyPillarsForDeleteFileResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileID(msg.getFileID());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setTo(msg.getReplyTo());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setCollectionID(getSettings().getCollectionID());
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        
        return res;
    }
}
