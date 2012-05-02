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
package org.bitrepository.alarm;

import java.util.Collection;
import java.util.Date;

import javax.jms.JMSException;

import org.bitrepository.alarm.handling.AlarmHandler;
import org.bitrepository.alarm.handling.AlarmMediator;
import org.bitrepository.alarm.store.AlarmStore;
import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.contributor.ContributorMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic alarm service
 */
public class BasicAlarmService implements AlarmService {
    /** The logger for the AlarmService.*/
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /** The messagebus for the communication.*/
    private final MessageBus messageBus;
    
    /** The store for the alarms.*/
    private final AlarmStore store;
    /** The mediator for handling the messages.*/
    private final ContributorMediator contributorMediator;

    /** The conversation mediator to keep track of the conversations.*/
    private final AlarmMediator alarmMediator;

    /**
     * Constructor.
     * @param messageBus The message bus.
     * @param settings The settings.
     * @param store The store for the alarms.
     * @param contributorMediator The contributor
     */
    public BasicAlarmService(MessageBus messageBus, Settings settings, AlarmStore store, ContributorMediator contributorMediator) {
        this.messageBus = messageBus;
        this.store = store;
        this.contributorMediator = contributorMediator;
        
        contributorMediator.start();
        alarmMediator = new AlarmMediator(messageBus, settings.getAlarmDestination());
    }

    @Override
    public void addHandler(AlarmHandler handler) {
        log.info("Adding handler '" + handler.getClass().getName() + "' for alarms.");
        alarmMediator.addHandler(handler);
    }
    
    @Override
    public void shutdown() {
        if(alarmMediator != null) {
            alarmMediator.close();
        }
        if(contributorMediator != null) {
            contributorMediator.close();
        }
        try {
            messageBus.close();
            // TODO Kill any lingering timer threads
        } catch (JMSException e) {
            log.info("Error during shutdown of messagebus ", e);
        }
    }

    @Override
    public void start() {}

    @Override
    public Collection<Alarm> extractAlarms(String componentID, AlarmCode alarmCode, Date minDate, Date maxDate,
            String fileID) {
        return store.extractAlarms(componentID, alarmCode, minDate, maxDate, fileID);
    }
}
