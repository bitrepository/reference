/*
 * #%L
 * Bitrepository Integration
 * 
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

import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

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
import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.settings.collectionsettings.AlarmLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class for dispatching alarms.
 */
public class AlarmDispatcher {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The settings for this AlarmDispatcher.*/
    private final Settings settings;
    
    /** The messagebus for communication.*/
    private final MessageBus messageBus;
    
    /**
     * Constructor.
     * @param settings The settings for the dispatcher.
     * @param messageBus The bus for sending the alarms.
     */
    public AlarmDispatcher(Settings settings, MessageBus messageBus) {
        this.settings = settings;
        this.messageBus = messageBus;
    }
    
    /**
     * Method for sending an alarm based on an IllegalArgumentException.
     * Is only send if the alarm level is 'WARNING', otherwise the exception is just logged.
     * @param exception The exception to base the alarm upon.
     */
    public void handleIllegalArgumentException(IllegalArgumentException exception) {
        if(settings.getCollectionSettings().getPillarSettings().getAlarmLevel() != AlarmLevel.WARNING) {
            log.warn("IllegalArgumentException caught, but we do not issue alarms for this, when the alarm level is '"
                    + settings.getCollectionSettings().getPillarSettings().getAlarmLevel() + "'", exception);
            return;
        }
        
        // create the Concerning part of the alarm.
        AlarmConcerning ac = createAlarmConcerning();
        ac.setMessages(exception.getMessage());
        ac.setFileInformation(null);
        
        // create a descriptor.
        AlarmDescription ad = new AlarmDescription();
        ad.setAlarmCode(AlarmcodeType.FAILED_OPERATION); //TODO Jonas see if this should be changed to another type
        ad.setAlarmText(exception.getMessage());
        ad.setOrigDateTime(CalendarUtils.getXmlGregorianCalendar(new Date()));
        ad.setPriority(PriorityCodeType.OTHER);
        RiskTYPE rt = new RiskTYPE();
        rt.setRiskArea(RiskAreaType.CONFIDENTIALITY);
        rt.setRiskImpactScore(RiskImpactScoreType.CRITICAL_IMPACT);
        rt.setRiskProbabilityScore(RiskProbabilityScoreType.HIGH_PROPABILITY);
        ad.setRisk(rt);
        
        sendAlarm(ac, ad);
    }
    
    /**
     * Sends an alarm for a RuntimeException. Such exceptions are sent unless the AlarmLevel is 'EMERGENCY',
     * otherwise the exception is just logged.
     * @param exception The exception causing the alarm.
     */
    public void handleRuntimeExceptions(RuntimeException exception) {
        if(settings.getCollectionSettings().getPillarSettings().getAlarmLevel() == AlarmLevel.EMERGENCY) {
            log.error("RuntimeException caught, but we do not issue alarms for this, when the alarm level is '"
                    + settings.getCollectionSettings().getPillarSettings().getAlarmLevel() + "'", exception);
            return;
        }
        
        log.error("Sending alarm for RunTimeException", exception);
        
        // create the Concerning part of the alarm.
        AlarmConcerning ac = createAlarmConcerning();
        ac.setMessages(exception.getMessage());
        ac.setFileInformation(null);
        
        // create a descriptor.
        AlarmDescription ad = new AlarmDescription();
        ad.setAlarmCode(AlarmcodeType.COMPONENT_FAILURE);
        ad.setAlarmText(exception.getMessage());
        ad.setOrigDateTime(CalendarUtils.getXmlGregorianCalendar(new Date()));
        ad.setPriority(PriorityCodeType.MANUAL_CHECK);
        RiskTYPE rt = new RiskTYPE();
        // TODO missing types: RiskAreaType.UNKNOWN, RiskImpactScoreType.UNKNOWN, RiskProbabilityScoreType.UNKNOWN 
        rt.setRiskArea(RiskAreaType.SAFETY);
        rt.setRiskImpactScore(RiskImpactScoreType.MEDIUM_IMPACT);
        rt.setRiskProbabilityScore(RiskProbabilityScoreType.HIGH_PROPABILITY);
        ad.setRisk(rt);
        
        sendAlarm(ac, ad);
    }

    /**
     * Method for sending an Alarm when something bad happens.
     * @param alarmConcerning What the alarm is concerning.
     * @param alarmDescription The description of the alarm, e.g. What caused the alarm.
     */
    public void sendAlarm(AlarmConcerning alarmConcerning, AlarmDescription alarmDescription) {
        log.warn("Sending alarm, concerning: '" + alarmConcerning + "', with description: '" + alarmDescription + "'");
        
        Alarm alarm = new Alarm();
        
        ComponentTYPE ct = new ComponentTYPE();
        ct.setComponentComment("ReferencePillar");
        ct.setComponentID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        ct.setComponentType(ComponentType.PILLAR);
        alarm.setAlarmRaiser(ct);
        
        alarm.setAlarmConcerning(alarmConcerning);
        alarm.setAlarmDescription(alarmDescription);
        
        alarm.setCollectionID(settings.getCollectionID());
        alarm.setCorrelationID(UUID.randomUUID().toString());
        alarm.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        alarm.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        alarm.setTo(settings.getAlarmDestination());
        alarm.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        
        messageBus.sendMessage(alarm);
    }

    /**
     * Creates the generic AlarmConcerning for the ReferencePillar.
     * Is missing:
     * <br/> - Messages
     * <br/> - FileInformation
     * @return The generic AlarmConcerning.
     */
    private AlarmConcerning createAlarmConcerning() {
        AlarmConcerning ac = new AlarmConcerning();
        Components comps = new Components();
        ComponentTYPE compType = new ComponentTYPE();
        compType.setComponentComment("ReferencePillar");
        compType.setComponentID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        compType.setComponentType(ComponentType.PILLAR);
        comps.getContributor().add(compType);
        comps.getDataTransmission().add(settings.getMessageBusConfiguration().toString());
        ac.setComponents(comps);

        return ac;
    }
}