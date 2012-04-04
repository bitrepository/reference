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

import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.AlarmDispatcher;
import org.bitrepository.pillar.AuditTrailManager;
import org.bitrepository.pillar.audit.MemorybasedAuditTrailManager;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.settings.collectionsettings.AlarmLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This instance handles the conversations for the reference pillar.
 * It only responds to requests. It does not it self start conversations, though it might send Alarms when something 
 * is not right.
 * All other messages than requests are considered garbage.
 * Every message (even garbage) is put into the audit trails.
 */
public class ReferencePillarMediator extends AbstractMessageListener {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The settings.*/
    private final Settings settings;
    /** The messagebus. Package protected on purpose.*/
    private final MessageBus messagebus;
    /** The archive. Package protected on purpose.*/
    private final ReferenceArchive archive;
    /** The handler of the audits. Package protected on purpose.*/
    private final AuditTrailManager audits;
    /** The dispatcher of alarms. Package protected on purpose.*/
    private final AlarmDispatcher alarmDispatcher;

    // THE MESSAGE HANDLERS!
    /** The map between the messagenames and their respective handlers.*/
    @SuppressWarnings("rawtypes")
    private Map<String, ReferencePillarMessageHandler> handlers = new HashMap<String, ReferencePillarMessageHandler>();

    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
     * 
     * @param messagebus The messagebus for this instance.
     * @param settings The settings for the reference pillar.
     * @param refArchive The archive for the reference pillar.
     * @param messageFactory The message factory.
     */
    public ReferencePillarMediator(MessageBus messagebus, Settings settings, ReferenceArchive refArchive) {
        ArgumentValidator.checkNotNull(messagebus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(refArchive, "ReferenceArchive refArchive");

        this.messagebus = messagebus;
        this.archive = refArchive;
        this.settings = settings;
        this.audits = new MemorybasedAuditTrailManager();
        this.alarmDispatcher = new AlarmDispatcher(settings, messagebus);

        // Initialise the messagehandlers.
        initialiseHandlers();

        // add to both the general topic and the local queue.
        messagebus.addListener(settings.getCollectionDestination(), this);
        messagebus.addListener(settings.getReferenceSettings().getPillarSettings().getReceiverDestination(), this);
    }
    
    /**
     * Method for instantiating the handlers.
     */
    private void initialiseHandlers() {
        this.handlers.put(IdentifyPillarsForGetFileRequest.class.getName(), 
                new IdentifyPillarsForGetFileRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(GetFileRequest.class.getName(), 
                new GetFileRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(IdentifyPillarsForGetFileIDsRequest.class.getName(), 
                new IdentifyPillarsForGetFileIDsRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(GetFileIDsRequest.class.getName(), 
                new GetFileIDsRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(IdentifyPillarsForGetChecksumsRequest.class.getName(), 
                new IdentifyPillarsForGetChecksumsRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(GetChecksumsRequest.class.getName(), 
                new GetChecksumsRequestHandler(settings, messagebus, alarmDispatcher, archive));
        
        this.handlers.put(IdentifyPillarsForPutFileRequest.class.getName(), 
                new IdentifyPillarsForPutFileRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(PutFileRequest.class.getName(), 
                new PutFileRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(IdentifyPillarsForDeleteFileRequest.class.getName(), 
                new IdentifyPillarsForDeleteFileRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(DeleteFileRequest.class.getName(), 
                new DeleteFileRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(IdentifyPillarsForReplaceFileRequest.class.getName(), 
                new IdentifyPillarsForReplaceFileRequestHandler(settings, messagebus, alarmDispatcher, archive));
        this.handlers.put(ReplaceFileRequest.class.getName(), 
                new ReplaceFileRequestHandler(settings, messagebus, alarmDispatcher, archive));
    }
    
    /**
     * Method for sending an alarm when a received message does not have a handler.
     * 
     * @param message The message which does not have a handler.
     */
    private void noHandlerAlarm(Object message) {
        String msg = "Cannot handle message of type '" + message.getClass().getCanonicalName() + "'";
        log.warn(msg + ": " + message.toString());
        
        // create a descriptor.
        Alarm ad = new Alarm();
        ad.setAlarmCode(AlarmCode.FAILED_OPERATION);
        ad.setAlarmText(msg);
        
        alarmDispatcher.sendAlarm(ad);
    }
    
    @Override
    protected void reportUnsupported(Object message) {
        audits.addAuditEvent("", "", "", "", FileAction.OTHER);
        if(AlarmLevel.WARNING.equals(settings.getCollectionSettings().getPillarSettings().getAlarmLevel())) {
            noHandlerAlarm(message);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(DeleteFileRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<DeleteFileRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(GetAuditTrailsRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<GetAuditTrailsRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(GetChecksumsRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<GetChecksumsRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(GetFileIDsRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<GetFileIDsRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }    
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(GetFileRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<GetFileRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }    
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(GetStatusRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<GetStatusRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }    
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(IdentifyPillarsForDeleteFileRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<IdentifyPillarsForDeleteFileRequest> handler = handlers.get(
                message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<IdentifyPillarsForGetChecksumsRequest> handler 
                = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }    
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<IdentifyPillarsForGetFileIDsRequest> handler = handlers.get(
                message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }    
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<IdentifyPillarsForGetFileRequest> handler = handlers.get(
                message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<IdentifyPillarsForPutFileRequest> handler = handlers.get(
                message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(IdentifyPillarsForReplaceFileRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<IdentifyPillarsForReplaceFileRequest> handler = handlers.get(
                message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(PutFileRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<PutFileRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(ReplaceFileRequest message) {
        log.info("Received: " + message);

        ReferencePillarMessageHandler<ReplaceFileRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }
    }

    /**
    * Closes the mediator by removing all the message handlers.
    */
    public void close() {
        handlers.clear();
        // removes to both the general topic and the local queue.
        messagebus.removeListener(settings.getCollectionDestination(), this);
        messagebus.removeListener(settings.getReferenceSettings().getPillarSettings().getReceiverDestination(), this);
    }
}
