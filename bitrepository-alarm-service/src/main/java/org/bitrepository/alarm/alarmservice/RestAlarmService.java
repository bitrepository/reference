/*
 * #%L
 * Bitrepository Alarm Service
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.alarm.alarmservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.alarm.AlarmService;
import org.bitrepository.alarm.AlarmServiceFactory;
import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.utils.CalendarUtils;

@Path("/AlarmService")
public class RestAlarmService {
    private AlarmService alarmService;
    private CalendarUtils calendarUtils = CalendarUtils.getInstance(TimeZone.getDefault());
    
    public RestAlarmService() {
        alarmService = AlarmServiceFactory.getAlarmService();
       
    }
    
    /**
     * getShortAlarmList exposes the possibility of getting the list of the most recent alarms  
     * @return A string containing the data of the last alarms. 
     */
    @GET
    @Path("/getShortAlarmList/")
    @Produces("application/json")
    public List<Alarm> getShortAlarmList() {
        List<Alarm> alarmList = new ArrayList<>();
        alarmList.addAll(alarmService.extractAlarms(null, null, null, null, null, null, 10, false));
        return alarmList;
    }
    
    /**
     * getFullAlarmList exposes the possibility of getting the list of all alarms received   
     * @return A string containing the data of all alarms received. 
     */
    @GET
    @Path("/getFullAlarmList/")
    @Produces("application/json")
    public List<Alarm> getFullAlarmList() {
        List<Alarm> alarmList = new ArrayList<>();
        alarmList.addAll(alarmService.extractAlarms(null, null, null, null, null, null, null, true));
        return alarmList;
    }
       
    @POST
    @Path("/queryAlarms/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public List<WebAlarm> queryAlarms(
            @FormParam("fromDate") String fromDate,
            @FormParam("toDate") String toDate,
            @FormParam("fileID") String fileID,
            @FormParam("reportingComponent") String reportingComponent,
            @FormParam("alarmCode") String alarmCode,
            @FormParam("collectionID") String collectionID,
            @DefaultValue("10") @FormParam("maxAlarms") Integer maxAlarms,
            @DefaultValue("true") @FormParam ("oldestAlarmFirst") boolean oldestAlarmFirst) {
        Date from = calendarUtils.makeStartDateObject(fromDate);
        Date to = calendarUtils.makeEndDateObject(toDate);
        
        Collection<Alarm> alarms = alarmService.extractAlarms(contentOrNull(reportingComponent), makeAlarmCode(alarmCode), 
                from, to, contentOrNull(fileID), makeCollectionID(collectionID), maxAlarms, oldestAlarmFirst);
        
        return alarms.stream().map(a -> new WebAlarm(a)).collect(Collectors.toList());
    }
    
    @POST
    @Path("/queryAlarms/")
    @Consumes("application/json")
    @Produces("application/json")
    public List<Alarm> queryAlarms(AlarmServiceInput input) {
        Date from = calendarUtils.makeStartDateObject(input.getFromDate());
        Date to = calendarUtils.makeEndDateObject(input.getToDate());
       
        Collection<Alarm> alarms = alarmService.extractAlarms(contentOrNull(input.getReportingComponent()), makeAlarmCode(input.getAlarmCode()),
                from, to, contentOrNull(input.getFileID()), makeCollectionID(input.getCollectionID()), input.getMaxAlarms(), input.isOldestAlarmsFirst());
       
        return new ArrayList<>(alarms);
        
    }
    
    private String makeCollectionID(String collectionID) {
        if(collectionID.equals("ALL")) {
           return null;
        } else {
            return collectionID;
        }
    }
    
    private AlarmCode makeAlarmCode(String alarmCodeStr) {
        if(alarmCodeStr.equals("ALL")) {
            return null;
        } else {
            return AlarmCode.fromValue(alarmCodeStr);
        }
    } 
    
    private String contentOrNull(String input) {
        if(input != null && input.trim().isEmpty()) {
            return null;
        } else {
            return input.trim();
        }
    }
}
