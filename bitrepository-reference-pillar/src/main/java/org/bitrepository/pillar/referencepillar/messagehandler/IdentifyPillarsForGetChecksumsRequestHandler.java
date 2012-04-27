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
package org.bitrepository.pillar.referencepillar.messagehandler;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.exceptions.IdentifyPillarsException;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.utils.TimeMeasurementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetChecksums operation.
 */
public class IdentifyPillarsForGetChecksumsRequestHandler 
        extends ReferencePillarMessageHandler<IdentifyPillarsForGetChecksumsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForGetChecksumsRequestHandler(PillarContext context, ReferenceArchive referenceArchive) {
        super(context, referenceArchive);
    }

    /**
     * Handles the identification messages for the GetChecksums operation.
     * @param message The IdentifyPillarsForGetChecksumsRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForGetChecksumsRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyPillarsForGetChecksumsRequest message");

        try {
            validateBitrepositoryCollectionId(message.getCollectionID());
            validateChecksumSpecification(message.getChecksumRequestForExistingFile());
            checkThatAllRequestedFilesAreAvailable(message);
            respondSuccesfullIdentification(message);
        } catch (IllegalArgumentException e) {
            getAlarmDispatcher().handleIllegalArgumentException(e);
        } catch (IdentifyPillarsException e) {
            log.warn("Unsuccessfull identification for the GetChecksums operation.", e);
            respondUnsuccessfulIdentification(message, e);
        } catch (RuntimeException e) {
            getAuditManager().addAuditEvent(message.getFileIDs().getFileID(), message.getFrom(), 
                    "Failed identifying pillar.", message.getAuditTrailInformation(), FileAction.FAILURE);
            getAlarmDispatcher().handleRuntimeExceptions(e);
        }
    }
    
    /**
     * Validates that all the requested files in the filelist are present. 
     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
     * @param message The message containing the list files. An empty filelist is expected 
     * when "AllFiles" or the parameter option is used.
     */
    public void checkThatAllRequestedFilesAreAvailable(IdentifyPillarsForGetChecksumsRequest message) {
        FileIDs fileids = message.getFileIDs();
        if(fileids == null) {
            log.debug("No fileids are defined in the identification request ('" + message.getCorrelationID() + "').");
            return;
        }
        
        List<String> missingFiles = new ArrayList<String>();
        String fileID = fileids.getFileID();
        if(fileID != null && !fileID.isEmpty() && !getArchive().hasFile(fileID)) {
            missingFiles.add(fileID);
        }
        
        // Throw exception if any files are missing.
        if(!missingFiles.isEmpty()) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            irInfo.setResponseText(missingFiles.size() + " missing files: '" + missingFiles + "'");
            
            throw new IdentifyPillarsException(irInfo);
        }
    }
    
    /**
     * Method for making a successful response to the identification.
     * @param message The request message to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetChecksumsRequest message) {
        // Create the response.
        IdentifyPillarsForGetChecksumsResponse reply = createIdentifyPillarsForGetChecksumsResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, AuditTrailInformation, IdentifyResponseInfo
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        reply.setResponseInfo(irInfo);
        
        // Send resulting file.
        getMessageBus().sendMessage(reply);
    }
    
    /**
     * Sends a bad response with the given cause.
     * @param message The identification request to respond to.
     * @param cause The cause of the bad identification (e.g. which files are missing).
     */
    private void respondUnsuccessfulIdentification(IdentifyPillarsForGetChecksumsRequest message, 
            IdentifyPillarsException cause) {
        IdentifyPillarsForGetChecksumsResponse reply = createIdentifyPillarsForGetChecksumsResponse(message);
        
        reply.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
        reply.setResponseInfo(cause.getResponseInfo());
        
        getMessageBus().sendMessage(reply);
    }
    
    /**
     * Creates a IdentifyPillarsForGetChecksumsResponse based on a 
     * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - AuditTrailInformation
     * <br/> - IdentifyResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param msg The IdentifyPillarsForGetFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForGetChecksumsResponse createIdentifyPillarsForGetChecksumsResponse(
            IdentifyPillarsForGetChecksumsRequest msg) {
        IdentifyPillarsForGetChecksumsResponse res 
                = new IdentifyPillarsForGetChecksumsResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileIDs(msg.getFileIDs());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setTo(msg.getReplyTo());
        res.setChecksumRequestForExistingFile(msg.getChecksumRequestForExistingFile());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setCollectionID(getSettings().getCollectionID());
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        
        return res;
    }
}
