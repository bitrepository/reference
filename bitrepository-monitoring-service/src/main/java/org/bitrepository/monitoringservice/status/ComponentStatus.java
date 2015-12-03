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
package org.bitrepository.monitoringservice.status;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ResultingStatus;

/**
 * Class to encapsulate the status of a component.  
 */
public class ComponentStatus {
    /** The number of missing replies.*/
    private int numberOfMissingReplies;
    /** The current status code.*/
    private ComponentStatusCode status;
    /** The date for the latest reply.*/
    private XMLGregorianCalendar lastReply;
    /** The status information of the latest reply.*/
    private String info;
    /** Indication whether an alarm has been sent due to the components status */
    private Boolean alarmed;
    
    
    /**
     * Constructor 
     */
    public ComponentStatus() {
        numberOfMissingReplies = 0;
        status = ComponentStatusCode.UNKNOWN;
        lastReply = null;
        info = "No status received yet.";
        alarmed = false;
    }
    
    /**
     * Update the status of a component with the given results.
     * @param resultingStatus FIXME
     */
    public void updateStatus(ResultingStatus resultingStatus) {
        numberOfMissingReplies = 0;
        status = ComponentStatusCode.valueOf(resultingStatus.getStatusInfo().getStatusCode().toString());
        lastReply = resultingStatus.getStatusTimestamp();
        info = resultingStatus.getStatusInfo().getStatusText();
        alarmed = false;
    }

    /**
     * Add another missing reply.
     */
    public void updateReplys() {
        numberOfMissingReplies++;
    }
    
    /**
     * Marks the component as unresponsive.
     */
    public void markAsUnresponsive() {
        status = ComponentStatusCode.UNRESPONSIVE;
    }
    
    /**
     * @return The number of missing replies in a row.
     */
    public int getNumberOfMissingReplies() {
        return numberOfMissingReplies;
    }

    /**
     * @return The code for the latest reply.
     */
    public ComponentStatusCode getStatus() {
        return status;
    }

    /**
     * @return The date for the latest reply.
     */
    public XMLGregorianCalendar getLastReply() {
        return lastReply;
    }

    /**
     * @return The latest status message.
     */
    public String getInfo() {
        return info;
    }    
    
    public Boolean hasAlarmed() {
    	return alarmed;
    }
    
    public void alarmed() {
    	alarmed = true;
    }
}
