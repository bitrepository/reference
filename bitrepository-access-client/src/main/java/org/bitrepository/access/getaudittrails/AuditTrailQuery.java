/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getaudittrails;

/**
 * Encapsulates the information need to communicate with a Bit Repository component over the message bus.
 */
public class AuditTrailQuery {
    private final String componentID;
    private final Integer minSequenceNumber;
    private final Integer maxSequenceNumber;

    /**
     *
     * @param componentID
     */
    public AuditTrailQuery(String componentID) {
        this(componentID, null, null);
    }

    /**
     *
     * Queries for all Audit Trails with sequence number larger than minSequenceNumber.
     * @param minSequenceNumber
     * @param componentID
     */
    public AuditTrailQuery(String componentID, Integer minSequenceNumber) {
        this(componentID, minSequenceNumber, null);
    }
    /**
     *
     *
     * Queries for all Audit Trails with sequence number between minSequenceNumber and maxSequenceNumber.
     * @param minSequenceNumber
     * @param maxSequenceNumber
     * @param componentID
     */
    public AuditTrailQuery(String componentID, Integer minSequenceNumber, Integer maxSequenceNumber) {
        super();
        this.componentID = componentID;
        this.minSequenceNumber = minSequenceNumber;
        this.maxSequenceNumber = maxSequenceNumber;
    }
    
    /**
     * @return The componentID for the component.
     */
    public String getComponentID() {
        return componentID;
    }

    public Integer getMinSequenceNumber() {
        return minSequenceNumber;
    }

    public Integer getMaxSequenceNumber() {
        return maxSequenceNumber;
    }
}
