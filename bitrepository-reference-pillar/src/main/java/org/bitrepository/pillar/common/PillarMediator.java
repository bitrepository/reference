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
package org.bitrepository.pillar.common;

import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.settings.collectionsettings.AlarmLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the abstract instance for delegating the conversations for the pillar.
 * It only responds to requests. It does not it self start conversations, though it might send Alarms when something 
 * is not right.
 * All other messages than requests are considered garbage.
 */
public abstract class PillarMediator extends AbstractMessageListener {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The context for the mediator.*/
    private final PillarContext context;

    // THE MESSAGE HANDLERS!
    /** The map between the messagenames and their respective handler.*/
    @SuppressWarnings("rawtypes")
    protected Map<String, PillarMessageHandler> handlers = new HashMap<String, PillarMessageHandler>();

    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
     * 
     * @param messagebus The messagebus for this instance.
     * @param settings The settings for the reference pillar.
     * @param refArchive The archive for the reference pillar.
     */
    public PillarMediator(PillarContext context) {
        ArgumentValidator.checkNotNull(context, "PillarContext context");

        this.context = context;
        
        // add to both the general topic and the local queue.
        context.getMessageBus().addListener(context.getSettings().getCollectionDestination(), this);
        context.getMessageBus().addListener(
                context.getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination(), this);
    }
    
    /**
     * Method for instantiating the handler.
     */
    protected abstract void initialiseHandlers(PillarContext context); 
    
    /**
     * Method for sending an alarm when a received message does not have a handler.
     * 
     * @param message The message which does not have a handler.
     */
    protected void noHandlerAlarm(Object message) {
        String msg = "Cannot handle message of type '" + message.getClass().getCanonicalName() + "'";
        log.warn(msg + ": " + message.toString());
        
        // create a descriptor.
        Alarm ad = new Alarm();
        ad.setAlarmCode(AlarmCode.FAILED_OPERATION);
        ad.setAlarmText(msg);
        
        context.getAlarmDispatcher().sendAlarm(ad);
    }
    
    @Override
    protected void reportUnsupported(Object message) {
        if(AlarmLevel.WARNING.equals(context.getSettings().getCollectionSettings().getPillarSettings().getAlarmLevel())) {
            noHandlerAlarm(message);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void onMessage(Message message) {
        log.info("Received Message of super type <MESSAGE>: " + message);
        
        PillarMessageHandler handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(DeleteFileRequest message) {
        log.info("Received: " + message);

        PillarMessageHandler<DeleteFileRequest> handler = handlers.get(message.getClass().getName());
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

        PillarMessageHandler<GetAuditTrailsRequest> handler = handlers.get(message.getClass().getName());
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

        PillarMessageHandler<GetChecksumsRequest> handler = handlers.get(message.getClass().getName());
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

        PillarMessageHandler<GetFileIDsRequest> handler = handlers.get(message.getClass().getName());
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

        PillarMessageHandler<GetFileRequest> handler = handlers.get(message.getClass().getName());
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

        PillarMessageHandler<GetStatusRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }    
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(IdentifyContributorsForGetAuditTrailsRequest message) {
        log.info("Received: " + message);

        PillarMessageHandler<IdentifyContributorsForGetAuditTrailsRequest> handler = handlers.get(
                message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(IdentifyContributorsForGetStatusRequest message) {
        log.info("Received: " + message);

        PillarMessageHandler<IdentifyContributorsForGetStatusRequest> handler = handlers.get(
                message.getClass().getName());
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

        PillarMessageHandler<IdentifyPillarsForDeleteFileRequest> handler = handlers.get(
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

        PillarMessageHandler<IdentifyPillarsForGetChecksumsRequest> handler 
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

        PillarMessageHandler<IdentifyPillarsForGetFileIDsRequest> handler = handlers.get(
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

        PillarMessageHandler<IdentifyPillarsForGetFileRequest> handler = handlers.get(
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

        PillarMessageHandler<IdentifyPillarsForPutFileRequest> handler = handlers.get(
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

        PillarMessageHandler<IdentifyPillarsForReplaceFileRequest> handler = handlers.get(
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

        PillarMessageHandler<PutFileRequest> handler = handlers.get(message.getClass().getName());
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

        PillarMessageHandler<ReplaceFileRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }
    }

    /**
    * Closes the mediator by removing all the message handler.
    */
    public void close() {
        handlers.clear();
        // removes to both the general topic and the local queue.
        context.getMessageBus().removeListener(context.getSettings().getCollectionDestination(), this);
        context.getMessageBus().removeListener(context.getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination(), this);
    }
}
