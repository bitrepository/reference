/*
 * #%L
 * Bitrepository Integration
 * 
 * $Id: AlarmDispatcher.java 627 2011-12-09 15:13:13Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/messagehandler/AlarmDispatcher.java $
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
package org.bitrepository.integrityclient.collector.eventhandler;

import java.math.BigInteger;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.AlarmConcerning;
import org.bitrepository.bitrepositoryelements.AlarmConcerning.FileInformation;
import org.bitrepository.bitrepositoryelements.AlarmDescription;
import org.bitrepository.bitrepositoryelements.AlarmcodeType;
import org.bitrepository.bitrepositoryelements.ComponentTYPE;
import org.bitrepository.bitrepositoryelements.PriorityCodeType;
import org.bitrepository.bitrepositoryelements.RiskAreaType;
import org.bitrepository.bitrepositoryelements.RiskImpactScoreType;
import org.bitrepository.bitrepositoryelements.RiskProbabilityScoreType;
import org.bitrepository.bitrepositoryelements.RiskTYPE;
import org.bitrepository.bitrepositoryelements.AlarmConcerning.Components;
import org.bitrepository.bitrepositoryelements.ComponentTYPE.ComponentType;
import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityclient.checking.IntegrityReport;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class for dispatching alarms.
 */
public class IntegrityAlarmDispatcher {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The classpath to the 'xsd'.*/
    protected static final String XSD_CLASSPATH = "xsd/";
    /** The name of the XSD containing the BitRepositoryData elements. */
    protected static final String XSD_BR_MESSAGE = "BitRepositoryMessage.xsd";

    /** The settings for this AlarmDispatcher.*/
    private final Settings settings;
    
    /** The messagebus for communication.*/
    private final MessageBus messageBus;
    
    /**
     * Constructor.
     * @param settings The settings for the dispatcher.
     * @param messageBus The bus for sending the alarms.
     */
    public IntegrityAlarmDispatcher(Settings settings, MessageBus messageBus) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        
        this.settings = settings;
        this.messageBus = messageBus;
    }
    
    /**
     * Sends an alarm based on an integrity report.
     * @param report The report to base the alarm upon.
     */
    public void integrityFailed(IntegrityReport report) {
        AlarmConcerning ac = new AlarmConcerning();
        Components comps = new Components();
        ComponentTYPE compType = new ComponentTYPE();
        compType.setComponentComment("IntegrityService");
        compType.setComponentID("IntegrityService");
        compType.setComponentType(ComponentType.COORLAYER);
        comps.getContributor().add(compType);
        comps.getDataTransmission().add(settings.getMessageBusConfiguration().toString());
        ac.setComponents(comps);
        ac.setMessages(report.generateReport());
        FileInformation fi = new FileInformation();
        fi.setFileIDs(report.getFileIDs());
        ac.setFileInformation(fi);
        
        AlarmDescription ad = new AlarmDescription();
        ad.setAlarmCode(AlarmcodeType.INCONSISTENT_REQUEST);
        ad.setAlarmText(report.generateReport());
        ad.setOrigDateTime(CalendarUtils.getNow());
        if(!report.getChecksumErrors().isEmpty()) {
            ad.setPriority(PriorityCodeType.CHECKSUM_ERROR);
        } else if(!report.getMissingFileIDs().isEmpty()) {
            ad.setPriority(PriorityCodeType.MISSING_IDS);
        } else {
            // TODO ???
            ad.setPriority(PriorityCodeType.MANUAL_CHECK);
        }
        
        RiskTYPE rt = new RiskTYPE();
        // TODO missing types: RiskAreaType.UNKNOWN, RiskImpactScoreType.UNKNOWN, RiskProbabilityScoreType.UNKNOWN 
        rt.setRiskArea(RiskAreaType.SAFETY);
        rt.setRiskImpactScore(RiskImpactScoreType.HIGH_IMPACT);
        rt.setRiskProbabilityScore(RiskProbabilityScoreType.VERY_HIGH_PROPABILITY);
        ad.setRisk(rt);
        
        sendAlarm(ac, ad);
    }
    
    /**
     * Method for sending an Alarm when something bad happens.
     * @param alarmConcerning What the alarm is concerning.
     * @param alarmDescription The description of the alarm, e.g. What caused the alarm.
     */
    public void sendAlarm(AlarmConcerning alarmConcerning, AlarmDescription alarmDescription) {
        ArgumentValidator.checkNotNull(alarmConcerning, "alarmConcerning");
        ArgumentValidator.checkNotNull(alarmDescription, "alarmDescription");
        
        log.warn("Sending alarm, concerning: '" + alarmConcerning + "', with description: '" + alarmDescription + "'");
        
        Alarm alarm = new Alarm();
        
        ComponentTYPE ct = new ComponentTYPE();
        ct.setComponentComment("IntegrityService");
        ct.setComponentID("IntegrityService");
        ct.setComponentType(ComponentType.COORLAYER);
        alarm.setAlarmRaiser(ct);
        
        alarm.setAlarmConcerning(alarmConcerning);
        alarm.setAlarmDescription(alarmDescription);
        
        alarm.setCollectionID(settings.getCollectionID());
        alarm.setCorrelationID(UUID.randomUUID().toString());
        alarm.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        alarm.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        alarm.setTo(settings.getAlarmDestination());
        alarm.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        
        messageBus.sendMessage(alarm);
    }
}