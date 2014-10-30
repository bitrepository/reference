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
package org.bitrepository.pillar.common;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * The class for dispatching alarms.
 */
public class PillarAlarmDispatcher extends AlarmDispatcher {
    
    /**
     * @param settings The settings for this alarm dispatcher.
     * @param sender The sender for this alarm dispatcher.
     */
    public PillarAlarmDispatcher(Settings settings, MessageSender sender) {
        super(settings, sender, settings.getReferenceSettings().getPillarSettings().getAlarmLevel());
    }
    
    /**
     * Method for sending an alarm based on an IllegalArgumentException.
     * Is only send if the alarm level is 'WARNING', otherwise the exception is just logged.
     * @param exception The exception to base the alarm upon.
     */
    public void handleIllegalArgumentException(IllegalArgumentException exception) {
        ArgumentValidator.checkNotNull(exception, "IllegalArgumentException exception");

        Alarm ad = new Alarm();
        ad.setAlarmCode(AlarmCode.INCONSISTENT_REQUEST);
        ad.setAlarmText(exception.getMessage());
        
        warning(ad);
    }

    /**
     * Sends an alarm for a RuntimeException. Such exceptions are sent unless the AlarmLevel is 'EMERGENCY',
     * otherwise the exception is just logged.
     * @param exception The exception causing the alarm.
     */
    public void handleRuntimeExceptions(RuntimeException exception) {
        ArgumentValidator.checkNotNull(exception, "RuntimeException exception");

        Alarm alarm = new Alarm();
        alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
        alarm.setAlarmText(exception.toString());

        error(alarm);
    }

    /**
     * Handles the case when the request causes a RequestHandlerException.
     * @param e The exception causing this alarm case.
     */
    public void handleRequestException(RequestHandlerException e) {
        Alarm alarm = new Alarm();
        
        if(e.getResponseInfo().getResponseCode() == ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE) {
            alarm.setAlarmCode(AlarmCode.INVALID_MESSAGE);
        } else if(e.getResponseInfo().getResponseCode() == ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE
                || e.getResponseInfo().getResponseCode() == ResponseCode.NEW_FILE_CHECKSUM_FAILURE) {
            alarm.setAlarmCode(AlarmCode.CHECKSUM_ALARM);
        } else {
            alarm.setAlarmCode(AlarmCode.FAILED_OPERATION);
        }

        alarm.setAlarmText(e.getResponseInfo().getResponseText());
        alarm.setCollectionID(e.getCollectionId());
        
        if(e instanceof IllegalOperationException) {
            IllegalOperationException ioe = (IllegalOperationException) e;
            alarm.setFileID(ioe.getFileId());
            error(alarm);
        } else {
            warning(alarm);
        }
    }
}