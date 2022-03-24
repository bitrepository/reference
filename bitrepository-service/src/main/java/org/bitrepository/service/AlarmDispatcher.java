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
package org.bitrepository.service;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.bitrepository.service.contributor.MessageDispatcher;
import org.bitrepository.settings.referencesettings.AlarmLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

/**
 * The class for dispatching alarms.
 */
public class AlarmDispatcher extends MessageDispatcher {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AlarmLevel alarmLevel;

    /**
     * @param sender     Used for sending the alarms.
     * @param settings   The configuration.
     * @param alarmLevel The alarm level, if null set to ERROR
     */
    public AlarmDispatcher(Settings settings, MessageSender sender, AlarmLevel alarmLevel) {
        super(settings, sender);
        this.alarmLevel = Objects.requireNonNullElse(alarmLevel, AlarmLevel.ERROR);
    }

    /**
     * Delegates to #AlarmDispatcher(MessageSender, AlarmLevel) with a ERROR alarm level.
     *
     * @param sender   Used for sending the alarms.
     * @param settings The configuration.
     */
    public AlarmDispatcher(Settings settings, MessageSender sender) {
        this(settings, sender, AlarmLevel.ERROR);
    }

    /**
     * Send an alarm at warning-level.
     * If the settings does not have alarm level at 'warning', then a log is made instead.
     *
     * @param alarm The alarm to send.
     */
    public void warning(Alarm alarm) {
        ArgumentValidator.checkNotNull(alarm, "alarm");
        if (alarmLevel != AlarmLevel.WARNING) {
            log.debug("Will send a '" + AlarmLevel.WARNING + "' alarm, when the alarm level is '"
                    + alarmLevel + "'{}", alarm);
        } else {
            sendAlarm(alarm);
        }
    }

    /**
     * Send an alarm at error-level.
     * If the settings does not have alarm level at 'error', then a log is made instead.
     *
     * @param alarm The alarm to send.
     */
    public void error(Alarm alarm) {
        ArgumentValidator.checkNotNull(alarm, "alarm");
        if (alarmLevel == AlarmLevel.EMERGENCY) {
            log.debug("Cannot send a '" + AlarmLevel.ERROR + "' alarm, when the alarm level is '"
                    + alarmLevel + "'{}", alarm);
        } else {
            sendAlarm(alarm);
        }
    }

    /**
     * Send an alarm at emergency-level. At this level all alarms will be sent.
     *
     * @param alarm The alarm to send.
     */
    public void emergency(Alarm alarm) {
        ArgumentValidator.checkNotNull(alarm, "alarm");
        sendAlarm(alarm);
    }

    /**
     * Method for sending an Alarm when something bad happens.
     *
     * @param alarm The alarm to send to the destination for the alarm service.
     */
    protected void sendAlarm(Alarm alarm) {
        AlarmMessage message = new AlarmMessage();
        alarm.setAlarmRaiser(settings.getComponentID());
        alarm.setOrigDateTime(CalendarUtils.getNow());

        message.setAlarm(alarm);
        message.setCorrelationID(UUID.randomUUID().toString());
        message.setReplyTo(settings.getContributorDestinationID());
        message.setDestination(settings.getAlarmDestination());
        message.setCollectionID(alarm.getCollectionID());

        log.warn("Sending alarm: " + alarm);
        dispatchMessage(message);
    }
}