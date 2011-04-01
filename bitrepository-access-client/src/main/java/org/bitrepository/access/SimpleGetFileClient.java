/*
 * #%L
 * bitrepository-access-client
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
package org.bitrepository.access;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.access_client.configuration.AccessConfiguration;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The client for sending and handling 'GetFile' operations.
 * Is able to either retrieve a file from a specific pillar, or to identify how fast each pillar in a given SLA is to 
 * retrieve  a specific file and then retrieve it from the fastest pillar.
 * The files are delivered to a preconfigured directory.
 * 
 * TODO move all the message generations into separate methods, or use the auto-generated constructors, which Mikis
 * has talked about.
 */
public class SimpleGetFileClient extends GetFileClientAPI implements GetFileClientExternalAPI {
    /** The log for this instance.*/
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The connection to the message bus.*/
    private final MessageBus messageBus;
    /** The queue to talk. TODO replace with a configuration.*/
    private final String queue;
    /** The GetClientServer, which receives the messages.*/
    private final GetFileClientMessageListener messageListener;
    /** The directory where the retrieved files should be placed.*/
    private final File fileDir;
    /** The configuration for the access module.*/
    private final AccessConfiguration config;
    
    /** Map for keeping track of which files are outstanding for retrieval. Key is the unique id for the file and value
     * is the container for keeping track the outstanding file instance.*/
    private Map<FileIdInstance, OutstandingFileID> outstandingFiles = Collections.synchronizedMap(
            new HashMap<FileIdInstance, OutstandingFileID>());
    /** Map for the files which are attempted to be retrieved from the pillars. Maps between unique identification 
     * of the file and the id for the pillar, where it is being retrieved from. */
    private Map<FileIdInstance, String> awaitingComplete = Collections.synchronizedMap(new HashMap<FileIdInstance, 
            String>());
    
    /**
     * Constructor.
     * Initialises the file directory, the message listener and puts the message listener on the queue.
     * The fileDir and the queue is retrieved from the configuration.
     */
    public SimpleGetFileClient() {
        log.info("Initialising the GetClient");
        config = AccessComponentFactory.getInstance().getConfig();
        
        // retrieve the directory for delivering the output files.
        fileDir = FileUtils.retrieveDirectory(config.getFileDir());
        
        // retrieve the queue from the configuration.
        queue = config.getQueue();
        messageListener = new GetFileClientMessageListener(this);
        
        // Add the messageListener to the messagebus for listening to the queue.
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
        messageBus.addListener(queue, messageListener);
    }
    
    @Override
    public void retrieveFastest(String fileId, String slaId, int number) {
        // validate arguments
        if(fileId == null || fileId.isEmpty()) {
            throw new IllegalArgumentException("The String fileId may not be null or the empty string.");
        }
        if(slaId == null || slaId.isEmpty()) {
            throw new IllegalArgumentException("The String slaId may not be null or the empty string.");
        }
        log.info("Requesting fastest retrieval of the file '" + fileId + "' which belong to the SLA '" + slaId + "'.");
        // create message requesting delivery time for the given file.
        IdentifyPillarsForGetFileRequest msg = new IdentifyPillarsForGetFileRequest();
        msg.setMinVersion((short) 1);
        msg.setVersion((short) 1);
        msg.setFileID(fileId);
        msg.setCorrelationID("TheCorrelationID_FOR_THIS_MESSAGE");
        msg.setSlaID(slaId);
        msg.setReplyTo(queue);
        
        // send the message.
        messageBus.sendMessage(queue, msg);
        
        // Store that the file is outstanding.
        FileIdInstance id = FileIdInstance.getInstance(fileId, slaId);
        outstandingFiles.put(id, new OutstandingFileID(id, this, number));
    }
    
    /**
     * Method for sending a get file request to a specific pillar.
     * 
     * @param fileId The id for the file to retrieve.
     * @param pillarId The pillar where the file should be retrieved.
     */
    @Override
    public void getFile(String fileId, String slaId, String pillarId) {
        log.info("Requesting the file '" + fileId + "' from pillar '" + pillarId + "'.");
        try {
            URL url = ProtocolComponentFactory.getInstance().getFileExchange().getURL(fileId);
            GetFileRequest msg = new GetFileRequest();
            msg.setSlaID(slaId);
            msg.setFileAddress(url.toExternalForm());
            msg.setFileID(fileId);
            msg.setPillarID(pillarId);
            msg.setReplyTo(queue);
            msg.setMinVersion((short) 1);
            msg.setVersion((short) 1);
            messageBus.sendMessage(queue, msg);
            
            // set the file to be awaiting retrieval.
            awaitingComplete.put(FileIdInstance.getInstance(fileId, slaId), pillarId);
        } catch (Exception e) {
            throw new AccessException("Problems sending a request for retrieving a specific file.", e);
        }
    }
    
    /**
     * Handles a reply for identifying the fastest pillar to deliver a given file.
     * Removes the pillar responsible for the reply from the outstanding list for the file in the reply. If no more 
     * pillars are outstanding, then the file is requested from fastest pillar.
     * 
     * @param reply The IdentifyPillarsForGetFileReply to handle.
     */
    @Override
    void handleIdentifyPillarsForGetFileReply(IdentifyPillarsForGetFileReply reply) {
        // validate arguments
        if(reply == null) {
            throw new IllegalArgumentException("The IdentifyPillarsForGetFileReply reply may not be null.");
        }
        
        FileIdInstance id = FileIdInstance.getInstance(reply.getFileID(), reply.getSlaID());
        if(!outstandingFiles.containsKey(id)) {
            // TODO Handle differently if using own queue instead of the global topic.
            log.debug("Received reply for fastest delivery of file '" + id + "', but is not missing this file. "
                    + "Message ignored!");
            return;
        }
        log.debug("Received reply for fastest delivery of file '" + id + "': " + reply);
        
        TimeMeasureTYPE time = reply.getTimeToDeliver();
        OutstandingFileID outstanding = outstandingFiles.get(id);
        outstanding.pillarReply(reply.getPillarID(), time.getMiliSec().longValue());
    }
    
    /**
     * Method for handling the GetFileResponse messages.
     * 
     * @param msg The GetFileResponse message to be handled by this method.
     */
    @Override
    void handleGetFileResponse(GetFileResponse msg) {
        // TODO Something else?
        log.info("Received response for retrieval of file '" + msg.getFileID() + "' in SLA '" + msg.getSlaID() 
                + "' from pillar '" + msg.getPillarID() + "' with the following message: \n" + msg.getResponseCode()
                + " : " + msg.getResponseText());
    }
    
    /**
     * Method for completing the get.
     * 
     * @param msg The GetFileCompleteMessage.
     */
    @Override
    void handleGetFileComplete(GetFileComplete msg) {
        if(msg == null) {
            throw new IllegalArgumentException("Cannot handle a null as GetFileComplete.");
        }
        
        FileIdInstance id = FileIdInstance.getInstance(msg.getFileID(), msg.getSlaID());
        if(!awaitingComplete.containsKey(id)) {
            // not for me.
            log.debug("GetFileComplete for '" + id + "', but it is not awaited by me. Ignoring message.");
            return;
        }
        
        try {
            log.info("Downloading the file '" + id + "' from '" + msg.getFileAddress() + "'.");
            
            URL url = new URL(msg.getFileAddress());
            
            File outputFile = new File(fileDir, id.getFileId());
            
            // Handle the scenario when a file already exists by deprecating the old one (move to '*.old')
            if(outputFile.exists()) {
                log.warn("The file '" + id + "' does already exist. Moving old one.");
                moveDeprecatedFile(outputFile);
            }
            
            FileOutputStream outStream = null;
            try {
                // download the file.
                outStream = new FileOutputStream(outputFile);
                ProtocolComponentFactory.getInstance().getFileExchange().downloadFromServer(outStream, url);
                outStream.flush();
            } finally {
                if(outStream != null) {
                    outStream.close();
                }
            }
        } catch (Exception e) {
            throw new AccessException("Problems with retrieving the file '" + id + "'.", e);
        }
        
        // remove from list when download is completed.
        awaitingComplete.remove(id);
    }
    
    /**
     * Method for deprecating a file by renaming it to '*.old' (where '*' is the current file name).
     * If an old version already exists, then rename it to '*.old.old', etc.
     * Should also work with a directory, though not be intended. 
     * 
     * TODO verify!
     * 
     * @param current The current file to deprecate. Aka move to old.
     */
    private void moveDeprecatedFile(File current) {
        File newLocation = new File(current.getParent(), current.getName() + ".old");
        // Handle scenario, when a old copy already exists. Move the old one too!
        if(newLocation.exists()) {
            moveDeprecatedFile(newLocation);
        }
        current.renameTo(newLocation);
    }
    
    /**
     * TEST FUNCTION. Is to be replaced by configurations.
     * @return The fileDir for this get file client.
     */
    File getFileDir() {
        return fileDir;
    }
}
