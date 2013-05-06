/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails;

import org.bitrepository.audittrails.preserver.AuditTrailPreserver;

public class MockAuditPreserver implements AuditTrailPreserver {
    
    private int callsToStart = 0;
    @Override
    public void start() {
        callsToStart++;
    }
    public int getCallsToStart() {
        return callsToStart;
    }
    
    private int callsToPreserveAuditTrailsNow = 0;
    @Override
    public void preserveRepositoryAuditTrails() {
        callsToPreserveAuditTrailsNow++;
    }
    public int getCallsToPreserveAuditTrailsNow() {
        return callsToPreserveAuditTrailsNow;
    }
    
    private int callsToClose = 0;
    @Override
    public void close() {
        callsToClose++;
    }
    public int getCallsToClose() {
        return callsToClose;
    }
    
}
