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

import org.bitrepository.bitrepositoryelements.ResultingStatus;

import javax.xml.datatype.XMLGregorianCalendar;

public class ComponentStatus {
    private int numberOfMissingReplies;
    private ComponentStatusCode status;
    private XMLGregorianCalendar lastReply;
    private String info;
    private Boolean alarmed;

    public ComponentStatus() {
        numberOfMissingReplies = 0;
        status = ComponentStatusCode.UNKNOWN;
        lastReply = null;
        info = "No status received yet.";
        alarmed = false;
    }

    /**
     * Update the status of a component with the given results.
     *
     * @param resultingStatus The status will be updated to the string value of the status code.
     */
    public void updateStatus(ResultingStatus resultingStatus) {
        numberOfMissingReplies = 0;
        status = ComponentStatusCode.valueOf(resultingStatus.getStatusInfo().getStatusCode().toString());
        lastReply = resultingStatus.getStatusTimestamp();
        info = resultingStatus.getStatusInfo().getStatusText();
        alarmed = false;
    }

    /**
     * Updates the number of missing replies by exactly one.
     */
    public void updateReplies() {
        numberOfMissingReplies++;
    }

    /**
     * Marks the component as unresponsive.
     */
    public void markAsUnresponsive() {
        status = ComponentStatusCode.UNRESPONSIVE;
    }

    /**
     * @return The number of missing replies.
     */
    public int getNumberOfMissingReplies() {
        return numberOfMissingReplies;
    }

    /**
     * @return The status code for the latest reply.
     */
    public ComponentStatusCode getStatus() {
        return status;
    }

    /**
     * @return The date for the latest reply.
     */
    public XMLGregorianCalendar getLastReplyDate() {
        return lastReply;
    }

    /**
     * @return The information message from the latest reply.
     */
    public String getInfo() {
        return info;
    }

    /**
     * @return Returns a {@link Boolean} indicating whether the component has invoked an alarm.
     */
    public Boolean hasAlarmed() {
        return alarmed;
    }

    public void alarmed() {
        alarmed = true;
    }
}
