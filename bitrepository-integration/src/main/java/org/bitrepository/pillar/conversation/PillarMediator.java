/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AccessComponentFactory.java 212 2011-07-05 10:04:10Z bam $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/AccessComponentFactory.java $
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
package org.bitrepository.pillar.conversation;

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.pillar.AuditTrailHandler;
import org.bitrepository.pillar.PillarSettings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.pillar.ReferencePillarMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This instance handles the conversations for the reference pillar.
 * It only responds to requests. It does not it self start conversations, though it might send Alarms when something 
 * is not right.
 * All other messages than requests are considered garbage.
 * Every message (even garbage) is put into the audit trails.
 */
public class PillarMediator implements MessageListener {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The settings.*/
    final PillarSettings settings;
    /** The messagebus. Package protected on purpose.*/
    final MessageBus messagebus;
    /** The archive. Package protected on purpose.*/
    final ReferenceArchive archive;
    /** The message factory. Package protected on purpose.*/
    final ReferencePillarMessageFactory msgFactory;
    /** The handler of the audits. Package protected on purpose.*/
    private final AuditTrailHandler audits;

    // THE MESSAGE HANDLERS!
    /** The handler for the messages regarding access to the archive (e.g. Get operations).*/
    private final AccessMessageHandler accessHandler;
    /** The handler for the messages regarding modification of the archive (e.g. Put, Delete, etc.). */
    private final ModifyMessageHandler modifyHandler;
    
    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
     * 
     * @param messagebus The messagebus for this instance.
     * @param pSettings The settings for the reference pillar.
     * @param refArchive The archive for the reference pillar.
     * @param messageFactory The message factory.
     */
    public PillarMediator(MessageBus messagebus, PillarSettings pSettings, 
            ReferenceArchive refArchive, ReferencePillarMessageFactory messageFactory) {
        this.messagebus = messagebus;
        this.archive = refArchive;
        this.msgFactory = messageFactory;
        this.settings = pSettings;
        this.audits = new AuditTrailHandler();
        this.accessHandler = new AccessMessageHandler(this);
        this.modifyHandler = new ModifyMessageHandler(this);
        
        // add to both the general topic and the local queue.
        messagebus.addListener(settings.getBitRepositoryCollectionTopicID(), this);
        messagebus.addListener(settings.getLocalQueue(), this);
    }

    public void handleException(Exception e) {
        // TODO ?? send alarm?
        log.error("Received excepton '" + e.getMessage() + "'.", e);
    }

    @Override
    public void onMessage(Alarm message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetAuditTrailsRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        // TODO Auto-generated method stub
    }

    @Override
    public void onMessage(GetAuditTrailsProgressResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetAuditTrailsFinalResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetChecksumsFinalResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetChecksumsRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        
        try {
            accessHandler.handleMessage(message);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onMessage(GetChecksumsProgressResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetFileFinalResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetFileIDsFinalResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        
        try {
            accessHandler.handleMessage(message);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onMessage(GetFileIDsProgressResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetFileRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        
        try {
            accessHandler.handleMessage(message);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onMessage(GetFileProgressResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetStatusRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMessage(GetStatusProgressResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(GetStatusFinalResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        
        try {
            accessHandler.handleMessage(message);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        
        try {
            accessHandler.handleMessage(message);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        
        try {
            accessHandler.handleMessage(message);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        
        try {
            modifyHandler.handleMessage(message);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onMessage(PutFileFinalResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }

    @Override
    public void onMessage(PutFileRequest message) {
        log.info("Received: " + message);
        audits.insertAudit(message);
        
        try {
            modifyHandler.handleMessage(message);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onMessage(PutFileProgressResponse message) {
        log.warn("Should not have received a '" + message.getClass().getName() + "' message. Content: " + message);
        audits.insertAudit(message);
    }
}
