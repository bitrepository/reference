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
import org.bitrepository.bitrepositoryelements.AlarmConcerning.BitRepositoryCollections;
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
     * @param exception The exception to base the alarm upon.
     */
    public void alarmIllegalArgument(IllegalArgumentException exception) {
        // create the Concerning part of the alarm.
        AlarmConcerning ac = new AlarmConcerning();
        BitRepositoryCollections brcs = new BitRepositoryCollections();
        brcs.getBitRepositoryCollectionID().add(settings.getCollectionID());
        ac.setBitRepositoryCollections(brcs);
        ac.setMessages(exception.getMessage());
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
        ad.setAlarmCode(AlarmcodeType.UNKNOWN_USER);
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
        
        alarm.setAuditTrailInformation("ReferencePillar: " + 
        settings.getReferenceSettings().getPillarSettings().getPillarID());
        alarm.setBitRepositoryCollectionID(settings.getCollectionID());
        alarm.setCorrelationID(UUID.randomUUID().toString());
        alarm.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        alarm.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        alarm.setTo(settings.getAlarmDestination());
        alarm.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        
        messageBus.sendMessage(alarm);
    }
}