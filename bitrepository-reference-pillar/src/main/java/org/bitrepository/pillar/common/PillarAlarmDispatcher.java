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
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * The class for dispatching alarms.
 */
public class PillarAlarmDispatcher extends AlarmDispatcher {
    /** The settings for this AlarmDispatcher.*/
    private final ContributorContext context;
    
    /**
     * Constructor.
     * @param context The context for the contributor for this alarm dispatcher.
     */
    public PillarAlarmDispatcher(ContributorContext context) {
        super(context, context.getSettings().getReferenceSettings().getPillarSettings().getAlarmLevel());
        this.context = context;
    }
    
    /**
     * Method for sending an alarm based on an IllegalArgumentException.
     * Is only send if the alarm level is 'WARNING', otherwise the exception is just logged.
     * @param exception The exception to base the alarm upon.
     */
    public void handleIllegalArgumentException(IllegalArgumentException exception) {
        ArgumentValidator.checkNotNull(exception, "IllegalArgumentException exception");
        
        // create a descriptor.
        Alarm ad = new Alarm();
        ad.setAlarmCode(AlarmCode.INCONSISTENT_REQUEST);
        ad.setAlarmText(exception.getMessage());
        ad.setAlarmRaiser(context.getSettings().getComponentID());
        ad.setOrigDateTime(CalendarUtils.getNow());
        
        warning(ad);
    }
    
    /**
     * Sends an alarm for a RuntimeException. Such exceptions are sent unless the AlarmLevel is 'EMERGENCY',
     * otherwise the exception is just logged.
     * @param exception The exception causing the alarm.
     */
    public void handleRuntimeExceptions(RuntimeException exception) {
        ArgumentValidator.checkNotNull(exception, "RuntimeException exception");
        
        // create a descriptor.
        Alarm alarm = new Alarm();
        alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
        alarm.setAlarmText(exception.toString());
        alarm.setAlarmRaiser(context.getSettings().getComponentID());
        alarm.setOrigDateTime(CalendarUtils.getNow());
        
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
        
        alarm.setAlarmRaiser(context.getSettings().getComponentID());
        alarm.setAlarmText(e.getResponseInfo().getResponseText());
        alarm.setOrigDateTime(CalendarUtils.getNow());
        
        if(e instanceof IllegalOperationException) {
            error(alarm);
        } else {
            warning(alarm);
        }
    }
}