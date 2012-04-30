/*
 * #%L
 * Bitrepository Monitoring Service
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
package org.bitrepository.monitoringservice;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.common.utils.CalendarUtils;

/**
 * Class to encapsulate the status of a component.  
 */
public class ComponentStatus {

    /**
     * Enumeration of possible status states for a component.  
     */
    public enum ComponentStatusCode {
        UNKNOWN,
        OK,
        WARNING,
        ERROR,
        UNRESPONSIVE;

        public String value() {
            return name();
        }

        public static ComponentStatusCode fromValue(String v) {
            return valueOf(v);
        }
    }
    
    private int numberOfMissingReplys;
    private ComponentStatusCode status;
    private XMLGregorianCalendar lastReply;
    private String info;
    
    /**
     * Constructor 
     */
    public ComponentStatus() {
        numberOfMissingReplys = 0;
        status = ComponentStatusCode.UNKNOWN;
        lastReply = CalendarUtils.getEpoch();
        info = "No status requested yet.";
    }
    
    /**
     * Update the status of a component 
     */
    public void updateStatus(ResultingStatus resultingStatus) {
        numberOfMissingReplys = 0;
        status = ComponentStatusCode.fromValue(resultingStatus.getStatusInfo().getStatusCode().toString());
        lastReply = resultingStatus.getStatusTimestamp();
        info = resultingStatus.getStatusInfo().getStatusText();
    }

    public void updateReplys() {
        numberOfMissingReplys++;
    }
    
    public void markAsUnresponsive() {
        status = ComponentStatusCode.UNRESPONSIVE;
    }
    
    public int getNumberOfMissingReplys() {
        return numberOfMissingReplys;
    }

    public ComponentStatusCode getStatus() {
        return status;
    }

    public XMLGregorianCalendar getLastReply() {
        return lastReply;
    }

    public String getInfo() {
        return info;
    }
    
    
}
