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
import java.util.Collections;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Method for the ReferencePillar to keep track of audits.
 */
public class AuditTrailHandler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The map to keep the audit trails.*/
    private SortedMap<Date, String> auditTrails = Collections.synchronizedSortedMap(new TreeMap<Date, String>());

    /** Constructor.*/
    public AuditTrailHandler() {}

    /**
     * Inserts an audit trail into the map with the date 'now'. 
     * @param msg The audit trail to insert.
     */
    public synchronized void insertAudit(Object msg) {
        Date now = new Date();
        log.info("At '" + now + "' inserted audit: " + msg.toString());
        auditTrails.put(now, msg.toString());
    }
    
    /**
     * Inserts an audit trail into the map with the given date.
     * @param date The date of the audit trail.
     * @param msg The message to insert into the map.
     */
    public synchronized void insertAudit(Date date, Object msg) {
        log.info("At '" + date + "' inserted audit: " + msg.toString());
        auditTrails.put(date, msg.toString());
    }

    /**
     * Method for extracting all the audit trails.
     * @return The all audit trails.
     */
    public Collection<String> getAllAudits() {
        return auditTrails.values();
    }

    /**
     * Retrieves all the audit trails after a given date.
     * @param date The earliest date for the audit trails.
     * @return The list of audit trails after the date.
     */
    public Collection<String> getAuditsAfterDate(Date date) {
        return auditTrails.tailMap(date).values();
    }

    /**
     * Retrieves all the audit trails before a given date.
     * @param date The latest date for the audit trails.
     * @return The list of audit trails prior to the date.
     */
    public Collection<String> getAuditsBeforeDate(Date date) {
        return auditTrails.headMap(date).values();
    }
}
