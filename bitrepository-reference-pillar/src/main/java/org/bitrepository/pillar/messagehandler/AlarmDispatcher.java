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
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.JaxbHelper;
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
    public AlarmDispatcher(Settings settings, MessageBus messageBus) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        
        this.settings = settings;
        this.messageBus = messageBus;
    }
    
    /**
     * Method for sending an alarm based on an IllegalArgumentException.
     * Is only send if the alarm level is 'WARNING', otherwise the exception is just logged.
     * @param exception The exception to base the alarm upon.
     */
    public void handleIllegalArgumentException(IllegalArgumentException exception) {
        ArgumentValidator.checkNotNull(exception, "IllegalArgumentException exception");
        if(settings.getCollectionSettings().getPillarSettings().getAlarmLevel() != AlarmLevel.WARNING) {
            log.warn("IllegalArgumentException caught, but we do not issue alarms for this, when the alarm level is '"
                    + settings.getCollectionSettings().getPillarSettings().getAlarmLevel() + "'", exception);
            return;
        }
        
        // create a descriptor.
        Alarm ad = new Alarm();
        ad.setAlarmCode(AlarmCode.FAILED_OPERATION); //TODO Jonas see if this should be changed to another type
        ad.setAlarmText(exception.getMessage());
        
        sendAlarm(ad);
    }
    
    /**
     * Sends an alarm for a RuntimeException. Such exceptions are sent unless the AlarmLevel is 'EMERGENCY',
     * otherwise the exception is just logged.
     * @param exception The exception causing the alarm.
     */
    public void handleRuntimeExceptions(RuntimeException exception) {
        ArgumentValidator.checkNotNull(exception, "RuntimeException exception");
        if(settings.getCollectionSettings().getPillarSettings().getAlarmLevel() == AlarmLevel.EMERGENCY) {
            log.error("RuntimeException caught, but we do not issue alarms for this, when the alarm level is '"
                    + settings.getCollectionSettings().getPillarSettings().getAlarmLevel() + "'", exception);
            return;
        }
        
        log.error("Sending alarm for RunTimeException", exception);
        
        // create a descriptor.
        Alarm alarm = new Alarm();
        alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
        alarm.setAlarmText(exception.getMessage());
        alarm.setAlarmRaiser(settings.getReferenceSettings().getPillarSettings().getPillarID());
        
        sendAlarm(alarm);
    }

    /**
     * Method for sending an Alarm when something bad happens.
     * @param alarmConcerning What the alarm is concerning.
     * @param alarmDescription The description of the alarm, e.g. What caused the alarm.
     */
    public void sendAlarm(Alarm alarm) {
        ArgumentValidator.checkNotNull(alarm, "alarm");
        AlarmMessage message = new AlarmMessage();
        alarm.setAlarmRaiser(settings.getReferenceSettings().getPillarSettings().getPillarID());
        alarm.setOrigDateTime(CalendarUtils.getNow());
        
        message.setCollectionID(settings.getCollectionID());
        message.setCorrelationID(UUID.randomUUID().toString());
        message.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        message.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        message.setTo(settings.getAlarmDestination());
        message.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        
        messageBus.sendMessage(message);
    }
    
    /**
     * Method for creating and sending an Alarm about the checksum being invalid.
     * @param message The 
     * @param fileId
     * @param alarmText
     */
    public void sendInvalidChecksumAlarm(Object message, String fileId, String alarmText) {
        Alarm alarm = new Alarm();
        alarm.setAlarmCode(AlarmCode.CHECKSUM_ALARM);
        alarm.setAlarmText(alarmText);
        alarm.setOrigDateTime(CalendarUtils.getNow());
        sendAlarm(alarm);
    }
}