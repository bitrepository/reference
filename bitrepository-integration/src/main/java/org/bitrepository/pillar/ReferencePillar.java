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
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.common.utils.ArgumentValidationUtils;
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
public class ReferencePillar implements PillarAPI {
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
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
    private ReferencePillarMessageFactory messageCreator;
    
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
        messageCreator = new ReferencePillarMessageFactory(this);
        
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
    public void identifyForGetFile(IdentifyPillarsForGetFileRequest msg) {
        ArgumentValidationUtils.checkNotNull(msg, "IdentifyPillarsForGetFileRequest msg");
        if(!slaIds.contains(msg.getBitRepositoryCollectionID())) {
            // TODO is this the correct log-level? This pillar is just no part of the given SLA!
            log.warn("The SLA '{}' is not known by this reference pillar. Ignoring "
                    + "IdentifyPillarsForGetFileRequest '{}'.", msg.getBitRepositoryCollectionID(), msg);
            return;
        }
        
        // find file.
        File targetFile = archive.findFile(msg.getFileID(), msg.getBitRepositoryCollectionID());
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
    public void identifyForGetFileIds(IdentifyPillarsForGetFileIDsRequest msg) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("To be implemented, when needed");
    }

    @Override
    public void identifyForGetChecksum(IdentifyPillarsForGetChecksumsRequest msg) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("To be implemented, when needed");
    }

    @Override
    public void identifyForPutFile(IdentifyPillarsForPutFileRequest msg) {
        ArgumentValidationUtils.checkNotNull(msg, "IdentifyPillarsForPutFileRequest msg");
        if(!slaIds.contains(msg.getBitRepositoryCollectionID())) {
            // TODO is this the correct log-level? This pillar is just no part of the given SLA!
            log.warn("The SLA '{}' is not known by this reference pillar. Ignoring "
                    + "IdentifyPillarsForGetFileRequest '{}'.", msg.getBitRepositoryCollectionID(), msg);
            return;
        }
        
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
    public void getChecksum(GetChecksumsRequest msg) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("To be implemented, when needed");
    }

    @Override
    public void getFile(GetFileRequest msg) {
        ArgumentValidationUtils.checkNotNull(msg, "GetFileRequest msg");
        if(!slaIds.contains(msg.getBitRepositoryCollectionID())) {
            // TODO is this the correct log-level? This pillar is just no part of the given SLA!
            log.warn("The SLA '{}' is not known by this reference pillar. Ignoring "
                    + "IdentifyPillarsForGetFileRequest '{}'.", msg.getBitRepositoryCollectionID(), msg);
            return;
        }
        if(!msg.getPillarID().equals(pillarId)) {
            log.debug("The GetFileRequest was meant for another pillar ('" + msg.getPillarID() + "'). I will "
                    + "ignore it!");
            return;
        }
        
        // retrieve the file.
        File targetFile = archive.findFile(msg.getFileID(), msg.getBitRepositoryCollectionID());
        
        // create the progress response message 
        GetFileProgressResponse response = messageCreator.createGetFileProgressResponse(msg);
        // set progress response accordingly to where the file exists.
        if(targetFile != null) {
            response.setFileSize(BigInteger.valueOf(targetFile.length()));
            ProgressResponseInfo info = new ProgressResponseInfo();
            info.setProgressResponseCode("1");
            info.setProgressResponseText("Found the file and will begin to upload.");
            response.setProgressResponseInfo(info);
        } else {
            response.setFileSize(BigInteger.valueOf(-1L));
            ProgressResponseInfo info = new ProgressResponseInfo();
            info.setProgressResponseCode("404");
            info.setProgressResponseText("File is missing. Cannot upload what cannot be found.");
            response.setProgressResponseInfo(info);
        }
        // TODO handle these?
//        response.setChecksum("??");
//        response.setFileAddress("??");
//        response.setFileChecksumType("??");
//        response.setPartLength("??");
//        response.setPartOffSet("??");
//        response.setPillarChecksumType("??");
//        response.setReplyTo("??");

        // Send the progress response message.
        messageBus.sendMessage(msg.getReplyTo(), response);
        
        // If the file was not found, then do not upload it. Just stop here!
        if(targetFile == null) {
            log.error("Could not find the file '" + msg.getFileID() + "' for the SLA '" 
            		+ msg.getBitRepositoryCollectionID() + "' which was requested for retrieval.");
            return;
        }
        
        // upload the file.
        URL url = ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(targetFile);
        // TODO handle the case, when the upload fails!
        
        // create the final response message 
        GetFileFinalResponse complete = messageCreator.createGetFileFinalResponse(msg);
        // adjust message according to 
        complete.setFileAddress(url.toExternalForm());
        FinalResponseInfo finalInfo = new FinalResponseInfo();
        finalInfo.setFinalResponseCode("1");
        finalInfo.setFinalResponseText("File successfully uploaded to server and is ready to be downloaded by you "
                + "(the GetFileClient)!");
        complete.setFinalResponseInfo(finalInfo);
        // TODO handle these?
//        complete.setPartLength("??");
//        complete.setPartOffSet("??");
//        complete.setPillarChecksumType("??");

        // Send the complete message.
        messageBus.sendMessage(msg.getReplyTo(), complete);
    }

    @Override
    public void getFileIds(GetFileIDsRequest msg) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("To be implemented, when needed");
    }

    @Override
    public void putFile(PutFileRequest msg) {
        ArgumentValidationUtils.checkNotNull(msg, "PutFileRequest msg");
        if(!slaIds.contains(msg.getBitRepositoryCollectionID())) {
            // TODO is this the correct log-level? This pillar is just no part of the given SLA!
            log.warn("The SLA '{}' is not known by this reference pillar. Ignoring "
                    + "IdentifyPillarsForGetFileRequest '{}'.", msg.getBitRepositoryCollectionID(), msg);
            return;
        }
        if(!msg.getPillarID().equals(pillarId)) {
            log.debug("The PutFileRequest was meant for another pillar ('" + msg.getPillarID() + "'). I will ignore "
                    + "it!");
            return;
        }
        
        File targetFile = archive.findFile(msg.getFileID(), msg.getBitRepositoryCollectionID());
        
        PutFileProgressResponse response = messageCreator.createPutFileProgressResponse(msg);
        if(targetFile == null) {
        	ProgressResponseInfo info = new ProgressResponseInfo();
            info.setProgressResponseCode("1");
            info.setProgressResponseText("We are ready to receive the file.");
            response.setProgressResponseInfo(info);
        } else {
        	ProgressResponseInfo info = new ProgressResponseInfo();
            info.setProgressResponseCode("2");
            info.setProgressResponseText("We already have the file.");
            response.setProgressResponseInfo(info);
        }
        // TODO handle these?
//      response.setFileAddress(msg.getFileAddress());
//      response.setPillarChecksumType("??")
//      response.setReplyTo("??")
        
        // Send the response message.
        messageBus.sendMessage(msg.getReplyTo(), response);
        
        // Do not continue if the file was already known
        if(targetFile != null) {
            log.warn("Asked for putting file '" + msg.getFileID() + "' for sla '" + msg.getBitRepositoryCollectionID() 
            		+ "', but it already exists. Do not continue with put!");
            return;
        }
        
        // retrieve a file where it should be downloaded. 
        File fileToDownload = archive.getNewFile(msg.getFileID());
        
        // Download the file. 
        // TODO send a erroneous PutFileResponse if the download fails.
        ProtocolComponentFactory.getInstance().getFileExchange().downloadFromServer(fileToDownload, 
                msg.getFileAddress());
        
        archive.archiveFile(msg.getFileID(), msg.getBitRepositoryCollectionID());
        
        PutFileFinalResponse complete = messageCreator.createPutFileFinalResponse(msg);
        FinalResponseInfo finalInfo = new FinalResponseInfo();
        finalInfo.setFinalResponseCode("1");
        finalInfo.setFinalResponseText("File successfully put");
        complete.setFinalResponseInfo(finalInfo);
        // TODO handle these?
//      complete.setReplyTo(value)
//      complete.setCompleteSaltChecksum(value)
//      complete.setFileAddress(value)
//      complete.setPillarChecksumType(value)
        
        // Send the complete message.
        messageBus.sendMessage(msg.getReplyTo(), complete);
    }
}
