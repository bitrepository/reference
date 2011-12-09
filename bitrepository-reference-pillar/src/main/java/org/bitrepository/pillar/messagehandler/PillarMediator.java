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

import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.AlarmConcerning;
import org.bitrepository.bitrepositoryelements.AlarmConcerning.Components;
import org.bitrepository.bitrepositoryelements.AlarmDescription;
import org.bitrepository.bitrepositoryelements.AlarmcodeType;
import org.bitrepository.bitrepositoryelements.ComponentTYPE;
import org.bitrepository.bitrepositoryelements.ComponentTYPE.ComponentType;
import org.bitrepository.bitrepositoryelements.PriorityCodeType;
import org.bitrepository.bitrepositoryelements.RiskAreaType;
import org.bitrepository.bitrepositoryelements.RiskImpactScoreType;
import org.bitrepository.bitrepositoryelements.RiskProbabilityScoreType;
import org.bitrepository.bitrepositoryelements.RiskTYPE;
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
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.pillar.audit.MemorybasedAuditTrailManager;
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
public class PillarMediator extends AbstractMessageListener {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The settings.*/
    final Settings settings;
    /** The messagebus. Package protected on purpose.*/
    final MessageBus messagebus;
    /** The archive. Package protected on purpose.*/
    final ReferenceArchive archive;
    /** The handler of the audits. Package protected on purpose.*/
    private final MemorybasedAuditTrailManager audits;
    /** The dispatcher of alarms. Package protected on purpose.*/
    final AlarmDispatcher alarmDispatcher;

    // THE MESSAGE HANDLERS!
    /** The map between the messagenames and their respective handlers.*/
    @SuppressWarnings("rawtypes")
    private Map<String, PillarMessageHandler> handlers = new HashMap<String, PillarMessageHandler>();

    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
     * 
     * @param messagebus The messagebus for this instance.
     * @param settings The settings for the reference pillar.
     * @param refArchive The archive for the reference pillar.
     * @param messageFactory The message factory.
     */
    public PillarMediator(MessageBus messagebus, Settings settings, ReferenceArchive refArchive) {
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
    }

    
    /**
     * Method for sending an alarm when a received message does not have a handler.
     * 
     * @param message The message which does not have a handler.
     */
    private void noHandlerAlarm(Object message) {
        String msg = "Cannot handle message of type '" + message.getClass().getCanonicalName() + "'";
        log.warn(msg + ": " + message.toString());
        
        // create the Concerning part of the alarm.
        AlarmConcerning ac = new AlarmConcerning();
        ac.setMessages(msg);
        ac.setFileInformation(null);
        Components comps = new Components();
        ComponentTYPE compType = new ComponentTYPE();
        compType.setComponentComment("ReferencePillar");
        compType.setComponentID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        compType.setComponentType(ComponentType.PILLAR);
        comps.getContributor().add(compType);
        comps.getDataTransmission().add(settings.getMessageBusConfiguration().toString());
        ac.setComponents(comps);
        
        // create a descriptor.
        AlarmDescription ad = new AlarmDescription();
        ad.setAlarmCode(AlarmcodeType.FAILED_OPERATION);
        ad.setAlarmText(msg);
        ad.setOrigDateTime(CalendarUtils.getNow());
        ad.setPriority(PriorityCodeType.OTHER);
        RiskTYPE rt = new RiskTYPE();
        rt.setRiskArea(RiskAreaType.AVAILABILITY);
        rt.setRiskImpactScore(RiskImpactScoreType.HIGH_IMPACT);
        rt.setRiskProbabilityScore(RiskProbabilityScoreType.MEDIUM_PROPABILITY);
        ad.setRisk(rt);
        
        alarmDispatcher.sendAlarm(ac, ad);
    }
    
    @Override
    protected void reportUnsupported(Object message) {
        audits.addMessageReceivedAudit("Received unsupported: " + message.getClass());
        if(AlarmLevel.WARNING.equals(settings.getCollectionSettings().getPillarSettings().getAlarmLevel())) {
            noHandlerAlarm(message);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(DeleteFileRequest message) {
        log.info("Received: " + message);
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

        PillarMessageHandler<GetStatusRequest> handler = handlers.get(message.getClass().getName());
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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

        PillarMessageHandler<IdentifyPillarsForDeleteFileRequest> handler = handlers.get(message.getClass().getName());
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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

        PillarMessageHandler<IdentifyPillarsForGetChecksumsRequest> handler = handlers.get(message.getClass().getName());
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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

        PillarMessageHandler<IdentifyPillarsForGetFileIDsRequest> handler = handlers.get(message.getClass().getName());
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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message);

        PillarMessageHandler<IdentifyPillarsForGetFileRequest> handler = handlers.get(message.getClass().getName());
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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

        PillarMessageHandler<IdentifyPillarsForPutFileRequest> handler = handlers.get(message.getClass().getName());
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
        audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());

        PillarMessageHandler<PutFileRequest> handler = handlers.get(message.getClass().getName());
        if(handler != null) {
            handler.handleMessage(message);
        } else {
            noHandlerAlarm(message.getClass());
        }
    }
}
