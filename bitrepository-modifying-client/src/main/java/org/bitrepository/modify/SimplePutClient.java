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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumsTypeTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest.ChecksumForCheck;
import org.bitrepository.bitrepositorymessages.PutFileRequest.SaltForCheck;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.http.HTTPFileExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the PutClient.
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
    
    // TODO make the following into a data structure instead of three maps.
    /** Map for keeping track of which file IDs belong to which SLA.*/
    private Map<String, String> fileIdToSla = Collections.synchronizedMap(
            new HashMap<String, String>());
    /** Map for correspondence between file ID and file.*/
    private Map<String, File> fileIdToFile = Collections.synchronizedMap(
            new HashMap<String, File>());
    /** Map for keeping track of the location where a file with the given file 
     * Id has been uploaded.*/
    private Map<String, URL> fileIdToUrl = Collections.synchronizedMap(
            new HashMap<String, URL>());
    
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
     * @param fileId The fileId for the file.
     * @param slaId The SLA id for identifying the pillars which should receive
     * this file.
     * @throws Exception If something goes wrong with the connection.
     */
    @Override
    public final synchronized void putFileWithId(File file, String fileId, 
            String slaId) {
        if(file == null || slaId == null || slaId.isEmpty()) {
            throw new IllegalArgumentException("Unacceptable argument: "
                    + "fil = '" + file + "', slaId = '" + slaId + "'");
        }
        if(!file.isFile()) {
            throw new IllegalArgumentException("The file '" 
                    + file.getAbsolutePath() + "' is not a valid file.");
        }
        if(fileId == null || fileId.isEmpty()) {
            log.warn("The file id is invalid. Using the name of the file.");
            fileId = file.getName();
        }
        
        log.info("Initialising the put of file '" + fileId + "' at location '"
                + file.getAbsolutePath() + "' by identifying which pillars "
                + "belong to the corresponding SLA '" + slaId + "'.");
        
        // Send request for identifying pillars.
        IdentifyPillarsForPutFileRequest identifyMsg 
                = new IdentifyPillarsForPutFileRequest();
        identifyMsg.setCorrelationID("SOME-CORRELATION-ID");
        identifyMsg.setFileID(fileId);
        identifyMsg.setReplyTo(queue);
        identifyMsg.setSlaID(slaId);
        identifyMsg.setMinVersion((short) 1);
        identifyMsg.setVersion((short) 1);
        try {
            messageBus.sendMessage(queue, identifyMsg);
        } catch (Exception e) {
            throw new ModifyException("Cannot send message.", e);
        }
        
        // Store information about the file and SLA.
        fileIdToSla.put(fileId, slaId);
        fileIdToFile.put(fileId, file);
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
        if(!fileIdToFile.containsKey(fileId) 
                || !fileIdToSla.containsKey(fileId)) {
            log.warn("Could not handle reply message for putting file '"
                    + fileId + "', since no such file is known.");
            return;
        }
        if(!fileIdToSla.get(fileId).equals(msg.getSlaID())) {
            // TODO handle this scenario differently? Probably send alarm.
            log.warn("The known SLA for the file '" + fileId + "' is not "
                    + "identical to the SLA from the reply message. "
                    + "Expected : '" + fileIdToSla.get(fileId) + "', but was '"
                    + msg.getSlaID() + "'. WE DO NOT PROCEED!");
            return;
        }
        
        File file = fileIdToFile.get(fileId);
        URL url = getUrl(fileId);
        
        // create the putMessage.
        PutFileRequest putMsg = getPutFileRequestMessage(file, fileId, url, 
                msg.getPillarID(), msg.getSlaID());
        
        try {
            // send the put message.
            messageBus.sendMessage(queue, putMsg);
        } catch (Exception e) {
            throw new ModifyException("Problems sending the message '" 
                    + putMsg + "'.", e);
        }
    }
    
    /**
     * Method for storing the URL for a file which has been uploaded.
     * 
     * @param fileId The id for the file.
     * @return The URL for the location where the file has been uploaded.
     */
    private URL getUrl(String fileId) {
        // check whether it already has been uploaded to a server.
        if(!fileIdToUrl.containsKey(fileId)) {
            try {
                // get file, upload it and store the URL.
                File file = fileIdToFile.get(fileId);
                URL url = HTTPFileExchange.uploadToServer(file);
                fileIdToUrl.put(fileId, url);
            } catch (Exception e) {
                throw new ModifyException("Could not upload the file '"
                        + fileId + "'.", e);
            }
        }
        return fileIdToUrl.get(fileId);
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
}
