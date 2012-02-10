/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.store;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;

public class SimpleStore implements AuditTrailStore{
    private Set<AuditTrailEvent> auditTrails = 
            new TreeSet<AuditTrailEvent>();
    
    @Override
    public AuditTrailEvent[] getAuditTrails(XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url) {
        return (AuditTrailEvent[])auditTrails.toArray(new AuditTrailEvent[auditTrails.size()]);
    }

    @Override
    public void addAuditTrails(AuditTrailEvents newAuditTrails) {
        for (AuditTrailEvent event : newAuditTrails.getAuditTrailEvent()) {
            auditTrails.add(event);
        }
    }
}
