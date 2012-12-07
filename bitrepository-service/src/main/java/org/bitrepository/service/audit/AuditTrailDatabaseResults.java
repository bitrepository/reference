/*
 * #%L
 * Bitrepository Service
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
package org.bitrepository.service.audit;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for the results of an extract of the AuditTrail database.
 * Contains both the audit trails and whether more results was found.
 */
public class AuditTrailDatabaseResults {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The collection of audit trail events.*/
    private final AuditTrailEvents events;
    /** Whether more results has been reported.*/ 
    private boolean hasMoreResults;
    
    /**
     * Constructor.
     */
    public AuditTrailDatabaseResults() {
        events = new AuditTrailEvents();
        hasMoreResults = false;
    }
    
    /**
     * @return The entity for containing the audit trail events.
     */
    public AuditTrailEvents getAuditTrailEvents() {
        return events;
    }
    
    /**
     * Adding an audit trail event to this result set.
     * @param event The audit trail event to add.
     */
    public void addAuditTrailEvent(AuditTrailEvent event) {
        log.trace("Adding audit trail event to results: {}", event);
        events.getAuditTrailEvent().add(event);
    }
    
    /**
     * @return Whether more results has been reported.
     */
    public boolean moreResults() {
        return hasMoreResults;
    }
    
    /**
     * Sets that more results has been found.
     */
    public void reportMoreResultsFound() {
        hasMoreResults = true;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", contains " + events.getAuditTrailEvent().size() 
                + " audit trail events, and " + (hasMoreResults ? "has more results" : "does not have more results");
    }
}
