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

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityclient.checking.IntegrityReport;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.hibernate.type.ComponentType;
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
        Alarm ad = new Alarm();
        ad.setAlarmCode(AlarmCode.INCONSISTENT_REQUEST);
        ad.setAlarmText(report.generateReport());
        sendAlarm(ad);
    }
    
    /**
     * Method for sending an Alarm when something bad happens.
     * @param alarmConcerning What the alarm is concerning.
     * @param alarm The description of the alarm, e.g. What caused the alarm.
     */
    public void sendAlarm(Alarm alarm) {
        ArgumentValidator.checkNotNull(alarm, "alarm");

        alarm.setAlarmRaiser("IntegrityService");
        alarm.setOrigDateTime(CalendarUtils.getNow());
        
        log.warn("Sending alarm: " + alarm);
        
        AlarmMessage message = new AlarmMessage();
        
        
        message.setAlarm(alarm);
        
        message.setCollectionID(settings.getCollectionID());
        message.setCorrelationID(UUID.randomUUID().toString());
        message.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        message.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        message.setTo(settings.getAlarmDestination());
        message.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        
        messageBus.sendMessage(message);
    }
}