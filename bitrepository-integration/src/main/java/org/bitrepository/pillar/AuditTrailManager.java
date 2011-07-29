/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar;

import java.util.Collection;
import java.util.Date;

/**
 * The interface for the audit trail handlers.
 */
public interface AuditTrailManager {
    /**
     * Inserts an audit trail into the map with the date 'now'. 
     * @param msg The audit trail to insert.
     */
    void addMessageReceivedAudit(Object msg);
    
    /**
     * Method for extracting all the audit trails.
     * @return The all audit trails.
     */
    public Collection<String> getAllAudits();
    
    /**
     * Retrieves all the audit trails after a given date.
     * @param date The earliest date for the audit trails.
     * @return The list of audit trails after the date.
     */
    public Collection<String> getAuditsAfterDate(Date date);
    
    /**
     * Retrieves all the audit trails before a given date.
     * @param date The latest date for the audit trails.
     * @return The list of audit trails prior to the date.
     */
    public Collection<String> getAuditsBeforeDate(Date date);
    
    /**
     * Retrieves all the audit trails between two given dates.
     * @param start The earliest date for the audit trails.
     * @param end The latest date for the audit trails.
     * @return The list of audit trails prior to the date.
     */
    public Collection<String> getAuditsBetweenDates(Date start, Date end);
}
