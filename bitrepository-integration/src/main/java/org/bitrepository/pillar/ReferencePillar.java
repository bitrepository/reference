/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar;

import org.apache.commons.lang.NotImplementedException;
import org.bitrepository.bitrepositoryelements.CompleteInfo;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileResponse;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Reference pillar.
 * The TODOs Will be implemented as needed. 
 */
public class ReferencePillar extends PillarAPI {
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(ReferencePillar.class);
    
    /** The amount of bytes per megabyte.*/
    private static long BYTES_PER_MB = 1024*1024;
    
    /** The reference pillar message listener.*/
    private PillarMessageListener listener;
    /** The message bus which is used for all the communication.*/
    private MessageBus messageBus;
    /** The management of the filestructure beneath.*/
    private ReferenceArchive archive;
    
    // TODO retrieve these from settings instead of the hard-coded values.
    /** The time to upload a megabyte (is set to 100 milliseconds). */
    long millisUploadPerMB = 100;
    
    /** The id for this reference pillar.*/
    private String pillarId;
    
    /** The list of ids for the SLAs which this pillar belongs to.*/
    private List<String> slaIds;
    
    /** The instance for generating the messages.*/
    private ReferencePillarMessageCreation messageCreator;
    
    /**
     * Constructor.
     */
    public ReferencePillar() throws Exception {
        // TODO use settings.
        pillarId = "Reference-Pillar";
        slaIds = new ArrayList<String>();
        slaIds.add("DefaultSla");
        
        // TODO use settings.
        archive = new ReferenceArchive("filedir");
        messageCreator = new ReferencePillarMessageCreation(this);
        
        listener = new PillarMessageListener(this);
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
        for(String slaId : slaIds) {
            messageBus.addListener(slaId, listener);
        }
    }
    
    /**
     * Method for retrieving the pillarId from this pillar.
     * @return The id for this pillar.
     */
    public String getPillarId() {
        return pillarId;
    }

    @Override
    void identifyForGetFile(IdentifyPillarsForGetFileRequest msg) {
        // validate the message
        if(msg == null) {
            throw new IllegalArgumentException("The IdentifyPillarsForGetFileRequest may not be null!");
        }
        if(!slaIds.contains(msg.getSlaID())) {
            // TODO is this the correct log-level? This pillar is just no part of the given SLA!
            log.warn("The SLA '" + msg.getSlaID() + "' is not known by this reference pillar. Ignoring "
                    + "IdentifyPillarsForGetFileRequest '" + msg + "'.");
            return;
        }
        
        // find file.
        File targetFile = archive.findFile(msg.getFileID(), msg.getSlaID());
        TimeMeasureTYPE timeToDeliver = new TimeMeasureTYPE();
        if(targetFile != null) {
            // get time
            synchronized(targetFile) {
                long time = millisUploadPerMB * targetFile.length() / BYTES_PER_MB;
                timeToDeliver.setTimeMeasureValue(BigInteger.valueOf(time));
                timeToDeliver.setTimeMeasureUnit("milliseconds");
            }
        } else {
            timeToDeliver.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
            timeToDeliver.setTimeMeasureUnit("years");
        }
        
        // Create the reply.
        IdentifyPillarsForGetFileResponse reply = messageCreator.createIdentifyPillarsForGetFileResponse(msg);
        reply.setTimeToDeliver(timeToDeliver);
        // TODO missing elements in the reply: PillarCheckType, ReplyTo ?
        
        // Send the reply.
        messageBus.sendMessage(msg.getReplyTo(), reply);
    }

    @Override
    void identifyForGetFileIds(IdentifyPillarsForGetFileIDsRequest msg) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("To be implemented, when needed");
    }

    @Override
    void identifyForGetChecksum(IdentifyPillarsForGetChecksumsRequest msg) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("To be implemented, when needed");
    }

    @Override
    void identifyForPutFile(IdentifyPillarsForPutFileRequest msg) {
        // validate the message
        if(msg == null) {
            throw new IllegalArgumentException("The IdentifyPillarsForPutFileRequest may not be null!");
        }
        if(!slaIds.contains(msg.getSlaID())) {
            // TODO is this the correct log-level? This pillar is just no part of the given SLA!
            log.warn("The SLA '" + msg.getSlaID() + "' is not known by this reference pillar. Ignoring "
                    + "IdentifyPillarsForPutFileRequest '" + msg + "'.");
            return;
        }
        
        // TODO handle the case, when the file already exists.
        
        // create the reply.
        IdentifyPillarsForPutFileResponse reply = messageCreator.createIdentifyPillarsForPutFileResponse(msg);
        // TODO should these be set?
//        reply.setTimeToDeliver("??");
//        reply.setPillarChecksumType("??");
//        reply.setReplyTo("??");
        
        // Send the reply.
        messageBus.sendMessage(msg.getReplyTo(), reply);
    }

    @Override
    void getChecksum(GetChecksumsRequest msg) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("To be implemented, when needed");
    }

    @Override
    void getFile(GetFileRequest msg) {
        // validate the message
        if(msg == null) {
            throw new IllegalArgumentException("The GetFileRequest may not be null!");
        }
        if(!slaIds.contains(msg.getSlaID())) {
            // TODO is this the correct log-level? This pillar is just no part of the given SLA!
            log.warn("The SLA '" + msg.getSlaID() + "' is not known by this reference pillar. Ignoring "
                    + "IdentifyPillarsForPutFileRequest '" + msg + "'.");
            return;
        }
        if(!msg.getPillarID().equals(pillarId)) {
            log.debug("The GetFileRequest was meant for another pillar ('" + msg.getPillarID() + "'). I will "
                    + "ignore it!");
            return;
        }
        
        // retrieve the file.
        File targetFile = archive.findFile(msg.getFileID(), msg.getSlaID());
        
        // create the response message 
        GetFileResponse response = messageCreator.createGetFileResponse(msg);
        // set response accordingly to where the file exists.
        if(targetFile != null) {
            response.setFileSize(BigInteger.valueOf(targetFile.length()));
            ResponseInfo info = new ResponseInfo();
            info.setResponseCode("1");
            info.setResponseText("Found the file and will begin to upload.");
            response.setResponseInfo(info);
        } else {
            response.setFileSize(BigInteger.valueOf(-1L));
            ResponseInfo info = new ResponseInfo();
            info.setResponseCode("2");
            info.setResponseText("File is missing. Cannot upload what cannot be found.");
            response.setResponseInfo(info);
        }
        // TODO handle these?
//        response.setChecksum("??");
//        response.setFileAddress("??");
//        response.setFileChecksumType("??");
//        response.setPartLength("??");
//        response.setPartOffSet("??");
//        response.setPillarChecksumType("??");
//        response.setReplyTo("??");

        // Send the response message.
        messageBus.sendMessage(msg.getReplyTo(), response);
        
        // If the file was not found, then do not upload it. Just stop here!
        if(targetFile == null) {
            log.error("Could not find the file '" + msg.getFileID() + "' for the SLA '" + msg.getSlaID() 
                    + "' which was requested for retrieval.");
            return;
        }
        
        // upload the file.
        URL url = ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(targetFile);
        // TODO handle the case, when the upload fails!
        
        // create the complete message 
        GetFileComplete complete = messageCreator.createGetFileComplete(msg);
        // adjust message according to 
        complete.setFileAddress(url.toExternalForm());
        CompleteInfo cInfo = new CompleteInfo();
        cInfo.setCompleteCode("1");
        cInfo.setCompleteText("File successfully uploaded to server and is ready to be downloaded by you "
                + "(the GetFileClient)!");
        complete.setCompleteInfo(cInfo);
        // TODO handle these?
//        complete.setPartLength("??");
//        complete.setPartOffSet("??");
//        complete.setPillarChecksumType("??");

        // Send the complete message.
        messageBus.sendMessage(msg.getReplyTo(), complete);
    }

    @Override
    void getFileIds(GetFileIDsRequest msg) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("To be implemented, when needed");
    }

    @Override
    void putFile(PutFileRequest msg) {
        // validate the message
        if(msg == null) {
            throw new IllegalArgumentException("The PutFileRequest may not be null!");
        }
        if(!slaIds.contains(msg.getSlaID())) {
            // TODO is this the correct log-level? This pillar is just no part of the given SLA!
            log.warn("The SLA '" + msg.getSlaID() + "' is not known by this reference pillar. Ignoring "
                    + "IdentifyPillarsForPutFileRequest '" + msg + "'.");
            return;
        }
        if(!msg.getPillarID().equals(pillarId)) {
            log.debug("The PutFileRequest was meant for another pillar ('" + msg.getPillarID() + "'). I will ignore "
                    + "it!");
            return;
        }
        
        File targetFile = archive.findFile(msg.getFileID(), msg.getSlaID());
        
        PutFileResponse response = messageCreator.createPutFileResponse(msg);
        // give response accordingly to whether
        if(targetFile == null) {
            ResponseInfo info = new ResponseInfo();
            info.setResponseCode("1");
            info.setResponseText("We are ready to receive the file.");
            response.setResponseInfo(info);
        } else {
            ResponseInfo info = new ResponseInfo();
            info.setResponseCode("2");
            info.setResponseText("We already have the file.");
            response.setResponseInfo(info);
        }
        // TODO handle these?
//      response.setFileAddress(msg.getFileAddress());
//      response.setPillarChecksumType("??")
//      response.setReplyTo("??")
        
        // Send the response message.
        messageBus.sendMessage(msg.getReplyTo(), response);
        
        // Do not continue if the file was already known
        if(targetFile != null) {
            log.warn("Asked for putting file '" + msg.getFileID() + "' for sla '" + msg.getSlaID() + "', but it "
                    + "already exists. Do not continue with put!");
            return;
        }
        
        // retrieve a file where it should be downloaded. 
        File fileToDownload = archive.getNewFile(msg.getFileID());
        
        // Download the file. 
        // TODO send a erroneous PutFileResponse if the download fails.
        ProtocolComponentFactory.getInstance().getFileExchange().downloadFromServer(fileToDownload, 
                msg.getFileAddress());
        
        archive.archiveFile(msg.getFileID(), msg.getSlaID());
        
        PutFileComplete complete = messageCreator.createPutFileComplete(msg);
        CompleteInfo cInfo = new CompleteInfo();
        cInfo.setCompleteCode("1");
        cInfo.setCompleteText("File successfully put");
        complete.setCompleteInfo(cInfo);
        // TODO handle these?
//      complete.setReplyTo(value)
//      complete.setCompleteSaltChecksum(value)
//      complete.setFileAddress(value)
//      complete.setPillarChecksumType(value)
        
        // Send the complete message.
        messageBus.sendMessage(msg.getReplyTo(), complete);
    }
}
