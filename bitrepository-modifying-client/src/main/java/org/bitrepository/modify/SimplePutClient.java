/*
 * #%L
 * Bitmagasin modify client
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.modify_client.configuration.ModifyConfiguration;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the PutClient.
 * 
 * TODO perhaps merge the 'outstanding' and the 'FileIdForPut'?
 */
public class SimplePutClient extends PutClientAPI implements PutClientExternalAPI {
    /** The log for this class.*/
    private final Logger log = LoggerFactory.getLogger(SimplePutClient.class);
    
    /** The server, which handles the messages.*/
    private final PutClientMessageListener listener;
    /** The queue where the messages are sent.*/
    private final String queue;
    /** The message bus which is used for all the communication.*/
    private final MessageBus messageBus;
//    
//    
//    
//    /** Which type of checksum to use. TODO define in settings.*/
//    private static final ChecksumsTypeTYPE DEFAULT_CHECKSUM_TYPE 
//            = ChecksumsTypeTYPE.MD_5;
    
    /** Map for keeping track of the data for a fileId. */
    private Map<String, FileIdForPut> fileIds = Collections.synchronizedMap(
            new HashMap<String, FileIdForPut>());
    
    /** The container for keeping track of which files are outstanding and at which pillars they are outstanding. */
    OutstandingPutFiles outstandings = new OutstandingPutFiles();
    /** The configuration for this module.*/
    private final ModifyConfiguration config;
    
    /**
     * Constructor.
     */
    public SimplePutClient() {
        log.info("Initialising the PutClient");
        
        config = ModifyComponentFactory.getInstance().getConfig();
        
        // instantiate the messagelistener and put it onto the queue.
        listener = new PutClientMessageListener(this);
        queue = config.getQueue();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
        messageBus.addListener(queue, listener);
    }
    
    /**
     * Method for putting a file to all pillars for a given SLA.
     * 
     * @param file The file to put.
     * @param idForFile The fileId for the file.
     * @param slaId The SLA id for identifying the pillars which should receive this file.
     * @throws Exception If something goes wrong with the connection.
     */
    @Override
    public final synchronized void putFileWithId(File file, String idForFile, String slaId) {
        if(file == null || slaId == null || slaId.isEmpty()) {
            throw new IllegalArgumentException("Unacceptable argument: fil = '" + file + "', slaId = '" + slaId + "'");
        }
        if(!file.isFile()) {
            throw new IllegalArgumentException("The file '" + file.getAbsolutePath() + "' is not a valid file.");
        }
        // extract the filename for better verification.
        String fileId = idForFile;
        if(fileId == null || fileId.isEmpty()) {
            // TODO perhaps throw exception instead?
            log.warn("The file id is invalid. Using the name of the file.");
            fileId = file.getName();
        }
        
        log.info("Initialising the put of file '" + fileId + "' at location '" + file.getAbsolutePath() + "' by "
                + "identifying which pillars belong to the corresponding SLA '" + slaId + "'.");
        
        // Send request for identifying pillars.
        IdentifyPillarsForPutFileRequest identifyMsg = makeIdentifyPillarsMessage(fileId, slaId);
        messageBus.sendMessage(queue, identifyMsg);
        
        // Store information about the file and SLA.
        fileIds.put(fileId, new FileIdForPut(fileId, file, slaId));
    }
    
    /**
     * Handles replies from pillars, which has are ripe and ready for storing a file.
     * 
     * @param msg The IdentifyPillarsForPutFileReply message to be handled.
     */
    @Override
    final synchronized void identifyResponse(IdentifyPillarsForPutFileResponse msg) {
        // TODO !
//        String fileId = msg.getFileID();
//        // validate the content of the message
//        if(!fileIds.containsKey(fileId)) {
//            log.warn("Could not handle reply message for putting file '" + fileId + "', since no such file is known.");
//            return;
//        }
//        // verify that the slaId for the fileId is the same as the message.
//        if(!fileIds.get(fileId).getSlaId().equals(msg.getSlaID())) {
//            // TODO handle this scenario differently? Probably send alarm.
//            log.warn("The known SLA for the file '" + fileId + "' is not identical to the SLA from the reply message. "
//                    + "Expected : '" + fileIds.get(fileId).getSlaId() + "', but was '" + msg.getSlaID() 
//                    + "'. WE DO NOT PROCEED!");
//            return;
//        }
//        
//        File file = fileIds.get(fileId).getFile();
//        URL url = fileIds.get(fileId).getUrl();
//        String pillarId = msg.getPillarID();
//        
//        // create the putMessage and set file to be outstanding at the pillar.
//        PutFileRequest putMsg = getPutFileRequestMessage(file, fileId, url, pillarId, msg.getSlaID());
//        outstandings.insertEntry(fileId, pillarId);
//        
//        try {
//            // send the put message.
//            messageBus.sendMessage(queue, putMsg);
//        } catch (Exception e) {
//            throw new ModifyException("Problems sending the message '" + putMsg + "'.", e);
//        }
    }
    
    /**
     * Method for handling a PutFileProgressResponse. This message tells how far in the storage process the given pillar is. 
     * It is possible for the pillars have a storage procedure which involves several steps before the file is 
     * properly stored. After each step one of these PutFileProgressResponse messages should be sent, and only when the 
     * storage process is finished should the final PutFileFinalResponse message be sent.
     * 
     * @param msg The PutFileProgressResponse to be handled.
     */
    @Override
    final synchronized void handlePutProgressResponse(PutFileProgressResponse msg) {
        ProgressResponseInfo info = msg.getProgressResponseInfo();
        // TODO Perform the actual handling of this message! 
        log.debug("The pillar '" + msg.getPillarID() + "' is in the process of storing the file '" + msg.getFileID() 
                + "' and has sent the following response status code '" + info.getProgressResponseCode() 
                + "' along with the text: " + info.getProgressResponseText());
    }
    
    /**
     * Method for handling a PutFileFinalResponse message.
     * The file is removed as outstanding on the given pillar.
     * 
     * @param msg The PutFileFinalResponse message to be handled.
     */
    @Override
    final synchronized void handlePutFinalResponse(PutFileFinalResponse msg) {
        log.debug("The pillar '" + msg.getPillarID() + "' has finished storing the file '" + msg.getFileID() + "'");
        
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
     * @param url The url to the position where the file can be retrieved by the pillar receiving this message.
     * @param pillarID The id of the pillar which should receive this message.
     * @param slaID The id of the SLA which the file belongs to.
     * @return The PutFileRequest for this file.
     */
    private PutFileRequest getPutFileRequestMessage(File file, String fileId, URL url, String pillarID, String slaID) {
        PutFileRequest putMsg = new PutFileRequest();
        if(config.isUseChecksum()) {
            // TODO !!
//            ChecksumForCheck check = new ChecksumForCheck();
//            check.setFileChecksumType(DEFAULT_CHECKSUM_TYPE);
//            check.setChecksum(getChecksum(DEFAULT_CHECKSUM_TYPE, file));
//            putMsg.setChecksumForCheck(check);
        }
        putMsg.setCorrelationID("THE-CORRELATION-ID");
        putMsg.setFileSize(BigInteger.valueOf(file.length()));
        putMsg.setReplyTo(queue);
        if(config.isUseSalt()) {
            // TODO !!
//            SaltForCheck salt = new SaltForCheck();
//            salt.setSaltChecksumType(DEFAULT_CHECKSUM_TYPE);
//            salt.setSaltParameter(config.getSalt());
//            putMsg.setSaltForCheck(salt);
        }
        putMsg.setMinVersion(BigInteger.valueOf(1L));
        putMsg.setVersion(BigInteger.valueOf(1L));
        
        putMsg.setFileAddress(url.toExternalForm());
        putMsg.setFileID(fileId);
        putMsg.setPillarID(pillarID);
        putMsg.setBitrepositoryContextID(slaID);

        return putMsg;
    }
    
    /**
     * Method for creating the message for identifying the pillars which should receive the put for a given file 
     * belonging to a specific SLA.
     * 
     * @param fileId The id of the file.
     * @param slaId The id of the SLA.
     * @return The message for identifying the pillars for a put file request.
     */
    private IdentifyPillarsForPutFileRequest makeIdentifyPillarsMessage(String fileId, String slaId) {
        IdentifyPillarsForPutFileRequest identifyMsg = new IdentifyPillarsForPutFileRequest();
        identifyMsg.setCorrelationID("SOME-CORRELATION-ID");
        identifyMsg.setReplyTo(queue);
        identifyMsg.setBitrepositoryContextID(slaId);
        identifyMsg.setMinVersion(BigInteger.valueOf(1L));
        identifyMsg.setVersion(BigInteger.valueOf(1L));

        return identifyMsg;
    }
    
//    
//    /**
//     * Calculates the checksum for a file based on the given checksums type. The type of checksum is used for 
//     * finding the corresponding algorithm, which is used for the actual calculation of the checksum.
//     * The answer is delivered in hexadecimal form.
//     * 
//     * @param type The type of checksum.
//     * @param file The file to calculate the checksum for.
//     * @return The checksum in hexadecimal form.
//     */
//    public static String getChecksum(ChecksumsTypeTYPE type, File file) {
//        try {
//            MessageDigest md;
//            if(type.equals(ChecksumsTypeTYPE.MD_5)) {
//                md = MessageDigest.getInstance("MD5");
//            } else if(type.equals(ChecksumsTypeTYPE.SHA_3)) {
//                md = MessageDigest.getInstance("SHA");
//            } else {
//                throw new IllegalArgumentException("The digest is not supported! . . . Yet at least.");
//            }
//            
//            return ChecksumUtils.generateChecksum(file, md);
//        } catch (NoSuchAlgorithmException e) {
//            throw new ModifyException("Could not digest checksum algorith '" + type.value() + "'.", e);
//        }
//    }
}
