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
package org.bitrepository.modify;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumsTypeTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest.ChecksumForCheck;
import org.bitrepository.bitrepositorymessages.PutFileRequest.SaltForCheck;
import org.bitrepository.bitrepositorymessages.PutFileResponse;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the PutClient.
 * 
 * TODO perhaps merge the 'outstanding' and the 'FileIdForPut'?
 */
public class SimplePutClient extends PutClientAPI {
    /** The log for this class.*/
    private Logger log = LoggerFactory.getLogger(SimplePutClient.class);
    
    /** The server, which handles the messages.*/
    private PutClientMessageListener server;
    /** The queue where the messages are sent.*/
    String queue;
    /** The message bus which is used for all the communication.*/
    private MessageBus messageBus;
    
    /** Whether to use a checksum. TODO use settings!*/
    private static final boolean USE_CHECKSUM = true;
    /** Whether to demand a salt for verifying the file. TODO use settings.*/
    private static final boolean USE_SALT = false;
    /** Which type of checksum to use. TODO define in settings.*/
    private static final ChecksumsTypeTYPE DEFAULT_CHECKSUM_TYPE 
            = ChecksumsTypeTYPE.MD_5;
    /** The default letter used for the salt. TODO define in settings.*/
    private static final String DEFAULT_SALT = "X";
    
    /** Map for keeping track of the data for a fileId. */
    private Map<String, FileIdForPut> fileIds = Collections.synchronizedMap(
            new HashMap<String, FileIdForPut>());
    
    /** The container for keeping track of which files are outstanding and 
     * at which pillars they are outstanding. */
    OutstandingPutFiles outstandings = new OutstandingPutFiles();
    
    /**
     * Constructor.
     * @throws Exception If a connection to the messagebus could not be 
     * established.
     */
    public SimplePutClient() throws Exception {
        log.info("Initialising the PutClient");
        // TODO
        // Load settings!
        // Establish connection to bus!
        
        server = new PutClientMessageListener(this);
        
        // TODO use a settings. Temporarily use the current time.
        queue = "" + (new Date().getTime());
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
        messageBus.addListener(queue, server);
    }
    
    /**
     * Method for putting a file to all pillars for a given SLA.
     * 
     * @param file The file to put.
     * @param idForFile The fileId for the file.
     * @param slaId The SLA id for identifying the pillars which should receive
     * this file.
     * @throws Exception If something goes wrong with the connection.
     */
    @Override
    public final synchronized void putFileWithId(File file, String idForFile, 
            String slaId) {
        if(file == null || slaId == null || slaId.isEmpty()) {
            throw new IllegalArgumentException("Unacceptable argument: "
                    + "fil = '" + file + "', slaId = '" + slaId + "'");
        }
        if(!file.isFile()) {
            throw new IllegalArgumentException("The file '" 
                    + file.getAbsolutePath() + "' is not a valid file.");
        }
        // extract the filename for better verification.
        String fileId = idForFile;
        if(fileId == null || fileId.isEmpty()) {
            log.warn("The file id is invalid. Using the name of the file.");
            fileId = file.getName();
        }
        
        log.info("Initialising the put of file '" + fileId + "' at location '"
                + file.getAbsolutePath() + "' by identifying which pillars "
                + "belong to the corresponding SLA '" + slaId + "'.");
        
        // Send request for identifying pillars.
        IdentifyPillarsForPutFileRequest identifyMsg 
                = makeIdentifyPillarsMessage(fileId, slaId);
        try {
            messageBus.sendMessage(queue, identifyMsg);
        } catch (Exception e) {
            throw new ModifyException("Cannot send message.", e);
        }
        
        // Store information about the file and SLA.
        fileIds.put(fileId, new FileIdForPut(fileId, file, slaId));
    }
    
    /**
     * Handles replies from pillars, which has are ripe and ready for storing
     * a file.
     * 
     * @param msg The IdentifyPillarsForPutFileReply message to be handled.
     */
    @Override
    final synchronized void identifyReply(IdentifyPillarsForPutFileReply msg) {
        String fileId = msg.getFileID();
        // validate the content of the message
        if(!fileIds.containsKey(fileId)) {
            log.warn("Could not handle reply message for putting file '"
                    + fileId + "', since no such file is known.");
            return;
        }
        // verify that the slaId for the fileId is the same as the message.
        if(!fileIds.get(fileId).getSlaId().equals(msg.getSlaID())) {
            // TODO handle this scenario differently? Probably send alarm.
            log.warn("The known SLA for the file '" + fileId + "' is not "
                    + "identical to the SLA from the reply message. "
                    + "Expected : '" + fileIds.get(fileId).getSlaId() 
                    + "', but was '" + msg.getSlaID() 
                    + "'. WE DO NOT PROCEED!");
            return;
        }
        
        File file = fileIds.get(fileId).getFile();
        URL url = fileIds.get(fileId).getUrl();
        String pillarId = msg.getPillarID();
        
        // create the putMessage and set file to be outstanding at the pillar.
        PutFileRequest putMsg = getPutFileRequestMessage(file, fileId, url, 
                pillarId, msg.getSlaID());
        outstandings.insertEntry(fileId, pillarId);
        
        try {
            // send the put message.
            messageBus.sendMessage(queue, putMsg);
        } catch (Exception e) {
            throw new ModifyException("Problems sending the message '" 
                    + putMsg + "'.", e);
        }
    }
    
    /**
     * Method for handling a PutFileResponse. This message tells how far in the
     * storage process the given pillar is. 
     * It is possible for the pillars have a storage procedure which involves
     * several steps before the file is properly stored. After each step one of
     * these PutFileResponse messages should be sent, and only when the storage
     * process is finished should the final PutFileComplete message be sent.
     * 
     * @param msg The PutFileResponse to be handled.
     */
    @Override
    final synchronized void handlePutResponse(PutFileResponse msg) {
        // TODO Perform the actual handling of this message! 
        log.debug("The pillar '" + msg.getPillarID() + "' is in the process "
                + "of storing the file '" + msg.getFileID() + "' and has sent "
                + "the following response status code '" + msg.getResponseCode() 
                + "' along with the text: " + msg.getResponseText());
    }
    
    @Override
    final synchronized void handlePutComplete(PutFileComplete msg) {
        log.debug("The pillar '" + msg.getPillarID() + "' has finished storing "
                + "the file '" + msg.getFileID() + "'");
        
        outstandings.removeEntry(msg.getFileID(), msg.getPillarID());
    }

    
    /**
     * Method for creating a PutFileRequest message for a specific file.
     * 
     * Needs to be specified:
     * <br/>- SLA id
     * <br/>- pillar id
     * <br/>- url to the file
     * <br/>- file id
     * 
     * @param file The file to be put.
     * @param fileId The id for the file to be put.
     * @param url The url to the position where the file can be retrieved by 
     * the pillar receiving this message.
     * @param pillarID The id of the pillar which should receive this message.
     * @param slaID The id of the SLA which the file belongs to.
     * @return The PutFileRequest for this file.
     */
    private PutFileRequest getPutFileRequestMessage(File file, String fileId, 
            URL url, String pillarID, String slaID) {
        PutFileRequest putMsg = new PutFileRequest();
        if(USE_CHECKSUM) {
            ChecksumForCheck check = new ChecksumForCheck();
            check.setFileChecksumType(DEFAULT_CHECKSUM_TYPE);
            check.setChecksum(ChecksumUtils.getChecksum(DEFAULT_CHECKSUM_TYPE, 
                    file));
            putMsg.setChecksumForCheck(check);
        }
        putMsg.setCorrelationID("THE-CORRELATION-ID");
        putMsg.setExpectedFileSize(BigInteger.valueOf(file.length()));
        putMsg.setReplyTo(queue);
        if(USE_SALT) {
            SaltForCheck salt = new SaltForCheck();
            salt.setSaltChecksumType(DEFAULT_CHECKSUM_TYPE);
            salt.setSaltParameter(DEFAULT_SALT);
            putMsg.setSaltForCheck(salt);
        }
        putMsg.setMinVersion((short) 1);
        putMsg.setVersion((short) 1);
        
        putMsg.setFileAddress(url.toExternalForm());
        putMsg.setFileID(fileId);
        putMsg.setPillarID(pillarID);
        putMsg.setSlaID(slaID);

        return putMsg;
    }
    
    /**
     * Method for creating the message for identifying the pillars which should
     * receive the put for a given file belonging to a specific SLA.
     * 
     * @param fileId The id of the file.
     * @param slaId The id of the SLA.
     * @return The message for identifying the pillars for a put file request.
     */
    private IdentifyPillarsForPutFileRequest makeIdentifyPillarsMessage(
            String fileId, String slaId) {
        IdentifyPillarsForPutFileRequest identifyMsg
                = new IdentifyPillarsForPutFileRequest();
        identifyMsg.setCorrelationID("SOME-CORRELATION-ID");
        identifyMsg.setFileID(fileId);
        identifyMsg.setReplyTo(queue);
        identifyMsg.setSlaID(slaId);
        identifyMsg.setMinVersion((short) 1);
        identifyMsg.setVersion((short) 1);

        return identifyMsg;
    }
    
    /**
     * Class for keeping track of which files are outstanding at which pillars.
     * It is currently only being handled in the memory. 
     * TODO use a more permanent storage format for keeping track of these 
     * outstanding files, like a file or a database.
     */
    class OutstandingPutFiles {
        /** The map for keeping track of the outstanding files and pillars.
         * Maps between a fileId and a list of pillarIds.*/
        Map<String, List<String>> outstandingPutFiles 
                = Collections.synchronizedMap(new HashMap<String, 
                        List<String>>());
        
        /** Default constructor.*/
        public OutstandingPutFiles() { }
        
        /**
         * Method for inserting a entry for a file being put to a specific 
         * pillar.
         * 
         * @param fileId The id of the file to be put.
         * @param pillarId The id of the pillar, where the file is being put.
         */
        public void insertEntry(String fileId, String pillarId) {
            // TODO validate arguments?
            
            // Retrieve the list of pillars outstanding for this file. If the
            // the file is not yet outstanding, then create a new list for the 
            // file.
            List<String> pillarIds;
            if(outstandingPutFiles.containsKey(fileId)) {
                pillarIds = outstandingPutFiles.get(fileId);
                if(pillarIds.contains(pillarId)) {
                    // TODO handle the scenario, when it is already known, that
                    // a pillar is missing this file.
                    log.warn("The pillar '" + pillarId + "' is already missing "
                            + " the file '" + fileId + "'");
                    return;
                }
            } else {
                pillarIds = new ArrayList<String>();
            }
            
            // put the pillar on the list, and insert it into the outstanding.
            pillarIds.add(pillarId);
            outstandingPutFiles.put(fileId, pillarIds);
        }
        
        /**
         * Method for telling that a file is no longer outstanding at a given
         * pillar. 
         * 
         * @param fileId The file which is no longer outstanding at the given
         * pillar.
         * @param pillarId The pillar where the file is no longer outstanding.
         */
        public void removeEntry(String fileId, String pillarId) {
            // TODO validate arguments?
            
            if(!outstandingPutFiles.containsKey(fileId)) {
                // TODO handle this. Perhaps throw exception?
                log.error("The file '" + fileId + "' is not known to be "
                        + "outstanding anywhere. Not even at '" + pillarId 
                        + "' where it is set to no longer being outstanding.");
                return;
            }
            
            List<String> pillarIds = outstandingPutFiles.remove(fileId);
            if(!pillarIds.contains(pillarId)) {
                // TODO handle this. Perhaps throw exception?
                log.error("The file '" + fileId + "' is known to be "
                        + "outstanding, but not at '" + pillarId + "' where it "
                        + "is set to be removed from the outstanding.");
            } else {
                // remove the pillar
                pillarIds.remove(pillarId);
                log.debug("The file '" + fileId + "' is no longer considered "
                        + "outstanding at pillar '" + pillarId + "'.");
            }
            
            // reinsert the list of outstanding pillars, unless not more pillars
            // are outstanding.
            if(!pillarIds.isEmpty()) {
                outstandingPutFiles.put(fileId, pillarIds);
                log.debug("The file '" + fileId + "' is still outstanding at '"
                        + pillarIds + "'");
            } else {
                log.debug("The file '" + fileId + "' is no longer outstanding "
                        + "at any pillar, and the put is therefore completed.");
            }
        }
        
        /**
         * Method for telling whether a given file is outstanding anywhere.
         * 
         * @param fileId The id of the file, which might be outstanding.
         * @return Whether the file is actually outstanding.
         */
        public boolean isOutstanding(String fileId) {
            // TODO validate argument?
            return outstandingPutFiles.containsKey(fileId);
        }
        
        /**
         * Method for telling whether a given file is outstanding at a specific
         * pillar. 
         * 
         * @param fileId The id of the file, which might be outstanding at the 
         * pillar.
         * @param pillarId The id of the pillar, where the file might be 
         * outstanding.
         * @return Whether the file is outstanding at the given pillar.
         */
        public boolean isOutstandingAtPillar(String fileId, String pillarId) {
            // TODO validate arguments?
            if(!outstandingPutFiles.containsKey(fileId)) {
                return false;
            }
            return outstandingPutFiles.get(fileId).contains(pillarId);
        }
    }
}
