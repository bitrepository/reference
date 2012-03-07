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

import java.util.concurrent.ArrayBlockingQueue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.alarm.AlarmStore;
import org.bitrepository.alarm.AlarmStoreDataItem;
import org.bitrepository.alarm.AlarmStoreFactory;


@Path("/AlarmService")
public class RestAlarmService {
    private AlarmStore alarmStore;
    
    public RestAlarmService() {
        alarmStore = AlarmStoreFactory.getAlarmStore();
    }
    
    /**
     * getShortAlarmList exposes the possibility of getting the list of the most recent alarms  
     * @return A string containing the data of the last alarms. 
     */
    @GET
    @Path("/getShortAlarmList/")
    @Produces("text/html")
    public String getShortAlarmList() {
    	ArrayBlockingQueue<AlarmStoreDataItem> alarmList = alarmStore.getShortList();

		StringBuilder sb = new StringBuilder();
		sb.append("<table id=\"users\" class=\"ui-widget ui-widget-content\">\n");
		sb.append("<thead>\n");
		sb.append("<tr class=\"ui-widget-header\">\n");
		sb.append("<th width=\"70\">Date</th>\n");
		sb.append("<th width=\"80\">Raiser</th>\n");
		sb.append("<th width=\"80\">Alarm code</th>\n");
		sb.append("<th>Description</th>\n");
		sb.append("</tr>\n");
		sb.append("</thead>\n");
		sb.append("<tbody>\n");
		for(AlarmStoreDataItem item : alarmList) {
			sb.append(item.toString());	
			sb.append("\n");
		}
		sb.append("</tbody>\n");
		sb.append("</table>\n");
		return sb.toString();
    }
    
    /**
     * getFullAlarmList exposes the possibility of getting the list of all alarms received   
     * @return A string containing the data of all alarms received. 
     */
    @GET
    @Path("/getFullAlarmList/")
    @Produces("text/html")
    public String getFullAlarmList() {
        return alarmStore.getFullList();     
    }
}