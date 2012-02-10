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
package org.bitrepository.access.audittrails.client;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.access.audittrails.ComponentDestination;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;

/**
 * Handles the retrieval of from contributors and storage of the audit trails into the AuditTrail storage.
 */
public class AuditTrailClient {
    
    public AuditTrailEvent[] getAuditTrails(ComponentDestination contributorDestination, XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url) {
        return null;
    }
    
    public AuditTrailEvent[] getAuditTrailsFromContributor(ComponentDestination contributorDestination, Integer minSequenceNumber, Integer maxSequenceNumber, String url) {
        return null;
    }
}
