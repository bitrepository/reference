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

import java.math.BigInteger;

import org.bitrepository.bitrepositoryelements.AlarmcodeType;
import org.bitrepository.bitrepositoryelements.ErrorcodeGeneralType;
import org.bitrepository.bitrepositoryelements.IdentifyResponseCodePositiveType;
import org.bitrepository.bitrepositoryelements.IdentifyResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE.TimeMeasureUnit;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFile operation.
 * TODO handle error scenarios.
 */
public class GetFileIdentificationMessageHandler extends PillarMessageHandler<IdentifyPillarsForGetFileRequest> {

    /**
     * Constructor.
     * @param mediator The mediator for this pillar.
     */
    public GetFileIdentificationMessageHandler(PillarMediator mediator) {
        super(mediator);
    }
    
    /**
     * Handles the identification messages for the GetFile operation.
     * TODO perhaps synchronisation?
     * @param message The IdentifyPillarsForGetFileRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForGetFileRequest message) {
        try {
            // Validate the message.
            validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());

            if(!mediator.archive.hasFile(message.getFileID())) {
                respondBadIdentification(message, "The file does not exist within the archive.");
            } else {
                respondSuccesfullIdentification(message);
            }
        } catch (IllegalArgumentException e) {
            alarmIllegalArgument(e);
        }
    }
    
    /**
     * Method for making a successfull response to the identification.
     * TODO perhaps synchronisation?
     * @param message The request message to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetFileRequest message) {
        // Create the response.
        IdentifyPillarsForGetFileResponse reply = mediator.msgFactory.createIdentifyPillarsForGetFileResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, AuditTrailInformation
        reply.setTimeToDeliver(mediator.settings.getPillar().getTimeToDeliver());
        // TODO handle audit trails!!!
        reply.setAuditTrailInformation(null);
        
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        irInfo.setIdentifyResponseCode(IdentifyResponseCodePositiveType.IDENTIFICATION_POSITIVE.value().toString());
        irInfo.setIdentifyResponseText("Operation acknowledged and accepted.");
        reply.setIdentifyResponseInfo(irInfo);
        
        // Send resulting file.
        mediator.messagebus.sendMessage(reply);
    }
    
    /**
     * Method for sending a bad response.
     * @param message The identification request to respond to.
     */
    private void respondBadIdentification(IdentifyPillarsForGetFileRequest message, String cause) {
        // Create the response.
        IdentifyPillarsForGetFileResponse reply = mediator.msgFactory.createIdentifyPillarsForGetFileResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, AuditTrailInformation, IdentifyResponseInfo
        TimeMeasureTYPE timeToDeliver = new TimeMeasureTYPE();
        timeToDeliver.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        timeToDeliver.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        reply.setTimeToDeliver(timeToDeliver);
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        irInfo.setIdentifyResponseCode(ErrorcodeGeneralType.DUPLICATE_FILE.value().toString());
        irInfo.setIdentifyResponseText("ERROR: " + cause);
        reply.setIdentifyResponseInfo(irInfo);
        // TODO handle audit trails!!!
        reply.setAuditTrailInformation(null);
        
        // Send resulting file.
        mediator.messagebus.sendMessage(reply);
    }
}
